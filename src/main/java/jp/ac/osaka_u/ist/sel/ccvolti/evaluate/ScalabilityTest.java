package jp.ac.osaka_u.ist.sel.ccvolti.evaluate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.Buffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class ScalabilityTest {
	static final File datasetPath = new File("D:\\IJaDataset2.0\\dataset");
	// static final String outputPath = "D:\\workspace\\CloneDetectorTest\\subset4";
	static final long seed = 241675004816786605l;

	static ArrayList<String> fileList = new ArrayList<String>();
	static ArrayList<File> copyList = new ArrayList<File>();

	public static void main(String[] args) {
		// System.out.println("search file");
		// try {
		// searchFile(datasetPath);
		// } catch (IOException e) {
		// // TODO 自動生成された catch ブロック
		// e.printStackTrace();
		// }
		System.out.println("read file list");
		readFileList(new File(args[0]));
		System.out.println(fileList.size() + "files\r\n");

		String outputPath = args[1];
		File newdir = new File(outputPath);
		delete(newdir);
		newdir.mkdir();

		int scale = 1000;
		Random rand = new Random(seed);
		ArrayList<Integer> list = new ArrayList<Integer>();

		int lines = 0;
		int loc = 0;
		for (int i = 0; i < 6; i++) {
			File subdir = new File(newdir + "\\" + Integer.toString(scale));
			System.out.println(subdir.getName() + "\t start");
			if (i == 0)
				subdir.mkdir();
			int targetScale = (int) (scale * 1.04);
			double rate = 0;
			for (;;) {
				File tmpdir = new File(newdir + "\\" + "tmp");
				tmpdir.mkdirs();
				/*
				 * int interval = scale / 1000; if(interval > 1000) interval = 1000; for (int i1
				 * = 0; i1 < interval; i1++) {
				 */
				int p = rand.nextInt(fileList.size());

				// File out = new File(subdir + "\\" +
				// fileList.get(p).getName());
				while (!isUTF(new File(fileList.get(p)))) {
					p = rand.nextInt(fileList.size());
					fileList.remove(p);
				}
				lines += countLines(new File(fileList.get(p)));
				copyList.add(new File(fileList.get(p)));
				fileList.remove(p);
				// }
				if (lines > targetScale) {
					for (File source : copyList) {
						File out = new File(subdir + "\\" + source.getParentFile().getName() + "_" + source.getName());
						try {
							Files.copy(source.toPath(), out.toPath());

						} catch (IOException e) {
							// TODO 自動生成された catch ブロック
							e.printStackTrace();
						}
					}
					copyList.clear();
					loc = countLOC(subdir);
					System.out.println(subdir.getName() + "\t: " + loc);
					lines = loc;
					if (loc > scale * 0.95) {
						System.out.println("done");
						break;
					}
					if(rate == 0) rate = scale * 1.04 / loc;
					int inc = (int) ((scale * 1.04 - loc)*rate);
					targetScale = loc + inc;

				}
			}

			if (i < 5) {
				try {
					File out = new File(newdir + "\\" + Integer.toString(scale * 10));
					Files.copy(subdir.toPath(), out.toPath());
					for (File file : subdir.listFiles()) {
						Files.copy(file.toPath(), new File(out.toString() + "\\" + file.getName()).toPath());
					}
				} catch (IOException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
			}
			scale *= 10;
			/*
			 * File out = new File(subdir + "\\" + fileList.get(p).getName()); try {
			 * Files.copy(fileList.get(p).toPath(), out.toPath()); fileList.remove(p);
			 * size--; } catch (IOException e) { // TODO 自動生成された catch ブロック
			 * e.printStackTrace(); } } if(lines <= scale) { lines += countLines(tmpdir);
			 * if(lines>scale) loc = countLOC(subdir); } else loc += countLOC(tmpdir);
			 * 
			 * for (File file : tmpdir.listFiles()) { try { Files.copy(file.toPath(), new
			 * File(subdir + "\\" + file.getName()).toPath()); file.delete(); } catch
			 * (IOException e) { // TODO 自動生成された catch ブロック e.printStackTrace(); } }
			 * 
			 * if (loc >= scale) { System.out.println(subdir.getName() + "\t: " + loc);
			 * System.out.println("done"); System.out.println(); break; } else {
			 * System.out.println(subdir.getName() + "\t: " + loc); } }
			 */

		}

	}

	private static void readFileList(File file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				fileList.add(line);
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

	public static void searchFile(File file) throws IOException {
		System.out.println("search " + file.toString());
		String[] f = file.list();
		for (String string : f) {
			File ff = new File(string);
			if (ff.isFile()) {
				fileList.add(string);
			} else {
				searchFile(ff);
			}
		}
		System.out.println("done");
	}

	private static int countLOC(File file) {
		int loc = -1;
		ProcessBuilder pb = new ProcessBuilder("D:\\workspace\\CloneDetectorTest\\cloc", file.getAbsolutePath(),
				"--csv", "--include-lang=Java");
		Process p;
		StringBuilder buffer = new StringBuilder();
		try {
			p = pb.start();
			InputStream errIn = p.getErrorStream();
			InputStream stdIn = p.getInputStream();
			int c;
			while ((c = stdIn.read()) != -1) {
				// System.out.print((char) c);
				buffer.append((char) c);
			}
			stdIn.close();
			while ((c = errIn.read()) != -1)
				System.err.print((char) c);
			errIn.close();
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		String[] lines = buffer.toString().split("\\r\\n");

		loc = Integer.parseInt(lines[5].split(",")[4]);
		return loc;

	}

	public static int countLines(File file) {
		int lines = 0;
		if (file.isFile()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (!line.startsWith("\\s*/"))
						lines++;
				}
				reader.close();
			} catch (FileNotFoundException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		} else {
			for (File file2 : file.listFiles()) {
				lines += countLines(file2);
			}
		}
		return lines;

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

	private static boolean isUTF(File file) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
			byte[] src = Files.readAllBytes(file.toPath());
			byte[] tmp = new String(src, "UTF8").getBytes("UTF8");
			if (!Arrays.equals(tmp, src))
				return false;

			int c;
			while ((c = reader.read()) != -1) {
				if (!Character.isDefined(c)) {
					return false;
				}
				if (!(c == 9 || c == 10 || c == 13) && Character.isISOControl(c)) {
					return false;
				}
			}
			return true;
		} catch (UnsupportedEncodingException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}
}
