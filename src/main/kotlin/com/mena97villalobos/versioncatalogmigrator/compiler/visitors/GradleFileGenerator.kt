package com.mena97villalobos.versioncatalogmigrator.compiler.visitors

import com.mena97villalobos.versioncatalogmigrator.compiler.ast.Visitor
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.declarations.*
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.terminals.Identifier
import com.mena97villalobos.versioncatalogmigrator.compiler.syntax.TokenType

class GradleFileGenerator: Visitor {
    override fun visitIdentifier(declaration: Identifier, o: Any): Any {
        val stringBuilder = o.getStringBuilder()
        stringBuilder.append(declaration.spelling)
        return stringBuilder
    }

    override fun visitImplementationDeclaration(declaration: DependencyImplementationDeclaration, o: Any): Any {
        val stringBuilder = o.getStringBuilder()
        stringBuilder.append(declaration.keywordSpelling)
        stringBuilder.append(TokenType.LEFT_PARENTHESIS.spelling)
        declaration.dependencyIdentifier.visit(this, stringBuilder)
        stringBuilder.append(TokenType.RIGHT_PARENTHESIS.spelling)
        return stringBuilder
    }

    override fun visitUnrelatedFileContent(content: UnrelatedFileContent, o: Any): Any {
        val stringBuilder = o.getStringBuilder()
        stringBuilder.append(content.spelling)
        return stringBuilder
    }

    override fun visitVariableDeclaration(variable: VariableDeclaration, o: Any): Any {
        // We should not visit variables here
        return o
    }

    override fun visitDependencyBlock(block: DependenciesBlockDeclaration, o: Any): StringBuilder {
        val stringBuilder = o.getStringBuilder()
        stringBuilder.append(TokenType.DEPENDENCIES.spelling)
        block.implementations.forEach {
            it.visit(this, stringBuilder)
        }
        stringBuilder.append(TokenType.RIGHT_BRACKET.spelling)
        stringBuilder.append("\n")
        return stringBuilder
    }

    override fun visitModuleIdentifier(module: DependencyDeclaration, o: Any): StringBuilder {
        val stringBuilder = o.getStringBuilder()
        module.versionName.visit(this, stringBuilder)
        return stringBuilder
    }

    private fun Any.getStringBuilder(): StringBuilder = this as? StringBuilder ?: StringBuilder()
}