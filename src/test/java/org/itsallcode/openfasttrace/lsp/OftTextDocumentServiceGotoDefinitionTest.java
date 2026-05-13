// SPDX-License-Identifier: GPL-3.0-or-later
package org.itsallcode.openfasttrace.lsp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.lsp4j.Location;
import org.itsallcode.openfasttrace.api.core.SpecificationItem;
import org.itsallcode.openfasttrace.api.core.SpecificationItemId;
import org.itsallcode.openfasttrace.lsp.index.OftWorkspaceIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OftTextDocumentServiceGotoDefinitionTest {

    private static final String SPEC_URI = "file:///workspace/spec.md";
    private static final String IMPL_URI = "file:///workspace/impl.md";

    private OftTextDocumentService service;

    @BeforeEach
    void setUp() {
        service = new OftTextDocumentService();
    }

    // [utest->req~goto-definition-tag-to-spec~1]
    @Test
    void definition_cursorOnCoverageTagInImplFile_returnsSpecItemLocation()
            throws ExecutionException, InterruptedException {
        final var specItem = specItemAt("req~my-req~1", "/workspace/spec.md", 9);
        final var coverageTag = coverageTagAt("impl~impl~0", "req~my-req~1",
                "/workspace/impl.md", 5);
        service.updateIndex(new OftWorkspaceIndex(List.of(specItem, coverageTag)));

        // Cursor is in impl.md on "req~my-req~1" → jump to spec item
        final var locations = service.definitionForLine("Covers: req~my-req~1", 9, IMPL_URI);

        assertThat(locations).hasSize(1);
        assertThat(locations.get(0).getUri()).isEqualTo(SPEC_URI);
        assertThat(locations.get(0).getRange().getStart().getLine()).isEqualTo(9);
    }

    // [utest->req~goto-definition-spec-to-tags~1]
    @Test
    void definition_cursorOnSpecItemIdInSpecFile_returnsAllCoveringTagLocations()
            throws ExecutionException, InterruptedException {
        final var specItem = specItemAt("req~my-req~1", "/workspace/spec.md", 10);
        final var tag1 = coverageTagAt("impl~mod-a~0", "req~my-req~1", "/workspace/a.md", 3);
        final var tag2 = coverageTagAt("impl~mod-b~0", "req~my-req~1", "/workspace/b.md", 7);
        service.updateIndex(new OftWorkspaceIndex(List.of(specItem, tag1, tag2)));

        // Cursor is in spec.md on its own ID → jump to all coverage tags
        final var locations = service.definitionForLine("`req~my-req~1`", 3, SPEC_URI);

        assertThat(locations).hasSize(2);
        final var uris = locations.stream().map(Location::getUri).toList();
        assertThat(uris).containsExactlyInAnyOrder(
                "file:///workspace/a.md", "file:///workspace/b.md");
    }

    // [utest->req~goto-definition-tag-to-spec~1]
    @Test
    void definition_unknownId_returnsEmpty() {
        service.updateIndex(OftWorkspaceIndex.empty());

        final var locations = service.definitionForLine("req~unknown~1", 0, IMPL_URI);

        assertThat(locations).isEmpty();
    }

    private SpecificationItem specItemAt(final String id, final String path, final int line) {
        return SpecificationItem.builder()
                .id(SpecificationItemId.parseId(id))
                .title("Title of " + id)
                .description("Description.")
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
