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
		
		Vector < String > dv = d.get_body_vector();
		
		Map<String, Integer> documentTermFrequency = getTermFrequency(dv);
		Map<String, Integer> queryTermFrequency = getTermFrequency(qv);

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
			double tf = (double) documentTermFrequency.get(documentTerm);
			//double tf = (double) .termFrequency(documentTerm);
			double idf = getIDF(documentTerm);
			double xi = tf*idf;

			documentVector.add(xi);

			double yi = 0.0;
			if(qv.contains(documentTerm)){	
				String queryTerm = documentTerm;
				//Calculating the query vector
				double queryTerm_tf = (double) queryTermFrequency.get(queryTerm);
				double queryTerm_idf = getIDF(queryTerm);
				yi = queryTerm_tf * queryTerm_idf;	
			}

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

		return score;
	}
	
	

}