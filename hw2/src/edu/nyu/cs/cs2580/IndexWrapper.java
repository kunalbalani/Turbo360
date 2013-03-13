package edu.nyu.cs.cs2580;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;


public class IndexWrapper extends HashMap<Integer, PostingsWithOccurences>{

	private static final long serialVersionUID = 6127445645999471161L;

	private String _indexTempFolder;
	private int _tempFileCount = 0;

	@SuppressWarnings("unused")
	private IndexWrapper(){};

	public IndexWrapper(String indexTempFolder)
	{
		_indexTempFolder = indexTempFolder;
		File tempdir = new File(indexTempFolder);
		if(!tempdir.exists())
			tempdir.mkdir();
	}


	public void writeToDisk() 
	{
		T3FileWriter t3 = new T3FileWriter(_indexTempFolder + "/" + Integer.toString(_tempFileCount));

		Set<Integer> terms = this.keySet();
		Integer[] keys = terms.toArray(new Integer[terms.size()]);
		Arrays.sort(keys);

		System.out.println("have to write "+keys.length + " entries");
		for(int i=0; i<keys.length; i++)
		{
			t3.write(keys[i]+":");
			t3.write(this.get(keys[i])+"");
			t3.write("\n");
		}
		
		t3.close();
		this.clear();

		System.out.println("Created " + Integer.toString(_tempFileCount));
		_tempFileCount++;

	}


}
