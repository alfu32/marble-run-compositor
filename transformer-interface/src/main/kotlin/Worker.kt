package eu.ec.oib.training.alferio

import java.io.File
import java.lang.reflect.Modifier
import java.net.URLClassLoader
import java.nio.file.FileSystem
import java.nio.file.Paths
import java.util.Enumeration
import java.util.jar.JarFile
import java.util.jar.JarEntry
import kotlin.io.path.Path
import kotlin.io.path.name

abstract class Worker() {
    init{}
    lateinit var jarPath: String
    open var declaredPorts: MutableList<String> = mutableListOf()
    private fun ensurePorts(ports: MutableMap<String,MutableList<ByteArray>>){
        for (port in declaredPorts) {
            if (port !in ports){
                ports[port] = mutableListOf()
            }
        }
    }
    abstract fun config(conf: Map<String,String>);
    open fun run(ports: MutableMap<String,MutableList<ByteArray>>){
        ensurePorts(ports)
    }
    override fun toString(): String {
        return """
            {
                _type : ${this.javaClass.name},
                _file : ${this.javaClass.protectionDomain.codeSource.location.file},
                jarPath : ${jarPath},
                declaredPorts : $declaredPorts,
            }
        """.trimIndent()
    }
}
fun getJar(jarPath: String) : Map<String, Worker> {
    val jarFile = File(jarPath)
    if (!jarFile.exists()) {
        // If the jar doesn't exist, skip
        throw Throwable ( "could not find the jar file $jarPath" )
    } else {
        // Create a fresh ClassLoader for this JAR
        val classLoader = URLClassLoader(
            arrayOf(jarFile.toURI().toURL()),
            Worker::class.java.classLoader
        )

        // Open the JAR to scan for classes
        val jar = JarFile(jarFile)
        return jar.entries().toList()
            .filter{ entry ->
                val entryJavaName = entry.name
                    .replace(".class","")
                    .replace("/",".")
                try {
                    val clazz = classLoader.loadClass(entryJavaName)
                    // Check if it implements Worker and is concrete (not abstract)
                    if (Worker::class.java.isAssignableFrom(clazz)
                        && !Modifier.isAbstract(clazz.modifiers)
                    ) {
                        println("found valid Worker $entry / $entryJavaName")
                        val workerClass = clazz as Class<out Worker>
                        try {
                            workerClass.getDeclaredConstructor().newInstance()
                            println("can instantiate valid Worker $entry / $entryJavaName")
                            true
                        } catch (err: Throwable) {
                            println("could not instantiate ${entry.name} / $entryJavaName")
                            false
                        }
                    } else {
                        println("found invalid Worker $entryJavaName (either abstract or not assignable to Worker)")
                        false
                    }
                } catch (e: ClassNotFoundException) {
                    println("could not load class ${entry.name} / $entryJavaName")
                    false
                }
            }
            .groupBy { entry ->
                val entryJavaName = entry.name
                    .replace(".class","")
                    .replace("/",".")
                "$jarPath:$entryJavaName"
            }
            .mapValues {
                    entry ->
                val entryJavaName = entry.value[0].name
                    .replace(".class","")
                    .replace("/",".")
                val clazz = runCatching { classLoader.loadClass(entryJavaName) }.getOrNull()
                val workerClass = clazz as Class<out Worker>
                try{
                    val workerInstance = workerClass.getDeclaredConstructor().newInstance()
                    workerInstance.jarPath=jarPath
                    println("instantiated Worker\n class:$entryJavaName entry:\n $entry key:\n ${entry.key}")
                    workerInstance
                }catch (x:Throwable) {
                    println()
                    throw Throwable ( "$entryJavaName cannot be instantiated because $x" )
                }

            }
    }

}
fun instantiateByFqn(fqn: String): Worker {
    try{
        // Load the class
        val clazz = Class.forName(fqn)

        // Check if it implements Worker and is concrete (not abstract)
        return if (Worker::class.java.isAssignableFrom(clazz)
            && !Modifier.isAbstract(clazz.modifiers)
        ) {
            println("found valid Worker $fqn")
            val workerClass = clazz as Class<out Worker>
            try {
                println("can instantiate valid Worker $fqn")
                val wk = workerClass.getDeclaredConstructor().newInstance()
                wk.jarPath=""
                wk
            } catch (err: Throwable) {
                println("could not instantiate $fqn")
                throw Throwable ( "$fqn cannot be instantiated because $err" )
            }
        } else {
            throw Throwable ("found invalid Worker $fqn (either abstract or not assignable to Worker)")
        }
    }catch (x:Throwable) {
        println()
        throw Throwable ( "$fqn cannot be instantiated because $x" )
    }
}
fun getWorker(jarclass:String) : Worker {
    val (jarPath,className) = jarclass.split(":")
    if(jarPath.isBlank()) {
        return instantiateByFqn(className)
    } else {
        val entries = getJar(jarPath)
        if (jarclass in entries) {
            return entries[jarclass]!!
        }
    }
    throw Throwable("class \n def:\n $className not found in jar:\n $jarPath with key:\n $jarclass")
}

fun declarePort(worker:Worker,portName:String) {
    if (!worker.declaredPorts.contains(portName)){
        worker.declaredPorts.add(portName)
    }
}
fun addWorkerInstance(composite:CompositeWorker,instance:InstanceDeclaration): Worker {
    return if ( instance.name !in composite.workers ) {
        val wki = getWorker(instance.workerRef)
        wki.config(instance.params)
        composite.workers[instance.name] = wki
        composite.workerPorts[wki] = mutableMapOf(
            *(wki.declaredPorts.map { it to mutableListOf<ByteArray>() }.toTypedArray())
        )
        println("added worker instance\n ref:\n ${instance.workerRef} \n name:\n ${instance.name}")
        wki
    } else {
        try {
            composite.workers[instance.name]!!
        }catch (th:Throwable){
            throw Throwable("worker ${instance.getFQN()} in ${instance.getJar()} identified by ${instance.name} not found")
        }
    }
}

class Link(
    var sourceWorker:String,
    var sourcePort:String,
    var destinationWorker:String,
    var destinationPort:String,
    /**
     * propagation type, can be "move" or "copy"
     */
    var propagationType:LinkType=LinkType.MOVE,
) {
    fun getSourceKey() = "$sourceWorker:$sourcePort"
    fun getDestinationKey() = "$destinationWorker:$destinationPort"
    companion object {
        fun fromString(definition:String):Link{
            val sourceDestination = definition.split("""\s*->\s*""".toRegex()).map { it.trim(' ') }
            println(sourceDestination)
            if (sourceDestination.size == 2) {
                val (source,destination) = sourceDestination
                val sourceWorkerPort = source.split("""\s*:\s*""".toRegex()).map { it.trim(' ') }
                if (sourceWorkerPort.size != 3) {
                    throw Throwable("invalid source worker:Port definition $source")
                }
                val (sourceJar,sourceWorker,sourcePort) = sourceWorkerPort
                val destinationWorkerPort = destination.split("""\s*:\s*""".toRegex()).map { it.trim(' ') }
                if (destinationWorkerPort.size != 3) {
                    throw Throwable("invalid destination worker:Port definition $destination")
                }
                val (destinationJar,destinationWorker,destinationPort) = destinationWorkerPort
                return Link(
                    sourceWorker=sourceWorker,
                    sourcePort=sourcePort,
                    destinationWorker=destinationWorker,
                    destinationPort=destinationPort,
                )
            } else {
                throw Throwable("invalid link definition $definition")
            }
        }
        fun fromLinkDeclaration(linkDeclaration:LinkDeclaration): Link {
            return Link(
                sourceWorker = linkDeclaration.source.name,
                sourcePort = linkDeclaration.source.port!!,
                destinationWorker = linkDeclaration.target.name,
                destinationPort = linkDeclaration.target.port!!,
                propagationType = linkDeclaration.linkType
            )
        }

    }
    override fun toString(): String {
        return """
            {
                _type : ${this.javaClass.name},
                _file : ${this.javaClass.protectionDomain.codeSource.location.file},
                sourceWorker : $sourceWorker,
                sourcePort : $sourcePort,
                destinationWorker : $destinationWorker,
                destinationPort : $destinationPort,
                propagationType : $propagationType
            }
        """.trimIndent()
    }
}

enum class ScriptStatementType(
    var regexValue:Regex
) {
    COMMENT("""^#.*?$""".toRegex(RegexOption.IGNORE_CASE)),
    EMPTY("""^$""".toRegex(RegexOption.IGNORE_CASE)),
    LINK("""^[\t\s]+(link|queue)[\t\s]+(copy|move)?[\t\s]+[a-zA-Z0-9_:]+[\t\s]*->[\t\s]*[a-zA-Z0-9._:]+""".toRegex(setOf(RegexOption.DOT_MATCHES_ALL,RegexOption.MULTILINE))),
    VARIABLE("""^[\t\s]+var[\t\s]+[a-zA-Z0-9_]+[\t\s]*=[\t\s]*[a-zA-Z0-9._:]+""".toRegex(setOf(RegexOption.DOT_MATCHES_ALL,RegexOption.MULTILINE))),
    INSTANCE("""^[\t\s]+var[\t\s]+[a-zA-Z0-9_]+[\t\s]*=[\t\s]*[a-zA-Z0-9._:]+""".toRegex(setOf(RegexOption.DOT_MATCHES_ALL,RegexOption.MULTILINE))),
}
class TypedMap(
    var values:MutableMap<String,String> = mutableMapOf(),
    var mapType:ScriptStatementType = ScriptStatementType.EMPTY
)
class CompositeWorker:Worker(
){
    /**
     * strategy supported values "parallel","sequential"
     */
    lateinit var strategy:String
    /**
     * contains the map between the jar path and the worker instance
     */
    var  workers: MutableMap<String,Worker> = mutableMapOf()
    /**
     * contains the map between the jar path + port name and the worker instances to which one packet in a port will be propagated to
     */
    var  links: MutableList<Link> = mutableListOf()
    /**
     * contains the map between the jar path of the worker + port name and the queue of byte array packets
     */
    var workerPorts: MutableMap<Worker,MutableMap<String,MutableList<ByteArray>>> = mutableMapOf()
    override fun config(conf: Map<String, String>) {

    }


    override fun run(ports: MutableMap<String,MutableList<ByteArray>>) {
        workers.forEach { workerName, worker ->
            val wports = workerPorts[worker]!!
            worker.run(wports)
        }
        val sourcePacketsToRemove: MutableList<Link> = mutableListOf()
        for(link in links) {
            val srcWorker = workers[link.sourceWorker]
            println("workers[${link.sourceWorker}] === $srcWorker ")
            val srcPort = try{
                workerPorts[srcWorker]!![link.sourcePort]!!
            }catch(x:Throwable){
                declarePort(srcWorker!!,link.sourcePort)
                workerPorts[srcWorker]!![link.sourcePort] = mutableListOf()
                workerPorts[srcWorker]!![link.sourcePort]!!
                // throw Throwable("could not find port ${link.sourcePort} in workerPorts[$srcWorker] === ${workerPorts[srcWorker]}")
            }
            if (srcPort.size > 0) {
                val destWorker = workers[link.destinationWorker]
                val destPort = workerPorts[destWorker]!![link.destinationPort]!!
                val packet = srcPort.first()
                destPort.add(packet)
                sourcePacketsToRemove.add(link)
            }
        }
        for(link in sourcePacketsToRemove) {
            val srcWorker = workers[link.sourceWorker]
            val srcPort = workerPorts[srcWorker]!![link.sourcePort]!!
            if (srcPort.size > 0) {
                srcPort.removeAt(0)
            }
        }
    }
    fun compile(fullPath:String){
        val scriptText = File(fullPath).readText()
        val packageName = Path(fullPath).parent.toString()
        val className = Path(fullPath).fileName.name
        val sourcecode= object {
            val packageDecl = mutableListOf("package $packageName")
            val imports = mutableListOf("")
            val classDeclaration = mutableListOf("""
                class $className:Worker(){
                    /**** INJECTED DECL CODE ****/
                    override fun config(conf: Map<String, String>) {
                        /**** INJECTED CONFIG CODE ****/
                    }
                    override fun run(ports: MutableMap<String,MutableList<ByteArray>>) {
                        /**** INJECTED RUN CODE ****/
                    }
                }
            """.trimIndent())
        }
        var parser = Parser()

        val (varDeclarations,instanceDeclarations,linkDeclarations) = parser.parse(scriptText)
        val varNames = varDeclarations.groupBy { it.name }
        val codeDeclarations = mutableListOf("/** generated code from fullPath")
        val codeConfig = mutableListOf("/** generated code from fullPath")
        val codeRun = mutableListOf("/** generated code from fullPath")
        for(varDecl in varDeclarations) {
            codeDeclarations.add("""private String ${varDecl.name}=${varDecl.value}""")
        }
        for(instanceDecl in instanceDeclarations) {
            codeDeclarations.add("""private HashMap<String,ArrayList<ByteArray>> ${instanceDecl.name}Ports=new HashMap<String,ArrayList<ByteArray>>()""")
            codeDeclarations.add("""private ${instanceDecl.getFQN()} ${instanceDecl.name}=new ${instanceDecl.getFQN()}()""")
            codeConfig.add("""${instanceDecl.name}config(new HashMap<String,String>(){{""")
            for ( param in instanceDecl.params ) {
                codeConfig.add("""put("${param.key}","${param.value}")""")
            }
            codeConfig.add("""}})""")
            codeRun.add("""""")
        }
        for(link in links) {
            when(link.propagationType){
                LinkType.MOVE -> {
                    codeRun.add("""${link.destinationWorker}Ports.get("${link.destinationPort}").add(${link.sourceWorker}Ports.get("${link.sourcePort}").pop())""")
                }
                LinkType.COPY -> {
                    codeRun.add("""${link.destinationWorker}Ports.get("${link.destinationPort}").add(${link.sourceWorker}Ports.get("${link.sourcePort}").get(0))""")
                }
            }
        }
        codeRun.add("""}""")
    }
    companion object {
        fun fromScript(scriptText: String): CompositeWorker{
            val composite = CompositeWorker()
            var parser = Parser()

            val (varDeclarations,instanceDeclarations,linkDeclarations) = parser.parse(scriptText)
            val varNames = varDeclarations.groupBy { it.name }
            for (instanceDeclaration in instanceDeclarations) {
                addWorkerInstance(composite,instanceDeclaration)
                println("addWorkerInstance($instanceDeclaration)")
            }
            for (linkDeclaration in linkDeclarations) {
                if (linkDeclaration.source.name.isBlank()){
                    declarePort(composite,linkDeclaration.source.port!!)
                }
                if (linkDeclaration.target.name.isBlank()){
                    declarePort(composite,linkDeclaration.target.port!!)
                }
                try{
                    val sw = composite.workers[linkDeclaration.source.name]!!
                    declarePort(sw, linkDeclaration.source.port!!)

                    composite.workerPorts[sw] = mutableMapOf(
                        *(sw.declaredPorts.map { it to mutableListOf<ByteArray>() }.toTypedArray())
                    )
                    println("declared port of ${linkDeclaration.source.name} with name ${linkDeclaration.source.port}")
                }catch(x:Throwable){
                    throw Exception("could not find the worker instance ${linkDeclaration.source.name} with port ${linkDeclaration.source.port}")
                }
                try{
                    val dw = composite.workers[linkDeclaration.target.name]!!
                    declarePort(dw, linkDeclaration.target.port!!)
                    composite.workerPorts[dw] = mutableMapOf(
                        *(dw.declaredPorts.map { it to mutableListOf<ByteArray>() }.toTypedArray())
                    )
                    println("declared port of ${linkDeclaration.target.name} with name ${linkDeclaration.target.port}")
                }catch(x:Throwable){
                    throw Exception("could not find the worker instance ${linkDeclaration.target.name} with port ${linkDeclaration.target.port}")
                }
                composite.links.add(Link.fromLinkDeclaration(linkDeclaration))
            }
            return composite
        }

    }
}