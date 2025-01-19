package eu.ec.oib.training.alferio.mbrun.intellij.plugin

import com.intellij.psi.tree.IElementType

class MbrunTokenType(debugName: String) : IElementType(debugName, MbrunLanguage)

// Example minimal token instances:
object MbrunTokens {
    val KEYWORD_VAR       = MbrunTokenType("KEYWORD_VAR")
    val KEYWORD_INSTANCE  = MbrunTokenType("KEYWORD_INSTANCE")
    val KEYWORD_LINK      = MbrunTokenType("KEYWORD_LINK")
    val KEYWORD_COPY      = MbrunTokenType("KEYWORD_COPY")
    val KEYWORD_MOVE      = MbrunTokenType("KEYWORD_MOVE")

    val IDENTIFIER        = MbrunTokenType("IDENTIFIER")
    val INSTANCE        = MbrunTokenType("INSTANCE")
    val CONSTRUCTOR_KEY        = MbrunTokenType("CONSTRUCTOR_KEY")
    val CONSTRUCTOR_VALUE        = MbrunTokenType("CONSTRUCTOR_VALUE")

    val JAR             = MbrunTokenType("JAR")
    val PORT             = MbrunTokenType("PORT")
    val PROTOTYPE             = MbrunTokenType("PROTOTYPE")
    val WORKER             = MbrunTokenType("WORKER")
    val VARIABLE_NAME     = MbrunTokenType("VARIABLE_NAME")
    val VARIABLE_REFERENCE     = MbrunTokenType("VARIABLE_REFERENCE")

    val STRING_LITERAL    = MbrunTokenType("STRING_LITERAL")

    val COLON             = MbrunTokenType("COLON")
    val EQUAL             = MbrunTokenType("EQUAL")
    val ARROW             = MbrunTokenType("ARROW")

    // etc...

    val COMMENT        = MbrunTokenType("COMMENT")

    val WHITESPACE     = MbrunTokenType("WHITESPACE")
    // ... add more if needed (e.g. newline tokens, etc.)
}
