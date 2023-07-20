package com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies

import com.mena97villalobos.versioncatalogmigrator.compiler.ast.Visitor
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.base.Variable
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.terminals.Identifier
import com.mena97villalobos.versioncatalogmigrator.compiler.syntax.SourcePosition

data class VariableDeclaration(
    val identifier: Identifier,
    val value: Identifier,
    val position: SourcePosition
): Variable(position) {
    override fun visit(v: Visitor, o: Any): Any = v.visitVariableDeclaration(this, o)
}