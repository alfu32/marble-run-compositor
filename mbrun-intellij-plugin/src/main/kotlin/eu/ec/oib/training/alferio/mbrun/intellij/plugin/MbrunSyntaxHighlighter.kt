package eu.ec.oib.training.alferio.mbrun.intellij.plugin

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.psi.tree.IElementType
import java.awt.Color
import java.awt.Font

class MbrunSyntaxHighlighter : SyntaxHighlighter {


    private val lexer = MbrunLexer()

    override fun getHighlightingLexer(): Lexer = lexer

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when (tokenType) {
            MbrunTokens.KEYWORD_VAR,
            MbrunTokens.KEYWORD_INSTANCE,
            MbrunTokens.KEYWORD_LINK,
            MbrunTokens.KEYWORD_COPY,
            MbrunTokens.KEYWORD_MOVE -> arrayOf(KEYWORD)

            MbrunTokens.INSTANCE,
            MbrunTokens.VARIABLE_NAME -> arrayOf(NAME_DECLARATION)
            MbrunTokens.VARIABLE_REFERENCE -> arrayOf(REFERENCE)
            MbrunTokens.IDENTIFIER,
            MbrunTokens.WORKER-> arrayOf(VARIABLE_OR_INSTANCE)
            MbrunTokens.JAR-> arrayOf(PACKAGE)
            MbrunTokens.CLASS_NAME,
            MbrunTokens.PORT-> arrayOf(PORT)
            MbrunTokens.CONSTRUCTOR_KEY -> arrayOf(MAP_KEY)
            MbrunTokens.CONSTRUCTOR_VALUE -> arrayOf(MAP_VALUE)

            MbrunTokens.STRING_LITERAL -> arrayOf(TEXT_LITERAL)

            MbrunTokens.COLON,
            MbrunTokens.EQUAL,
            MbrunTokens.ARROW -> arrayOf(PUNCTUATION)

            else -> emptyArray()
        }
    }

    companion object {
        // We create TextAttributesKeys with the desired default colors & styles
        val KEYWORD: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "MBRUN_KEYWORD",
            DefaultLanguageHighlighterColors.KEYWORD
        )
        val NAME_DECLARATION: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "MBRUN_NAME_DECLARATION",
            DefaultLanguageHighlighterColors.LOCAL_VARIABLE
        )
        val REFERENCE: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "MBRUN_REFERENCE",
            DefaultLanguageHighlighterColors.CLASS_REFERENCE
        )
        val MAP_KEY: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "MBRUN_MAP_KEY",
            DefaultLanguageHighlighterColors.INSTANCE_FIELD
        )
        val MAP_VALUE: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "MBRUN_MAP_VALUE",
            DefaultLanguageHighlighterColors.CONSTANT
        )

        val VARIABLE_OR_INSTANCE: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "MBRUN_VARIABLE_OR_INSTANCE",
            DefaultLanguageHighlighterColors.INSTANCE_METHOD
        )

        val TEXT_LITERAL: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "MBRUN_STRING_LITERAL",
            DefaultLanguageHighlighterColors.STRING
        )

        val PACKAGE: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "MBRUN_PACKAGE",
            DefaultLanguageHighlighterColors.CLASS_NAME
        )
        val PORT: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "MBRUN_PORT",
            DefaultLanguageHighlighterColors.PARAMETER
        )

        val PUNCTUATION: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "MBRUN_PUNCTUATION",
            DefaultLanguageHighlighterColors.OPERATION_SIGN
        )
    }
}
