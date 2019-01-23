package jp.ac.osaka_u.ist.sel.ccvolti.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Word {

	private String name;
	private int type;
	private int count;
	public static final int WORD = 0;
	public static final int STATEMENT = 1;
	private static final Pattern snakePatern = Pattern.compile("_|-|\\d+");
	private static final Pattern camelPatern = Pattern.compile("(?<=[A-Z])(?=[A-Z][a-z])|(?<=[a-z])(?=[A-Z])|\\d+");

	/**
	 * <p>
	 * コンストラクタ
	 * </p>
	 * 
	 * @param word
	 */
	public Word(String name, int type, int count) {
		this.name = name;
		this.type = type;
		this.count = count;
	}

	/**
	 * <p>
	 * 識別子名の分割
	 * </p>
	 */
	public static String[] separateIdentifier(String identifier) {
		String[] strings;
		if (identifier.contains("_") || identifier.contains("-"))
			strings = snakePatern.split(identifier);
		else
			strings = camelPatern.split(identifier);

		List<String> list = new ArrayList<String>();
		for (String string : strings) {
			if (string != "")
				list.add(string.toLowerCase());
		}

		return list.toArray(new String[list.size()]);
	}

	/**
	 * <p>
	 * 単語名取得
	 * </p>
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * <p>
	 * 単語名設定
	 * </p>
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * <p>
	 * カウント値取得
	 * </p>
	 * 
	 * @return
	 */
	public double getCount() {
		return count;
	}

	/**
	 * <P>
	 * カウント
	 * </p>
	 */
	public void addCount(int c) {
		count += c;
	}

	/**
	 * <p>
	 * ワードタイプの取得
	 * <p>
	 * 
	 * @return
	 */
	public int getType() {
		return type;
	}

	/**
	 * <p>
	 * ワードタイプの設定
	 * <p>
	 * 
	 * @param type
	 */
	public void setType(int type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Word other = (Word) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return name;
	}

}
