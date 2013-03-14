package edu.nyu.cs.cs2580;

import java.util.ArrayList;
import java.util.List;

public class T3IndexReader {
	
	public T3FileReader getCurrentFileReader() {
		return currentFileReader;
	}

	public void setCurrentFileReader(T3FileReader currentFileReader) {
		this.currentFileReader = currentFileReader;
	}

	//split file after these many entries
	public final Integer splitCount = 1000000;
	
	private String rootFileName;
	private Integer currentReader = 0;
	private T3FileReader currentFileReader;
	private String currentFileName;
	List<T3FileReader> files = new ArrayList<T3FileReader>();
	
	public T3IndexReader (String rootFileName)
	{
		this.rootFileName = rootFileName;
		currentFileName = rootFileName+currentReader+".idx";
		currentFileReader = new T3FileReader(currentFileName);
	}
	
	public String getCurrentFileName() {
		return currentFileName;
	}

	public void setCurrentFileName(String currentFileName) {
		this.currentFileName = currentFileName;
	}

	public boolean contains(String termId){
		Integer fileToBeSearched = Integer.parseInt(termId.charAt(0)+"") / 1000;
		loadFile(fileToBeSearched);		
		return 	currentFileReader.isStringPresent(termId);
	}
	
	private void loadFile(Integer fileIndex){
		currentFileName = rootFileName+fileIndex+".idx";
		currentFileReader = new T3FileReader(currentFileName);
	}
}
