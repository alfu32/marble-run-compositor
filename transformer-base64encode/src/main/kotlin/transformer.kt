package eu.ec.oib.training.alferio

import java.util.*

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
class Base64EncodeTransformer:Transformer {
    override fun transform(input: ByteArray) : ByteArray {
        println("Base64EncodeTransformer:")
        println("input: ${input.toString(Charsets.UTF_8)}")
        val output= Base64.getEncoder().encode(input)
        println("output: ${output.toString(Charsets.UTF_8)}")
        return output
    }
}