package uk.ac.man.cs.ont;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.semanticweb.owlapi.search.EntitySearcher.getAnnotationObjects;

/**
 * Created by slava on 05/09/17.
 */
public class ClassFinder {

    private OWLOntology ontology;
    private File file;
    private Map<String, OWLClass> nameClassMap;

    public ClassFinder(OWLOntology ontology) {
        this.ontology = ontology;
        init();
    }
    
    public ClassFinder(OWLOntology ontology, File file) throws IOException{
    	 this.ontology = ontology;
    	 this.file = file;
         init(addAnnotation());
    }

    private void init() {
        nameClassMap = new HashMap<String, OWLClass>();
        for (OWLClass cl : ontology.getClassesInSignature(Imports.INCLUDED)) {       	
            nameClassMap.put(cl.getIRI().getShortForm().toLowerCase(), cl);
            Iterable<OWLAnnotation> annotations = getAnnotationObjects(cl, ontology);
            for (OWLAnnotation ann : annotations) {
                nameClassMap.put(ann.getValue().toString().toLowerCase(), cl);
            }
        }
    }
    
    private void init(Map<String,String> termWithLabel){
    	nameClassMap = new HashMap<String, OWLClass>();
        for (OWLClass cl : ontology.getClassesInSignature(Imports.INCLUDED)) { 
        	String name = cl.getIRI().getShortForm().toLowerCase();
			if(termWithLabel.keySet().contains(name)){
				nameClassMap.put(termWithLabel.get(name), cl);
			}
            nameClassMap.put(name, cl);
            Iterable<OWLAnnotation> annotations = getAnnotationObjects(cl, ontology);
            for (OWLAnnotation ann : annotations) {
                nameClassMap.put(ann.getValue().toString().toLowerCase(), cl);
            }
        }
    }
    
    private Map<String,String> addAnnotation() throws IOException{
    	CSVReader reader = new CSVReader(new FileReader(file));
		List<String[]> rows = reader.readAll();
		Map<String,String> termWithLabel = new HashMap();
		for(String[] row:rows){
			String name = IRI.create(row[0]).getShortForm().toLowerCase();
			termWithLabel.put(name, row[1]);
		}	
		return termWithLabel;
    }


    public OWLOntology getOntology() {
        return ontology;
    }


    public OWLClass find(String className) {
        if (nameClassMap.containsKey(className)) {
            return nameClassMap.get(className);
        }
        return findShortest(className);
    }


    private OWLClass findShortest(String className) {
        int minLength = Integer.MAX_VALUE;
        OWLClass shortestClass = null;
        for (String name : nameClassMap.keySet()) {
            if (name.length() < minLength && name.contains(className)) {
                minLength = name.length();
                shortestClass = nameClassMap.get(name);
            }
        }
        return shortestClass;
    }


    public boolean contains(String term) {
        return find(term) != null;
    }
}
