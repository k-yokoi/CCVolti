package jp.ac.osaka_u.ist.sel.ccvolti.grammar.CPP14;

import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.*;

import jp.ac.osaka_u.ist.sel.ccvolti.CloneDetector;
import jp.ac.osaka_u.ist.sel.ccvolti.model.Block;
import jp.ac.osaka_u.ist.sel.ccvolti.model.Method;

public class CPP14MyParser{
	TokenStream _tokens;
	private List<CPP14MyListener> _parseListeners;
	BlockTree currentNode;
	private Stack<BlockTree> _save = new Stack<BlockTree>();
	Token BlockName = null;
	
	
	private static final int
		Identifier=CPP14Lexer.Identifier, Assign=CPP14Lexer.Assign, Case=CPP14Lexer.Case, Default=CPP14Lexer.Default,
		If=CPP14Lexer.If, Else=CPP14Lexer.Else, Switch=CPP14Lexer.Switch,
		While=CPP14Lexer.While, Do=CPP14Lexer.Do, For=CPP14Lexer.For,
		Goto=CPP14Lexer.Goto, Continue=CPP14Lexer.Continue, Break=CPP14Lexer.Break, Return=CPP14Lexer.Return,
		LeftParen=CPP14Lexer.LeftParen, RightParen=CPP14Lexer.RightParen,
		LeftBrace=CPP14Lexer.LeftBrace, RightBrace=CPP14Lexer.RightBrace, Colon=CPP14Lexer.Colon, Semi=CPP14Lexer.Semi, EOF=IntStream.EOF;
	
	public CPP14MyParser(TokenStream tokens) {
		this._tokens = tokens;
		//_tokens.fill();
	}
	
	/**
	 * <p>ソースファイルから関数を抽出する</p>
	 */
	public BlockTree extractFunction() {
		BlockTree root = new BlockTree();
		currentNode = root;
		
		Token functionName = null;
		while(getType(2) != EOF){
			if(getType(2)==LeftParen){
				if(getType(1)==Identifier){
					functionName = getToken(1);
				} else {
					functionName = null;
				}
				consume();
				consume();
			}
			if(getType(2)==LeftBrace && getType(1)==RightParen && functionName!=null) {
				consume();
				BlockTree t = new BlockTree(functionName);
				root.addChild(t);
				BlockTree _save = currentNode;
				currentNode = t;
				
				currentNode.setSart(getIndex());
				if(compoundStatement()){
					//System.out.println(methodName);
					
					//int end = getIndex()-1;
					/*
					for (CPP14MyListener listener : _parseListeners) {
						listener.visitFunction(_tokens, functionName, start, end);
					}
					*/
				}
				else{
					System.err.println(_tokens.getSourceName() + " : " + functionName.getText()+ " : " + functionName.getLine());
				}
				currentNode.setEnd(getIndex()-1);
				currentNode = _save;
				functionName = null;
			}
			else {
				consume();
			}
		}
		
		return root;
	}
	
	private boolean compoundStatement(){
		if(!match(LeftBrace)) return false;
		
		while(getType(1)!=RightBrace){
			if(!blockitem()) {
				//seekIndex(mark);
				//searchRBrace();
				return false;
			}
		}
		
		return match(RightBrace);
	}
	
	private boolean blockitem(){
		if(declaration()) return true;
		if(statement()) return true;
		return false;
	}
	
	private boolean statement(){
		
		String statName = getToken(1).getText();
		if		(labeledStatement());
		else if	(compoundStatement());
		else if	(selectionStatement());
		else if	(iterationStatement());
		else if	(jumpStatement());
		else if	(unknownBlock());
		else if	(expressionStatement());
		else {
		    System.err.println("statement error : " + getToken(1).getLine());
		    
		    return false;
		}
		return true;
	}
	
	private boolean labeledStatement(){
		if(getType(2)==Colon){
			if(getType(1)==Identifier || getType(1)==Default){
				match(Identifier);
				match(Default);
				return statement();
			}
		} 
		if (match(Case)) {
			while(getType(1)!=EOF){
				if(match(Colon)) return statement();
				matchWild();
			}
			return false;
		} 
	
		return false;
	}
	
	private boolean selectionStatement(){
		Token blockName = getToken(1);
		if(match(If)) {
			if(!parentheses()) return false;
			enterRule(blockName);
			if(!statement()) {
				System.err.println("if error : " + getToken(1).getLine());
			    return false;
			}
			exitRule();
			if(match(Else)) {
				if(!statement()){
					System.err.println("else error : " + getToken(1).getLine());
				    return false;
				}
				return true;
			}
			return true;
		}
		if(match(Switch)){
			if(!parentheses()) return false;
			if(!statement()){
				System.err.println("switch error : " + getToken(1).getLine());
				return false;
			}
			return true;
		}
		
		return false;
	}
	
	private boolean iterationStatement(){
		if(match(While)){
			if(!parentheses()) return false;
			if(!statement()){
				System.err.println("while error : " + getToken(1).getLine());
				return false;
			}
			return true;
		}
		if(match(Do)){
			if(!statement()) {
				System.err.println("do-while error : " + getToken(1).getLine());
				return false;
			}
			if(!match(While)) return false;
			if(!parentheses()) return false;
			return match(Semi);
		}
		if(match(For)){
			if(!parentheses()) return false;
			if(!statement()){
				System.err.println("for error : " + getToken(1).getLine());
				return false;
			}
			return true;
		}
		return false;
	}
	
	private boolean jumpStatement(){
		if(match(Goto) || match(Return)){
			return searchSemicolon();
		}
		if(match(Continue) || match(Break)){
			return match(Semi);
		}
		
		return false;
	}
	
	private boolean unknownBlock(){
		if(!match(Identifier)) return false; 
		if(getType(1)==LeftParen){
			if(!parentheses()) return false;
		}
		if(!Braces()) return false;
		return true;
	}
	
	private boolean expressionStatement(){
		return searchSemicolon();
	}
	
	private boolean declaration(){
		match(CPP14Lexer.Typedef);
		if(match(CPP14Lexer.Struct) || match(CPP14Lexer.Union) || match(CPP14Lexer.Enum)){
			Braces();
			return true;
		}
		return false;
	}
	
	private boolean parentheses() {
		if(!match(LeftParen)) return false;
		while(getType(1)!=EOF){
			if(getType(1)==LeftParen) {
				parentheses();
				continue;
			}
			if(match(RightParen)) return true;
			consume();
		}
		return false;
	}
	
	private boolean Braces(){
		if(!match(LeftBrace)) return false;
		while(getType(1)!=EOF){
			if(getType(1)==LeftBrace) {
				Braces();
				continue;
			}
			if(match(RightBrace)) return true;
			consume();
		}
		return false;
	}
	
	private boolean searchSemicolon(){
		int mark = getIndex();
		while(getType(1)!=EOF){
			if(match(Semi)) return true;
			if(getType(2)==LeftBrace && getType(1)!=Assign) {
				seekIndex(mark);
				return false;
			}
			if(getType(2)==LeftBrace && getType(1)==Assign) {
				consume();
				Braces();
				continue;
			}
			consume();
		}
		seekIndex(mark);
		return false;
	}
	
	//以下はパーサー関連のメソッド
	
	private void consume(){
		_tokens.consume();
	}
	
	private boolean match(int tType){
		if( _tokens.LA(1) == tType) {
			currentNode.addTokenNode(_tokens.LT(1));
			_tokens.consume();
			return true;
		}
		return false;
	}
	
	private void matchWild(){
		currentNode.addTokenNode(_tokens.LT(1));
		_tokens.consume();
	}
	
	private int getType(int i){
		return _tokens.LA(i);
	}
	
	private Token getToken(int i){
		return _tokens.LT(i);
	}
	
	private int getIndex(){
		return _tokens.index();
	}
	
	private void seekIndex(int i){
		_tokens.seek(i);
	}
	
	//以下はリスナー関連のメソッド
	
	public void addParseListener(CPP14MyListener listener) {
		if (listener == null) {
			throw new NullPointerException("listener");
		}

		if (_parseListeners == null) {
			_parseListeners = new ArrayList<CPP14MyListener>();
		}

		this._parseListeners.add(listener);
	}

	public void removeParseListener(CPP14MyListener listener) {
		if (_parseListeners != null) {
			if (_parseListeners.remove(listener)) {
				if (_parseListeners.isEmpty()) {
					_parseListeners = null;
				}
			}
		}
	}

	public void removeParseListeners() {
		_parseListeners = null;
	}
	
	private void enterRule(Token token){
			BlockTree t = new BlockTree(token);
			currentNode.addChild(t);
			currentNode = t;
			_save.push(t);
			currentNode.setSart(getIndex());
		
	}
	
	private void exitRule(){
			currentNode.setEnd(getIndex()-1);
			currentNode = _save.pop();
			
	}

}
