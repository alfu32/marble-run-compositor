package eu.ec.oib.training.alferio.mbrun.intellij.plugin



import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl // for Java string literal
import com.intellij.openapi.util.TextRange

class MbrunMultiHostInjector : MultiHostInjector {

    override fun elementsToInjectIn(): List<Class<out PsiElement>> {
        // Return the PSI element classes you want to inject into.
        // For Java string literals, it's PsiLiteralExpressionImpl, or just PsiLiteralExpression::class.java
        return listOf(PsiLiteralExpressionImpl::class.java)
    }

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        // Check if this is indeed a string literal
        if (context is PsiLanguageInjectionHost && context.text.startsWith("\"") && context.text.endsWith("\"")) {
            // Example: inject the entire literal’s contents as MBrun
            val range = TextRange(1, context.textLength - 1) // exclude the quotes

            registrar
                .startInjecting(MbrunLanguage)  // use your MbrunLanguage object
                // addPlace(prefix, suffix, host, textRange)
                .addPlace("", "", context, range)
                .doneInjecting()
        }
    }
}
