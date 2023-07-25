package com.mena97villalobos.versioncatalogmigrator.compiler.ast

import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.declarations.*
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.terminals.Identifier

interface Visitor {

    fun visitDependencyBlock(block: DependenciesBlockDeclaration, o: Any): Any

    fun visitModuleIdentifier(module: DependencyDeclaration, o: Any): Any

    fun visitImplementationDeclaration(declaration: DependencyImplementationDeclaration, o: Any): Any

    fun visitUnrelatedFileContent(content: UnrelatedFileContent, o: Any): Any

    fun visitVariableDeclaration(variable: VariableDeclaration, o: Any): Any

    fun visitIdentifier(declaration: Identifier, o: Any): Any

}