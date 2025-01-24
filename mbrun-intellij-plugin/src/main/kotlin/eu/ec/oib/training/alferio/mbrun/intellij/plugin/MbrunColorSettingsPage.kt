package eu.ec.oib.training.alferio.mbrun.intellij.plugin

import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

class MbrunColorSettingsPage : ColorSettingsPage {
    private val descriptors = arrayOf(
        AttributesDescriptor("Keyword",MbrunSyntaxHighlighter.KEYWORD),
        AttributesDescriptor("Name Declaration",MbrunSyntaxHighlighter.NAME_DECLARATION),
        AttributesDescriptor("Reference",MbrunSyntaxHighlighter.REFERENCE),
        AttributesDescriptor("Map Key",MbrunSyntaxHighlighter.MAP_KEY),
        AttributesDescriptor("Map Value",MbrunSyntaxHighlighter.MAP_VALUE),
        AttributesDescriptor("Variable Or Instance",MbrunSyntaxHighlighter.VARIABLE_OR_INSTANCE),
        AttributesDescriptor("Text Literal",MbrunSyntaxHighlighter.TEXT_LITERAL),
        AttributesDescriptor("Package",MbrunSyntaxHighlighter.PACKAGE),
        AttributesDescriptor("Port",MbrunSyntaxHighlighter.PORT),
        AttributesDescriptor("Punctuation",MbrunSyntaxHighlighter.PUNCTUATION),
    )

    override fun getDisplayName(): String = "MBrun"
    override fun getIcon(): Icon? = null
    override fun getHighlighter() = MbrunSyntaxHighlighter()
    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = null
    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = descriptors
    override fun getColorDescriptors() = emptyArray<com.intellij.openapi.options.colors.ColorDescriptor>()
    override fun getDemoText(): String = """
        # hello
        # variables
        var MM="10"
        
        # worker instances
        instance config=libs/std.jar:org.marblerun.std.ReadStaticJson file=settings.json
        instance fanout1=libs/std.jar:org.marblerun.std.FanOut outputs=3
        instance timer=libs/std.jar:org.marblerun.std.Timer cron="*/10 * * * * *"
        instance counter=libs/std.jar:org.marblerun.std.Counter
        instance log=libs/std.jar:org.marblerun.sys.FileBuffer file="log.txt"
        instance stdout=libs/std.jar:org.marblerun.sys.Stdout
        instance stderr=libs/std.jar:org.marblerun.sys.Stderr
        instance null=libs/std.jar:org.marblerun.sys.DevNull
        instance worker1=project/a.b.c.d.Passthrough
        instance worker2=project/a.b.c.d.Passthrough
        
        # topology
        link copy config:out -> timer:config
        link copy config:out -> counter:config
        link move timer:out -> fanout1:in
        link move fanout1:out0 -> worker1:in
        link move fanout1:out1 -> worker2:in
        link move fanout1:out1 -> counter:in
        link move worker1:out -> stdout:in
        link move worker1:err -> stderr:in
        link move worker2:out -> log:in
        link move worker2:err -> null:in
        link move counter:out -> log:in
    """.trimIndent()

    fun getFileType(): FileType = MbrunFileType()
}
