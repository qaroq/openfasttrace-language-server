// SPDX-License-Identifier: GPL-3.0-or-later
package org.itsallcode.openfasttrace.lsp.intellij

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.LspServerSupportProvider.LspServerStarter

/**
 * Activates the OpenFastTrace LSP server for Markdown files.
 *
 * IntelliJ calls [fileOpened] for every file the user opens. When the file
 * is a supported OFT file, we tell the [LspServerStarter] to start (or reuse)
 * the server for this project.
 */
internal class OftLspServerSupportProvider : LspServerSupportProvider {

    override fun fileOpened(
        project: Project,
        file: VirtualFile,
        serverStarter: LspServerStarter,
    ) {
        if (OftLspServerDescriptor.isSupportedFile(file)) {
            serverStarter.ensureServerStarted(OftLspServerDescriptor(project))
        }
    }
}
