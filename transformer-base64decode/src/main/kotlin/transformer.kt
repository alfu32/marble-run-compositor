import eu.ec.oib.training.alferio.Transformer
import java.util.Base64

class Base64DecodeTransformer: Transformer {
    override fun transform(input: ByteArray) : ByteArray {
        println("Base64DecodeTransformer:")
        println("input: ${input.toString(Charsets.UTF_8)}")
        val output = Base64.getDecoder().decode(input)
        println("output: ${output.toString(Charsets.UTF_8)}")
        return output
    }
}