package edu.nyu.cs.cs2580;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
public class IndexerInvertedCompressed extends Indexer {
	private static final long serialVersionUID = 5882841104467811379L; 
	
	// Maps each term to their posting list
		private Map<Integer, PostingsWithOccurences> _invertedIndexWithCompresion 
		= new HashMap<Integer, PostingsWithOccurences>();
		
		// Maps terms to (docs to position offsets) -----------------------------------------
//		private Map<Integer, HashMap<Integer, Vector<String>>> _invertedIndexWithCompresion
//		= new HashMap<Integer, HashMap<Integer, Vector<String>>>();
		
		// Maps terms to (docs to position offset Sums) -----------------------------------------
		private Map<Integer, HashMap<Integer, Integer>> _sumOfOffsets
		= new HashMap<Integer, HashMap<Integer, Integer>>();

		// Maps each term to their posting list
		private Map<Integer, HashMap<Integer, Integer>> _docTermFrequencyInvertedIndex 
		= new HashMap<Integer, HashMap<Integer, Integer>>();

		// Maps each term to their integer representation
		private Map<String, Integer> _dictionary = new HashMap<String, Integer>();

		// Maps each url to its docid
		private Map<String, Integer> _docIds = new HashMap<String, Integer>();

		// All unique terms appeared in corpus. Offsets are integer representations.
		private Vector<String> _terms = new Vector<String>();

		// Term document frequency, key is the integer representation of the term and
		// value is the number of documents the term appears in.
		private Map<Integer, Integer> _termDocFrequency =
				new HashMap<Integer, Integer>();

		// Term frequency, key is the integer representation of the term and value is
		// the number of times the term appears in the corpus.
		private Map<Integer, Integer> _termCorpusFrequency =
				new HashMap<Integer, Integer>();

		// Stores all Document in memory.
		private Vector<DocumentIndexed> _documents = new Vector<DocumentIndexed>();


		private final Integer INFINITY = Integer.MAX_VALUE;
		private final String contentFolderName = "data/wiki";
		private final String indexFolderName = "invertedOccurenceompressionIndex";
		private final String indexTempFolderName = 
				_options._indexPrefix + "/" + indexFolderName + "/temp";
		private final String indexFileName = "invertedOccurenceIndex.idx";


  public IndexerInvertedCompressed(Options options) {
    super(options);
    System.out.println("Using Indexer: " + this.getClass().getSimpleName());
  }

	
	private static String hexOut(Integer inNo){
		String in = Integer.toBinaryString(inNo);
		int buffer = 0;
		if(in.length() % 7 != 0)
			buffer = 7 - in.length() % 7;
		StringBuilder sevenAligned = new StringBuilder();
		while(buffer != 0) {
			sevenAligned.append("0");
			buffer--;
		}
		sevenAligned.append(in);
		int length = sevenAligned.length();
		StringBuilder out = new StringBuilder();
		int index = 0;
		while(length != 0) {
			
			if(length-7 == 0) {
				String eightAligned = "1" + sevenAligned.substring(index,index+7);
				String temp = Integer.toHexString(Integer.parseInt(eightAligned,2));
				if(temp.length() == 1)
					out.append("0"+temp);
				else
					out.append(temp);
			}
			else {
				String eightAligned = "0" + sevenAligned.substring(index, index+7);
				String temp = Integer.toHexString(Integer.parseInt(eightAligned,2));
				if(temp.length() == 1)
					out.append("0"+temp);
				else
					out.append(temp);
			}
			index += 7;
			length -= 7;
		}
		return out.toString();
	}
	
	
	private static boolean getMore(String hexIn) {
		int i = Integer.parseInt(hexIn.substring(0,1), 16);
		String Bin = Integer.toBinaryString(i);
		if(Bin.charAt(0) == '1')
			return true;
		else
			return false;
	}
	
	
	
	private static int intOut(Vector<String> offsets) {
		String temp = new String();
		int length = offsets.size();
		
		String comp = offsets.get(length-1);
		String tempStr = new String();
		System.out.println(comp);
		do {
			tempStr = temp;
			int i = Integer.parseInt(comp, 16);
		    String Bin = Integer.toBinaryString(i);
		    int buffer = 0;
		    if(Bin.length() % 8 != 0)
		    	buffer  = 8 - Bin.length() % 8;
		    StringBuilder eightAligned = new StringBuilder();
		    while(buffer != 0) {
		    	eightAligned.append("0");
				buffer--;
			}
		    eightAligned.append(Bin);
			temp = eightAligned.toString().substring(1) + tempStr;
			System.out.println(temp);
			length--;
			if(length == 0)
				break;
			comp = offsets.get(length-1);
		} while(!getMore(comp)) ;
		
		System.out.println(temp);
		return Integer.parseInt(temp, 2);
	}
	
	private static Vector<Integer> decode(Vector<String> encoded) {
		Vector<String> temp = new Vector<String>();
		Vector<Integer> out = new Vector<Integer>();
		int outValue = 0;
		for(int i = 0; i < encoded.size(); i++) {
			temp.add(encoded.elementAt(i));
			if(!getMore(encoded.elementAt(i))) {
				continue;
			}
			else {
				outValue = intOut(temp);
				out.add(outValue);
				temp.removeAllElements();
			}
		}
		return deltaDecode(out);
	}
	
	private static Vector<Integer> deltaDecode(Vector<Integer> encoded) {
		Vector<Integer> out = new Vector<Integer>();
		out.add(encoded.elementAt(0));
		for(int i = 1; i < encoded.size(); i++) {
			out.add(i, encoded.elementAt(i)+out.elementAt(i-1));
		}
		return out;
	}
	
  
  
  @Override
  public void constructIndex() throws IOException {
	  DocumentProcessor documentProcessor = new DocumentProcessor();

		File contentFolder = new File(contentFolderName);

		for(File file : contentFolder.listFiles()){
			processDocument(file, documentProcessor);
		}


		System.out.println(
				"Indexed " + Integer.toString(_numDocs) + " docs with " +
						Long.toString(_totalTermFrequency) + " terms.");

		String indexFile = _options._indexPrefix + "/"+indexFileName;
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
	 */
	private void processDocument(File file, DocumentProcessor documentProcessor) {
		try{

			System.out.println(file.getName());

			Vector<String> titleTokens_Str = documentProcessor.process(file.getName());
			Vector<String> bodyTokens_Str = documentProcessor.process(new FileReader(file));

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
			_documents.add(doc);
			_docIds.put(title, documentID);
			++_numDocs;

			Set<Integer> uniqueTerms = new HashSet<Integer>();
			updateStatistics(documentID, doc.getDocumentTokens(), uniqueTerms);

			for (Integer idx : uniqueTerms) {
				_termDocFrequency.put(idx, _termDocFrequency.get(idx) + 1);
			}

		}catch(FileNotFoundException fnfe){
			fnfe.printStackTrace();
		}

	}

	/**
	 * Tokenize {@code content} into terms, translate terms into their integer
	 * representation, store the integers in {@code tokens}.
	 * @param content
	 * @param tokens
	 */
	private void readTermVector(Vector<String> tokens_str, Vector<Integer> tokens) {
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
		return;
	}


	/**
	 * Update the corpus statistics with {@code tokens}. Using {@code uniques} to
	 * bridge between different token vectors.
	 * @param tokens
	 * @param uniques
	 */
	@SuppressWarnings("unchecked")
	private void updateStatistics(Integer documentID, Vector<Integer> tokens, Set<Integer> uniques) {

		for(int i=0; i<tokens.size(); i++){

			Integer idx = tokens.get(i);
			uniques.add(idx);
			
			//populates the inverted index
			if(!_invertedIndexWithCompresion.containsKey(idx)){
				_invertedIndexWithCompresion.put(idx, new PostingsWithOccurences<String>());
				_sumOfOffsets.put(idx, new HashMap<Integer,Integer>());
			}
			if(!_sumOfOffsets.get(idx).containsKey(documentID))
				_sumOfOffsets.get(idx).put(documentID, 0);
			
			int tempSum = _sumOfOffsets.get(idx).get(documentID);
			String temp = hexOut(i+1-tempSum);
			_invertedIndexWithCompresion.get(idx).addEntry(documentID, temp); //offset start from 1
			
//			if(!_invertedIndexWithCompresion.containsKey(idx)) {
//				_invertedIndexWithCompresion.put(idx, new HashMap<Integer,Vector<String>>());
//				_sumOfOffsets.put(idx, new HashMap<Integer,Integer>());
//			}
//			if(!_invertedIndexWithCompresion.get(idx).containsKey(documentID)) {
//				_invertedIndexWithCompresion.get(idx).put(documentID, new Vector<String>());
//				_sumOfOffsets.get(idx).put(documentID, 0);
//			}
//			int tempSum = _sumOfOffsets.get(idx).get(documentID);
//			String temp = hexOut(i+1-tempSum);
//			_invertedIndexWithCompresion.get(idx).get(documentID).add(temp);
//			_sumOfOffsets.get(idx).put(documentID,tempSum+i+1);
			
			

			_termCorpusFrequency.put(idx, _termCorpusFrequency.get(idx) + 1);
			++_totalTermFrequency;

			//populating the docTermFrequency index
			if(!_docTermFrequencyInvertedIndex.containsKey(idx)){
				_docTermFrequencyInvertedIndex.put(idx, new HashMap<Integer, Integer>());
			}
			Map<Integer, Integer> termDocFrequencyList = _docTermFrequencyInvertedIndex.get(idx);

			if(!termDocFrequencyList.containsKey(documentID)){
				termDocFrequencyList.put(documentID, new Integer(1));
			}else{
				termDocFrequencyList.put(documentID, termDocFrequencyList.get(documentID)+1);
			}
		}
	}

  

	@Override
	public void loadIndex() throws IOException, ClassNotFoundException {
		String indexFile = _options._indexPrefix + "/"+indexFileName;
		System.out.println("Load index from: " + indexFile);

		ObjectInputStream reader =
				new ObjectInputStream(new FileInputStream(indexFile));
		IndexerInvertedCompressed loaded = (IndexerInvertedCompressed) reader.readObject();

		this._documents = loaded._documents;
		// Compute numDocs and totalTermFrequency b/c Indexer is not serializable.
		this._numDocs = _documents.size();
		for (Integer freq : loaded._termCorpusFrequency.values()) {
			this._totalTermFrequency += freq;
		}
		this._dictionary = loaded._dictionary;
		this._docIds = loaded._docIds;
		this._terms = loaded._terms;
		this._termCorpusFrequency = loaded._termCorpusFrequency;
		this._termDocFrequency = loaded._termDocFrequency;
		reader.close();

		System.out.println(Integer.toString(_numDocs) + " documents loaded " +
				"with " + Long.toString(_totalTermFrequency) + " terms!");
	}



	@Override
	public DocumentIndexed getDoc(int docid) {
		return (docid >= _documents.size() || docid < 0) ? null : _documents.get(docid);
	}



	/**
	 * In HW2, you should be using {@link DocumentIndexed}.
	 */
	@Override
	public DocumentIndexed nextDoc(Query query, int docid) {

		Vector<String> queryTerms = query._tokens;

		//case 1 
		Vector <Integer> docIds = new Vector<Integer>();
		for(String token : queryTerms) {
			Integer nextDocID = next(token,docid);
			if(nextDocID == INFINITY) {
				//value not found;
				return null;
			}
			docIds.add(nextDocID);
		}

		//case 2 
		boolean documentFound = true;

		for(int i = 0 ; i < docIds.size()-1 ; i++) {
			if(docIds.get(i) != docIds.get(i+1)){
				documentFound = false;
				break;
			}
		}

		if(documentFound) {
			return getDoc(docIds.get(0));
		}

		//case 3 
		Integer maxDocID = Collections.max(docIds);

		return nextDoc(query, maxDocID-1);
	}


	/**
	 * Finds the next document containing the term.
	 * If not found then it returns Integer.Maxvalue
	 * @param term
	 * @param docid 
	 * @return
	 */
	private int next(String term , int current) {

		PostingsWithOccurences postingList = _invertedIndexWithCompresion.get(term);

		Integer lt = postingList.size();
		Integer ct = postingList.getCachedIndex();

		if(lt == 0 || ((PostingEntry) postingList.get(lt-1)).getDocID() <= current) {
			return INFINITY;
		}

		if(((PostingEntry) postingList.get(0)).getDocID() > current) {
			postingList.setCachedIndex(0);
			return ((PostingEntry) postingList.get(0)).getDocID();
		}

		if(ct > 0 && ((PostingEntry) postingList.get(ct-1)).getDocID() > current) {
			ct = 0;
		}

		while(((PostingEntry) postingList.get(ct)).getDocID() <= current) {
			ct++;
		}

		return ((PostingEntry) postingList.get(ct)).getDocID();
	}




	/**
	 *Finds the nex Phrase.
	 */
	public int nextPhrase(Query query, int docid, int position) {

		Document document_verfiy = nextDoc(query, docid-1);
		if(document_verfiy._docid != docid)
			return INFINITY;

		Vector<String> queryTerms = query._tokens;

		//case 1 
		Vector <Integer> positions = new Vector<Integer>();
		for(String token : queryTerms) {
			Integer nextPosition = nextPosition(token,docid, position);
			if(nextPosition == INFINITY) {
				//value not found;
				return INFINITY;
			}
			positions.add(nextPosition);
		}

		//case 2 
		boolean documentFound = true;

		for(int i = 0 ; i < positions.size()-1 ; i++) {
			if(positions.get(i) + 1 != positions.get(i+1)){
				documentFound = false;
				break;
			}
		}

		if(documentFound) {
			return positions.get(0);
		}

		//case 3 
		return nextPhrase(query, docid, Collections.max(positions));
	}


	/**
	 * Finds the next position of the term in document.
	 * If not found then it returns Integer.MAXVALUE
	 * @param term
	 * @param docid 
	 * @return
	 */
	private int nextPosition(String term ,int docId, int pos) {
		PostingsWithOccurences postingList = _invertedIndexWithCompresion.get(term);

		PostingEntry documentEntry = postingList.searchDocumentID(docId);

		if(documentEntry != null && documentEntry.getDocID() == docId){
			Vector<String> strOffsets = documentEntry.getOffset();
			Vector<Integer> offsets = decode(strOffsets);
			for(int i=0; i<offsets.size()-1; i++){
				if(offsets.get(i) == pos){
					return offsets.get(i+1);
				}
			}
		}

		return INFINITY;
	}

	@Override
	public int corpusDocFrequencyByTerm(String term) {
		return _termDocFrequency.get(_dictionary.get(term));
	}

	@Override
	public int corpusTermFrequency(String term) {
		return _termCorpusFrequency.get(_dictionary.get(term));
	}

	@Override
	public int documentTermFrequency(String term, String url) {
		int term_idx = _dictionary.get(term);
		int docID = _docIds.get(url);
		//		if(!_docTermFrequencyInvertedIndex.containsKey(term_idx) || 
		//				!_docTermFrequencyInvertedIndex.get(term_idx).containsKey(docid))
		//			return 0;
		//
		//		return _docTermFrequencyInvertedIndex.get(term_idx).get(docid);
		PostingsWithOccurences<String> list = _invertedIndexWithCompresion.get(term_idx);
		PostingEntry entry = list.searchDocumentID(docID);

		return entry.getOffset().size();
	}



	//Utility
	private void mergeIndexes(){
		try{
			File tempFolder = new File(indexTempFolderName);
			if(tempFolder.exists() && tempFolder.isDirectory()){
				FileInputStream fileReader = new FileInputStream(indexTempFolderName+"/0");
				ObjectInputStream reader = new ObjectInputStream(fileReader);
				Integer termID = (Integer) reader.readObject();
				PostingsWithOccurences postingList = (PostingsWithOccurences) reader.readObject();

				System.out.println(termID);
				System.out.println(postingList.get(0));
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
  
}
