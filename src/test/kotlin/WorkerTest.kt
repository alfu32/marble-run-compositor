import eu.ec.oib.training.alferio.CompositeWorker
import eu.ec.oib.training.alferio.Worker
import eu.ec.oib.training.alferio.declarePort
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class WorkerTest {

    @Test
    fun test_run_worker() {
        val portsPrinter = object: Worker() {
            override fun config(conf: Map<String, String>) {
            }

            override fun run(ports: MutableMap<String, MutableList<ByteArray>>) {
                ports.forEach { name, queue ->
                    println("=== Port $name (${queue.size}):")
                    for(ba in queue){
                        val s = ba.toString(Charsets.UTF_8)
                        val slen=s.length
                        val sp = if(slen>200) { "${s.substring(0..50)} ... (${slen} bytes) ... ${s.substring(slen-50..<slen)}" } else "$s (${slen} bytes)"
                        println(" - $sp")
                    }
                }
                ports["log"]!!.add("printed".toByteArray())
            }

        }
        val wk = object: Worker() {
            override fun config(conf: Map<String, String>) {
            }

            override fun run(ports: MutableMap<String, MutableList<ByteArray>>) {
                val input = ports["in"]!!.first().toString(Charsets.UTF_8)
                if( input == "error" ) {
                    ports["err"]!!.add("Hello $input !".toByteArray())
                } else {
                    ports["out"]!!.add("Hello $input !".toByteArray())
                }
            }
        }
        declarePort(portsPrinter,"log")
        declarePort(wk,"in")
        declarePort(wk,"out")
        declarePort(wk,"err")
        val ports = mutableMapOf(
            "in" to mutableListOf("World".toByteArray()),
            "out" to mutableListOf(),
            "err" to mutableListOf(),
            "log" to mutableListOf(),
        )
        println("=== Pass 1 ======================================================")
        wk.run(ports)
        portsPrinter.run(ports)
        ports["in"]=mutableListOf("error".toByteArray())
        println("=== Pass 2 ======================================================")
        wk.run(ports)
        portsPrinter.run(ports)
    }
}