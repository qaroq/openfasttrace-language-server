// SPDX-License-Identifier: GPL-3.0-or-later
package org.itsallcode.openfasttrace.lsp.intellij

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import java.io.File
import java.nio.file.Files

private val LOG = logger<OftLspServerDescriptor>()

/**
 * Describes how to start the OpenFastTrace LSP server process.
 *
 * Server JAR resolution order:
 * 1. `<project-root>/target/openfasttrace-language-server-*-standalone.jar`
 *    (produced by `mvn package` in the LSP server project)
 * 2. `lib/openfasttrace-language-server.jar` bundled in the plugin resources
 *    (copied in during the Gradle build via the `copyServerJar` task)
 */
internal class OftLspServerDescriptor(project: Project) :
    ProjectWideLspServerDescriptor(project, "OpenFastTrace LSP") {

    override fun isSupportedFile(file: VirtualFile): Boolean = Companion.isSupportedFile(file)

    override fun createCommandLine(): GeneralCommandLine {
        val jarPath = resolveServerJar()
        LOG.info("Starting OpenFastTrace LSP server: $jarPath")
        return GeneralCommandLine("java", "-jar", jarPath)
    }

    private fun resolveServerJar(): String {
        // 1. Maven build output — present during development
        val projectDir = File(project.basePath ?: "")
        val targetDir = projectDir.resolve("target")
        val jarInTarget = targetDir
            .listFiles { f -> f.name.matches(SERVER_JAR_PATTERN) }
            ?.firstOrNull()
        if (jarInTarget != null && jarInTarget.exists()) {
            LOG.debug("Using JAR from Maven target: ${jarInTarget.absolutePath}")
            return jarInTarget.absolutePath
        }

        // 2. JAR bundled inside the plugin resources
        val resource = javaClass.classLoader.getResourceAsStream("lib/openfasttrace-language-server.jar")
        if (resource != null) {
            val tempDir = Files.createTempDirectory("oft-lsp-server").toFile()
            val tempJar = File(tempDir, "openfasttrace-language-server.jar")
            resource.use { input -> tempJar.outputStream().use { input.copyTo(it) } }
            LOG.debug("Extracted bundled JAR to: ${tempJar.absolutePath}")
            return tempJar.absolutePath
        }

        error(
            "OpenFastTrace Language Server JAR not found.\n" +
                "Run 'mvn package' in the openfasttrace-language-server project root first,\n" +
                "or rebuild the IntelliJ plugin to bundle the JAR."
        )
    }

    companion object {
        private val SERVER_JAR_PATTERN = Regex(
            "openfasttrace-language-server-.*-standalone\\.jar"
        )

        fun isSupportedFile(file: VirtualFile): Boolean = file.extension == "md"
    }
}
