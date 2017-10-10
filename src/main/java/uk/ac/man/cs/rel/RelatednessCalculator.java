package uk.ac.man.cs.rel;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
//import org.semanticweb.owlapi.model.apibinding.OWLManager;
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

import uk.ac.man.cs.ont.ClassFinder;
import uk.ac.man.cs.ont.ReasonerLoader;
import uk.ac.man.cs.ont.ReasonerName;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
/**
 * Created by chris on 03/10/17
 */
public class RelatednessCalculator {

    private ClassFinder finder;
    private OWLReasoner reasoner;
    private OWLDataFactory factory;
    private OWLOntologyManager manager;
    private OWLOntology ontology;
    private Set<String[]> A_p_B;

    public RelatednessCalculator(ClassFinder finder) {
        this.finder = finder;
        this.factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        this.manager = OWLManager.createOWLOntologyManager();
        this.ontology = this.finder.getOntology();
        this.A_p_B = new HashSet<String[]>();
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

    private boolean checkSubsumption(String term1, String term2){

        boolean res = false;

        OWLClass cl1 = finder.find(term1);
        OWLClass cl2 = finder.find(term2);

        if(cl1 == null || cl2 == null)
            return res;

        Set<OWLClass> superclasses = reasoner.getSuperClasses(cl1, false).getFlattened();
        Set<OWLClass> subclasses = reasoner.getSubClasses(cl1, false).getFlattened();

        if(superclasses != null)
            res = res || superclasses.contains(cl2);

        if(subclasses != null)
            res = res || subclasses.contains(cl2);

        return res;
    }

    private boolean checkExistentialRestriction(OWLClass A, OWLObjectProperty p, OWLClass B){
        OWLClassExpression some_p_B = factory.getOWLObjectSomeValuesFrom(p,B);
        OWLSubClassOfAxiom A_subclass_some_p_B = factory.getOWLSubClassOfAxiom(A,some_p_B);
        return(reasoner.isEntailed(A_subclass_some_p_B));
    }

    private boolean checkUniversalRestriction(OWLClass A, OWLObjectProperty p, OWLClass B){
        OWLClassExpression all_p_B = factory.getOWLObjectAllValuesFrom(p,B);
        OWLSubClassOfAxiom A_subclass_all_p_B = factory.getOWLSubClassOfAxiom(A,all_p_B);
        return reasoner.isEntailed(A_subclass_all_p_B);
    }

    private boolean checkCardinalityRestriction(OWLClass A, OWLObjectProperty p, OWLClass B){
        OWLClassExpression lt1_p_B = factory.getOWLObjectMinCardinality(1,p,B);
        OWLClassExpression gt1_p_B = factory.getOWLObjectMaxCardinality(1,p,B);
        OWLSubClassOfAxiom A_subclass_lt1_p_B = factory.getOWLSubClassOfAxiom(A,lt1_p_B);
        OWLSubClassOfAxiom A_subclass_gt1_p_B = factory.getOWLSubClassOfAxiom(A,gt1_p_B);
        return(reasoner.isEntailed(A_subclass_lt1_p_B) || reasoner.isEntailed(A_subclass_gt1_p_B));
    }

    private boolean checkRestrictions(OWLClass cl1, OWLObjectProperty p, OWLClass cl2){
        boolean existentialCheck = false;
        boolean universalCheck = false;
        boolean cardinalityCheck = false;

        if(checkExistentialRestriction(cl1, p, cl2)){
            existentialCheck=true;
            A_p_B.add(new String[]{cl1.toString(), p.toString(), cl2.toString(), "E"});
        }

        if(checkUniversalRestriction(cl1, p, cl2)){
            universalCheck = true;
            A_p_B.add(new String[]{cl1.toString(), p.toString(), cl2.toString(), "A"});
        }

        if(checkCardinalityRestriction(cl1, p, cl2)){
            cardinalityCheck = true;
            A_p_B.add(new String[]{cl1.toString(), p.toString(), cl2.toString(), "C"});

        }
        return(existentialCheck || universalCheck || cardinalityCheck);
    }

    private boolean bruteForceRestrictionCheck(String term1, String term2){
        boolean res = false;

        OWLClass cl1 = finder.find(term1);
        OWLClass cl2 = finder.find(term2);

        if(cl1 == null || cl2 == null)
            return res;

        Set<OWLClass> classes = ontology.getClassesInSignature(); 
        Set<OWLObjectProperty> properties = ontology.getObjectPropertiesInSignature();

        //checks whether two classes are related via an object property
        for(OWLObjectProperty p : properties){ 
            if(checkRestrictions(cl1, p, cl2)){
                res = true;
            }
            if(checkRestrictions(cl2, p, cl1)){
                res = true;
            }
        }

        return res;
    }

    private boolean bruteForceCheckWithTopRelation(String term1, String term2){
        boolean res = false;
        Set<OWLClass> classes = ontology.getClassesInSignature(); 
        Set<OWLObjectProperty> properties = ontology.getObjectPropertiesInSignature();
        //create TOP property
        OWLObjectProperty relTop = factory.getOWLObjectProperty(IRI.create(ontology.getOntologyID().getOntologyIRI() + "#relTop"));

        //make all properties subproperties of TOP
        for(OWLObjectProperty p : properties){ 
            manager.addAxiom(ontology, factory.getOWLSubObjectPropertyOfAxiom(p, relTop));
        }

        for(OWLClass A : classes) {
            for(OWLClass B : classes){
                res = res || checkExistentialRestriction(A, relTop, B);
                res = res || checkUniversalRestriction(A, relTop, B);
                res = res || checkCardinalityRestriction(A, relTop, B);
            }
        }

        return res;
    }

    private boolean optimizedCheck(String term1, String term2){

        boolean res = false;

        OWLClass cl1 = finder.find(term1);
        OWLClass cl2 = finder.find(term2);

        if(cl1 == null || cl2 == null)
            return res;

        Set<OWLClass> classes = ontology.getClassesInSignature(); 
        Set<OWLObjectProperty> properties = ontology.getObjectPropertiesInSignature();
        Set<OWLClassExpression> restrictionClasses = new HashSet<>();

        //collect all restriction classes
        for(OWLClass A : classes) {
            for(OWLObjectProperty p : properties){
                restrictionClasses.add(factory.getOWLObjectSomeValuesFrom(p,A));
                restrictionClasses.add(factory.getOWLObjectAllValuesFrom(p,A));
                //TODO: cardinality, etc.
            }
        }

        //create IRI name
        String axiomName = "x0";
        IRI iri;

        for(OWLClassExpression exp : restrictionClasses){

            //generate UUID randomUUID()
            iri = IRI.create(ontology.getOntologyID().getOntologyIRI() + "#" + axiomName);
            axiomName = "x" + (Integer.parseInt(axiomName.substring(1,axiomName.length()))+1);
            OWLClass cls = factory.getOWLClass(iri);

            manager.addAxiom(ontology, factory.getOWLEquivalentClassesAxiom(cls, exp));
        }

            reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

            Set<OWLClass> superclasses = reasoner.getSuperClasses(cl1, false).getFlattened();
            Set<OWLClass> subclasses = reasoner.getSubClasses(cl1, false).getFlattened();

            if(superclasses != null)
                res = superclasses.contains(cl2);

            if(subclasses != null)
                res = res || subclasses.contains(cl2);

            return res;

    }

    private void printRelatedByRelation(){
        for(String[] row : A_p_B){
            System.out.println(row[0] + " " + row[1] + " " + row[2]);
        }
        return;
    }

    private void storeRelationsInFiles(File destDir) throws IOException{
        File resultRelCSV = new File(destDir, ontology.getOntologyID().getOntologyIRI().get() + ".csv");
        CSVWriter writer = new CSVWriter(new FileWriter(resultRelCSV));
        writer.writeAll(A_p_B);
        writer.close();
    }

    public boolean related(String term1, String term2, boolean classesOnly) throws IOException { 

        boolean res = false;

        if(classesOnly) {

            //compute 'named' sub- and superclasses
            res = checkSubsumption(term1, term2);

            //naive approach
            res = res || bruteForceRestrictionCheck(term1, term2);
            //res = res || bruteForceCheckWithTopRelation();

            //printRelatedByRelation();
            storeRelationsInFiles(new File("/home/chris/PhD/1year/similarities/test/"));
            //optimized approach
            //res = optimizedCheck(term1, term2);
        }

        if(!classesOnly) {

            //TODO: consider annodations, datatypse etc.
            /*
            for(OWLClass c : superclasses) {
                for(OWLEntity e : c.getSignature())
                {
                }
                Set<OWLAnnotationProperty> cAnnotations = c.getAnnotationPropertiesInSignature();
            }*/

            return false;
        }
        return res;
    }
}
