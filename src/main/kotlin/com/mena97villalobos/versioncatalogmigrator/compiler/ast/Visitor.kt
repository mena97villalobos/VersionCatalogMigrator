package com.mena97villalobos.versioncatalogmigrator.compiler.ast

import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.DependencyBlockDeclaration
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.ImplementationDeclaration
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.ModuleIdentifier
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.VariableDeclaration
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.terminals.Identifier

interface Visitor {

    fun visitIdentifier(declaration: Identifier, o: Any): Any

    fun visitImplementationDeclaration(declaration: ImplementationDeclaration, o: Any): Any

    fun visitVariableDeclaration(variable: VariableDeclaration, o: Any): Any

    fun visitDependencyBlock(block: DependencyBlockDeclaration, o: Any): Any

    fun visitModuleIdentifier(module: ModuleIdentifier, o: Any): Any

}