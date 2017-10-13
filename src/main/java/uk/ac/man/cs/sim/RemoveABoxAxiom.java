package uk.ac.man.cs.sim;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;

public class RemoveABoxAxiom {
	OWLOntology ontology;
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
