// SPDX-License-Identifier: GPL-3.0-or-later
package org.itsallcode.openfasttrace.lsp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.itsallcode.openfasttrace.api.core.SpecificationItem;
import org.itsallcode.openfasttrace.api.core.SpecificationItemId;
import org.itsallcode.openfasttrace.lsp.index.OftWorkspaceIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OftTextDocumentServiceTest {

    private OftTextDocumentService service;

    @BeforeEach
    void setUp() {
        service = new OftTextDocumentService();
    }

    // [utest->req~hover-title-and-description~1]
    @Test
    void hover_onCoverageTag_returnsSpecItemTitleAndDescription()
            throws ExecutionException, InterruptedException {
        final var specItem = SpecificationItem.builder()
                .id(SpecificationItemId.parseId("req~my-req~1"))
                .title("My Requirement")
                .description("This is the description.")
                .build();
        service.updateIndex(new OftWorkspaceIndex(List.of(specItem)));

        // Position is on "req~my-req~1" in the line "Covers: req~my-req~1"
        final var params = new HoverParams(
                new TextDocumentIdentifier("file:///any.md"),
                new Position(0, 9));  // col 9 is inside "req~my-req~1"

        // Simulate the line content via a document snapshot — hover reads
        // from the actual file, so we test via a line that contains the ID.
        // For a unit test we use the line resolver directly.
        final String line = "Covers: req~my-req~1";
        final var result = service.hoverForLine(line, params.getPosition().getCharacter(),
                params.getPosition().getLine());

        assertThat(result).isPresent();
        final String hoverText = result.get().getContents().getRight().getValue();
        assertThat(hoverText)
                .contains("My Requirement")
                .contains("This is the description.");
    }

    // [utest->req~hover-title-and-description~1]
    @Test
    void hover_noIdAtPosition_returnsEmpty() throws ExecutionException, InterruptedException {
        service.updateIndex(OftWorkspaceIndex.empty());

        final var result = service.hoverForLine("plain text", 0, 0);

        assertThat(result).isEmpty();
    }

    // [utest->req~hover-title-and-description~1]
    @Test
    void hover_unknownId_returnsEmpty() throws ExecutionException, InterruptedException {
        service.updateIndex(OftWorkspaceIndex.empty());

        final var result = service.hoverForLine("req~unknown~1", 0, 0);

        assertThat(result).isEmpty();
    }

    // [utest->req~hover-title-and-description~1]
    @Test
    void hover_onSourceCodeCoverageTag_returnsSpecItemInfo() {
        final var specItem = SpecificationItem.builder()
                .id(SpecificationItemId.parseId("req~my-req~1"))
                .title("My Requirement")
                .description("Desc.")
                .build();
        service.updateIndex(new OftWorkspaceIndex(List.of(specItem)));

        // Java-style coverage tag in a source code comment
        final String line = "// [impl->req~my-req~1]";
        final int col = 12; // cursor inside "req~my-req~1"
        final var result = service.hoverForLine(line, col, 0);

        assertThat(result).isPresent();
        assertThat(result.get().getContents().getRight().getValue())
                .contains("My Requirement");
    }

    // [utest->req~index-refresh-on-save~1]
    @Test
    void updateIndex_republishesDiagnosticsForAllOpenFiles()
            throws ExecutionException, InterruptedException {
        final LanguageClient client = mock(LanguageClient.class);
        final List<PublishDiagnosticsParams> published = new ArrayList<>();
        doAnswer(inv -> { published.add(inv.getArgument(0)); return null; })
                .when(client).publishDiagnostics(any());

        service.connect(client);
        service.didOpen(openParams("file:///spec.md"));
        service.didOpen(openParams("file:///source.java"));
        published.clear();

        // Simulate index rebuild arriving after a save
        service.updateIndex(OftWorkspaceIndex.empty());

        assertThat(published).extracting(PublishDiagnosticsParams::getUri)
                .containsExactlyInAnyOrder("file:///spec.md", "file:///source.java");
    }

    // [utest->req~index-refresh-on-save~1]
    @Test
    void didClose_removesFileFromOpenSet() throws Exception {
        final LanguageClient client = mock(LanguageClient.class);
        service.connect(client);
        service.didOpen(openParams("file:///spec.md"));
        service.didClose(closeParams("file:///spec.md"));

        reset(client);
        service.updateIndex(OftWorkspaceIndex.empty());

        verify(client, never()).publishDiagnostics(any());
    }

    private static DidOpenTextDocumentParams openParams(final String uri) {
        final var item = new TextDocumentItem(uri, "plaintext", 1, "");
        return new DidOpenTextDocumentParams(item);
    }

    private static DidCloseTextDocumentParams closeParams(final String uri) {
        return new DidCloseTextDocumentParams(new TextDocumentIdentifier(uri));
    }
}
