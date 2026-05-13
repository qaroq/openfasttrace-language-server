// SPDX-License-Identifier: GPL-3.0-or-later
package org.itsallcode.openfasttrace.lsp;

import java.util.logging.Logger;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.itsallcode.openfasttrace.lsp.index.OftWorkspaceIndex;

public class OftWorkspaceService implements WorkspaceService {

    private static final Logger LOG = Logger.getLogger(OftWorkspaceService.class.getName());

    @SuppressWarnings("unused")
    private LanguageClient client;
    private volatile OftWorkspaceIndex index = OftWorkspaceIndex.empty();

    void updateIndex(final OftWorkspaceIndex index) {
        this.index = index;
    }

    void connect(final LanguageClient client) {
        this.client = client;
    }

    @Override
    public void didChangeConfiguration(final DidChangeConfigurationParams params) {
        LOG.fine("didChangeConfiguration");
    }

    // [impl->req~index-refresh-on-save~1]
    @Override
    public void didChangeWatchedFiles(final DidChangeWatchedFilesParams params) {
        LOG.fine("didChangeWatchedFiles: " + params.getChanges().size() + " change(s)");
        // TODO Phase 2: trigger debounced re-index
    }
}
