package edu.nyu.cs.cs2580.ranking_model;

import java.util.Vector;

import edu.nyu.cs.cs2580.Document;
import edu.nyu.cs.cs2580.Index;

public class SimpleLinearModel extends Model 
{

	public SimpleLinearModel(Index _index) {
		super(_index);
	}

	@Override
	public Double getScore(Vector<String> qv, Document d) {

		double cosineBeta = 0.2;
		double qlBeta = 0.2;
		double phraseBeta = 0.5;
		double numviewsBeta = 0.1;
		
		Index _index = get_index();
		
		double cosineScore = cosineBeta * ModelFactory.getModel(_index, "cosine").getScore(qv,d);
		double qlScore = qlBeta * ModelFactory.getModel(_index, "ql").getScore(qv,d);
		double phraseScore = phraseBeta * ModelFactory.getModel(_index, "phrase").getScore(qv,d);
		double numviewsScore = numviewsBeta * ModelFactory.getModel(_index, "numviews").getScore(qv,d);
		
		
//		return cosineScore + qlScore + phraseScore + numviewsScore;
		return 0.0;
	}

}
