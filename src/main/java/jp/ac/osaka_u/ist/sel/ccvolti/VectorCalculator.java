package jp.ac.osaka_u.ist.sel.ccvolti;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.linear.OpenMapRealVector;

import jp.ac.osaka_u.ist.sel.ccvolti.model.Block;
import jp.ac.osaka_u.ist.sel.ccvolti.model.RealVectorUtil;
import jp.ac.osaka_u.ist.sel.ccvolti.model.Word;

public class VectorCalculator {
	private static final int APPEARANCE_TH = 1;
	private static final int SPARSE = 0;
	private static final int DENSE = 1;
	private int dimention;

	public int getDimention() {
		return dimention;
	}

	/**
	 * <p>
	 * メソッドリストのフィルタリング
	 * </p>
	 * 
	 * @return
	 */
	public ArrayList<Block> filterMethod(List<Block> blockList) {
		ArrayList<Block> newBlockList = new ArrayList<Block>(blockList.size());
		int i = 0;
		for (Block block : blockList) {
			if (filter(block)) {
				/*
				 * && !('A' <= block.getName().charAt(0) && block.getName().charAt(0) <= 'Z')
				 */
				// !method.getName().contains("test") &&
				// !method.getName().contains("Test")&&
				// !method.getClassName().contains("test") &&
				// !method.getClassName().contains("Test")){
				block.setId(i++);
				newBlockList.add(block);
				/*
				 * for(Word word: block.getWordList()){
				 * if(!CloneDetector.wordMap.containsKey(word.getName()))
				 * CloneDetector.wordMap.put(word.getName(),i++); }
				 */
			}
		}
		newBlockList.trimToSize();
		return newBlockList;
	}

	private static boolean filter(Block block) {
		if (block.getParent() == null) {
			if (block.getNodeNum() < Config.METHOD_NODE_TH)
				return false;
		} else {
			if (block.getNodeNum() < Config.BLOCK_NODE_TH)
				return false;
		}
		if (block.getLineSize() < Config.LINE_TH)
			return false;

		return true;
	}

	/**
	 * <p>
	 * 重みの計算
	 * </p>
	 * 
	 * @throws FileNotFoundException
	 */
	public void calculateVector(List<Block> blockList) throws FileNotFoundException {
		// ワードマップの生成
		HashMap<String, Integer> wordFreqMap = new HashMap<String, Integer>();
		ArrayList<String> dictionary = new ArrayList<String>();

		// ワードの出現頻度の計測と、ワード辞書の生成
		int elementCount = 0;
		for (Block block : blockList) {
			if (block.getParent() == null) {
				for (Word word : block.getWordList()) {
					if (wordFreqMap.containsKey(word.getName())) {
						int value = wordFreqMap.get(word.getName());
						wordFreqMap.put(word.getName(), ++value);
					} else {
						wordFreqMap.put(word.getName(), 1);
						dictionary.add(word.getName());
					}
					elementCount++;
				}
			} else {
				elementCount += block.getWordList().size();
			}
		}

		System.out.println("blocklist size : " + blockList.size());
		System.out.println("word count : " + wordFreqMap.size());
		System.out.println("element count : " + elementCount);
		System.out.println("Density : " + String.format("%f",
				(double) elementCount / ((double) wordFreqMap.size() * (double) blockList.size())));

		// ワードの出現回数でフィルタリング（デフォルト 1以下は除去）
		Map<String, Integer> wordMap = new HashMap<>();
		int wordFreq[] = new int[wordFreqMap.size()];
		Iterator<String> iter = dictionary.iterator();
		for (int i = 0; iter.hasNext();) {
			String wordName = iter.next();
			if (wordFreqMap.get(wordName) > APPEARANCE_TH) {
				wordMap.put(wordName, i);
				wordFreq[i] = wordFreqMap.get(wordName);
				i++;
			} else {
				iter.remove();
			}
		}
		dimention = wordMap.size();
		System.out.println("filtered word count : " + wordMap.size());

		long start = System.currentTimeMillis();
		{
			final int size = blockList.size();
			for (int i = 0; i < size; i++) {
				blockList.set(i, calcTfIdf(blockList.get(i), wordMap, wordFreq, CloneDetector.countMethod));
			}
		}
		System.out.print("calc vector done : ");
		System.out.println(System.currentTimeMillis() - start + "[ms]");

		System.out.println("file out start");
		start = System.currentTimeMillis();

		if (Config.LSH_PRG == LSHController.E2LSH) {
			outputDenseDataset(blockList);
		} else {
			// outputSparseDataset(CloneDetector.blockList);
			outputSparseDatasetBinary(blockList);
		}
		outputDictionary(dictionary);

		System.out.print("file out done : ");
		System.out.println(System.currentTimeMillis() - start + "[ms]");
	}

	private static void outputSparseDataset(ArrayList<Block> blockList) {
		try {

			PrintWriter writer = new PrintWriter(
					new BufferedWriter(new FileWriter(CloneDetector.DATASET_FILE + ".txt")));
			for (Block block : blockList) {
				StringBuilder buf = new StringBuilder();
				OpenMapRealVector vector = block.getVector();
				for (int index : RealVectorUtil.getSparseIndexList(vector)) {
					buf.append(Integer.toString(index));
					buf.append(':');
					buf.append(dtos(vector.getEntry(index), 6));
					buf.append(' ');
				}
				writer.println(buf.toString());
			}
			writer.close();
		} catch (Exception e) {
			System.err.println(e);
		}

	}

	private static void outputSparseDatasetBinary(List<Block> blockList) {
		byte[] writeBuffer = new byte[8];

		try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(CloneDetector.DATASET_FILE))) {

			for (Block block : blockList) {
				OpenMapRealVector vector = block.getVector();
				int size = (int) Math.round(vector.getDimension() * vector.getSparsity());
				out.write((size >>> 0) & 0xFF);
				out.write((size >>> 8) & 0xFF);
				out.write((size >>> 16) & 0xFF);
				out.write((size >>> 24) & 0xFF);

				for (int index : RealVectorUtil.getSparseIndexList(vector)) {
					out.write((index >>> 0) & 0xFF);
					out.write((index >>> 8) & 0xFF);
					out.write((index >>> 16) & 0xFF);
					out.write((index >>> 24) & 0xFF);

					long entry = Double.doubleToLongBits(vector.getEntry(index));
					writeBuffer[0] = (byte) (entry >>> 0);
					writeBuffer[1] = (byte) (entry >>> 8);
					writeBuffer[2] = (byte) (entry >>> 16);
					writeBuffer[3] = (byte) (entry >>> 24);
					writeBuffer[4] = (byte) (entry >>> 32);
					writeBuffer[5] = (byte) (entry >>> 40);
					writeBuffer[6] = (byte) (entry >>> 48);
					writeBuffer[7] = (byte) (entry >>> 56);
					out.write(writeBuffer, 0, 8);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	private static void outputDenseDataset(List<Block> blockList) {
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(CloneDetector.DATASET_FILE)));
			for (Block block : blockList) {
				StringBuilder buf = new StringBuilder();
				OpenMapRealVector vector = block.getVector();
				for (double value : RealVectorUtil.getDenseValueList(vector)) {
					buf.append(dtos(value, 6));
					buf.append(' ');
				}
				writer.println(buf.toString());
			}
			writer.close();
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	private static void outputDictionary(ArrayList<String> dictionary) {
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("dictionary.txt")));
			for (String string : dictionary) {
				writer.println(string);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String dtos(double x, int n) {
		StringBuilder sb = new StringBuilder();
		if (x < 0) {
			sb.append('-');
			x = -x;
		}
		x += Math.pow(10, -n) / 2;
		// if(x < 0){ x = 0; }
		sb.append((long) x);
		sb.append(".");
		x -= (long) x;
		for (int i = 0; i < n; i++) {
			x *= 10;
			sb.append((int) x);
			x -= (int) x;
		}
		return sb.toString();
	}

	private static Block calcTfIdf(Block block, Map<String, Integer> wordMap, int[] wordFreq, int numMethod) {
		int wordCount = 0;
		int statementCount = 0;
		for (Word word : block.getWordList()) {
			if (wordMap.containsKey(word.getName())) {
				switch (word.getType()) {
				case Word.WORD:
					wordCount += word.getCount();
					break;
				case Word.STATEMENT:
					statementCount += word.getCount();
					break;
				}
			}
		}

		// 重みの計算
		List<Integer> indexList = new ArrayList<Integer>();
		List<Double> valueList = new ArrayList<Double>();
		double len = 0;
		for (Word word : block.getWordList()) {
			if (wordMap.containsKey(word.getName())) {
				int id = wordMap.get(word.getName());
				double tf = 0;
				switch (word.getType()) {
				case Word.WORD:
					tf = (double) word.getCount() / (double) wordCount;
					break;
				case Word.STATEMENT:
					tf = (double) word.getCount() / (double) statementCount;
					break;
				}
				double idf = 1.0 + Math.log((double) numMethod / (double) wordFreq[id]);
				double tfidf = tf * idf;
				indexList.add(id);
				valueList.add(tfidf);
				len += tfidf * tfidf;
			}
		}
		len = Math.sqrt(len);
		OpenMapRealVector vector = new OpenMapRealVector(wordMap.size());
		final int size = indexList.size();
		for (int i = 0; i < size; i++) {
			vector.setEntry(indexList.get(i), valueList.get(i) / len);
		}

		block.setVector(vector);
		block.setLen(len);
		block.clearWordList();
		// block.setStringVector(stringVector);
		return block;
	}
}
