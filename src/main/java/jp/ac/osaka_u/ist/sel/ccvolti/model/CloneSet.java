package jp.ac.osaka_u.ist.sel.ccvolti.model;

import java.util.ArrayList;

public class CloneSet {

	public ArrayList<Block> cloneList = new ArrayList<Block>();
	public int over50Token = 0;
	public double tokenAverage = 0;
	
	/**
	 * <p>メトリック計算</p>
	 */
	public void calculateMetric(){
		double sum = 0;
		for(Block clone: cloneList){
			if(clone.getNodeNum() >= 50) over50Token++;
			sum += clone.getNodeNum();
		}
		tokenAverage = sum/(double)cloneList.size();		
	}
	
}
