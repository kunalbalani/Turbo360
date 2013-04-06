package edu.nyu.cs.cs2580;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2.
 */
public class IndexerInvertedDoconly extends Indexer implements Serializable
{
	private static final long serialVersionUID = 1077111905740085030L;

	// Maps each term to their posting list
	private Map<Integer, Postings> _invertedIndex = new HashMap<Integer, Postings>();

	//Stores all Document in memory.
	private Vector<Document> _documents = new Vector<Document>();

	private Map<String, Integer> _docIds = new HashMap<String, Integer>();

	// Term frequency, key is the integer representation of the term and value is
	// the number of times the term appears in the corpus.
	private Map<Integer, Integer> _termCorpusFrequency =
		new HashMap<Integer, Integer>();

	// Maps each term to their integer representation
	Map<String, Integer> _dictionary = new HashMap<String, Integer>();

	// All unique terms appeared in corpus. Offsets are integer representations.
	Vector<String> _terms = new Vector<String>();

	// Term document frequency, key is the integer representation of the term and
	// value is the number of documents the term appears in.
	Map<Integer, Integer> _termDocFrequency = new HashMap<Integer, Integer>();

	public IndexerInvertedDoconly(Options options) {
		super(options);
		System.out.println("Using Indexer: " + this.getClass().getSimpleName());
	}

	/**
	 * Constructs the index from the corpus file.
	 * 
	 * @throws IOException
	 */
	@Override
	public void constructIndex() throws IOException {

		String corpus = _options._corpusPrefix;
		System.out.println("Construct index from: " + corpus);

		File corpusDirectory = new File(corpus);

		int fileCount = 0;
		if(corpusDirectory.isDirectory()){
			for(File corpusFile :corpusDirectory.listFiles()){
				processDocument(corpusFile);	
				fileCount++;
				System.out.println(fileCount +" out of 10264 Indexing complete");
				
			}
			System.out.println(fileCount+ " ->"+ corpusDirectory.listFiles().length);
		}

		System.out.println(
				"Indexed " + Integer.toString(_numDocs) + " docs with " +
				Long.toString(_totalTermFrequency) + " terms.");

		String indexFile = _options._indexPrefix + "/corpus.idx";
		System.out.println("Store index to: " + indexFile);

		ObjectOutputStream writer =
			new ObjectOutputStream(new FileOutputStream(indexFile));
		writer.writeObject(this);
		writer.close();
	}

	/**
	 * Process the raw content (i.e., one line in corpus.tsv) corresponding to a
	 * document, and constructs the token vectors for both title and body.
	 * @param content
	 * @throws FileNotFoundException 
	 */
	private void processDocument(File file) throws FileNotFoundException
	{

		DocumentProcessor documentProcessor = new DocumentProcessor();

		Vector<String> titleTokens_Str = documentProcessor .process(file.getName());
		Vector<String> bodyTokens_Str = documentProcessor.process(file);

		Vector<Integer> titleTokens = new Vector<Integer>();
		readTermVector(titleTokens_Str, titleTokens);

		Vector<Integer> bodyTokens = new Vector<Integer>();
		readTermVector(bodyTokens_Str, bodyTokens);

		//Document tokens
		Vector<Integer> documentTokens = bodyTokens;
		documentTokens.addAll(titleTokens);

		String title = file.getName();

		//no numViews for wiki docs
		int numViews = 0;
		Integer documentID = _documents.size();

		DocumentIndexed doc = new DocumentIndexed(documentID, this);
		doc.setTitle(title);
		doc.setNumViews(numViews);
		doc.setDocumentTokens(documentTokens);
		doc.setUrl(title);
		_documents.add(doc);
		_docIds.put(title, documentID);
		++_numDocs;

		Set<Integer> uniqueTerms = new HashSet<Integer>();
		updateStatistics(documentID, doc.getDocumentTokens(), uniqueTerms);

		for (Integer idx : uniqueTerms) {
			_termDocFrequency.put(idx, _termDocFrequency.get(idx) + 1);
		}


	}

	/**
	 * Tokenize {@code content} into terms, translate terms into their integer
	 * representation, store the integers in {@code tokens}.
	 * @param content
	 * @param tokens
	 */
	private void readTermVector(Vector<String> tokens_str, Vector<Integer> tokens) {
		try{
			for (String token : tokens_str) {
				int idx = -1;
				if (_dictionary.containsKey(token)) {
					idx = _dictionary.get(token);
				} else {
					idx = _terms.size();
					_terms.add(token);
					_dictionary.put(token, idx);
					_termCorpusFrequency.put(idx, 0);
					_termDocFrequency.put(idx, 0);
				}
				tokens.add(idx);
			}
		}catch (OutOfMemoryError oome){
			throw new OutOfMemoryError(oome.getLocalizedMessage());
		} 
		return;
	}


	/**
	 * Update the corpus statistics with {@code tokens}. Using {@code uniques} to
	 * bridge between different token vectors.
	 * @param tokens
	 * @param uniques
	 */
	private void updateStatistics(Integer documentID, Vector<Integer> tokens, Set<Integer> uniques) {

		try{
			for(int i=0; i<tokens.size(); i++){

				Integer idx = tokens.get(i);
				uniques.add(idx);

				//create and initialize the posting list
				if(!_invertedIndex.containsKey(idx)){
					_invertedIndex.put(idx,new Postings());
				}
				
				if(!_invertedIndex.get(idx).contains(documentID)){
					_invertedIndex.get(idx).add(documentID);
				}

				_termCorpusFrequency.put(idx, _termCorpusFrequency.get(idx) + 1);
				++_totalTermFrequency;
			}
		}catch (OutOfMemoryError oome){
			throw new OutOfMemoryError(oome.getLocalizedMessage());
		} 
	}

	///// Loading related functions.

	/**
	 * Loads the index from the index file.
	 * 
	 * @throws IOException, ClassNotFoundException
	 */
	@Override
	public void loadIndex() throws IOException, ClassNotFoundException {

		String indexFile = _options._indexPrefix + "/corpus.idx";
		System.out.println("Load index from: " + indexFile);

		ObjectInputStream reader =
			new ObjectInputStream(new FileInputStream(indexFile));
		IndexerInvertedDoconly loaded = (IndexerInvertedDoconly) reader.readObject();

		this._documents = loaded._documents;
		// Compute numDocs and totalTermFrequency b/c Indexer is not serializable.
		this._numDocs = _documents.size();
		for (Integer freq : loaded._termCorpusFrequency.values()) {
			this._totalTermFrequency += freq;
		}
		this._dictionary = loaded._dictionary;
		this._terms = loaded._terms;
		this._termCorpusFrequency = loaded._termCorpusFrequency;
		this._termDocFrequency = loaded._termDocFrequency;
		this._invertedIndex = loaded._invertedIndex;
		this._docIds = loaded._docIds;
		reader.close();

		System.out.println(Integer.toString(_numDocs) + " documents loaded " +
				"with " + Long.toString(_totalTermFrequency) + " terms!");
	}

	@Override
	public Document getDoc(int docid) {
		return (docid >= _documents.size() || docid < 0) ? null : _documents.get(docid);
	}

	/**
	 * In HW2, you should be using {@link DocumentIndexed}
	 */
	@Override
	public Document nextDoc(Query query, int docid) {

		Vector<String> queryTerms = query._tokens;
		
		if(docid<0){
			docid = 0;
		}

		//case 1 
		Vector <Integer> docIds = new Vector<Integer>();
		for(String token : queryTerms){
			Integer nextDocID = next(token,docid);
			if(nextDocID == Integer.MAX_VALUE){
				//value not found;
				return null;
			}
			docIds.add(nextDocID);
		}

		//case 2 
		boolean documentFound = true;

		for(int i = 0 ; i < docIds.size() -1 ; i++){
			if(docIds.get(i) != docIds.get(i+1)){
				documentFound = false;
				break;
			}
		}

		if(documentFound){
			Document doc = getDoc(docIds.get(0));
			return doc;
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

		Postings postingList = _invertedIndex.get(_dictionary.get(term));

		Integer lt = postingList.size();
		Integer ct = postingList.getCachedIndex();

		boolean isExit = postingList.get(lt-1) <= current;
		if(lt == 0 || isExit){
			return Integer.MAX_VALUE;
		}

		if(postingList.get(1) > current){
			postingList.setCachedIndex(1);
			return postingList.get(ct);
		}

		if(ct > 1 && postingList.get(ct-1) > current){
			postingList.setCachedIndex(1);
		}

		while(postingList.get(ct) <= current){
			ct = ct + 1;
		}
		return postingList.get(ct);
	}

	@Override
	public int corpusDocFrequencyByTerm(String term) {
		return _invertedIndex.get(_dictionary.get(term)).size();
	}

	@Override
	public int corpusTermFrequency(String term) {
		return _termCorpusFrequency.get(_dictionary.get(term));
	}

	@Override
	public int documentTermFrequency(String term, String url) {
		
		int returnValue = 0;
		int docID =  _docIds.get(url);
		returnValue = _invertedIndex.get(_dictionary.get(term)).get_countTerm().get(docID);
		return returnValue;
	}

	@Override
	public int nextPhrase(Query query, int docid, int position) {
		// TODO Auto-generated method stub
		return 0;
	}
}
