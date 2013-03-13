package edu.nyu.cs.cs2580;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class T3FileWriter {
	
	
	private FileWriter fileOutputStream ;
	private BufferedWriter writer ;
	
	public T3FileWriter(String filepath){
		
		try 
		{
			fileOutputStream = new FileWriter(filepath);
			writer = new BufferedWriter(fileOutputStream);

		} catch (IOException e){
			System.out.println("Creating new file");
			File tmp = new File(filepath);
			try {
				tmp.getParentFile().mkdirs();
				tmp.createNewFile();
				
				
				fileOutputStream = new FileWriter(filepath);
				writer = new BufferedWriter(fileOutputStream);
			} catch (IOException e1) {
				
				e1.printStackTrace();
			}
			
		}
	}
	
	public void write(String content){
		try {
			writer.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close(){
		try {
			writer.close();
			fileOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
