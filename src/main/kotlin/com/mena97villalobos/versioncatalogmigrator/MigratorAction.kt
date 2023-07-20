package com.mena97villalobos.versioncatalogmigrator

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.mena97villalobos.versioncatalogmigrator.compiler.Compiler
import java.io.File

class MigratorAction: AnAction() {

    private val compiler = Compiler()

    override fun actionPerformed(e: AnActionEvent) {
        e.project?.basePath?.let {basePath ->
            val file = VfsUtil.findFileByIoFile(
                File("$basePath/app/build.gradle.kts"), true
            )

            if (file != null) {
                Messages.showInfoMessage("Working", "Working")
            }
            file?.let { compiler.compileGradleFiles(e.project!!, listOf(it)) }
        }
    }
}