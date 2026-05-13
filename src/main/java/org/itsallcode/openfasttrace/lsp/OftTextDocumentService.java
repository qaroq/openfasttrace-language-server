// SPDX-License-Identifier: GPL-3.0-or-later
package org.itsallcode.openfasttrace.lsp;

import java.util.logging.Logger;

import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

public class OftTextDocumentService implements TextDocumentService {

    private static final Logger LOG = Logger.getLogger(OftTextDocumentService.class.getName());

    @SuppressWarnings("unused")
    private LanguageClient client;

    void connect(final LanguageClient client) {
        this.client = client;
    }

    @Override
    public void didOpen(final DidOpenTextDocumentParams params) {
        LOG.fine("didOpen: " + params.getTextDocument().getUri());
    }

    @Override
    public void didChange(final DidChangeTextDocumentParams params) {
        // Index is not refreshed on every keystroke (ADR-0006).
    }

    @Override
    public void didClose(final DidCloseTextDocumentParams params) {
        LOG.fine("didClose: " + params.getTextDocument().getUri());
    }

    // [impl->req~index-refresh-on-save~1]
    @Override
    public void didSave(final DidSaveTextDocumentParams params) {
        LOG.fine("didSave: " + params.getTextDocument().getUri());
        // TODO Phase 2: trigger debounced re-index
    }
}
