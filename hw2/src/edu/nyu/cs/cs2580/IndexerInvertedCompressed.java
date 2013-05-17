package edu.nyu.cs.cs2580;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import edu.nyu.cs.cs2580.SearchEngine.Options;
import edu.nyu.cs.cs2580.FileManager.T3FileReader;
import edu.nyu.cs.cs2580.FileManager.T3FileWriter;

public class IndexerInvertedCompressed extends Indexer implements Serializable {

	private static final long serialVersionUID = 1L;

	// Maps terms to (docs to position offset Sums) 
	private Map<Integer, HashMap<Integer, Integer>> _sumOfOffsets
	= new HashMap<Integer, HashMap<Integer, Integer>>();

	// Maps each term to their posting list
	//	private Map<Integer, HashMap<Integer, Integer>> _docTermFrequencyInvertedIndex 
	//	= new HashMap<Integer, HashMap<Integer, Integer>>();

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
	private Map<Integer, Document> _documentsMap = new HashMap<Integer, Document>();



	private final Integer INFINITY = Integer.MAX_VALUE;
	private final String indexFolder = "invertedOccurenceCompressionIndex";
	private final String indexFileName = "invertedOccurenceCompressionIndex.idx";


	//used for merging indexes
	private static int fileId = 1;
	private static int docId = 1;
	private static int documentsCount = 0;
	private int mergeCount = 5000;
	private int fileCountPerFile = 100;
	private static Map<String,Scanner> scanners = new HashMap<String,Scanner>();
	private static Map<String,String> pointerToScanners = new HashMap<String, String>();
	private static int finalIndexCount = 1;

	private final String CORPUS = "wiki";

	// Maps each term to their posting list
	private Map<Integer, PostingsWithOccurences<String>>  _invertedIndexWithCompresion = 
			new TreeMap<Integer, PostingsWithOccurences<String>>();

	Map<Integer, PostingsWithOccurences<String>> _corpusPostingLists = 
			new HashMap<Integer, PostingsWithOccurences<String>>();

	public IndexerInvertedCompressed(Options options) {
		super(options);
		try{

			System.out.println("Using Indexer: " + this.getClass().getSimpleName());
			String idxFolder = _options._indexPrefix + "/"+indexFolder;
			File indexDirectory = new File(idxFolder);
			if(!indexDirectory.exists())
				indexDirectory.mkdir();

		}catch(Exception e){
			throw new RuntimeException(e);
		}
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



	public static Vector<Integer> decode(Vector<String> encoded) {
		Vector<Integer> out = new Vector<Integer>();
		for(String str : encoded) {
			out.add(decodeCorrected(str));
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



	private static int decodeCorrected(String hex) {
		int buffer = 0;
		int length = hex.length();
		if(hex.length() % 2 != 0)
			buffer  = 8 - hex.length() % 8;
		StringBuilder twoAligned = new StringBuilder();
		while(buffer != 0) {
			twoAligned.append("0");
			buffer--;
		}
		twoAligned.append(hex);
		StringBuilder outBinary = new StringBuilder();
		int index = 0;
		while(length != 0) {
			outBinary.append(hexToBinary(twoAligned.substring(index, index+2)));
			length -=2;
			index +=2;
			if(length == 0)
				break;
		}

		return Integer.parseInt(outBinary.toString(),2);
	}



	private static String hexToBinary(String hex) {
		String tempStr = "";
		//tempStr = temp;
		int i = Integer.parseInt(hex, 16);
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
		tempStr = eightAligned.toString().substring(1);
		return tempStr;
	}





	@Override
	public void constructIndex() throws IOException {

		try{
			createWikiIndex(new File(_options._corpusPrefix));

			System.out.println(
					"Indexed " + Integer.toString(_numDocs) + " docs with " +
							Long.toString(_totalTermFrequency) + " terms.");

			String indexFile = _options._indexPrefix + "/" + indexFileName;
			System.out.println("Store index to: " + indexFile);
			ObjectOutputStream writer =
					new ObjectOutputStream(new FileOutputStream(indexFile));
			writer.writeObject(this);
			writer.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}




	private void createWikiIndex(File corpusDirectory) throws FileNotFoundException {

		DocumentProcessor documentProcessor = new DocumentProcessor();

		int fileCount = 0;
		finalIndexCount = 1;
		System.out.println("Processing Documents");

		if(corpusDirectory.isDirectory()){
			for(File corpusFile :corpusDirectory.listFiles()){
				processDocument(corpusFile, documentProcessor);	
				fileCount++;

				if(fileCount > 0 && fileCount % fileCountPerFile == 0){
					saveIndexInFile();
				}
			}
		}
		//save the remaining data
		saveIndexInFile();
		mergeFile();
	}

	private void processDocument(File file, DocumentProcessor documentProcessor) throws FileNotFoundException {

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

		int documentID = documentsCount++;
		int numView = 0;
		DocumentIndexed doc = new DocumentIndexed(documentID, null);
		doc.setTitle(title);
		doc.setNumViews(numView);
		doc.setDocumentTokens(documentTokens);
		doc.setUrl(title);
		_documents.add(doc);
		_docIds.put(title, documentID);
		++_numDocs;

		Set<Integer> uniqueTerms = new HashSet<Integer>();
		updateStatistics(documentID, doc.getDocumentTokens(), uniqueTerms);

		for (int idx : uniqueTerms) {
			_termDocFrequency.put(idx, _termDocFrequency.get(idx) + 1);
		}
	}


	private void mergeFile() {

		try{
			String tempIndex = _options._indexPrefix+"/"+indexFolder+"/temp/"+CORPUS+"/";
			//Final index file
			String finalIndex = _options._indexPrefix+"/"+indexFolder+"/"+CORPUS+"/"+(finalIndexCount++)+".idx";
			T3FileWriter indexWriter = new T3FileWriter(finalIndex);

			File indexDirectory = new File(tempIndex);

			if(indexDirectory.isDirectory()) {

				File[] files = indexDirectory.listFiles();

				Comparator<File> comp = new Comparator<File>() {
					public int compare(File f1, File f2) {

						// Alphabetic order otherwise
						Integer fileIndex1 = Integer.parseInt(f1.getName().replaceFirst(".idx",""));
						Integer fileIndex2 = Integer.parseInt(f2.getName().replaceFirst(".idx",""));
						return fileIndex1.compareTo(fileIndex2);
					}
				};
				Arrays.sort(files, comp); 

				indexWriter.write("{");
				for(int i = 0 ; i < _dictionary.size();i++){

					//get posting list of term_id i from all the files and merge them
					StringBuilder mergedPostingListStr = new StringBuilder();

					for(int f=0; f<files.length; f++) {
						File indexTempFile = files[f];

						if(scanners.get(indexTempFile.getName()) == null) {
							try {
								Scanner scanner = new Scanner(indexTempFile);
								scanner.useDelimiter("],");
								scanners.put(indexTempFile.getName(),scanner);
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
						}



						String postingList = getPostingList(indexTempFile , i);

						//sample posting list = [0:81 82 02b2 03f8 0185 02df, 2:81 0290 cc]
						if(postingList != null){
							try{

								postingList = postingList.replaceAll("\\]", "");
								postingList = postingList.replaceAll("\\[", "");

								if(mergedPostingListStr.length() > 0)
									mergedPostingListStr.append(", ");

								mergedPostingListStr.append(postingList);

							}catch(Exception e){
								e.printStackTrace();
							}
						}
					}


					//Write the merger list to file i
					if(i%mergeCount == 0 && i>0 ){
						indexWriter.write("}");
						indexWriter.close();
						indexWriter = new T3FileWriter(_options._indexPrefix+"/"+indexFolder+"/"+CORPUS+"/"+(finalIndexCount++)+".idx");
						indexWriter.write("{");
					}

					String entry = "\""+i+"\""+":["+mergedPostingListStr+"]";
					indexWriter.write(entry);
					if((i+1)%mergeCount != 0 && i != _dictionary.size()-1){
						indexWriter.write(",");
					}

					if(i == _dictionary.size()-1){
						indexWriter.write("}");
						indexWriter.close();
					}
				}
			}

			indexWriter.close();
			//clean temp files 
			deleteTempFiles();

		}catch(Exception e){
			e.printStackTrace();
		}
	}


	private void deleteTempFiles() {
		File directory = new File(_options._indexPrefix+"/"+indexFolder+"/temp/"+CORPUS);
		try{
			delete(directory);
		}catch(IOException e){
			e.printStackTrace();
		}
	}


	public static void delete(File file) throws IOException{

		if(file.isDirectory()){

			//directory is empty, then delete it
			if(file.list().length==0){

				file.delete();
				System.out.println("Directory is deleted : " 
						+ file.getAbsolutePath());

			}else{

				//list all the directory contents
				String files[] = file.list();

				for (String temp : files) {
					//construct the file structure
					File fileDelete = new File(file, temp);

					//recursive delete
					delete(fileDelete);
				}

				//check the directory again, if empty then delete it
				if(file.list().length==0){
					file.delete();
					System.out.println("Directory is deleted : " 
							+ file.getAbsolutePath());
				}
			}

		}else{
			//if file, then delete it
			file.delete();
		}
	}


	public List<Integer> asList(int[] ints) {
		List<Integer> intList = new ArrayList<Integer>();
		for (int index = 0; index < ints.length; index++)
		{
			intList.add(ints[index]);
		}
		return intList;
	}


	private String getPostingList(File indexTempFile, int term_id) {
		Scanner scanner = scanners.get(indexTempFile.getName());

		while(scanner.hasNext()){

			String nextElement;
			if(pointerToScanners.get(indexTempFile.getName()) == null){
				nextElement = scanner.next();
				if(nextElement.startsWith(".")) continue;
			}else{
				nextElement = pointerToScanners.get(indexTempFile.getName());
				if(nextElement.startsWith(".")) continue;
			}

			String currentTerm_id;

			if(nextElement.startsWith("{")){
				nextElement = nextElement.substring(nextElement.indexOf("{"));
				currentTerm_id = nextElement.substring(nextElement.indexOf("{")+1,nextElement.indexOf("=", 1));
			}else{
				currentTerm_id = nextElement.substring(0, nextElement.indexOf("="));
				currentTerm_id = currentTerm_id.trim();
			}

			int currentTermID_int = Integer.parseInt(currentTerm_id);


			if(term_id == currentTermID_int){
				pointerToScanners.remove(indexTempFile.getName());
				return nextElement.substring(nextElement.indexOf("=")+1);
			}

			if(currentTermID_int > term_id){
				pointerToScanners.put(indexTempFile.getName(), nextElement);
				break;
			}else{
				System.out.println("If this line is showing up then something went terribly wrong....");
			}
		}

		return null;
	}


	private void saveIndexInFile() {

		try{
			String tempIndex = _options._indexPrefix+"/"+indexFolder+"/temp/"+CORPUS+"/";

			System.out.println("Saving file "+fileId);

			T3FileWriter fileWriter= new T3FileWriter(tempIndex+(fileId++)+".idx");
			
			String json = _invertedIndexWithCompresion.toString();

			fileWriter.write(json);
			fileWriter.close();

			fileWriter= new T3FileWriter(_options._indexPrefix+"/Documents/"+CORPUS+"/"+(docId++)+".idx");
			Gson gson = new Gson();
			json = gson.toJson(_documents);
			fileWriter.write(json);
			fileWriter.close();

			clearMem();
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	private void clearMem() {
		_invertedIndexWithCompresion.clear();
		_documents.clear();
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
	private void updateStatistics(int documentID, Vector<Integer> tokens, Set<Integer> uniques) {

		for(int i=0; i<tokens.size(); i++){

			int idx = tokens.get(i);
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
			//			_sumOfOffsets.get(idx).put(documentID, tempSum+i+1);
			_sumOfOffsets.get(idx).put(documentID, i+1);
			_termCorpusFrequency.put(idx, _termCorpusFrequency.get(idx) + 1);
			++_totalTermFrequency;

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
	public Document getDoc(int docid) {
	
		if(_documentsMap.containsKey(docid))
			return _documentsMap.get(docid);

		//if not then retrieve from backend store
		_documentsMap = getDocuments(docid);

		return _documentsMap.get(docid);
	}

	private Map<Integer, Document> getDocuments(int docid) {

		int file_no = (int)(docid/fileCountPerFile) + 1;
		String filepath = _options._indexPrefix+"/Documents/"+CORPUS+"/"+file_no+".idx";
		T3FileReader fileReader = new T3FileReader(filepath);
		String fileContents = fileReader.readAllBytes();
		fileReader.close();

		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		JsonArray array = parser.parse(fileContents).getAsJsonArray();

		Type type = new TypeToken<DocumentIndexed>(){}.getType();

		Map<Integer, Document> retVal = new HashMap<Integer, Document>();
		try{
			for(int i=0; i<array.size(); i++){
				DocumentIndexed doc = gson.fromJson(array.get(i), type);
				retVal.put(doc._docid, doc);
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return retVal;
	}


	/**
	 * In HW2, you should be using {@link DocumentIndexed}.
	 */
	@Override
	public Document nextDoc(Query query, int docid) {
	
		Vector<String> queryTerms = query._tokens;

		//case 1 
		Vector<Integer> docIds = new Vector<Integer>();
		for(String token : queryTerms){
			int nextDocID = INFINITY;

			if(token.indexOf(" ") == -1){ // normal query
				nextDocID = next(token, docid);
			}else{ //phrase query
				Query phraseToken = new Query(token);
				phraseToken.processQuery();
				Document nextPhraseDoc = null;

				int currentPhraseDocID = docid;
				while((nextPhraseDoc = nextDoc(phraseToken, currentPhraseDocID)) != null){
					int nextPhrase = nextPhrase(phraseToken, nextPhraseDoc._docid, -1);
					if(nextPhrase != INFINITY){
						nextDocID = nextPhraseDoc._docid;
						break;
					}
					currentPhraseDocID = nextPhraseDoc._docid;
				}



			}

			if(nextDocID == INFINITY){
				//value not found;
				return null;
			}

			docIds.add(nextDocID);
		}

		//case 2 
		boolean documentFound = true;
		for(int i = 0; i < docIds.size(); i++){
			if(!docIds.get(i).equals(docIds.get(0))){
				documentFound = false;
				break;
			}
		}

		if(documentFound){
			if(docIds.size() == 0) return null;
			Document doc = getDoc(docIds.get(0));
			return doc;
		}

		//case 3 
		int maxDocID = Collections.max(docIds);
		int nextDocID = maxDocID-1;
		if(nextDocID == docid) nextDocID++;
		return nextDoc(query, nextDocID);
	}


	/**
	 * Finds the next document containing the term.
	 * If not found then it returns Integer.Maxvalue
	 * @param term
	 * @param docid 
	 * @return
	 */
	private int next(String term , int currentDoc) {

		PostingsWithOccurences<String> postingList = null;
		Integer termID = _dictionary.get(term);

		if(termID == null) 
			return INFINITY;

		if(_corpusPostingLists.containsKey(termID)){
			postingList = _corpusPostingLists.get(termID);
		} else if(_invertedIndexWithCompresion != null && _dictionary != null && termID != null){
			postingList = _invertedIndexWithCompresion.get(termID);
			_corpusPostingLists.put(termID, postingList);
		}

		if(postingList == null){
			_invertedIndexWithCompresion = getIndex(termID);
			postingList = _invertedIndexWithCompresion.get(termID);
			_corpusPostingLists.put(termID, postingList);
		}

		if(postingList == null || postingList.size() == 0)
			return INFINITY;

		int lt = postingList.size();

		Integer ct = postingList.getCachedIndex();
		if(lt == 0)
			return INFINITY;

		if(ct == null){
			ct = 0;
			postingList.setCachedIndex(ct);
		}

		boolean isExit = postingList.get(lt-1).getDocID() <= currentDoc;
		if(lt == 0 || isExit){
			return INFINITY;
		}

		if(postingList.get(0).getDocID() > currentDoc){
			postingList.setCachedIndex(0);
			return postingList.get(0).getDocID();
		}

		if(ct > 0 && postingList.get(ct-1).getDocID() > currentDoc){
			ct = 0;
			postingList.setCachedIndex(0);
		}

		while(postingList.get(ct).getDocID() <= currentDoc){
			ct = ct + 1;
		}

		postingList.setCachedIndex(ct);
		return postingList.get(ct).getDocID();
//		int nextDoc = postingList.get(ct).getDocID();
//		if(nextDoc == currentDoc)
//			nextDoc++;
//		
//		return nextDoc;
	}

	private Map<Integer, PostingsWithOccurences<String>> getIndex(int term) {

		Map<Integer, PostingsWithOccurences<String>> invertedIndex = 
				new HashMap<Integer, PostingsWithOccurences<String>>();

		try{
			int file_no = (int)(term/mergeCount) + 1;
			String filepath = _options._indexPrefix+"/"+indexFolder+"/"+CORPUS+"/"+file_no+".idx";
			T3FileReader fileReader = new T3FileReader(filepath);
			String fileContents = fileReader.readAllBytes();
			fileReader.close();

			//divide string into terms
			String[] terms = fileContents.split("\\],");

			for(int i=0; i<terms.length; i++){
				String termList = terms[i];
				//extract termID
				int termID = Integer.parseInt(termList.substring(termList.indexOf("\"")+1, termList.lastIndexOf("\"")));
				//extract postinglist
				String postingListStr = termList.substring(termList.indexOf("[")+1);
				//divide termList into document with its occurences
				String[] docWithOccurences = postingListStr.split(", ");

				PostingsWithOccurences<String> postingList;

				if(invertedIndex.containsKey(termID))
					postingList = invertedIndex.get(termID);
				else
					postingList = new PostingsWithOccurences<String>();

				//for each docid and occurence update the postinglist
				for(int j=0; j<docWithOccurences.length; j++){
					String docWithOccurenceStr = docWithOccurences[j];
					String[] docWithOccurenceArray = docWithOccurenceStr.split(":");
					String docIDStr = docWithOccurenceArray[0];

					if(docIDStr.isEmpty()) continue;
					int docID = Integer.parseInt(docIDStr);
					String[] occurences = docWithOccurenceArray[1].split("\\s+");

					for(int k=0; k<occurences.length; k++)
						postingList.addEntry(docID, occurences[k]);

				}

				invertedIndex.put(termID, postingList);
			}

		}catch(Exception e){
			e.printStackTrace();
		}

		return invertedIndex;
	}


	/**
	 *Finds the next Phrase.
	 */
	@Override
	public int nextPhrase(Query query, int docid, int position) {

		Document document_verfiy = nextDoc(query, docid-1);

		if(document_verfiy == null || document_verfiy._docid != docid)
			return INFINITY;


		Vector<String> queryTerms = query._tokens;

		//case 1 
		Vector <Integer> positions = new Vector<Integer>();
		for(String token : queryTerms) {
			int nextPosition = nextPosition(token, docid, position);
			if(nextPosition == INFINITY) {
				//value not found;
				return INFINITY;
			}
			positions.add(nextPosition);
		}

		//case 2 
		boolean documentFound = true;
		for(int i = 0 ; i < positions.size()-1 ; i++) {
			if(positions.get(i)+1 != positions.get(i+1).intValue()){
				documentFound = false;
				break;
			}
		}

		if(documentFound) {
			if(positions.size() == 0) return INFINITY;
			return positions.get(0);
		}

		//case 3 
		int newPosition = Collections.max(positions)-queryTerms.size()+1;

		//avoid infinite loops
		if(newPosition == position)
			newPosition++;

		return nextPhrase(query, docid, newPosition);
	}


	/**
	 * Finds the next position of the term in document.
	 * If not found then it returns Integer.MAXVALUE
	 * @param term
	 * @param docid 
	 * @return
	 */
	private int nextPosition(String term ,int docId, int pos) {

		if(!_dictionary.containsKey(term))
			return INFINITY;

		PostingsWithOccurences<String> postingList = null;
		int termID = _dictionary.get(term);

		if(_corpusPostingLists.containsKey(termID)){
			postingList = _corpusPostingLists.get(termID);
		} else if(_invertedIndexWithCompresion != null && _dictionary != null){
			postingList = _invertedIndexWithCompresion.get(termID);
			_corpusPostingLists.put(termID, postingList);
		}

		if(postingList == null){
			_invertedIndexWithCompresion = getIndex(termID);
			postingList = _invertedIndexWithCompresion.get(termID);
			_corpusPostingLists.put(termID, postingList);
		}

		PostingEntry<String> documentEntry = postingList.searchDocumentID(docId);

		if(documentEntry != null && documentEntry.getDocID() == docId){
			Vector<String> strOffsets = documentEntry.getOffset();
			Vector<Integer> offsets = decode(strOffsets);
			if(offsets.size() == 0) return INFINITY;
			if(pos == -1) return offsets.get(0);

			//binary search for the index or the index where the pos would 
			//be and start searching form there.
			int binarySearchIndex = Collections.binarySearch(offsets, pos);
			if(binarySearchIndex < 0)
				binarySearchIndex = -binarySearchIndex - 1;

			if(binarySearchIndex == offsets.size()) return INFINITY;

			for(int i=binarySearchIndex; i<offsets.size(); i++){
				if(offsets.get(i).intValue() >= pos){
					return offsets.get(i);
				}
			}
		}

		return INFINITY;
	}

	@Override
	public int corpusDocFrequencyByTerm(String term) {

		PostingsWithOccurences<String> p = _invertedIndexWithCompresion.get(_dictionary.get(term));
		if(p == null){
			_invertedIndexWithCompresion = getIndex(_dictionary.get(term));
		}
		return _invertedIndexWithCompresion.get(_dictionary.get(term)).size();
	}

	@Override
	public int documentTermFrequency(String term, String url) {
		if(!_dictionary.containsKey(term))
			return 0;

		int term_idx = _dictionary.get(term);
		Integer docID = _docIds.get(url);
		if(docID == null) return 0;


		PostingsWithOccurences<String> list = 
				_invertedIndexWithCompresion.get(term_idx);

		if(list == null){
			_invertedIndexWithCompresion = getIndex(_dictionary.get(term));
		}

		PostingEntry<String> entry = list.searchDocumentID(docID);
		return entry.getOffset().size();
	}


	@Override
	public int corpusTermFrequency(String term) {
		return _termCorpusFrequency.get(_dictionary.get(term));
	}

	public String getTerm(int termId) {
		return _terms.get(termId);
	}

	public int getTermID(String term) {
		return _dictionary.get(term);
	}

}
