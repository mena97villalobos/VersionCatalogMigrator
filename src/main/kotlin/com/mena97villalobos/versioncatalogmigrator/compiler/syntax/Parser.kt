package com.mena97villalobos.versioncatalogmigrator.compiler.syntax

import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.DependencyBlockDeclaration
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.ImplementationDeclaration
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.ModuleIdentifier
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.VariableDeclaration
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.terminals.Identifier
import com.mena97villalobos.versioncatalogmigrator.compiler.utils.ErrorReporter

class Parser(private val scanner: Scanner, private val errorReporter: ErrorReporter) {

    private val dummyId = Identifier("", SourcePosition())
    private var currentToken: Token = Token(TokenType.ERROR, "", SourcePosition())
    private var sourcePosition = SourcePosition(0, 0)
    private val restOfFile = StringBuilder()

    // Parsers
    fun parseGradleFile(): DependencyBlockDeclaration {
        val dependencies = arrayListOf<ImplementationDeclaration>()
        val variables = arrayListOf<VariableDeclaration>()
        currentToken = scanner.scan()

        while (currentToken.kind != TokenType.DEPENDENCIES) {
            restOfFile.append(currentToken.spelling)
            currentToken = scanner.scan()
        }
        sourcePosition.start = currentToken.position.start

        if (currentToken.kind == TokenType.DEPENDENCIES) {
            currentToken = scanner.scan()
            parseBrackets()

            while (currentToken.kind != TokenType.RIGHT_BRACKET) {
                when {
                    currentToken.kind.isDependency() -> parseImplementationDeclaration()?.let(dependencies::add)
                    currentToken.kind.isVariable() -> variables.add(parseVariableDeclaration())
                    else -> currentToken = scanner.scan()
                }
            }
            sourcePosition.finish = currentToken.position.finish
        } else {
            errorReporter.reportError("Dependencies Block Expected")
        }
        return DependencyBlockDeclaration(dependencies, variables, restOfFile.toString(), currentToken.position)
    }

    private fun parseImplementationDeclaration(): ImplementationDeclaration? {
        if (currentToken.kind.isDependency()) {
            val keyword = currentToken.spelling
            currentToken = scanner.scan()
            parseParenthesis()
            val id = parseModuleIdentifier()
            parseParenthesis()

            return if (id != null) {
                ImplementationDeclaration(keyword, id, currentToken.position)
            } else {
                null
            }
        }

        return ImplementationDeclaration("", ModuleIdentifier(listOf(), dummyId, dummyId, currentToken.position), currentToken.position)
    }

    private fun parseParenthesis() {
        if (currentToken.kind == TokenType.RIGHT_PARENTHESIS || currentToken.kind == TokenType.LEFT_PARENTHESIS) {
            currentToken = scanner.scan()
        } else {
            errorReporter.reportError("Parenthesis Expected Here")
        }
    }

    private fun parseBrackets() {
        if (currentToken.kind == TokenType.RIGHT_BRACKET || currentToken.kind == TokenType.LEFT_BRACKET) {
            currentToken = scanner.scan()
        } else {
            errorReporter.reportError("Brackets Expected Here")
        }
    }

    private fun parseModuleIdentifier(): ModuleIdentifier? {
        val packageModuleInfo = arrayListOf<Identifier>()
        val moduleVersion = arrayListOf<String>()
        val position = SourcePosition()
        var moduleName = Identifier("", position)

        when (currentToken.kind) {
            TokenType.QUOTES -> {
                position.copyPosition(currentToken.position)
                var scanningPackageInfo = true
                var scanningModuleName = false
                var scanningModuleVersion = false

                do {
                    currentToken = scanner.scan()
                    when {
                        currentToken.kind == TokenType.IDENTIFIER && scanningPackageInfo -> {
                            packageModuleInfo.add(Identifier(currentToken.spelling, this.currentToken.position))
                        }
                        currentToken.kind == TokenType.IDENTIFIER && scanningModuleName -> {
                            moduleName = Identifier(currentToken.spelling, currentToken.position)
                        }
                        currentToken.kind == TokenType.IDENTIFIER && scanningModuleVersion -> {
                            moduleVersion.add(currentToken.spelling)
                        }
                        currentToken.kind == TokenType.DOT && scanningModuleVersion -> {
                            moduleVersion.add(currentToken.spelling)
                        }
                        currentToken.kind == TokenType.COLON && !scanningModuleName -> {
                            scanningPackageInfo = false
                            scanningModuleName = true
                            scanningModuleVersion = false
                        }
                        currentToken.kind == TokenType.COLON && scanningModuleName -> {
                            scanningPackageInfo = false
                            scanningModuleName = false
                            scanningModuleVersion = true
                        }
                        currentToken.kind == TokenType.DOLLAR && scanningModuleVersion -> {
                            moduleVersion.add(currentToken.spelling)
                        }
                    }
                } while (currentToken.kind != TokenType.QUOTES)
                currentToken = scanner.scan()
            }
            TokenType.IDENTIFIER -> {
                while (!currentToken.kind.isDependency() && !currentToken.kind.isVariable()) {
                    currentToken = scanner.scan()
                }
                return null
            }
            else -> {
                errorReporter.reportError("Module Identifier expected here")
                return null
            }
        }

        return ModuleIdentifier(
            packageModuleInfo,
            moduleName,
            Identifier(moduleVersion.joinToString(""), position),
            position
        ).also { currentToken = scanner.scan() }
    }

    private fun parseVariableDeclaration(): VariableDeclaration {
        if (currentToken.kind == TokenType.VAL) {
            currentToken = scanner.scan()
            val varName = parseIdentifier()
            parseOperator()
            val varValue = parseVariableValue()
            return VariableDeclaration(varName, varValue, currentToken.position)
        }
        errorReporter.reportError("Variable Expected Here")
        return VariableDeclaration(dummyId, dummyId, currentToken.position)
    }

    private fun parseIdentifier(): Identifier {
        if (currentToken.kind == TokenType.IDENTIFIER) {
            return Identifier(currentToken.spelling, currentToken.position).also {
                currentToken = scanner.scan()
            }
        }
        errorReporter.reportError("Identifier Expected Here")
        return Identifier("", currentToken.position)
    }

    private fun parseVariableValue(): Identifier {
        val position = currentToken.position
        parseQuotes()
        val res = arrayListOf<String>()
        while (currentToken.kind != TokenType.QUOTES) {
            res.add(currentToken.spelling)
            currentToken = scanner.scan()
        }
        parseQuotes()
        position.finish = currentToken.position.finish
        return Identifier(res.joinToString(""), position)
    }

    private fun parseOperator() {
        if (currentToken.kind == TokenType.OPERATOR) {
            currentToken = scanner.scan()
        } else {
            errorReporter.reportError("Assignment expected here")
        }
    }

    private fun parseQuotes() {
        if (currentToken.kind == TokenType.QUOTES) {
            currentToken = scanner.scan()
        } else {
            errorReporter.reportError("Quotes expected Here")
        }
    }
}