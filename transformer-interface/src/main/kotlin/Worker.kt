package eu.ec.oib.training.alferio

import java.io.File
import java.lang.reflect.Modifier
import java.net.URLClassLoader
import java.nio.file.FileSystem
import java.nio.file.Paths
import java.util.jar.JarFile

abstract class Worker(
) {
    lateinit var jarPath: String
    open var declaredPorts: MutableList<String> = mutableListOf()
    private fun ensurePorts(ports: MutableMap<String,MutableList<ByteArray>>){
        for (port in declaredPorts) {
            if (port !in ports){
                ports[port] = mutableListOf()
            }
        }
    }
    open fun run(ports: MutableMap<String,MutableList<ByteArray>>){
        ensurePorts(ports)
    }
    override fun toString(): String {
        return """
            {
                _type : ${this.javaClass.name},
                _file : ${this.javaClass.protectionDomain.codeSource.location.file},
                jarPath : $jarPath,
                declaredPorts : $declaredPorts,
            }
        """.trimIndent()
    }
}

//getFirstWorkerInstance
fun getWorkers(jarPath:String):MutableMap<String,Worker>{
    val workers = mutableMapOf<String,Worker>()
    // Build the File for the jar in the "transformers" folder
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
        val entries = jar.entries()

        // Collect classes implementing Worker
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            if (!entry.isDirectory && entry.name.endsWith(".class")) {
                val className = entry.name
                    .removeSuffix(".class")
                    .replace('/', '.')  // e.g., "com/example/MyClass" -> "com.example.MyClass"

                // Attempt to load the class
                val clazz = runCatching { classLoader.loadClass(className) }.getOrNull() ?: continue

                // Check if it implements Worker and is concrete (not abstract)
                if (Worker::class.java.isAssignableFrom(clazz)
                    && !Modifier.isAbstract(clazz.modifiers)
                ) {
                    val workerClass = clazz as Class<out Worker>
                    val workerInstance = workerClass.getDeclaredConstructor().newInstance()
                    workerInstance.jarPath = jarPath
                    workers["$jarPath:${clazz.canonicalName}"] = workerInstance
                } else {
                    println("found invalid Worker $jarPath:$className (either abstract or not assignable to Worker)")
                }
            }
        }
        return workers
    }
}

fun declarePort(worker:Worker,portName:String) {
    if (!worker.declaredPorts.contains(portName)){
        worker.declaredPorts.add(portName)
    }
}
fun addWorkerInstance(composite:CompositeWorker,jarPath:String,workerName:String): Worker {
    val key = "$jarPath:$workerName"
    return if ( !composite.workers.containsKey(key) ) {
        val wki = if (key == ":") {
            composite
        } else {
            getWorkers(jarPath)[key]!!
        }
        composite.workers[key] = wki
        composite.workerPorts[wki] = mutableMapOf(
            *(wki.declaredPorts.map { it to mutableListOf<ByteArray>() }.toTypedArray())
        )
        wki
    } else {
        try {
            composite.workers[key]!!
        }catch (th:Throwable){
            throw Throwable("worker $workerName in $jarPath identified by $key not found")
        }
    }
}

class Link(
    var sourceJar:String,
    var sourceWorker:String,
    var sourcePort:String,
    var destinationJar:String,
    var destinationWorker:String,
    var destinationPort:String,
) {
    fun getSourceKey() = "$sourceJar:$sourceWorker"
    fun getDestinationKey() = "$destinationJar:$destinationWorker"
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
                    sourceJar=sourceJar,
                    sourceWorker=sourceWorker,
                    sourcePort=sourcePort,
                    destinationJar=destinationJar,
                    destinationWorker=destinationWorker,
                    destinationPort=destinationPort,
                )
            } else {
                throw Throwable("invalid link definition $definition")
            }
        }

    }
    override fun toString(): String {
        return """
            {
                _type : ${this.javaClass.name},
                _file : ${this.javaClass.protectionDomain.codeSource.location.file},
                sourceJar : $sourceJar,
                sourceWorker : $sourceWorker,
                sourcePort : $sourcePort,
                destinationJar : $destinationJar,
                destinationWorker : $destinationWorker,
                destinationPort : $destinationPort,
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


    override fun run(ports: MutableMap<String,MutableList<ByteArray>>) {
        workers.forEach { workerName, worker ->
            val wports = workerPorts[worker]!!
            worker.run(wports)
        }
        val sourcePacketsToRemove: MutableList<Link> = mutableListOf()
        for(link in links) {
            val srcWorker = workers[link.getSourceKey()]
            println("workers[${link.getSourceKey()}] === $srcWorker ")
            val srcPort = try{
                workerPorts[srcWorker]!![link.sourcePort]!!
            }catch(x:Throwable){
                declarePort(srcWorker!!,link.sourcePort)
                workerPorts[srcWorker]!![link.sourcePort] = mutableListOf()
                workerPorts[srcWorker]!![link.sourcePort]!!
                // throw Throwable("could not find port ${link.sourcePort} in workerPorts[$srcWorker] === ${workerPorts[srcWorker]}")
            }
            if (srcPort.size > 0) {
                val destWorker = workers[link.getDestinationKey()]
                val destPort = workerPorts[destWorker]!![link.destinationPort]!!
                val packet = srcPort.first()
                destPort.add(packet)
                sourcePacketsToRemove.add(link)
            }
        }
        for(link in sourcePacketsToRemove) {
            val srcWorker = workers[link.getDestinationKey()]
            val srcPort = workerPorts[srcWorker]!![link.sourcePort]!!
            if (srcPort.size > 0) {
                srcPort.removeAt(0)
            }
        }
    }
    companion object {
        /**
         * Example of parsing lines in "Mermaid-like" format, e.g.:
         *
         *   (error)base64Encode -> logger(input)
         *
         * Left side  : (error)base64Encode
         * Right side : logger(input)
         *
         * We'll extract:
         *   - outPort = "error"
         *   - leftWorkerName = "base64Encode"
         *   - rightWorkerName = "logger"
         *   - inPort = "input"
         */
        fun fromGraph(graph: String): CompositeWorker {
            val composite = CompositeWorker()

            val lines = graph
                .lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.trim(' ').startsWith("#") }

            for (definition in lines) {
                val link = Link.fromString(definition)
                composite.links.add(link)
                if (link.sourceWorker.isBlank()){
                    declarePort(composite,link.sourcePort)
                }
                if (link.destinationWorker.isBlank()){
                    declarePort(composite,link.destinationPort)
                }
                val sw = addWorkerInstance(composite,link.sourceJar,link.sourceWorker)
                declarePort(sw,link.sourcePort)
                val dw = addWorkerInstance(composite,link.destinationJar,link.destinationWorker)
                declarePort(dw,link.sourcePort)
            }

            return composite
        }
        fun fromScript(scriptText: String): CompositeWorker{
            val composite = CompositeWorker()
            var parser = Parser()

            val script = parser.parse(scriptText)
            for (worker in parser.instances) {
                // addWorkerInstance(composite,worker.value.workerRef)
            }
            for (link in parser.links) {
                // addWorkerInstance(composite,worker.value.workerRef)
            }
            return composite
        }
    }
}