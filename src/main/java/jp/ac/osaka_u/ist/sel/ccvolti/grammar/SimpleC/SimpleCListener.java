// Generated from SimpleC.g4 by ANTLR 4.5.3
package jp.ac.osaka_u.ist.sel.ccvolti.grammar.SimpleC;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SimpleCParser}.
 */
public interface SimpleCListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SimpleCParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void enterCompilationUnit(SimpleCParser.CompilationUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimpleCParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void exitCompilationUnit(SimpleCParser.CompilationUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimpleCParser#translationUnit}.
	 * @param ctx the parse tree
	 */
	void enterTranslationUnit(SimpleCParser.TranslationUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimpleCParser#translationUnit}.
	 * @param ctx the parse tree
	 */
	void exitTranslationUnit(SimpleCParser.TranslationUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimpleCParser#externalDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterExternalDeclaration(SimpleCParser.ExternalDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimpleCParser#externalDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitExternalDeclaration(SimpleCParser.ExternalDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimpleCParser#functionDefinition}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDefinition(SimpleCParser.FunctionDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimpleCParser#functionDefinition}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDefinition(SimpleCParser.FunctionDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimpleCParser#compoundStatement}.
	 * @param ctx the parse tree
	 */
	void enterCompoundStatement(SimpleCParser.CompoundStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimpleCParser#compoundStatement}.
	 * @param ctx the parse tree
	 */
	void exitCompoundStatement(SimpleCParser.CompoundStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimpleCParser#parentheses}.
	 * @param ctx the parse tree
	 */
	void enterParentheses(SimpleCParser.ParenthesesContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimpleCParser#parentheses}.
	 * @param ctx the parse tree
	 */
	void exitParentheses(SimpleCParser.ParenthesesContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimpleCParser#baraces}.
	 * @param ctx the parse tree
	 */
	void enterBaraces(SimpleCParser.BaracesContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimpleCParser#baraces}.
	 * @param ctx the parse tree
	 */
	void exitBaraces(SimpleCParser.BaracesContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimpleCParser#brackets}.
	 * @param ctx the parse tree
	 */
	void enterBrackets(SimpleCParser.BracketsContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimpleCParser#brackets}.
	 * @param ctx the parse tree
	 */
	void exitBrackets(SimpleCParser.BracketsContext ctx);
}