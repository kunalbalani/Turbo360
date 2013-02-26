package edu.nyu.cs.cs2580;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

class QueryHandler implements HttpHandler
{
	private static String plainResponse =
			"Request received, but I am not smart enough to echo yet!\n";

	private Ranker _ranker;

	public QueryHandler(Ranker ranker){
		_ranker = ranker;
	}

	public static Map<String, String> getQueryMap(String query){  
		String[] params = query.split("&");  
		Map<String, String> map = new HashMap<String, String>();  
		for (String param : params){  
			String name = param.split("=")[0];  
			String value = param.split("=")[1];  
			map.put(name, value);  
		}
		return map;  
	} 

	public void handle(HttpExchange exchange) throws IOException {
		String requestMethod = exchange.getRequestMethod();
		if (!requestMethod.equalsIgnoreCase("GET")){  // GET requests only.
			return;
		}

		// Print the user request header.
		Headers requestHeaders = exchange.getRequestHeaders();
		System.out.print("Incoming request: ");
		for (String key : requestHeaders.keySet()){
			System.out.print(key + ":" + requestHeaders.get(key) + "; ");
		}
		System.out.println();
		String queryResponse = "";  
		String contentType = "text/plain";
		String uriQuery = exchange.getRequestURI().getQuery();
		String uriPath = exchange.getRequestURI().getPath();

		if ((uriPath != null) && (uriQuery != null)){
			if (uriPath.equals("/search")){
				Map<String,String> query_map = getQueryMap(uriQuery);
				Set<String> keys = query_map.keySet();
				if (keys.contains("query")){

					Vector < ScoredDocument > sds = null;
					String outputFileName = "";

					if (keys.contains("ranker")){
						String ranker_type = query_map.get("ranker");

						// @CS2580: Invoke different ranking functions inside your
						// implementation of the Ranker class.
						if (ranker_type.equalsIgnoreCase("cosine")){
							sds = _ranker.runquery(query_map.get("query"), "cosine");
							outputFileName = "hw1.1-vsm.tsv";
						} else if (ranker_type.equalsIgnoreCase("QL")){
							sds = _ranker.runquery(query_map.get("query"), "QL");
							outputFileName = "hw1.1-ql.tsv";
						} else if (ranker_type.equals("phrase")){
							queryResponse = (ranker_type + " not implemented.");
							outputFileName = "hw1.1-phrase.tsv";
						} else if (ranker_type.equals("numviews")){
							sds = _ranker.runquery(query_map.get("query"), "numviews");
							outputFileName = "hw1.1-numviews.tsv";
						} else if (ranker_type.equals("linear")){
							queryResponse = (ranker_type + " not implemented.");
							outputFileName = "hw1.2-linear.tsv";
						} else {
							queryResponse = (ranker_type+" not implemented.");
						}


					} else {
						// @CS2580: The following is instructor's simple ranker that does not
						// use the Ranker class.
						sds = _ranker.runquery(query_map.get("query"));
					}  

					//defaults to text, if query doesnt contain format parameter
					//and checks if format is equal to "text", if format is present
					if(!keys.contains("format") || query_map.get("format").equalsIgnoreCase("text")){
						queryResponse = getTextOutput(query_map, sds);
						writeResultToFile(queryResponse, outputFileName);
					}else{
						contentType = "text/html";
						queryResponse = getHTMLOutput(query_map, sds, 10);
					}
				}
			}
		}

		// Construct a simple response.
		Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.set("Content-Type", contentType);
		exchange.sendResponseHeaders(200, 0);  // arbitrary number of bytes
		OutputStream responseBody = exchange.getResponseBody();
		responseBody.write(queryResponse.getBytes());
		responseBody.close();
	}



	/**
	 * Creates HTML output
	 * */
	private static String getHTMLOutput(Map<String,String> query_map, 
			Vector<ScoredDocument> sds, int numberOfResults) {

		String output = "";
		String query = "";
		String ranker = "";

		if(query_map.containsKey("query")){
			query = query_map.get("query");
		}
		if(query_map.containsKey("ranker")){
			ranker = query_map.get("ranker");
		}

		output += "<html><body><br>" + 
				"<div style='font-size:25px; font-weight:bold'>Query : " + query + "<br>" +
				"Ranker : " + ranker + "<br>" +
				"<br><hr></div><br>";

		Iterator < ScoredDocument > itr = sds.iterator();
		while (itr.hasNext() && numberOfResults > 0){
			ScoredDocument sd = itr.next();
			String title = sd._title;
			int did = sd._did;
			double score = sd._score;
			output += "<div>" +
					"<span style='font-size:20px'><a href='clicked?did="+did+"' target='_blank'>"+title+"</a></span><br>" +
					"<span>Score : "+Double.toString(score)+"</span>" +
					"</div><br><br>";
			numberOfResults--;
		}

		output += "</body></html>";
		
		return output;
	}


	/**
	 * Creates text output
	 * */
	private static String getTextOutput(Map<String,String> query_map, 
			Vector<ScoredDocument> sds){
		String output = "";

		Iterator < ScoredDocument > itr = sds.iterator();
		while (itr.hasNext()){
			ScoredDocument sd = itr.next();
			if (output.length() > 0){
				output = output + "\n";
			}
			output = output + query_map.get("query") + "\t" + sd.asString();
		}
		if (output.length() > 0){
			output = output + "\n";
		}

		return output;
	}


	/**
	 * Writes the ranking results to the appropriate files in the results folder
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * */
	private void writeResultToFile(String queryResponse,
			String outputFileName) throws IOException {

		if(outputFileName.isEmpty())
			return;

		OutputStream outputStream = null;
		Writer out = null;

		try{
			outputStream = new FileOutputStream("results/"+outputFileName);
			out = new OutputStreamWriter(outputStream);
			out.write(queryResponse);
		}catch(FileNotFoundException fnfe){
			throw new FileNotFoundException("File Not Found : "+outputFileName+"\n");
		}finally{
			if(out != null)
				out.close();
			if(outputStream != null)
				outputStream.close();
		}

	}
}
