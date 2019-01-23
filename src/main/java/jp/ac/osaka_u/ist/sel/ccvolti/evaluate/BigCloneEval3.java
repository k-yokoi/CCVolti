package jp.ac.osaka_u.ist.sel.ccvolti.evaluate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import jp.ac.osaka_u.ist.sel.ccvolti.CloneDetector;
import jp.ac.osaka_u.ist.sel.ccvolti.model.Block;
import jp.ac.osaka_u.ist.sel.ccvolti.model.ClonePair;

public class BigCloneEval3 {

	private static File[] readDatasetList(String file) {
		File path = new File(file);
		File[] files = path.listFiles();
		return files;
	}

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		System.out.println("BigCloneEval");
		File newdir = new File("bcb_data");
		delete(newdir);
		newdir.mkdir();

		File path = new File("bcb_reduced");

		File[] dirs = path.listFiles();

		for (File file : dirs) {
			try {
				System.out.println(file.getName() + " : " + searchFile(file) + "\tLOC");
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
	}

	public static int searchFile(File file) throws IOException {
		int loc = 0;
		if (file.isFile() && (file.getName().endsWith(".java"))) {
			try {
				loc += countLOC(file);
			} catch (Exception e) {
				System.err.println(file.getPath() + "\\" + file.getName() + " : error" + e);
			}
		} else if (file.isDirectory()) {
			// System.out.println(file.toString());
			File[] fileList = file.listFiles();
			for (File f : fileList)
				loc += searchFile(f);
		}
		return loc;
	}
	
	public static int countLOC(File file){
		int loc = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			while(reader.readLine()!=null){
				loc++;
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return loc;
		
	}
	private static void delete(File f) {
		/*
		 * ファイルまたはディレクトリが存在しない場合は何もしない
		 */
		if (f.exists() == false) {
			return;
		}

		if (f.isFile()) {
			/*
			 * ファイルの場合は削除する
			 */
			f.delete();

		} else if (f.isDirectory()) {
			/*
			 * ディレクトリの場合は、すべてのファイルを削除する
			 */

			/*
			 * 対象ディレクトリ内のファイルおよびディレクトリの一覧を取得
			 */
			File[] files = f.listFiles();

			/*
			 * ファイルおよびディレクトリをすべて削除
			 */
			for (int i = 0; i < files.length; i++) {
				/*
				 * 自身をコールし、再帰的に削除する
				 */
				delete(files[i]);
			}
			/*
			 * 自ディレクトリを削除する
			 */
			f.delete();
		}
	}
}