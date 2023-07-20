package com.mena97villalobos.versioncatalogmigrator.compiler.ast

import com.mena97villalobos.versioncatalogmigrator.compiler.syntax.SourcePosition

abstract class AST(position: SourcePosition) {

    var thePosition: SourcePosition
        private set

    init {
        thePosition = position
    }

    abstract fun visit(v: Visitor, o: Any): Any


}