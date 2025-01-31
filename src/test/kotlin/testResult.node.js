import Worker from './eu.ec.oib.training.alferio.Worker'
/***imports***/
import SequenceGeneratorWorker from './org.mbrun.std.SequenceGeneratorWorker'
import CircularSequenceGeneratorWorker from './org.mbrun.std.CircularSequenceGeneratorWorker'
import FileLoggerWorker from './org.mbrun.std.FileLoggerWorker'
import BlackholeWorker from './org.mbrun.std.BlackholeWorker'

export default function config(conf){
    const ports = {}
    /***classDeclarations***/
    var MK="10"
    var genNumber1 = SequenceGeneratorWorker({"maxValue": "$MK"})
    var genNumber2 = CircularSequenceGeneratorWorker({"maxValue": "10"})
    var fanOut = CircularSequenceGeneratorWorker({"outputs": "2"})
    var logSink = FileLoggerWorker({"filename": "application.log",
        "journaled": "daily"})
    var devnull = BlackholeWorker({})

    /***config***/
    ports["genNumber1"]={}
    ports["genNumber2"]={}
    ports["fanOut"]={}
    ports["logSink"]={}
    ports["devnull"]={}
    ports["genNumber1"]["out"]=[]
    ports["fanOut"]["in"]=[]
    ports["fanOut"]["out0"]=[]
    ports["logSink"]["in"]=[]
    ports["fanOut"]["out1"]=[]
    ports["devnull"]["in"]=[]
    ports["genNumber2"]["out"]=[]
    ports["logSink"]["in"]=[]

    return function (ports){
        const defered = []
        /***runPre***/
        genNumber1(this.ports["genNumber1"])
        genNumber2(this.ports["genNumber2"])
        fanOut(this.ports["fanOut"])
        logSink(this.ports["logSink"])
        devnull(this.ports["devnull"])

        /***runPost***/
        genNumber1(this.ports["genNumber1"])
        genNumber2(this.ports["genNumber2"])
        fanOut(this.ports["fanOut"])
        logSink(this.ports["logSink"])
        devnull(this.ports["devnull"])
        const fanOut_in = this.ports["fanOut"]["in"]
        if(!fanOut_in.isNullOrEmpty()) {
            const fanOut_in_first = fanOut_in.first()
            fanOut_in.push(fanOut_in_first)
            defered.push(function(){
                fanOut_in.pop()
            })

        }
        const devnull_in = this.ports["devnull"]["in"]
        if(!devnull_in.isNullOrEmpty()) {
            const devnull_in_first = devnull_in.first()
            devnull_in.push(devnull_in_first)
            defered.push(function(){
                devnull_in.pop()
            })
        }
        const logSink_in = this.ports["logSink"]["in"]
        if(!logSink_in.isNullOrEmpty()) {
            const logSink_in_first = logSink_in.first()
            logSink_in.push(logSink_in_first)
            defered.push(function(){
                logSink_in.pop()
            })
        }

        defered.forEach((fn) => fn())
    }
}