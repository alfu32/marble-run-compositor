package eu.ec.oib.training.alferio


@FunctionalInterface
interface Transformer {
    fun transform(input: String) : String
}