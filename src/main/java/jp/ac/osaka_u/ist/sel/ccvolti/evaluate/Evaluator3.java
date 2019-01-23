package jp.ac.osaka_u.ist.sel.ccvolti.evaluate;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import jp.ac.osaka_u.ist.sel.ccvolti.CloneDetector;
import jp.ac.osaka_u.ist.sel.ccvolti.model.Block;
import jp.ac.osaka_u.ist.sel.ccvolti.model.ClonePair;

public class Evaluator3 {
	static String[] args1 = { "-l", "c", "--size", "30", "-d", "", "--sizeb", "20" };
	static ArrayList<ClonePair270> clonePair270list = new ArrayList<ClonePair270>();

	private static void evaluate(String path, int start, int end) throws Exception {
		args1[5] = path;
		CloneDetector.main(args1);
		for (ClonePair270 pairA : clonePair270list) {
			if (start <= pairA.id && pairA.id <= end && 2 <= pairA.NumataIshizuChong) {
				boolean flg = false;
				for (ClonePair pairB : CloneDetector.clonePairList) {
					if (pairA.cloneA.fileName.equals(pairB.cloneA.getFileName())
							&& pairA.cloneB.fileName.equals(pairB.cloneB.getFileName())) {
						int typeA, typeB;
						if ((typeA = checkBlock(pairA.cloneA, pairB.cloneA)) != -1
								&& (typeB = checkBlock(pairA.cloneB, pairB.cloneB)) != -1) {
							System.out.println(pairA.id + " : " + typeA + "," + typeB + " : {(" + pairA.cloneA.startLine
									+ "-" + pairA.cloneA.endLine + "),(" + pairA.cloneB.startLine + "-"
									+ pairA.cloneB.endLine + ")} {(" + pairB.cloneA.getStartLine() + "-"
									+ pairB.cloneA.getEndLine() + "),(" + pairB.cloneB.getStartLine() + "-"
									+ pairB.cloneB.getEndLine() + ")}");
							flg = true;
						} 
					} else if (pairA.cloneA.fileName.equals(pairB.cloneB.getFileName())
							&& pairA.cloneB.fileName.equals(pairB.cloneA.getFileName())) {
						int typeA, typeB;
						if ((typeA = checkBlock(pairA.cloneA, pairB.cloneB)) != -1
								&& (typeB = checkBlock(pairA.cloneB, pairB.cloneA)) != -1) {
							System.out.println(pairA.id + " : " + typeA + "," + typeB + " : {(" + pairA.cloneA.startLine
									+ "-" + pairA.cloneA.endLine + "),(" + pairA.cloneB.startLine + "-"
									+ pairA.cloneB.endLine + ")} {(" + pairB.cloneB.getStartLine() + "-"
									+ pairB.cloneB.getEndLine() + "),(" + pairB.cloneA.getStartLine() + "-"
									+ pairB.cloneA.getEndLine() + ")}");
							flg = true;
						} 
					}
				}
				if (!flg)
					System.out.println(pairA.id + " : false");

			}
		}

	}

	private int checkPair(ClonePair270 pairA, ClonePair pairB) {
		if (!pairA.cloneA.fileName.equals(pairB.cloneA.getFileName())
				|| !pairA.cloneB.fileName.equals(pairB.cloneB.getFileName()))
			return -1;

		return 0;
	}

	private static int checkBlock(Block270 blockA, Block blockB) {
		if (blockA.endLine < blockB.getStartLine() || blockB.getEndLine() < blockA.startLine)
			return -1;

		if (blockA.startLine == blockB.getStartLine() && blockA.endLine == blockB.getEndLine())
			return 0;

		if (blockB.getStartLine() <= blockA.startLine && blockA.endLine <= blockB.getEndLine())
			return 1;

		if (blockA.startLine <= blockB.getStartLine() && blockB.getEndLine() <= blockA.endLine)
			return 2;

		return 3;
	}

	private static void readBenchmark(String file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			int id = 1;
			while ((line = reader.readLine()) != null) {
				clonePair270list.add(new ClonePair270(id++, line.split(",")));
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		System.out.println("Evaluator3");
		readBenchmark("benchmark\\clonepair270.csv");

		try {
			//evaluate("C:\\Users\\k-yokoi\\org\\httpd", 1, 90);
			//evaluate("C:\\Users\\k-yokoi\\org\\postgreSQL", 91, 180);
			evaluate("C:\\Users\\k-yokoi\\org\\python", 181, 270);
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

	}
 
}

class ClonePair270 {
	int id;
	Block270 cloneA;
	Block270 cloneB;
	int NumataIshizu;
	int NumataIshizuChong;

	public ClonePair270(int id, String[] args) {
		this.id = id;
		this.cloneA = new Block270(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
		this.cloneB = new Block270(args[3], Integer.parseInt(args[4]), Integer.parseInt(args[5]));
		this.NumataIshizu = Integer.parseInt(args[6]);
		this.NumataIshizuChong = Integer.parseInt(args[7]);
	}
}

class Block270 {
	String fileName;
	int startLine;
	int endLine;

	public Block270(String fileName, int startLine, int endLine) {
		this.fileName = fileName;
		this.startLine = startLine;
		this.endLine = endLine;
	}
}
