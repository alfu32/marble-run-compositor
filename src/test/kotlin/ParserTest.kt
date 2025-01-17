import eu.ec.oib.training.alferio.ParseException
import eu.ec.oib.training.alferio.Parser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ParserTest {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }
    @Test
    fun testParseScript01(){

        val scriptText = """
        # this is an example script without errors
        
        # variables
        var MK="10"
        
        # worker instances
        instance genNumber1 = libs/stdlib.jar:com.mbrun.SequenceGeneratorWorker maxValue="${'$'}MK"
        instance genNumber2 = libs/stdlib.jar:com.mbrun.CircularSequenceGeneratorWorker maxValue=10
        instance fanOut = libs/stdlib.jar:com.mbrun.CircularSequenceGeneratorWorker outputs="2"
        instance logSink = libs/stdlib.jar:com.mbrun.FileLoggerWorker filename="application.log" journaled="daily"
        instance devnull = libs/stdlib.jar:com.mbrun.BlackholeWorker
        
        # links
        link move genNumber1:out -> fanOut:in
        link copy fanOut:out0 -> logSink:in
        link move fanOut:out1 -> devnull:in
        link move genNumber2:out -> logSink:in
    """.trimIndent()

        val ast = Parser().parse(scriptText)

        // Now do something with 'ast'
        println(ast)
    }
    @Test
    fun testParseScript02_multiline(){

        val scriptText = """
        # this is an example script without errors
        
        # variables
        var MK="10"
        
        # worker instances
        instance genNumber1 = libs/stdlib.jar:com.mbrun.SequenceGeneratorWorker maxValue="${'$'}MK"
        instance genNumber2 = libs/stdlib.jar:com.mbrun.CircularSequenceGeneratorWorker maxValue=10
        instance fanOut = libs/stdlib.jar:com.mbrun.CircularSequenceGeneratorWorker outputs="2"
        instance logSink = libs/stdlib.jar:com.mbrun.FileLoggerWorker \
                 filename="application.log" \
                 journaled="daily"
        instance devnull = libs/stdlib.jar:com.mbrun.BlackholeWorker
        
        # links
        link move genNumber1:out -> fanOut:in
        link copy fanOut:out0 -> logSink:in
        link move fanOut:out1 -> devnull:in
        link move genNumber2:out -> logSink:in
    """.trimIndent()

        try{
            val ast = Parser().parse(scriptText)
            // Now do something with 'ast'
            println(ast)
        }catch(x:ParseException){
            println(x.message)
        }
    }
    @Test
    fun testParseScript02_duplicate_variable(){

        val scriptText = """
            # this is an example script with errors
            
            # variables
            var MK="10"
            var A="11"
            var B="12"
            var MK="not my problem"
        """.trimIndent()
        try{
            val ast = Parser().parse(scriptText)
            // Now do something with 'ast'
            println(ast)
        }catch(x:ParseException){
            println(x.message)
        }
    }
    @Test
    fun testParseScript02_duplicate_instance(){

        val scriptText = """
            # this is an example script without errors
            
            # worker instances
            instance genNumber1 = libs/stdlib.jar:com.mbrun.SequenceGeneratorWorker maxValue="${'$'}MK"
            instance genNumber2 = libs/stdlib.jar:com.mbrun.CircularSequenceGeneratorWorker maxValue=10
            instance fanOut = libs/stdlib.jar:com.mbrun.CircularSequenceGeneratorWorker outputs="2"
            instance logSink = libs/stdlib.jar:com.mbrun.FileLoggerWorker filename="application.log" journaled="daily"
            instance devnull = libs/stdlib.jar:com.mbrun.BlackholeWorker
            instance genNumber1 = libs/stdlib.jar:com.mbrun.CircularSequenceGeneratorWorker maxValue=10
        """.trimIndent()
        try{
            val ast = Parser().parse(scriptText)
            // Now do something with 'ast'
            println(ast)
        }catch(x:ParseException){
            println(x.message)
        }
    }
    @Test
    fun testParseScript02_duplicate_link(){

        val scriptText = """
            # this is an example script with errors
            
            # links
            link move genNumber1:out -> fanOut:in
            link copy fanOut:out0 -> logSink:in
            link move fanOut:out1 -> devnull:in
            link move genNumber2:out -> logSink:in
            link copy genNumber1:out -> fanOut:in
        """.trimIndent()
        try{
            val ast = Parser().parse(scriptText)
            // Now do something with 'ast'
            println(ast)
        }catch(x:ParseException){
            println(x.message)
        }
    }
}