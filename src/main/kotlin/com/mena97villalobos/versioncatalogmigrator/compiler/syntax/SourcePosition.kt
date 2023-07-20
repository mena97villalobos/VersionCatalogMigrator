package com.mena97villalobos.versioncatalogmigrator.compiler.syntax

data class SourcePosition(
    var start: Int = 0,
    var finish: Int = 0
) {

    fun copyPosition(source: SourcePosition) {
        start = source.start
        finish = source.finish
    }

    override fun toString(): String = "($start, $finish)"
}