package eu.ec.oib.training.alferio.mbrun.intellij.plugin

import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object MbrunLanguage : Language("MBRUN") {
    private fun readResolve(): Any = MbrunLanguage
}

class MbrunFileType : LanguageFileType(MbrunLanguage) {
    override fun getName(): String = "mbrun file"
    override fun getDescription(): String = "mbrun script file"
    override fun getDefaultExtension(): String = "mbrun"
    override fun getIcon(): Icon? = null // You can provide a custom icon if you want
}