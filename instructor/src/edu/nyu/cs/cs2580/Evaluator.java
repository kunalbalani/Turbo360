package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

class Evaluator {

	public static void main(String[] args) throws IOException {
		HashMap < String , HashMap < Integer , Double > > relevance_judgments =
				new HashMap < String , HashMap < Integer , Double > >();
		if (args.length < 1){
			System.out.println("need to provide relevance_judgments");
			return;
		}
		String p = args[0];
		// first read the relevance judgments into the HashMap
		readRelevanceJudgments(p,relevance_judgments);

		// now evaluate the results from stdin
		//evaluateStdin(relevance_judgments);

		// Evaluates All Metrics
		evaluateStdin_AllMetrics(relevance_judgments);
	}

	public static void readRelevanceJudgments(
			String p,HashMap < String , HashMap < Integer , Double > > relevance_judgments){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(p));
			try {
				String line = null;
				while ((line = reader.readLine()) != null){
					// parse the query,did,relevance line
					Scanner s = new Scanner(line).useDelimiter("\t");
					String query = s.next();
					int did = Integer.parseInt(s.next());
					String grade = s.next();
					double rel = 0.0;
					// convert to binary relevance
					if (grade.equals("Perfect"))
						rel = 10.0;
					else if (grade.equals("Excellent"))
						rel = 7.0;
					else if (grade.equals("Good"))
						rel = 5.0;
					else if (grade.equals("Fair"))
						rel = 1.0;
					else if (grade.equals("Bad"))
						rel = 0.0;
					
					if (relevance_judgments.containsKey(query) == false){
						HashMap < Integer , Double > qr = new HashMap < Integer , Double >();
						relevance_judgments.put(query,qr);
					}
					HashMap < Integer , Double > qr = relevance_judgments.get(query);
					qr.put(did,rel);
				}
			} finally {
				reader.close();
			}
		} catch (IOException ioe){
			System.err.println("Oops " + ioe.getMessage());
		}
	}
	
	
	


	public static void evaluateStdin(
			HashMap < String , HashMap < Integer , Double > > relevance_judgments){
		// only consider one query per call   

		try {

			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

			try{

				String line = null;
				double RR = 0.0;
				double N = 0.0;
				while ((line = reader.readLine()) != null){
					Scanner s = new Scanner(line).useDelimiter("\t");
					String query = s.next();
					int did = Integer.parseInt(s.next());
					String title = s.next();
					double rel = Double.parseDouble(s.next());
					if (relevance_judgments.containsKey(query) == false){
						throw new IOException("query not found");
					}
					HashMap < Integer , Double > qr = relevance_judgments.get(query);
					if (qr.containsKey(did) != false){
						RR += qr.get(did);					
					}
					++N;
				}
				System.out.println(Double.toString(RR/N));
			} finally {
				reader.close();
			}
		} catch (Exception e){
			System.err.println("Error:" + e.getMessage());
		}
	}


	public static void evaluateStdin_AllMetrics(
			HashMap < String , HashMap < Integer , Double > > relevance_judgments){

		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			try{
				String line = null;
				Vector<String> results = new Vector<String>();
				String query = null;
				String outputFileName = "tempEvaluatorResults.tsv";

				boolean gotQuery = false;
				while ((line = reader.readLine()) != null){
					if(!gotQuery){
						Scanner s = new Scanner(line).useDelimiter("\t");
						query = s.next();
						s.close();
						gotQuery = true;
					}
					results.add(line);
				}

				Double precision_1 = calculatePrecision(results, 1, relevance_judgments);
				Double precision_5 = calculatePrecision(results, 5, relevance_judgments);
				Double precision_10 = calculatePrecision(results, 10, relevance_judgments);

				Double recall_1 = calculateRecall(results, 1, relevance_judgments);
				Double recall_5 = calculateRecall(results, 5, relevance_judgments);
				Double recall_10 = calculateRecall(results, 10, relevance_judgments);
				
				Double f0_50_1 = calculateF(results, 1, relevance_judgments, 0.5);
				Double f0_50_5 = calculateF(results, 5, relevance_judgments, 0.5);
				Double f0_50_10 = calculateF(results, 10, relevance_judgments, 0.5);
				
				
				Double averagePrecision = calculateAveragePrecision(results, relevance_judgments);
				
				Double ndcg_1 = calculateDCG(results, 1, relevance_judgments);
				Double ndcg_5 = calculateDCG(results, 5, relevance_judgments);
				Double ndcg_10 = calculateDCG(results, 10, relevance_judgments);
				
				Double reciprocalRank = calculateReciprocalRank(results, relevance_judgments);
				
				String output = query+"\t" + 
						Double.toString(precision_1) + "\t" + 
						Double.toString(precision_5) + "\t" + 
						Double.toString(precision_10) + "\t" + 
						Double.toString(recall_1) + "\t" + 
						Double.toString(recall_5) + "\t" + 
						Double.toString(recall_10) + "\t" + 
						Double.toString(f0_50_1) + "\t" + 
						Double.toString(f0_50_5) + "\t" + 
						Double.toString(f0_50_10) + "\t" +
						Double.toString(averagePrecision) + "\t" + 
						Double.toString(ndcg_1) + "\t" +
						Double.toString(ndcg_5) + "\t" +
						Double.toString(ndcg_10) + "\t" +
						Double.toString(reciprocalRank) + "\n";


				writeResultToFile(output, outputFileName);
				
				System.out.println(output);
				
			} finally {
				reader.close();
			}

		}catch(Exception e){
			e.printStackTrace();
			System.err.println("Error:" + e.getMessage());
		}

	}


	/**
	 * Calculates Precision
	 * */
	private static Double calculatePrecision(Vector<String> results, int k, 
			HashMap < String , HashMap < Integer , Double > > relevance_judgments){

		if(k == 0 || results.size() == 0) return 0.0;
		
		if(PrecisionRecord.containsKey(k)) return PrecisionRecord.get(k);
		
		double precision = 0.0;

		try{
			double RR = 0.0;
			String line = null;
			
			for(int i=0; i<k; i++){
				line = results.get(i);
				Scanner s = new Scanner(line).useDelimiter("\t");
				String query = s.next();
				int did = Integer.parseInt(s.next());
				String title = s.next();
				double rel = Double.parseDouble(s.next());
				if (relevance_judgments.containsKey(query) == false){
					throw new IOException("query not found");
				}
				HashMap < Integer , Double > qr = relevance_judgments.get(query);
				if (qr.containsKey(did) != false){
					RR += qr.get(did);					
				}
			}
			
			precision = RR/k;
			
			PrecisionRecord.put(k, RR);
			
		}catch (Exception e){
			System.err.println("Error:" + e.getMessage());
		}
		
		return precision;
	}


	/**
	 * Calculates the Recall at k
	 * */
	private static Double calculateRecall(Vector<String> results, int k, 
			HashMap < String , HashMap < Integer , Double > > relevance_judgments){

		if(k == 0 || results.size() == 0) return 0.0;

		if(RecallRecord.containsKey(k)) return RecallRecord.get(k);

		double recall = 0.0;

		try{

			double RR = 0.0;
			double R = 0.0;
			
			String line = results.get(0);

			Scanner s = new Scanner(line).useDelimiter("\t");
			String query = s.next();
			s.close();

			HashMap<Integer, Double> relevantJudgements = relevance_judgments.get(query);
			Set<Integer> documents = relevantJudgements.keySet();
			Iterator<Integer> iter = documents.iterator();

			while(iter.hasNext()){
				Integer did = iter.next();
				R += relevantJudgements.get(did);
			}

			for(int i=0; i<k; i++){
				line = results.get(i);
				s = new Scanner(line).useDelimiter("\t");
				query = s.next();
				int did = Integer.parseInt(s.next());
				String title = s.next();
				double rel = Double.parseDouble(s.next());
				
				if (relevance_judgments.containsKey(query) == false){
					throw new IOException("query not found");
				}
				
				HashMap < Integer , Double > qr = relevance_judgments.get(query);
				if (qr.containsKey(did) != false){
					RR += qr.get(did);					
				}
			}
			
			recall = RR/R;
			
			RecallRecord.put(k, recall);
			
		}catch (Exception e){
			System.err.println("Error:" + e.getMessage());
		}
		return recall;
	}

	/**
	 * Calculates the F-Measure at alpha
	 * */
	private static Double calculateF(Vector<String> results, int k, 
			HashMap < String , HashMap < Integer , Double > > relevance_judgments, Double alpha){
		
		Double precision = calculatePrecision(results, k, relevance_judgments);
		Double recall = calculateRecall(results, k, relevance_judgments);
		
		Double temp = (alpha*(1/precision) + (1-alpha)*(1/recall));
		
		return Math.pow(temp,-1);
	}
	
	
	/**
	 * Calculates Average Precision
	 * */
	private static Double calculateAveragePrecision(Vector<String> results, 
			HashMap < String , HashMap < Integer , Double > > relevance_judgments){
		
		if(results.size() == 0) return 0.0;
		double AP = 0.0;
		double RR = 0.0;
		
		try{
			String line = null;
			
			for(int i=0; i<results.size(); i++){
				
				line = results.get(i);
				Scanner s = new Scanner(line).useDelimiter("\t");
				String query = s.next();
				int did = Integer.parseInt(s.next());
				String title = s.next();
				double rel = Double.parseDouble(s.next());
				if (relevance_judgments.containsKey(query) == false){
					throw new IOException("query not found");
				}
				HashMap < Integer , Double > qr = relevance_judgments.get(query);
				if (qr.containsKey(did) != false){
					RR += qr.get(did);
					if(qr.get(did).equals(1.0)){
						AP += RR/(i+1); //+1 To avoid divide by 0 error;
					}
				}
				
			}
			
		}catch(Exception e){
			System.err.println("Error:" + e.getMessage());
		}
		
		return (AP/RR);
	}
	
	
	/**
	 * Calculates Reciprocal Rank
	 * */
	private static Double calculateReciprocalRank(Vector<String> results, 
			HashMap < String , HashMap < Integer , Double > > relevance_judgments){
		
		if(results.size() == 0) return 0.0;
		
		try{
			String line = null;
			
			for(int i=0; i<results.size(); i++){
				
				line = results.get(i);
				Scanner s = new Scanner(line).useDelimiter("\t");
				String query = s.next();
				int did = Integer.parseInt(s.next());
				String title = s.next();
				double rel = Double.parseDouble(s.next());
				if (relevance_judgments.containsKey(query) == false){
					throw new IOException("query not found");
				}
				HashMap < Integer , Double > qr = relevance_judgments.get(query);
				if (qr.containsKey(did) != false){
					if(qr.get(did).equals(1.0)){
						return 1.0/(i+1);
					}
				}
				
			}
			
		}catch(Exception e){
			System.err.println("Error:" + e.getMessage());
		}
		
		return 0.0;
	}
	
	
	/**
	 * Calculates DCG
	 * */
	private static Double calculateDCG(Vector<String> results, int k, 
			HashMap < String , HashMap < Integer , Double > > relevance_judgments){

		if(k == 0 || results.size() == 0) return 0.0;
		
		class VectorComparator implements Comparator<String>
		{
		    public int compare(String s1, String s2)
		    {	
		    	try{
			    	Scanner s11 = new Scanner(s1).useDelimiter("\t");
					String query1 = s11.next();
					int did1 = Integer.parseInt(s11.next());
					String title1 = s11.next();
					double rel1 = Double.parseDouble(s11.next());
					
					Scanner s22 = new Scanner(s2).useDelimiter("\t");
					String query2 = s22.next();
					int did2 = Integer.parseInt(s22.next());
					String title2 = s22.next();
					double rel2 = Double.parseDouble(s22.next());
					
					if (rel1 <= rel2) {
			            return -1;
			        } else {
			            return 1;
			        }
		    		}catch (Exception e){
		    			System.err.println("Error:" + e.getMessage());
		    		}
				return 0.0;
		        //return o1.message.compareTo(o2.message);
		    }
		}
		
		Vector<String> sortedResults = new Vector<String>(results);
		Comparator<String> comparator = new VectorComparator<String>();
		Collections.sort(sortedResults, comparator);
		
		
		double DCGMax = 0.0;

		try{
			
			String line = null;
			
			for(int i=0; i<k; i++){
				line = sortedResults.get(i);
				Scanner s = new Scanner(line).useDelimiter("\t");
				String query = s.next();
				int did = Integer.parseInt(s.next());
				String title = s.next();
				double rel = Double.parseDouble(s.next());
				if (relevance_judgments.containsKey(query) == false){
					throw new IOException("query not found");
				}
				HashMap < Integer , Double > qr = relevance_judgments.get(query);
				if (qr.containsKey(did) != false){
					DCGMax += (qr.get(did))/(Math.log(i+2)/Math.log(2));					
				}
			}
			
			
		}catch (Exception e){
			System.err.println("Error:" + e.getMessage());
		}
		
		
		
		double DCG = 0.0;

		try{
			
			String line = null;
			
			for(int i=0; i<k; i++){
				line = results.get(i);
				Scanner s = new Scanner(line).useDelimiter("\t");
				String query = s.next();
				int did = Integer.parseInt(s.next());
				String title = s.next();
				double rel = Double.parseDouble(s.next());
				if (relevance_judgments.containsKey(query) == false){
					throw new IOException("query not found");
				}
				HashMap < Integer , Double > qr = relevance_judgments.get(query);
				if (qr.containsKey(did) != false){
					DCG += (qr.get(did))/(Math.log(i+2)/Math.log(2));					
				}
			}
			
			
		}catch (Exception e){
			System.err.println("Error:" + e.getMessage());
		}
		
		if(DCGMax!=0)
			return DCG/DCGMax;
		else
			return 0.0;
	}

	
	
	/**
	 * Writes the evaluator results to the appropriate file in the results folder
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * */
	private static void writeResultToFile(String body,
			String outputFileName) throws IOException {

		if(outputFileName.isEmpty())
			return;

		OutputStream outputStream = null;
		Writer out = null;

		try{
			outputStream = new FileOutputStream("./results/"+outputFileName);
			out = new OutputStreamWriter(outputStream);
			out.write(body);
		}catch(FileNotFoundException fnfe){
			throw new FileNotFoundException("File Not Found : "+outputFileName+"\n");
		}finally{
			if(out != null)
				out.close();
			if(outputStream != null)
				outputStream.close();
		}

	}
	
	
	//Keeps the records of already calculated metrics
	private static Map<Integer, Double> PrecisionRecord = new HashMap<Integer, Double>();
	private static Map<Integer, Double> RecallRecord = new HashMap<Integer, Double>();
	
}
