package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
					if ((grade.equals("Perfect")) ||
							(grade.equals("Excellent")) ||
							(grade.equals("Good"))){
						rel = 1.0;
					}
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

				String output = query+"\t" + 
						Double.toString(precision_1) + "\t" + 
						Double.toString(precision_5) + "\t" + 
						Double.toString(precision_10) + "\t" + 
						Double.toString(recall_1) + "\t" + 
						Double.toString(recall_5) + "\t" + 
						Double.toString(recall_10) + "\t" + 
						Double.toString(f0_50_1) + "\t" + 
						Double.toString(f0_50_5) + "\t" + 
						Double.toString(f0_50_10) + "\n";


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
	 * Calculates the Recall at k
	 * */
	private static Double calculateF(Vector<String> results, int k, 
			HashMap < String , HashMap < Integer , Double > > relevance_judgments, Double alpha){
		
		Double precision = calculatePrecision(results, k, relevance_judgments);
		Double recall = calculateRecall(results, k, relevance_judgments);
		
		Double temp = (alpha*(1/precision) + (1-alpha)*(1/recall));
		
		return Math.pow(temp,-1);
	}
	
	//Keeps the records of already calculated metrics
	private static Map<Integer, Double> PrecisionRecord = new HashMap<Integer, Double>();
	private static Map<Integer, Double> RecallRecord = new HashMap<Integer, Double>();
	
}
