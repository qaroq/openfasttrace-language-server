// SPDX-License-Identifier: GPL-3.0-or-later
package org.itsallcode.openfasttrace.lsp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.itsallcode.openfasttrace.api.core.SpecificationItem;
import org.itsallcode.openfasttrace.api.core.SpecificationItemId;
import org.itsallcode.openfasttrace.lsp.index.OftWorkspaceIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OftTextDocumentServiceFindReferencesTest {

    private OftTextDocumentService service;

    @BeforeEach
    void setUp() {
        service = new OftTextDocumentService();
    }

    // [utest->req~find-references-covering-tags~1]
    @Test
    void references_onSpecItem_returnsAllCoverageTags() throws Exception {
        final var specItem = specItemAt("req~my-req~1", "/workspace/spec.md", 5);
        final var tag1 = coverageTagAt("impl~mod-a~0", "req~my-req~1", "/workspace/a.md", 2);
        final var tag2 = coverageTagAt("impl~mod-b~0", "req~my-req~1", "/workspace/b.md", 8);
        service.updateIndex(new OftWorkspaceIndex(List.of(specItem, tag1, tag2)));

        final var params = new ReferenceParams(
                new TextDocumentIdentifier("file:///workspace/spec.md"),
                new Position(0, 5),
                new ReferenceContext(false));

        final var result = service.referencesForLine("`req~my-req~1`", 5);

        assertThat(result).hasSize(2);
        final var uris = result.stream().map(Location::getUri).toList();
        assertThat(uris).containsExactlyInAnyOrder(
                "file:///workspace/a.md", "file:///workspace/b.md");
    }

    // [utest->req~find-references-covering-tags~1]
    @Test
    void references_unknownId_returnsEmpty() {
        service.updateIndex(OftWorkspaceIndex.empty());

        final var result = service.referencesForLine("req~unknown~1", 0);

        assertThat(result).isEmpty();
    }

    // [utest->req~find-references-covering-tags~1]
    @Test
    void references_noCoverageTags_returnsEmpty() {
        final var specItem = specItemAt("req~lonely~1", "/workspace/spec.md", 3);
        service.updateIndex(new OftWorkspaceIndex(List.of(specItem)));

        final var result = service.referencesForLine("`req~lonely~1`", 1);

        assertThat(result).isEmpty();
    }

    private SpecificationItem specItemAt(final String id, final String path, final int line) {
        return SpecificationItem.builder()
                .id(SpecificationItemId.parseId(id))
                .title("Title")
                .description("Desc")
                .location(org.itsallcode.openfasttrace.api.core.Location.create(path, line + 1))
                .build();
    }

    private SpecificationItem coverageTagAt(final String tagId, final String coveredId,
            final String path, final int line) {
        return SpecificationItem.builder()
                .id(SpecificationItemId.parseId(tagId))
                .title("")
                .description("")
                .location(org.itsallcode.openfasttrace.api.core.Location.create(path, line + 1))
                .addCoveredId(SpecificationItemId.parseId(coveredId))
                .build();
    }
}
