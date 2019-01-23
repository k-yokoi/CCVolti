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
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.xpath.XPath;

import jp.ac.osaka_u.ist.sel.ccvolti.CloneDetector;
import jp.ac.osaka_u.ist.sel.ccvolti.Config;
import jp.ac.osaka_u.ist.sel.ccvolti.grammar.Java.JavaLexer;
import jp.ac.osaka_u.ist.sel.ccvolti.grammar.Java.JavaParser;
import jp.ac.osaka_u.ist.sel.ccvolti.grammar.Java.JavaParser.CompilationUnitContext;
import jp.ac.osaka_u.ist.sel.ccvolti.model.Block;
import jp.ac.osaka_u.ist.sel.ccvolti.model.BlockFactory;

public class JavaAnalyzer3 {

	// private ASTParser parser = ASTParser.newParser(AST.JLS4);
	private ArrayList<String> allWordList;
	private static int blockID;
	public int countFiles;
	public int countParseFiles;

	/**
	 * <p>
	 * コンストラクタ
	 * </p>
	 */
	public JavaAnalyzer3() {
		// parser.setBindingsRecovery(true);
		// parser.setStatementsRecovery(true);
		// parser.setResolveBindings(true);
		allWordList = new ArrayList<String>();
		blockID = 0;
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
	public List<Block> analyze(List<String> fileList) throws IOException {
		List<Block> blockList = new ArrayList<>();

		for (String file : fileList) {
			countFiles++;
			CharStream stream = CharStreams.fromFileName(file, Charset.forName(Config.charset));
			JavaLexer lexer = new JavaLexer(stream);
			lexer.removeErrorListeners();
			// lexer.addErrorListener(SilentErrorListener.INSTANCE);

			CommonTokenStream tokens = new CommonTokenStream(lexer);
			JavaParser parser = new JavaParser(tokens);

			// parser.addParseListener(new JavaMyListener());
			CompilationUnitContext tree = null;
			parser.removeErrorListeners();
			// parser.addErrorListener(SilentErrorListener.INSTANCE);

			parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
			try {
				tree = parser.compilationUnit(); // STAGE 1
			} catch (Exception ex) {
				System.out.println("try predictionMode LL");
				lexer = new JavaLexer(stream);
				lexer.removeErrorListeners();
				// lexer.addErrorListener(DescriptiveErrorListener.INSTANCE);
				tokens = new CommonTokenStream(lexer); // rewind input stream
				parser = new JavaParser(tokens);
				parser.getInterpreter().setPredictionMode(PredictionMode.LL);
				parser.removeErrorListeners();
				// parser.addErrorListener(ConsoleErrorListener.INSTANCE);
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
			blockList.addAll(extractMethod(tree, parser));
			countParseFiles++;
			tokens.fill();
			CloneDetector.countLine += tokens.LT(tokens.size()).getLine();

		}
		return blockList;
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
	private static List<Block> extractMethod(ParseTree tree, JavaParser parser) {
		List<Block> blockList = new ArrayList<>();
		for (ParseTree t : XPath.findAll(tree, "//methodDeclaration", parser)) {
			Token start = null;
			CloneDetector.countMethod++;
			for (ParseTree subt : ((JavaParser.MethodDeclarationContext) t).children) {
				if (subt instanceof RuleContext) {
					if (subt instanceof JavaParser.MethodBodyContext) {
						if (subt.getSourceInterval().length() <= Config.METHOD_NODE_TH)
							break;

						Block block = BlockFactory.create(blockID++, start.getText(), parser, subt.getChild(0),
								JavaLexer.IDENTIFIER);
						blockList.add(block);
						if (CloneDetector.enableBlockExtract)
							blockList.addAll(extractBlock(subt.getChild(0), parser, block));
					}
				} else {
					TerminalNode token = (TerminalNode) subt;
					if (token.getSymbol().getType() == JavaLexer.IDENTIFIER) {
						// System.out.println(token.getText());
						start = token.getSymbol();
					}
				}
			}
		}
		return blockList;
	}

	private static List<Block> extractBlock(ParseTree tree, JavaParser parser, Block parent) {
		List<Block> blockList = new ArrayList<>();
		for (ParseTree t : XPath.findAll(tree, "/block/blockStatement/statement", parser)) {
			if (!(t.getChild(0) instanceof RuleContext)) {
				TerminalNode token = (TerminalNode) t.getChild(0);
				List<Integer> args = new ArrayList<Integer>();

				switch (token.getSymbol().getType()) {
				case JavaLexer.IF:
					args.add(2);
					if (t.getChildCount() == 5)
						args.add(4);
					break;

				case JavaLexer.FOR:
					args.add(4);
					break;

				case JavaLexer.WHILE:
					args.add(2);
					break;

				case JavaLexer.DO:
					args.add(1);
					break;

				case JavaLexer.SWITCH:
					args.add(2);
					break;
				}

				if (args.size() == 0)
					continue;

				for (Integer arg : args) {
					CloneDetector.countBlock++;
					if (t.getChild(arg).getSourceInterval().length() <= Config.BLOCK_NODE_TH)
						continue;
					if (t.getChild(arg).getChild(0) instanceof JavaParser.BlockContext) {
						if (t.getChild(arg - 1).getText().equals("else")) {
							token = (TerminalNode) t.getChild(arg - 1);
						}
						Block block = BlockFactory.create(blockID++,
								parent.getName() + " - " + token.getSymbol().getText(), parser,
								t.getChild(arg).getChild(0), JavaLexer.IDENTIFIER);
						block.setParent(parent);
						blockList.add(block);
						blockList.addAll(extractBlock(t.getChild(arg).getChild(0), parser, block));
					}
				}
			}
		}
		return blockList;
	}
}
