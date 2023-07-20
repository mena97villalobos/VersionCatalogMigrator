package com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies

import com.mena97villalobos.versioncatalogmigrator.compiler.ast.Visitor
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.base.DependencyBlock
import com.mena97villalobos.versioncatalogmigrator.compiler.syntax.SourcePosition

data class DependencyBlockDeclaration(
    val implementations: List<ImplementationDeclaration>,
    val variables: List<VariableDeclaration>,
    val restOfFile: String,
    val position: SourcePosition
): DependencyBlock(position) {
    override fun visit(v: Visitor, o: Any): Any = v.visitDependencyBlock(this, o)

}