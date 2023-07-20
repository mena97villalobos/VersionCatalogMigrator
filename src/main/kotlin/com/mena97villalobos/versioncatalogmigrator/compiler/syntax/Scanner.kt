package com.mena97villalobos.versioncatalogmigrator.compiler.syntax

class Scanner(private val sourceFile: SourceFile) {

    private var scanningToken = false
    private var currentChar: Char? = null
    private var currentSpelling = StringBuilder()

    private fun takeIt() {
        if (scanningToken && currentChar != SourceFile.EOT)
            currentSpelling.append(currentChar)
        currentChar = sourceFile.getCurrentChar()
    }

    // scanSeparator skips a single separator.
    private fun scanSeparator() {
        when (currentChar) {
            ' ', '\n', '\r', '\t' -> takeIt()
        }
    }

    private fun scanToken(): TokenType {
        if (currentChar?.isLetter() == true || currentChar?.isDigit() == true) {
            takeIt()
            while (currentChar!!.isLetter() || currentChar!!.isDigit() || currentChar == '-' || currentChar == '_')
                takeIt()
            return TokenType.IDENTIFIER
        }

        return when(currentChar.toString()) {
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
            else -> {
                takeIt()
                TokenType.ERROR
            }
        }
    }

    fun scan(): Token {
        if (currentChar == null) {
            takeIt()
        }

        scanningToken = false
        while (currentChar == '!' || currentChar == ' ' || currentChar == '\n' || currentChar == '\r' || currentChar == '\t') scanSeparator()

        scanningToken = true
        currentSpelling.clear()
        val pos = SourcePosition()
        pos.start = sourceFile.currentChar
        val kind: TokenType = scanToken()
        pos.finish = sourceFile.currentChar
        return Token(kind, currentSpelling.toString(), pos)
    }

}