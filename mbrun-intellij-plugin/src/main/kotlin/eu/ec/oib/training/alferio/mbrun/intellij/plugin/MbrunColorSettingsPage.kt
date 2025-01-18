package eu.ec.oib.training.alferio.mbrun.intellij.plugin

import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

class MbrunColorSettingsPage : ColorSettingsPage {
    private val descriptors = arrayOf(
        AttributesDescriptor("Keyword", MbrunSyntaxHighlighter.KEYWORD),
        AttributesDescriptor("Variables/Instances", MbrunSyntaxHighlighter.VARIABLE_OR_INSTANCE),
        AttributesDescriptor("String Literals", MbrunSyntaxHighlighter.TEXT_LITERAL),
        AttributesDescriptor("Ports", MbrunSyntaxHighlighter.PORT),
        AttributesDescriptor("Punctuation", MbrunSyntaxHighlighter.PUNCTUATION)
    )

    override fun getDisplayName(): String = "MBrun"
    override fun getIcon(): Icon? = null
    override fun getHighlighter() = MbrunSyntaxHighlighter()
    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = null
    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = descriptors
    override fun getColorDescriptors() = emptyArray<com.intellij.openapi.options.colors.ColorDescriptor>()
    override fun getDemoText(): String = """
        # comment
        var MK="10"
        instance genNumber1 = libs/stdlib.jar:com.mbrun.SequenceGeneratorWorker maxValue="$MK"
        link copy genNumber1 -> logSink:in
        link move genNumber1:out -> devnull:in
    """.trimIndent()

    override fun getFileType(): FileType = MbrunFileType
}
