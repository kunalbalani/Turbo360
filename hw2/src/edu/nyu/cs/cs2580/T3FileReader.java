package edu.nyu.cs.cs2580;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class T3FileReader{


	private FileInputStream fileInputStream ;
	private ObjectInputStream reader;
	private String filePath;
	
	public T3FileReader(String filepath){

		this.filePath = filepath;
		try 
		{
			fileInputStream = new FileInputStream(filepath);
			reader = new ObjectInputStream(fileInputStream);
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	public Object read(){

		Object retVal = null;
		try {
			retVal =  reader.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return retVal;
	}

	public void close(){
		try {
			reader.close();
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isObjectPresent(Integer term){
		
		Integer termID;
		try {
			while((termID = (Integer)reader.readObject()) != Integer.MIN_VALUE){
				if(termID == term){
					return true;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public void merge(Integer termID, PostingsWithOccurences postingList) {
		
		close();
		T3FileWriter t3W = new T3FileWriter(this.filePath);
//		t3W.getWriter().
	}	
}
