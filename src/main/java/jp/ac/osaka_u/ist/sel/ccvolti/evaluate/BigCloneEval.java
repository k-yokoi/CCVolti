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

public class BigCloneEval {

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
			System.out.println(file + " detecting");
			try {
				ProcessBuilder pb = new ProcessBuilder("java", "-jar", "bcd4bcb.jar", "-l", "java", "-d",
						file.getAbsolutePath());
				File tmp = new File("bcb_data\\" + file.getName());
				// tmp.mkdir();
				pb.redirectErrorStream(true);

				Process p = pb.start();
				InputStream errIn = p.getErrorStream();
				InputStream stdIn = p.getInputStream();
				int c;
				StringBuilder buffer = new StringBuilder();
				while ((c = stdIn.read()) != -1) {
					System.out.print((char) c);
					buffer.append((char) c);
				}
				stdIn.close();
				while ((c = errIn.read()) != -1)
					;// System.out.print((char)c);
				errIn.close();
				p.waitFor();

				BufferedWriter writer = new BufferedWriter(new FileWriter("sysout.log"));
				writer.write(buffer.toString());
				writer.close();
				File file1 = new File("bcd.clones");
				File file2 = new File("bcb_data\\" + file.getName() + ".clone");
				if (!file1.renameTo(file2)) {
					System.err.println("移動失敗");
				}

				File file3 = new File("sysout.log");
				File file4 = new File("bcb_data\\" + file.getName() + ".log");
				if (!file3.renameTo(file4)) {
					System.err.println("移動失敗");
				}

				System.out.println(file + " done\n");
			} catch (InterruptedException | IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
		System.out.println("merge clone data");
		BigCloneEval2.main();
		long elapseTime = System.currentTimeMillis() - start;
		long minutes = TimeUnit.MILLISECONDS.toMinutes(elapseTime);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(elapseTime) - (minutes * 60);
		long miliSeconds = elapseTime - ((minutes * 60) + seconds) * 1000;
		System.out.println("finished");
		System.out.println(minutes + "m" + seconds + "s" + miliSeconds + "ms");
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