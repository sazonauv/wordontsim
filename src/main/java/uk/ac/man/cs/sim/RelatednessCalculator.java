package uk.ac.man.cs.sim;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.Set;

/**
 * Created by chris on 03/10/17.
 */
public class RelatednessCalculator {

    private ClassFinder finder;

    private OWLReasoner reasoner;


    public RelatednessCalculator(ClassFinder finder) {
        this.finder = finder;
        loadReasoner();
    }

    private void loadReasoner() {
        try {
            reasoner = ReasonerLoader.initReasoner(finder.getOntology());
            reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean related(String term1, String term2, boolean classesOnly)
    {
        OWLClass cl1 = finder.find(term1);
        OWLClass cl2 = finder.find(term2);
        boolean res = false;

        if(cl1 == null || cl2 == null)
            return res;

        if(classesOnly)
        {
            Set<OWLClass> superclasses = reasoner.getSuperClasses(cl1, false).getFlattened();
            Set<OWLClass> subclasses = reasoner.getSubClasses(cl1, false).getFlattened();

            if(superclasses != null)
                res = superclasses.contains(cl2);

            if(subclasses != null)
                res = res || subclasses.contains(cl2);
        }

        if(!classesOnly)
        {
            //TODO: consider annodations, datatypse etc.
            return false;
        }

        return res;
    }
}
