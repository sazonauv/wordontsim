package uk.ac.man.cs.exp;

import com.opencsv.CSVWriter;
import uk.ac.man.cs.io.Out;
import uk.ac.man.cs.io.TermDataLoader;
import uk.ac.man.cs.io.TermPair;
import uk.ac.man.cs.ont.ClassFinder;
import uk.ac.man.cs.ont.OntologyLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by slava on 05/09/17.
 */
public class BioportalCoverageExperiment {

    private static final Logger log = Logger.getLogger(String.valueOf(BioportalCoverageExperiment.class));

    private Set<TermPair> termPairs;

    public BioportalCoverageExperiment(Set<TermPair> termPairs) {
        this.termPairs = termPairs;
    }

    public static void main(String[] args) throws IOException {

        File ontDir = new File(args[0]);
        File analogyFile = new File(args[1]);
        File relatednessFile = new File(args[2]);
        File similarityFile = new File(args[3]);

        TermDataLoader tdLoader = new TermDataLoader(analogyFile, relatednessFile, similarityFile);
        BioportalCoverageExperiment bioCovExp = new BioportalCoverageExperiment(tdLoader.getAllPairs());
        bioCovExp.findCoveredPairs(ontDir, analogyFile);
    }


    private void findCoveredPairs(File ontDir, File analogyFile) throws IOException {

        Set<String> allTerms = new HashSet<>();
        for (TermPair pair : termPairs) {
            allTerms.add(pair.first);
            allTerms.add(pair.second);
        }

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
                }
                if (++termCount % 100 == 0) {
                    log.info("\t\t" + termCount + " terms are checked");
                }
            }

        }

        Map<String, Set<TermPair>> ontTermsMap = new HashMap<>();
        for (File ontFile : ontDir.listFiles()) {
            ontTermsMap.put(ontFile.getName(), new HashSet<>());
        }

        int pairsCoveredCount = 0;
        for (TermPair pair : termPairs) {
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

        Out.p(pairsCoveredCount + " / " + termPairs.size() + " term pairs are found");

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

}
