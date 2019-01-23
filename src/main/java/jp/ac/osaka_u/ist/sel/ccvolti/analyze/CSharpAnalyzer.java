package jp.ac.osaka_u.ist.sel.ccvolti.analyze;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ListTokenSource;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.xpath.XPath;

import jp.ac.osaka_u.ist.sel.ccvolti.CloneDetector;
import jp.ac.osaka_u.ist.sel.ccvolti.Config;
import jp.ac.osaka_u.ist.sel.ccvolti.grammar.CSharp.CSharpLexer;
import jp.ac.osaka_u.ist.sel.ccvolti.grammar.CSharp.CSharpParser;
import jp.ac.osaka_u.ist.sel.ccvolti.grammar.CSharp.CSharpParser.Compilation_unitContext;
import jp.ac.osaka_u.ist.sel.ccvolti.model.Block;
import jp.ac.osaka_u.ist.sel.ccvolti.model.BlockFactory;

public class CSharpAnalyzer {

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
	public CSharpAnalyzer() {
		// parser.setBindingsRecovery(true);
		// parser.setStatementsRecovery(true);
		// parser.setResolveBindings(true);
		allWordList = new ArrayList<String>();
		blockID = 0;
		countFiles = 0;
		countParseFiles = 0;

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
		if (file.isFile() && file.getName().endsWith(".cs")) {
			fileList.add(file.getAbsolutePath());
		} else if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				fileList.addAll(searchFiles(f.getAbsolutePath()));
			}
		}
		return fileList;
	}

	public static InputStream skipUTF8BOM(InputStream is, String charSet) throws IOException {
		if (!charSet.toUpperCase().equals("UTF-8"))
			return is;
		if (!is.markSupported()) {
			// マーク機能が無い場合BufferedInputStreamを被せる
			is = new BufferedInputStream(is);
		}
		is.mark(3); // 先頭にマークを付ける
		if (is.available() >= 3) {
			byte b[] = { 0, 0, 0 };
			is.read(b, 0, 3);
			if (b[0] != (byte) 0xEF || b[1] != (byte) 0xBB || b[2] != (byte) 0xBF) {
				is.reset();// BOMでない場合は先頭まで巻き戻す
			}
		}
		return is;
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

			List<Token> codeTokens = new ArrayList<Token>();
			// List<Token> commentTokens = new ArrayList<Token>();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					skipUTF8BOM(new FileInputStream(file), Config.charset), Charset.forName(Config.charset)));
			CharStream stream = CharStreams.fromReader(reader, file);
			CSharpLexer preprocessorLexer = new CSharpLexer(stream);
			preprocessorLexer.removeErrorListeners();
			preprocessorLexer.addErrorListener(DescriptiveErrorListener.INSTANCE);

			// Collect all tokens with lexer (CSharpLexer.g4).
			List<? extends Token> tokens;
			try {
				tokens = preprocessorLexer.getAllTokens();
			} catch (Exception e) {
				continue;
			}

			// List<Token> directiveTokens = new ArrayList<Token>();
			// ListTokenSource directiveTokenSource = new ListTokenSource(directiveTokens);
			// CommonTokenStream directiveTokenStream = new
			// CommonTokenStream(directiveTokenSource, CSharpLexer.DIRECTIVE);
			// CSharpPreprocessorParser preprocessorParser = new
			// CSharpPreprocessorParser(directiveTokenStream);

			int index = 0;
			boolean compiliedTokens = true;
			while (index < tokens.size()) {
				Token token = tokens.get(index);
				if (token.getType() == CSharpLexer.SHARP) {
					// directiveTokens.clear();
					int directiveTokenIndex = index + 1;
					// Collect all preprocessor directive tokens.
					while (directiveTokenIndex < tokens.size()
							&& tokens.get(directiveTokenIndex).getType() != CSharpLexer.EOF
							&& tokens.get(directiveTokenIndex).getType() != CSharpLexer.DIRECTIVE_NEW_LINE
							&& tokens.get(directiveTokenIndex).getType() != CSharpLexer.SHARP) {
						/*
						 * if (tokens.get(directiveTokenIndex).getChannel() ==
						 * CSharpLexer.COMMENTS_CHANNEL) {
						 * commentTokens.add(tokens.get(directiveTokenIndex)); } else if
						 * (tokens.get(directiveTokenIndex).getChannel() != CSharpLexer.HIDDEN) {
						 * directiveTokens.add(tokens.get(directiveTokenIndex)); }
						 */
						directiveTokenIndex++;
					}

					/*
					 * directiveTokenSource = new ListTokenSource(directiveTokens);
					 * directiveTokenStream = new CommonTokenStream(directiveTokenSource,
					 * CSharpLexer.DIRECTIVE);
					 * preprocessorParser.setInputStream(directiveTokenStream);
					 * preprocessorParser.reset(); Parse condition in preprocessor directive (based
					 * on CSharpPreprocessorParser.g4 grammar).
					 * CSharpPreprocessorParser.Preprocessor_directiveContext directive =
					 * preprocessorParser .preprocessor_directive(); if true than next code is valid
					 * and not ignored. compiliedTokens = directive.value;
					 */
					index = directiveTokenIndex - 1;
				} else if (token.getChannel() != CSharpLexer.HIDDEN && token.getType() != CSharpLexer.DIRECTIVE_NEW_LINE
						&& compiliedTokens) {
					codeTokens.add(token); // Collect code tokens.
				}
				index++;
			}

			// At second stage tokens parsed in usual way.
			ListTokenSource codeTokenSource = new ListTokenSource(codeTokens);
			CommonTokenStream codeTokenStream = new CommonTokenStream(codeTokenSource);
			CSharpParser parser = new CSharpParser(codeTokenStream);
			// Parse syntax tree (CSharpParser.g4)
			Compilation_unitContext tree = null;

			parser.removeErrorListeners();
			// parser.addErrorListener(SilentErrorListener.INSTANCE);

			parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
			try {
				tree = parser.compilation_unit(); // STAGE 1
			} catch (Exception ex) {
				// System.out.println("try predictionMode LL");
				codeTokenSource = new ListTokenSource(codeTokens);
				codeTokenStream = new CommonTokenStream(codeTokenSource);
				parser = new CSharpParser(codeTokenStream);

				parser.getInterpreter().setPredictionMode(PredictionMode.LL);
				parser.removeErrorListeners();
				parser.addErrorListener(DescriptiveErrorListener.INSTANCE);
				// parser.addErrorListener(ParserErrorListener.INSTANCE);
				try {
					tree = parser.compilation_unit(); // STAGE 2
					// System.out.println("success");
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
			CloneDetector.countLine += tokens.get(tokens.size() - 1).getLine();

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
	private static List<Block> extractMethod(ParseTree tree, CSharpParser parser) {
		List<Block> blockList = new ArrayList<>();
		for (ParseTree t : XPath.findAll(tree, "//method_declaration", parser)) {
			CloneDetector.countMethod++;
			int c = t.getChildCount();
			for (ParseTree subt : ((CSharpParser.Method_declarationContext) t).children) {
				if (subt instanceof RuleContext) {
					if (subt instanceof CSharpParser.Method_bodyContext) {
						if (subt.getSourceInterval().length() <= Config.METHOD_NODE_TH)
							break;

						Block block = BlockFactory.create(blockID++, "", parser, subt.getChild(0),
								CSharpLexer.IDENTIFIER);
						blockList.add(block);
						if (CloneDetector.enableBlockExtract)
							blockList.addAll(extractBlock(subt.getChild(0), parser, block));
					}
				}
			}
		}
		return blockList;
	}

	private static List<Block> extractBlock(ParseTree tree, CSharpParser parser, Block parent) {
		List<Block> blockList = new ArrayList<>();
		for (ParseTree t : XPath.findAll(tree, "//block", parser)) {
			Block block = BlockFactory.create(blockID++, "", parser, t, CSharpLexer.IDENTIFIER);
			block.setParent(parent);
			CloneDetector.countBlock++;
			blockList.add(block);
		}
		return blockList;
	}

}