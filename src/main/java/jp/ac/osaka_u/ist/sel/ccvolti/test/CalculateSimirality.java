package jp.ac.osaka_u.ist.sel.ccvolti.test;

import java.awt.image.BufferedImageFilter;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CalculateSimirality {
	static int id0 = 554;
	static int id1 = 29835;
	static ArrayList<String> dictionary=new ArrayList<String>();

	public static void main(String[] args) {
		double[] vector0 = null, vector1 = null;
		readDictionary();
		try {
			BufferedReader reader = new BufferedReader(new FileReader("blocklist.csv"));
			String line;
			reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] str = line.split(",");
				if (Integer.parseInt(str[0]) == id0) {
					vector0 = getVector(str);
				}
				if (Integer.parseInt(str[0]) == id1) {
					vector1 = getVector(str);
					System.out.println(simirality(vector0, vector1));
					break;
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

		System.out.println("vec 0");
		printVector(vector0);
		System.out.println("vec 1");
		printVector(vector1);
		System.out.println("finish");
	}

	private static double[] getVector(String[] str) {
		double[] vector = new double[Integer.parseInt(str[6])];
		for (String string : str[8].split("\\s")) {
			vector[Integer.parseInt(string.split(":")[0])] = Double.parseDouble(string.split(":")[1]);
		}
		return vector;
	}
	
	private static void readDictionary(){
		try {
			BufferedReader reader = new BufferedReader(new FileReader("dictionary.txt"));
			String line;
			while ((line = reader.readLine()) != null) {
				dictionary.add(line);
			}
		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
	
	private static void printVector(double[] vec) {
		for (int i = 0; i < vec.length ; i++) {
			if(vec[i] != 0.0) System.out.println(dictionary.get(i) + "\t:" + vec[i] + "\t");
		}
		System.out.println();
	}

	private static double simirality(double[] v0, double[] v1) {
		double scalar = 0, len0 = 0, len1 = 0;
		for (int i = 0; i < v1.length; i++) {
			scalar += v0[i] * v1[i];
			len0 += v0[i] * v0[i];
			len1 += v1[i] * v1[i];
		}
		
		for (int i = 0; i < v1.length; i++) {
			if(v0[i] * v1[i] == 0){
				//System.out.println(i + "\t: " + v0[i] /Math.sqrt(len0) + "\t, " + v1[i] /Math.sqrt(len1));
			}
		}
		
		
		return scalar / (Math.sqrt(len0) * Math.sqrt(len1));
	}
}
