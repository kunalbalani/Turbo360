package edu.nyu.cs.cs2580;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Vector;

import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2 based on a refactoring of your favorite
 * Ranker (except RankerPhrase) from HW1. The new Ranker should no longer rely
 * on the instructors' {@link IndexerFullScan}, instead it should use one of
 * your more efficient implementations.
 */
public class RankerFavorite extends Ranker {

	public RankerFavorite(Options options,
			CgiArguments arguments, Indexer indexer) {
		super(options, arguments, indexer);
		System.out.println("Using Ranker: " + this.getClass().getSimpleName());
	}

	@Override
	public Vector<ScoredDocument> runQuery(Query query, int numResults) {
		
		Queue<ScoredDocument> rankQueue = new PriorityQueue<ScoredDocument>();
		Document doc = null;
		int docid = -1;
		
		while ((doc = _indexer.nextDoc(query, docid)) != null) {
			//Scoring the document
			rankQueue.add(new ScoredDocument(doc, this.getScore(query, doc)));
			if (rankQueue.size() > numResults) {
				rankQueue.poll();
			}
			docid = doc._docid;
		}

		Vector<ScoredDocument> results = new Vector<ScoredDocument>();
		ScoredDocument scoredDoc = null;
		while ((scoredDoc = rankQueue.poll()) != null) {
			results.add(scoredDoc);
		}
		Collections.sort(results, Collections.reverseOrder());
		return results;
	}


	/**
	 * Calculates the Cosine Similarity Score
	 * */
	private Double getScore(Query query, Document d) {

		Vector < String > dv =  new Vector<String>();

		String title = d.getTitle();

		Scanner s = new Scanner(title).useDelimiter("\t");
		while(s.hasNext()){
			dv.add(s.next());
		}

		Vector < String > qv =  query._tokens;

		Map<String, Integer> documentTermFrequency = getTermFrequency(dv);
		Map<String, Integer> queryTermFrequency = getTermFrequency(qv);

		//Document vector that stores all tf.idfs for the documents
		Vector<Double> documentVector = new Vector<Double>();
		Vector<Double> queryVector = new Vector<Double>();


		double xi2 = 0.0; //Stores the sum of xi^2
		double yi2 = 0.0; //Stores the sum of yi^2
		double score = 0.0; 


		for (int i = 0; i < dv.size(); ++i){
			String documentTerm = dv.get(i);

			//Calculating the tf.idfs Document vectors
			double tf = (double) documentTermFrequency.get(documentTerm);
			double idf = getIDF(documentTerm);
			double xi = tf*idf;

			documentVector.add(xi);

			//This generates the query vector by checking if the current 
			//document term is present in query. If its present it calculates 
			//tf.idf for the query term
			double yi = 0.0;
			if(qv.contains(documentTerm)){	
				String queryTerm = documentTerm;
				//Calculating the query vector
				double queryTerm_tf = (double) queryTermFrequency.get(queryTerm);
				double queryTerm_idf = getIDF(queryTerm);
				yi = queryTerm_tf * queryTerm_idf;	
			}

			queryVector.add(yi);

			xi2 += Math.pow(xi,2);
			yi2 += Math.pow(yi,2);

		}

		double xi_norm = Math.sqrt(xi2);
		double yi_norm = Math.sqrt(yi2);

		Vector<Double> documentVector_normalized = new Vector<Double>();
		Vector<Double> queryVector_normalized = new Vector<Double>();

		//Calculating L2-Normalized tf.tdf vectors 
		for(int i=0; i<documentVector.size();i++){
			documentVector_normalized.add(documentVector.get(i)/xi_norm);
			queryVector_normalized.add(queryVector.get(i)/yi_norm);
		}


		/*
		 * Cosine Similarity 
		 * Sum(Xi*Yi)/(Sqrt( Sum(Xi^2)*Sum(Yi^2) ))
		 * */
		double Xi2 = 0.0;
		double Yi2 = 0.0;
		for(int i=0; i<documentVector_normalized.size(); i++){
			score += documentVector_normalized.get(i) * queryVector_normalized.get(i);
			Xi2 += Math.pow(documentVector_normalized.get(i), 2);
			Yi2 += Math.pow(queryVector_normalized.get(i), 2);
		}

		return score/Math.sqrt(Xi2*Yi2);

	}


	/**
	 * Computes the inverse document frequency.
	 * 
	 * @param term
	 * @return
	 */
	private Double getIDF(String term)
	{
		return 1d + Math.log(_indexer.numDocs()/(_indexer.corpusDocFrequencyByTerm(term)))/Math.log(2);
	}


	/**
	 * Counts the term frequency within the document.
	 * 
	 * @param documentVector
	 * @return Mapping of term to frequency
	 */
	private Map<String, Integer> getTermFrequency(Vector<String> documentVector){

		Map<String, Integer> termFrequency = new HashMap<String, Integer>();
		for(String dt : documentVector){
			if(termFrequency.containsKey(dt))
				termFrequency.put(dt, termFrequency.get(dt)+1);
			else
				termFrequency.put(dt, 1);
		}

		return termFrequency;
	}
}
