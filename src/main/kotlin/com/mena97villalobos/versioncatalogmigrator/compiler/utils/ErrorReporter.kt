package com.mena97villalobos.versioncatalogmigrator.compiler.utils

import com.intellij.openapi.ui.Messages

class ErrorReporter {
    private val errors = arrayListOf<String>()

    fun containsErrors() = errors.isNotEmpty()

    fun reportError(message: String) {
        errors.add(message)
    }

    fun showAllErrors() {
        Messages.showErrorDialog(
            errors.joinToString("\n"),
            "Compile Error"
        )
    }

}