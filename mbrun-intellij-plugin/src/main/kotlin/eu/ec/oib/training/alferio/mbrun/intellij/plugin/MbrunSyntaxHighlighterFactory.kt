package eu.ec.oib.training.alferio.mbrun.intellij.plugin

import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.fileTypes.SingleLazyInstanceSyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class MbrunSyntaxHighlighterFactory : SingleLazyInstanceSyntaxHighlighterFactory() {
    override fun createHighlighter(project: Project?, virtualFile: VirtualFile?) =
        MbrunSyntaxHighlighter()
}
