package eu.ec.oib.training.alferio

class IdentityTransformer:Transformer {
    override fun transform(input: ByteArray) : ByteArray {
        println("IdentityTransformer:")
        println("input: ${input.toString(Charsets.UTF_8)}")
        println("output: ${input.toString(Charsets.UTF_8)}")
        return input
    }

}