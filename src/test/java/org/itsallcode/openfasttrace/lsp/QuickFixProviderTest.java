// SPDX-License-Identifier: GPL-3.0-or-later
package org.itsallcode.openfasttrace.lsp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.itsallcode.openfasttrace.api.core.SpecificationItem;
import org.itsallcode.openfasttrace.api.core.SpecificationItemId;
import org.itsallcode.openfasttrace.lsp.index.OftWorkspaceIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuickFixProviderTest {

    private QuickFixProvider provider;

    @BeforeEach
    void setUp() {
        provider = new QuickFixProvider();
    }

    // [utest->req~quickfix-updates-version~1]
    @Test
    void quickfix_forOutdatedDiagnostic_producesTextEdit() {
        final var specItem = SpecificationItem.builder()
                .id(SpecificationItemId.parseId("req~my-req~3"))
                .title("Title")
                .description("Desc")
                .build();
        final var index = new OftWorkspaceIndex(List.of(specItem));

        // Diagnostic covers the range of "req~my-req~1" on line 2, cols 8-20
        final var range = new Range(new Position(2, 8), new Position(2, 20));
        final var diagnostic = new Diagnostic(range, "Outdated reference: 'req~my-req~1'",
                DiagnosticSeverity.Warning, "openfasttrace-lsp");

        final var actions = provider.quickFixesForDiagnostic(
                diagnostic, "file:///workspace/impl.md", index);

        assertThat(actions).hasSize(1);
        final CodeAction action = actions.get(0);
        assertThat(action.getKind()).isEqualTo(CodeActionKind.QuickFix);
        assertThat(action.getTitle()).contains("req~my-req~3");

        // The edit should replace the old ID with the current revision
        final var edits = action.getEdit().getChanges()
                .get("file:///workspace/impl.md");
        assertThat(edits).hasSize(1);
        assertThat(edits.get(0).getNewText()).isEqualTo("req~my-req~3");
        assertThat(edits.get(0).getRange()).isEqualTo(range);
    }

    // [utest->req~quickfix-updates-version~1]
    @Test
    void quickfix_noSpecItemFound_returnsEmpty() {
        final var diagnostic = new Diagnostic(
                new Range(new Position(0, 0), new Position(0, 13)),
                "Outdated reference: 'req~unknown~1'",
                DiagnosticSeverity.Warning, "openfasttrace-lsp");

        final var actions = provider.quickFixesForDiagnostic(
                diagnostic, "file:///any.md", OftWorkspaceIndex.empty());

        assertThat(actions).isEmpty();
    }
}
