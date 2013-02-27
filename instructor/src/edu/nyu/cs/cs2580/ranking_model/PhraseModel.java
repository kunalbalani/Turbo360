package edu.nyu.cs.cs2580.ranking_model;

import java.util.Vector;

import edu.nyu.cs.cs2580.Document;
import edu.nyu.cs.cs2580.Index;

public class PhraseModel extends Model {

	
	public PhraseModel(Index _index) {
		super(_index);
	}
	
	@Override
	public Double getScore(Vector<String> qv, Document d){
		
		Vector<String> dv = d.get_document_vector();
		
		// Score the document. Here we have provided a very simple ranking model,
		// where a document is scored 1.0 if it gets hit by at least one query term.
		double score = 0.0;
		for (int j = 0; j < qv.size()-1; ++j){
			Vector <String> sub_query = new Vector <String>();
			sub_query.add(qv.get(j));
			sub_query.add(qv.get(j+1));
			score += count_phrase(sub_query,dv);
		}
		return score;
	}

	/**
	 * Returns the count of the sub_query in document vector
	 * @param sub_query
	 * @param dv
	 * @return
	 */
	int count_phrase(Vector <String> sub_query, Vector<String> dv){

		int count = 0;

		if(sub_query.size() != 2){
			throw new IllegalArgumentException("Phrase subquery contains more than 2 terms");
		}

		for(int i = 0 ; i < dv.size()-1 ;i++)
		{
			if(sub_query.firstElement().equalsIgnoreCase(dv.get(i)) && 
				sub_query.lastElement().equalsIgnoreCase(dv.get(i+1))){
				count++;
			}
		}

		return count;
	}
}
