package eu.ec.oib.training.alferio.mbrun.intellij.plugin

import com.intellij.lexer.LexerPosition

import com.intellij.lexer.Lexer
import com.intellij.psi.tree.IElementType

/**
 * A simple, hand-written lexer for .mbrun files.
 *
 * NOTE: This example reads the entire input into a list of tokens
 * in start(). Then the IntelliJ API methods step through that list.
 */
class MbrunLexer : Lexer() {


    /**
     * A simple implementation of LexerPosition that just stores the token index.
     */
    private data class MbrunLexerPosition(val tokenIndex: Int) : LexerPosition {
        override fun getOffset(): Int = 0
        override fun getState(): Int = 0
    }

    // We'll store the entire buffer here
    private var buffer: CharSequence = ""
    private var bufferStart = 0
    private var bufferEnd = 0

    // The current list of tokens (produced in start())
    private val tokens = mutableListOf<TokenInfo>()
    private var currentTokenIndex = 0

    // Data class representing a single token
    private data class TokenInfo(
        val type: IElementType,
        val start: Int,
        val end: Int
    )

    /**
     * Called by IntelliJ to initialize the lexer on new text.
     */
    override fun start(
        buffer: CharSequence,
        startOffset: Int,
        endOffset: Int,
        initialState: Int
    ) {
        this.buffer = buffer
        this.bufferStart = startOffset
        this.bufferEnd = endOffset

        tokens.clear()
        currentTokenIndex = 0

        // Actually lex the entire input into 'tokens'
        tokenize()
    }

    /**
     * Return the current lexer state, if you need multiple states.
     * This example is single-state; we just return 0.
     */
    override fun getState(): Int {
        return 0
    }

    /**
     * Return the type of the current token or null if we're at end.
     */
    override fun getTokenType(): IElementType? {
        return tokens.getOrNull(currentTokenIndex)?.type
    }

    /**
     * Return the start of the current token in the buffer.
     */
    override fun getTokenStart(): Int {
        return tokens.getOrNull(currentTokenIndex)?.start ?: 0
    }

    /**
     * Return the end of the current token in the buffer.
     */
    override fun getTokenEnd(): Int {
        return tokens.getOrNull(currentTokenIndex)?.end ?: 0
    }

    /**
     * Advance to the next token.
     */
    override fun advance() {
        if (currentTokenIndex < tokens.size) {
            currentTokenIndex++
        }
    }

    /**
     * Return the whole buffer.
     */
    override fun getBufferSequence(): CharSequence {
        return buffer
    }

    /**
     * Return the end offset (buffer length).
     */
    override fun getBufferEnd(): Int {
        return bufferEnd
    }

    // -------------------------
    //   Tokenization Logic
    // -------------------------
    private fun previousTokenIndex(start:Int,predicate: (value:TokenInfo,index:Int) -> Boolean) :Int {
        var i=start;
        while (i>=0 && tokens[i].type != MbrunTokens.WHITESPACE && !predicate(tokens[i],i)) {
            if(predicate(tokens[i],i)){
                return i
            }
            i--
        }
        return -1
    }
    private fun previousTokensMatch(list: List<IElementType>) :Boolean {
        val mi=list.size;
        val match = list.joinToString(","){it.toString()}
        val tokensString = tokens.filter{it.type != MbrunTokens.WHITESPACE}.takeLast(mi).joinToString(",") { it.type.toString()  }
        // if(tokensString != "") {
        //     println("filtered (${tokens.size})tokens == $tokensString ")
        //     println("tokens to match == $match ")
        // }
        return tokensString == match
    }

    private fun tokenize() {
        var offset = bufferStart

        while (offset < bufferEnd) {
            val c = buffer[offset]

            // 1) Check comment: # until end of line
            if (c == '#') {
                val startPos = offset
                offset = consumeLine(offset)
                tokens += TokenInfo(MbrunTokens.COMMENT, startPos, offset)
                continue
            }

            // 2) Check whitespace
            if (c.isWhitespace()) {
                val startPos = offset
                offset = consumeWhile(offset) { it.isWhitespace() }
                tokens += TokenInfo(MbrunTokens.WHITESPACE, startPos, offset)
                continue
            }

            // 3) Check arrow "->"
            if (c == '-' && offset + 1 < bufferEnd && buffer[offset + 1] == '>') {
                tokens += TokenInfo(MbrunTokens.ARROW, offset, offset + 2)
                offset += 2
                continue
            }

            // 4) Check single-char punctuation
            if (c == '=') {
                tokens+=TokenInfo(MbrunTokens.EQUAL, offset, offset + 1)
                //var i = previousTokenIndex(tokens.size-1) {v,_ -> v.type != MbrunTokens.IDENTIFIER}
                //if(i>0){
                //    var lastToken=tokens[i]
                //    tokens += when (lastToken.type) {
                //        MbrunTokens.KEYWORD_VAR -> {
                //            TokenInfo(MbrunTokens.ASSIGN_OP, offset, offset + 1)
                //        }
                //
                //        MbrunTokens.KEYWORD_INSTANCE -> {
                //            TokenInfo(MbrunTokens.ALIAS, offset, offset + 1)
                //        }
                //
                //        else -> {
                //            TokenInfo(MbrunTokens.EQUAL, offset, offset + 1)
                //        }
                //    }
                //}
                offset++
                continue
            }
            if (c == ':') {
                tokens += TokenInfo(MbrunTokens.COLON, offset, offset + 1)
                offset++
                continue
            }

            // 5) Check string literal in double-quotes
            if (c == '"') {
                val startPos = offset
                offset++ // skip opening quote
                while (offset < bufferEnd && buffer[offset] != '"') {
                    offset++
                }
                if (offset < bufferEnd) {
                    // skip closing quote
                    offset++
                }
                tokens += TokenInfo(MbrunTokens.STRING_LITERAL, startPos, offset)
                continue
            }

            // 6) Otherwise parse either identifier or unknown text
            if (c.isLetterOrDigit() || "_-$/\\.".contains(c)) {
                val startPos = offset
                offset = consumeWhile(offset) { it.isLetterOrDigit() || "_-$/\\.".contains(it) }
                val text = buffer.substring(startPos, offset)

                // Check if it's a recognized keyword
                val tokenType = when (text) {
                    "var" -> MbrunTokens.KEYWORD_VAR
                    "instance" -> MbrunTokens.KEYWORD_INSTANCE
                    "link" -> MbrunTokens.KEYWORD_LINK
                    "copy" -> MbrunTokens.KEYWORD_COPY
                    "move" -> MbrunTokens.KEYWORD_MOVE
                    else -> {
                        if(previousTokensMatch(listOf(MbrunTokens.KEYWORD_VAR))){
                            MbrunTokens.VARIABLE_NAME
                        }else if(previousTokensMatch(listOf(MbrunTokens.KEYWORD_VAR,MbrunTokens.VARIABLE_NAME,MbrunTokens.EQUAL))){
                            MbrunTokens.STRING_LITERAL
                        }else if(previousTokensMatch(listOf(MbrunTokens.KEYWORD_INSTANCE))){
                            MbrunTokens.INSTANCE
                        }else if(previousTokensMatch(listOf(MbrunTokens.INSTANCE,MbrunTokens.EQUAL))){
                            MbrunTokens.JAR
                        }else if(previousTokensMatch(listOf(MbrunTokens.JAR,MbrunTokens.COLON))){
                            MbrunTokens.CLASS_NAME
                        }else if(previousTokensMatch(listOf(MbrunTokens.CLASS_NAME))){
                            MbrunTokens.CONSTRUCTOR_KEY
                        }else if(previousTokensMatch(listOf(MbrunTokens.CONSTRUCTOR_KEY,MbrunTokens.EQUAL))){
                            MbrunTokens.CONSTRUCTOR_VALUE
                        }else if(previousTokensMatch(listOf(MbrunTokens.CONSTRUCTOR_VALUE))){
                            MbrunTokens.CONSTRUCTOR_KEY
                        }else if(previousTokensMatch(listOf(MbrunTokens.KEYWORD_MOVE))){
                            MbrunTokens.VARIABLE_REFERENCE
                        }else if(previousTokensMatch(listOf(MbrunTokens.KEYWORD_COPY))){
                            MbrunTokens.VARIABLE_REFERENCE
                        }else if(previousTokensMatch(listOf(MbrunTokens.ARROW))){
                            MbrunTokens.VARIABLE_REFERENCE
                        }else if(previousTokensMatch(listOf(MbrunTokens.VARIABLE_REFERENCE,MbrunTokens.COLON))){
                            MbrunTokens.PORT
                        } else {
                            MbrunTokens.IDENTIFIER
                        }
                    }
                }
                tokens += TokenInfo(tokenType, startPos, offset)
                continue
            }

            // 7) If we reach here, it's some unrecognized single char
            // We'll just treat it as an identifier or skip it, or you can define an ERROR token
            tokens += TokenInfo(MbrunTokens.IDENTIFIER, offset, offset + 1)
            offset++
        }
    }

    /**
     * Consume characters until the end of the line (or buffer).
     */
    private fun consumeLine(pos: Int): Int {
        var i = pos
        while (i < bufferEnd && buffer[i] != '\n') {
            i++
        }
        // we stop after the newline, so the token includes the newline
        if (i < bufferEnd) {
            i++
        }
        return i
    }

    /**
     * Consume characters while the predicate is true.
     */
    private fun consumeWhile(start: Int, predicate: (Char) -> Boolean): Int {
        var i = start
        while (i < bufferEnd && predicate(buffer[i])) {
            i++
        }
        return i
    }

    /**
     * Provide a snapshot of the lexer’s current position:
     * - The current token index in our tokens list
     */
    override fun getCurrentPosition(): LexerPosition {
        return MbrunLexerPosition(currentTokenIndex)
    }

    /**
     * Restore the lexer state to a previously returned position.
     */
    override fun restore(lexerPosition: LexerPosition) {
        if (lexerPosition is MbrunLexerPosition) {
            currentTokenIndex = lexerPosition.tokenIndex
        }
    }
}
