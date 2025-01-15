package a.b.c.de.testTest

import eu.ec.oib.training.alferio.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class WorkerKtTest {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    class WkPrn(): Worker() {
        init {
            this.declaredPorts = "in,out,err".split(',').toMutableList()
            this.jarPath=""
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
    @Test
    fun testGetFirstWorkerInstance() {
        val wks = getWorkers("src/test/resources/workers-lib-std-1.0-SNAPSHOT.jar")
        val prn = WkPrn()
        for (wk in wks) {
            val ports = mutableMapOf(
                "in" to mutableListOf<ByteArray>("test package in 00001".toByteArray()),
                "out" to mutableListOf<ByteArray>("test package out 00001".toByteArray()),
                "error" to mutableListOf<ByteArray>("test package error 00001".toByteArray()),
            )
            println("====== running worker ${wk.key} ==================================")
            prn.run(ports)
            wk.value.run(ports)
            prn.run(ports)
            println("------ result ----------------------------------------------------")
        }
    }

    @Test
    fun testDeclarePort() {
        val wk = object : Worker() {
            override var declaredPorts = "a,b,c,d,e".split(",").toMutableList()
            override fun run(ports: MutableMap<String, MutableList<ByteArray>>) {
                println("nothin'")
            }

        }
        println(wk.declaredPorts)
        declarePort(wk,"coucou")
        println(wk.declaredPorts)
    }

    @Test
    fun testAddWorkerInstance() {
        val wks = getWorkers("src/test/resources/workers-lib-std-1.0-SNAPSHOT.jar")
        println(wks)
        val composite = CompositeWorker()
        println(composite.workers)
        addWorkerInstance(composite,"src/test/resources/workers-lib-std-1.0-SNAPSHOT.jar","a.b.c.de.test.WorkerCounter")
        println(composite.workers)
        addWorkerInstance(composite,"src/test/resources/workers-lib-std-1.0-SNAPSHOT.jar","a.b.c.de.test.WorkerClearSink")
        println(composite.workers)
    }
}