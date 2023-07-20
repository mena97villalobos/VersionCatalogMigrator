package com.mena97villalobos.versioncatalogmigrator.compiler.visitors

import com.mena97villalobos.versioncatalogmigrator.compiler.ast.Visitor

import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.DependencyBlockDeclaration
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.ImplementationDeclaration
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.ModuleIdentifier
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.VariableDeclaration
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.terminals.Identifier
import com.mena97villalobos.versioncatalogmigrator.compiler.syntax.SourcePosition

class GradleDependenciesGenerator(private val versionsReference: MutableList<VersionReference>) : Visitor {
    override fun visitIdentifier(declaration: Identifier, o: Any): String {
        return declaration.spelling
    }

    override fun visitImplementationDeclaration(
        declaration: ImplementationDeclaration,
        o: Any
    ): ImplementationDeclaration {
        val newModule = declaration.dependencyIdentifier.visit(this, o) as ModuleIdentifier
        return ImplementationDeclaration(declaration.keywordSpelling, newModule, declaration.position)
    }

    override fun visitVariableDeclaration(variable: VariableDeclaration, o: Any): VariableDeclaration {
        // We should not visit variables here
        return variable
    }

    override fun visitDependencyBlock(block: DependencyBlockDeclaration, o: Any): DependencyBlockDeclaration =
        DependencyBlockDeclaration(
            block.implementations.map { it.visit(this, o) as ImplementationDeclaration }.toMutableList(),
            mutableListOf(),
            block.restOfFile,
            block.thePosition
        )

    override fun visitModuleIdentifier(module: ModuleIdentifier, o: Any): ModuleIdentifier {
        val moduleName = module.moduleName.visit(this, o) as String
        val moduleGroup = module.identifiers.joinToString(".") { it.visit(this, o) as String }

        var newVersion = "\"ERROR\""

        versionsReference.forEach {
            if (it.originalReference.contains("$moduleGroup:$moduleName:")) {
                newVersion = it.libsReference
            }
        }

        return ModuleIdentifier(
            mutableListOf(),
            Identifier("", SourcePosition()),
            Identifier(newVersion, module.versionName.position),
            module.position
        )
    }
}