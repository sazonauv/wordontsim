package uk.ac.man.cs.sim;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import wordontsim.TermDataLoader;

public class BioportalSimilarityExperiment {
	
	private static final Logger log = Logger.getLogger(String.valueOf(BioportalSimilarityExperiment.class));
	private static Set<String[]> allPairs;
	
	public static void main(String[] args) throws IOException {
		 File ontDir = new File(args[0]);
		 File analogyFile = new File(args[1]);
		 File relatednessFile = new File(args[2]);
		 File similarityFile = new File(args[3]);
		 
		 BioportalSimilarityExperiment simTest = new BioportalSimilarityExperiment(new TermDataLoader().getAllPairs(analogyFile, relatednessFile, similarityFile));
		 simTest.calculateSimilarityScores(ontDir,analogyFile);
	}
	
	public BioportalSimilarityExperiment(Set<String[]> allPairs){
		this.allPairs = allPairs;
	}
	
	private static void calculateSimilarityScores(File ontDir,File analogyFile) throws IOException{
	    	
		File csvDir = analogyFile.getParentFile();
		File coverage = new File(csvDir.getAbsolutePath()+"/bioportal_coverage_distr.csv");
		Map<String, String> coverData = getCoverData(coverage);
	    	
		File resultCSV = new File(csvDir, "bioportal_calculate_similarity.csv");       
	    	
		List<String> row = new ArrayList<>();
		Set<String> terms = new HashSet<>();
		row.add("ontologies");
		clear();
		for(String[] pairs : allPairs ){
			String pair = pairs[0] +" && "+ pairs[1];
			row.add(pair);   	
			terms.add(pairs[0]);
			terms.add(pairs[1]);
		}  
		Out.p(allPairs.size());
		Out.p(terms.size());   	   	
	    	
		CSVWriter writer = new CSVWriter(new FileWriter(resultCSV));
		writer.writeNext(row.toArray(new String[row.size()]));	    	
	  
		for (File ontFile : ontDir.listFiles()) {
			Out.p(coverData.get(ontFile.getName()));
			List<String> scores = new ArrayList<>();
			scores.add(ontFile.getName());
	    		
			log.info("checking "+ontFile.getName()+"\n");
			OntologyLoader loader = new OntologyLoader(ontFile, true);
			ClassFinder finder = new ClassFinder(loader.getOntology());  
			SimilarityCalculator sim = new SimilarityCalculator(finder);
			sim.contains(new ArrayList<>(terms));
			if(coverData.get(ontFile.getName()).equals("0")){
				for(String[] pairs : allPairs ){
					scores.add("0.0"); 
				}
			}
			else{
				for(String[] pairs : allPairs ){        			
					String score = String.valueOf(sim.computeScore(pairs[0], pairs[1]));
					log.info(String.valueOf(sim.score(pairs[0], pairs[1]))+" "+pairs[0]+" "+pairs[1]+"\n");
					scores.add(score);   					       
				} 
			}
			writer.writeNext(scores.toArray(new String[scores.size()]));  		
		} 
		writer.close();
	        	
	    		
		log.info("Finish loop..");
	        
	}
	private static Map<String, String> getCoverData(File coverage) throws IOException{
		Map<String, String> coverData = new HashMap<>();
		CSVReader reader = new CSVReader(new FileReader(coverage));
		List<String[]> rows = reader.readAll();
		rows.remove(rows.get(0));
		for (String[] row : rows) {
			coverData.put(row[0],row[1]);   		
		}
		return coverData;
	}
	
	private static void clear(){
    	Set<String> allpair = new HashSet<>();
    	for(String[] s : allPairs){
    		allpair.add(s[0]+" "+s[1]);
    	}
    	allPairs.clear();
    	for(String s1 : allpair){
    		String[] s2= {s1.substring(0, s1.indexOf(" ")),s1.substring(s1.indexOf(" ")+1)};
    		allPairs.add(s2);
    	}   	
    } 
}
