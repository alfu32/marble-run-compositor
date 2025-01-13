package eu.ec.oib.training.alferio

import java.io.File
import java.lang.reflect.Modifier
import java.net.URLClassLoader
import java.util.jar.JarFile

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
class TT:Transformer{
    override fun transform(input: String): String {
        return input
    }
}


fun loadTransformers(directoryPath: String): Map<String, Transformer> {
    val transformersMap = mutableMapOf<String, Transformer>()
    val transformerDir = File(directoryPath)

    // List all .jar files in the given directory
    val jarFiles = transformerDir.listFiles()?.filter { it.extension == "jar" } ?: emptyList()

    for (jarFile in jarFiles) {
        // Create a ClassLoader for each JAR
        val urls = arrayOf(jarFile.toURI().toURL())
        val classLoader = URLClassLoader(urls, Transformer::class.java.classLoader)

        // Scan the JAR for classes
        JarFile(jarFile).use { jar ->
            val entries = jar.entries()

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()

                // We're only interested in .class files (skip directories, etc.)
                if (!entry.isDirectory && entry.name.endsWith(".class")) {
                    val className = entry.name
                        .removeSuffix(".class")
                        .replace('/', '.') // e.g. "com/example/Foo" -> "com.example.Foo"

                    // Try loading the class
                    val loadedClass = try {
                        classLoader.loadClass(className)
                    } catch (ex: ClassNotFoundException) {
                        // If the class can't be found/loaded, skip
                        continue
                    }

                    // Check if the class implements Transformer and is not abstract
                    if (Transformer::class.java.isAssignableFrom(loadedClass)
                        && !Modifier.isAbstract(loadedClass.modifiers)
                    ) {
                        // Create an instance using the no-arg constructor
                        val instance = loadedClass.getDeclaredConstructor().newInstance() as Transformer

                        // Put it in the map; you can decide on a suitable key
                        transformersMap[className] = instance
                    }
                }
            }
        }
    }

    return transformersMap
}
fun loadAllClassesFromJar(jarPath: String): List<Class<*>> {
    val jarFile = JarFile(jarPath)
    val classLoader = URLClassLoader(arrayOf(File(jarPath).toURI().toURL()))
    val loadedClasses = mutableListOf<Class<*>>()

    jarFile.use { file ->
        val entries = file.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            // We only care about .class files, skip directories and non-class files
            if (!entry.isDirectory && entry.name.endsWith(".class")) {
                // Convert path-like "com/example/MyClass.class" -> "com.example.MyClass"
                val className = entry.name
                    .removeSuffix(".class")
                    .replace('/', '.')
                // Load the class via the URLClassLoader
                loadedClasses.add(classLoader.loadClass(className))
            }
        }
    }

    return loadedClasses
}

fun processTransformers(inputInit: String): String {
    // 1. Read the list of JAR files from "transformers/process.txt"
    val processFile = File("transformers/process.txt")
    val jarNames = if (processFile.exists()) {
        processFile.readLines().filter { it.isNotBlank() }
    } else {
        emptyList()
    }

    var result = inputInit

    // 2. For each JAR filename, load and apply transforms
    for (jarName in jarNames) {
        println("processing transformers/$jarName")
        val jarFile = File("transformers", jarName).takeIf { it.exists() }
            ?: continue // skip if the file doesn't exist

        // Create a fresh ClassLoader for this JAR
        val classLoader = URLClassLoader(arrayOf(jarFile.toURI().toURL()), Transformer::class.java.classLoader)
        try {
            // Open the JAR to scan for classes
            JarFile(jarFile).use { jar ->
                val entries = jar.entries()

                // Collect classes implementing Transformer
                val transformerClasses = mutableListOf<Class<out Transformer>>()

                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (!entry.isDirectory && entry.name.endsWith(".class")) {
                        val className = entry.name
                            .removeSuffix(".class")
                            .replace('/', '.')  // e.g., "com/example/MyClass" -> "com.example.MyClass"

                        // Attempt to load the class
                        val clazz = runCatching { classLoader.loadClass(className) }.getOrNull() ?: continue

                        // Check if it implements Transformer and is concrete (not abstract)
                        if (Transformer::class.java.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.modifiers)) {
                            @Suppress("UNCHECKED_CAST")
                            transformerClasses.add(clazz as Class<out Transformer>)
                        }
                    }
                }

                // 3. Instantiate each Transformer and apply it to the running result
                for (tc in transformerClasses) {
                    val transformerInstance = tc.getDeclaredConstructor().newInstance()
                    result = transformerInstance.transform(result)
                }
            }
        } finally {
            // 4. Close the ClassLoader to “unload” the JAR
            classLoader.close()
        }
    }

    // 5. Return the final transformed result
    return result
}
fun main(args: Array<String>) {
    args.forEachIndexed {
        k,arg ->
        println("""args[$k] = $arg""")
    }
    val name = "Kotlin"
    //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
    // to see how IntelliJ IDEA suggests fixing it.
    println("Hello, " + name + "!")

    for (i in 1..5) {
        //TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
        // for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
        println("i = $i")
    }
}
