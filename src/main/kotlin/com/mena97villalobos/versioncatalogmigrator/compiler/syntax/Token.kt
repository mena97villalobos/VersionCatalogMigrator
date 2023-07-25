package com.mena97villalobos.versioncatalogmigrator.compiler.syntax

data class Token(
    var kind: TokenType,
    val spelling: String,
    val position: SourcePosition
) {

    init {
        if (kind == TokenType.IDENTIFIER) {
            TokenType.reservedWords.find { it.spelling == spelling }?.let {
                kind = it
            }
        }
    }

    override fun toString(): String = "Kind=$kind, spelling=$spelling, position=$position"

}

enum class TokenType(val spelling: String) {
    // Literals
    IDENTIFIER("<identifier>"),
    OPERATOR("<operator>"),
    CHARACTER("<char>"),

    // Reserved Words
    ANDROID_TEST_IMPLEMENTATION("androidTestImplementation"),
    DEPENDENCIES("dependencies"),
    IMPLEMENTATION("implementation"),
    KAPT("kapt"),
    KSP("ksp"),
    TEST_IMPLEMENTATION("testImplementation"),
    VAL("val"),

    // Symbols
    LEFT_BRACKET("{"),
    RIGHT_BRACKET("}"),
    LEFT_PARENTHESIS("("),
    RIGHT_PARENTHESIS(")"),
    QUOTES("\""),
    DOT("."),
    COLON(":"),
    DOLLAR("$"),
    SLASH("/"),
    NEW_LINE("\n"),

    // Special Tokens
    ERROR("<error>"),
    EOT("<EOT>");

    fun isDependency() =
        this == IMPLEMENTATION || this == ANDROID_TEST_IMPLEMENTATION || this == TEST_IMPLEMENTATION ||
                this == KAPT || this == KSP

    fun isVariable() = this == VAL

    companion object {
        const val ASSIGN_OPERATOR = "="
        val reservedWords =
            listOf(ANDROID_TEST_IMPLEMENTATION, DEPENDENCIES, IMPLEMENTATION, KAPT, KSP, TEST_IMPLEMENTATION, VAL)
    }
}