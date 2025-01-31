import eu.ec.oib.training.alferio.Worker
/***imports***/
import org.mbrun.std.SequenceGeneratorWorker
import org.mbrun.std.CircularSequenceGeneratorWorker
import org.mbrun.std.FileLoggerWorker
import org.mbrun.std.BlackholeWorker
class Composite:Worker(){
    private val ports: MutableMap<String,MutableMap<String,MutableList<ByteArray>>> = mutableMapOf()
    /***classDeclarations***/
    private var MK:String="10"
    private val genNumber1:SequenceGeneratorWorker = SequenceGeneratorWorker()
    private val genNumber2:CircularSequenceGeneratorWorker = CircularSequenceGeneratorWorker()
    private val fanOut:CircularSequenceGeneratorWorker = CircularSequenceGeneratorWorker()
    private val logSink:FileLoggerWorker = FileLoggerWorker()
    private val devnull:BlackholeWorker = BlackholeWorker()

    override fun config(conf: Map<String,String>){
        /***config***/
        genNumber1.config(mutableMapOf("maxValue" to "$MK"))
        ports["genNumber1"]=mutableMapOf()
        genNumber2.config(mutableMapOf("maxValue" to "10"))
        ports["genNumber2"]=mutableMapOf()
        fanOut.config(mutableMapOf("outputs" to "2"))
        ports["fanOut"]=mutableMapOf()
        logSink.config(mutableMapOf("filename" to "application.log",
            "journaled" to "daily"))
        ports["logSink"]=mutableMapOf()
        devnull.config(mutableMapOf())
        ports["devnull"]=mutableMapOf()
        ports["genNumber1"]?.set("out", mutableListOf())
        ports["fanOut"]?.set("in", mutableListOf())
        ports["fanOut"]?.set("out0", mutableListOf())
        ports["logSink"]?.set("in", mutableListOf())
        ports["fanOut"]?.set("out1", mutableListOf())
        ports["devnull"]?.set("in", mutableListOf())
        ports["genNumber2"]?.set("out", mutableListOf())
        ports["logSink"]?.set("in", mutableListOf())

    }
    override fun run(ports: MutableMap<String,MutableList<ByteArray>>){
        super.run(ports)
        val defered = mutableListOf<()->Unit>()
        /***runPre***/
        genNumber1.run(this.ports["genNumber1"]!!)
        genNumber2.run(this.ports["genNumber2"]!!)
        fanOut.run(this.ports["fanOut"]!!)
        logSink.run(this.ports["logSink"]!!)
        devnull.run(this.ports["devnull"]!!)

        /***runPost***/
        genNumber1.run(this.ports["genNumber1"]!!)
        genNumber2.run(this.ports["genNumber2"]!!)
        fanOut.run(this.ports["fanOut"]!!)
        logSink.run(this.ports["logSink"]!!)
        devnull.run(this.ports["devnull"]!!)
        val fanOut_in = this.ports["fanOut"]?.get("in")
        if(!fanOut_in.isNullOrEmpty()) {
            val fanOut_in_first = fanOut_in.first()
            fanOut_in.add(fanOut_in_first)
            defered.add(fun(){
                fanOut_in.removeAt(0)
            })

        }
        val devnull_in = this.ports["devnull"]?.get("in")
        if(!devnull_in.isNullOrEmpty()) {
            val devnull_in_first = devnull_in.first()
            devnull_in.add(devnull_in_first)
            defered.add(fun(){
                devnull_in.removeAt(0)
            })

        }
        val logSink_in = this.ports["logSink"]?.get("in")
        if(!logSink_in.isNullOrEmpty()) {
            val logSink_in_first = logSink_in.first()
            logSink_in.add(logSink_in_first)
            defered.add(fun(){
                logSink_in.removeAt(0)
            })

        }

        for (df in defered){
            df()
        }
    }
}