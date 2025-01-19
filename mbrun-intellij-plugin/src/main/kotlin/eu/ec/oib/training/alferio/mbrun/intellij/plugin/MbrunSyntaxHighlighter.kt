package eu.ec.oib.training.alferio.mbrun.intellij.plugin

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.editor.markup.EffectType
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

            MbrunTokens.VARIABLE_NAME -> arrayOf(NAME_DECLARATION)
            MbrunTokens.VARIABLE_REFERENCE -> arrayOf(REFERENCE)
            MbrunTokens.IDENTIFIER,
            MbrunTokens.INSTANCE,
            MbrunTokens.PROTOTYPE,
            MbrunTokens.WORKER-> arrayOf(VARIABLE_OR_INSTANCE)
            MbrunTokens.JAR-> arrayOf(PACKAGE)
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
            // Bold Fuchsia
            TextAttributes(
                Color(0xFF00FF),   // foreground color (Fuchsia)
                null,             // background color
                null,             // effect color
                null,             // effect type
                Font.BOLD
            )
        )
        val NAME_DECLARATION: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "MBRUN_NAME_DECLARATION",
            // Regular Cyan
            TextAttributes(
                Color(0x277FFF),  // foreground color (Cyan)
                null,
                Color(0x277FFF),
                EffectType.LINE_UNDERSCORE,
                Font.BOLD
            )
        )
        val REFERENCE: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "MBRUN_REFERENCE",
            // Regular Cyan
            TextAttributes(
                Color(0x277FFF),  // foreground color (Cyan)
                null,
                Color(0x277FFF),
                EffectType.LINE_UNDERSCORE,
                Font.BOLD
            )
        )
        val MAP_KEY: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "MBRUN_MAP_KEY",
            // Regular Cyan
            TextAttributes(
                Color(0x4A8F59),  // foreground color (Cyan)
                null,
                null,
                null,
                Font.ITALIC
            )
        )
        val MAP_VALUE: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "MBRUN_MAP_VALUE",
            // Regular Cyan
            TextAttributes(
                Color(0xC1CDC5),  // foreground color (Cyan)
                null,
                null,
                null,
                Font.PLAIN
            )
        )

        val VARIABLE_OR_INSTANCE: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "MBRUN_VARIABLE_OR_INSTANCE",
            // Regular Cyan
            TextAttributes(
                Color(0x00FFFF),  // foreground color (Cyan)
                null,
                null,
                null,
                Font.PLAIN
            )
        )

        val TEXT_LITERAL: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "MBRUN_STRING_LITERAL",
            // Dark Green
            TextAttributes(
                Color(0x006400),  // foreground color (Dark Green)
                null,
                null,
                null,
                Font.PLAIN
            )
        )

        val PACKAGE: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "MBRUN_PACKAGE",
            // This might be used if you want to highlight "port" specifically,
            // but let's assume we highlight them with the same as punctuation or differently
            TextAttributes(
                Color(0xBC4D00),  // Yellow
                null,
                null,
                null,
                Font.PLAIN
            )
        )
        val PORT: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "MBRUN_PORT",
            // This might be used if you want to highlight "port" specifically,
            // but let's assume we highlight them with the same as punctuation or differently
            TextAttributes(
                Color(0xFFFF00),  // Yellow
                null,
                null,
                null,
                Font.PLAIN
            )
        )

        val PUNCTUATION: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "MBRUN_PUNCTUATION",
            // Fuchsia
            TextAttributes(
                Color(0xFFB7FF),
                null,
                null,
                null,
                Font.PLAIN
            )
        )
    }
}
