package uk.ac.man.cs.sim;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
//import org.semanticweb.owlapi.reasoner.OWLOntologyCreationException;
import org.semanticweb.owlapi.util.AbstractOWLStorer;

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


    public boolean related(String term1, String term2, boolean classesOnly) { 

        OWLClass cl1 = finder.find(term1);
        OWLClass cl2 = finder.find(term2);
        boolean res = false;

        if(cl1 == null || cl2 == null)
            return res;

        if(classesOnly) {

            //compute 'named' sub- and superclasses
            Set<OWLClass> superclasses = reasoner.getSuperClasses(cl1, false).getFlattened();
            Set<OWLClass> subclasses = reasoner.getSubClasses(cl1, false).getFlattened();

            if(superclasses != null)
                res = superclasses.contains(cl2);

            if(subclasses != null)
                res = res || subclasses.contains(cl2);

            //compute existential restrictred sub- and superclasses
            //"naive" approach
            Set<OWLClass> classes = finder.getOntology().getClassesInSignature(); 
            Set<OWLObjectProperty> properties = finder.getOntology().getObjectPropertiesInSignature();
            OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();

            //get the ontology to check against
            OWLOntology ont = finder.getOntology();
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

            //create TOP property
            OWLObjectProperty relTop = df.getOWLObjectProperty(IRI.create(ont.getOntologyID().getOntologyIRI() + "#relTop"));

            //make all properties subproperties of TOP
            for(OWLObjectProperty p : properties){ 
                manager.addAxiom(ont, df.getOWLSubObjectPropertyOfAxiom(p, relTop));
            }

            //compute entailed relatedness by using a brute force approach
            for(OWLClass A : classes) {
                for(OWLClass B : classes){
                    //instead of testing all properties, we check the TOP relation
                    ////for(OWLObjectProperty p : properties){
                    
                        //construct class expression for superclasses
                        OWLClassExpression some_p_B = df.getOWLObjectSomeValuesFrom(relTop,B);
                        OWLClassExpression all_p_B = df.getOWLObjectAllValuesFrom(relTop,B);
                        OWLClassExpression lt1_p_B = df.getOWLObjectMinCardinality(1,relTop,B);
                        OWLClassExpression gt1_p_B = df.getOWLObjectMaxCardinality(1,relTop,B);

                        //construct subsumtion axiom
                        OWLSubClassOfAxiom A_subclass_some_p_B = df.getOWLSubClassOfAxiom(A,some_p_B);
                        OWLSubClassOfAxiom A_subclass_all_p_B = df.getOWLSubClassOfAxiom(A,all_p_B);
                        OWLSubClassOfAxiom A_subclass_lt1_p_B = df.getOWLSubClassOfAxiom(A,lt1_p_B);
                        OWLSubClassOfAxiom A_subclass_gt1_p_B = df.getOWLSubClassOfAxiom(A,gt1_p_B);

                        //check entailment
                        res = res || reasoner.isEntailed(A_subclass_some_p_B); 
                        res = res || reasoner.isEntailed(A_subclass_all_p_B);
                        res = res || reasoner.isEntailed(A_subclass_lt1_p_B);
                        res = res || reasoner.isEntailed(A_subclass_gt1_p_B);
                        //res = res || ontology.contains(A_subclass_some_p_B);  change entails to contain

                    //}//end of for loop for properties
                }
            }


            /*
            //compute restricted sub- and superclasses
            //optimized approach by computing the class hierarchy via "precomputeinferences"
            Set<OWLClass> classes = finder.getOntology().getClassesInSignature(); 
            Set<OWLObjectProperty> properties = finder.getOntology().getObjectPropertiesInSignature();
            OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
            Set<OWLClassExpression> restrictionClasses = new HashSet<>();

            //collect all restriction classes
            for(OWLClass A : classes) {
                for(OWLObjectProperty p : properties){
                    restrictionClasses.add(df.getOWLObjectSomeValuesFrom(p,A));
                    //TODO: allquantifier, etc.
                }
            }

            //create IRI name
            String axiomName = "x0";
            OWLOntology ont = finder.getOntology();
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            IRI iri;

            for(OWLClassExpression exp : restrictionClasses){


                //generate UUID randomUUID()
                iri = IRI.create(ont.getOntologyID().getOntologyIRI() + "#" + axiomName);
                axiomName = "x" + (Integer.parseInt(axiomName.substring(1,axiomName.length()))+1);
                OWLClass cls = df.getOWLClass(iri);

                manager.addAxiom(ont, df.getOWLEquivalentClassesAxiom(cls, exp));
                //manager.applyChange(

            }

            reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

            Set<OWLClass> superclasses1 = reasoner.getSuperClasses(cl1, false).getFlattened();
            Set<OWLClass> subclasses1 = reasoner.getSubClasses(cl1, false).getFlattened();

            if(superclasses1 != null)
                res = superclasses.contains(cl2);

            if(subclasses1 != null)
                res = res || subclasses.contains(cl2);
            */


        }


        if(!classesOnly) {
            Set<OWLClass> superclasses = reasoner.getSuperClasses(cl1, false).getFlattened();
            Set<OWLClass> subclasses = reasoner.getSubClasses(cl1, false).getFlattened();

            Set<String> termsInSuperClasses = new HashSet<>();
            Set<String> termsInSubClasses = new HashSet<>();

            for(OWLClass c : superclasses) {
                for(OWLEntity e : c.getSignature())
                {
                }
                Set<OWLAnnotationProperty> cAnnotations = c.getAnnotationPropertiesInSignature();
            }


            //TODO: consider annodations, datatypse etc.
            return false;
        }

        return res;
    }
}
