package eu.ec.oib.training.alferio

/**
 * The root node: a script with multiple statements.
 */
class Script(val statements: List<Statement>) {
    override fun toString(): String {
        return """
            Script {
                ${
            statements.joinToString("\n                ") { it.toString() }
        }
            }
        """.trimIndent()
    }

    operator fun component1():List<VarDeclaration>{
        //TODO("implement filter")
        return statements.filterIsInstance<VarDeclaration>()
    }

    operator fun component2():List<InstanceDeclaration>{
        //TODO("implement filter")
        return statements.filterIsInstance<InstanceDeclaration>()
    }
    operator fun component3():List<LinkDeclaration>{
        //TODO("implement filter")
        return statements.filterIsInstance<LinkDeclaration>()
    }
}

/**
 * A sealed interface for all possible statement types in this language.
 */
sealed interface Statement

/**
 * A comment line starting with '#'.
 */
data class CommentStatement(val text: String) : Statement

/**
 * A variable declaration of the form:
 *     var MY_VAR="something"
 */
data class VarDeclaration(
    val name: String,
    val value: String
) : Statement {
    fun uid(): String {
        return name
    }
}

/**
 * An instance declaration of the form:
 *     instance NAME = libs/stdlib.jar:com.mbrun.SomeWorker param1=val1 param2="val2"
 */
data class InstanceDeclaration(
    val name: String,
    val workerRef: String,
    val params: Map<String, String>
) : Statement {
    fun uid(): String {
        return name
    }
}

/**
 * A link declaration of the form:
 *     link (copy|move) source[:port] -> target[:port]
 */
data class LinkDeclaration(
    val linkType: LinkType,
    val source: Endpoint,
    val target: Endpoint
) : Statement {
    fun uid(): String {
        return "${source.name}:${source.port}->${target.name}:${target.port}"
    }
}

/**
 * The type of link: 'copy' or 'move'.
 */
enum class LinkType { COPY, MOVE }

/**
 * Represents either "workerName" or "workerName:port"
 */
data class Endpoint(
    val name: String,
    val port: String?
)

class Parser {
    val vars: MutableMap<String, VarDeclaration> = mutableMapOf()
    val instances: MutableMap<String, InstanceDeclaration> = mutableMapOf()
    val links: MutableMap<String, LinkDeclaration> = mutableMapOf()

    /**
     * Main entry point: parse a multi-line script into an AST.
     */
    fun parse(script: String): Script {
        val lines = script.split("\n")
        val statements = mutableListOf<Statement>()

        for (n in lines.indices) {
            val line = lines[n]
            val trimmed = line.trim()
            if (trimmed.isBlank()) {
                // Ignore empty lines
                continue
            }

            if (trimmed.startsWith("#")) {
                // It's a comment
                statements += CommentStatement(trimmed.removePrefix("#").trim())
            } else {
                // Parse a statement
                val statement = parseStatement(trimmed, n)
                statements += statement
            }
        }

        return Script(statements)
    }

    /**
     * Dispatch to the correct statement parser based on the first token.
     */
    private fun parseStatement(line: String, lineNumber: Int): Statement {
        // Break line into tokens (considering quotes).
        val tokens = tokenize(line)

        if (tokens.isEmpty()) {
            throw ParseException("Cannot parse empty statement: $line")
        }

        return when (tokens[0]) {
            "var" -> {
                val decl = parseVarDeclaration(tokens, line, lineNumber)
                if (decl.uid() in vars) {
                    throw ParseException("Error Line [$lineNumber] : redeclaration of $decl on $line")
                }
                vars[decl.uid()] = decl
                decl
            }

            "instance" -> {
                val decl = parseInstanceDeclaration(tokens, line, lineNumber)
                if (decl.uid() in instances) {
                    throw ParseException("Error Line [$lineNumber] : redeclaration of $decl on $line")
                }
                instances[decl.uid()] = decl
                decl
            }

            "link" -> {
                val decl = parseLinkDeclaration(tokens, line, lineNumber)
                if (decl.uid() in links) {
                    throw ParseException("Error Line [$lineNumber] : duplication of $decl on $line")
                }
                links[decl.uid()] = decl
                decl
            }

            else -> throw ParseException("Error Line [$lineNumber] : Unknown statement type: ${tokens[0]} in line: $line \n tokens:$tokens")
        }
    }

    /**
     * Tokenizer that splits a line into tokens, respecting quoted strings.
     *
     * e.g.:
     *   link copy genNumber1 -> logSink:in
     * becomes ["link", "copy", "genNumber1", "->", "logSink:in"]
     *
     *   instance foo = libs/stdlib.jar:Foo param1="hello world" param2=10
     * becomes ["instance", "foo", "=", "libs/stdlib.jar:Foo", "param1=\"hello world\"", "param2=10"]
     *
     * If you prefer a different approach (like extracting param1, param2 separately),
     * you can refine this or do a 2-phase parse (split first on spaces, then parse param expressions).
     */
    private fun tokenize(line: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < line.length) {
            when {
                // Skip whitespace
                line[i].isWhitespace() -> i++

                // If we find a double quote, read until matching quote
                line[i] == '"' -> {
                    val start = i
                    i++ // skip the initial quote
                    val sb = StringBuilder()
                    while (i < line.length && line[i] != '"') {
                        sb.append(line[i])
                        i++
                    }
                    if (i >= line.length) {
                        throw ParseException("Unclosed quote starting at $start in line: $line")
                    }
                    // skip the closing quote
                    i++
                    tokens.add("\"${sb}\"")
                }

                line[i] == '=' -> {
                    i++
                    tokens.add("=")
                }

                line[i] == '-' -> {
                    if (line[i + 1] == '>') {
                        tokens.add("->")
                        i += 2
                    } else {
                        throw ParseException("Incomplete link operator in $line at position $i")
                    }
                }

                else -> {
                    // Accumulate a 'word' until whitespace or end-of-line
                    val sb = StringBuilder()
                    var startLinkOperator = false
                    while (i < line.length && !line[i].isWhitespace()) {
                        // We stop if we detect a new quote -- meaning next token might be quoted
                        if (line[i] == '"') {
                            break
                        }
                        // We stop if we detect an assignment operator
                        if (line[i] == '=') {
                            break
                        }
                        // We stop if we detect an assignment operator
                        if (line[i] == '-' && line[i + 1] == '>') {
                            break
                        }
                        sb.append(line[i])
                        i++
                    }
                    tokens.add(sb.toString())
                }
            }
        }
        return tokens.filter { it.isNotBlank() }
    }

    // -------------------------------------------------------
    //  Parsers for each statement type
    // -------------------------------------------------------

    /**
     * Expects tokens like:
     *   ["var", <identifier>, "=", "\"someValue\""]
     * or possibly:
     *   ["var", <identifier>, "=", "someValue"]
     */
    private fun parseVarDeclaration(tokens: List<String>, originalLine: String, lineNumber: Int): VarDeclaration {
        if (tokens.size < 4) {
            throw ParseException("Error Line [$lineNumber] : Invalid var declaration: '$originalLine', \ntokens:$tokens")
        }
        // tokens[0] == "var"
        val name = tokens[1]
        if (tokens[2] != "=") {
            throw ParseException("Error Line [$lineNumber] : Expected '=' after var name in: $originalLine")
        }
        val rawValue = tokens[3]
        val value = stripQuotes(rawValue)

        return VarDeclaration(name, value)
    }

    /**
     * Expects tokens like:
     *   ["instance", <name>, "=", <workerRef>, "param1=foo", "param2=\"bar baz\""]
     */
    private fun parseInstanceDeclaration(
        tokens: List<String>,
        originalLine: String,
        lineNumber: Int
    ): InstanceDeclaration {
        if (tokens.size < 4) {
            throw ParseException("Error Line [$lineNumber] : Invalid instance declaration: $originalLine \n tokens:$tokens")
        }
        // tokens[0] == "instance"
        val name = tokens[1]
        if (tokens[2] != "=") {
            throw ParseException("Error Line [$lineNumber] : Expected '=' after instance name in: $originalLine \n tokens:$tokens")
        }
        val workerRef = tokens[3]

        // The remaining tokens (if any) are params
        val params = mutableMapOf<String, String>()
        var i = 4
        while (i <= tokens.size - 3) {
            val key = tokens[i]
            val operator = tokens[i + 1]
            val rawVal = tokens[i + 2]
            // We expect something like key=val
            params[key] = stripQuotes(rawVal)
            i += 3
        }

        return InstanceDeclaration(name, workerRef, params)
    }

    /**
     * Expects tokens like:
     *   ["link", "copy", "genNumber1", "->", "logSink:in"]
     *   or
     *   ["link", "move", "timer:out", "->", "keepAlive:in"]
     *
     * We'll parse linkType (copy/move), source endpoint, then "->", then target endpoint.
     */
    private fun parseLinkDeclaration(tokens: List<String>, originalLine: String, lineNumber: Int): LinkDeclaration {
        if (tokens.size < 5) {
            throw ParseException("Error Line [$lineNumber] : Invalid link declaration: $originalLine")
        }
        // tokens[0] == "link"
        val linkTypeStr = tokens[1]
        val linkType = when (linkTypeStr) {
            "copy" -> LinkType.COPY
            "move" -> LinkType.MOVE
            else -> throw ParseException("Error Line [$lineNumber] : Unknown link type '$linkTypeStr' in: $originalLine")
        }

        val sourceStr = tokens[2]
        if (tokens[3] != "->") {
            throw ParseException("Error Line [$lineNumber] : Expected '->' in link declaration: $originalLine")
        }
        val targetStr = tokens[4]

        val sourceEp = parseEndpoint(sourceStr)
        val targetEp = parseEndpoint(targetStr)

        return LinkDeclaration(linkType, sourceEp, targetEp)
    }

    /**
     * Parse "name:port" or just "name".
     */
    private fun parseEndpoint(text: String): Endpoint {
        val parts = text.split(":", limit = 2)
        return if (parts.size == 2) {
            Endpoint(parts[0], parts[1])
        } else {
            Endpoint(text, null)
        }
    }

    /**
     * Utility to strip the surrounding quotes if present.
     */
    private fun stripQuotes(text: String): String {
        return if (text.startsWith("\"") && text.endsWith("\"") && text.length >= 2) {
            text.substring(1, text.length - 1)
        } else {
            text
        }
    }
}

/**
 * Simple exception type for parser errors.
 */
class ParseException(message: String) : RuntimeException(message)
