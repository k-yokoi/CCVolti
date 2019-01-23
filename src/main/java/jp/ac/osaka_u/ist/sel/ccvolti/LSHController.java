package jp.ac.osaka_u.ist.sel.ccvolti;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jp.ac.osaka_u.ist.sel.ccvolti.model.Block;

public class LSHController {
	public static final int FALCONN64 = 0;
	public static final int FALCONN32 = 1;
	public static final int E2LSH = 2;
	public static final int NO_LSH = -1;


	/**
	 * <p>
	 * パラメータ計算
	 * </p>
	 */
	public void computeParam(List<Block> blockList, int dimention) {
		String[] fromArray = { "E2LSH\\LSHMain.exe", Integer.toString(blockList.size()),
				Integer.toString(blockList.size()), Integer.toString(dimention),
				Double.toString(Config.LSH_PROB), Double.toString(Config.LSH_R), CloneDetector.DATASET_FILE,
				CloneDetector.DATASET_FILE, "3978000000", "-c" };
		for (String string : fromArray) {
			System.out.print(string + " ");
		}
		System.out.println();
		ProcessBuilder pb = new ProcessBuilder(fromArray);
		try {
			PrintWriter writer = new PrintWriter(new FileOutputStream(CloneDetector.PARAM_FILE));
			Process p = pb.start();
			InputStream errIn = p.getErrorStream();
			InputStream stdIn = p.getInputStream();
			int c;
			while ((c = stdIn.read()) != -1)
				writer.print((char) c);
			stdIn.close();
			while ((c = errIn.read()) != -1)
				;// System.out.print((char)c);
			errIn.close();
			p.waitFor();
			writer.close();
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}

	public void computeParamFalse(List<Block> blockList, Map<String, Integer> wordMap) {
		try {
			PrintWriter writer = new PrintWriter(new FileOutputStream(CloneDetector.PARAM_FILE));
			writer.println("1");
			writer.println("R");
			writer.println(Double.toString(Config.LSH_R));
			writer.println("Success probability");
			writer.println(Double.toString(Config.LSH_PROB));
			writer.println("Dimension");
			writer.println(wordMap.size());
			writer.println("R^2");
			writer.println(Double.toString(Config.LSH_R * Config.LSH_R));
			writer.println("Use <u> functions");
			writer.println("1");
			writer.println("k");
			writer.println("4");
			writer.println("m [# independent tuples of LSH functions]");
			writer.println("4");
			writer.println("L");
			writer.println("6");
			writer.println("W");
			writer.println("4.0000000000");
			writer.println("T");
			writer.println(blockList.size());
			writer.println("typeHT");
			writer.println("3");
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>
	 * LSHの実行
	 * </p>
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void execute(List<Block> blockList, int dimention, int program) {
		int numHardThread = Runtime.getRuntime().availableProcessors();
		if (Config.NUM_THREADS == 0 || Config.NUM_THREADS > numHardThread)
			Config.NUM_THREADS = numHardThread;

		ArrayList<String> fromArray = new ArrayList<String>();
		if (program == FALCONN64) {
			fromArray.add(Paths.get(CloneDetector.javaClassPath, "FALCONN", "falconn4bcd.exe").toString());
			fromArray.add(Integer.toString(blockList.size()));
			fromArray.add(Integer.toString(dimention));
			fromArray.add(Integer.toString(Config.LSH_L));
			fromArray.add(CloneDetector.DATASET_FILE);
			fromArray.add(CloneDetector.DATASET_FILE);
			fromArray.add(Double.toString(Config.SIM_TH));
			fromArray.add(Integer.toString(Config.NUM_THREADS));
		} else if (program == FALCONN32) {
			fromArray.add(Paths.get(CloneDetector.javaClassPath, "FALCONN32", "falconn4bcd.exe").toString());
			fromArray.add(Integer.toString(blockList.size()));
			fromArray.add(Integer.toString(dimention));
			fromArray.add(Integer.toString(Config.LSH_L));
			fromArray.add(CloneDetector.DATASET_FILE);
			fromArray.add(CloneDetector.DATASET_FILE);
			fromArray.add(Double.toString(Config.SIM_TH));
			fromArray.add(Integer.toString(Config.NUM_THREADS));
		} else if (program == E2LSH) {
			computeParam(blockList, dimention);

			fromArray.add("E2LSH\\LSHMain.exe");
			fromArray.add(Integer.toString(blockList.size()));
			fromArray.add(Integer.toString(blockList.size()));
			fromArray.add(Integer.toString(dimention));
			fromArray.add(Double.toString(Config.LSH_PROB));
			fromArray.add(Double.toString(Config.LSH_R));
			fromArray.add(CloneDetector.DATASET_FILE);
			fromArray.add(CloneDetector.DATASET_FILE);
			fromArray.add("3978000000");
			fromArray.add("-p");
			fromArray.add(CloneDetector.PARAM_FILE);
		}

		for (String string : fromArray) {
			System.out.print(string + " ");
		}
		System.out.println();

		ProcessBuilder pb = new ProcessBuilder(fromArray);
		try {
			PrintWriter writer;
			if (Config.LSH_PRG == E2LSH) {
				writer = new PrintWriter(new FileOutputStream(CloneDetector.LSH_FILE));
			} else {
				writer = new PrintWriter(new FileOutputStream(CloneDetector.LSH_LOG));
			}
			Process p = pb.start();
			InputStream errIn = p.getErrorStream();
			InputStream stdIn = p.getInputStream();
			int c;
			while ((c = stdIn.read()) != -1)
				writer.print((char) c);
			stdIn.close();
			while ((c = errIn.read()) != -1)
				System.err.print((char) c);
			errIn.close();
			p.waitFor();
			writer.close();
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}
}

