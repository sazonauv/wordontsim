//package wordontsim;
package uk.ac.man.cs.sim;
import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;

import uk.ac.man.cs.ont.OntologyLoader;
import uk.ac.man.cs.io.Out;

public class RemoveOntologyRule {
	private OWLOntology ontology;
	
	public static void main(String[] args){
		File ontFile = new File(args[1]);    	
    	OntologyLoader loader = new OntologyLoader(ontFile, true);
    	RemoveOntologyRule test = new RemoveOntologyRule(loader.getOntology());
    	test.removeAll();
	}
	
	public RemoveOntologyRule(OWLOntology ontology){
		this.ontology = ontology;
	}
	
	public void removeAll(){
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		//Out.p(ontology.getAxioms().size());
		for(OWLAxiom owlAxiom : ontology.getAxioms()){
			
			if(owlAxiom.toString().contains("xsd")||owlAxiom.getAxiomType().toString().equals("Rule")){
				RemoveAxiom removeAxiom = new RemoveAxiom(ontology, owlAxiom);
		        m.applyChange(removeAxiom);
			}
			
		}
		Out.p("finish romoveing ruls..");
	}
	
	public void removeDatatypesRule(){
		for(OWLAxiom owlAxiom : ontology.getAxioms()){
			if(owlAxiom.toString().contains("xsd")){
				Out.p(owlAxiom);
			}				
		}
	}
	public void removeSWRLRule(){
		for(OWLAxiom owlAxiom : ontology.getAxioms()){
			if(owlAxiom.getAxiomType().toString().equals("Rule")){
				Out.p(owlAxiom);
			}				
		}			
	}
	public void printAxiomType(){
		Set<String> axiomTypes = new HashSet();
		for(OWLAxiom owlAxiom : ontology.getAxioms()){
			axiomTypes.add(owlAxiom.getAxiomType().toString());				
		}
		for(String axiomType:axiomTypes){
			Out.p(axiomType);
		}	
	}
}
