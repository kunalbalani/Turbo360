package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class T3FileWriter {


	private FileWriter fileOutputStream ;
	private BufferedWriter writer ;
	private String filepath;

	
	public T3FileWriter(String filepath){

		this.filepath = filepath;
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
			writer.flush();
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

	public void merge(String entry)
	{
		//close the curren file
		close();

		String tempFile = readFileAsString(filepath);
		clearFile(filepath);
		
		String delimiter = "\\n";

		if(tempFile != null){
			
			for(String line : tempFile.split(delimiter))
			{
				Integer lineTermID = T3Parser.parseTermInvertedIndex(line);
				Integer entryTermID = T3Parser.parseTermInvertedIndex(entry);

				if(lineTermID == entryTermID){
					
					PostingsWithOccurences<Integer> p1 = T3Parser.parsePostingInvertedIndex(line);
					PostingsWithOccurences<Integer> p2 = T3Parser.parsePostingInvertedIndex(entry);
					
					p1.addAll(p2);
					
					String newEntry = lineTermID + p1.toString();
					
					tempFile.replaceFirst(line, newEntry);
				}
			}
			
		}

	}

	private String readFileAsString(String filePath)
	{
		try
		{

			StringBuffer fileData = new StringBuffer();
			BufferedReader reader = new BufferedReader(
					new FileReader(filePath));
			char[] buf = new char[1024];
			int numRead=0;
			while((numRead=reader.read(buf)) != -1)
			{
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
			}
			reader.close();
			return fileData.toString();
		}catch (IOException e){
			e.printStackTrace();
			return null;
		}
	}
	
	public void clearFile(String fileLocation){
	    try{
	        BufferedWriter bw = new BufferedWriter(new FileWriter(fileLocation));
	        bw.write("");
	        bw.flush();
	        bw.close();
	    }catch(IOException ioe){
	        // You should really do something more appropriate here
	        ioe.printStackTrace();
	    }
	}

}
