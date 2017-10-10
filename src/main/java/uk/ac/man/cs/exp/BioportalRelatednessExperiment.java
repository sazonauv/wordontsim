package uk.ac.man.cs.exp;

import com.opencsv.CSVWriter;
import uk.ac.man.cs.io.TermDataLoader;
import uk.ac.man.cs.io.TermPair;
import uk.ac.man.cs.ont.ClassFinder;
import uk.ac.man.cs.ont.OntologyLoader;
import uk.ac.man.cs.rel.RelatednessCalculator;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

/**
 * Created by chris on 05/09/17.
 */
public class BioportalRelatednessExperiment {

    private static final Logger log = Logger.getLogger(String.valueOf(BioportalRelatednessExperiment.class));


    private Set<TermPair> termPairs;


    public BioportalRelatednessExperiment(Set<TermPair> termPairs) {
        this.termPairs = termPairs;
    }


    public static void main(String[] args) throws IOException {
        File ontDir = new File(args[0]);
        File analogyFile = new File(args[1]);
        File relatednessFile = new File(args[2]);
        File similarityFile = new File(args[3]);

        TermDataLoader tdLoader = new TermDataLoader(analogyFile, relatednessFile, similarityFile);
        BioportalRelatednessExperiment bioRelExp = new BioportalRelatednessExperiment(tdLoader.getAllPairs());

        // you can use this to filter out ontologies containing no or few pairs (see BioportalSimilarityExperiment)
        File coverageFile = new File(analogyFile.getParentFile(), "bioportal_coverage_distr.csv");

        File destDir = new File(analogyFile.getParentFile().getPath() + "/relatednessResults");
        destDir.mkdir();

        bioRelExp.findRelatedPairs(ontDir, destDir);

        Set<TermPair> relatedPairs = tdLoader.getRelatednessPairs();
        Set<TermPair> similarPairs = tdLoader.getSimilarityPairs();
        Set<TermPair> related_similar_pairs = new HashSet<>(relatedPairs);
        related_similar_pairs.addAll(similarPairs);

        bioRelExp.analyseResults(new File(destDir.getPath() + "/relatedInOntology.csv"), related_similar_pairs, destDir);

    }



    private void findRelatedPairs(File ontDir, File csvDir) throws IOException {

        // container for all found related pairs
        Set<String[]> relatedInOntologySet = new HashSet<>();

        for (File ontFile : ontDir.listFiles()) {

            log.info("\tLoading ontology to check for RELATEDNESS " + " : " + ontFile.getName());
            OntologyLoader loader = new OntologyLoader(ontFile, true);
            ClassFinder finder = new ClassFinder(loader.getOntology());

            RelatednessCalculator relCalc = new RelatednessCalculator(finder, csvDir);

            for (TermPair pair : termPairs) {
                if(relCalc.related(pair.first, pair.second, true, csvDir, ontFile.getName())){
                    log.info("RELATED PAIR: " + pair);
                    String[] row = {pair.first, pair.second, ontFile.getName()};
                    relatedInOntologySet.add(row);
                }
            }
        }

        List<String[]> relatedInOntology = new ArrayList<>(relatedInOntologySet);

        File resultRelCSV = new File(csvDir, "relatedInOntology.csv");
        CSVWriter writer = new CSVWriter(new FileWriter(resultRelCSV));
        writer.writeAll(relatedInOntology);
        writer.close();
    }

    private void analyseResults(File resultFile, Set<TermPair> GoldStadard_related, File destDir) throws IOException {

        CSVReader reader = new CSVReader(new FileReader(resultFile));
        List<String[]> rows = reader.readAll();
        Set<String[]> inStandard = new HashSet<String[]>();
        Set<String[]> notInStandard = new HashSet<String[]>();

        for(String[] row : rows){
            if(GoldStadard_related.contains(new TermPair(row[0], row[1]))){
                inStandard.add(new String[]{row[0], row[1], "1"});
            }else{
                notInStandard.add(new String[]{row[0], row[1], "0"}); 
            } 
        }

        File resultRelCSV = new File(destDir, "inStandard.csv");
        CSVWriter writer = new CSVWriter(new FileWriter(resultRelCSV));
        writer.writeAll(inStandard);
        writer.close();

        File resultRelCSV1 = new File(destDir, "notInStandard.csv");
        CSVWriter writer1 = new CSVWriter(new FileWriter(resultRelCSV1));
        writer1.writeAll(notInStandard);
        writer1.close();

    }


}
