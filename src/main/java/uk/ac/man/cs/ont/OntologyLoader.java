package uk.ac.man.cs.ont;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.util.AutoIRIMapper;

import java.io.File;

/**
 * Created by slava on 05/09/17.
 */
public class OntologyLoader {

    private OWLOntology ontology;
    private OWLOntologyManager manager;

    public OntologyLoader(File file, boolean includeImports) {
        if (includeImports) {
            loadOntologyWithImports(file);
        } else {
            loadOntology(file);
        }
    }

    private void loadOntology(File ontFile) {
        manager = OWLManager.createOWLOntologyManager();
        ontology = null;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(ontFile);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }

    private void loadOntologyWithImports(File ontFile) {
        manager = OWLManager.createOWLOntologyManager();
        AutoIRIMapper mapper = new AutoIRIMapper(ontFile.getParentFile(), true);
        OWLOntologyManager tempManager = OWLManager.createOWLOntologyManager();
        tempManager.addIRIMapper(mapper);
        try {
            OWLOntology o = tempManager.loadOntologyFromOntologyDocument(ontFile);
            // include all imports
            ontology = manager.createOntology(o.getAxioms(Imports.INCLUDED));
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }


    public OWLOntology getOntology() {
        return ontology;
    }
}
