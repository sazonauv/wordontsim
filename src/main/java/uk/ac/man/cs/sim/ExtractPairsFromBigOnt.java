package uk.ac.man.cs.sim;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.opencsv.CSVWriter;
import java.util.List;
import java.util.Map;

import uk.ac.man.cs.ont.OntologyLoader;
import uk.ac.man.cs.io.TermDataLoader;
import uk.ac.man.cs.io.TermPair;
import uk.ac.man.cs.ont.ClassFinder;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class ExtractPairsFromBigOnt {
	private static final Logger log = Logger.getLogger(String.valueOf(ExtractPairsFromBigOnt.class));
	private static OWLOntology newOntology;
	private static String analogies = "owlfile/Analogies.csv";
	private static String relatedness = "owlfile/UMNSRS_relatedness.csv";
	private static String similarity = "owlfile/UMNSRS_similarity.csv";
	private static List<File> bigOntFiles;
	
	static{
		bigOntFiles = new ArrayList();
		File ontFile1 = new File("owlfile/owlxml/aura.kb_bio_101.1.owl.xml");
		File ontFile2 = new File("owlfile/owlxml/biomodels.biomodels-ontology.3.owl.xml");
		File ontFile3 = new File("owlfile/owlxml/pxo.proteasix-ontology.10.owl.xml");
		File ontFile4 = new File("owlfile/owlxml/galen.galen-ontology.1.owl.xml");
		bigOntFiles.add(ontFile1);
		//bigOntFiles.add(ontFile2);
		//bigOntFiles.add(ontFile3);
		bigOntFiles.add(ontFile4);
	}
	
	public static void main(String[] args) throws IOException, OWLOntologyCreationException{
		
		File analogyFile = new File(analogies);
		File relatednessFile = new File(relatedness);
		File similarityFile = new File(similarity);
		
		File bigOntScore = new File(analogyFile.getParentFile(), "bigOntScore.csv");
		CSVWriter writer = new CSVWriter(new FileWriter(bigOntScore));
				
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
		writer.writeNext(row.toArray(new String[row.size()]));	  
		
		for(File file : bigOntFiles){
			OntologyLoader loader = new OntologyLoader(file, true);
			OWLOntology ontology = loader.getOntology();
			OWLOntology newOntology = new RemoveABoxAxiom(ontology).remove();
			System.out.println(ontology.getAxioms().size());
			System.out.println(newOntology.getAxioms().size());
			ClassFinder finder = new ClassFinder(newOntology);	
			
			List<String> scores = new ArrayList();
			scores.add(file.getName());
			
			SimilarityCalculator sim = new SimilarityCalculator(finder, SimilarityType.MODULE);
			log.info("begin check contain...");
			sim.contains(terms);
			log.info("finish checking");
			Map<String, OWLClass> nameClassMap = sim.getNameClassMap();
			//List size = new ArrayList<>();
			for(TermPair pair : allPairs){
				if(nameClassMap.keySet().contains(pair.first)&&nameClassMap.keySet().contains(pair.second)){
					log.info("begin extract...");
					ExtractPairsFromBigOnt extractPairsFromBigOnt = new ExtractPairsFromBigOnt(newOntology, pair, nameClassMap);
					log.info("finish extract...");
					System.out.println(extractPairsFromBigOnt.getNewOntology().getAxioms().size());
					//size.add(extractPairsFromBigOnt.getNewOntology().getAxioms().size());
					String score = String.valueOf(sim.computeModuleScore(pair.first, pair.second, extractPairsFromBigOnt.getNewOntology()));
					log.info(String.valueOf(sim.computeModuleScore(pair.first, pair.second, extractPairsFromBigOnt.getNewOntology()))
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
	
	public ExtractPairsFromBigOnt(OWLOntology ontology, TermPair pair, Map<String, OWLClass> nameClassMap) throws OWLOntologyCreationException{
		SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(ontology.getOWLOntologyManager(), ontology, ModuleType.BOT);
		Set<OWLEntity> entities = new HashSet();
		entities.addAll(nameClassMap.get(pair.first).getSignature());
		entities.addAll(nameClassMap.get(pair.second).getSignature());
		
		
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		newOntology = m.createOntology();		
		Set<OWLAxiom> axioms = extractor.extract(entities);
		for(OWLAxiom axiom:axioms){
			AddAxiom addAxiom = new AddAxiom(newOntology, axiom);
	        m.applyChange(addAxiom);
		}
	}
	
	public OWLOntology getNewOntology(){
		return newOntology;
	}
}
