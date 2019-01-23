package jp.ac.osaka_u.ist.sel.ccvolti.model;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;

public class BlockFactory {
	public static Block create(int id, String name, Parser parser, ParseTree tree, int idType) {
		Block block = new Block();
		block.setId(id);
		block.setName(name);
		block.setFileName(parser.getSourceName());

		Interval interval = tree.getSourceInterval();
		final int start = interval.a;
		final int end = interval.b;
		TokenStream tokens = parser.getTokenStream();

		block.setStartLine(tokens.get(start).getLine());
		block.setEndLine(tokens.get(end).getLine());
		block.setNodeNum(interval.length());

		if (block.getParent() != null) {
			block.setMethodStartLine(block.getParent().getMethodStartLine());
			block.setMethodEndLine(block.getParent().getMethodEndLine());
		} else {
			block.setMethodStartLine(block.getStartLine());
			block.setMethodEndLine(block.getEndLine());
		}

		for (int i = start; i <= end; i++) {
			if (tokens.get(i).getType() == idType) {
				String[] words = Word.separateIdentifier(tokens.get(i).getText());
				block.addWord(words);
			} else if (tokens.get(i).getText().matches("\\w+")) {
				block.addWord(tokens.get(i).getText());
			}
		}
		return block;
	}

}
