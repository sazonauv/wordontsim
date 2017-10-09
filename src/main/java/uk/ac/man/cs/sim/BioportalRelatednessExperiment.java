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
 * Created by chris on 05/09/17.
 */
public class BioportalRelatednessExperiment {

    private static final Logger log = Logger.getLogger(String.valueOf(BioportalCoverageExperiment.class));

    private static void findRelatedPairs(File ontDir, File analogyFile,
                                         File relatednessFile, File similarityFile) throws IOException {

        log.info("Loading CSV files");
        Set<String[]> analogyPairs = getAnalogyPairs(analogyFile);
        Set<String[]> relatednessPairs = getRelatednessPairs(relatednessFile);
        Set<String[]> similarityPairs = getSimilarityPairs(similarityFile);

        log.info("There are " + analogyPairs.size() + " analogy Pairs.");
        log.info("There are " + relatednessPairs.size() + " related Pairs.");
        log.info("There are " + similarityPairs.size() + " similar Pairs.");

        Set<String[]> allPairs = new HashSet<>(analogyPairs);
        allPairs.addAll(relatednessPairs);
        allPairs.addAll(similarityPairs);

        Set<String[]> relatedAndSimilarPairs = new HashSet<>(relatednessPairs);
        relatedAndSimilarPairs.addAll(similarityPairs);

        Set<String> allTerms = new HashSet<>();
        for (String[] pair : allPairs) {
            allTerms.add(pair[0]);
            allTerms.add(pair[1]);
        }

        //container for all found related pairs
        Set<String[]> relatedInOntologySet = new HashSet<>();

        for (File ontFile : ontDir.listFiles()) {

            log.info("\tLoading ontology to check for RELATEDNESS " + " : " + ontFile.getName());
            OntologyLoader loader = new OntologyLoader(ontFile, true);
            ClassFinder finder = new ClassFinder(loader.getOntology()); 

            RelatednessCalculator RLDC = new RelatednessCalculator(finder);

            for (String[] pair : relatedAndSimilarPairs) {
                if(RLDC.related(pair[0], pair[1], true)){
                    log.info("RELATED PAIR: " + pair[0] + " | " + pair[1]);
                    String[] row = {pair[0], pair[1], ontFile.getName()};
                    relatedInOntologySet.add(row);
                }
            }
        }

        List<String[]> relatedInOntology = new ArrayList<String[]>(relatedInOntologySet);
        
        File csvDir = analogyFile.getParentFile();
        File resultRelCSV = new File(csvDir, "relatedInOntology.csv");
        CSVWriter writer = new CSVWriter(new FileWriter(resultRelCSV));
        writer.writeAll(relatedInOntology);
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
