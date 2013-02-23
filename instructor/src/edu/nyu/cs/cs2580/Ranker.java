package edu.nyu.cs.cs2580;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
	    	if (ranker_type.equals("cosine")){
	    		doc = runquery_cosine(query, i);
	        } else if (ranker_type.equals("QL")){
	              
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

	    //Document vector that stores all tf.idfs for the document
	    //Vector<Double> documentVector = new Vector<Double>();
	    //Vector<Double> queryVector = new Vector<Double>();
	    
	    double xiyi = 0.0; //Store the sum of xi*yi
	    double xi2 = 0.0; //Stores the sum of xi^2
	    double yi2 = 0.0; //Stores the sum of yi^2
	    double score = 0.0; 
	    
	    
	    for (int i = 0; i < dv.size(); ++i){
	    	String documentTerm = dv.get(i);
	    	
	    	//Calculating the tf.idfs Document vectors
	    	double tf = getTermFrequency(documentTerm, dv);
	    	double idf = 1d + Math.log(_index.numDocs()/(_index.documentFrequency(documentTerm)))/Math.log(2);
	    	double xi = tf*idf;
	    	
	    	//documentVector.add(tf*idf);
	    	
	    	//Calculating the query vector
	    	//queryVector.add((qv.contains(documentTerm)) ? 1d : 0d);
	    	double yi = (qv.contains(documentTerm)) ? 1d : 0d;
	    	
	    	/*
	    	 * Cosine Similarity 
	    	 * Sum(xi*yi)/(Sqrt( Sum(xi^2)*Sum(yi^2) ))
	    	 * */
	    	xiyi += xi*yi;
	    	xi2 += Math.pow(xi,2);
	    	yi2 += Math.pow(yi,2);
	    	
	    }
	    
	    score = xiyi/Math.sqrt(xi2 * yi2);
	    
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
  private int getTermFrequency(String t, Vector<String> documentVector){
	  
	  int frequency = 0;
	  for(String dt : documentVector){
		  frequency += (dt.equalsIgnoreCase(t)) ? 1 : 0;
	  }
	  
	  return frequency;
  }
  
}
