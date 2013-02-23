package edu.nyu.cs.cs2580;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

class Ranker {
  private Index _index;

  public Ranker(String index_source){
    _index = new Index(index_source);
  }

  public Vector < ScoredDocument > runquery(String query){
    Vector < ScoredDocument > retrieval_results = new Vector < ScoredDocument > ();
    for (int i = 0; i < _index.numDocs(); ++i){
      retrieval_results.add(runquery(query, i));
    }
    return retrieval_results;
  }
  
  
  public Vector < ScoredDocument > runquery(String query, String ranker_type){
	    Vector < ScoredDocument > retrieval_results = new Vector < ScoredDocument > ();
	    for (int i = 0; i < _index.numDocs(); ++i){
	    	ScoredDocument doc = null;
	    	if (ranker_type.equalsIgnoreCase("cosine")){
	    		doc = runquery_cosine(query, i);
	        } else if (ranker_type.equalsIgnoreCase("QL")){
	        	doc = runquery_ql(query, i);
	        } else if (ranker_type.equals("phrase")){
	              
	        } else if (ranker_type.equals("numviews")){
	        	doc = runquery_numviews(query, i);
	        } else if (ranker_type.equals("linear")){
            	
	        } else{
	        	doc = runquery(query, i);
	        }
	    	
	    	retrieval_results.add(doc);
	    }
	    
	    //Sorts the documents in descendng order.
	    Collections.sort(retrieval_results, new Comparator<ScoredDocument>() {

			@Override
			public int compare(ScoredDocument o1, ScoredDocument o2) {
				Double o1_score = o1._score;
				Double o2_score = o2._score;
				if(o1_score.isNaN())
					return 1;
				else if (o2_score.isNaN())
					return -1;
				else
					return -Double.compare(o1._score, o2._score); //Sorts in descending order
			}
		});	
		
	    return retrieval_results;
	  }

  public ScoredDocument runquery(String query, int did){

    // Build query vector
    Scanner s = new Scanner(query);
    Vector < String > qv = new Vector < String > ();
    while (s.hasNext()){
      String term = s.next();
      qv.add(term);
    }

    // Get the document vector. For hw1, you don't have to worry about the
    // details of how index works.
    Document d = _index.getDoc(did);
    Vector < String > dv = d.get_title_vector();

    // Score the document. Here we have provided a very simple ranking model,
    // where a document is scored 1.0 if it gets hit by at least one query term.
    double score = 0.0;
    for (int i = 0; i < dv.size(); ++i){
      for (int j = 0; j < qv.size(); ++j){
        if (dv.get(i).equals(qv.get(j))){
          score = 1.0;
          break;
        }
      }
    }

    return new ScoredDocument(did, d.get_title_string(), score);
  }
  
  
  
  /**
   * Calculates the Cosine Similarity
   * */
  public ScoredDocument runquery_cosine(String query, int did){

	    // Build query vector
	    Scanner s = new Scanner(query);
	    Vector < String > qv = new Vector < String > ();
	    while (s.hasNext()){
	      String term = s.next();
	      qv.add(term);
	    }

	    // Get the document vector. For hw1, you don't have to worry about the
	    // details of how index works.
	    Document d = _index.getDoc(did);
	    Vector < String > dv = d.get_body_vector();
	    Map<String, Integer> termFrequency = getTermFrequency(dv);
	    		
	    //Document vector that stores all tf.idfs for the document
	    Vector<Double> documentVector = new Vector<Double>();
	    Vector<Double> queryVector = new Vector<Double>();
	    
	    
//	    double xiyi = 0.0; //Store the sum of xi*yi
	    double xi2 = 0.0; //Stores the sum of xi^2
	    double yi2 = 0.0; //Stores the sum of yi^2
	    double score = 0.0; 
	    
	    
	    for (int i = 0; i < dv.size(); ++i){
	    	String documentTerm = dv.get(i);
	    	
	    	//Calculating the tf.idfs Document vectors
	    	double tf = (double) termFrequency.get(documentTerm);
	    	//double tf = (double) .termFrequency(documentTerm);
	    	double idf = 1d + Math.log(_index.numDocs()/(_index.documentFrequency(documentTerm)))/Math.log(2);
	    	double xi = tf*idf;
	    	
	    	documentVector.add(xi);
	    	
	    	//Calculating the query vector
	    	double yi = (qv.contains(documentTerm)) ? 1d : 0d;
	    	queryVector.add(yi);
	    	
	    	/*
	    	 * Cosine Similarity 
	    	 * Sum(xi*yi)/(Sqrt( Sum(xi^2)*Sum(yi^2) ))
	    	 * */
	    	//xiyi += xi*yi;
	    	xi2 += Math.pow(xi,2);
	    	yi2 += Math.pow(yi,2);
	    	
	    }
	    
	    double xi_norm = Math.sqrt(xi2);
	    double yi_norm = Math.sqrt(yi2);
	    
	    Vector<Double> documentVector_normalized = new Vector<Double>();
	    Vector<Double> queryVector_normalized = new Vector<Double>();
	    
	    for(int i=0; i<documentVector.size();i++){
	    	documentVector_normalized.add(documentVector.get(i)/xi_norm);
	    	queryVector_normalized.add(queryVector.get(i)/yi_norm);
	    }
	    
	    for(int i=0; i<documentVector_normalized.size(); i++){
	    	score += documentVector_normalized.get(i) * queryVector_normalized.get(i);
	    }
	    
	    //score = xiyi/Math.sqrt(xi2 * yi2);
	    
	    return new ScoredDocument(did, d.get_title_string(), score);
	  }
  
  
  /**
   * Calculates Query Likelihood 
   * */
  public ScoredDocument runquery_ql(String query, int did){

	  	double smoothFactor = 0.5;
        // Build query vector
        Scanner s = new Scanner(query);
        Vector < String > qv = new Vector < String > ();
        while (s.hasNext()){
          String term = s.next();
          qv.add(term);
        }

        // Get the document vector. For hw1, you don't have to worry about the
        // details of how index works.
        Document d = _index.getDoc(did);
        Vector < String > dv = d.get_body_vector();
        Map<String, Integer> termFrequency = getTermFrequency(dv);
        double score = 1d;
        int docTermCount = dv.size();
        int collectionTermCount = _index.termFrequency();
        for(int i = 0; i < qv.size(); i++){
            String queryTerm = qv.get(i);
            int qtermFreqDoc = (termFrequency.containsKey(queryTerm)) ? termFrequency.get(queryTerm) : 0;
            double firstTerm = (double) qtermFreqDoc/docTermCount;
            firstTerm *= (1-smoothFactor);
            int qtermFreqCollection = _index.termFrequency(queryTerm);
            double secondTerm = (double) qtermFreqCollection/collectionTermCount;
            secondTerm *= smoothFactor;
            score *= (firstTerm + secondTerm);
            
        }
        
        return new ScoredDocument(did, d.get_title_string(), score);
      }
  
  
  
  /**
   * Calculates the Numviews ranking signal
   * */
  public ScoredDocument runquery_numviews(String query, int did){

	    // Build query vector
	    Scanner s = new Scanner(query);
	    Vector < String > qv = new Vector < String > ();
	    while (s.hasNext()){
	      String term = s.next();
	      qv.add(term);
	    }

	    Document d = _index.getDoc(did);
	    //get the numof views for the document
	   	double score = d.get_numviews();
	    return new ScoredDocument(did, d.get_title_string(), score);
	  }
  
  
  
	
  /**
   * Counts the term frequency within the document.
   * */
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
