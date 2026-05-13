// SPDX-License-Identifier: GPL-3.0-or-later
package org.itsallcode.openfasttrace.lsp;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ServerInfo;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

// [impl->req~index-on-startup~1]
public class OftLanguageServer implements LanguageServer, LanguageClientAware {

    private static final Logger LOG = Logger.getLogger(OftLanguageServer.class.getName());

    private final OftTextDocumentService textDocumentService = new OftTextDocumentService();
    private final OftWorkspaceService workspaceService = new OftWorkspaceService();

    @Override
    public CompletableFuture<InitializeResult> initialize(final InitializeParams params) {
        LOG.info("initialize: rootUri=" + params.getRootUri());

        final var capabilities = new ServerCapabilities();
        // Capabilities will be declared here as features are implemented in later phases.

        final var serverInfo = new ServerInfo("OpenFastTrace Language Server", "0.1.0-SNAPSHOT");
        return CompletableFuture.completedFuture(new InitializeResult(capabilities, serverInfo));
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        LOG.info("shutdown requested");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void exit() {
        LOG.info("exit");
        System.exit(0);
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return workspaceService;
    }

    @Override
    public void connect(final LanguageClient client) {
        LOG.info("client connected");
        textDocumentService.connect(client);
        workspaceService.connect(client);
    }
}
