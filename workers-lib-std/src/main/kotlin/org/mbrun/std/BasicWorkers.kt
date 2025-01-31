package org.mbrun.std

import eu.ec.oib.training.alferio.Worker
import java.time.LocalDateTime


class WorkerFanOut(
): Worker() {
    private var portsCount:Int=1
    private lateinit var branches:MutableList<String>

    override fun config(conf: Map<String, String>) {
        portsCount = conf["ports"]?.toInt()?:1

        val n = portsCount.toString().length
        this.branches = (1..portsCount).map { "out$n" }.toMutableList()
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

    override fun config(conf: Map<String, String>) {
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
    override fun config(conf: Map<String, String>) {
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
    override fun config(conf: Map<String, String>) {
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
): Worker() {
    private lateinit var targetPort: String

    override fun config(conf: Map<String, String>) {
        targetPort=conf["targetPort"]?:"out"
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
): Worker() {
    private lateinit var interval:String

    override fun config(conf: Map<String, String>) {
        this.declaredPorts = "out".split(',').toMutableList()
        interval = conf["interval"]?:""
    }

    override fun run(ports: MutableMap<String, MutableList<ByteArray>>) {
        // println does nothing
        println("WorkerTimer queues current unix timestamp to out")
        ports["out"]!!.add(LocalDateTime.now().toString().toByteArray())
    }
}
class WorkerCounter(): Worker() {
    private var counter:Long=0
    private var min:Long=0
    private var max:Long=10
    private var ring:Boolean=true
    override fun config(conf: Map<String, String>) {
        min=(conf["min"]?.toLong()?:0)
        max=(conf["max"]?.toLong()?:10)
        counter=(conf["init"]?.toLong()?:min)
        this.declaredPorts = "out".split(',').toMutableList()
    }

    override fun run(ports: MutableMap<String, MutableList<ByteArray>>) {
        // println does nothing

        counter = if(ring) if(counter>max) min else counter+1 else counter+1

        println("WorkerCounter counts every time it is invoked")
        ports["out"]!!.add((counter).toString().toByteArray())
    }
}


class SequenceGeneratorWorker:Worker(){
    override fun config(conf: Map<String, String>) {
        //TODO("Not yet implemented")
    }
}
class CircularSequenceGeneratorWorker:Worker(){
    override fun config(conf: Map<String, String>) {
        //TODO("Not yet implemented")
    }
}
class FileLoggerWorker:Worker(){
    override fun config(conf: Map<String, String>) {
        //TODO("Not yet implemented")
    }
}
class BlackholeWorker:Worker(){
    override fun config(conf: Map<String, String>) {
        //TODO("Not yet implemented")
    }
}