package com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.terminals

import com.mena97villalobos.versioncatalogmigrator.compiler.ast.Visitor
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.base.Terminal
import com.mena97villalobos.versioncatalogmigrator.compiler.syntax.SourcePosition

data class Identifier(val spelling: String, val position: SourcePosition): Terminal(position) {
    override fun visit(v: Visitor, o: Any): Any = v.visitIdentifier(this, o)
}