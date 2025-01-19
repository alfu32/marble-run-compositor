package eu.ec.oib.training.alferio.mbrun.intellij.plugin

import com.intellij.psi.tree.IElementType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

/**
 * Tests for the MbrunLexer.
 * Ensures we cover all major code paths in 'tokenize()',
 * including comment, whitespace, arrow, punctuation, string literals,
 * keywords, and the "previous tokens" logic for specialized token types.
 */
class MbrunLexerTest {

    /**
     * Helper function: run the MbrunLexer on [text] and
     * return a list of all [IElementType] tokens in order.
     */
    private fun tokenize(text: String): List<IElementType> {
        val lexer = MbrunLexer()
        lexer.start(text, 0, text.length, 0)

        val result = mutableListOf<IElementType>()
        while (true) {
            val tokenType = lexer.tokenType ?: break
            result.add(tokenType)
            lexer.advance()
        }
        return result
    }



    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun testPreviousTokensMatch(){
        val snippet = """
            var MYVAR="123"
        """.trimIndent()
        val tokens = tokenize(snippet)
        println(tokens)
    }

    @Test
    fun testComment() {
        val snippet = "# This is a comment\n"
        val tokens = tokenize(snippet)
        // Expect one COMMENT token, possibly followed by whitespace (if we count the newline).
        // In your implementation, the newline is included in the COMMENT token itself,
        // so we likely get just [COMMENT].
        assertEquals(1, tokens.size, "Should have exactly one token (COMMENT)")

        assertEquals(MbrunTokens.COMMENT, tokens[0])
    }

    @Test
    fun testWhitespace() {
        val snippet = "    \t   \n"
        val tokens = tokenize(snippet)
        // Expect a single WHITESPACE token that covers all spaces/tabs/newlines.
        assertEquals(1, tokens.size)
        assertEquals(MbrunTokens.WHITESPACE, tokens[0])
    }

    @Test
    fun testArrow() {
        val snippet = "->"
        val tokens = tokenize(snippet)
        // Expect [ARROW]
        assertEquals(1, tokens.size)
        assertEquals(MbrunTokens.ARROW, tokens[0])
    }

    @Test
    fun testPunctuation() {
        val snippet = "=:"
        val tokens = tokenize(snippet)
        // Expect [EQUAL, COLON]
        assertEquals(2, tokens.size)
        assertEquals(MbrunTokens.EQUAL, tokens[0])
        assertEquals(MbrunTokens.COLON, tokens[1])
    }

    @Test
    fun testStringLiteral() {
        val snippet = "\"hello world\""
        val tokens = tokenize(snippet)
        // Expect one STRING_LITERAL token
        assertEquals(1, tokens.size)
        assertEquals(MbrunTokens.STRING_LITERAL, tokens[0])
    }

    @Test
    fun testKeywords() {
        val snippet = "var instance link copy move"
        val tokens = tokenize(snippet)
        println(tokens)
        // We expect the lexer to tokenize these as KEYWORD_VAR, KEYWORD_INSTANCE, KEYWORD_LINK, KEYWORD_COPY, KEYWORD_MOVE
        assertEquals(9, tokens.size)
        assertEquals(MbrunTokens.KEYWORD_VAR, tokens[0])
        assertEquals(MbrunTokens.KEYWORD_INSTANCE, tokens[2])
        assertEquals(MbrunTokens.KEYWORD_LINK, tokens[4])
        assertEquals(MbrunTokens.KEYWORD_COPY, tokens[6])
        assertEquals(MbrunTokens.KEYWORD_MOVE, tokens[8])
    }

    @Test
    fun testVarDeclaration() {
        // Let's test: var MYVAR="123"
        // We want to see how your "previousTokensMatch" logic classifies MYVAR -> VARIABLE_NAME
        val snippet = """var MYVAR="123""""
        val tokens = tokenize(snippet)
        println(tokens)
        // Step through:
        // "var" -> KEYWORD_VAR
        // " " -> WHITESPACE
        // "MYVAR" -> because the previous (non-whitespace) token is KEYWORD_VAR, we get VARIABLE_NAME
        // "=" -> EQUAL
        // "\"123\"" -> STRING_LITERAL
        assertEquals(5, tokens.size)
        assertEquals(MbrunTokens.KEYWORD_VAR, tokens[0])
        assertEquals(MbrunTokens.WHITESPACE, tokens[1])
        assertEquals(MbrunTokens.VARIABLE_NAME, tokens[2])
        assertEquals(MbrunTokens.EQUAL, tokens[3])
        assertEquals(MbrunTokens.STRING_LITERAL, tokens[4])
    }

    @Test
    fun testInstanceDeclaration() {
        // e.g.: instance foo = libs/stdlib.jar:com.mbrun.ClassName
        // Should yield:
        // KEYWORD_INSTANCE -> WHITESPACE -> INSTANCE -> WHITESPACE -> EQUAL -> WHITESPACE ->
        // JAR -> COLON -> PROTOTYPE
        val snippet = "instance foo = libs/stdlib.jar:com.mbrun.ClassName"
        val tokens = tokenize(snippet)

        // Let's walk through them:
        // 0: KEYWORD_INSTANCE  ("instance")
        // 1: WHITESPACE
        // 2: INSTANCE          ("foo") because previousTokensMatch(KEYWORD_INSTANCE)
        // 3: WHITESPACE
        // 4: EQUAL
        // 5: WHITESPACE
        // 6: JAR ("libs/stdlib.jar") because previousTokensMatch(KEYWORD_INSTANCE)
        // 7: COLON
        // 8: PROTOTYPE ("com.mbrun.ClassName") because previousTokensMatch(listOf(JAR, COLON))
        assertEquals(9, tokens.size, "Should produce 9 tokens in total")

        assertEquals(MbrunTokens.KEYWORD_INSTANCE, tokens[0])
        assertEquals(MbrunTokens.WHITESPACE, tokens[1])
        assertEquals(MbrunTokens.INSTANCE, tokens[2])
        assertEquals(MbrunTokens.WHITESPACE, tokens[3])
        assertEquals(MbrunTokens.EQUAL, tokens[4])
        assertEquals(MbrunTokens.WHITESPACE, tokens[5])
        assertEquals(MbrunTokens.JAR, tokens[6])
        assertEquals(MbrunTokens.COLON, tokens[7])
        assertEquals(MbrunTokens.PROTOTYPE, tokens[8])
    }

    @Test
    fun testConstructorParams() {
        // e.g.: com.mbrun.ClassName param1=foo param2="bar"
        // After an existing PROTOTYPE token, the next IDENTIFIER might become CONSTRUCTOR_KEY,
        // followed by EQUAL -> then CONSTRUCTOR_VALUE
        val snippet = "com.mbrun.ClassName param1=foo param2=\"bar\""
        // We'll pretend the PROTOTYPE token was set if the previous token was JAR: or something,
        // but let's see how "previousTokensMatch" logic triggers.
        // If there's no preceding JAR or COLON, it might default to IDENTIFIER,
        // but let's see if your code tries to interpret "com.mbrun.ClassName"
        // as PROTOTYPE on the first token if there's no preceding tokens.

        val tokens = tokenize(snippet)
        // We'll just check the distribution:
        // Usually:
        // 0: IDENTIFIER or PROTOTYPE (the code won't see JAR,COLON before it, so likely IDENTIFIER)
        // 1: WHITESPACE
        // 2: CONSTRUCTOR_KEY? (or IDENTIFIER)
        // 3: EQUAL
        // 4: IDENTIFIER (foo)
        // 5: WHITESPACE
        // 6: CONSTRUCTOR_KEY? (param2)
        // 7: EQUAL
        // 8: STRING_LITERAL ("bar")

        // Because your 'previousTokensMatch' logic might not match if the first token is not preceded by (JAR, COLON).
        // Let's just assert we have 9 tokens and see if they match or not. Then refine as needed.
        // The key point is "previousTokensMatch(listOf(MbrunTokens.PROTOTYPE))" won't succeed unless the preceding token is PROTOTYPE.

        assertEquals(9, tokens.size)
    }

    @Test
    fun testLinkCopy() {
        // e.g.: link copy genNumber1 -> logSink:in
        //
        // According to your logic:
        //  - "link" -> KEYWORD_LINK
        //  - "copy" -> KEYWORD_COPY
        //  - "genNumber1" -> if the previous token is KEYWORD_COPY, this becomes VARIABLE_REFERENCE
        //  - "->" -> ARROW
        //  - "logSink" -> if previous token is ARROW, becomes VARIABLE_REFERENCE
        //  - ":" -> COLON
        //  - "in" -> if previous token is VARIABLE_REFERENCE then COLON, becomes PORT

        val snippet = "link copy genNumber1:out -> logSink:in"
        val tokens = tokenize(snippet)
        // Let's see how many tokens we get:
        // 0: KEYWORD_LINK
        // 1: WHITESPACE
        // 2: KEYWORD_COPY
        // 3: WHITESPACE
        // 4: VARIABLE_REFERENCE (genNumber1)
        // 5: WHITESPACE
        // 6: ARROW
        // 7: WHITESPACE
        // 8: VARIABLE_REFERENCE (logSink)
        // 9: COLON
        // 10: PORT (in)

        assertEquals(11, tokens.size)
        assertEquals(MbrunTokens.KEYWORD_LINK, tokens[0])
        assertEquals(MbrunTokens.KEYWORD_COPY, tokens[2])
        assertEquals(MbrunTokens.VARIABLE_REFERENCE, tokens[4])
        assertEquals(MbrunTokens.ARROW, tokens[6])
        assertEquals(MbrunTokens.VARIABLE_REFERENCE, tokens[8])
        assertEquals(MbrunTokens.COLON, tokens[9])
        assertEquals(MbrunTokens.PORT, tokens[10])
    }
}