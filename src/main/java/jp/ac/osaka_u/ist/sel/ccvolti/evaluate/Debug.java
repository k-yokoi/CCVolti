package jp.ac.osaka_u.ist.sel.ccvolti.evaluate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jp.ac.osaka_u.ist.sel.ccvolti.model.Block;
import jp.ac.osaka_u.ist.sel.ccvolti.model.Word;

public class Debug {


	public static void debug(List<Block> blockList) throws FileNotFoundException {
		System.out.println(blockList.size());
		PrintWriter writer=new PrintWriter(new FileOutputStream("word.csv"));	
		for (Block method : blockList) {
			writer.println(method.getFileName() + "." +method.getName());
			for(Word word: method.getWordList()){
				writer.printf(",%s,%d,\r\n",word.getName(), word.getCount());
			}						
		}		
		writer.close();
	}
	

	public static void searchFile(File file) throws IOException{		
		if(file.isFile() && file.getName().endsWith(".c") || file.getName().endsWith(".h")){	
			covnert02(file);			
		}else if(file.isDirectory()){
			File[] fileList = file.listFiles();
			for(File f: fileList)
				searchFile(f);
		}	
	}

	static private void covnert(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file.toString()));
		String createFile = file.toString().substring(1);
		(new File(createFile)).getParentFile().mkdirs();
		PrintWriter writer=new PrintWriter(new FileOutputStream(createFile));
		String line = null;
		boolean ret = false;
		boolean com1 = false;
		boolean com2 = false;
		while((line=reader.readLine())!=null){
			char[] buf = line.toCharArray();
			int len = buf.length;
			boolean txtf = true;
			for(int i=0;i<len;i++) {
				if(!com1 && !com2 &&!ret && buf[i]=='"')
					ret = true;
				if(!com1 && !com2 && ret && buf[i]=='"')
					ret = false;
				if(!ret && !com1 && !com2 && buf[i]=='/'){
					if(i+1<len){
						if(buf[i+1]=='/'){
							i++;
							com1 = true;
						}
					}
				}	
				if(!ret && com1 && !com2 && buf[i]=='/'){
					if(i+1<len){
						if(buf[i+1]=='/'){
							i++; 
							com1 = false;
							txtf = false;
						}
					}
				}
				if(!ret && !com1 && !com2 && buf[i]=='/'){
					if(i+1<len){
						if(buf[i+1]=='*'){
							i++; 
							com2 = true;
							
						}
					}
				}	
				if(!ret && !com1 && com2 && buf[i]=='*'){
					if(i+1<len){
						if(buf[i+1]=='/'){
							i++; 
							com2 = false;
							txtf = false;
						}
					}
				}
				if(!com1 && !com2 && i<len && txtf)
					writer.print(buf[i]);
	        }	
			if(!com1 && !com2)
				writer.println();
		}
		reader.close();
		writer.close();
	}
	
	static private void covnert02(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file.toString()));
		String createFile = file.toString().substring(1);
		(new File(createFile)).getParentFile().mkdirs();
		PrintWriter writer=new PrintWriter(new FileOutputStream(createFile));
		String line = null;
		while((line=reader.readLine())!=null){
			if(!line.startsWith("#"))
				writer.println(line);

		}
		reader.close();
		writer.close();
	}
	

}
