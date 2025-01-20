package eu.ec.oib.training.alferio.mbrun.intellij.plugin


import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

object MbrunAstFactory {
    fun createElement(node: ASTNode?): PsiElement? {
        // If you want specific element classes for certain node types, do something like:
        // val type = node?.elementType
        // if (type == MbrunTokens.KEYWORD_VAR) return SomeCustomPsiElement(node)
        return null
    }
}