package a.b.c.de.test

import eu.ec.oib.training.alferio.Worker
import java.time.LocalDateTime


class WorkerFanOut(
    var portsCount:Int = 3
): Worker() {
    private var branches:MutableList<String>
    init {
        val n = portsCount.toString().length
        this.branches = (1..portsCount).map { "out${java.lang.String.format("%0${n}",portsCount)}" }.toMutableList()
        this.declaredPorts = mutableListOf("in","err")
        this.declaredPorts.addAll(this.branches)
    }

    override fun run(ports: MutableMap<String, MutableList<ByteArray>>) {
        super.run(ports)
        // println does nothing
        println("WorkerFanOut duplicates each packet from in to ${this.branches.joinToString(",")}")
        for(pack in ports["in"]!!){
            for (outputKey in branches) {
                ports[outputKey]!!.add(pack)
            }
        }
    }
}

class WorkerPipethrough(): Worker() {
    init {
        this.declaredPorts = "in,out,err".split(',').toMutableList()
    }
    override fun run(ports: MutableMap<String, MutableList<ByteArray>>) {
        super.run(ports)
        // println does nothing
        println("WorkerPipethrough pipes in to out through")
        val all = mutableListOf<ByteArray>()
        for(pack in ports["in"]!!){
            ports["out"]!!.add(pack)
        }
    }
}

class WorkerPrintPorts(): Worker() {
    init {
        this.declaredPorts = "in,out,err".split(',').toMutableList()
    }
    override fun run(ports: MutableMap<String, MutableList<ByteArray>>) {
        super.run(ports)
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
        super.run(ports)
        // println does nothing
        println("WorkerNull drains in,out and err ports")
        for(kv in ports) {
            kv.value.clear()
        }
    }
}
class WorkerMergeAll(
    var targetPort: String = "out"
): Worker() {
    init {
        this.declaredPorts = "in,$targetPort,err".split(',').toMutableList()
    }
    override fun run(ports: MutableMap<String, MutableList<ByteArray>>) {
        super.run(ports)
        // println does nothing
        println("WorkerIdentity pipes everything to out ")
        val all = mutableListOf<ByteArray>()
        for( kv in ports){
            if(kv.key != targetPort) {
                for (pack in kv.value) {
                    all.add(pack)
                }
            }
        }
        ports.forEach { port, queue -> queue.clear() }
        ports[targetPort] = all
    }
}
class WorkerTimer(
    var interval:String=""
): Worker() {
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
        counter+=1
        println("WorkerCounter counts every time it is invoked")
        ports["out"]!!.add((counter).toString().toByteArray())
    }
}