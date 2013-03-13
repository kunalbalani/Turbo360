package edu.nyu.cs.cs2580;

import java.util.Vector;

import org.junit.Assert;
import org.junit.Test;

public class Tests {

	@Test
	public void testPhraseQueryTokens() {
		
		QueryPhrase qp = new QueryPhrase("x y \"New york\"");
		qp.processQuery();
		Vector<String> v = qp._tokens;
		
		Assert.assertEquals("x", v.get(0));
		Assert.assertEquals("y", v.get(1));
		Assert.assertEquals("New york", v.get(2));
		Assert.assertEquals(3, v.size());
	}
	
	
	public void testDocumentProcessor(){
		
		
	}

}
