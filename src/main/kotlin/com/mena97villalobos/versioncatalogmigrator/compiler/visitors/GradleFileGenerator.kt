package com.mena97villalobos.versioncatalogmigrator.compiler.visitors

import com.mena97villalobos.versioncatalogmigrator.compiler.ast.Visitor
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.DependencyBlockDeclaration
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.ImplementationDeclaration
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.ModuleIdentifier
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.VariableDeclaration
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.terminals.Identifier
import com.mena97villalobos.versioncatalogmigrator.compiler.syntax.TokenType

class GradleFileGenerator: Visitor {
    override fun visitIdentifier(declaration: Identifier, o: Any): Any {
        val stringBuilder = o.getStringBuilder()
        stringBuilder.append(declaration.spelling)
        return stringBuilder
    }

    override fun visitImplementationDeclaration(declaration: ImplementationDeclaration, o: Any): Any {
        val stringBuilder = o.getStringBuilder()
        stringBuilder.append(declaration.keywordSpelling)
        stringBuilder.append(TokenType.LEFT_PARENTHESIS.spelling)
        declaration.dependencyIdentifier.visit(this, stringBuilder)
        stringBuilder.append(TokenType.RIGHT_PARENTHESIS.spelling)
        stringBuilder.append("\n")
        return stringBuilder
    }

    override fun visitVariableDeclaration(variable: VariableDeclaration, o: Any): Any {
        // We should not visit variables here
        return o
    }

    override fun visitDependencyBlock(block: DependencyBlockDeclaration, o: Any): StringBuilder {
        val stringBuilder = o.getStringBuilder()
        stringBuilder.append(TokenType.DEPENDENCIES.spelling)
        stringBuilder.append(" ")
        stringBuilder.append(TokenType.LEFT_BRACKET.spelling)
        stringBuilder.append("\n")
        block.implementations.forEach {
            it.visit(this, stringBuilder)
        }
        stringBuilder.append("\n")
        stringBuilder.append(TokenType.RIGHT_BRACKET.spelling)
        stringBuilder.append("\n")
        return stringBuilder
    }

    override fun visitModuleIdentifier(module: ModuleIdentifier, o: Any): StringBuilder {
        val stringBuilder = o.getStringBuilder()
        module.versionName.visit(this, stringBuilder)
        return stringBuilder
    }

    private fun Any.getStringBuilder(): StringBuilder = this as? StringBuilder ?: StringBuilder()
}