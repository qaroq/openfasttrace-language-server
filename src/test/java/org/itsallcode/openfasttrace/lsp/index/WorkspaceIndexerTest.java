// SPDX-License-Identifier: GPL-3.0-or-later
package org.itsallcode.openfasttrace.lsp.index;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.itsallcode.openfasttrace.api.core.SpecificationItemId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class WorkspaceIndexerTest {

    private WorkspaceIndexer indexer;

    @BeforeEach
    void setUp() {
        indexer = new WorkspaceIndexer();
    }

    private Path testWorkspacePath() throws URISyntaxException {
        return Paths.get(getClass().getClassLoader().getResource("testworkspace").toURI());
    }

    // [utest->req~index-on-startup~1]
    @Test
    void buildIndex_loadsSpecItems() throws URISyntaxException {
        final var index = indexer.buildIndex(testWorkspacePath());

        final var id = SpecificationItemId.parseId("feat~test-feature~1");
        assertThat(index.findSpecItem(id)).isPresent();
    }

    // [utest->req~index-on-startup~1]
    @Test
    void buildIndex_specItemHasTitleAndDescription() throws URISyntaxException {
        final var index = indexer.buildIndex(testWorkspacePath());

        final var id = SpecificationItemId.parseId("feat~test-feature~1");
        final var item = index.findSpecItem(id).orElseThrow();

        assertThat(item.getTitle()).isEqualTo("Test Feature");
        assertThat(item.getDescription()).contains("testing the workspace indexer");
    }

    // [utest->req~index-on-startup~1]
    @Test
    void buildIndex_indexesCoverageTags() throws URISyntaxException {
        final var index = indexer.buildIndex(testWorkspacePath());

        final var coveredId = SpecificationItemId.parseId("req~test-requirement~2");
        final var tags = index.findCoverageTags(coveredId);

        assertThat(tags).hasSize(1);
        assertThat(tags.get(0).getId().getArtifactType()).isEqualTo("impl");
    }

    // [utest->req~index-on-startup~1]
    @Test
    void buildIndex_emptyDirectory_returnsEmptyIndex(@TempDir final Path emptyDir) {
        final var index = indexer.buildIndex(emptyDir);

        assertThat(index.specItemCount()).isZero();
    }
}
