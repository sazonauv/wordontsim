package uk.ac.man.cs.exp;

import com.opencsv.CSVWriter;
import uk.ac.man.cs.io.Out;
import uk.ac.man.cs.io.TermDataLoader;
import uk.ac.man.cs.io.TermPair;
import uk.ac.man.cs.ont.ClassFinder;
import uk.ac.man.cs.ont.OntologyLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

/**
 * Created by slava on 05/09/17.
 */
public class BioportalCoverageExperiment {

    private static final Logger log = Logger.getLogger(String.valueOf(BioportalCoverageExperiment.class));

    //Pairs given in the gold standard
    private static Set<TermPair> allTermPairs;
    private static Set<TermPair> relatedTermPairs;
    private static Set<TermPair> similarTermPairs;
    private static Set<TermPair> analogyTermPairs;

    //Terms covered
    private static Set<String> allCoveredTerms;
    private static Set<String> coveredRelatedTerms;
    private static Set<String> coveredSimilarTerms;
    private static Set<String> coveredAnalogyTerms;

    //Pairs covered (NOTE: pairs are collected with respect to individual ontologies)
    private static Set<TermPair> allCoveredPairs;
    private static Set<TermPair> coveredRelatedPairs;
    private static Set<TermPair> coveredSimilarPairs;
    private static Set<TermPair> coveredAnalogyPairs;

    public BioportalCoverageExperiment(TermDataLoader tdLoader) {
        this.allTermPairs = tdLoader.getAllPairs();
        this.relatedTermPairs = tdLoader.getRelatednessPairs();
        this.similarTermPairs = tdLoader.getSimilarityPairs();
        this.analogyTermPairs = tdLoader.getAnalogyPairs();

        this.allCoveredTerms = new HashSet<String>();
        this.coveredRelatedTerms = new HashSet<String>();
        this.coveredSimilarTerms = new HashSet<String>();
        this.coveredAnalogyTerms = new HashSet<String>();

        this.allCoveredPairs = new HashSet<>();
        this.coveredRelatedPairs = new HashSet<>();
        this.coveredSimilarPairs = new HashSet<>();
        this.coveredAnalogyPairs = new HashSet<>();
    }

    public static void main(String[] args) throws IOException {

        File ontDir = new File(args[0]);
        File analogyFile = new File(args[1]);
        File relatednessFile = new File(args[2]);
        File similarityFile = new File(args[3]);

        File destDir = new File(analogyFile.getParentFile().getPath() + "/coverageResults");
        destDir.mkdir();

        ConsoleHandler handler = new ConsoleHandler();
        log.addHandler(handler);
        handler.setLevel(Level.ALL);

        TermDataLoader tdLoader = new TermDataLoader(analogyFile, relatednessFile, similarityFile);

        BioportalCoverageExperiment bioCovExp = new BioportalCoverageExperiment(tdLoader);
        bioCovExp.findCoveredPairs(ontDir, analogyFile);

        /*
        Set<String[]> relatedGSTermpairs = new HashSet<String[]>();
        Set<String[]> coveredPairs = findCoveredPairs(allTermPairs, allCoveredTerms);
        Set<String[]> coveredRelatedPairs = findCoveredPairs(relatedTermPairs, coveredRelatedTerms);
        Set<String[]> coveredSimilarPairs = findCoveredPairs(similarTermPairs, coveredSimilarTerms);
        Set<String[]> coveredAnalogyPairs = findCoveredPairs(analogyTermPairs, coveredAnalogyTerms);

        storeResults(coveredPairs, destDir, "coverdPairs.csv");
        storeResults(coveredRelatedPairs, destDir, "coverdRelatedPairs.csv");
        storeResults(coveredSimilarPairs, destDir, "coverdSimilarPairs.csv");
        storeResults(coveredAnalogyPairs, destDir, "coverdAnalogyPairs.csv");
        */

        storeResults(pairs2tuples(allCoveredPairs), destDir, "coverdPairs.csv");
        storeResults(pairs2tuples(coveredRelatedPairs), destDir, "coverdRelatedPairs.csv");
        storeResults(pairs2tuples(coveredSimilarPairs), destDir, "coverdSimilarPairs.csv");
        storeResults(pairs2tuples(coveredAnalogyPairs), destDir, "coverdAnalogyPairs.csv");

    }


    private void findCoveredPairs(File ontDir, File analogyFile) throws IOException {

        // boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("jdwp") >= 0;
        // System.out.println("WHAAT" + isDebug);
        Set<String> allTerms = pairs2terms(allTermPairs);
        Set<String> relatedTerms = pairs2terms(relatedTermPairs);
        Set<String> similarTerms = pairs2terms(similarTermPairs);
        Set<String> analogyTerms = pairs2terms(analogyTermPairs);

        Set<String> ontologyCoveredTerms = new HashSet<>();
        Set<String> ontologyCoveredRelatedTerms = new HashSet<>();
        Set<String> ontologyCoveredSimilarTerms = new HashSet<>();
        Set<String> ontologyCoveredAnalogyTerms = new HashSet<>();

        Map<String, Set<String>> termOntsMap = new HashMap<>();
        for (String term : allTerms) {
            termOntsMap.put(term, new HashSet<>());
        }

        log.info("Checking ontologies");
        int ontCount = 0;
        for (File ontFile : ontDir.listFiles()) {
            log.info("\tLoading " + ++ontCount + " : " + ontFile.getName());
            OntologyLoader loader = new OntologyLoader(ontFile, true);
            ClassFinder finder = new ClassFinder(loader.getOntology());
            int termCount = 0;
            for (String term : termOntsMap.keySet()) {
                Set<String> onts = termOntsMap.get(term);
                if (finder.contains(term)) {
                    onts.add(ontFile.getName());
                    if(relatedTerms.contains(term)){
                        coveredRelatedTerms.add(term);
                        ontologyCoveredRelatedTerms.add(term);
                    }
                    if(similarTerms.contains(term)){
                        coveredSimilarTerms.add(term);
                        ontologyCoveredSimilarTerms.add(term);
                    }
                    if(analogyTerms.contains(term)){
                        coveredAnalogyTerms.add(term);
                        ontologyCoveredAnalogyTerms.add(term);
                    }
                    if(allTerms.contains(term)){
                        allCoveredTerms.add(term);
                        ontologyCoveredTerms.add(term);
                    }
                }
                if (++termCount % 100 == 0) {
                    log.info("\t\t" + termCount + " terms are checked");
                }
            }

            for(TermPair pair : allTermPairs){
                if(ontologyCoveredTerms.contains(pair.first) && ontologyCoveredTerms.contains(pair.second)){
                   allCoveredPairs.add(new TermPair(pair.first, pair.second));
                } 
            }
            for(TermPair pair : relatedTermPairs){
                if(ontologyCoveredRelatedTerms.contains(pair.first) && ontologyCoveredRelatedTerms.contains(pair.second)){
                   coveredRelatedPairs.add(new TermPair(pair.first, pair.second));
                } 
            }
            for(TermPair pair : similarTermPairs){
                if(ontologyCoveredSimilarTerms.contains(pair.first) && ontologyCoveredSimilarTerms.contains(pair.second)){
                   coveredSimilarPairs.add(new TermPair(pair.first, pair.second));
                } 
            }
            for(TermPair pair : analogyTermPairs){
                if(ontologyCoveredAnalogyTerms.contains(pair.first) && ontologyCoveredAnalogyTerms.contains(pair.second)){
                   coveredAnalogyPairs.add(new TermPair(pair.first, pair.second));
                } 
            }

            ontologyCoveredTerms.clear();
            ontologyCoveredRelatedTerms.clear();
            ontologyCoveredSimilarTerms.clear();
            ontologyCoveredAnalogyTerms.clear();
        }

        Map<String, Set<TermPair>> ontTermsMap = new HashMap<>();
        for (File ontFile : ontDir.listFiles()) {
            ontTermsMap.put(ontFile.getName(), new HashSet<>());
        }

        int pairsCoveredCount = 0;
        for (TermPair pair : allTermPairs) {
            Set<String> onts1 = termOntsMap.get(pair.first);
            Set<String> onts2 = termOntsMap.get(pair.second);
            if (onts1.isEmpty() || onts2.isEmpty()) {
                continue;
            }
            for (String ont : onts1) {
                if (onts2.contains(ont)) {
                    pairsCoveredCount++;
                    break;
                }
            }
            for (String ont : onts1) {
                if (onts2.contains(ont)) {
                    Set<TermPair> ontPairs = ontTermsMap.get(ont);
                    ontPairs.add(pair);
                }
            }
        }

        Out.p(pairsCoveredCount + " / " + allTermPairs.size() + " term pairs are found");

        Out.p("\nPairs distribution over ontologies:\n");
        List<String[]> resultList = new ArrayList<>();
        String[] header = new String[]{"ontology", "covered_term_pairs"};
        resultList.add(header);
        for (String ont : ontTermsMap.keySet()) {
            int coveredPairsCount = ontTermsMap.get(ont).size();
            String[] row = new String[]{ont, Integer.toString(coveredPairsCount)};
            resultList.add(row);
            Out.p(ont + " : " + coveredPairsCount + " term pairs");
        }

        File csvDir = analogyFile.getParentFile();
        File resultCSV = new File(csvDir, "bioportal_coverage_distr.csv");
        CSVWriter writer = new CSVWriter(new FileWriter(resultCSV));
        writer.writeAll(resultList);
        writer.close();
    }

    private Set<String> pairs2terms(Set<TermPair> termPairs){
        Set<String> terms = new HashSet<>();
        for (TermPair pair : termPairs) {
            terms.add(pair.first);
            terms.add(pair.second);
        }

        return terms;
    }

    private static Set<String[]> pairs2tuples(Set<TermPair> termPairs){
        Set<String[]> terms = new HashSet<>();
        for (TermPair pair : termPairs) {
            terms.add(new String[] {pair.first, pair.second});
        }

        return terms;
    }

    private static void storeResults(Set<String[]> relatedInOntologySet, File csvDir, String name) throws IOException {

        List<String[]> relatedInOntology = new ArrayList<>(relatedInOntologySet);

        File resultRelCSV = new File(csvDir, name);
        CSVWriter writer = new CSVWriter(new FileWriter(resultRelCSV));
        writer.writeAll(relatedInOntology);
        writer.close();

    }
    

    private static Set<String[]> findCoveredPairs(Set<TermPair> pairs, Set<String> coveredTerms){
        Set<String[]> coveredPairs = new HashSet<String[]>();

        for(TermPair pair : pairs){
            if(coveredTerms.contains(pair.first) && coveredTerms.contains(pair.second)){
                coveredPairs.add(new String[]{pair.first, pair.second});
            }
        }

        return coveredPairs;

    }

}
