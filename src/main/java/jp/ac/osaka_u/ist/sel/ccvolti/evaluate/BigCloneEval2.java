package jp.ac.osaka_u.ist.sel.ccvolti.evaluate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;

public class BigCloneEval2 {
	private static HashSet<String> clonePairSet = new HashSet<String>();
	public static void main() {
		File path = new File("bcb_data");

		File[] dirs = path.listFiles();

		for (File file : dirs) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line;
				while ((line = reader.readLine()) != null) {
					clonePairSet.add(line);
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
		
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("BCB.clones")));
			for (String string : clonePairSet) {
				writer.print(string);
				writer.print("\n");
			}
			writer.close();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

}
