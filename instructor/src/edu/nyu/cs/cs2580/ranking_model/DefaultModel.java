package edu.nyu.cs.cs2580.ranking_model;

import java.util.Vector;

import edu.nyu.cs.cs2580.Document;
import edu.nyu.cs.cs2580.Index;

public class DefaultModel extends Model {

	public DefaultModel(Index _index) {
		super(_index);
	}

	public Double getScore(Vector<String> qv, Document d){
		return super.getScore(qv, d);
	}
}
