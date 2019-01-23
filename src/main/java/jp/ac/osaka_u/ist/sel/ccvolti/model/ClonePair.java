package jp.ac.osaka_u.ist.sel.ccvolti.model;

public class ClonePair implements Comparable<ClonePair> {
	public final Block cloneA;
	public final Block cloneB;
	public final double sim;
//	public boolean check = false;
//	public int set;
	public ClonePair(Block cloneA, Block cloneB, double sim) {
		if (cloneA.getId() < cloneB.getId()) {
			this.cloneA = cloneA;
			this.cloneB = cloneB;
		} else {
			this.cloneA = cloneB;
			this.cloneB = cloneA;
		}
		this.sim = sim;
	}

	@Override
	public int compareTo(ClonePair o) {
		if (this.cloneA.getId() == o.cloneA.getId())
			return this.cloneB.getId() - o.cloneB.getId();

		return this.cloneA.getId() - o.cloneA.getId();
	}
}
