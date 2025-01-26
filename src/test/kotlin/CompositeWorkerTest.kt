import eu.ec.oib.training.alferio.CompositeWorker
import eu.ec.oib.training.alferio.Worker
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.lang.Thread.sleep
import kotlin.properties.Delegates

class Wk00: Worker(){
    var counter:Int=1
    override fun config(conf: Map<String, String>) {
        if("counter" in conf) {
            counter=conf["counter"]!!.toInt()
        }
    }

    override fun run(ports: MutableMap<String, MutableList<ByteArray>>) {
        super.run(ports)
        println("blip $counter")
        counter+=1
    }
}

class CompositeWorkerTest {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun test_create_from_script() {
        val dirPkgRef = "workers-lib-std/build/libs/workers-lib-std-1.0-SNAPSHOT.jar:a.b.c.de.test"
        val definitions ="""
            ## instances
            instance counter0 =:Wk00 count=12
            instance counter = workers-lib-std/build/libs/workers-lib-std-1.0-SNAPSHOT.jar:a.b.c.de.test.WorkerCounter ring=true max=10
            instance fanout = workers-lib-std/build/libs/workers-lib-std-1.0-SNAPSHOT.jar:a.b.c.de.test.WorkerFanOut outputs=3
            instance pipe1 = workers-lib-std/build/libs/workers-lib-std-1.0-SNAPSHOT.jar:a.b.c.de.test.WorkerPipethrough
            instance pipe2 = workers-lib-std/build/libs/workers-lib-std-1.0-SNAPSHOT.jar:a.b.c.de.test.WorkerPipethrough
            instance print = workers-lib-std/build/libs/workers-lib-std-1.0-SNAPSHOT.jar:a.b.c.de.test.WorkerPrintPorts
            
            ## links
            link move counter:out -> fanout:in
            link move fanout:out -> pipe1:in
            link move fanout:out2 -> pipe1:in
            link move fanout:out3 -> pipe1:in
            link move pipe1:out -> print:in
            link move pipe1:out2 -> print:in
            link copy pipe2:out3 -> print:in
        """.trimIndent()
        val cw = CompositeWorker.fromScript(definitions)
        println("workers : \n ${cw.workers}")
        println("links : \n ${cw.links}")
        println("workerPorts : \n ${cw.workerPorts}")
    }
    @Test
    fun test_run() {
        val dirPkgRef = "workers-lib-std/build/libs/workers-lib-std-1.0-SNAPSHOT.jar:a.b.c.de.test"
        val definitions ="""
            ## instances
            instance counter = workers-lib-std/build/libs/workers-lib-std-1.0-SNAPSHOT.jar:a.b.c.de.test.WorkerCounter ring=true max=10
            instance fanout = workers-lib-std/build/libs/workers-lib-std-1.0-SNAPSHOT.jar:a.b.c.de.test.WorkerFanOut outputs=3
            instance pipe1 = workers-lib-std/build/libs/workers-lib-std-1.0-SNAPSHOT.jar:a.b.c.de.test.WorkerPipethrough
            instance pipe2 = workers-lib-std/build/libs/workers-lib-std-1.0-SNAPSHOT.jar:a.b.c.de.test.WorkerPipethrough
            instance print = workers-lib-std/build/libs/workers-lib-std-1.0-SNAPSHOT.jar:a.b.c.de.test.WorkerPrintPorts
            
            ## links
            link move counter:out -> fanout:in
            link move fanout:out -> pipe1:in
            link move fanout:out2 -> pipe1:in
            link move fanout:out3 -> pipe1:in
            link move pipe1:out -> print:in
            link move pipe1:out2 -> print:in
            link move pipe2:out3 -> print:in
        """.trimIndent()
        val cw = CompositeWorker.fromScript(definitions)
        println("workers : \n ${cw.workers}")
        println("links : \n ${cw.links}")
        println("workerPorts : \n ${cw.workerPorts}")
        for( iter in 0..9) {
            println("::::::::::::::::: ::: ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::")
            println("::::::::::::::::: $iter/9 ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::")
            println("::::::::::::::::: ::: ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::")
            println()
            cw.run(mutableMapOf())
            println()
            sleep(1000)
        }
    }
}