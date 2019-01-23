package jp.ac.osaka_u.ist.sel.ccvolti.model;

import java.util.ArrayList;

public class Method {
	
	private int id=0;
	private String className;
	private int startLine = 0;
	private int endLine=0;
	private String name;
	private String code;
	private double len;
	private ArrayList<Word> wordList = new ArrayList<Word>();
	private int wordNum=0;
	private int statementNum=0;
	private int operatorNum=0;
	private int nodeNum=0;
	private double vector[];
	private boolean checkFlg = false;


	/**
	 * <p>メソッドIDの取得</p>
	 * @return
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * <p>メソッドIDの設定</p>
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	
	
	/**
	 * <p>メソッド名の取得</p>
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * <p>メソッド名の設定</p>
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * <p>クラス名の取得</p>
	 * @return
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * <p>クラス名の設定</p>
	 * @param className
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * <p>ワードリストの取得</p>
	 * @return
	 */
	public ArrayList<Word> getWordList() {
		return wordList;
	}


	/**
	 * <p>ソースコード取得</p>
	 * @return
	 */
	public String getCode() {
		return code;
	}

	/**
	 * <p>ソースコード設定</p>
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * <p>ノード数取得</p>
	 * @return
	 */
	public int getNodeNum() {
		return nodeNum;
	}

	/**
	 * <p>ノード数設定</p>
	 * @param length
	 */
	public void incNodeNum() {
		nodeNum++;
	}

	/**
	 * <p>距離の取得</p>
	 * @return
	 */
	public double getLen() {
		return len;
	}

	/**
	 * <p>距離の設定</p>
	 * @return
	 */
	public void setLen(double len) {
		this.len = len;
	}

	/**
	 * <p>ワード数の取得</p>
	 * @return
	 */
	public int getWordNum() {
		return wordNum;
	}

	/**
	 * <p>ワード数の設定</p>
	 * @param wordCount
	 */
	public void setWordNum(int wordCount) {
		this.wordNum = wordCount;
	}
	
	
	/**
	 * <p>ステートメント数の取得</p>
	 * @return
	 */
	public int getStatementNum() {
		return statementNum;
	}

	/**
	 * <p>ステートメント数の設定</p>
	 * @param statementNum
	 */
	public void setStatementNum(int statementNum) {
		this.statementNum = statementNum;
	}
	
	/**
	 * <p>オペレータ数の取得</p>
	 * @return
	 */
	public int getOperatorNum() {
		return operatorNum;
	}

	/**
	 * <p>オペレータ数の設定</p>
	 * @param operatorNum
	 */
	public void setOperatorNum(int operatorNum) {
		this.operatorNum = operatorNum;
	}

	/**
	 * <p>特徴ベクトルの取得</p>
	 * @return
	 */
	public double[] getVector() {
		return vector;
	}

	/**
	 * <p>特徴ベクトルの設定</p>
	 * @param vector
	 */
	public void setVector(double[] vector) {
		this.vector = vector;
	}

	/**
	 * <p>判定</p>
	 * @return
	 */
	public boolean isCheckFlg() {
		return checkFlg;
	}

	/**
	 * <p>判定</p>
	 * @param checkFlg
	 */
	public void setCheckFlg(boolean checkFlg) {
		this.checkFlg = checkFlg;
	}

	
	/**
	 * <p>開始行の取得</p>
	 * @return
	 */
	public int getStartLine() {
		return startLine;
	}

	/**
	 * <p>開始行の設定</p>
	 * @param startLine
	 */
	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	/**
	 * <p>終了行の取得</p>
	 * @return
	 */
	public int getEndLine() {
		return endLine;
	}
	
	/**
	 * <p>終了行の設定</p>
	 * @param endLine
	 */
	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}


	
	
	
	
	
	
	
	
	
	
	
	

}
