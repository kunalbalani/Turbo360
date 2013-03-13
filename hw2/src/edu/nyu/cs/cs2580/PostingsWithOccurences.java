package edu.nyu.cs.cs2580;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * This class represents a single entry in the {@link PostingsWithOccurences} 
 * posting list. It stores the document ID and the term offset within the document.
 * 
 * @author samitpatel
 * */
class PostingEntry implements Serializable {

	private static final long serialVersionUID = 3167577397839073790L;
	private Integer _docid;
	private Vector<Integer> _offset;

	@SuppressWarnings("unused")
	private PostingEntry(){}

	@Override
	public String toString() {
		return _docid+"->"+_offset;
	}

	/**
	 * Constructs the object with document ID and term offset within document.
	 * 
	 * @param docid Document ID
	 * @param offset Term offset within document
	 * */
	PostingEntry (Integer docid, Integer offset){
		_docid = docid;
		_offset = new Vector<Integer>();
		_offset.add(offset);
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
	public Vector<Integer> getOffset(){
		return _offset;
	}

	public void addOffset(Integer offset) {
		_offset.add(offset);
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
		try{
			PostingEntry lastDocument = this.lastElement();
			if(lastDocument.getDocID() == docid){
				lastDocument.addOffset(offset);
			}
		}catch (NoSuchElementException e) {
			PostingEntry entry = new PostingEntry(docid, offset);
			super.addElement(entry);
		}

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
	 * Searchs the documentID in the Vector
	 * */
	public PostingEntry searchDocumentID(Integer docID) {

		int documentID = Collections.binarySearch(this, new PostingEntry(docID, 0), new Comparator<PostingEntry>() {

			@Override
			public int compare(PostingEntry o1, PostingEntry o2) {
				return o1.getDocID().compareTo(o2.getDocID());
			}
		});

		return this.get(documentID);

	}

}
