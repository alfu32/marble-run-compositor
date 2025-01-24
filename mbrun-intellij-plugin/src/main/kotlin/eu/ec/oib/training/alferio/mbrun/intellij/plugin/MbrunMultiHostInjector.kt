package eu.ec.oib.training.alferio.mbrun.intellij.plugin



import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl // for Java string literal
import com.intellij.openapi.util.TextRange
import com.intellij.psi.InjectedLanguagePlaces
import com.intellij.psi.LanguageInjector

class MbrunMultiHostInjector : MultiHostInjector,LanguageInjector {

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

    /**
     * Called for each PSI element that belongs to the classes in elementsToInjectIn().
     * If it's a suitable context, add an InjectedLanguagePlace so IntelliJ treats
     * the string as MBrun code.
     */
    override fun getLanguagesToInject(context: PsiLanguageInjectionHost, places: InjectedLanguagePlaces) {
        // 1) Verify it's a valid Java string literal, or check your own condition
        if (context !is PsiLiteralExpressionImpl) return

        // e.g. You could check if the literal starts with a special marker
        val text = context.text
        if (!text.startsWith("\"") || !text.endsWith("\"")) return

        // 2) Decide whether or not to inject. For example, you might check an annotation,
        // or do a quick test:
        // if (!someCondition(text)) return

        // 3) If you want to inject MBrunLanguage for the entire string contents
        // (excluding the quotes), do:
        val language = MbrunLanguage  // your Language object
        val startOffset = 1
        val endOffset = text.length - 1

        places.addPlace(
            language,
            TextRange(startOffset, endOffset),
            null,
            null
        )
    }
}
