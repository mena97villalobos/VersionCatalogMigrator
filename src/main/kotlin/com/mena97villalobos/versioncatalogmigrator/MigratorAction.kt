package com.mena97villalobos.versioncatalogmigrator

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.mena97villalobos.versioncatalogmigrator.compiler.Compiler
import com.mena97villalobos.versioncatalogmigrator.utils.enableVersionCatalogsPreview
import java.io.File

class MigratorAction: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        e.project?.basePath?.let { basePath ->
            val compiler = Compiler(e.project!!)
            val file = VfsUtil.findFileByIoFile(
                File("$basePath/app/build.gradle.kts"), true
            )
            file?.let { compiler.compileGradleFiles(listOf(it)) }
            VfsUtil.findFileByIoFile(
                File("$basePath/settings.gradle.kts"), true
            )?.enableVersionCatalogsPreview(e.project!!)
        }
    }
}