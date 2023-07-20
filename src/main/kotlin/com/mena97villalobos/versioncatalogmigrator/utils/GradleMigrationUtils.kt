package com.mena97villalobos.versioncatalogmigrator.utils

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor

private const val GRADLE_KTS_FILE_EXTENSION = "kts"
private val gradleImplementationRegex = """implementation\("(.+)"\)\n""".toRegex()
private val gradleTestImplementationRegex = """testImplementation\("(.+)"\)\n""".toRegex()
private val gradleAndroidImplementationRegex = """testImplementation\("(.+)"\)\n""".toRegex()
private val gradleKaptRegex = """kapt\("(.+)"\)\n""".toRegex()
private val gradleKspRegex = """ksp\("(.+)"\)\n""".toRegex()
private val gradleApiRegex = """api\("(.+)"\)\n""".toRegex()


fun AnActionEvent.getAllGradleFiles(): List<VirtualFile> {
    val gradleFiles: MutableList<VirtualFile> = ArrayList()

    getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.forEach { file ->
        VfsUtil.visitChildrenRecursively(file, object : VirtualFileVisitor<Any?>() {
            override fun visitFile(file: VirtualFile): Boolean {
                if (file.isInLocalFileSystem && file.extension.equals(GRADLE_KTS_FILE_EXTENSION)) {
                    gradleFiles.add(file)
                }
                return file.isInLocalFileSystem && file.isDirectory
            }
        })
    }
    return gradleFiles.toSet().toList()
}

fun VirtualFile.saveFileContents(project: Project, text: String) {
    WriteCommandAction.runWriteCommandAction(project) {
        VfsUtil.saveText(this, text)
    }
}

fun String.substringAfterInclusive(delimiter: String, missingDelimiterValue: String = this): String {
    val index = indexOf(delimiter)
    return if (index == -1) missingDelimiterValue else substring(index, length)
}