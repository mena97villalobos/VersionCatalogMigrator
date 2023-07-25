package com.mena97villalobos.versioncatalogmigrator.compiler.visitors

import com.mena97villalobos.versioncatalogmigrator.compiler.ast.Visitor
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.base.BaseDependencyImplementation
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.declarations.*

import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.terminals.Identifier
import com.mena97villalobos.versioncatalogmigrator.compiler.syntax.SourcePosition

class GradleDependenciesGenerator(private val versionsReference: MutableList<VersionReference>) : Visitor {
    override fun visitIdentifier(declaration: Identifier, o: Any): String {
        return declaration.spelling
    }

    override fun visitImplementationDeclaration(
        declaration: DependencyImplementationDeclaration,
        o: Any
    ): BaseDependencyImplementation {
        val newModule = declaration.dependencyIdentifier.visit(this, o) as DependencyDeclaration
        return DependencyImplementationDeclaration(declaration.keywordSpelling, newModule, declaration.position)
    }

    override fun visitUnrelatedFileContent(content: UnrelatedFileContent, o: Any): BaseDependencyImplementation =
        content

    override fun visitVariableDeclaration(variable: VariableDeclaration, o: Any): VariableDeclaration {
        // We should not visit variables here
        return variable
    }

    override fun visitDependencyBlock(block: DependenciesBlockDeclaration, o: Any): DependenciesBlockDeclaration =
        DependenciesBlockDeclaration(
            block.implementations.map { it.visit(this, o) as BaseDependencyImplementation }.toMutableList(),
            mutableListOf(),
            block.restOfFile,
            block.thePosition
        )

    override fun visitModuleIdentifier(module: DependencyDeclaration, o: Any): DependencyDeclaration {
        val moduleName = module.moduleName.visit(this, o) as String
        val moduleGroup = module.identifiers.joinToString(".") { it.visit(this, o) as String }

        var newVersion = "\"ERROR\""

        versionsReference.firstOrNull { it.originalReference.contains("$moduleGroup:$moduleName:") }?.let {
            newVersion = it.libsReference
        }

        return DependencyDeclaration(
            mutableListOf(),
            Identifier("", SourcePosition()),
            Identifier(newVersion, module.versionName.thePosition),
            module.position
        )
    }
}