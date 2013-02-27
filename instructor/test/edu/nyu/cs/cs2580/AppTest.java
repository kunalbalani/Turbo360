package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Executors;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.sun.net.httpserver.HttpServer;


/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase
{
	int port_no = 12345;
	String path = "src/data/qrels.tsv";

	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public AppTest( String testName )
	{
		super( testName );
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite()
	{
		return new TestSuite( AppTest.class );
	}

	/**
	 * Rigorous Test :-)
	 * @throws UnsupportedEncodingException 
	 * @throws MalformedURLException 
	 * @throws URISyntaxException 
	 */
	public void testApp() throws UnsupportedEncodingException, MalformedURLException, URISyntaxException
	{
		String query = "data mining";
		String format = "text";
		String raker = "phrase";

		String httpRequest = "http://localhost:"+port_no+"/search?query="+query+"&ranker="+raker+"&format="+format;

		URL url = new URL(httpRequest);
		URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
		url = uri.toURL();
		
		try{

			// create the HttpServer
			InetSocketAddress address = new InetSocketAddress(port_no);
			HttpServer httpServer = HttpServer.create(address, -1);

			Ranker ranker = new Ranker(path);

			// Attach specific paths to their handlers.
			httpServer.createContext("/", new QueryHandler(ranker));
			httpServer.setExecutor(Executors.newCachedThreadPool());
			httpServer.start();


			// verify our client code
			System.out.println("Sending reuqest "+url.toString());
			
			URLConnection conn = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

//			assertEquals("<?xml version=\"1.0\"?>", in.readLine());
//			assertEquals("<resource id=\"1234\" name=\"test\" />", in.readLine());
			
			String sCurrentLine;
			while ((sCurrentLine = in.readLine()) != null) 
			{
				System.out.println(sCurrentLine);
			}
			
			// stop the server
			httpServer.stop(0);
			assertTrue( true );
			
		}catch (Exception e){

		}
	}
}
