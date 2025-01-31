package eu.ec.oib.training.alferio

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
    fun getJar() : String {
        return workerRef.split(":")[0]
    }
    fun getFQN() : String {
        return workerRef.split(":")[1]
    }

    fun getClassName(): String {
        return getFQN().split(".").last()
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