package jp.ac.osaka_u.ist.sel.ccvolti.evaluate;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import jp.ac.osaka_u.ist.sel.ccvolti.Config;
import jp.ac.osaka_u.ist.sel.ccvolti.model.Block;
import jp.ac.osaka_u.ist.sel.ccvolti.model.ClonePair;


public class Evaluator {

	public static final String RESULT = "result.csv";
	public static final String BENCHMARK = "benchmark_ant.txt";
	public static final String PRECISION_RESULT = "precision_result.csv";
	public static final String RECALL_RESULT = "recall_result.csv";
	public static ArrayList<ClonePair> resultList = new ArrayList<ClonePair>();
	public static ArrayList<ClonePair> benchmarkList = new ArrayList<ClonePair>();
	
	public static void evaluate(List<Block> blockList) throws Exception {
		
		readResultCVS();
		readBenchmark(blockList);
		PrintWriter writer01=new PrintWriter(new FileOutputStream(PRECISION_RESULT));		
		PrintWriter writer02=new PrintWriter(new FileOutputStream(RECALL_RESULT));
		
		//Precisionの評価
		int truePositive=0;
		for(ClonePair pairA:resultList){
			boolean f=false;
			for(ClonePair pairB:benchmarkList){
				if((pairA.cloneA.equals(pairB.cloneA) && pairA.cloneB.equals(pairB.cloneB))
					|| (pairA.cloneA.equals(pairB.cloneB) && pairA.cloneB.equals(pairB.cloneA))){	
					truePositive++;
					f=true;
					break;
				}				
			}	
			writer01.printf("%s,%s,%f,%s,\r\n",pairA.cloneA,pairA.cloneB,pairA.sim,f);			
		}			
		
		for(ClonePair pair:benchmarkList){
			writer02.printf("%s,%s,%f,%s,\r\n",pair.cloneA,pair.cloneB);				
		}	
		
		writer01.close();	
		writer02.close();	
		
		double precision = (double)truePositive/(double)resultList.size();
		double recall = (double)truePositive/(double)benchmarkList.size();
		System.out.printf("Precision : %f\r\n",precision);
		System.out.printf("Recall : %f\r\n",recall);
		System.out.printf("F : %f\r\n",2/(1/precision+1/recall));
		
	}


	/**
	 * <p>ベンチマーク読み込み</p>
	 * @throws IOException 
	 */
	private static void readBenchmark(List<Block> blockList) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(BENCHMARK)));		
		String line = null;
		while((line=reader.readLine())!=null){
			ClonePair pair = new ClonePair(new Block(), new Block(), 0.0);
			String[] tmp = line.split("\t");
			pair.cloneA.setName(getName(tmp[1]));
			pair.cloneB.setName(getName(tmp[5]));
			double diff = Double.valueOf(tmp[9]);
			
			Block cloneA = null;
			Block cloneB = null;
			for (Block method : blockList) {
				if((method.getFileName()+"."+method.getName()).equals(pair.cloneA))
					cloneA = method;
				if((method.getFileName()+"."+method.getName()).equals(pair.cloneB))
					cloneB = method;
			}
			
			
			if(cloneA!=null && cloneB!=null &&
				//Math.abs(cloneA.getNodeNum()-cloneB.getNodeNum())<Params.DIFF_TH &&
				!pair.cloneA.getName().contains("test") && !pair.cloneA.getName().contains("Test")
				&&!pair.cloneB.getName().contains("test") && !pair.cloneB.getName().contains("Test") 
				&& diff<=Config.E_DIFF)
				benchmarkList.add(pair);
		}		
		reader.close();		
	}

	private static String getName(String str) {
		String name = "";
		for(char c: str.toCharArray()){
			if(c=='(') break;
			else if(c !='\"') name = name+c;			
		}
		return name;
	}


	/**
	 * <p>検出結果読み込み</p>
	 * @throws IOException
	 */
	private static void readResultCVS() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(RESULT)));		
		String line = null;
		while((line=reader.readLine())!=null){
			ClonePair pair = new ClonePair(new Block(), new Block(), 0.0);
			String[] tmp = line.split(",");
			pair.cloneA.setName(tmp[0]);
			pair.cloneB.setName(tmp[1]);
			//pair.sim = Double.valueOf(tmp[6]);
			if(!pair.cloneA.getName().contains("test") && !pair.cloneA.getName().contains("Test")
				&&!pair.cloneB.getName().contains("test") && !pair.cloneB.getName().contains("Test") )
				resultList.add(pair);
		}		
		reader.close();
		
	}
}