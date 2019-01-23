package jp.ac.osaka_u.ist.sel.ccvolti.grammar.C;

import java.util.ArrayList;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

public class CMyListener extends CBaseListener{
	ArrayList<Token> tokenList;
	public CMyListener(ArrayList<Token> tokenList) {
		this.tokenList = tokenList;
	}

	@Override
	public void visitTerminal(TerminalNode node) {
		tokenList.add(node.getSymbol());
	}
	
}
