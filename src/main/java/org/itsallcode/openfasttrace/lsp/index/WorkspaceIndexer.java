// SPDX-License-Identifier: GPL-3.0-or-later
package org.itsallcode.openfasttrace.lsp.index;

import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import org.itsallcode.openfasttrace.api.core.SpecificationItem;
import org.itsallcode.openfasttrace.api.importer.ImportSettings;
import org.itsallcode.openfasttrace.core.OftRunner;

// [impl->req~index-on-startup~1]
// [impl->req~index-refresh-on-save~1]
public class WorkspaceIndexer {

    private static final Logger LOG = Logger.getLogger(WorkspaceIndexer.class.getName());

    private final OftRunner runner;

    public WorkspaceIndexer() {
        this(new OftRunner());
    }

    WorkspaceIndexer(final OftRunner runner) {
        this.runner = runner;
    }

    public OftWorkspaceIndex buildIndex(final Path workspaceRoot) {
        LOG.info("Indexing workspace: " + workspaceRoot);
        final ImportSettings settings = ImportSettings.builder()
                .addInputs(workspaceRoot)
                .build();
        final List<SpecificationItem> items = runner.importItems(settings);
        LOG.info("Indexed " + items.size() + " specification item(s)");
        return new OftWorkspaceIndex(items);
    }
}
