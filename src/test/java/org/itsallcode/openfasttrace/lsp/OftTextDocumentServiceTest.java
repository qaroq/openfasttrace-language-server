// SPDX-License-Identifier: GPL-3.0-or-later
package org.itsallcode.openfasttrace.lsp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
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
}
