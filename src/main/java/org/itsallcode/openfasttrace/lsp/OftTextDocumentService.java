// SPDX-License-Identifier: GPL-3.0-or-later
package org.itsallcode.openfasttrace.lsp;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.itsallcode.openfasttrace.api.core.SpecificationItem;
import org.itsallcode.openfasttrace.lsp.index.OftIdAtPosition;
import org.itsallcode.openfasttrace.lsp.index.OftWorkspaceIndex;

public class OftTextDocumentService implements TextDocumentService {

    private static final Logger LOG = Logger.getLogger(OftTextDocumentService.class.getName());

    @SuppressWarnings("unused")
    private LanguageClient client;
    private volatile OftWorkspaceIndex index = OftWorkspaceIndex.empty();

    void updateIndex(final OftWorkspaceIndex index) {
        this.index = index;
    }

    void connect(final LanguageClient client) {
        this.client = client;
    }

    // [impl->req~hover-title-and-description~1]
    @Override
    public CompletableFuture<Hover> hover(final HoverParams params) {
        final String uri = params.getTextDocument().getUri();
        final int line = params.getPosition().getLine();
        final int col = params.getPosition().getCharacter();
        LOG.fine("hover: uri=" + uri + " line=" + line + " col=" + col);
        return CompletableFuture.supplyAsync(() -> {
            final String lineText = readLine(uri, line);
            return hoverForLine(lineText, col, line).orElse(null);
        });
    }

    Optional<Hover> hoverForLine(final String lineText, final int col, final int line) {
        return OftIdAtPosition.findAt(lineText, col)
                .flatMap(id -> index.findSpecItem(id))
                .map(this::toHover);
    }

    private Hover toHover(final SpecificationItem item) {
        final String markdown = "**" + item.getTitle() + "**\n\n" + item.getDescription();
        final var content = new MarkupContent(MarkupKind.MARKDOWN, markdown);
        return new Hover(content);
    }

    private String readLine(final String uri, final int lineIndex) {
        try {
            final List<String> lines = Files.readAllLines(Path.of(URI.create(uri)));
            if (lineIndex >= 0 && lineIndex < lines.size()) {
                return lines.get(lineIndex);
            }
        } catch (final IOException | IllegalArgumentException e) {
            LOG.fine("Could not read file for hover: " + uri + " — " + e.getMessage());
        }
        return "";
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
