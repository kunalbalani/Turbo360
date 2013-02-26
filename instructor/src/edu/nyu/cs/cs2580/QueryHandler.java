package edu.nyu.cs.cs2580;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.Vector;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

class QueryHandler implements HttpHandler
{
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


					String searchSessionID = getSearchSessionID();
					logAction(query_map, sds, searchSessionID);

					//defaults to text, if query doesnt contain format parameter
					//and checks if format is equal to "text", if format is present
					if(!keys.contains("format") || query_map.get("format").equalsIgnoreCase("text")){
						queryResponse = getTextOutput(query_map, sds);
						//writes the result to the appropriate file in resutls folder
						writeToFile(queryResponse, outputFileName);
					}else{
						contentType = "text/html";
						queryResponse = getHTMLOutput(query_map, sds, 10, searchSessionID);
					}

				}
			} else if (uriPath.equals("/click")){
				logClick(uriQuery);
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
			Vector<ScoredDocument> sds, int numberOfResults, String searchSessionID) {

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
					"<span style='font-size:20px'>" +
					"<a href='click?query="+query+"&did="+did+"&ssid="+searchSessionID+"' " +
					"target='_blank'>"+title+"</a>" +
					"</span><br>" +
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
	 * Logs the user search session actions
	 * @throws IOException 
	 * */
	private void logAction(Map<String, String> query_map,
			Vector<ScoredDocument> sds, String searchSessionID) throws IOException {

		String query = query_map.get("query");
		String logMessage = "";		
		
		String DATE_FORMAT = "yyyyMMddHHmmssZ";
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		String dateTimeString =  sdf.format(new Date());

		Iterator < ScoredDocument > itr = sds.iterator();
		while (itr.hasNext()){
			ScoredDocument sd = itr.next();
			logMessage += searchSessionID + "\t" + 
						  query + "\t" + 
						  sd._did + "\t" +
						  "render\t" + 
						  dateTimeString + "\n";
		}

		writeToFile(logMessage, "hw1.4-log.tsv");
	}

	
	/**
	 * Logs the click event
	 * */
	private void logClick(String uriQuery) throws IOException {
		
		Map<String,String> query_map = getQueryMap(uriQuery);
		
		String DATE_FORMAT = "yyyyMMddHHmmssZ";
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		String dateTimeString =  sdf.format(new Date());
	
		String logMessage = query_map.get("ssid") + "\t" + 
				query_map.get("query") + "\t" + 
				query_map.get("did") + "\t" +
				  "click\t" + 
				  dateTimeString + "\n";
		
		writeToFile(logMessage, "hw1.4-log.tsv");
	}

	
	/**
	 * Generates a Universally Unique IDentifier.
	 * */
	private static String getSearchSessionID(){
		return UUID.randomUUID().toString();
	}


	/**
	 * Writes the ranking results to the appropriate files in the results folder
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * */
	private void writeToFile(String text,
			String outputFileName) throws IOException {

		if(outputFileName.isEmpty())
			return;

		FileWriter fileWriter = null;

		try{
			fileWriter = new FileWriter("./results/"+outputFileName, true);
			fileWriter.write(text);
			fileWriter.close();
		}finally{
			if(fileWriter != null)
				fileWriter.close();
		}

	}
}
