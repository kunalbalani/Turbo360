package edu.nyu.cs.cs2580.ranking_model;

import java.util.Vector;

import edu.nyu.cs.cs2580.Document;
import edu.nyu.cs.cs2580.Index;

public class NumViewsModel extends Model {

	public NumViewsModel(Index _index) {
		super(_index);
	}

	@Override
	public Double getScore(Vector<String> qv, Document d){
		return (double) d.get_numviews();
	}
}
