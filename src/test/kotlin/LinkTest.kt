import eu.ec.oib.training.alferio.Link
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LinkTest {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun testRegex() {
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
        for(definition in definitions.lines()) {
            println(" ========================= =========================================")
            println(" - definition : $definition")
            val sourceDestination = definition.split("""\s*->\s*""".toRegex()).map { it.trim(' ') }
            println(" - sourceDestination : $sourceDestination")
        }
    }

    @Test
    fun testLinkFromString(){
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
        for(definition in definitions.lines()) {
            println(" ========================= =========================================")
            println(" - definition : $definition")
            val ln = Link.fromString(definition)
            println(" - link : $ln")
        }
    }
}