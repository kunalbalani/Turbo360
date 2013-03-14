package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Merge {

	/**
	 * @param tempFiles file names
	 * @param output output filename
	 * @param delimiter 
	 * */
	private static void merge(String[] tempFiles, String base, String delimiter) throws IOException{
		
		BufferedReader[] readers = new BufferedReader[tempFiles.length];
		String[] currentLines = new String[tempFiles.length];
		
		File file = new File(base+"index.idx");
		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fileWritter = new FileWriter(file);
		BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
		
		for(int i = 0; i<tempFiles.length; i++){
			readers[i] = new BufferedReader(new FileReader(base + "temp/" + tempFiles[i]));
			currentLines[i] = readers[i].readLine();
		}
		
		int endOfFiles = 0;
		int currentTermID = 0;
		String currentTermPostingList = "";
		
		while(endOfFiles != tempFiles.length){
			currentTermPostingList = currentTermID+" ";
			for(int i = 0; i < tempFiles.length; i++){
				
				if(currentLines[i] != null && 
						currentLines[i].startsWith(Integer.toString(currentTermID))){
					currentTermPostingList += " "+currentLines[i].substring(currentLines[i].indexOf(":")+1, currentLines[i].length());
					currentLines[i] = readers[i].readLine();
					if(currentLines[i] == null){
						endOfFiles++;
						readers[i].close();
					}
				}
			}
			currentTermID++;
			bufferWritter.write(currentTermPostingList + "\n");
		}
		
		bufferWritter.close();

		//Delete all temporary files
		for(String tFile : tempFiles){
			File currentFile = new File(base + "temp/" + tFile);
			currentFile.delete();
		}
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String[] tempFiles = new String[]{"0","1","2","3","4","5","6","7","8","9","10"};
		merge(tempFiles, "data/index/invertedOccurenceIndex/", "\n");

	}

}
