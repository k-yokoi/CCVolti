package jp.ac.osaka_u.ist.sel.ccvolti;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.linear.OpenMapRealVector;

import jp.ac.osaka_u.ist.sel.ccvolti.model.Block;
import jp.ac.osaka_u.ist.sel.ccvolti.model.ClonePair;
import jp.ac.osaka_u.ist.sel.ccvolti.model.CloneSet;
import jp.ac.osaka_u.ist.sel.ccvolti.model.RealVectorUtil;

public class Outputter {
	/**
	 * <p>
	 * 評価用出力
	 * <p>
	 * 
	 * @param clonePairList
	 * @throws IOException
	 */
	public static void outputCSV(List<ClonePair> clonePairList) throws IOException {
		System.out.println("output CSV");
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(Config.resultCSV)));
		writer.println(
				"A name,A class name,A start line,A end line, A method start line , A method end line, B name,B class name,B start line,B end line, B method start line , B method end line,similarity");
		for (ClonePair pair : clonePairList) {
			Block cloneA = pair.cloneA;
			Block cloneB = pair.cloneB;
			writer.printf("%s,%s,%d,%d,%d,%d,%s,%s,%d,%d,%d,%d,%f,\r\n", cloneA.getName(), cloneA.getFileName(),
					cloneA.getStartLine(), cloneA.getEndLine(), cloneA.getMethodStartLine(), cloneA.getMethodEndLine(),
					cloneB.getName(), cloneB.getFileName(), cloneB.getStartLine(), cloneB.getEndLine(),
					cloneB.getMethodStartLine(), cloneB.getMethodEndLine(), pair.sim);
		}
		writer.close();
	}

	/**
	 * <p>
	 * 可視化用出力
	 * <p>
	 * 
	 * @param cloneSetList
	 * @throws IOException
	 */
	public static void outputHTML(List<ClonePair> clonePairList) throws IOException {
		System.out.println("output HTML");
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(Config.resultHTML)));
		BufferedReader reader = null;
		writer.println("<pre>");
		int id = 0;
		for (ClonePair pair : clonePairList) {
			writer.printf("***********************\r\n");
			writer.printf("clonepair:%d == %f \r\n", id++, pair.sim);
			writer.printf("-----------\r\n");
			writer.printf("filename : %s (%d-%d) \r\n", pair.cloneA.getFileName(), pair.cloneA.getStartLine(),
					pair.cloneA.getEndLine());
			writer.printf("methodname : %s (%d) \r\n", pair.cloneA.getName(), pair.cloneA.getNodeNum());
			writer.printf("blockID : (%d) \r\n", pair.cloneA.getId());

			reader = new BufferedReader(new FileReader(pair.cloneA.getFileName()));

			for (int i = 1; i <= pair.cloneA.getMethodEndLine(); i++) {
				String line = reader.readLine();
				line = line.replaceAll("<", "\\&lt;");
				line = line.replaceAll(">", "\\&gt;");

				if (i == pair.cloneA.getStartLine())
					writer.println("<font color=\"#ff0000\">");
				if (i >= pair.cloneA.getMethodStartLine())
					writer.printf("%4d: %s\r\n", i, line);
				if (i == pair.cloneA.getEndLine())
					writer.println("</font>");

			}
			reader.close();
			writer.printf("-----------\r\n");
			writer.printf("filename : %s (%d-%d) \r\n", pair.cloneB.getFileName(), pair.cloneB.getStartLine(),
					pair.cloneB.getEndLine());
			writer.printf("methodname : %s (%d) \r\n", pair.cloneB.getName(), pair.cloneB.getNodeNum());
			writer.printf("blockID : (%d) \r\n", pair.cloneB.getId());

			reader = new BufferedReader(new FileReader(pair.cloneB.getFileName()));
			for (int i = 1; i <= pair.cloneB.getMethodEndLine(); i++) {
				String line = reader.readLine();
				line = line.replaceAll("<", "\\&lt;");
				line = line.replaceAll(">", "\\&gt;");

				if (i == pair.cloneB.getStartLine())
					writer.println("<font color=\"#ff0000\">");
				if (i >= pair.cloneB.getMethodStartLine())
					writer.printf("%4d: %s\r\n", i, line);
				if (i == pair.cloneB.getEndLine())
					writer.println("</font>");

			}
			reader.close();
		}
		writer.println("</pre>");
		writer.close();
	}

	/**
	 * <p>
	 * 評価用出力
	 * <p>
	 * 
	 * @param cloneSetList
	 * @throws FileNotFoundException
	 */
	public static void outputCSVforCPP(ArrayList<ClonePair> clonePairList) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(new FileOutputStream(Config.resultCSV));
		for (ClonePair pair : clonePairList) {
			Block cloneA = pair.cloneA;
			Block cloneB = pair.cloneB;
			writer.printf("%s,%s,%d,%d,%f,%f,%f,\r\n", cloneA.getFileName() + "." + cloneA.getName(),
					cloneB.getFileName() + "." + cloneB.getName(), cloneA.getNodeNum(), cloneB.getNodeNum(),
					cloneA.getLen(), cloneB.getLen(), pair.sim);
		}
		writer.close();
	}

	/**
	 * <p>
	 * 可視化用出力
	 * <p>
	 * 
	 * @param cloneSetList
	 * @throws IOException
	 */
	public static void outputTXT(List<ClonePair> clonePairList) throws IOException {
		System.out.println("output CSV");
		PrintWriter writer = new PrintWriter(new FileOutputStream(Config.resultTXT));
		BufferedReader reader = null;
		int id = 0;
		for (ClonePair pair : clonePairList) {
			writer.printf("***********************\r\n");
			writer.printf("clonepair:%d == %f \r\n", id++, pair.sim);
			writer.printf("-----------\r\n");
			writer.printf("filename : %s (%d-%d) \r\n", pair.cloneA.getFileName(), pair.cloneA.getStartLine(),
					pair.cloneA.getEndLine());
			writer.printf("methodname : %s (%d) \r\n", pair.cloneA.getName(), pair.cloneA.getNodeNum());
			writer.printf("blockID : (%d) \r\n", pair.cloneA.getId());

			reader = new BufferedReader(new FileReader(pair.cloneA.getFileName()));
			for (int i = 1; i <= pair.cloneA.getEndLine(); i++) {
				String line = reader.readLine();
				if (i >= pair.cloneA.getStartLine() && i <= pair.cloneA.getEndLine())
					writer.printf("%4d: %s\r\n", i, line);
			}
			reader.close();
			writer.printf("-----------\r\n");
			writer.printf("filename : %s (%d-%d) \r\n", pair.cloneB.getFileName(), pair.cloneB.getStartLine(),
					pair.cloneB.getEndLine());
			writer.printf("methodname : %s (%d) \r\n", pair.cloneB.getName(), pair.cloneB.getNodeNum());
			writer.printf("blockID : (%d) \r\n", pair.cloneB.getId());

			reader = new BufferedReader(new FileReader(pair.cloneB.getFileName()));
			for (int i = 1; i <= pair.cloneB.getEndLine(); i++) {
				String line = reader.readLine();
				if (i >= pair.cloneB.getStartLine() && i <= pair.cloneB.getEndLine())
					writer.printf("%4d: %s\r\n", i, line);
			}
			reader.close();
		}
		writer.close();
	}

	/**
	 * <p>
	 * CSV出力
	 * <p>
	 * 
	 * @param cloneSetList
	 * @throws FileNotFoundException
	 */
	public static void outputCloneSetCSVforCPP(ArrayList<CloneSet> cloneSetList) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(new FileOutputStream(Config.resultCSV));
		int i = 0;
		for (CloneSet set : cloneSetList) {
			writer.printf("%d,%d,%d,%f\r\n", i++, set.cloneList.size(), set.over50Token, set.tokenAverage);
		}
		writer.close();
	}

	/**
	 * <p>
	 * 可視化用出力
	 * <p>
	 * 
	 * @param cloneSetList
	 * @throws IOException
	 */
	public static void outputCloneSetTXTforCPP(List<CloneSet> cloneSetList) throws IOException {
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(Config.resultCloneSet)));
		BufferedReader reader = null;
		int id = 0;
		for (CloneSet set : cloneSetList) {

			writer.printf("=================================================\r\n");
			writer.printf("cloneset:%d (%d)\r\n", id++, set.cloneList.size());
			writer.printf("=================================================\r\n");
			for (Block clone : set.cloneList) {
				writer.printf("------------------------------------\r\n");
				writer.printf("filename : %s ( line:%d-%d ) \r\n", clone.getFileName(), clone.getStartLine(),
						clone.getEndLine());
				writer.printf("methodname : %s ( token:%d ) \r\n", clone.getName(), clone.getNodeNum());
				writer.printf("-------------------------------------\r\n");
				reader = new BufferedReader(new FileReader(clone.getFileName()));
				for (int i = 1; i <= clone.getEndLine() + 10; i++) {
					String line = reader.readLine();
					if (line == null)
						break;
					if (i >= clone.getStartLine() && i <= clone.getEndLine())
						writer.printf("%s\r\n", line);
				}
				reader.close();
			}
		}
		writer.close();
	}

	/**
	 * <p>
	 * 評価用出力
	 * <p>
	 * 
	 * @param cloneSetList
	 * @throws FileNotFoundException
	 */
	public static void outputCSVforJava(ArrayList<ClonePair> clonePairList) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(new FileOutputStream(Config.resultCSV));
		for (ClonePair pair : clonePairList) {
			Block cloneA = pair.cloneA;
			Block cloneB = pair.cloneB;
			writer.printf("%s,%s,%d,%d,%f,%f,%f,\r\n", cloneA.getFileName() + "." + cloneA.getName(),
					cloneB.getFileName() + "." + cloneB.getName(), cloneA.getNodeNum(), cloneB.getNodeNum(),
					cloneA.getLen(), cloneB.getLen(), pair.sim);
		}
		writer.close();
	}

	/**
	 * <p>
	 * 可視化用出力
	 * <p>
	 * 
	 * @param cloneSetList
	 * @throws FileNotFoundException
	 */
	//	public static void outputTXTforJava(ArrayList<ClonePair> clonePairList) throws FileNotFoundException {
	//		PrintWriter writer = new PrintWriter(new FileOutputStream(CloneDetector.RESULT_FILE));
	//		int id = 0;
	//		for (ClonePair pair : clonePairList) {
	//			writer.printf("***********************\r\n");
	//			writer.printf("clonepair:%d=%f\r\n", id++, pair.sim);
	//			writer.printf("-----------\r\n");
	//			writer.printf("%s:%d,%f\r\n", pair.cloneA.getFileName(), pair.cloneA.getNodeNum(), pair.cloneA.getLen());
	//			writer.println(pair.cloneA.getCode());
	//			writer.printf("-----------\r\n");
	//			writer.printf("%s:%d,%f\r\n", pair.cloneB.getFileName(), pair.cloneB.getNodeNum(), pair.cloneB.getLen());
	//			writer.println(pair.cloneB.getCode());
	//		}
	//		writer.close();
	//	}

	public static void outputBlockList(ArrayList<Block> blockList) throws IOException {
		System.out.println("output BlockList");
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(CloneDetector.BLOCKLIST_CSV)));
		writer.printf("id,class,name,start,end,node,vector dimention,vector len,vector\r\n");
		for (Block block : blockList) {
			writer.printf("%d,%s,%s,%d,%d,%d,%d,%f,", block.getId(), block.getFileName(), block.getName(),
					block.getStartLine(), block.getEndLine(), block.getNodeNum(), block.getVector().getDimension(),
					block.getLen());
			OpenMapRealVector vector = block.getVector();
			for (int index : RealVectorUtil.getSparseIndexList(vector)) {
				writer.printf("%d:%f ", index, vector.getEntry(index));
			}
			writer.printf(",\r\n");
		}
	}

	public static void outputStatisticsSample(ArrayList<ClonePair> clonePairList, double e, double m, double p)
			throws IOException {
		System.out.println("output Statistic Sample");
		double N = clonePairList.size();
		int n = (int) (N / (Math.pow(e / m, 2) * ((N - 1.0) / (p * (1.0 - p))) + 1.0));
		System.out.println("the number of randam sample : " + n);
		Random rand = new Random();
		boolean num[] = new boolean[clonePairList.size()];
		int sample[] = new int[n];
		for (int i = 0; i < num.length; i++) {
			num[i] = false;
		}
		for (int i = 0; i < n;) {
			int r = rand.nextInt(clonePairList.size());
			if (num[r] == false) {
				sample[i] = r;
				num[r] = true;
				i++;
			}
		}
		Arrays.sort(sample);

		PrintWriter writer = new PrintWriter(
				new BufferedWriter(new FileWriter(Config.resultTXT.split("\\.")[0] + "-sample.txt")));
		BufferedReader reader = null;

		for (int i = 0; i < n; i++) {
			ClonePair pair = clonePairList.get(sample[i]);
			writer.printf("***********************\r\n");
			writer.printf("%d:clonepair:%d == %f \r\n", i, sample[i], pair.sim);
			writer.printf("-----------\r\n");
			writer.printf("filename : %s (%d-%d) \r\n", pair.cloneA.getFileName(), pair.cloneA.getStartLine(),
					pair.cloneA.getEndLine());
			writer.printf("methodname : %s (%d) \r\n", pair.cloneA.getName(), pair.cloneA.getNodeNum());
			writer.printf("blockID : (%d) \r\n", pair.cloneA.getId());

			reader = new BufferedReader(new FileReader(pair.cloneA.getFileName()));
			for (int j = 1; j <= pair.cloneA.getEndLine(); j++) {
				String line = reader.readLine();
				if (j >= pair.cloneA.getStartLine() && j <= pair.cloneA.getEndLine())
					writer.printf("%4d: %s\r\n", j, line);
			}
			reader.close();
			writer.printf("-----------\r\n");
			writer.printf("filename : %s (%d-%d) \r\n", pair.cloneB.getFileName(), pair.cloneB.getStartLine(),
					pair.cloneB.getEndLine());
			writer.printf("methodname : %s (%d) \r\n", pair.cloneB.getName(), pair.cloneB.getNodeNum());
			writer.printf("blockID : (%d) \r\n", pair.cloneB.getId());

			reader = new BufferedReader(new FileReader(pair.cloneB.getFileName()));
			for (int j = 1; j <= pair.cloneB.getEndLine(); j++) {
				String line = reader.readLine();
				if (j >= pair.cloneB.getStartLine() && j <= pair.cloneB.getEndLine())
					writer.printf("%4d: %s\r\n", j, line);
			}
			reader.close();
		}

		writer.close();
	}

	public static void outputForBigCloneEval(ArrayList<ClonePair> clonePariList) {
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("bcd.clones")));
			for (ClonePair clonePair : clonePariList) {
				String[] buffer = new String[8];
				String[] pathA = clonePair.cloneA.getFileName().split("\\\\");
				buffer[0] = pathA[pathA.length - 2];
				buffer[1] = pathA[pathA.length - 1];
				buffer[2] = Integer.toString(clonePair.cloneA.getStartLine());
				buffer[3] = Integer.toString(clonePair.cloneA.getEndLine());

				String[] pathB = clonePair.cloneB.getFileName().split("\\\\");
				buffer[4] = pathB[pathB.length - 2];
				buffer[5] = pathB[pathB.length - 1];
				buffer[6] = Integer.toString(clonePair.cloneB.getStartLine());
				buffer[7] = Integer.toString(clonePair.cloneB.getEndLine());

				for (int i = 0; i < 7; i++) {
					writer.print(buffer[i]);
					writer.print(',');
				}
				writer.println(buffer[7]);
			}
			writer.close();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	/**
	 * <p>
	 * 可視化用出力
	 * <p>
	 *
	 * @param cloneSetList
	 * @param fileList 
	 * @throws IOException
	 */
	public static void outputNotifier(List<CloneSet> cloneSetList, List<String> fileList) throws IOException {
		System.out.println("output for CloneNotifier");
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(Config.resultNotifier)));
		int id = 0;
		writer.printf("source_files {\r\n");
		for (String filename : fileList) {
			writer.printf("%s\r\n", filename);
		}
		writer.printf("}\r\n");
		writer.printf("clone_sets {\r\n");
		id = 0;
		for (CloneSet set : cloneSetList) {
			writer.printf("=================================================\r\n");
			writer.printf("cloneset:%d\r\n", id++);
			for (Block clone : set.cloneList) {
				writer.printf("\t %s @ %s ( startline:%d endline:%d token:%d )\r\n", clone.getName(),
						clone.getFileName(), clone.getStartLine(), clone.getEndLine(), clone.getNodeNum());
			}
		}
		writer.printf("}/r/n");
		writer.close();
	}
}
