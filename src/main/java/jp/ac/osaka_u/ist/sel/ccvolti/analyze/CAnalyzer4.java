package jp.ac.osaka_u.ist.sel.ccvolti.analyze;

import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import jp.ac.osaka_u.ist.sel.ccvolti.CloneDetector;
import jp.ac.osaka_u.ist.sel.ccvolti.Config;
import jp.ac.osaka_u.ist.sel.ccvolti.grammar.CPP14.CPP14Lexer;
import jp.ac.osaka_u.ist.sel.ccvolti.model.Block;
import jp.ac.osaka_u.ist.sel.ccvolti.model.Word;

public class CAnalyzer4 {

	private ArrayList<String> allWordList = new ArrayList<String>();
	int blockId = 0;
	private Block currentBlock;
	private int p;
	private static final String[] controlFlow = { "if", "else", "switch", "case", "default", "for", "while", "do",
			"continue", "break", "return" };
	private static HashSet<String> controlFlowSet;

	public CAnalyzer4() {
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
	public ArrayList<String> searchFiles(String pathname) {
		ArrayList<String> fileList = new ArrayList<String>();
		File file = new File(pathname);
		if (file.isFile() && (file.getName().endsWith(".c") || file.getName().endsWith(".cpp"))) {
			fileList.add(file.getAbsolutePath());
		} else if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				fileList.addAll(searchFiles(f.getAbsolutePath()));
			}
		}
		return fileList;
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

	public List<Block> analyze(List<String> fileList) {
		List<Block> blockList = new ArrayList<>();
		for (String file : fileList) {
			try {
				blockList.addAll(extractMethod(new File(file)));
			} catch (Exception e) {
				System.err.println(file + " : " + e);
			}
		}
		return blockList;
	}

	/**
	 * <p>
	 * ソースファイルから関数を抽出する
	 * </p>
	 * 
	 * @param file
	 * @throws Exception
	 * @throws IOException
	 */
	private List<Block> extractMethod(File file) throws Exception {
		List<Block> blockList = new ArrayList<>();
		String input = preProcessor(file);
		CharStream stream = CharStreams.fromString(input, file.toString());
		CPP14Lexer lexer = new CPP14Lexer(stream);
		lexer.removeErrorListeners();
		// lexer.addErrorListener(DescriptiveErrorListener.INSTANCE);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		tokens.fill();

		Token token = null;
		Token beforeToken = null;
		String methodName = null;
		int start = 0;
		p = 0;
		while (p < tokens.size()) {
			token = tokens.get(p);
			switch (token.getType()) {
			case CPP14Lexer.LeftParen:
				if (beforeToken != null && beforeToken.getType() == CPP14Lexer.Identifier) {
					methodName = beforeToken.getText();
					start = token.getLine();
				} else {
					methodName = null;
				}
				break;
			case CPP14Lexer.LeftBrace:
				if (methodName != null && beforeToken.getType() == CPP14Lexer.RightParen && !methodName.equals("for")
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
					blockList.addAll(extractBlock(tokens, block));

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
		return blockList;
	}

	// プリプロセッサ
	// マクロの除去
	private String preProcessor(File file) throws Exception {
		List<String> lines = new ArrayList<>();
		try (Stream<String> stream = Files.lines(file.toPath(), Charset.forName(Config.charset))) {
			stream.forEach(line -> {
				line = line.replaceAll("\0", ""); // 制御文字 NULL文字 の削除
				line = line.replaceAll("\f", ""); // 制御文字 書式送り の削除
				line = line.replaceAll("\\$", "_"); // ドル文字 をアンダーバーに置換
				lines.add(line);
			});
		} catch (Exception e) {
			throw e;
		}

		StringBuilder buf = new StringBuilder();
		for (Iterator<String> lineIter = lines.iterator(); lineIter.hasNext();) {
			String line = lineIter.next();
			int skipCount = 0;
			while (line.endsWith("\\")) {
				line = line.substring(0, line.length() - 1);
				line = line + lineIter.next();
				skipCount++;
			}
			if (line.startsWith("#else") || line.startsWith("#elif")) {
				line = lineIter.next();
				while (!line.startsWith("#endif")) {
					skipCount++;
					line = lineIter.next();
				}
				skipCount++;
				line = "";
			}

			if (line.startsWith("#")) {
				line = "";
			}

			buf.append(line);
			buf.append('\n');
			for (int i = 0; i < skipCount; i++)
				buf.append('\n');
		}

		return buf.toString();
	}

	private int blockLength(CommonTokenStream tokens, int ptr) {
		Token token;
		int length = 1;
		int depth = 0;
		while (++ptr < tokens.size()) {
			token = tokens.get(ptr);
			switch (token.getType()) {
			case CPP14Lexer.LeftBrace:
				depth++;
				break;
			case CPP14Lexer.RightBrace:
				if (depth == 0)
					return length;
				depth--;
				break;
			}
			length++;
		}
		return --length;
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
	private List<Block> extractBlock(CommonTokenStream tokens, Block block) {
		List<Block> blockList = new ArrayList<>();
		Token token;
		Token beforeToken = null;
		String blockName = null;

		// int beforeToken = 0;

		while (p < tokens.size()) {
			token = tokens.get(p);
			Block b = block;
			while (b != null) {
				b.incNodeNum();
				b = b.getParent();
			}

			switch (token.getType()) {
			case CPP14Lexer.LeftParen:
				if (beforeToken != null) {
					if (beforeToken.getType() == CPP14Lexer.If || beforeToken.getType() == CPP14Lexer.While
							|| beforeToken.getType() == CPP14Lexer.For || beforeToken.getType() == CPP14Lexer.Switch) {
						blockName = beforeToken.getText();
					}
				} else {
					blockName = null;
				}
				break;

			case CPP14Lexer.LeftBrace:

				Block parent = null;

				if (CloneDetector.enableBlockExtract && beforeToken != null) {
					if ((blockName != null && beforeToken.getType() == CPP14Lexer.RightParen)
							|| beforeToken.getType() == CPP14Lexer.Do) {
						if (beforeToken.getType() == CPP14Lexer.Do)
							blockName = "do-while";

						Block child = new Block();
						parent = block;
						child.setParent(parent);
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
				blockList.addAll(extractBlock(tokens, block));

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
			case CPP14Lexer.RightBrace:
				return blockList;
			case CPP14Lexer.Identifier:
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
		return blockList;
	}
}
