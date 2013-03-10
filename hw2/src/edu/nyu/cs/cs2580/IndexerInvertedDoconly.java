package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2.
 */
public class IndexerInvertedDoconly extends Indexer 
{

	// Maps each term to their posting list
	private Map<String, Postings> _invertedIndex = new HashMap<String, Postings>();

	//Stores all Document in memory.
	private Vector<Document> _documents = new Vector<Document>();

	// Term frequency, key is the integer representation of the term and value is
	// the number of times the term appears in the corpus.
	private Map<Integer, Integer> _termCorpusFrequency =
			new HashMap<Integer, Integer>();

	public IndexerInvertedDoconly(Options options) 
	{
		super(options);
		System.out.println("Using Indexer: " + this.getClass().getSimpleName());
	}

	@Override
	public void constructIndex() throws IOException 
	{

	}

	@Override
	public void loadIndex() throws IOException, ClassNotFoundException
	{

	}

	@Override
	public Document getDoc(int docid) 
	{
		return (docid >= _documents.size() || docid < 0) ? null : _documents.get(docid);
	}

	/**
	 * In HW2, you should be using {@link DocumentIndexed}
	 */
	@Override
	public Document nextDoc(Query query, int docid) {

		Vector<String> queryTerms = query._tokens;


		//case 1 
		Vector <Integer> docIds = new Vector<Integer>();
		for(String token : queryTerms)
		{
			Integer nextDocID = next(token,docid);
			if(nextDocID == Integer.MAX_VALUE)
			{
				//value not found;
				return null;
			}
			docIds.add(nextDocID);
		}

		//case 2 
		boolean documentFound = true;

		for(int i = 0 ; i < docIds.size() -1 ; i++)
		{
			if(docIds.get(i) != docIds.get(i+1)){
				documentFound = false;
				break;
			}
		}

		if(documentFound)
		{
			return getDoc(docIds.get(0));
		}

		//case 3 
		Integer maxDocID = Collections.max(docIds);

		return nextDoc(query,maxDocID-1);
	}

	/**
	 * Finds the next document containing the term.
	 * If not found then it returns Integer.Maxvalue
	 * @param term
	 * @param docid 
	 * @return
	 */
	private int next (String term , int current){

		Postings postingList = _invertedIndex.get(term);

		Integer lt = postingList.size();
		Integer ct = postingList.getCachedIndex();

		if(lt == 0 || postingList.get(lt) <= current)
		{
			return Integer.MAX_VALUE;
		}

		if(postingList.get(1) > current)
		{
			postingList.setCachedIndex(1);
			return postingList.get(ct);
		}

		if(ct > 1 && postingList.get(ct-1) > current)
		{
			postingList.setCachedIndex(1);
		}

		while(postingList.get(ct) <= current)
		{
			ct = ct + 1;
		}

		return postingList.get(ct);
	}

	@Override
	public int corpusDocFrequencyByTerm(String term) {
		return _invertedIndex.get(term).size();
	}

	@Override
	public int corpusTermFrequency(String term) {
		return 0;
	}

	@Override
	public int documentTermFrequency(String term, String url) {
		SearchEngine.Check(false, "Not implemented!");
		return 0;
	}

	public static void main(String args[]){

	}
}
