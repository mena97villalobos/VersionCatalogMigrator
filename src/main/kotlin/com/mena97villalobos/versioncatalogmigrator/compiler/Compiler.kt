package com.mena97villalobos.versioncatalogmigrator.compiler

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.mena97villalobos.versioncatalogmigrator.compiler.visitors.VersionCatalogGenerator
import com.mena97villalobos.versioncatalogmigrator.compiler.syntax.Parser
import com.mena97villalobos.versioncatalogmigrator.compiler.syntax.Scanner
import com.mena97villalobos.versioncatalogmigrator.compiler.syntax.SourceFile
import com.mena97villalobos.versioncatalogmigrator.compiler.utils.ErrorReporter
import com.mena97villalobos.versioncatalogmigrator.utils.saveFileContents
import java.io.File

class Compiler {
    private val versionCatalog = VersionCatalogGenerator()
    private val errorReporter = ErrorReporter()

    fun compileGradleFiles(project: Project, files: List<VirtualFile>) {
        files.forEach(this::compileGradleFile)
        generateVersionCatalogue(project)
    }

    private fun compileGradleFile(sourceFile: VirtualFile) {
        val scanner = Scanner(SourceFile(sourceFile))
        val parser = Parser(scanner, errorReporter)

        parser.parseGradleFile().visit(versionCatalog, Unit)
    }

    fun reportErrors() {
        if (errorReporter.containsErrors()) {
            errorReporter.showAllErrors()
        } else {
            Messages.showInfoMessage(
                "All files are now updated",
                "Compilation Successful"
            )
        }
    }

    private fun generateVersionCatalogue(project: Project) {
        // Must visit all AST before calling this function
        project.basePath?.let { basePath ->
            val libsNewFile = VfsUtil.findFileByIoFile(
                File("$basePath/gradle"), true
            )?.createChildData(null, "libs.versions.toml")
            libsNewFile?.saveFileContents(
                project,
                versionCatalog.generateVersionCatalogue()
            )
        }
    }
}