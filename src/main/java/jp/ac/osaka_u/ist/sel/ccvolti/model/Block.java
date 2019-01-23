package jp.ac.osaka_u.ist.sel.ccvolti.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.OpenMapRealVector;

public class Block implements Comparable<Block> {

	private int id;
	private String fileName;
	private int startLine;
	private int endLine;
	private int methodStartLine;
	private int methodEndLine;
	private String name;
	// private String code;
	private double len;
	private List<Word> wordList = new ArrayList<Word>();
	private int nodeNum = 0;
	private OpenMapRealVector vector;
	// private HashMap<Integer, String> stringVector;
	// private boolean checkFlg = false;
	private Block parent;
	// private ArrayList<Block> children;

	/**
	 * <p>
	 * メソッドIDの取得
	 * </p>
	 * 
	 * @return
	 */
	public final int getId() {
		return id;
	}

	/**
	 * <p>
	 * メソッドIDの設定
	 * </p>
	 * 
	 * @param id
	 */
	public final void setId(int id) {
		this.id = id;
	}

	/**
	 * <p>
	 * メソッド名の取得
	 * </p>
	 * 
	 * @return
	 */
	public final String getName() {
		return name;
	}

	/**
	 * <p>
	 * メソッド名の設定
	 * </p>
	 * 
	 * @param name
	 */
	public final void setName(String name) {
		this.name = name;
	}

	/**
	 * <p>
	 * クラス名の取得
	 * </p>
	 * 
	 * @return
	 */
	public final String getFileName() {
		return fileName;
	}

	/**
	 * <p>
	 * クラス名の設定
	 * </p>
	 * 
	 * @param className
	 */
	public final void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public final double getLen() {
		return len;
	}

	public final void setLen(double len) {
		this.len = len;
	}
	
	/**
	 * <p>
	 * ワードリストへの追加
	 * </p>
	 */
	public final boolean addWord(String word) {
		for(Word w : wordList) {
			if(w.getName().equals(word)) {
				w.addCount(1);
				return true;
			}
		}
		wordList.add(new Word(word, Word.WORD, 1));
		return true;
	}
	
	/**
	 * <p>
	 * ワードリストへの追加
	 * </p>
	 */
	public final boolean addWord(String[] words) {
		for (String word : words) {
			this.addWord(word);
		}
		return true;
	}

	/**
	 * <p>
	 * ワードリストの取得
	 * </p>
	 * 
	 * @return
	 */
	public final List<Word> getWordList() {
		return wordList;
	}

	public final void clearWordList() {
		wordList.clear();
		wordList = null;
	}

	/**
	 * <p>
	 * ノード数取得
	 * </p>
	 * 
	 * @return
	 */
	public final int getNodeNum() {
		return nodeNum;
	}

	/**
	 * <p>
	 * ノード数設定
	 * </p>
	 * 
	 * @param nodeNum
	 */
	public final void setNodeNum(int nodeNum) {
		this.nodeNum = nodeNum;
	}

	/**
	 * <p>
	 * ノード数加算
	 * </p>
	 * 
	 * @param
	 */
	public final void incNodeNum() {
		nodeNum++;
	}

	/**
	 * <p>
	 * 特徴ベクトルの取得
	 * </p>
	 * 
	 * @return
	 */
	public final OpenMapRealVector getVector() {
		return vector;
	}

	/**
	 * <p>
	 * 特徴ベクトルの設定
	 * </p>
	 * 
	 * @param vector
	 */
	public final void setVector(OpenMapRealVector vector) {
		this.vector = vector;
	}

	// /**
	// * <p>判定</p>
	// * @return
	// */
	// public final boolean isCheckFlg() {
	// return checkFlg;
	// }
	//
	// /**
	// * <p>判定</p>
	// * @param checkFlg
	// */
	// public final void setCheckFlg(boolean checkFlg) {
	// this.checkFlg = checkFlg;
	// }

	/**
	 * <p>
	 * 開始行の取得
	 * </p>
	 * 
	 * @return
	 */
	public final int getStartLine() {
		return startLine;
	}

	/**
	 * <p>
	 * 開始行の設定
	 * </p>
	 * 
	 * @param startLine
	 */
	public final void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	/**
	 * <p>
	 * 終了行の取得
	 * </p>
	 * 
	 * @return
	 */
	public final int getEndLine() {
		return endLine;
	}

	/**
	 * <p>
	 * 終了行の設定
	 * </p>
	 * 
	 * @param endLine
	 */
	public final void setEndLine(int endLine) {
		this.endLine = endLine;
	}

	/**
	 * <p>
	 * ブロックの行数の取得
	 * </p>
	 * 
	 * @param endLine
	 */
	public final int getLineSize() {
		return endLine - startLine + 1;
	}

	/**
	 * <p>
	 * 開始行の取得
	 * </p>
	 * 
	 * @return
	 */
	public final int getMethodStartLine() {
		return methodStartLine;
	}

	/**
	 * <p>
	 * 開始行の設定
	 * </p>
	 * 
	 * @param startLine
	 */
	public final void setMethodStartLine(int startLine) {
		this.methodStartLine = startLine;
	}

	/**
	 * <p>
	 * 終了行の取得
	 * </p>
	 * 
	 * @return
	 */
	public final int getMethodEndLine() {
		return methodEndLine;
	}

	/**
	 * <p>
	 * 終了行の設定
	 * </p>
	 * 
	 * @param endLine
	 */
	public final void setMethodEndLine(int endLine) {
		this.methodEndLine = endLine;
	}

	/**
	 * <p>
	 * 親ブロックの取得
	 * </p>
	 * 
	 * @return
	 */
	public final Block getParent() {
		return parent;
	}

	/**
	 * <p>
	 * 親ブロックの設定
	 * </p>
	 * 
	 * @param parent
	 */
	public final void setParent(Block parent) {
		this.parent = parent;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Block other = (Block) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public boolean match(String className, String name) {
		if (this.fileName.equals(className) && this.name.equals(name)) {
			return true;
		}
		return false;
	}

	@Override
	public int compareTo(Block b) {
		return this.id - b.id;
	}

}
