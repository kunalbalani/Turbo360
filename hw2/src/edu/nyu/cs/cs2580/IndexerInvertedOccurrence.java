package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
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
 * This class extends {@link Indexer} and indexes terms with their occurrences 
 * (i.e. offsets) in the documents.
 * 
 * @author samitpatel
 */
public class IndexerInvertedOccurrence extends Indexer implements Serializable 
{
	private static final long serialVersionUID = 5882841104467811379L;

	// Maps each term to their integer representation
	private Map<String, Integer> _dictionary = new HashMap<String, Integer>();

	// Maps each url to its docid
	private Map<String, Integer> _docIds = new HashMap<String, Integer>();

	// All unique terms appeared in corpus. Offsets are integer representations.
	private Vector<String> _terms = new Vector<String>();

	// Term document frequency, key is the integer representation of the term and
	// value is the number of documents the term appears in.
	private Map<Integer, Integer> _termDocFrequency = new HashMap<Integer, Integer>();

	// Term frequency, key is the integer representation of the term and value is
	// the number of times the term appears in the corpus.
	private Map<Integer, Integer> _termCorpusFrequency = new HashMap<Integer, Integer>();

	// Stores all Document in memory.
	private Vector<DocumentIndexed> _documents = new Vector<DocumentIndexed>();


	private final Integer INFINITY = Integer.MAX_VALUE;
	private final Integer documentBlock = 1000;

	private final String contentFolderName = "data/wiki";
	private final String indexFolderName = "invertedOccurenceIndex";
	private final String indexTempFolderName = 
		_options._indexPrefix + "/" + indexFolderName + "/temp";

	private final String indexFileName = "invertedOccurenceIndex.idx";

	// Maps each term to their posting list
	private IndexWrapper _invertedIndexWithOccurences = 
		new IndexWrapper(_options._indexPrefix + "/"+indexFolderName);

	public IndexerInvertedOccurrence(Options options) {
		super(options);
		System.out.println("Using Indexer: " + this.getClass().getSimpleName());
		String indexFolder = _options._indexPrefix + "/"+indexFolderName;
		File indexDirectory = new File(indexFolder);
		if(!indexDirectory.exists())
			indexDirectory.mkdir();
	}

	public IndexerInvertedOccurrence() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void constructIndex() throws IOException
	{

		DocumentProcessor documentProcessor = new DocumentProcessor();

		File contentFolder = new File(contentFolderName);
		int fileCount = 0;

		for(File file : contentFolder.listFiles()){

			processDocument(file, documentProcessor);
			fileCount++;

			if(fileCount == documentBlock)
			{
				_invertedIndexWithOccurences.writeToDisk();
				Runtime.getRuntime().gc();
				fileCount=0;
			}
		}

		_invertedIndexWithOccurences.writeToDisk();

		//Merges all the temp index.
//		mergeIndexes();
		Merge.merge(_options._indexPrefix + "/" + indexFolderName, _terms.size()/10);

		System.out.println(
				"Indexed " + Integer.toString(_numDocs) + " docs with " +
				Long.toString(_totalTermFrequency) + " terms.");

		String indexFile = _options._indexPrefix + "/"+indexFolderName+"/"+indexFileName;
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
	 * @throws IOException 
	 */
	private void processDocument(File file, DocumentProcessor documentProcessor) throws IOException {
		try{

			Vector<String> titleTokens_Str = documentProcessor.process(file.getName());
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
			_documents.add(doc);
			_docIds.put(title, documentID);
			++_numDocs;

			Set<Integer> uniqueTerms = new HashSet<Integer>();
			updateStatistics(documentID, doc.getDocumentTokens(), uniqueTerms);

			for (Integer idx : uniqueTerms) {
				_termDocFrequency.put(idx, _termDocFrequency.get(idx) + 1);
			}


		} catch(FileNotFoundException fnfe){
			fnfe.printStackTrace();
		} catch (OutOfMemoryError oome){
			throw new OutOfMemoryError(oome.getLocalizedMessage());
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

				//populates the inverted index
				if(!_invertedIndexWithOccurences.containsKey(idx)){
					_invertedIndexWithOccurences.put(idx, new PostingsWithOccurences<Integer>());
				}
				_invertedIndexWithOccurences.get(idx).addEntry(documentID, i+1); //offset start from 1

				_termCorpusFrequency.put(idx, _termCorpusFrequency.get(idx) + 1);
				++_totalTermFrequency;
			}
		}catch (OutOfMemoryError oome){
			throw new OutOfMemoryError(oome.getLocalizedMessage());
		} 
	}


	@Override
	public void loadIndex() throws IOException, ClassNotFoundException {

		String indexFile = _options._indexPrefix + "/"+indexFolderName+"/"+indexFileName;
		System.out.println("Load index from: " + indexFile);

		ObjectInputStream reader =
			new ObjectInputStream(new FileInputStream(indexFile));
		IndexerInvertedOccurrence loaded = (IndexerInvertedOccurrence) reader.readObject();

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
			Integer nextDocID = next(token, docid);
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

		PostingsWithOccurences<Integer> postingList = 
				_invertedIndexWithOccurences.getPostingList(_dictionary.get(term));

		Integer lt = postingList.size();
		Integer ct = postingList.getCachedIndex();

		if(lt == 0 || postingList.get(lt-1).getDocID() <= current) {
			return INFINITY;
		}

		if(postingList.get(0).getDocID() > current) {
			postingList.setCachedIndex(0);
			return postingList.get(0).getDocID();
		}

		if(ct > 0 && postingList.get(ct-1).getDocID() > current) {
			ct = 0;
		}

		while(postingList.get(ct).getDocID() <= current) {
			ct++;
		}

		return postingList.get(ct).getDocID();
	}




	/**
	 *Finds the next Phrase.
	 */
	@Override
	public int nextPhrase(Query query, int docid, int position) {

		Document document_verfiy = nextDoc(query, docid-1);
		if(document_verfiy._docid != docid)
			return INFINITY;

		Vector<String> queryTerms = query._tokens;

		//case 1 
		Vector <Integer> positions = new Vector<Integer>();
		for(String token : queryTerms) {
			Integer nextPosition = nextPosition(token, docid, position);
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
		PostingsWithOccurences<Integer> postingList = 
				_invertedIndexWithOccurences.getPostingList(_dictionary.get(term));

		PostingEntry<Integer> documentEntry = postingList.searchDocumentID(docId);

		if(documentEntry != null && documentEntry.getDocID() == docId){
			Vector<Integer> offsets = documentEntry.getOffset();
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
		PostingsWithOccurences<Integer> list = 
				_invertedIndexWithOccurences.getPostingList(_dictionary.get(term));
		PostingEntry<Integer> entry = list.searchDocumentID(docID);

		return entry.getOffset().size();
	}



	//Utility
	private static void mergeIndexes()
	{	

		T3FileReader t3R;

		String indexTempFolderName = "data/index/invertedOccurenceIndex/temp";

		String indexFileName = "data/index/invertedOccurenceIndex/Index";
		T3IndexWriter indexWriter = new T3IndexWriter(indexFileName);

		File tempFolder = new File(indexTempFolderName);

		if(tempFolder.exists() && tempFolder.isDirectory())
		{


			for(int i = 0 ; i < 1000; i ++){

				File[] files = tempFolder.listFiles();

				T3FileReader firstFile = new T3FileReader(indexTempFolderName+"/"+files[0].getName());
				String bufferIndexLine = firstFile.read();

				for(int f = 1 ; f < files.length; f++)
				{
					File file = files[f];
					t3R = new T3FileReader(indexTempFolderName+"/"+file.getName());

					String entry  = t3R.read();

					if(entry != null)
					{
						boolean isPresentInIndex = bufferIndexLine.charAt(0) ==
							entry.charAt(0) ? true : false;

						if(isPresentInIndex){
							merge(bufferIndexLine,entry);
							removeEntry(indexTempFolderName+"/"+file.getName(),entry);
						}else{
							indexWriter.write(entry);
						}

					}

					indexWriter.write(bufferIndexLine);
				}
			}
		}
	}

	private static void removeEntry(String fileName,String entry) {
		String contents = readFileAsString(fileName);
		contents.replaceFirst(entry,"");
		contents.trim();
		T3FileWriter fileWriter = new T3FileWriter(fileName);
		fileWriter.write(contents);
		fileWriter.close();

	}

	private static String readFileAsString(String filePath)
	{
		try
		{

			StringBuffer fileData = new StringBuffer();
			BufferedReader reader = new BufferedReader(
					new FileReader(filePath));
			char[] buf = new char[1024];
			int numRead=0;
			while((numRead=reader.read(buf)) != -1)
			{
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
			}
			reader.close();
			return fileData.toString();
		}catch (IOException e){
			e.printStackTrace();
			return null;
		}
	}

	public static void merge(String entry,String entry2)
	{
		String delimiter = "\\n";

		for(String line : entry2.split(delimiter))
		{
			Integer lineTermID = T3Parser.parseTermInvertedIndex(line);
			Integer entryTermID = T3Parser.parseTermInvertedIndex(entry);

			if(lineTermID == entryTermID){

				PostingsWithOccurences<Integer> p1 = T3Parser.parsePostingInvertedIndex(line);
				PostingsWithOccurences<Integer> p2 = T3Parser.parsePostingInvertedIndex(entry);

				p2.addAll(p1);

				String newEntry = p2.formatString();
				newEntry.trim();

				entry.replaceFirst(line, newEntry);
			}
		}


	}

	public static void main(String args[]){
		IndexerInvertedOccurrence.mergeIndexes();
	}

}
