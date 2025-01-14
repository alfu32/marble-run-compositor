package eu.ec.oib.training.alferio

import java.io.File
import java.lang.reflect.Modifier
import java.net.URLClassLoader
import java.util.jar.JarFile


class SequentialTransformerJarExecutor(
    private val jarListPath: String
) : Transformer {

    override fun transform(input: ByteArray): ByteArray {
        // 1. Read the list of JAR files from jarListPath
        val processFile = File(this.jarListPath)
        val jarNames = if (processFile.exists()) {
            processFile.readLines().filter { it.isNotBlank() }
        } else {
            emptyList()
        }

        return jarNames.fold(input) { currentOutput, jarName ->
            println("Processing $jarName")

            // Build the File for the jar in the "transformers" folder
            val jarFile = File("transformers", jarName)
            if (!jarFile.exists()) {
                // If the jar doesn't exist, skip
                currentOutput
            } else {
                // Create a fresh ClassLoader for this JAR
                val classLoader = URLClassLoader(
                    arrayOf(jarFile.toURI().toURL()),
                    Transformer::class.java.classLoader
                )

                var newOutput = currentOutput
                try {
                    // Open the JAR to scan for classes
                    JarFile(jarFile).use { jar ->
                        val entries = jar.entries()

                        // Collect classes implementing Transformer
                        while (entries.hasMoreElements()) {
                            val entry = entries.nextElement()
                            if (!entry.isDirectory && entry.name.endsWith(".class")) {
                                val className = entry.name
                                    .removeSuffix(".class")
                                    .replace('/', '.')  // e.g., "com/example/MyClass" -> "com.example.MyClass"

                                // Attempt to load the class
                                val clazz = runCatching { classLoader.loadClass(className) }.getOrNull() ?: continue

                                // Check if it implements Transformer and is concrete (not abstract)
                                if (Transformer::class.java.isAssignableFrom(clazz)
                                    && !Modifier.isAbstract(clazz.modifiers)
                                ) {
                                    val transformerClass = clazz as Class<out Transformer>
                                    val transformerInstance = transformerClass.getDeclaredConstructor().newInstance()
                                    // Apply this transformer to the output so far
                                    newOutput = transformerInstance.transform(newOutput)
                                }
                            }
                        }
                    }
                } finally {
                    // 4. Close the ClassLoader to “unload” the JAR
                    classLoader.close()
                }
                newOutput
            }
        }
    }
}
fun main(args: Array<String>) {
    args.forEachIndexed {
        k,arg ->
        println("""args[$k] = $arg""")
    }
    val input = "hello".toByteArray()
    val output = SequentialTransformerJarExecutor("transformers/process.txt").transform(input)
    println("MainTransformer:")
    println("input: ${input.toString(Charsets.UTF_8)}")
    println("output: ${output.toString(Charsets.UTF_8)}")
}
