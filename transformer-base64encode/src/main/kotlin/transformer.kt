package eu.ec.oib.training.alferio

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
class Base64EncodeTransformer:Transformer {
    override fun transform(input: String): String {
        println("Base64EncodeTransformer:")
        println("input: $input")
        println("output: $input")
        return input
    }
}