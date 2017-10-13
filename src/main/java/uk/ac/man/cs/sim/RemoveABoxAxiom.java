package uk.ac.man.cs.sim;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.opencsv.CSVWriter;

import uk.ac.man.cs.ont.OntologyLoader;
import uk.ac.man.cs.io.TermDataLoader;
import uk.ac.man.cs.io.TermPair;
import uk.ac.man.cs.ont.ClassFinder;

public class RemoveABoxAxiom {
	OWLOntology ontology;
	private static final Logger log = Logger.getLogger(String.valueOf(ExtractPairsFromBigOnt.class));
	private static String analogies = "owlfile/Analogies.csv";
	private static String relatedness = "owlfile/UMNSRS_relatedness.csv";
	private static String similarity = "owlfile/UMNSRS_similarity.csv";
	private static List<File> files;
	
	static{
		files = new ArrayList();
		File ontFile1 = new File("owlfile/owlxml/bof.biodiversity-ontology.2.owl.xml");
		File ontFile2 = new File("owlfile/owlxml/ogdi.ontology-for-genetic-disease-investigations.3.owl.xml");
		File ontFile3 = new File("owlfile/owlxml/natpro.natural-products-ontology.1.owl.xml");
		files.add(ontFile1);
		files.add(ontFile2);
		files.add(ontFile3);
	}
	
	public static void main(String[] args) throws IOException, OWLOntologyCreationException{
		File analogyFile = new File(analogies);
		File relatednessFile = new File(relatedness);
		File similarityFile = new File(similarity);
		
		TermDataLoader tdLoader = new TermDataLoader(analogyFile, relatednessFile, similarityFile);		
		Set<TermPair> allPairs = tdLoader.getAllPairs();
		List<String> row = new ArrayList<>();
		row.add("ontologies");
		Set<String> terms = new HashSet<>();
		for(TermPair pair : allPairs){
			row.add(pair.toString());
			terms.add(pair.first);
			terms.add(pair.second);
		} 
		
		File result = new File(analogyFile.getParentFile(), "removeABox_result.csv"); 
		CSVWriter writer = new CSVWriter(new FileWriter(result));
		writer.writeNext(row.toArray(new String[row.size()]));
		
		for(File file:files){
			OntologyLoader loader = new OntologyLoader(file, true);
			OWLOntology ontology = loader.getOntology();
			OWLOntology newOntology = new RemoveABoxAxiom(ontology).remove();
			
			ClassFinder finder = new ClassFinder(newOntology);				
			List<String> scores = new ArrayList();
			
			scores.add(file.getName());
			SimilarityCalculator sim = new SimilarityCalculator(finder, SimilarityType.ABOX);
			
			log.info("begin check contain...");
			sim.contains(terms);
			log.info("finish checking");
			Map<String, OWLClass> nameClassMap = sim.getNameClassMap();	
			for(TermPair pair : allPairs){
				if(nameClassMap.keySet().contains(pair.first)&&nameClassMap.keySet().contains(pair.second)){
					log.info("begin extract...");
					log.info("finish extract...");
					String score = String.valueOf(sim.computeModuleScore(pair.first, pair.second, newOntology));
					log.info(String.valueOf(sim.computeModuleScore(pair.first, pair.second, newOntology))
							+" "+pair.first+" "+pair.second+"\n");
					scores.add(score);
				}
				else{
					log.info("without values");
					scores.add("0");
				}
			}
			writer.writeNext(scores.toArray(new String[scores.size()]));
			
		}
		writer.close();
	}
	
	public RemoveABoxAxiom(OWLOntology ontology){
		this.ontology = ontology;
	}
	
	public OWLOntology remove() throws OWLOntologyCreationException{
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology newOntology = m.createOntology();
		
		for(OWLAxiom axiom:ontology.getTBoxAxioms(Imports.INCLUDED)){
			AddAxiom addAxiom = new AddAxiom(newOntology, axiom);
	        m.applyChange(addAxiom);
		}
		return newOntology;
	}
	
}
