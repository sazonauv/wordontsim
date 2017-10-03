package uk.ac.man.cs.sim;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by slava on 05/09/17.
 */
public class BioportalCoverageExperiment {

    private static final Logger log = Logger.getLogger(String.valueOf(BioportalCoverageExperiment.class));

    public static void main(String[] args) throws IOException {

        File ontDir = new File(args[0]);
        File analogyFile = new File(args[1]);
        File relatednessFile = new File(args[2]);
        File similarityFile = new File(args[3]);

//        findCoveredTerms(ontDir, analogyFile, relatednessFile, similarityFile);
        findCoveredPairs(ontDir, analogyFile, relatednessFile, similarityFile);

    }


    private static void findCoveredPairs(File ontDir, File analogyFile,
                                         File relatednessFile, File similarityFile) throws IOException {
        log.info("Loading CSV files");
        Set<String[]> analogyPairs = getAnalogyPairs(analogyFile);
        Set<String[]> relatednessPairs = getRelatednessPairs(relatednessFile);
        Set<String[]> similarityPairs = getSimilarityPairs(similarityFile);

        Set<String[]> allPairs = new HashSet<>(analogyPairs);
        allPairs.addAll(relatednessPairs);
        allPairs.addAll(similarityPairs);

        Set<String> allTerms = new HashSet<>();
        for (String[] pair : allPairs) {
            allTerms.add(pair[0]);
            allTerms.add(pair[1]);
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

        Map<String, Set<String[]>> ontTermsMap = new HashMap<>();
        for (File ontFile : ontDir.listFiles()) {
            ontTermsMap.put(ontFile.getName(), new HashSet<>());
        }

        int pairsCoveredCount = 0;
        for (String[] pair : allPairs) {
            Set<String> onts1 = termOntsMap.get(pair[0]);
            Set<String> onts2 = termOntsMap.get(pair[1]);
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
                    Set<String[]> ontPairs = ontTermsMap.get(ont);
                    ontPairs.add(pair);
                }
            }
        }

        Out.p(pairsCoveredCount + " / " + allPairs.size() + " term pairs are found");

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

    private static Set<String[]> getAnalogyPairs(File analogyFile) throws IOException {
        Set<String[]> pairs = new HashSet<>();
        CSVReader reader = new CSVReader(new FileReader(analogyFile));
        List<String[]> rows = reader.readAll();
        for (String[] row : rows) {
            pairs.add(new String[]{row[0].toLowerCase(), row[1].toLowerCase()});
            pairs.add(new String[]{row[2].toLowerCase(), row[3].toLowerCase()});
        }
        return pairs;
    }

    private static Set<String[]> getRelatednessPairs(File relatednessFile) throws IOException {
        Set<String[]> pairs = new HashSet<>();
        CSVReader reader = new CSVReader(new FileReader(relatednessFile));
        List<String[]> rows = reader.readAll();
        for (String[] row : rows) {
            pairs.add(new String[]{row[2].toLowerCase(), row[3].toLowerCase()});
        }
        return pairs;
    }

    private static Set<String[]> getSimilarityPairs(File similarityFile) throws IOException {
        Set<String[]> pairs = new HashSet<>();
        CSVReader reader = new CSVReader(new FileReader(similarityFile));
        List<String[]> rows = reader.readAll();
        for (String[] row : rows) {
            pairs.add(new String[]{row[2].toLowerCase(), row[3].toLowerCase()});
        }
        return pairs;
    }




    private static void findCoveredTerms(File ontDir, File analogyFile,
                                         File relatednessFile, File similarityFile) throws IOException {
        log.info("Loading CSV files");
        Set<String> analogyTerms = getAnalogyTerms(analogyFile);
        Set<String> relatednessTerms = getRelatednessTerms(relatednessFile);
        Set<String> similarityTerms = getSimilarityTerms(similarityFile);

        Set<String> allTerms = new HashSet<>(analogyTerms);
        allTerms.addAll(relatednessTerms);
        allTerms.addAll(similarityTerms);

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
                    log.info("\t\t" + termCount + " terms checked");
                }
            }
        }

        int termsCoveredCount = 0;
        for (String term : termOntsMap.keySet()) {
            Set<String> onts = termOntsMap.get(term);
            if (!onts.isEmpty()) {
                termsCoveredCount++;
            }
        }

        Out.p(termsCoveredCount + " / " + allTerms.size() + " terms are found");
    }



    private static Set<String> getAnalogyTerms(File analogyFile) throws IOException {
        Set<String> terms = new HashSet<>();
        CSVReader reader = new CSVReader(new FileReader(analogyFile));
        List<String[]> rows = reader.readAll();
        for (String[] row : rows) {
            for (String value : row) {
                terms.add(value.toLowerCase());
            }
        }
        return terms;
    }

    private static Set<String> getRelatednessTerms(File relatednessFile) throws IOException {
        Set<String> terms = new HashSet<>();
        CSVReader reader = new CSVReader(new FileReader(relatednessFile));
        List<String[]> rows = reader.readAll();
        for (String[] row : rows) {
            terms.add(row[2].toLowerCase());
            terms.add(row[3].toLowerCase());
        }
        return terms;
    }

    private static Set<String> getSimilarityTerms(File similarityFile) throws IOException {
        Set<String> terms = new HashSet<>();
        CSVReader reader = new CSVReader(new FileReader(similarityFile));
        List<String[]> rows = reader.readAll();
        for (String[] row : rows) {
            terms.add(row[2].toLowerCase());
            terms.add(row[3].toLowerCase());
        }
        return terms;
    }





}
