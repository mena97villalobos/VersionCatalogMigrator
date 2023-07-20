package com.mena97villalobos.versioncatalogmigrator.compiler.visitors

import com.mena97villalobos.versioncatalogmigrator.compiler.ast.Visitor
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.base.DependencyBlock
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.base.GradleFile
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.ImplementationDeclaration
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.ModuleIdentifier
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.VariableDeclaration
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.terminals.Identifier

class GradleFileGenerator: Visitor {
    override fun visitIdentifier(declaration: Identifier, o: Any) {
        TODO("Not yet implemented")
    }

    override fun visitImplementationDeclaration(declaration: ImplementationDeclaration, o: Any): Any {
        TODO("Not yet implemented")
    }

    override fun visitVariableDeclaration(variable: VariableDeclaration, o: Any): Any {
        TODO("Not yet implemented")
    }

    override fun visitDependencyBlock(block: DependencyBlock, o: Any): Any {
        TODO("Not yet implemented")
    }

    override fun visitGradleFile(file: GradleFile, o: Any): Any {
        TODO("Not yet implemented")
    }

    override fun visitModuleIdentifier(module: ModuleIdentifier, o: Any): Any {
        TODO("Not yet implemented")
    }
}