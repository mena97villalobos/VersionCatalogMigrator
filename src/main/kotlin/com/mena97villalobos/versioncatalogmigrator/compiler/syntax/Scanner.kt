package com.mena97villalobos.versioncatalogmigrator.compiler.syntax

class Scanner(private val sourceFile: SourceFile) {

    private var currentChar: Char? = null
    private var currentSpelling = StringBuilder()

    private fun takeIt() {
        if (currentChar != SourceFile.EOT)
            currentSpelling.append(currentChar)
        currentChar = sourceFile.getCurrentChar()
    }
    private fun scanToken(): TokenType {
        if (currentChar?.isLetter() == true || currentChar?.isDigit() == true) {
            takeIt()
            while (currentChar!!.isLetter() || currentChar!!.isDigit() || currentChar == '-' || currentChar == '_')
                takeIt()
            return TokenType.IDENTIFIER
        }

        return when(currentChar.toString()) {
            "\n" -> {
                takeIt()
                TokenType.NEW_LINE
            }
            " ", "\t" -> {
                takeIt()
                TokenType.CHARACTER
            }
            TokenType.LEFT_BRACKET.spelling -> {
                takeIt()
                TokenType.LEFT_BRACKET
            }
            TokenType.RIGHT_BRACKET.spelling -> {
                takeIt()
                TokenType.RIGHT_BRACKET
            }
            TokenType.LEFT_PARENTHESIS.spelling -> {
                takeIt()
                TokenType.LEFT_PARENTHESIS
            }
            TokenType.RIGHT_PARENTHESIS.spelling -> {
                takeIt()
                TokenType.RIGHT_PARENTHESIS
            }
            TokenType.QUOTES.spelling -> {
                takeIt()
                TokenType.QUOTES
            }
            TokenType.DOT.spelling -> {
                takeIt()
                TokenType.DOT
            }
            TokenType.COLON.spelling -> {
                takeIt()
                TokenType.COLON
            }
            TokenType.DOLLAR.spelling -> {
                takeIt()
                TokenType.DOLLAR
            }
            TokenType.ASSIGN_OPERATOR -> {
                takeIt()
                TokenType.OPERATOR
            }
            TokenType.SLASH.spelling -> {
                takeIt()
                TokenType.SLASH
            }
            else -> {
                takeIt()
                TokenType.CHARACTER
            }
        }
    }

    fun scan(): Token {
        if (currentChar == null) takeIt()
        currentSpelling.clear()
        val pos = SourcePosition()
        pos.start = sourceFile.currentChar
        val kind: TokenType = scanToken()
        pos.finish = sourceFile.currentChar
        return Token(kind, currentSpelling.toString(), pos)
    }

}