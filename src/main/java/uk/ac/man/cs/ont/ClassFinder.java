package uk.ac.man.cs.ont;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.util.HashMap;
import java.util.Map;

import static org.semanticweb.owlapi.search.EntitySearcher.getAnnotationObjects;

/**
 * Created by slava on 05/09/17.
 */
public class ClassFinder {

    private OWLOntology ontology;

    private Map<String, OWLClass> nameClassMap;

    public ClassFinder(OWLOntology ontology) {
        this.ontology = ontology;
        init();
    }

    private void init() {
        nameClassMap = new HashMap<>();
        for (OWLClass cl : ontology.getClassesInSignature(Imports.INCLUDED)) {
            nameClassMap.put(cl.getIRI().getShortForm().toLowerCase(), cl);
            Iterable<OWLAnnotation> annotations = getAnnotationObjects(cl, ontology);
            for (OWLAnnotation ann : annotations) {
                nameClassMap.put(ann.getValue().toString().toLowerCase(), cl);
            }
        }
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
