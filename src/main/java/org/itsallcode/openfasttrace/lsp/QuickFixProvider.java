// SPDX-License-Identifier: GPL-3.0-or-later
package org.itsallcode.openfasttrace.lsp;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.itsallcode.openfasttrace.api.core.SpecificationItemId;
import org.itsallcode.openfasttrace.lsp.index.OftIdAtPosition;
import org.itsallcode.openfasttrace.lsp.index.OftWorkspaceIndex;

// [impl->req~quickfix-updates-version~1]
public class QuickFixProvider {

    public List<CodeAction> quickFixesForDiagnostic(final Diagnostic diagnostic,
            final String fileUri, final OftWorkspaceIndex index) {
        final String message = diagnostic.getMessage().getLeft();
        if (message == null) {
            return Collections.emptyList();
        }

        final Matcher matcher = OftIdAtPosition.OFT_ID_PATTERN.matcher(message);
        if (!matcher.find()) {
            return Collections.emptyList();
        }
        final SpecificationItemId outdatedId = SpecificationItemId.parseId(matcher.group());

        return index.findSpecItemByTypeAndName(
                outdatedId.getArtifactType(), outdatedId.getName())
                .filter(specItem -> specItem.getId().getRevision() != outdatedId.getRevision())
                .map(specItem -> {
                    final String newId = specItem.getId().toString();
                    final var edit = new TextEdit(diagnostic.getRange(), newId);
                    final Map<String, List<TextEdit>> changes = new HashMap<>();
                    changes.put(fileUri, List.of(edit));
                    final var workspaceEdit = new WorkspaceEdit(changes);
                    final var action = new CodeAction("Update to " + newId);
                    action.setKind(CodeActionKind.QuickFix);
                    action.setDiagnostics(List.of(diagnostic));
                    action.setEdit(workspaceEdit);
                    return List.<CodeAction>of(action);
                })
                .orElse(Collections.emptyList());
    }
}
