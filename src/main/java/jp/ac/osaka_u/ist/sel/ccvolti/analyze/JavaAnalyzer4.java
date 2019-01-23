package jp.ac.osaka_u.ist.sel.ccvolti.analyze;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import jp.ac.osaka_u.ist.sel.ccvolti.CloneDetector;
import jp.ac.osaka_u.ist.sel.ccvolti.Config;
import jp.ac.osaka_u.ist.sel.ccvolti.grammar.Java8.Java8Lexer;
import jp.ac.osaka_u.ist.sel.ccvolti.model.Block;
import jp.ac.osaka_u.ist.sel.ccvolti.model.Word;

public class JavaAnalyzer4 {

	private ArrayList<String> allWordList = new ArrayList<String>();
	int blockId = 0;
	private Block currentBlock;
	private int p;
	private static final String[] controlFlow = { "if", "else", "switch", "case", "default", "for", "while", "do",
			"continue", "break", "return" };
	private static HashSet<String> controlFlowSet;
	private List<Block> blockList = new ArrayList<>();

	public JavaAnalyzer4() {
		controlFlowSet = new HashSet<String>(Arrays.asList(controlFlow));
	}

	/**
	 * <p>
	 * ディレクトリ探索
	 * </p>
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void searchFile(File file) throws IOException {
		if (file.isFile() && (file.getName().endsWith(".java"))) {
			try {
				extractMethod(file);
			} catch (Exception e) {
				System.err.println(file.getPath() + "\\" + file.getName() + " : error" + e);
			}
		} else if (file.isDirectory()) {
			// System.out.println(file.toString());
			File[] fileList = file.listFiles();
			for (File f : fileList)
				searchFile(f);
		}
	}

	/**
	 * <p>
	 * 単語リストの取得
	 * </p>
	 * 
	 * @return
	 */
	public ArrayList<String> getWordList() {
		return allWordList;
	}

	/**
	 * <p>
	 * ソースファイルから関数を抽出する
	 * </p>
	 * 
	 * @param file
	 * @throws IOException
	 */
	private void extractMethod(File file) throws IOException {
		//String input = preProcessor(file);
		CharStream stream = CharStreams.fromFileName(file.toString(), Charset.forName(Config.charset));
		Java8Lexer lexer = new Java8Lexer(stream);
		lexer.removeErrorListeners();
		lexer.addErrorListener(DescriptiveErrorListener.INSTANCE);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		tokens.fill();

		Token token;
		Token beforeToken = null;
		String methodName = null;
		int start = 0;
		p = 0;
		while ((token = tokens.get(p)).getType() != Token.EOF) {
			switch (token.getType()) {
			case Java8Lexer.LPAREN:
				if (beforeToken != null && beforeToken.getType() == Java8Lexer.Identifier) {
					methodName = beforeToken.getText();
					start = token.getLine();
				} else {
					methodName = null;
				}
				break;
			case Java8Lexer.LBRACE:
				if (methodName != null && beforeToken.getType() == Java8Lexer.RPAREN && !methodName.equals("for")
						&& !methodName.equals("if") && !methodName.equals("switch") && !methodName.equals("while")) {
					Block block = new Block();
					currentBlock = block;
					// System.out.printf("%s - %s:
					// %d\r\n",file.toString(),methodName,start);
					block.setId(blockId++);
					block.setName(methodName);
					block.setFileName(file.toString());
					block.setStartLine(start);
					block.setMethodStartLine(start);
					blockList.add(block);
					CloneDetector.countMethod++;
					int endPtr = p + blockLength(tokens, p);
					block.setEndLine(tokens.get(endPtr).getLine());
					block.setMethodEndLine(block.getEndLine());
					p++;
					extractBlock(tokens, block);

					methodName = null;
				}
				break;
			// case CPP14Lexer.ErrorCharacter:
			// if(true){
			// System.out.println(token.getChannel());
			// System.out.println(tokens.getText());
			// System.err.println(token.getInputStream().getSourceName()+"line
			// "+token.getLine()+":"+token.getCharPositionInLine()+" lexee error
			// "+token.getText());
			// }

			}
			beforeToken = token;
			p++;
		}
		CloneDetector.countLine += token.getLine();
	}

	// プリプロセッサ
	// マクロの除去
	private String preProcessor(File file) {
		StringBuilder buf = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				buf.append(line);
				buf.append("\n");
			}
			reader.close();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return buf.toString();
	}

	private int blockLength(CommonTokenStream tokens, int ptr) {
		Token token;
		int length = 1;
		int depth = 0;
		while ((token = tokens.get(++ptr)).getType() != Token.EOF) {
			switch (token.getType()) {
			case Java8Lexer.LBRACE:
				depth++;
				break;
			case Java8Lexer.RBRACE:
				if (depth == 0)
					return length;
				depth--;
				break;
			}
			length++;
		}
		return length;
	}

	private void ignoreComment(StreamTokenizer tokenizer) throws IOException {
		int beforeToken = 0;
		int token = 0;
		while ((token = tokenizer.nextToken()) != StreamTokenizer.TT_EOF) {
			switch (token) {
			case '/':
				if (beforeToken == '*' || beforeToken == '/')
					return;
			case StreamTokenizer.TT_WORD:
				break;
			}
			beforeToken = token;
		}
	}

	/**
	 * <p>
	 * 関数からワードを抽出
	 * </p>
	 * 
	 * @param tokenizer
	 * @param method
	 * @throws IOException
	 */
	private void extractBlock(CommonTokenStream tokens, Block block) throws IOException {
		Token token;
		Token beforeToken = null;
		String blockName = null;

		// int beforeToken = 0;

		while ((token = tokens.get(p)).getType() != Token.EOF) {
			Block b = block;
			while (b != null) {
				b.incNodeNum();
				b = b.getParent();
			}

			switch (token.getType()) {
			case Java8Lexer.LPAREN:
				if (beforeToken != null) {
					if (beforeToken.getType() == Java8Lexer.IF || beforeToken.getType() == Java8Lexer.WHILE
							|| beforeToken.getType() == Java8Lexer.FOR || beforeToken.getType() == Java8Lexer.SWITCH) {
						blockName = beforeToken.getText();
					}
				} else {
					blockName = null;
				}
				break;

			case Java8Lexer.LBRACE:

				Block parent = null;

				if (CloneDetector.enableBlockExtract && beforeToken != null) {
					if ((blockName != null && beforeToken.getType() == Java8Lexer.RPAREN)
							|| beforeToken.getType() == Java8Lexer.DO) {
						if (beforeToken.getType() == Java8Lexer.DO)
							blockName = "do-while";

						Block child = new Block();
						parent = block;
						child.setParent(parent);
//						parent.addChild(child);

						child.setId(blockId++);
						child.setName(parent.getName() + " - " + blockName);
						child.setFileName(parent.getFileName());
						child.setStartLine(token.getLine());
						child.setMethodStartLine(parent.getMethodStartLine());
						int endPtr = p + blockLength(tokens, p);
						child.setEndLine(tokens.get(endPtr).getLine());
						child.setMethodEndLine(parent.getMethodEndLine());

						blockList.add(child);
						blockName = null;
						CloneDetector.countBlock++;

						block = child;
					}

				}

				p++;
				extractBlock(tokens, block);

				if (parent != null) {
					block.setEndLine(tokens.get(p).getLine());

					if (block.getParent() != null) {
						block.setMethodEndLine(block.getParent().getMethodEndLine());
					} else {
						block.setMethodStartLine(block.getStartLine());
						block.setMethodEndLine(block.getEndLine());
					}

					block = parent;
				}

				break;
			case Java8Lexer.RBRACE:
				return;
			case Java8Lexer.Identifier:
				b = block;
				while (b != null) {
					String[] words = Word.separateIdentifier(token.getText());
					b.addWord(words);
					b = b.getParent();
				}
				break;
			default:
				if (token.getText().matches("[a-zA-Z]+")) {
					b = block;
					while (b != null) {
						b.addWord(token.getText());
						b = b.getParent();
					}

				}
				break;
			}
			beforeToken = token;
			p++;
		}
	}
}
