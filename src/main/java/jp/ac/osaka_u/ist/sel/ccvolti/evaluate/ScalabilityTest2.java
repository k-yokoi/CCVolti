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

public class ScalabilityTest2 {
	static final File datasetPath = new File("D:\\IJaDataset2.0\\dataset");
	static final String outputPath = "D:\\workspace\\CloneDetectorTest\\subset4";
	static final long seed = 241675004816786605l;

	static ArrayList<String> fileList = new ArrayList<String>();
	static ArrayList<File> copyList = new ArrayList<File>();

	public static void main(String[] args) throws IOException {
		
		File file = new File("C:\\Users\\k-yokoi\\Desktop\\selected_1460530.java");
		if(isUTF(file)) {
			System.out.println("UTFです");
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		int c;
		while ((c=reader.read())!=-1) {
			if(!Character.isDefined(c)) {
				System.out.println("\n" + (char) c + " is not defined");
			}
			if(!Character.isBmpCodePoint(c)) {
				System.out.println("\n" + (char) c + "is not contain BMP point");
			}
			if(!(c== 9 || c == 10 || c == 13) && Character.isISOControl(c)) {
				System.out.println("\n" +  c + "is contrlo");
			}
			System.out.print((char) c);
		}
		reader.close();
		
	}

	private static boolean isUTF(File file) {
		try {
			byte[] src = Files.readAllBytes(file.toPath());
			byte[] tmp = new String(src, "UTF8").getBytes("UTF8");
			return Arrays.equals(tmp, src);
		} catch (UnsupportedEncodingException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}
}
