package jp.ac.osaka_u.ist.sel.ccvolti.evaluate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import jp.ac.osaka_u.ist.sel.ccvolti.model.Block;
import jp.ac.osaka_u.ist.sel.ccvolti.model.ClonePair;
import jp.ac.osaka_u.ist.sel.ccvolti.model.CloneSet;
import jp.ac.osaka_u.ist.sel.ccvolti.model.Method;

public class Comparator {
	
	public static final String BENCHMARK = "mecc_apache.txt";
	public static final String RECALL_RESULT = "recall_result.csv";
	private static List<Block> blockList;
	
	public static void compareMeCC(ArrayList<CloneSet> cloneSetList, List<Block> blockListIn) throws Exception {

		ArrayList<CloneSet> cloneSetListMeCC = readMeCCBenchmark();
		ArrayList<ClonePair> clonePairListMeCC = makeClonePairList(cloneSetListMeCC);
		ArrayList<ClonePair> clonePairList = makeClonePairList(cloneSetList);
		calcRecall(clonePairList,clonePairListMeCC);
		blockList = blockListIn;
		
	}
	
	/**
	 * <p>MeCCベンチマーク読み込み</p>
	 * @throws IOException
	 */
	private static ArrayList<CloneSet> readMeCCBenchmark() throws IOException{
		
		ArrayList<CloneSet> cloneSetList = new ArrayList<CloneSet>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(BENCHMARK)));		
		String line = null;
		
		CloneSet cloneset = null;
		String backLine = null;
		while((line=reader.readLine())!=null){
			if(line.length()>0 && ( '0'<= line.charAt(0) && '9'>= line.charAt(0))){
				if(cloneset!=null && cloneset.cloneList.size()>=2 && !backLine.contains("X"))
					cloneSetList.add(cloneset);
				cloneset= new CloneSet();
			}
			
			if(line.contains("function : ")){
				if(cloneset!=null){
					Method method = new Method();
					line = line.replace("function : ","");
					String[] tmp = line.split(" @ ");
					method.setName(tmp[0].replace(" ",""));
					method.setClassName(tmp[1].replace(" ","").replace("/","\\").replace("\t",""));					
					
					for (Block m : blockList) {
						System.out.println(m.getFileName()+":"+m.getName());
						System.out.println(method.getClassName()+":"+method.getName());
						if(m.getFileName().equals(method.getClassName())&&
							m.getName().equals(method.getName())){
							cloneset.cloneList.add(m);
							break;
						}						
					}					
				}
			}			
			backLine = line;
		}
		reader.close();
		return cloneSetList;
		
		
		
	}
	
	/**
	 * <p>クローンペアリストの生成</p>
	 */
	static private ArrayList<ClonePair> makeClonePairList(ArrayList<CloneSet> setList){
		ArrayList<ClonePair> clonePairList = new ArrayList<ClonePair>();
		int set=0;
		for(CloneSet cloneSet: setList){
			for(int i=0; i<cloneSet.cloneList.size(); i++){
				for(int j=i+1; j<cloneSet.cloneList.size(); j++){
//					ClonePair clonePair = new ClonePair();
//					clonePair.cloneA = cloneSet.cloneList.get(i);
//					clonePair.cloneB = cloneSet.cloneList.get(j);
//					clonePair.set = set;
//					clonePairList.add(clonePair);
				}
			}			
			set++;
		}
		return clonePairList;
	}

	static private void calcRecall(ArrayList<ClonePair> pairList, ArrayList<ClonePair> clonePairListMeCC) throws FileNotFoundException{
		
		int trueclone = 0;
		for(ClonePair pairA: clonePairListMeCC){
			for(ClonePair pairB: pairList){
				if( ((pairA.cloneA.getName().equals(pairB.cloneA.getName()) && (pairA.cloneA.getFileName().equals(pairB.cloneA.getFileName()))) &&
					(pairA.cloneB.getName().equals(pairB.cloneB.getName()) && (pairA.cloneB.getFileName().equals(pairB.cloneB.getFileName())))) ||
					((pairA.cloneA.getName().equals(pairB.cloneB.getName()) && (pairA.cloneA.getFileName().equals(pairB.cloneB.getFileName()))) &&
					(pairA.cloneB.getName().equals(pairB.cloneA.getName()) && (pairA.cloneB.getFileName().equals(pairB.cloneA.getFileName()))))){
					trueclone++;
//					pairA.check=true;
					break;					
				}				
			}	
		}
		
		System.out.printf("True:%d\r\n",trueclone);
		System.out.printf("|B|:%d\r\n",clonePairListMeCC.size());
		System.out.printf("|A|:%d\r\n",pairList.size());
		System.out.printf("|A+B|:%d\r\n",pairList.size()+clonePairListMeCC.size()-trueclone);
		System.out.printf("Recall:%f\r\n",(double)(pairList.size())/(double)(pairList.size()+clonePairListMeCC.size()-trueclone));
	
	
		PrintWriter writer=new PrintWriter(new FileOutputStream(RECALL_RESULT));
		for(ClonePair pair:clonePairListMeCC){
//			writer.printf("%d,%s,%s,%s\r\n",
//					pair.set,
//					pair.cloneA.getClassName()+"."+pair.cloneA.getName(),
//					pair.cloneB.getClassName()+"."+pair.cloneB.getName(),
//					pair.check
//					);
		}
		writer.flush();
		writer.close();
		
		
		
	}
	
	

	
}
