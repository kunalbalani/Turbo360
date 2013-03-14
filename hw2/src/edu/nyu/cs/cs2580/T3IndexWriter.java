package edu.nyu.cs.cs2580;


public class T3IndexWriter 
{
	//split file after these many entries
	public final Integer splitCount = 100000;

	private String rootFileName;
	private T3FileWriter currentFileWriter;
	private Integer totalObjectsWritten;
	private String currentFileName;

	public T3IndexWriter (String rootFileName)
	{
		this.rootFileName = rootFileName;
		this.totalObjectsWritten = splitCount;
		addNewFile();
	}


	public void write(String content){

		if(currentFileWriter == null || totalObjectsWritten <= 0)
		{
			addNewFile();
		}

		T3FileWriter writer = currentFileWriter;
		writer.write(content);
		writer.write("\n");
		totalObjectsWritten--;
	}
	
	public void merge(String entry)
	{
		if(currentFileWriter != null){
			currentFileWriter.merge(entry);
		}
	}

	private void addNewFile(){

		if(totalObjectsWritten > splitCount){
			throw new IllegalArgumentException("Objects written is greate than split count");
		}
		currentFileName = rootFileName+(splitCount-totalObjectsWritten)+".idx";
		currentFileWriter = new T3FileWriter(currentFileName);
		
		System.out.println("creating new Index file "+currentFileName);
	}


	public String getCurrentFileName() {
		return currentFileName;
	}


	public void setCurrentFileName(String currentFileName) {
		this.currentFileName = currentFileName;
	}	
}
