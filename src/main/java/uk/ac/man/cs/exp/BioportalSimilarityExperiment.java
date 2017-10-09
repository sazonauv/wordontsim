package uk.ac.man.cs.exp;

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
import uk.ac.man.cs.io.Out;
import uk.ac.man.cs.io.TermDataLoader;
import uk.ac.man.cs.io.TermPair;
import uk.ac.man.cs.ont.ClassFinder;
import uk.ac.man.cs.ont.OntologyLoader;
import uk.ac.man.cs.sim.SimilarityCalculator;


public class BioportalSimilarityExperiment {
	
	private static final Logger log = Logger.getLogger(String.valueOf(BioportalSimilarityExperiment.class));

	private Set<TermPair> termPairs;



	public static void main(String[] args) throws IOException {
		File ontDir = new File(args[0]);
		File analogyFile = new File(args[1]);
		File relatednessFile = new File(args[2]);
		File similarityFile = new File(args[3]);
		TermDataLoader tdLoader = new TermDataLoader(analogyFile, relatednessFile, similarityFile);
		BioportalSimilarityExperiment bioSimExp = new BioportalSimilarityExperiment(tdLoader.getAllPairs());
		File coverageFile = new File(analogyFile.getParentFile(), "bioportal_coverage_distr.csv");
		bioSimExp.calculateSimilarityScores(ontDir, coverageFile);
	}

	public BioportalSimilarityExperiment(Set<TermPair> termPairs) {
		this.termPairs = termPairs;
	}
	
	private void calculateSimilarityScores(File ontDir, File coverageFile) throws IOException {

		Map<String, String> coverageData = getCoverageData(coverageFile);
	    	
		File resultCSV = new File(coverageFile.getParent(), "bioportal_calculate_similarity.csv");
	    	
		List<String> row = new ArrayList<>();
		Set<String> terms = new HashSet<>();
		row.add("ontologies");
		for(TermPair pair : termPairs){
			String pairStr = pair.first +" && "+ pair.second;
			row.add(pairStr);
			terms.add(pair.first);
			terms.add(pair.second);
		}  
		Out.p(termPairs.size());
		Out.p(terms.size());   	   	
	    	
		CSVWriter writer = new CSVWriter(new FileWriter(resultCSV));
		writer.writeNext(row.toArray(new String[row.size()]));	    	
	  
		for (File ontFile : ontDir.listFiles()) {
			Out.p(coverageData.get(ontFile.getName()));
			List<String> scores = new ArrayList<>();
			scores.add(ontFile.getName());
	    		
			log.info("checking "+ontFile.getName()+"\n");
			OntologyLoader loader = new OntologyLoader(ontFile, true);
			ClassFinder finder = new ClassFinder(loader.getOntology());
			SimilarityCalculator sim = new SimilarityCalculator(finder);
			sim.contains(terms);
			if(coverageData.get(ontFile.getName()).equals("0")){
				for(TermPair pair : termPairs){
					scores.add("0.0"); 
				}
			}
			else{
				for(TermPair pair : termPairs){
					String score = String.valueOf(sim.computeScore(pair.first, pair.second));
					log.info(String.valueOf(sim.score(pair.first, pair.second))
							+" "+pair.first+" "+pair.second+"\n");
					scores.add(score);   					       
				} 
			}
			writer.writeNext(scores.toArray(new String[scores.size()]));  		
		} 
		writer.close();
		log.info("Finish loop..");
	}


	private static Map<String, String> getCoverageData(File coverageFile) throws IOException {
		Map<String, String> coverageData = new HashMap<>();
		CSVReader reader = new CSVReader(new FileReader(coverageFile));
		List<String[]> rows = reader.readAll();
		rows.remove(rows.get(0));
		for (String[] row : rows) {
			coverageData.put(row[0], row[1]);
		}
		return coverageData;
	}
	

}
