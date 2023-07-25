package com.mena97villalobos.versioncatalogmigrator.compiler

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.declarations.DependenciesBlockDeclaration
import com.mena97villalobos.versioncatalogmigrator.compiler.visitors.VersionCatalogGenerator
import com.mena97villalobos.versioncatalogmigrator.compiler.syntax.Parser
import com.mena97villalobos.versioncatalogmigrator.compiler.syntax.Scanner
import com.mena97villalobos.versioncatalogmigrator.compiler.syntax.SourceFile
import com.mena97villalobos.versioncatalogmigrator.compiler.utils.ErrorReporter
import com.mena97villalobos.versioncatalogmigrator.compiler.visitors.GradleDependenciesGenerator
import com.mena97villalobos.versioncatalogmigrator.compiler.visitors.GradleFileGenerator
import com.mena97villalobos.versioncatalogmigrator.utils.saveFileContents
import java.io.File

class Compiler(private val project: Project) {
    private val versionCatalog = VersionCatalogGenerator()
    private val errorReporter = ErrorReporter()
    private val fileGenerator = GradleFileGenerator()
    private val gradleGenerator by lazy {
        GradleDependenciesGenerator(versionCatalog.versionReference)
    }

    fun compileGradleFiles(files: List<VirtualFile>) {
        val asts = mutableListOf<GradleData>()
        files.forEach {
            asts.add(GradleData(it, compileGradleFile(it)))
        }
        generateVersionCatalogue()
        asts.forEach(this::regenerateGradleFiles)
    }

    private fun compileGradleFile(sourceFile: VirtualFile): DependenciesBlockDeclaration {
        val scanner = Scanner(SourceFile(sourceFile))
        val parser = Parser(scanner, errorReporter)
        return parser.parseGradleFile().apply {
            visit(versionCatalog, Unit)
        }
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

    private fun generateVersionCatalogue() {
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

    private fun regenerateGradleFiles(data: GradleData) {
        val newAst = data.ast.visit(gradleGenerator, Unit) as DependenciesBlockDeclaration

        val text = StringBuilder(newAst.restOfFile)
        newAst.visit(fileGenerator, text)
        data.sourceFile.saveFileContents(project, text.toString())
    }
}

data class GradleData(
    val sourceFile: VirtualFile,
    val ast: DependenciesBlockDeclaration
)