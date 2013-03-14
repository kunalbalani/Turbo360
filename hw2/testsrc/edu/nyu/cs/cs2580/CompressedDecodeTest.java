package edu.nyu.cs.cs2580;

import java.util.Vector;

import org.junit.Assert;
import org.junit.Test;

public class CompressedDecodeTest {

	@Test
	public void test() {
		Vector<String> encodedString = new Vector<String>();
		encodedString.add("99");
		encodedString.add("8d");
		encodedString.add("88");
		encodedString.add("95");
		encodedString.add("b6");
		encodedString.add("a6");
		
		Vector<Integer> decoded = IndexerInvertedCompressed.decode(encodedString);
		
		Vector<Integer> expected = new Vector<Integer>();
		expected.add(25);
		expected.add(38);
		expected.add(46);
		expected.add(67);
		expected.add(121);
		expected.add(159);
		
		Assert.assertEquals(expected, decoded);
		
	}

}
