package edu.nyu.cs.cs2580.documentProcessor;

import java.io.FileReader;
import java.util.Scanner;
import java.util.Vector;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;

public class DocumentProcessor {

	StemmingAndStopWordsWrapper stemmingAndStopWordsWrapper;

	public DocumentProcessor(){
		stemmingAndStopWordsWrapper 
		= new StemmingAndStopWordsWrapper(new PorterStemmer());
	}

	/**
	 * Process the text to include the following operations (removes the html tags, 
	 * replace punctuations with whitespace, downcase the words, 
	 * removes the stopwords and stems the words)
	 * 
	 * @param fileReader FileReader object
	 * */
	public Vector<String> process(FileReader fileReader) {

		Scanner scan = new Scanner(fileReader);  
		//reads all the text at once
		scan.useDelimiter("\\Z");  
		String content = scan.next();  

		return process(content);
	}


	/**
	 * Process the text to include the following operations (removes the html tags, 
	 * replace punctuations with whitespace, downcase the words, 
	 * removes the stopwords and stems the words)
	 * 
	 * @param text text to be process
	 * */
	public Vector<String> process(String text) {

		Vector<String> processedTokens = null;

		try{
			//removes HTML
			String content = ArticleExtractor.getInstance().getText(text);

			//removes non-alphanumeric characters
			content = content.replaceAll("\\W", " ");

			//splits based on whitespace
			String[] tokens = content.split("\\s+");
			
			processedTokens = stemmingAndStopWordsWrapper.process(tokens);
			
		}catch(BoilerpipeProcessingException e){
			e.printStackTrace();
		}

		return processedTokens;
	}


//	public static void main(String args[]) throws FileNotFoundException, BoilerpipeProcessingException{
//
//		FileReader fileReader = new FileReader("data/wiki/(Shake,_Shake,_Shake)_Shake_Your_Booty");
//
//		DocumentProcessor dp = new DocumentProcessor();
//
//		System.out.println(dp.process(fileReader));
//	}
}
