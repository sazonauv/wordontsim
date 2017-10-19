package uk.ac.man.cs.sim;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.validator.PublicClassValidator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.parameters.Imports;

import com.opencsv.CSVReader;

import uk.ac.man.cs.ont.OntologyLoader;

public class MakeAnnotaionOWL {
	private static OWLOntology ontology;
	private static Map<String,String> termWithLabel;
	private static String downloadOntPath = "";
	
	public static void main(String[] args) throws IOException, OWLOntologyCreationException, OWLOntologyStorageException{
		File annoFile = new File(args[0]);
		File ontFile1 = new File(args[1]);
		OntologyLoader loader = new OntologyLoader(ontFile1, true);
		OWLOntology ontology = loader.getOntology();
		MakeAnnotaionOWL makeAnnotation = new MakeAnnotaionOWL(ontology);
		makeAnnotation.makeAnnotation(annoFile,ontology);		
	}
	
	public MakeAnnotaionOWL(OWLOntology ontology){
		this.ontology = ontology;
	}
	
	public void makeAnnotation(File file, OWLOntology ontology) throws IOException, OWLOntologyCreationException, OWLOntologyStorageException{
		getLabel(file);
		addAnnotation();
	}
	
	private void getLabel(File file) throws IOException{
		CSVReader reader = new CSVReader(new FileReader(file));
		List<String[]> rows = reader.readAll();
		termWithLabel = new HashMap();
		for(String[] row:rows){
			String name = IRI.create(row[0]).getShortForm();
			termWithLabel.put(name, row[1]);
		}		
	}
	
	private void addAnnotation() throws OWLOntologyCreationException, OWLOntologyStorageException{
		File download = new File(downloadOntPath);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		for(OWLClass owlclass : ontology.getClassesInSignature(Imports.INCLUDED)){
			String name = owlclass.getIRI().getShortForm();
			if(termWithLabel.keySet().contains(name)){
				OWLAnnotation annotation = dataFactory.getOWLAnnotation(dataFactory.getRDFSComment(), dataFactory.getOWLLiteral(
						termWithLabel.get(name), "rdfs:label"));
				OWLAxiom axiom = dataFactory.getOWLAnnotationAssertionAxiom(owlclass.getIRI(), annotation);
				manager.applyChange(new AddAxiom(ontology, axiom));
			}
		}
		OWLOntology newOntology = ontology;
		OWLXMLDocumentFormat owlxmlFormat = new OWLXMLDocumentFormat();
		manager.saveOntology(newOntology,owlxmlFormat, IRI.create(download.toURI()));
	}
}
