package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class T3FileReader{


	private FileInputStream fileInputStream ;
	private DataInputStream in;
	private BufferedReader reader;
	private String filePath;
	
	public T3FileReader(String filepath){

		this.filePath = filepath;
		try 
		{
			fileInputStream = new FileInputStream(filepath);
			in = new DataInputStream(fileInputStream);
			reader = new BufferedReader(new InputStreamReader(in));
			
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	public String read(){

		String retVal = null;
		try {
			retVal =  reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return retVal;
	}

	public void close(){
		try {
			reader.close();
			in.close();
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isStringPresent(String term){
		
		String strLine;
		try {
			  //Read File Line By Line
			  while ((strLine = reader.readLine()) != null)   {
				  if(term.equalsIgnoreCase(strLine)){
					  return true;
				  }
			  }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return false;
	}

	public void merge(Integer termID, PostingsWithOccurences postingList) {
		
//		close();
//		T3FileWriter t3W = new T3FileWriter(this.filePath);
////		t3W.getWriter().
	}	
}
