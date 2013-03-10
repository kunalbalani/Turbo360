package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import edu.nyu.cs.cs2580.SearchEngine.Options;
import edu.nyu.cs.cs2580.documentProcessor.DocumentProcessor;

/**
 * @CS2580: Implement this class for HW2.
 */
public class IndexerInvertedOccurrence extends Indexer {

	// Maps each term to their integer representation
	  private Map<String, Integer> _dictionary = new HashMap<String, Integer>();
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
	  private Vector<Document> _documents = new Vector<Document>();

	public IndexerInvertedOccurrence(Options options) {
		super(options);
		System.out.println("Using Indexer: " + this.getClass().getSimpleName());
	}

	@Override
	public void constructIndex() throws IOException {
		
		DocumentProcessor documentProcessor = new DocumentProcessor();
		
		File contentFolder = new File("data/wiki");
		
		for(File file : contentFolder.listFiles()){
			Vector<String> titleTokens = documentProcessor.process(file.getName());
			Vector<String> bodyTokens = documentProcessor.process(new FileReader(file));
			
			processDocument(titleTokens, bodyTokens);
		}
		
//		String corpusFile = _options._corpusPrefix + "/corpus.tsv";
//		System.out.println("Construct index from: " + corpusFile);

//		BufferedReader reader = new BufferedReader(new FileReader(corpusFile));
//		try {
//			String line = null;
//			while ((line = reader.readLine()) != null) {
//				processDocument(line);
//			}
//		} finally {
//			reader.close();
//		}
		
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
	 */
	private void processDocument(Vector<String> titleTokens_Str, Vector<String> bodyTokens_Str) {
		
		Vector<Integer> titleTokens = new Vector<Integer>();
		readTermVector(titleTokens_Str, titleTokens);

		Vector<Integer> bodyTokens = new Vector<Integer>();
		readTermVector(bodyTokens_Str, bodyTokens);

		//no numViews for wiki docs
		int numViews = 0;

		DocumentFull doc = new DocumentFull(_documents.size(), this);
		doc.setTitle(title);
		doc.setNumViews(numViews);
		doc.setTitleTokens(titleTokens);
		doc.setBodyTokens(bodyTokens);
		_documents.add(doc);
		++_numDocs;

		Set<Integer> uniqueTerms = new HashSet<Integer>();
		updateStatistics(doc.getTitleTokens(), uniqueTerms);
		updateStatistics(doc.getBodyTokens(), uniqueTerms);
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

	@Override
	public void loadIndex() throws IOException, ClassNotFoundException {
	}

	@Override
	public Document getDoc(int docid) {
		return (docid >= _documents.size() || docid < 0) ? null : _documents.get(docid);
	}

	/**
	 * In HW2, you should be using {@link DocumentIndexed}.
	 */
	@Override
	public Document nextDoc(Query query, int docid) {
		return null;
	}

	@Override
	public int corpusDocFrequencyByTerm(String term) {
		return 0;
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
}
