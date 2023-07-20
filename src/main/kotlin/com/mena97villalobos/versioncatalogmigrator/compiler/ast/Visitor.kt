package com.mena97villalobos.versioncatalogmigrator.compiler.ast

import com.mena97villalobos.versioncatalogmigrator.compiler.ast.base.DependencyBlock
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.base.GradleFile
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.ImplementationDeclaration
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.ModuleIdentifier
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.VariableDeclaration
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.terminals.Identifier

interface Visitor {

    fun visitIdentifier(declaration: Identifier, o: Any)

    fun visitImplementationDeclaration(declaration: ImplementationDeclaration, o: Any): Any

    fun visitVariableDeclaration(variable: VariableDeclaration, o: Any): Any

    fun visitDependencyBlock(block: DependencyBlock, o: Any): Any

    fun visitGradleFile(file: GradleFile, o: Any): Any

    fun visitModuleIdentifier(module: ModuleIdentifier, o: Any): Any

}