// SPDX-License-Identifier: GPL-3.0-or-later
package org.itsallcode.openfasttrace.lsp.index;

import static org.assertj.core.api.Assertions.assertThat;

import org.itsallcode.openfasttrace.api.core.SpecificationItemId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OftIdAtPositionTest {

    @Test
    void findId_onCoverageTag_returnsId() {
        final String line = "Covers:  req~my-requirement~3  and more";
        final var result = OftIdAtPosition.findAt(line, 12);

        assertThat(result).contains(SpecificationItemId.parseId("req~my-requirement~3"));
    }

    @Test
    void findId_onSpecItemId_returnsId() {
        final String line = "`feat~my-feature~1`";
        final var result = OftIdAtPosition.findAt(line, 5);

        assertThat(result).contains(SpecificationItemId.parseId("feat~my-feature~1"));
    }

    @Test
    void findId_outsideAnyId_returnsEmpty() {
        final String line = "Some plain text without any OFT id here";
        final var result = OftIdAtPosition.findAt(line, 5);

        assertThat(result).isEmpty();
    }

    @Test
    void findId_inlineTag_returnsId() {
        final String line = "  - req~another~2 some trailing text";
        final var result = OftIdAtPosition.findAt(line, 6);

        assertThat(result).contains(SpecificationItemId.parseId("req~another~2"));
    }

    @ParameterizedTest(name = "position {1} in ''{0}''")
    @CsvSource({
        "prefix req~x~1 suffix, 7, req~x~1",
        "prefix req~x~1 suffix, 13, req~x~1",
        "prefix req~x~1 suffix, 14, "           // space after id — empty
    })
    void findId_atBoundaries(final String line, final int col, final String expectedId) {
        final var result = OftIdAtPosition.findAt(line, col);

        if (expectedId == null || expectedId.isBlank()) {
            assertThat(result).isEmpty();
        } else {
            assertThat(result).contains(SpecificationItemId.parseId(expectedId));
        }
    }
}
