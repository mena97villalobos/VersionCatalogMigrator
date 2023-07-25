package com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.declarations

import com.mena97villalobos.versioncatalogmigrator.compiler.ast.Visitor
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.base.BaseDependency
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.base.Terminal
import com.mena97villalobos.versioncatalogmigrator.compiler.syntax.SourcePosition

data class DependencyDeclaration(
    val identifiers: List<Terminal>,
    val moduleName: Terminal,
    val versionName: Terminal,
    val position: SourcePosition
): BaseDependency(position) {
    override fun visit(v: Visitor, o: Any): Any = v.visitModuleIdentifier(this, o)

}