package uk.ac.man.cs.sim;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import uk.ac.man.cs.ont.ClassFinder;
import uk.ac.man.cs.ont.ReasonerLoader;
import uk.ac.man.cs.ont.ReasonerName;

import java.util.*;

/**
 * Created by slava on 03/10/17.
 */
public class SimilarityCalculator {
    private Map<String, OWLClass> nameClassMap;
    private ClassFinder finder;

    private OWLReasoner reasoner;


    public SimilarityCalculator(ClassFinder finder) {
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

    public void contains(Collection<String> terms){
    	nameClassMap = new HashMap();
    	for(String term : terms){
    		OWLClass owlClass = finder.find(term);   		
    		if(owlClass != null){
    			nameClassMap.put(term, owlClass);
    		}
    	}
    }
    
    public double computeScore(String term1, String term2){
    	if(nameClassMap.keySet().contains(term1)&&nameClassMap.keySet().contains(term2)){
    		return computeScore(nameClassMap.get(term1), nameClassMap.get(term2));
    	}   	
    	return 0;
    }
    
    public double score(String term1, String term2) {
        OWLClass cl1 = finder.find(term1);
        OWLClass cl2 = finder.find(term2);
        if (cl1 == null || cl2 == null) {
            return 0;
        }
        return computeScore(cl1, cl2);
    }


    private double computeScore(OWLClass cl1, OWLClass cl2) {
        Set<OWLClass> subsumers1 = reasoner.getSuperClasses(cl1, false).getFlattened();
        Set<OWLClass> subsumers2 = reasoner.getSuperClasses(cl2, false).getFlattened();
        return countIntersection(subsumers1, subsumers2) / countUnion(subsumers1, subsumers2);
    }


    private static double countIntersection(Set<?> set1, Set<?> set2) {
        double intersect = 0;
        // should be faster for HashSet
        if (set1.size() <= set2.size()) {
            for (Object o : set1) {
                if (set2.contains(o)) {
                    intersect++;
                }
            }
        } else {
            for (Object o : set2) {
                if (set1.contains(o)) {
                    intersect++;
                }
            }
        }
        return intersect;
    }


    private static double countUnion(Set<?> set1, Set<?> set2) {
        return set1.size() + set2.size() - countIntersection(set1, set2);
    }




}
