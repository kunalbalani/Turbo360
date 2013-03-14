package edu.nyu.cs.cs2580;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Vector;

/**
 * Sorted List of doc id's
 * @author kunal
 *
 */
public class Postings extends Vector<Integer> implements Serializable
{
	//doc id maps to count
	private HashMap<Integer,Integer> _countTerm = new HashMap<Integer,Integer>();
	private static final long serialVersionUID = 5790192283947925472L;
	private Integer cachedIndex;
	
	
	@Override
	public boolean add(Integer e){
		if(!get_countTerm().containsKey(e)){
			
			get_countTerm().put(e, 0);//initalize to zero
		}else{
			//increment count
			get_countTerm().put(e,get_countTerm().get(e)+1);
		}
		return super.add(e);
	}

	public Integer getCachedIndex() {
		return cachedIndex;
	}

	public void setCachedIndex(Integer cachedIndex) {
		this.cachedIndex = cachedIndex;
	}

	public void set_countTerm(HashMap<Integer,Integer> _countTerm) {
		this._countTerm = _countTerm;
	}

	public HashMap<Integer,Integer> get_countTerm() {
		return _countTerm;
	}
}
