package eu.ec.oib.training.alferio.mbrun.intellij.plugin

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider

class MbrunFile(viewProvider: FileViewProvider)
    : PsiFileBase(viewProvider, MbrunLanguage) {

    override fun getFileType() = MbrunFileType()
    override fun toString() = "MbrunFile:$name"
}