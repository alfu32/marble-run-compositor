import eu.ec.oib.training.alferio.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class KotlinClassCompilerTest {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun testParseScript01() {


        val scriptText = """
            # this is an example script without errors
            
            # variables
            var MK="10"
            
            # worker instances
            instance genNumber1 = libs/stdlib.jar:org.mbrun.std.SequenceGeneratorWorker maxValue="${'$'}MK"
            instance genNumber2 = libs/stdlib.jar:org.mbrun.std.CircularSequenceGeneratorWorker maxValue=10
            instance fanOut = libs/stdlib.jar:org.mbrun.std.CircularSequenceGeneratorWorker outputs="2"
            instance logSink = libs/stdlib.jar:org.mbrun.std.FileLoggerWorker filename="application.log" journaled="daily"
            instance devnull = libs/stdlib.jar:org.mbrun.std.BlackholeWorker
            
            # links
            link move genNumber1:out -> fanOut:in
            link copy fanOut:out0 -> logSink:in
            link move fanOut:out1 -> devnull:in
            link move genNumber2:out -> logSink:in
        """.trimIndent()

        val ast: Script = Parser().parse(scriptText)
        // Now do something with 'ast'
        println(ast)
        val kc = KotlinClassCompiler()
        val compiled = kc.compile(ast)
        println(compiled)
    }

    @Test
    fun testParseScript02() {


        val scriptText = """
            # this is an example script without errors
            
            # variables
            var MK="10"
            
            # worker instances
            instance genNumber1 = libs/stdlib.jar:org.mbrun.std.SequenceGeneratorWorker maxValue="${'$'}MK"
            instance genNumber2 = libs/stdlib.jar:org.mbrun.std.CircularSequenceGeneratorWorker maxValue=10
            instance fanOut = libs/stdlib.jar:org.mbrun.std.CircularSequenceGeneratorWorker outputs="2"
            instance logSink = libs/stdlib.jar:org.mbrun.std.FileLoggerWorker filename="application.log" journaled="daily"
            instance devnull = libs/stdlib.jar:org.mbrun.std.BlackholeWorker
            
            # links
            link move genNumber1:out -> fanOut:in
            link copy fanOut:out0 -> logSink:in
            link move fanOut:out1 -> devnull:in
            link move genNumber2:out -> logSink:in
        """.trimIndent()

        val ast: Script = Parser().parse(scriptText)
        // Now do something with 'ast'
        println(ast)
        val kc = JavascriptCompiler()
        val compiled = kc.compile(ast)
        println(compiled)
    }
}
