package eu.ec.oib.training.alferio


abstract class ClassCompiler : Compiler() {
    val imports: MutableList<String> = mutableListOf()
    val classDeclarations: MutableList<String> = mutableListOf()
    val config: MutableList<String> = mutableListOf()
    val runPre: MutableList<String> = mutableListOf()
    val runPost: MutableList<String> = mutableListOf()

    abstract var scriptTemplate: String
    fun compileStatement(statement: Statement): CompiledStatement? {

        return when (statement) {
            is VarDeclaration -> this.compile(statement as VarDeclaration)
            is CommentStatement -> {
                null
            }//this.compile(statement as CommentStatement)
            is InstanceDeclaration -> this.compile(statement as InstanceDeclaration)
            is LinkDeclaration -> this.compile(statement as LinkDeclaration)
        }
    }

    override fun compile(script: Script): String {
        for (statement in script.statements) {
            val cs = compileStatement(statement)
            if (cs != null) {
                val (imports, classDeclarations, config, runPre, runPost) = cs
                this.imports.addAll(imports)
                this.classDeclarations.addAll(classDeclarations)
                this.config.addAll(config)
                this.runPre.addAll(runPre)
                this.runPost.addAll(runPost)
            }
        }
        return scriptTemplate
            .replace(
                "/***imports***/", "/***imports***/\n" + imports.groupBy { it }.values.joinToString("\n") { it.first() }
            )
            .replace(
                "/***classDeclarations***/",
                "/***classDeclarations***/\n" + classDeclarations.joinToString("\n").indent(4)
            )
            .replace(
                "/***config***/", "/***config***/\n" + config.joinToString("\n").indent(8)
            )
            .replace(
                "/***runPre***/", "/***runPre***/\n" + runPre.joinToString("\n").indent(8)
            )
            .replace(
                "/***runPost***/", "/***runPost***/\n" + runPost.joinToString("\n").indent(8)
            )
    }
}