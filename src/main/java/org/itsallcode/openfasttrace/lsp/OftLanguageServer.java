// SPDX-License-Identifier: GPL-3.0-or-later
package org.itsallcode.openfasttrace.lsp;

import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ServerInfo;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.itsallcode.openfasttrace.lsp.index.OftWorkspaceIndex;
import org.itsallcode.openfasttrace.lsp.index.WorkspaceIndexer;

public class OftLanguageServer implements LanguageServer, LanguageClientAware {

    private static final Logger LOG = Logger.getLogger(OftLanguageServer.class.getName());

    private final WorkspaceIndexer indexer;
    private final OftTextDocumentService textDocumentService;
    private final OftWorkspaceService workspaceService;

    private String rootUri;

    public OftLanguageServer() {
        this(new WorkspaceIndexer());
    }

    OftLanguageServer(final WorkspaceIndexer indexer) {
        this.indexer = indexer;
        this.textDocumentService = new OftTextDocumentService();
        this.workspaceService = new OftWorkspaceService();
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(final InitializeParams params) {
        this.rootUri = params.getRootUri();
        LOG.info("initialize: rootUri=" + rootUri);

        final var capabilities = new ServerCapabilities();
        capabilities.setHoverProvider(true);
        capabilities.setDefinitionProvider(true);
        capabilities.setReferencesProvider(true);

        final var serverInfo = new ServerInfo("OpenFastTrace Language Server", "0.1.0-SNAPSHOT");
        return CompletableFuture.completedFuture(new InitializeResult(capabilities, serverInfo));
    }

    // [impl->req~index-on-startup~1]
    @Override
    public void initialized(final InitializedParams params) {
        LOG.info("initialized — building workspace index");
        if (rootUri == null) {
            LOG.warning("No rootUri available, skipping index");
            return;
        }
        final Path workspaceRoot = Path.of(URI.create(rootUri));
        final OftWorkspaceIndex index = indexer.buildIndex(workspaceRoot);
        textDocumentService.updateIndex(index);
        workspaceService.updateIndex(index);
        LOG.info("Workspace index ready: " + index.specItemCount() + " item(s)");
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
