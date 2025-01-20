package eu.ec.oib.training.alferio.mbrun.intellij.plugin



import com.intellij.lang.*
import com.intellij.lexer.Lexer
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.*
import org.jetbrains.annotations.NotNull
import com.intellij.extapi.psi.ASTWrapperPsiElement

class MbrunParserDefinition : ParserDefinition {

    // The top-level node for `.mbrun` files
    companion object {
        val FILE = IFileElementType(MbrunLanguage)
    }

    // Create a lexer for highlighting/parsing
    @NotNull
    override fun createLexer(project: com.intellij.openapi.project.Project?): Lexer {
        return MbrunLexer()
    }

    // If you want a real parser, you can return an actual PsiParser here.
    // If you have no parser, you could return a "DummyParser" that doesn't do much.
    @NotNull
    override fun createParser(project: com.intellij.openapi.project.Project?): PsiParser {
        return PsiParser { root, builder ->
            // minimal "parse" that consumes all tokens
            val rootMarker = builder.mark()
            while (!builder.eof()) {
                builder.advanceLexer()
            }
            rootMarker.done(root)
            return@PsiParser builder.treeBuilt
        }
    }

    // Return the file node type
    @NotNull
    override fun getFileNodeType(): IFileElementType {
        return FILE
    }

    // Token sets that define comments, strings, etc. for your language
    @NotNull
    override fun getCommentTokens(): TokenSet {
        return TokenSet.create(MbrunTokens.COMMENT)
    }

    @NotNull
    override fun getStringLiteralElements(): TokenSet {
        return TokenSet.create(MbrunTokens.STRING_LITERAL)
    }

    // Create a PsiFile for your language
    @NotNull
    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return MbrunFile(viewProvider)
    }

    // If you had custom AST node creation, you'd return it here.
    // For a minimal approach, just return "null" => use default.
    override fun createElement(node: ASTNode?): PsiElement {
        return MbrunAstFactory.createElement(node) ?: ASTWrapperPsiElement(node!!)
    }

    // Language auto-indent (optional). "SPACE" or others
    @Deprecated("because one of its supers is deprecated",
        ReplaceWith("ParserDefinition.SpaceRequirements.MAY", "com.intellij.lang.ParserDefinition")
    )
    override fun spaceExistanceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): ParserDefinition.SpaceRequirements {
        return ParserDefinition.SpaceRequirements.MAY
    }
}