package jp.ac.osaka_u.ist.sel.ccvolti.analyze;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.xpath.XPath;

import jp.ac.osaka_u.ist.sel.ccvolti.CloneDetector;
import jp.ac.osaka_u.ist.sel.ccvolti.Config;
import jp.ac.osaka_u.ist.sel.ccvolti.grammar.Java8.Java8Lexer;
import jp.ac.osaka_u.ist.sel.ccvolti.grammar.Java8.Java8Parser;
import jp.ac.osaka_u.ist.sel.ccvolti.grammar.Java8.JavaMyListener;
import jp.ac.osaka_u.ist.sel.ccvolti.grammar.Java8.Java8Parser.CompilationUnitContext;
import jp.ac.osaka_u.ist.sel.ccvolti.model.Block;
import jp.ac.osaka_u.ist.sel.ccvolti.model.Word;

public class Java8analyzer {

	// private ASTParser parser = ASTParser.newParser(AST.JLS4);
	private ArrayList<String> allWordList;
	private static Block currentBlock;
	private static int blockID;
	private static ArrayList<Block> blockList;
	public int countFiles;
	public int countParseFiles;

	/**
	 * <p>
	 * コンストラクタ
	 * </p>
	 */
	public Java8analyzer() {
		// parser.setBindingsRecovery(true);
		// parser.setStatementsRecovery(true);
		// parser.setResolveBindings(true);
		allWordList = new ArrayList<String>();
		blockID = 0;
		blockList = new ArrayList<Block>();
		countFiles=0;
		countParseFiles=0;
		
	}

	/**
	 * <p>
	 * 単語リストの取得
	 * </p>
	 * 
	 * @return
	 */
	public final ArrayList<String> getWordList() {
		return allWordList;
	}
	
	public final ArrayList<Block> getBlockList(){
		return blockList;
	}

	public static final ArrayList<String> searchFiles(String pathname) {
		ArrayList<String> fileList = new ArrayList<String>();
		File file = new File(pathname);
		if(file.isFile() && file.getName().endsWith(".java")) {
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
	 * ディレクトリ探索
	 * </p>
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void analyze(List<String> fileList) throws IOException {
		for (String file : fileList) {
			System.out.println(file);
			countFiles++;
			CharStream stream = CharStreams.fromFileName(file, Charset.forName(Config.charset));
			Java8Lexer lexer = new Java8Lexer(stream);
			lexer.removeErrorListeners();
			lexer.addErrorListener(SilentErrorListener.INSTANCE);

			CommonTokenStream tokens = new CommonTokenStream(lexer);
			Java8Parser parser = new Java8Parser(tokens);

			// parser.addParseListener(new JavaMyListener());
			CompilationUnitContext tree = null;
			parser.removeErrorListeners();
			parser.addErrorListener(SilentErrorListener.INSTANCE);

			parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
			try {
				tree = parser.compilationUnit(); // STAGE 1
			} catch (Exception ex) {
				//System.out.println("try predictionMode LL");
				lexer = new Java8Lexer(stream);
				lexer.removeErrorListeners();
				lexer.addErrorListener(DescriptiveErrorListener.INSTANCE);
				tokens = new CommonTokenStream(lexer); // rewind input stream
				parser = new Java8Parser(tokens);
				parser.getInterpreter().setPredictionMode(PredictionMode.LL);
				// parser.removeErrorListeners();
				// parser.addErrorListener(ParserErrorListener.INSTANCE);
				try {
					tree = parser.compilationUnit(); // STAGE 2
//					System.out.println("success");
				} catch (ParseCancellationException e) {
					System.err.println(file + " parse cancel");
					continue;
				} catch (Exception e) {
					System.err.println(e);
					continue;
				}
				// if we parse ok, it's LL not SLL
			}
			extractMethod(tree, parser);
			countParseFiles++;
			tokens.fill();
			CloneDetector.countLine += tokens.LT(tokens.size()).getLine();

		}
	}

	/**
	 * <p>
	 * ソースコードテキスト取得
	 * </p>
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private char[] getCode(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String code = "";
		String line;
		while ((line = reader.readLine()) != null)
			code = code + "\n" + line;
		reader.close();
		return code.toCharArray();
	}

	/**
	 * <p>
	 * ASTから各メソッドのASTを構築
	 * </p>
	 * 
	 * @param method
	 * @param node
	 * @param parent
	 * @param className
	 */
	private static void extractMethod(ParseTree tree, Java8Parser parser) {
		for (ParseTree t : XPath.findAll(tree, "//methodDeclaration", parser)) {
			Block block = new Block();
			currentBlock = block;
			Token start = null;
			CloneDetector.countMethod++;
			int c = t.getChildCount();
			for (ParseTree subt : ((Java8Parser.MethodDeclarationContext) t).children) {
				if (subt instanceof RuleContext) {
					if (subt instanceof Java8Parser.MethodBodyContext) {
						if (subt.getSourceInterval().length() <= Config.METHOD_NODE_TH)
							break;

						initBlock(block, start, subt.getChild(0));
						blockList.add(block);
						// if (CloneDetector.enableBlockExtract)
							//extractBlock(subt.getChild(0), parser);
					} else if (subt instanceof Java8Parser.MethodHeaderContext) {
						for (ParseTree subt2 : ((Java8Parser.MethodHeaderContext) subt).children) {
							if (subt2 instanceof Java8Parser.MethodDeclaratorContext) {
								start = ((Java8Parser.MethodDeclaratorContext) subt2).start;
							}
						}
					}
				}
			}
		}
	}

	private static void extractBlock(ParseTree tree, Java8Parser parser) {
		for (ParseTree t : XPath.findAll(tree, "/block/blockStatement/statement", parser)) {
			if (!(t.getChild(0) instanceof RuleContext)) {
				TerminalNode token = (TerminalNode) t.getChild(0);
				List<Integer> args = new ArrayList<Integer>();

				switch (token.getSymbol().getType()) {
				case Java8Lexer.IF:
					args.add(2);
					if (t.getChildCount() == 5)
						args.add(4);
					break;

				case Java8Lexer.FOR:
					args.add(4);
					break;

				case Java8Lexer.WHILE:
					args.add(2);
					break;

				case Java8Lexer.DO:
					args.add(1);
					break;

				case Java8Lexer.SWITCH:
					args.add(2);
					break;
				}

				if (args.size() == 0)
					continue;

				for (Integer arg : args) {
					if (t.getChild(arg).getSourceInterval().length() <= Config.BLOCK_NODE_TH)
						continue;
					if (t.getChild(arg).getChild(0) instanceof Java8Parser.BlockContext) {

						Block block = new Block();
						block.setParent(currentBlock);
//						currentBlock.addChild(block);
						Block save = currentBlock;
						currentBlock = block;

						CloneDetector.countBlock++;
						if (t.getChild(arg - 1).getText().equals("else")) {
							token = (TerminalNode) t.getChild(arg - 1);
						}
						initBlock(block, token.getSymbol(), t.getChild(arg).getChild(0));
						blockList.add(block);
						extractBlock(t.getChild(arg).getChild(0), parser);

						currentBlock = save;
					}
				}
			}
		}
	}

	private static void initBlock(Block block, Token token, ParseTree tree) {
		block.setId(blockID++);
		block.setFileName(token.getInputStream().getSourceName());
		block.setStartLine(token.getLine());

		ArrayList<Token> tokenList = new ArrayList<Token>();
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk(new JavaMyListener(tokenList), tree);

		block.setEndLine(tokenList.get(tokenList.size() - 1).getLine());

		if (block.getParent() != null) {
			block.setName(block.getParent().getName() + " - " + token.getText());
			block.setMethodStartLine(block.getParent().getMethodStartLine());
			block.setMethodEndLine(block.getParent().getMethodEndLine());
		} else {
			block.setName(token.getText());
			block.setMethodStartLine(block.getStartLine());
			block.setMethodEndLine(block.getEndLine());
		}

		for (Token t : tokenList) {
			block.incNodeNum();
			if (t.getType() == Java8Lexer.Identifier) {
				String[] words = Word.separateIdentifier(t.getText());
				block.addWord(words);
			} else if (t.getText().matches("\\w+")) {
				block.addWord(t.getText());
			}

		}
	}
}
