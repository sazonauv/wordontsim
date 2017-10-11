package uk.ac.man.cs.exp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.semanticweb.owlapi.model.OWLOntology;

import com.opencsv.CSVWriter;

import uk.ac.man.cs.io.CoverageData;
import uk.ac.man.cs.io.TermDataLoader;
import uk.ac.man.cs.io.TermPair;
import uk.ac.man.cs.ont.ClassFinder;
import uk.ac.man.cs.ont.OntologyLoader;
import uk.ac.man.cs.sim.SimilarityCalculator;
//import wordontsim.RemoveOntologyRule;
import uk.ac.man.cs.sim.RemoveOntologyRule;

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
		File reasonerTestFile = new File(analogyFile.getParentFile(), "ontology_reasoner_test.csv");
		bioSimExp.calculateSimilarityScores(ontDir, coverageFile, reasonerTestFile);
	}

	public BioportalSimilarityExperiment(Set<TermPair> termPairs) {
		this.termPairs = termPairs;
	}
	
	private void calculateSimilarityScores(File ontDir, File coverageFile, File reasonerTestFile) throws IOException {

		Map<String, String> coverageData = new CoverageData(coverageFile).getCoverageData();		
		Map<String, String> reasonerData = new CoverageData(reasonerTestFile).getCoverageData();
		File resultCSV = new File(coverageFile.getParent(), "bioportal_calculate_similarity.csv");
	    	
		List<String> row = new ArrayList<>();
		Set<String> terms = new HashSet<>();
		row.add("ontologies");
		for(TermPair pair : termPairs){
			row.add(pair.toString());
			terms.add(pair.first);
			terms.add(pair.second);
		}  			   	
	    	
		CSVWriter writer = new CSVWriter(new FileWriter(resultCSV));
		writer.writeNext(row.toArray(new String[row.size()]));	    	
	  
		for (File ontFile : ontDir.listFiles()) {
			
			//List<String> scores = new ArrayList<>();
			//scores.add(ontFile.getName());	    		
			log.info("checking "+ontFile.getName()+ "with" +coverageData.get(ontFile.getName())+"terms\n");			
			if(!coverageData.get(ontFile.getName()).equals("0")){				
				if(!reasonerData.get(ontFile.getName()).equals("yes")
						&&!reasonerData.get(ontFile.getName()).equals("big ontology with terms")						
						&&!ontFile.getName().equals("galen.galen-ontology.1.owl.xml")						
						&&!ontFile.getName().equals("ogdi.ontology-for-genetic-disease-investigations.3.owl.xml")
						){
					System.out.println(ontFile.getName());
					List<String> scores = new ArrayList<>();
					scores.add(ontFile.getName());	
					OntologyLoader loader = new OntologyLoader(ontFile, true);
					ClassFinder finder = new ClassFinder(loader.getOntology());
					SimilarityCalculator sim = new SimilarityCalculator(finder);
					sim.contains(terms);
					for(TermPair pair : termPairs){
							
						String score = String.valueOf(sim.computeScore(pair.first, pair.second));
						log.info(String.valueOf(sim.score(pair.first, pair.second))
								+" "+pair.first+" "+pair.second+"\n");
						scores.add(score);   					       
					}					
					writer.writeNext(scores.toArray(new String[scores.size()]));  
				}				
			}
			
			//test ontologies with reasoning problem
			/*if(!coverageData.get(ontFile.getName()).equals("0")){
				if(reasonerData.get(ontFile.getName()).equals("yes")
					&&!ontFile.getName().equals("bof.biodiversity-ontology.2.owl.xml")
					&&!ontFile.getName().equals("natpro.natural-products-ontology.1.owl.xml")
					){
				
					System.out.println(ontFile.getName());
					List<String> scores = new ArrayList<>();
					scores.add(ontFile.getName());	
					OntologyLoader loader = new OntologyLoader(ontFile, true);
					OWLOntology ontology = loader.getOntology();
					new RemoveOntologyRule(ontology).removeAll();;
					ClassFinder finder = new ClassFinder(ontology);
					SimilarityCalculator sim = new SimilarityCalculator(finder);
					sim.contains(terms);
					for(TermPair pair : termPairs){
							
						String score = String.valueOf(sim.computeScore(pair.first, pair.second));
						log.info(String.valueOf(sim.score(pair.first, pair.second))
								+" "+pair.first+" "+pair.second+"\n");
						scores.add(score);   					       
					}					
					writer.writeNext(scores.toArray(new String[scores.size()]));  
				}	
			}*/	
			
		} 
		writer.close();
		log.info("Finish loop..");
	}	

}

