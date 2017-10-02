package sim;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.util.HashSet;
import java.util.Set;

import static org.semanticweb.owlapi.search.EntitySearcher.getAnnotationObjects;

/**
 * Created by slava on 05/09/17.
 */
public class ClassFinder {

    private OWLOntology ontology;

    private Set<String> classNames;
    private Set<String> classAnnotations;

    public ClassFinder(OWLOntology ontology) {
        this.ontology = ontology;
        init();
    }

    private void init() {
        classNames = new HashSet<>();
        classAnnotations = new HashSet<>();
        for (OWLClass cl : ontology.getClassesInSignature(Imports.INCLUDED)) {
            String name = cl.getIRI().getShortForm().toLowerCase();
            classNames.add(name);
            Iterable<OWLAnnotation> annotations = getAnnotationObjects(cl, ontology);
            for (OWLAnnotation ann : annotations) {
                classAnnotations.add(ann.getValue().toString().toLowerCase());
            }
        }
    }


    public boolean contains(String className) {
        if (classNames.contains(className)) {
            return true;
        }
        for (String name : classNames) {
            if (name.contains(className)) {
                return true;
            }
        }
        if (classAnnotations.contains(className)) {
            return true;
        }
        for (String ann : classAnnotations) {
            if (ann.contains(className)) {
                return true;
            }
        }
        return false;
    }

}
