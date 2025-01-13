package eu.ec.oib.training.alferio

class IdentityTransformer:Transformer {
    override fun transform(input: String): String {
        println("IdentityTransformer:")
        println("input: $input")
        println("output: $input")
        return input
    }

}