package edu.nyu.cs.cs2580;

import java.util.Vector;

/**
 * Sorted List of doc id's
 * @author kunal
 *
 */
public class Postings extends Vector<Integer> 
{
	private Integer cachedIndex;

	public Integer getCachedIndex() {
		return cachedIndex;
	}

	public void setCachedIndex(Integer cachedIndex) {
		this.cachedIndex = cachedIndex;
	}
}
