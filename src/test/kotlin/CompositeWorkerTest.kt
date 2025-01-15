import eu.ec.oib.training.alferio.CompositeWorker
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.lang.Thread.sleep

class CompositeWorkerTest {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun test_create() {
        val dirPkgRef = "src/test/resources/workers-lib-std-1.0-SNAPSHOT.jar:a.b.c.de.test"
        val definitions ="""
            $dirPkgRef.WorkerCounter:out -> $dirPkgRef.WorkerMultiply:in
            $dirPkgRef.WorkerMultiply:out -> $dirPkgRef.WorkerPipethrough:in
            $dirPkgRef.WorkerMultiply:out2 -> $dirPkgRef.WorkerPipethrough:in
            $dirPkgRef.WorkerMultiply:out3 -> $dirPkgRef.WorkerPipethrough:in
            $dirPkgRef.WorkerPipethrough:out -> $dirPkgRef.WorkerPrintPorts:in
            $dirPkgRef.WorkerPipethrough:out2 -> $dirPkgRef.WorkerPrintPorts:in
            $dirPkgRef.WorkerPipethrough:out3 -> $dirPkgRef.WorkerPrintPorts:in
        """.trimIndent()
        val cw = CompositeWorker.fromGraph(definitions)
        println(cw.workers)
        println(cw.links)
    }
    @Test
    fun test_run() {
        val dirPkgRef = "src/test/resources/workers-lib-std-1.0-SNAPSHOT.jar:a.b.c.de.test"
        val definitions ="""
            $dirPkgRef.WorkerTimer:out -> $dirPkgRef.WorkerMultiply:in
            $dirPkgRef.WorkerMultiply:out -> $dirPkgRef.WorkerPipethrough:in
            $dirPkgRef.WorkerMultiply:out2 -> $dirPkgRef.WorkerPipethrough:in
            $dirPkgRef.WorkerMultiply:out3 -> $dirPkgRef.WorkerPipethrough:in
            $dirPkgRef.WorkerPipethrough:out -> $dirPkgRef.WorkerPrintPorts:in
            $dirPkgRef.WorkerPipethrough:out2 -> $dirPkgRef.WorkerPrintPorts:in
            $dirPkgRef.WorkerPipethrough:out3 -> $dirPkgRef.WorkerPrintPorts:in
        """.trimIndent()
        val cw = CompositeWorker.fromGraph(definitions)
        println(cw.workers)
        println(cw.links)
        println(cw.workerPorts)
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