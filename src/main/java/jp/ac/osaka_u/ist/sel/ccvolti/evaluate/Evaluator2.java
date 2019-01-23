package jp.ac.osaka_u.ist.sel.ccvolti.evaluate;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.linear.OpenMapRealVector;

import jp.ac.osaka_u.ist.sel.ccvolti.CloneDetector;
import jp.ac.osaka_u.ist.sel.ccvolti.Config;
import jp.ac.osaka_u.ist.sel.ccvolti.model.Block;
import jp.ac.osaka_u.ist.sel.ccvolti.model.ClonePair;
import jp.ac.osaka_u.ist.sel.ccvolti.model.RealVectorUtil;


public class Evaluator2 {

	public static final String RESULT = "result.csv";
	public static final String BENCHMARK = "benchmark\\ant-1.8.2-mete-cmcd.csv";
	//public static final String BENCHMARK = "benchmark\\argouml-0.34-mete-cmcd.csv";
	public static final String PRECISION_RESULT = "precision_result.csv";
	public static final String RECALL_RESULT = "recall_result.csv";
	public static ArrayList<ClonePair> resultList = new ArrayList<ClonePair>();
	public static ArrayList<ClonePair> benchmarkList = new ArrayList<ClonePair>();
	public static ArrayList<Block> blockList = new ArrayList<>();
	public static HashMap<String, Integer> blockMap = new HashMap<String, Integer>();
	
	public static void evaluate() throws Exception {	
		readBlockListCSV();
		readResultCVS();
		readBenchmark();
		PrintWriter writer01=new PrintWriter(new FileOutputStream(PRECISION_RESULT));		
		PrintWriter writer02=new PrintWriter(new FileOutputStream(RECALL_RESULT));
		
		//Precisionの評価
		int truePositive=0;
		for(ClonePair pairA:resultList){
			boolean f=false;
			for(ClonePair pairB:benchmarkList){
				if((pairA.cloneA.getId()==pairB.cloneA.getId() && pairA.cloneB.getId()==pairB.cloneB.getId())
					|| (pairA.cloneA.getId()==pairB.cloneB.getId() && pairA.cloneB.getId()==pairB.cloneA.getId())){	
					truePositive++;
					f=true;
					break;
				}				
			}	
			writer01.printf("%s,%s,%f,%s,\r\n",pairA.cloneA.getName(),pairA.cloneB.getName(),pairA.sim,f);			
		}			
		
		for(ClonePair pairB:benchmarkList){
			boolean f=false;
			for (ClonePair pairA : resultList) {
				if((pairA.cloneA.getId()==pairB.cloneA.getId() && pairA.cloneB.getId()==pairB.cloneB.getId())
						|| (pairA.cloneA.getId()==pairB.cloneB.getId() && pairA.cloneB.getId()==pairB.cloneA.getId())){	
							f=true;
						break;
					}
			}
			writer02.printf("%s,%s,%s,%f,%d,%f,\r\n",pairB.cloneA.getName(),pairB.cloneB.getName(), f, innerPoint(pairB), 
					Math.abs(pairB.cloneA.getNodeNum()-pairB.cloneB.getNodeNum()),
					 Math.abs(pairB.cloneA.getLen() - pairB.cloneB.getLen()));				
		}	
		
		writer01.close();	
		writer02.close();	
		
		double precision = (double)truePositive/(double)resultList.size();
		double recall = (double)truePositive/(double)benchmarkList.size();
		System.out.printf("Precision : %f\r\n",precision);
		System.out.printf("Recall : %f\r\n",recall);
		System.out.printf("F : %f\r\n",2/(1/precision+1/recall));
		
	}

	private static double innerPoint(ClonePair pair){
		OpenMapRealVector v1 = pair.cloneA.getVector();
		OpenMapRealVector v2 = pair.cloneB.getVector();
		
		double s = 0;
		for (int index : RealVectorUtil.getSparseIndexList(v1)) {
			s += v1.getEntry(index) * v2.getEntry(index);
		}
		return s;
	}

	/**
	 * <p>ベンチマーク読み込み</p>
	 * @throws IOException 
	 */
	private static void readBenchmark() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(BENCHMARK)));		
		String line = null;
		int lineNo=1;
		while((line=reader.readLine())!=null){
			if(lineNo==34) break;
			lineNo++;
		}
		while((line=reader.readLine())!=null){
			if(line.equals("Cluster Information")) break;
			
			String[] tmp = line.split("\t");
			if(Double.valueOf(tmp[11])>Config.E_DIFF || Integer.valueOf(tmp[5])<Config.METHOD_NODE_TH || Integer.valueOf(tmp[10])<Config.METHOD_NODE_TH) continue;
			
			ClonePair pair;
			/*
			Block blockA = new Block();
			Block blockB = new Block();
			blockA.setName(tmp[1]+ "." + getName(tmp[2]));
			blockB.setName(tmp[6]+ "." + getName(tmp[7]));
			pair.cloneA = blockA;
			pair.cloneB = blockB;
			*/
			
			Block cloneA = null;
			Block cloneB = null;
			Integer a,b;
			String[] str = tmp[3].split("\\D");
			a=blockMap.get(tmp[1].replaceAll("\\/", "") + getName(tmp[2]) + tmp[3].split("\\D")[1]);
			b=blockMap.get(tmp[6].replaceAll("\\/", "") + getName(tmp[7]) + tmp[8].split("\\D")[1]);
			if(a!=null && b!=null){
				pair = new ClonePair(blockList.get(a), blockList.get(b), 0.0);
				
			
				if(	Math.abs(pair.cloneA.getNodeNum()-pair.cloneB.getNodeNum())<Config.DIFF_TH &&
				Math.abs(pair.cloneA.getLen() - pair.cloneB.getLen()) < Config.DIS_TH &&
				!pair.cloneA.getName().contains("test") && !pair.cloneA.getName().contains("Test")
				&& !pair.cloneB.getName().contains("test") && !pair.cloneB.getName().contains("Test") ) {
					benchmarkList.add(pair);
				}
			}
		}	
		reader.close();		
	}

	private static String getName(String str) {
		String[] tmp = str.split("\\.");
		return tmp[tmp.length-1].split("\\(")[0];
	}


	/**
	 * <p>検出結果読み込み</p>
	 * @throws IOException
	 */
	private static void readResultCVS() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(RESULT)));		
		String line = null;
		while((line=reader.readLine())!=null){
			ClonePair pair;
			String[] tmp = line.split(",");
			Block cloneA = null;
			Block cloneB = null;
			Integer a,b;
			a=blockMap.get(tmp[1].replaceAll("\\\\", "") + tmp[0] + tmp[2]);
			b=blockMap.get(tmp[4].replaceAll("\\\\", "") + tmp[3] + tmp[5]);
			if(a!=null && b!=null){
				pair = new ClonePair(blockList.get(a), blockList.get(b), Double.valueOf(tmp[6]));
				if(	!pair.cloneA.getName().contains("test") && !pair.cloneA.getName().contains("Test")
					&&!pair.cloneB.getName().contains("test") && !pair.cloneB.getName().contains("Test") )
				
				resultList.add(pair);
			}
			
			
			
				
		}		
		reader.close();
	}
	
	private static void readBlockListCSV() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(CloneDetector.BLOCKLIST_CSV)));
		String line;
		int i=0;
		while((line=reader.readLine())!=null){
			String[] tmp = line.split(",");
			if(tmp[0].equals("id")) continue;
			if(tmp.length>3){
				Block block = new Block();
				block.setId(Integer.parseInt(tmp[0]));
				block.setFileName(tmp[1].replaceAll("\\\\", "\\/"));
				block.setName(tmp[2]);
				block.setStartLine(Integer.parseInt(tmp[3]));
				block.setEndLine(Integer.parseInt(tmp[4]));
				block.setNodeNum(Integer.parseInt(tmp[5]));
				OpenMapRealVector vector = new OpenMapRealVector(Integer.parseInt(tmp[6]));
				block.setLen(Double.parseDouble(tmp[7]));
				for(String e:tmp[8].split("\\s")){
					String[] str = e.split(":");
					if(str.length==2)vector.setEntry(Integer.parseInt(str[0]), Double.parseDouble(str[1]));
				}
				block.setVector(vector);
				blockList.add(block);
				blockMap.put(tmp[1].replaceAll("\\\\", "") + tmp[2] + tmp[3], i++);
			}
		}
		reader.close();
	}
	
	public static void main(String[] args) throws Exception {
		evaluate();
		Toolkit.getDefaultToolkit().beep();
	}
}