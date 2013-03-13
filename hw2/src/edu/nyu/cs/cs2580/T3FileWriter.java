package edu.nyu.cs.cs2580;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class T3FileWriter {
	
	
	private FileOutputStream fileOutputStream ;
	private ObjectOutputStream writer ;
	
	public T3FileWriter(String filepath){
		
		try 
		{
			fileOutputStream = new FileOutputStream(filepath);
			writer = new ObjectOutputStream(fileOutputStream);

		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public FileOutputStream getFileOutputStream() {
		return fileOutputStream;
	}

	public void setFileOutputStream(FileOutputStream fileOutputStream) {
		this.fileOutputStream = fileOutputStream;
	}

	public ObjectOutputStream getWriter() {
		return writer;
	}

	public void setWriter(ObjectOutputStream writer) {
		this.writer = writer;
	}

	public void write(Object content){
		
		try {
			writer.writeObject(content);
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
