package edu.nyu.cs.cs2580.ranking_model;

import java.util.Map;
import java.util.Vector;

import edu.nyu.cs.cs2580.Document;
import edu.nyu.cs.cs2580.Index;

public class CosineModel extends Model
{

	public CosineModel(Index _index)
	{
		super(_index);
	}
	
	@Override
	public Double getScore(Vector<String> qv, Document d) {
		
		Vector < String > dv = d.get_document_vector();
		
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
			//Taking log of term frequency to reduce the impact of frequent terms
			double tf = Math.log((double) documentTermFrequency.get(documentTerm));
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
				double queryTerm_tf = Math.log((double) queryTermFrequency.get(queryTerm));
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
	

}
