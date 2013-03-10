package edu.nyu.cs.cs2580;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

class PostingEntry {
	private Integer _docid;
	private Integer _offset;
	
	@SuppressWarnings("unused")
	private PostingEntry(){}
	
	PostingEntry (Integer docid, Integer offset){
		_docid = docid;
		_offset = offset;
	}
	
	public Integer getDocID(){
		return _docid;
	}
	
	public Integer getOffset(){
		return _offset;
	}
}

public class PostingsWithOccurences extends Vector<PostingEntry>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7865772446822434246L;
	private Integer cachedIndex;

	public PostingsWithOccurences() {
		super();
		cachedIndex = -1;
	}
	
	/**
	 * Adds an entry to Posting List
	 * */
	public void addEntry(Integer docid, Integer offset){
		PostingEntry entry = new PostingEntry(docid, offset);
		super.addElement(entry);
	}
	
	/**
	 * Returns the Cached Index
	 * */
	public Integer getCachedIndex() {
		return cachedIndex;
	}

	/**
	 * Sets the cachedIndex
	 * */
	public void setCachedIndex(Integer cachedIndex) {
		this.cachedIndex = cachedIndex;
	}
	
	
	/**
	 * Sorts the posting list
	 * */
	public void sortPostingList(){
		
		Comparator<PostingEntry> comparator = new Comparator<PostingEntry>() {

			@Override
			public int compare(PostingEntry o1, PostingEntry o2) {
				if(o1.getDocID() < o2.getDocID())
					return 1;
				else if (o1.getDocID() < o2.getDocID())
					return -1;
				else
					if(o1.getOffset() < o2.getOffset())
						return 1;
					else if(o1.getOffset() > o2.getOffset())
						return -1;
				
				return 0;
			}
			
		};
		
		Collections.sort(this, comparator);
	}
}
