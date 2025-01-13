import eu.ec.oib.training.alferio.Transformer
import java.io.File
import java.lang.reflect.Modifier
import java.net.URLClassLoader
import java.util.jar.JarFile
class Base64DecodeTransformer: Transformer {
    override fun transform(input: String): String {
        println("Base64DecodeTransformer:")
        println("input: $input")
        println("output: $input")
        return input
    }
}