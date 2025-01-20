import org.intellij.lang.annotations.Language

fun main(args: Array<String>) {
    @Language("mbrun")
    var test_string = """
        # hello
        # variables
        var MM="10"

        # worker instances
        instance config=libs/std.jar:org.marbleRun.std.ReadStaticJson file="settings.json"
        instance fanOut1=libs/std.jar:org.marbleRun.std.FanOut outputs="3"
        instance timer=libs/std.jar:org.marbleRun.std.Timer cron="*/10 * * * * *"
        instance counter=libs/std.jar:org.marbleRun.std.Counter
        instance log=libs/std.jar:org.marbleRun.sys.FileBuffer file="log.txt"
        instance stdout=libs/std.jar:org.marbleRun.sys.Stdout buffer=false
        instance stderr=libs/std.jar:org.marbleRun.sys.Stderr
        instance null=libs/std.jar:org.marbleRun.sys.DevNull
        instance worker1=project/my-entities.jar:a.b.c.d.PassThrough
        instance worker2=project/my-entities.jar:a.b.c.d.PassThrough

        # topology
        link copy config:out -> timer:config
        link copy config:out -> counter:config
        link move timer:out -> fanOut1:in
        link move fanOut1:out0 -> worker1:in
        link move fanOut1:out1 -> worker2:in
        link move fanOut1:out1 -> counter:in
        link move worker1:out -> stdout:in
        link move worker1:err -> stderr:in
        link move worker2:out -> log:in
        link move worker2:err -> null:in
        link move counter:out -> log:in
    """.trimIndent()
    println("Hello World!")

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")
}