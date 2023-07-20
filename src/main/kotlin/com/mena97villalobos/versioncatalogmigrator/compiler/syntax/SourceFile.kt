package com.mena97villalobos.versioncatalogmigrator.compiler.syntax

import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.mena97villalobos.versioncatalogmigrator.utils.substringAfterInclusive

class SourceFile(source: VirtualFile) {

    companion object {
        private const val EOL = '\n'
        const val EOT = '\u0000'
    }

    private val text = VfsUtil.loadText(source)
        .substringAfterInclusive(TokenType.DEPENDENCIES.spelling)
    var currentChar = 0
        private set

    fun getCurrentChar(): Char =
        try {
            var c = text[currentChar]
            if (c.code == -1) {
                c = EOT
            }
            currentChar++
            c
        } catch (ignored: Exception) {
            EOT
        }
}