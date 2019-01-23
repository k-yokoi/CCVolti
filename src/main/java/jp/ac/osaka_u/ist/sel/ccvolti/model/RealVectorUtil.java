package jp.ac.osaka_u.ist.sel.ccvolti.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.RealVector;

public abstract class RealVectorUtil extends RealVector {
	public final static List<Integer> getSparseIndexList(OpenMapRealVector vector) {
		List<Integer> indexList = new ArrayList<Integer>();
		Iterator<Entry> iterator = vector.sparseIterator();
		while (iterator.hasNext()) {
			Entry entry = iterator.next();
			indexList.add(entry.getIndex());
		}
		Collections.sort(indexList);
		return indexList;
	}

	public final static List<Double> getDenseValueList(RealVector vector) {
		List<Double> valueList = new ArrayList<Double>(vector.getDimension());
		Iterator<Entry> iterator = vector.iterator();
		while (iterator.hasNext()) {
			Entry entry = iterator.next();
			valueList.add(entry.getValue());
		}
		return valueList;
	}

}
