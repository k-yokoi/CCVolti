package jp.ac.osaka_u.ist.sel.ccvolti.model;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SparseVector implements MyVector{

	public int dimentions;
	private Map<Integer, Double> sparseVector;
	
	public SparseVector(int d) {
		this.dimentions = d;
		this.sparseVector = new HashMap<Integer, Double>();
	}
	
	@Override
	public void set(int i, double e){
		if(i<0 || i>=dimentions) {
			throw new IndexOutOfBoundsException();
		}
		if(e>0.0) sparseVector.put(i, e);
	}
	
	@Override
	public double get(int i){
		if(i<0 || i>=dimentions) {
			throw new IndexOutOfBoundsException();
		}
		Double e;
		if((e=sparseVector.get(i))==null){
			return 0.0;
		}
		return e;
	}
	
	@Override
	public String toString(){
		List<Integer> sortedKeys = new ArrayList<Integer>(sparseVector.keySet());
		Collections.sort(sortedKeys);
		StringBuffer buffer = new StringBuffer();
		for (Integer i : sortedKeys) {
			buffer.append(i + ":" + sparseVector.get(i).floatValue() + " ");
		}
		return buffer.toString();
	}
	
	public Set<Integer> keySet(){
		return sparseVector.keySet();
	}
}
