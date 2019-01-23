package jp.ac.osaka_u.ist.sel.ccvolti.grammar.CPP14;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Token;

public class BlockTree {
	public Token token;
	public int start;
	public int end;
	public List<BlockTree> children;
	public List<Token> tokenNodes;
	
	public BlockTree(){
	}
	
	public BlockTree(Token token){
		this.token = token;
	}
	
	public void addChild(BlockTree t)  {
		if(children==null) children = new ArrayList<BlockTree>();
		children.add(t);
	}
	
	public void addTokenNode(Token token) {
		if(tokenNodes == null) tokenNodes = new ArrayList<Token>();
		tokenNodes.add(token);
	}
	
	public void walk(){
		for (BlockTree child : children) {
			child.walk();
		}
	}
	
	public void setSart(int start){
		this.start = start;
	}
	
	public void setEnd(int end){
		this.end = end;
	}
}
