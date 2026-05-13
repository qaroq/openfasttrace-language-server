// SPDX-License-Identifier: GPL-3.0-or-later
package org.itsallcode.openfasttrace.lsp.index;

import java.net.URI;
import java.nio.file.Path;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.itsallcode.openfasttrace.api.core.Location;

/** Converts OFT {@link Location} objects to LSP {@link org.eclipse.lsp4j.Location} instances. */
public final class LocationConverter {

    private LocationConverter() {
    }

    /**
     * Converts an OFT {@link Location} to an LSP {@link org.eclipse.lsp4j.Location}.
     *
     * <p>OFT uses 1-based line numbers; LSP uses 0-based positions.
     */
    public static org.eclipse.lsp4j.Location toLspLocation(final Location oftLocation) {
        final String uri = pathToUri(oftLocation.getPath());
        final int line = Math.max(0, oftLocation.getLine() - 1);
        final var start = new Position(line, 0);
        final var end = new Position(line, Integer.MAX_VALUE);
        return new org.eclipse.lsp4j.Location(uri, new Range(start, end));
    }

    static String pathToUri(final String path) {
        if (path.startsWith("file:")) {
            return path;
        }
        if (path.startsWith("/")) {
            // Unix-style absolute path: prepend file:// to get file:///path
            return "file://" + path;
        }
        return Path.of(path).toUri().toString();
    }
}
