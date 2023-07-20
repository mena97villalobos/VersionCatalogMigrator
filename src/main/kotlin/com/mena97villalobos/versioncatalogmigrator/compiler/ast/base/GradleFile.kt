package com.mena97villalobos.versioncatalogmigrator.compiler.ast.base

import com.mena97villalobos.versioncatalogmigrator.compiler.ast.AST
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.Visitor
import com.mena97villalobos.versioncatalogmigrator.compiler.syntax.SourcePosition

data class GradleFile(val dependencyBlock: DependencyBlock, val position: SourcePosition) : AST(position) {
    override fun visit(v: Visitor, o: Any): Any = v.visitGradleFile(this, o)
}