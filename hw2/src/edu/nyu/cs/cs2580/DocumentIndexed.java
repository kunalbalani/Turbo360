package edu.nyu.cs.cs2580;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * @CS2580: implement this class for HW2 to incorporate any additional
 * information needed for your favorite ranker.
 */
public class DocumentIndexed extends Document {
	private static final long serialVersionUID = 9184892508124423115L;

	private Indexer _indexer = null;
	
	private Vector<Integer> _documentTokens = new Vector<Integer>();
	private Map<Integer, Integer> _termFrequency = new HashMap<Integer, Integer>();

	public <T extends Indexer> DocumentIndexed(int docid, T indexer) {
		super(docid);
		_indexer = indexer;
	}

	public void setDocumentTokens(Vector<Integer> documentTokens) {
		_documentTokens = documentTokens;
	}

	public Vector<Integer> getDocumentTokens() {
		return _documentTokens;
	}
	
	public int getTermFrequency(Integer term){
		return _termFrequency.get(term);
	}

//	public Vector<String> getConvertedDocumentTokens() {
//		return _indexer.getTermVector(_documentTokens);
//	}
	
	
}
