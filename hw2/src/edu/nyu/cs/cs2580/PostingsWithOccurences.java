package edu.nyu.cs.cs2580;

import java.util.Vector;

/**
 * This class represents a single entry in the {@link PostingsWithOccurences} 
 * posting list. It stores the document ID and the term offset within the document.
 * 
 * @author samitpatel
 * */
class PostingEntry {
	
	private Integer _docid;
	private Integer _offset;
	
	@SuppressWarnings("unused")
	private PostingEntry(){}
	
	/**
	 * Constructs the object with document ID and term offset within document.
	 * 
	 * @param docid Document ID
	 * @param offset Term offset within document
	 * */
	PostingEntry (Integer docid, Integer offset){
		_docid = docid;
		_offset = offset;
	}
	
	/**
	 * Returns the Document ID.
	 * @return Document ID
	 * */
	public Integer getDocID(){
		return _docid;
	}
	
	/**
	 * Returns the offset of the term with the document.
	 * @return Term offset within the document
	 * */
	public Integer getOffset(){
		return _offset;
	}
}

/**
 * This class handles the Postings list for each term in the 
 * {@link IndexerInvertedOccurrence} indexer.
 * 
 * @author samitpatel
 * */
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
	
}
