package edu.nyu.cs.cs2580;

import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;
import java.util.Vector;

import edu.nyu.cs.cs2580.ranking_model.ModelFactory;

class Ranker 
{
	private Index _index;

	public Ranker(String index_source)
	{
		_index = new Index(index_source);
	}

	/**
	 * Iterates over the document and computes the score for each document
	 * based on default ranking model
	 * 
	 * @param query 
	 * @return Vector of Scored Document's
	 */
	public Vector < ScoredDocument > runquery(String query)
	{
		//execute default ranking
		return this.runquery(query,"");
	}

	/**
	 * Iterates over the document and computes the score for each document
	 * based on the given ranking_model. 
	 * 
	 * If the ranker_model is not defined then it selects a default model
	 * for ranking
	 * 
	 * @param query 
	 * @param ranker_type
	 * @return Vector of Scored Document's
	 */
	public Vector < ScoredDocument > runquery(String query, String ranker_type)
	{
		Vector < ScoredDocument > retrieval_results = new Vector < ScoredDocument > ();

		for (int i = 0; i < _index.numDocs(); ++i)
		{
			ScoredDocument doc = null;
			doc = runquery(query, i, ranker_type);
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

	/**
	 * This functions scores the document based on the query and 
	 * the similarity metric type or ranker_type. 
	 * 
	 * Correlation between the query and document is directly 
	 * Proportional to their corresponding score.
	 * 
	 * @param query
	 * @param did
	 * @param ranker_type Cosine,QL,linear,phrase,numviews
	 * @return
	 */
	public ScoredDocument runquery(String query, int did , String ranker_type)
	{
		// Build query vector
		Scanner s = new Scanner(query);
		Vector < String > qv = new Vector < String > ();
		while (s.hasNext())
		{
			String term = s.next();
			qv.add(term);
		}

		Document d = _index.getDoc(did);
		double score = ModelFactory.getModel(_index, ranker_type).getScore(qv,d);

		return new ScoredDocument(did, d.get_title_string(), score);
	}
}
