// SPDX-License-Identifier: GPL-3.0-or-later
package org.itsallcode.openfasttrace.lsp;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OftLanguageServerTest {

    private OftLanguageServer server;

    @BeforeEach
    void setUp() {
        server = new OftLanguageServer();
    }

    @Test
    void initializeReturnsServerInfo() throws Exception {
        final var params = new InitializeParams();
        params.setRootUri("file:///workspace");

        final InitializeResult result = server.initialize(params).get();

        assertThat(result.getServerInfo().getName())
                .isEqualTo("OpenFastTrace Language Server");
    }

    @Test
    void initializeReturnsCapabilities() throws Exception {
        final var params = new InitializeParams();

        final InitializeResult result = server.initialize(params).get();

        assertThat(result.getCapabilities()).isNotNull();
    }

    @Test
    void shutdownCompletesWithoutError() throws Exception {
        final Object result = server.shutdown().get();
        assertThat(result).isNull();
    }
}
