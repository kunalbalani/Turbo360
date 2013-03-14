package edu.nyu.cs.cs2580;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
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
	 * @throws FileNotFoundException 
	 * */
	public Vector<String> process(File file) throws FileNotFoundException {

		Scanner scan = new Scanner(file);  
		//reads all the text at once
		scan.useDelimiter("\\Z");  
		String content = scan.next();  
		scan.close();
		return process(content);
	}


	/**
	 * Process the text to include the following operations (removes the html tags, 
	 * replace punctuations with whitespace, downcase the words, 
	 * removes the stopwords and stems the words)
	 * 
	 * @param text text to be process
	 * */
	public Vector<String> process(String htmlText) {

		Vector<String> processedTokens = null;

		//removes HTML
		String content = htmlToText(htmlText);

		//removes non-alphanumeric characters
		content = content.replaceAll("\\W", " ");

		//splits based on whitespace
		String[] tokens = content.split("\\s+");

		processedTokens = stemmingAndStopWordsWrapper.process(tokens);

		return processedTokens;
	}
	
	


	/**
	 * Converts HTML to plain text. Removes everything in &lt;script&gt; tag.
	 * @throws BoilerpipeProcessingException 
	 * */
	public static String htmlToText(String htmlText) {

		String content = "";
		try{
			content = ArticleExtractor.getInstance().getText(htmlText);
		}catch (BoilerpipeProcessingException e) {
			e.printStackTrace();
		}
		return content;
	}


//	public static void main(String args[]) throws FileNotFoundException{
//		File file = new File("data/wiki/'03_Bonnie_&_Clyde");
//
//		Scanner scan = new Scanner(file);  
//		//reads all the text at once
//		scan.useDelimiter("\\Z");  
//		String content = scan.next();  
//		scan.close();
//
//		System.out.println(content+"\n\n\n\n-------------------------------------\n\n\n\n");
//
//		System.out.println(htmlToText(content));
//
//	}

}
