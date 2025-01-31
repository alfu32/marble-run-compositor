package eu.ec.oib.training.alferio

class KotlinClassCompiler : ClassCompiler() {

    override var scriptTemplate: String = """
        import eu.ec.oib.training.alferio.Worker
        /***imports***/
        class Composite:Worker(){
            val ports: MutableMap<String,MutableMap<String,MutableList<ByteArray>>> = mutableMapOf()
            /***classDeclarations***/
            override fun config(conf: Map<String,String>){
                /***config***/
            }
            override fun run(ports: MutableMap<String,MutableList<ByteArray>>){
                super.run(ports)
                val defered = mutableListOf<()->Unit>()
                /***runPre***/
                /***runPost***/
                for (df in defered){
                    df()
                }
            }
        }
    """.trimIndent()

    override fun compile(varDeclaration: VarDeclaration): CompiledStatement {
        return CompiledStatement(
            imports = listOf(),
            classDeclarations = listOf("""var ${varDeclaration.name}:String="${varDeclaration.value}""""),
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
                """val ${instanceDeclaration.name}:${instanceDeclaration.getClassName()} = ${instanceDeclaration.getClassName()}()"""
            ),
            config = listOf(
                """
                ${instanceDeclaration.name}.config(mutableMapOf(${
                    instanceDeclaration.params.map { """"${it.key}" to "${it.value}"""" }.joinToString(",\n")
                }))
                ports["${instanceDeclaration.name}"]=mutableMapOf()
                """.trimIndent()
            ),
            runPre = listOf(
                """
                    ${instanceDeclaration.name}.run(this.ports["${instanceDeclaration.name}"]!!)
                """.trimIndent()
            ),
            runPost = listOf(
                """
                    ${instanceDeclaration.name}.run(this.ports["${instanceDeclaration.name}"]!!)
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
                ports["${linkDeclaration.source.name}"]?.set("${linkDeclaration.source.port}", mutableListOf())
                ports["${linkDeclaration.target.name}"]?.set("${linkDeclaration.target.port}", mutableListOf())
            """.trimIndent(),
            ),
            runPre = listOf(),
            runPost = listOfNotNull(
                if (linkDeclaration.linkType == LinkType.MOVE) {
                    """
                        val ${linkDeclaration.target.name}_${linkDeclaration.target.port} = this.ports["${linkDeclaration.target.name}"]?.get("${linkDeclaration.target.port}")
                        if(!${linkDeclaration.target.name}_${linkDeclaration.target.port}.isNullOrEmpty()) {
                            val ${linkDeclaration.target.name}_${linkDeclaration.target.port}_first = ${linkDeclaration.target.name}_${linkDeclaration.target.port}.first()
                            ${linkDeclaration.target.name}_${linkDeclaration.target.port}.add(${linkDeclaration.target.name}_${linkDeclaration.target.port}_first)
                            defered.add(fun(){
                                ${linkDeclaration.target.name}_${linkDeclaration.target.port}.removeAt(0)
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