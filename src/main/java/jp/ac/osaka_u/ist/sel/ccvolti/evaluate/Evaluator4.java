package jp.ac.osaka_u.ist.sel.ccvolti.evaluate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.ac.osaka_u.ist.sel.ccvolti.CloneDetector;
import jp.ac.osaka_u.ist.sel.ccvolti.model.Block;
import jp.ac.osaka_u.ist.sel.ccvolti.model.ClonePair;

public class Evaluator4 {
	static String[] args1 = { "-l", "c", "--size", "30", "-d", "", "--sizeb", "20" };
	static List<List<Block>> cloneSetList = new ArrayList<List<Block>>();
	static List<String> lines = new ArrayList<String>();
	
	private static void evaluate(String path, int start, int end) throws Exception {
	
	}
	

	private static void readResult(String file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			int count = 0;
			List<Block> cloneList = new ArrayList<Block>();
			cloneSetList.add(cloneList);
			while ((line = reader.readLine()) != null) {
				lines.add(line);
				count++;
				if (line.split("\\s+").length==1) {
					cloneList = new ArrayList<Block>();
					cloneSetList.add(cloneList);
				} else {
					Block block = new Block();
					block.setId(count);
					block.setFileName(line.split("\\s+")[3]);
					block.setStartLine(Integer.parseInt(line.split("\\s+")[4].split(":")[1]));
					block.setEndLine(Integer.parseInt(line.split("\\s+")[4].split(":")[2]) + block.getStartLine());
					cloneList.add(block);
				}
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

	private static void searchClone(String cloneA, int startA, int stopA, String cloneB, int startB, int stopB) {
		for (List<Block> cloneSet : cloneSetList) {
			int Max = cloneSet.size();
			for (int i = 0; i < Max; i++) {
				for (int j = 0; j < Max; j++) {
					if (i != j) {
						if (isDetected(cloneA, startA, stopA, cloneSet.get(i).getFileName(),
								cloneSet.get(i).getStartLine(), cloneSet.get(i).getEndLine())
								&& isDetected(cloneB, startB, stopB, cloneSet.get(j).getFileName(),
										cloneSet.get(j).getStartLine(), cloneSet.get(j).getEndLine())) {
							System.out.println(cloneSet.get(i).getId() + ": " + lines.get(cloneSet.get(i).getId()-1));
							System.out.println(cloneSet.get(j).getId() + ": " + lines.get(cloneSet.get(j).getId()-1));
							System.out.println();
						}
					}
				}
			}
		}
	}

	private static boolean isDetected(String cloneFile, int cloneStart, int cloneStop, String refFile, int refStart,
			int refStop) {
		if (!refFile.contains(cloneFile))
			return false;
		if (cloneStart == refStart && cloneStop == refStop)
			return true;

		if (cloneStart > refStart && cloneStop < refStop)
			return true;

		if (Math.abs(refStart - cloneStart) <= 5 && Math.abs(cloneStop - refStop) <= 5)
			return true;
		return false;
	}

	public static void main(String[] args) {
		System.out.println("Evaluator3");
		//readResult("C:\\cygwin64\\home\\k-yokoi\\tool\\Deckard-parallel1.3\\app\\clusters\\post_cluster_vdb_50_2_allg_0.85_30");
		readResult("C:\\cygwin64\\home\\k-yokoi\\tool\\Deckard-parallel1.3\\app\\clusters\\post_cluster_vdb_50_2_allg_0.85_30");
		System.out.println("read done");
		//searchClone("srclib/apr-util/misc/apr_rmm.c",258,264,"srclib/apr-util/misc/apr_rmm.c",267,273);
		//searchClone("unix/readwrite.c",336,346,"unix/readwrite.c",356,361);
		searchClone("Modules/stropmodule.c",706,717,"Objects/stringobject.c",2319,2328);
		
		//boolean result = searchClone("modules/arch/netware/mod_nw_ssl.c",525,537,"modules/arch/win32/mod_isapi.c",287,301);
		System.out.println("done");
	}

}