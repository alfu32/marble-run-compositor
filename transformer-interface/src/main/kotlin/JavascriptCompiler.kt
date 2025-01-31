package eu.ec.oib.training.alferio

class JavascriptCompiler : ClassCompiler() {

    override var scriptTemplate: String = """
        import eu.ec.oib.training.alferio.Worker
        /***imports***/
        export default function config(conf){
            const ports = {}
            /***classDeclarations***/
                /***config***/
           return function (ports){
                const defered = []
                /***runPre***/
                /***runPost***/
                defered.forEach((fn) => fn())
            }
        }
    """.trimIndent()

    override fun compile(varDeclaration: VarDeclaration): CompiledStatement {
        return CompiledStatement(
            imports = listOf(),
            classDeclarations = listOf("""var ${varDeclaration.name}="${varDeclaration.value}""""),
            config = listOf(),
            runPre = listOf(),
            runPost = listOf(),
        )
    }

    override fun compile(instanceDeclaration: InstanceDeclaration): CompiledStatement {
        //mutableMapOf<String,MutableMap<String,MutableList<ByteArray>>>()
        return CompiledStatement(
            imports = listOf("""import ${instanceDeclaration.getFQN()}"""),
            classDeclarations = listOf(
                """var ${instanceDeclaration.name} = ${instanceDeclaration.getClassName()}({${
                    instanceDeclaration.params.map { """"${it.key}": "${it.value}"""" }.joinToString(",\n")
                }})"""
            ),
            config = listOf(
                """
                ports["${instanceDeclaration.name}"]={}
                """.trimIndent()
            ),
            runPre = listOf(
                """
                    ${instanceDeclaration.name}(this.ports["${instanceDeclaration.name}"])
                """.trimIndent()
            ),
            runPost = listOf(
                """
                    ${instanceDeclaration.name}(this.ports["${instanceDeclaration.name}"])
                """.trimIndent()
            ),
        )
    }

    override fun compile(linkDeclaration: LinkDeclaration): CompiledStatement {
        return CompiledStatement(
            imports = listOf(),
            classDeclarations = listOf(),
            config = listOf(
                """
                ports["${linkDeclaration.source.name}"]["${linkDeclaration.source.port}"]=[]
                ports["${linkDeclaration.target.name}"]["${linkDeclaration.target.port}"]=[]
            """.trimIndent(),
            ),
            runPre = listOf(),
            runPost = listOfNotNull(
                if (linkDeclaration.linkType == LinkType.MOVE) {
                    """
                        const ${linkDeclaration.target.name}_${linkDeclaration.target.port} = this.ports["${linkDeclaration.target.name}"]["${linkDeclaration.target.port}"]
                        if(!${linkDeclaration.target.name}_${linkDeclaration.target.port}.isNullOrEmpty()) {
                            const ${linkDeclaration.target.name}_${linkDeclaration.target.port}_first = ${linkDeclaration.target.name}_${linkDeclaration.target.port}.first()
                            ${linkDeclaration.target.name}_${linkDeclaration.target.port}.push(${linkDeclaration.target.name}_${linkDeclaration.target.port}_first)
                            defered.push(function(){
                                ${linkDeclaration.target.name}_${linkDeclaration.target.port}.pop()
                            })
                        
                        }
                    """.trimIndent()
                } else {
                    null
                }
            ),
        )
    }
}