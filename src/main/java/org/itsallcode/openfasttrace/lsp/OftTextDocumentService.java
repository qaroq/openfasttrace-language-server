// SPDX-License-Identifier: GPL-3.0-or-later
package org.itsallcode.openfasttrace.lsp;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.itsallcode.openfasttrace.api.core.SpecificationItem;
import org.itsallcode.openfasttrace.lsp.index.LocationConverter;
import org.itsallcode.openfasttrace.lsp.index.OftIdAtPosition;
import org.itsallcode.openfasttrace.lsp.index.OftWorkspaceIndex;

public class OftTextDocumentService implements TextDocumentService {

    private static final Logger LOG = Logger.getLogger(OftTextDocumentService.class.getName());

    private LanguageClient client;
    private volatile OftWorkspaceIndex index = OftWorkspaceIndex.empty();

    private final DiagnosticsProvider diagnosticsProvider = new DiagnosticsProvider();
    private final QuickFixProvider quickFixProvider = new QuickFixProvider();

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

    // [impl->req~goto-definition-tag-to-spec~1]
    // [impl->req~goto-definition-spec-to-tags~1]
    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>>
            definition(final DefinitionParams params) {
        final String uri = params.getTextDocument().getUri();
        final int line = params.getPosition().getLine();
        final int col = params.getPosition().getCharacter();
        LOG.fine("definition: uri=" + uri + " line=" + line + " col=" + col);
        return CompletableFuture.supplyAsync(() -> {
            final String lineText = readLine(uri, line);
            final List<Location> locations = definitionForLine(lineText, col, uri);
            return Either.<List<? extends Location>, List<? extends LocationLink>>forLeft(locations);
        });
    }

    List<Location> definitionForLine(final String lineText, final int col,
            final String currentFileUri) {
        return OftIdAtPosition.findAt(lineText, col)
                .map(id -> {
                    final Optional<SpecificationItem> specItem = index.findSpecItem(id);
                    final boolean cursorIsInSpecFile = specItem
                            .map(item -> item.getLocation())
                            .map(loc -> currentFileUri.endsWith(loc.getPath())
                                    || loc.getPath().equals(uriToPath(currentFileUri)))
                            .orElse(false);

                    if (cursorIsInSpecFile) {
                        return index.findCoverageTags(id).stream()
                                .map(tag -> LocationConverter.toLspLocation(tag.getLocation()))
                                .collect(Collectors.toList());
                    }
                    return specItem
                            .map(item -> LocationConverter.toLspLocation(item.getLocation()))
                            .map(List::of)
                            .orElse(Collections.emptyList());
                })
                .orElse(Collections.emptyList());
    }

    // [impl->req~find-references-covering-tags~1]
    @Override
    public CompletableFuture<List<? extends Location>> references(
            final org.eclipse.lsp4j.ReferenceParams params) {
        final String uri = params.getTextDocument().getUri();
        final int line = params.getPosition().getLine();
        final int col = params.getPosition().getCharacter();
        LOG.fine("references: uri=" + uri + " line=" + line + " col=" + col);
        return CompletableFuture.supplyAsync(() -> {
            final String lineText = readLine(uri, line);
            return referencesForLine(lineText, col);
        });
    }

    List<Location> referencesForLine(final String lineText, final int col) {
        return OftIdAtPosition.findAt(lineText, col)
                .map(id -> index.findCoverageTags(id).stream()
                        .map(tag -> LocationConverter.toLspLocation(tag.getLocation()))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    // [impl->req~diagnostic-outdated-version~1]
    // [impl->req~quickfix-updates-version~1]
    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(
            final CodeActionParams params) {
        final String uri = params.getTextDocument().getUri();
        return CompletableFuture.supplyAsync(() -> params.getContext().getDiagnostics().stream()
                .filter(d -> "openfasttrace-lsp".equals(d.getSource()))
                .flatMap(d -> quickFixProvider
                        .quickFixesForDiagnostic(d, uri, index).stream())
                .map(action -> Either.<Command, CodeAction>forRight(action))
                .collect(Collectors.toList()));
    }

    // [impl->req~index-refresh-on-save~1]
    @Override
    public void didSave(final DidSaveTextDocumentParams params) {
        final String uri = params.getTextDocument().getUri();
        LOG.fine("didSave: " + uri);
        publishDiagnostics(uri);
    }

    void publishDiagnosticsForUri(final String uri) {
        publishDiagnostics(uri);
    }

    private void publishDiagnostics(final String uri) {
        if (client == null) {
            return;
        }
        final List<String> lines = readAllLines(uri);
        final List<Diagnostic> diagnostics = diagnosticsProvider.diagnoseLines(lines, index);
        client.publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics));
    }

    @Override
    public void didOpen(final DidOpenTextDocumentParams params) {
        final String uri = params.getTextDocument().getUri();
        LOG.fine("didOpen: " + uri);
        publishDiagnostics(uri);
    }

    @Override
    public void didChange(final DidChangeTextDocumentParams params) {
        // Index is not refreshed on every keystroke (ADR-0006).
    }

    @Override
    public void didClose(final DidCloseTextDocumentParams params) {
        LOG.fine("didClose: " + params.getTextDocument().getUri());
    }

    private static String uriToPath(final String uri) {
        try {
            return Path.of(URI.create(uri)).toString();
        } catch (final Exception e) {
            return uri;
        }
    }

    private String readLine(final String uri, final int lineIndex) {
        final List<String> lines = readAllLines(uri);
        if (lineIndex >= 0 && lineIndex < lines.size()) {
            return lines.get(lineIndex);
        }
        return "";
    }

    private List<String> readAllLines(final String uri) {
        try {
            return Files.readAllLines(Path.of(URI.create(uri)));
        } catch (final IOException | IllegalArgumentException e) {
            LOG.fine("Could not read file: " + uri + " — " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
