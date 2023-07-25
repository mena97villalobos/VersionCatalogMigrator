package com.mena97villalobos.versioncatalogmigrator.compiler.syntax

import com.mena97villalobos.versioncatalogmigrator.compiler.ast.base.BaseDependency
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.base.BaseDependencyImplementation
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.base.BaseVariable
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.base.Terminal
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.declarations.*
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.terminals.Identifier
import com.mena97villalobos.versioncatalogmigrator.compiler.utils.ErrorReporter

class Parser(private val scanner: Scanner, private val errorReporter: ErrorReporter) {

    private val dummyId = Identifier("", SourcePosition())
    private var currentToken: Token = Token(TokenType.ERROR, "", SourcePosition())
    private var sourcePosition = SourcePosition(0, 0)
    private val restOfFile = StringBuilder()

    // Parsers
    fun parseGradleFile(): DependenciesBlockDeclaration {
        val dependencies = mutableListOf<BaseDependencyImplementation>()
        val variables = mutableListOf<BaseVariable>()
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
                    currentToken.kind.isDependency() -> dependencies.add(parseImplementationDeclaration())
                    currentToken.kind.isVariable() -> variables.add(parseVariableDeclaration())
                    currentToken.kind == TokenType.SLASH -> dependencies.add(parseUnrelatedFileContent(StringBuilder()))
                    currentToken.kind == TokenType.NEW_LINE || currentToken.kind == TokenType.CHARACTER ->
                        dependencies.add(parseUnrelatedFileContent(StringBuilder()))
                    else -> dependencies.add(parseUnrelatedFileContent(StringBuilder("")))
                }
            }
            parseBrackets()
            sourcePosition.finish = currentToken.position.finish
        } else {
            errorReporter.reportError("Dependencies Block Expected")
        }
        return DependenciesBlockDeclaration(dependencies, variables, restOfFile.toString(), sourcePosition)
    }

    private fun parseUnrelatedFileContent(spelling: StringBuilder): BaseDependencyImplementation {
        val position = getBasePosition()
        while (!(currentToken.kind.isDependency() || currentToken.kind.isVariable() || currentToken.kind == TokenType.RIGHT_BRACKET)) {
            spelling.append(currentToken.spelling)
            currentToken = scanner.scan()
        }
        position.finish = currentToken.position.finish
        return UnrelatedFileContent(spelling.toString(), position)
    }

    private fun parseImplementationDeclaration(): BaseDependencyImplementation {
        val position = getBasePosition()
        if (currentToken.kind.isDependency()) {
            val keyword = currentToken.spelling
            currentToken = scanner.scan()
            parseParenthesis()
            val id = parseModuleIdentifier()
            parseParenthesis()

            return if (id != null) {
                position.finish = currentToken.position.finish
                DependencyImplementationDeclaration(keyword, id, position)
            } else {
                parseUnrelatedFileContent(StringBuilder("$keyword("))
            }
        }

        return DependencyImplementationDeclaration(
            "",
            DependencyDeclaration(listOf(), dummyId, dummyId, position),
            currentToken.position
        )
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

    private fun parseModuleIdentifier(): BaseDependency? {
        val packageModuleInfo = arrayListOf<Identifier>()
        val moduleVersion = arrayListOf<String>()
        val position = getBasePosition()
        var moduleName = Identifier("", position)

        when (currentToken.kind) {
            TokenType.QUOTES -> {
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
                return null
            }

            else -> {
                errorReporter.reportError("Module Identifier expected here")
                return null
            }
        }
        position.finish = currentToken.position.finish

        return DependencyDeclaration(
            packageModuleInfo,
            moduleName,
            Identifier(moduleVersion.joinToString(""), position),
            position
        ).also { currentToken = scanner.scan() }
    }

    private fun parseVariableDeclaration(): BaseVariable {
        val position = getBasePosition()
        if (currentToken.kind == TokenType.VAL) {
            currentToken = scanner.scan()
            acceptIt(TokenType.CHARACTER)
            val varName = parseIdentifier()
            acceptIt(TokenType.CHARACTER)
            parseOperator()
            acceptIt(TokenType.CHARACTER)
            val varValue = parseVariableValue()
            position.finish = currentToken.position.finish
            return VariableDeclaration(varName, varValue, position)
        }
        errorReporter.reportError("Variable Expected Here")
        return VariableDeclaration(dummyId, dummyId, position)
    }

    private fun parseIdentifier(): Terminal {
        if (currentToken.kind == TokenType.IDENTIFIER) {
            return Identifier(currentToken.spelling, currentToken.position).also {
                currentToken = scanner.scan()
            }
        }
        errorReporter.reportError("Identifier Expected Here")
        return Identifier("", currentToken.position)
    }

    private fun parseVariableValue(): Terminal {
        val position = getBasePosition()
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

    private fun acceptIt(exceptedToken: TokenType) {
        if (currentToken.kind == exceptedToken) {
            currentToken = scanner.scan()
        }
    }

    private fun parseQuotes() {
        if (currentToken.kind == TokenType.QUOTES) {
            currentToken = scanner.scan()
        } else {
            errorReporter.reportError("Quotes expected Here")
        }
    }

    private fun getBasePosition(): SourcePosition = SourcePosition().apply { copyPosition(currentToken.position) }
}