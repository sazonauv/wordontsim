//package wordontsim;
package uk.ac.man.cs.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.opencsv.CSVWriter;

import uk.ac.man.cs.ont.OntologyLoader;
import uk.ac.man.cs.io.Out;
import uk.ac.man.cs.ont.ReasonerLoader;
import uk.ac.man.cs.ont.ReasonerName;
import uk.ac.man.cs.io.CoverageData;

public class OntologyForm {

	private static OWLReasoner reasoner;
	public static void main(String[] args) throws IOException{
		File ontDir = new File(args[0]);
		File analogyFile = new File(args[1]);
		File coverageFile = new File(analogyFile.getParent(), "bioportal_coverage_distr.csv");
		File resultCSV = new File(analogyFile.getParentFile(), "ontology_reasoner_test.csv"); 
		
		checkException(ontDir,coverageFile,resultCSV);
	}
	private static void checkException(File ontDir, File coverageFile, File resultCSV) throws IOException{
		CSVWriter writer = new CSVWriter(new FileWriter(resultCSV));
		String[] header = {"ontology","Hermit","Pellet"};
		writer.writeNext(header);
		int i=0;
		for (File ontFile : ontDir.listFiles()) {
			Out.p(ontFile+" "+i);
			//File ontFile = new File("owlfile/owlxml/aura.kb_bio_101.1.owl.xml");
			List<String> row = new ArrayList<>();
			row.add(ontFile.getName());
			Map<String, String> coverageData = new CoverageData(coverageFile).getCoverageData();
			if(!coverageData.get(ontFile.getName()).equals("0")){
				if((ontFile.length()/1024)<45000){				
					OntologyLoader loader = new OntologyLoader(ontFile, true);
		    		OWLOntology ontology = loader.getOntology();	    		
		    		loadReasoner(ontology, row);	    		
		    		writer.writeNext(row.toArray(new String[row.size()]));
		    		
		    		Out.p(ontFile+" "+i);
				}
				else{
					row.add("big ontology with terms");
					writer.writeNext(row.toArray(new String[row.size()]));
				}
			}						
		}	
		writer.close();
		Out.p("finish looping...");
	}
	
	private static void loadReasoner(OWLOntology ontology, List<String> row) {
		
        try {       	
            reasoner = ReasonerLoader.initReasoner(ReasonerName.HERMIT, ontology);
            
        } catch (Exception e) {
        	 //Out.p("Hermeit makes error");
        	 row.add("yes");
        	
        	try {
        		reasoner = ReasonerLoader.initReasoner(ReasonerName.PELLET, ontology);               
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				//e1.printStackTrace();
				//Out.p("Pellet makes error");
				row.add("yes");
			}
            //e.printStackTrace();
           
        }        
    
    }
}
