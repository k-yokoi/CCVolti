package jp.ac.osaka_u.ist.sel.ccvolti;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import jp.ac.osaka_u.ist.sel.ccvolti.analyze.CAnalyzer4;
import jp.ac.osaka_u.ist.sel.ccvolti.analyze.CSharpAnalyzer;
import jp.ac.osaka_u.ist.sel.ccvolti.analyze.JavaAnalyzer3;
import jp.ac.osaka_u.ist.sel.ccvolti.model.Block;
import jp.ac.osaka_u.ist.sel.ccvolti.model.ClonePair;
import jp.ac.osaka_u.ist.sel.ccvolti.model.CloneSet;

public class CloneDetector {
	public static final String PARAM_FILE = "dataset.txt.params";
	public static final String DATASET_FILE = "dataset";
	public static final String LSH_FILE = "lsh_result.txt";
	public static final String LSH_LOG = "lsh_log.txt";
	public static final String BLOCKLIST_CSV = "blocklist.csv";
	public static final boolean enableBlockExtract = true;
	public static final boolean removeMethodPair = false;
	public static final boolean lda = false;

	public static String javaClassPath;

	private static List<Block> blockList;
	public static List<ClonePair> clonePairList;
	private static HashMap<String, Integer> wordMap = new HashMap<String, Integer>();
	public static int countMethod, countBlock, countLine;
	private static final String version = "19.01.24";

	/**
	 * <p>
	 * メイン
	 * <p>
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("CCVolti " + version);
		// setJavaClassPath();
		getApplicationPath();
		commandOption(args);

		long start = System.currentTimeMillis();
		long subStart = start;
		long currentTime;

		System.out.println("Extract word in source code ...");
		countMethod = 0;
		countBlock = 0;
		countLine = 0;
		List<String> fileList = null;
		//fileListの取得をちゃんとする
		switch (Config.lang) {
		case 0: // "java"
			JavaAnalyzer3 javaanalyzer = new JavaAnalyzer3();
			fileList = JavaAnalyzer3.searchFiles(Config.target);
			blockList = javaanalyzer.analyze(fileList);
			System.out.println(
					"Parse file / All file = " + javaanalyzer.countParseFiles + " / " + javaanalyzer.countFiles);
			break;
		case 1: // "c"
			CAnalyzer4 canalyzer = new CAnalyzer4();
			fileList = canalyzer.searchFiles(Config.target);
			blockList = canalyzer.analyze(fileList);
			break;
		case 2: // "c#"
			CSharpAnalyzer csharpAnalyzer = new CSharpAnalyzer();
			fileList = CSharpAnalyzer.searchFiles(Config.target);
			blockList = csharpAnalyzer.analyze(fileList);
			System.out.println(
					"Parse file / All file = " + csharpAnalyzer.countParseFiles + " / " + csharpAnalyzer.countFiles);
			break;
		}
		System.out.println("The number of methods : " + countMethod);
		System.out.println("The number of blocks (Excluding methods) : " + countBlock);
		System.out.println("The line : " + countLine);
		System.out.println("Extract word in source code done : " + (System.currentTimeMillis() - start) + "[ms]");
		System.out.println();

		// 特徴ベクトル計算
		subStart = System.currentTimeMillis();

		VectorCalculator calculator = new VectorCalculator();
		blockList = calculator.filterMethod(blockList);
		System.out.println("The threshold of size for method : " + Config.METHOD_NODE_TH);
		System.out.println("The threshold of size for block : " + Config.BLOCK_NODE_TH);
		System.out.println("The threshold of line of block : " + Config.LINE_TH);
		System.out.println("Filtered blocks / All blocks : " + blockList.size() + " / " + (countMethod + countBlock));
		System.out.println();

		System.out.println("Calculate vector of each method ...");
		calculator.calculateVector(blockList);
		// System.out.println("wordmap.size = " + wordMap.size());
		currentTime = System.currentTimeMillis();
		System.out.println(
				"Calculate vector done : " + (currentTime - subStart) + "/" + (currentTime - start) + "[ms]\n");

		if (Config.LSH_PRG != LSHController.NO_LSH) {
			// LSHクラスタリング
			System.out.println("Cluster vector of each method ...");
			subStart = System.currentTimeMillis();

			// LSHController.computeParam(wordMap.size());
			System.out.println("LSH start");
			LSHController lshCtlr = new LSHController();
			lshCtlr.execute(blockList, calculator.getDimention(), Config.LSH_PRG);
			lshCtlr = null;
			System.out.println("LSH done : " + (System.currentTimeMillis() - subStart) + "[ms]");
			CloneJudgement cloneJudge = new CloneJudgement();
			clonePairList = cloneJudge.getClonePairList(blockList);
			cloneJudge = null;
		} else {
			CloneJudgement cloneJudge = new CloneJudgement();
			clonePairList = cloneJudge.getClonePairListNoLSH(blockList);
		}

		System.out.println("The number of clone pair : " + clonePairList.size());
		if (removeMethodPair)
			CloneJudgement.removePairOfMethod(clonePairList);

		currentTime = System.currentTimeMillis();
		System.out.println("Cluster done : " + (currentTime - subStart) + "/" + (currentTime - start) + "[ms]\n");

		List<CloneSet> cloneSetList = null;
		if (Config.resultNotifier != null || Config.resultCloneSet != null) {
			System.out.println("generate clone set start...");
			subStart = System.currentTimeMillis();
			cloneSetList = CloneJudgement.getCloneSetList(clonePairList, blockList);
			System.out.println("The number of clone set : " + cloneSetList.size());
			currentTime = System.currentTimeMillis();
			System.out.println(
					"generate clone set done : " + (currentTime - subStart) + "/" + (currentTime - start) + "[ms]\n");

		}

		// ファイル出力
		System.out.println("Output start ...");
		subStart = System.currentTimeMillis();
		// ArrayList<CloneSet> cloneSetList =
		// LSHController.getCloneSetList(clonePairList);
		// Outputter.outputCloneSetCSVforCPP(cloneSetList);
		// Outputter.outputCSVforCPP(clonePairList);
		if (Config.resultCSV != null)
			Outputter.outputCSV(clonePairList);
		if (Config.resultTXT != null)
			Outputter.outputTXT(clonePairList);
		if (Config.resultHTML != null)
			Outputter.outputHTML(clonePairList);
		if (Config.resultNotifier != null)
			Outputter.outputNotifier(cloneSetList, fileList);
		if (Config.resultCloneSet != null)
			Outputter.outputCloneSetTXTforCPP(cloneSetList);
		// Outputter.outputForBigCloneEval(clonePairList);
		// Outputter.outputTXTforCPP(clonePairList);
		// Outputter.outputCSVforJava(clonePairList);
		// Outputter.outputTXTforJava(clonePairList);
		//Outputter.outputBlockList(blockList);
		// Outputter.outputStatisticsSample(clonePairList, 0.05, 1.96, 0.9);
		currentTime = System.currentTimeMillis();
		System.out.println("Output done : " + (currentTime - subStart) + "/" + (currentTime - start) + "[ms]\n");
		// 評価
		// Comparator.compareMeCC(cloneSetList);
		// Evaluator2.evaluate();
		// Debug.debug();
		// Debug.outputResult02(cloneSetList);
		// Debug.outputResult(cloneSetList);

		System.out.print("Finished : ");
		currentTime = System.currentTimeMillis();
		System.out.println(currentTime - start + "[ms]");
	}

	private static void getApplicationPath() throws URISyntaxException {
		ProtectionDomain pd = CloneDetector.class.getProtectionDomain();
		CodeSource cs = pd.getCodeSource();
		URL location = cs.getLocation();
		URI uri = location.toURI();
		Path path = Paths.get(uri);
		javaClassPath = path.getParent().toString();
	}

	private static void setJavaClassPath() {
		Path path = null;
		try {
			path = Paths.get(CloneDetector.class.getClassLoader().getResource("").toURI());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		if (path.endsWith("bin"))
			path = path.getParent();
		javaClassPath = path.toString();
	}

	private static void commandOption(String[] args) {
		Options options = new Options();
		options.addOption(Option.builder("h").longOpt("help").desc("display help").build());
		options.addOption(Option.builder("d").longOpt("dir").desc("select directory for clone detection").hasArg()
				.argName("dirname").build());
		options.addOption(Option.builder("l").longOpt("lang")
				.desc("select language from following ( default: java )\r\n  * java\r\n  * c\\r\\n  * csharp").hasArg()
				.argName("lang")
				.build());
		// options.addOption(Option.builder("p").longOpt("param").desc("select
		// parameter file for LSH
		// execution").hasArg().argName("*.param").build());
		options.addOption(Option.builder("oc").longOpt("outputcsv")
				.desc("select csv file name for output").hasArg().argName("*.csv").build());
		options.addOption(Option.builder("ot").longOpt("outputtxt")
				.desc("select text file name for output").hasArg().argName("*.txt").build());
		options.addOption(Option.builder("oh").longOpt("outputhtml").desc("select html file name for output").hasArg()
				.argName("*.html").build());
		options.addOption(Option.builder("on").longOpt("outputnotifier").desc("select notifier file name for output")
				.hasArg().argName("filename").build());
		options.addOption(Option.builder("ocs").longOpt("outputcloneset")
				.desc("select text file name for output clone set").hasArg().argName("filename").build());

		options.addOption(Option.builder().longOpt("sim")
				.desc("set threshold of similarity for clone detection ( 0.0<=sim<=1.0 ) ( default: 0.9 )").hasArg()
				.argName("value").build());
		options.addOption(
				Option.builder().longOpt("size").desc("set threshold of size for method  ( 0<=size ) ( default: 50 )")
						.hasArg().argName("value").build());
		options.addOption(
				Option.builder().longOpt("sizeb")
						.desc("set threshold of size for block  ( 0<=size ) ( default: sizeb = size )")
						.hasArg().argName("value").build());
		options.addOption(Option.builder("mil").longOpt("min-lines")
				.desc("set threshold of line of block ( 0<=mil ) ( default: 0 )").hasArg().argName("value").build());
		options.addOption(Option.builder("cs").longOpt("charset")
				.desc("set the name of character encoding ( default: UTF-8 )").hasArg().argName("charset").build());
		options.addOption(Option.builder("t").longOpt("threads")
				.desc("set the number of threads. 0 indicates max threads. ( default: 1 )").hasArg().argName("value")
				.build());

		CommandLine cl = null;
		try {
			CommandLineParser parser = new DefaultParser();
			cl = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("Error: can't read options.");
			System.exit(1);
		}
		if (cl.hasOption("help") || args.length == 0) {
			HelpFormatter f = new HelpFormatter();
			f.printHelp("-d [dirname] -l [lang] <*options>", options);
			System.exit(0);
		}
		if (cl.hasOption("dir"))
			Config.target = cl.getOptionValue("dir");
		if (Config.target == null) {
			System.err.println("Usage Error: please select target directory for clone detection.");
			System.exit(1);
		}
		if (cl.hasOption("lang")) {
			if (cl.getOptionValue("lang").toLowerCase().equals("java"))
				Config.lang = 0;
			if (cl.getOptionValue("lang").toLowerCase().equals("c"))
				Config.lang = 1;
			if (cl.getOptionValue("lang").toLowerCase().equals("csharp"))
				Config.lang = 2;

		}
		/*
		 * if(cl.hasOption("param")) { Config.paramFile =
		 * cl.getOptionValue("param"); Config.paramFlg = false; }
		 */
		if (cl.hasOption("outputcsv"))
			Config.resultCSV = cl.getOptionValue("outputcsv");
		if (cl.hasOption("outputtxt"))
			Config.resultTXT = cl.getOptionValue("outputtxt");
		if (cl.hasOption("outputhtml"))
			Config.resultHTML = cl.getOptionValue("outputhtml");
		if (cl.hasOption("outputnotifier"))
			Config.resultNotifier = cl.getOptionValue("outputnotifier");
		if (cl.hasOption("outputcloneset"))
			Config.resultCloneSet = cl.getOptionValue("outputcloneset");
		if (Config.resultCSV == null && Config.resultTXT == null && Config.resultHTML == null
				&& Config.resultNotifier == null && Config.resultCloneSet == null) {
			System.err.println("Usage Error: please set output file name. (-oc, -ot, -oh, -on, -ocs)");
			System.exit(1);
		}

		if (cl.hasOption("sim"))
			try {
				Config.SIM_TH = Double.parseDouble(cl.getOptionValue("sim"));
				if (0.0D > Config.SIM_TH || 1.0D < Config.SIM_TH)
					throw new NumberFormatException();
			} catch (NumberFormatException e) {
				System.err.println("Usage Error: can't set similarity threshold.");
				System.exit(1);
			}
		if (cl.hasOption("size"))
			try {
				Config.METHOD_NODE_TH = Integer.parseInt(cl.getOptionValue("size"));
				if (Config.METHOD_NODE_TH < 0)
					throw new NumberFormatException();
			} catch (NumberFormatException e) {
				System.err.println("Usage Error: can't set method size threshold.");
				System.exit(1);
			}
		if (cl.hasOption("sizeb")) {
			try {
				Config.BLOCK_NODE_TH = Integer.parseInt(cl.getOptionValue("sizeb"));
				if (Config.BLOCK_NODE_TH < 0)
					throw new NumberFormatException();
			} catch (NumberFormatException e) {
				System.err.println("Usage Error: can't set block size threshold.");
				System.exit(1);
			}
		} else {
			Config.BLOCK_NODE_TH = Config.METHOD_NODE_TH;
		}
		if (cl.hasOption("min-lines"))
			try {
				Config.LINE_TH = Integer.parseInt(cl.getOptionValue("min-lines"));
				if (Config.LINE_TH < 0)
					throw new NumberFormatException();
			} catch (NumberFormatException e) {
				System.err.println("Usage Error: can't set min line threshold.");
				System.exit(1);
			}
		if (cl.hasOption("charset"))
			Config.charset = cl.getOptionValue("charset");
		if (cl.hasOption("threads")) {
			try {
				Config.NUM_THREADS = Integer.parseInt(cl.getOptionValue("threads"));
				if (Config.NUM_THREADS < 0)
					throw new NumberFormatException();
			} catch (NumberFormatException e) {
				System.err.println("Usage Error: can't set num threads to negative number.");
				System.exit(1);
			}
		} else
			Config.NUM_THREADS = 1;
	}

}
