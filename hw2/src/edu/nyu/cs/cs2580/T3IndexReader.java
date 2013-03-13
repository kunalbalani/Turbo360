package edu.nyu.cs.cs2580;

import java.util.ArrayList;
import java.util.List;

public class T3IndexReader {
	//split file after these many entries
	public final Integer splitCount = 1000;
	
	private String rootFileName;
	private Integer currentReader = 0;
	private T3FileReader currentFileReader;
	List<T3FileReader> files = new ArrayList<T3FileReader>();
	
	public T3IndexReader (String rootFileName)
	{
		this.rootFileName = rootFileName;
		currentFileReader = new T3FileReader(rootFileName+currentReader+".idx");
	}
	
	public boolean contains(Integer termId){
		Integer fileToBeSearched = termId / 1000;
		loadFile(fileToBeSearched);		
		return 	currentFileReader.isObjectPresent(termId);
	}
	
	public void merge(Integer termID, PostingsWithOccurences postingList){
		if(currentFileReader != null){
			currentFileReader.merge(termID,postingList);
		}
	}
	
	private void loadFile(Integer fileIndex){
		currentFileReader = new T3FileReader(rootFileName+fileIndex+".idx");
	}
}
