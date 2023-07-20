package com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies

import com.mena97villalobos.versioncatalogmigrator.compiler.ast.Visitor
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.base.Dependency
import com.mena97villalobos.versioncatalogmigrator.compiler.syntax.SourcePosition

data class ImplementationDeclaration(
    val dependencyIdentifier: ModuleIdentifier,
    val position: SourcePosition
): Dependency(position) {
    override fun visit(v: Visitor, o: Any): Any = v.visitImplementationDeclaration(this, o)
}