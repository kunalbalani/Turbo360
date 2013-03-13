package edu.nyu.cs.cs2580;


public class T3IndexWriter 
{
	//split file after these many entries
	public final Integer splitCount = 1000;

	private String rootFileName;
	private T3FileWriter currentFileWriter;
	private Integer totalObjectsWritten;

	public T3IndexWriter (String rootFileName)
	{
		this.rootFileName = rootFileName;
		this.totalObjectsWritten = splitCount;
	}


	public void write(Object content){

		if(currentFileWriter == null || totalObjectsWritten <= 0)
		{
			addNewFile();
		}

		T3FileWriter writer = currentFileWriter;
		writer.write(content);
		totalObjectsWritten--;
	}

	private void addNewFile(){

		if(totalObjectsWritten > splitCount){
			throw new IllegalArgumentException("Objects written is greate than split count");
		}
		currentFileWriter = new T3FileWriter(rootFileName+(splitCount-totalObjectsWritten)+".idx");
	}
}
