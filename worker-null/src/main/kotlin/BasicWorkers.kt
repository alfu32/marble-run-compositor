package a.b.c.de.test

import eu.ec.oib.training.alferio.Worker
import java.time.LocalDateTime

class WorkerPrintPorts(): Worker() {
    init {
        this.declaredPorts = "in,out,err".split(',').toMutableList()
    }
    override fun run(ports: MutableMap<String, MutableList<ByteArray>>) {
        // println does nothing
        println("WorkerPrintPorts prints all the ports and their content")
        println("${this.jarPath} worker ${this.javaClass.name} port ")
        for (pnpv in ports) {
            var c = 0
            var t = pnpv.value.size
            println(" === port ${pnpv.key} having $t packages ============")
            for(v in pnpv.value) {
                println("   - [${(++c)}/$t]  ${v.toString(Charsets.UTF_8)}")
            }
        }
    }
}
class WorkerClearSink(): Worker() {
    init {
        this.declaredPorts = "in,out,err".split(',').toMutableList()
    }
    override fun run(ports: MutableMap<String, MutableList<ByteArray>>) {
        // println does nothing
        println("WorkerNull drains in,out and err ports")
        for(kv in ports) {
            kv.value.clear()
        }
    }
}
class WorkerMergeAll(): Worker() {
    init {
        this.declaredPorts = "in,out,err".split(',').toMutableList()
    }
    override fun run(ports: MutableMap<String, MutableList<ByteArray>>) {
        // println does nothing
        println("WorkerIdentity pipes everything to out ")
        val all = mutableListOf<ByteArray>()
        for( kv in ports){
            for(pack in kv.value){
                all.add(pack)
            }
        }
        ports.forEach { port, queue -> queue.clear() }
        ports["out"] = all
    }
}
class WorkerTimer(): Worker() {
    init {
        this.declaredPorts = "out".split(',').toMutableList()
    }
    override fun run(ports: MutableMap<String, MutableList<ByteArray>>) {
        // println does nothing
        println("WorkerTimer queues curent unix timestamp to out")
        ports["out"]!!.add(LocalDateTime.now().toString().toByteArray())
    }
}
class WorkerCounter(): Worker() {
    init {
        this.declaredPorts = "out".split(',').toMutableList()
    }
    private var counter:Long = 0L
    override fun run(ports: MutableMap<String, MutableList<ByteArray>>) {
        // println does nothing
        println("WorkerCounter counts every time it is invoked")
        ports["out"]!!.add((++counter).toString().toByteArray())
    }
}