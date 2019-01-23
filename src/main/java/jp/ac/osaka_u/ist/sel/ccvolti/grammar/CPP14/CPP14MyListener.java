package jp.ac.osaka_u.ist.sel.ccvolti.grammar.CPP14;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;

public interface CPP14MyListener {
	
	public void visitFunction(TokenStream tokens, Token functionName, int start, int end);
	
	public void visitSelectionStatement(TokenStream tokens, Token blockName, int start, int end);
	
	public void visitIterationStatement(TokenStream tokens, Token blockName, int start, int end);
	
	public void visitUnknownBlock(TokenStream tokens, Token blockName, int start, int end);
	
}
