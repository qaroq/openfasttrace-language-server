// SPDX-License-Identifier: GPL-3.0-or-later
package org.itsallcode.openfasttrace.lsp;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * Entry point: wires stdin/stdout to the LSP server and starts listening.
 */
public class ServerLauncher {

    private static final Logger LOG = Logger.getLogger(ServerLauncher.class.getName());

    public static void main(final String[] args) throws InterruptedException, ExecutionException {
        LOG.info("Starting OpenFastTrace Language Server");
        launch(System.in, System.out);
    }

    static void launch(final InputStream in, final OutputStream out)
            throws InterruptedException, ExecutionException {
        final var server = new OftLanguageServer();
        final var launcher = LSPLauncher.createServerLauncher(server, in, out);
        final LanguageClient client = launcher.getRemoteProxy();
        server.connect(client);
        launcher.startListening().get();
    }
}
