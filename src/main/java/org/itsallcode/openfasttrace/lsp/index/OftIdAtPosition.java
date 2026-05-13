// SPDX-License-Identifier: GPL-3.0-or-later
package org.itsallcode.openfasttrace.lsp.index;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.itsallcode.openfasttrace.api.core.SpecificationItemId;

/**
 * Finds an OFT specification item ID at a given character position in a line of text.
 *
 * <p>An OFT ID has the form {@code <type>~<name>~<revision>}, e.g. {@code req~my-req~2}.
 */
public final class OftIdAtPosition {

    // Matches artifact-type~item-name~revision, e.g. req~my-req~2
    static final Pattern OFT_ID_PATTERN = Pattern.compile(
            "[a-zA-Z][a-zA-Z0-9_-]*~[a-zA-Z][a-zA-Z0-9_.-]*~\\d+");

    private OftIdAtPosition() {
    }

    /**
     * Returns the OFT ID that covers character position {@code col} in {@code line},
     * or empty if no ID is present at that position.
     *
     * @param line the full line text (0-indexed)
     * @param col  the 0-based character offset within the line
     */
    public static Optional<SpecificationItemId> findAt(final String line, final int col) {
        final Matcher matcher = OFT_ID_PATTERN.matcher(line);
        while (matcher.find()) {
            if (matcher.start() <= col && col < matcher.end()) {
                return Optional.of(SpecificationItemId.parseId(matcher.group()));
            }
        }
        return Optional.empty();
    }
}
