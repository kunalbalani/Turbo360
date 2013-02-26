package edu.nyu.cs.cs2580.ranking_model;

import java.util.Map;
import java.util.Vector;

import edu.nyu.cs.cs2580.Document;
import edu.nyu.cs.cs2580.Index;

public class QLModel extends Model 
{

	public QLModel(Index _index) {
		super(_index);
	}

	@Override
	public Double getScore(Vector<String> qv, Document d) {

		Vector < String > dv = d.get_body_vector();

		double smoothFactor = 0.5;
		
		Map<String, Integer> termFrequency = getTermFrequency(dv);
		double score = 1d;
		int docTermCount = dv.size();
		int collectionTermCount = get_index().termFrequency();
		for(int i = 0; i < qv.size(); i++){
			String queryTerm = qv.get(i);
			int qtermFreqDoc = (termFrequency.containsKey(queryTerm)) ? termFrequency.get(queryTerm) : 0;
			double firstTerm = (double) qtermFreqDoc/docTermCount;
			firstTerm *= (1-smoothFactor);
			int qtermFreqCollection = get_index().termFrequency(queryTerm);
			double secondTerm = (double) qtermFreqCollection/collectionTermCount;
			secondTerm *= smoothFactor;
			score *= (firstTerm + secondTerm);

		}
		return score;
	}

}
