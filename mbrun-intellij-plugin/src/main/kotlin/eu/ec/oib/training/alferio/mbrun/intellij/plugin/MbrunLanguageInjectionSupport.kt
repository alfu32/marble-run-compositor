package eu.ec.oib.training.alferio.mbrun.intellij.plugin

import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Factory
import com.intellij.openapi.util.Ref
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.ui.SimpleColoredText
import com.intellij.util.Consumer
import org.intellij.plugins.intelliLang.Configuration
import org.intellij.plugins.intelliLang.inject.LanguageInjectionSupport
import org.intellij.plugins.intelliLang.inject.config.BaseInjection
import org.jdom.Element

class MbrunLanguageInjectionSupport : LanguageInjectionSupport() {

    // This ID must be unique and is typically the same as your language ID or something similar
    override fun getId(): String = "MBRUN_INJECTION_SUPPORT"

    // override fun getDisplayName(): String = "MBrun Injections"

    // If you have custom patterns (like string-literals in a certain PSI context), you can return them.
    // For a basic scenario, just return an empty array or no special patterns.
    override fun getPatternClasses(): Array<Class<out PsiElementPattern<*, *>>> = emptyArray()


    // Called to see if injection is applicable to a certain PSI host (e.g. a string literal)
    override fun isApplicableTo(host: PsiLanguageInjectionHost): Boolean = true
    override fun useDefaultInjector(p0: PsiLanguageInjectionHost?): Boolean = true
    @Deprecated("because the parent is deprectaed")
    override fun useDefaultCommentInjector(): Boolean = true

    @Deprecated("because the parent is deprectaed")
    override fun findCommentInjection(p0: PsiElement, p1: Ref<in PsiElement>?): BaseInjection? = BaseInjection("MBRUN_INJECTION_DOCUMENT")

    override fun addInjectionInPlace(p0: Language?, p1: PsiLanguageInjectionHost?): Boolean = true

    override fun removeInjectionInPlace(p0: PsiLanguageInjectionHost?): Boolean = true

    override fun editInjectionInPlace(p0: PsiLanguageInjectionHost?): Boolean = true

    override fun createInjection(p0: Element?): BaseInjection = BaseInjection("MBRUN_INJECTION_SINGLETON")

    override fun setupPresentation(p0: BaseInjection?, p1: SimpleColoredText?, p2: Boolean) {
        //TODO("Not yet implemented")
    }

    override fun createSettings(p0: Project?, p1: Configuration?): Array<Configurable> {
        //TODO("Not yet implemented")
        return arrayOf()
    }

    override fun createAddActions(p0: Project?, p1: Consumer<in BaseInjection>?): Array<AnAction> {
        //TODO("Not yet implemented")
        return arrayOf()
    }

    override fun createEditAction(p0: Project?, p1: Factory<out BaseInjection>?): AnAction {
        //TODO("Not yet implemented")
        return object : AnAction(){
            override fun actionPerformed(p0: AnActionEvent) {
                // TODO("Not yet implemented")
            }
        }
    }
}