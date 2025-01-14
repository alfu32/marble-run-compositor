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
    abstract fun run(ports: MutableMap<String,MutableList<ByteArray>>)
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
            getWorkers(jarPath)[workerName]!!
        }
        composite.workers[key] = wki
        composite.workerPorts[wki] = mutableMapOf(
            *(wki.declaredPorts.map { it to mutableListOf<ByteArray>() }.toTypedArray())
        )
        wki
    } else {
        composite.workers[jarPath]!!
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
    companion object {
        fun fromString(definition:String):Link{
            val sourceDestination = definition.split(Regex.fromLiteral("""\s*->\s*""")).map { it.trim(' ') }
            if (sourceDestination.size == 2) {
                val (source,destination) = sourceDestination
                val sourceWorkerPort = source.split(Regex.fromLiteral("""\s*:\s*""")).map { it.trim(' ') }
                if (sourceWorkerPort.size != 3) {
                    throw Throwable("invalid source worker:Port definition $source")
                }
                val (sourceJar,sourceWorker,sourcePort) = sourceWorkerPort
                val destinationWorkerPort = destination.split(Regex.fromLiteral("""\s*:\s*""")).map { it.trim(' ') }
                if (destinationWorkerPort.size != 2) {
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
}

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
        links.forEach { link ->
            val srcWorker = workers[link.sourceWorker]
            val srcPort = workerPorts[srcWorker]!![link.sourcePort]!!
            if (srcPort.size > 0) {
                val destWorker = workers[link.destinationWorker]
                val destPort = workerPorts[destWorker]!![link.destinationPort]!!
                val packet = srcPort.first()
                destPort.add(packet)
                sourcePacketsToRemove.add(link)
            }
        }
        sourcePacketsToRemove.forEach { link ->
            val srcWorker = workers[link.sourceWorker]
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
    }
}