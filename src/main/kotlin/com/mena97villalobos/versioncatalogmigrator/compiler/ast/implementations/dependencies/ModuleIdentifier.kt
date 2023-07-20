package com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies

import com.mena97villalobos.versioncatalogmigrator.compiler.ast.Visitor
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.base.DependencyIdentifier
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.terminals.Identifier
import com.mena97villalobos.versioncatalogmigrator.compiler.syntax.SourcePosition

data class ModuleIdentifier(
    val identifiers: List<Identifier>,
    val moduleName: Identifier,
    val versionName: Identifier,
    val position: SourcePosition
): DependencyIdentifier(position) {
    override fun visit(v: Visitor, o: Any): Any = v.visitModuleIdentifier(this, o)

}