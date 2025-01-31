package eu.ec.oib.training.alferio


class CompiledStatement(
    val imports: List<String>,
    val classDeclarations: List<String>,
    val config: List<String>,
    val runPre: List<String>,
    val runPost: List<String>,
) {
    operator fun component1(): List<String> {
        return imports
    }

    operator fun component2(): List<String> {
        return classDeclarations
    }

    operator fun component3(): List<String> {
        return config
    }

    operator fun component4(): List<String> {
        return runPre
    }

    operator fun component5(): List<String> {
        return runPost
    }
}

/**
 * A sealed interface for all possible statement types in this language.
 */
abstract class Compiler {
    abstract fun compile(varDeclaration: VarDeclaration): CompiledStatement
    abstract fun compile(instanceDeclaration: InstanceDeclaration): CompiledStatement
    abstract fun compile(linkDeclaration: LinkDeclaration): CompiledStatement

    abstract fun compile(script: Script): String
}


