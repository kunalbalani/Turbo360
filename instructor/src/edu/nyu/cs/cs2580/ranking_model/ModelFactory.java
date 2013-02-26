package edu.nyu.cs.cs2580.ranking_model;

import edu.nyu.cs.cs2580.Index;

public class ModelFactory 
{
	public static Model getModel(Index _index,String ranker_type)
	{
		Model model = null;

		if (ranker_type.equalsIgnoreCase("cosine"))
		{
			model = new CosineModel(_index);
		}
		else if (ranker_type.equalsIgnoreCase("QL"))
		{
			model = new QLModel(_index);
		} 
		else if (ranker_type.equalsIgnoreCase("phrase"))
		{
			model = new PhraseModel(_index);	
		} 
		else if (ranker_type.equalsIgnoreCase("numviews"))
		{
			model = new NumViewsModel(_index);	
		}
		else if (ranker_type.equalsIgnoreCase("linear"))
		{
			//TODO need to implement linear model as sum
		} 
		else{
			//if no model is specified , use the default model
			model = new DefaultModel(_index);
		}
		
		return model;
	}

}
