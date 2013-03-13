package edu.nyu.cs.cs2580;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.junit.Test;

public class DocumentProcessorTest {

	@Test
	public void testDocumentProcessor() throws FileNotFoundException{
		File file = new File("data/wiki/'03_Bonnie_&_Clyde");
		
		Scanner scan = new Scanner(file);  
		//reads all the text at once
		scan.useDelimiter("\\Z");  
		String content = scan.next();  
		scan.close();
		
		System.out.println(DocumentProcessor.htmlToText(content));
	}

}
