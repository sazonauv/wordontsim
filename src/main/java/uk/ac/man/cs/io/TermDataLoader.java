package uk.ac.man.cs.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.opencsv.CSVReader;

public class TermDataLoader {

	private static final Logger log = Logger.getLogger(String.valueOf(TermDataLoader.class));

	private Set<TermPair> analogyPairs;

	private Set<TermPair> relatednessPairs;

	private Set<TermPair> similarityPairs;

	private Set<TermPair> allPairs;


	public TermDataLoader(File analogyFile, File relatednessFile, File similarityFile) throws IOException {
		log.info("Loading CSV files");
		analogyPairs = loadAnalogyPairs(analogyFile);
		relatednessPairs = loadRelatednessPairs(relatednessFile);
		similarityPairs = loadSimilarityPairs(similarityFile);

		allPairs = new HashSet<>(analogyPairs);
		allPairs.addAll(relatednessPairs);
		allPairs.addAll(similarityPairs);
	}


	public Set<TermPair> getAnalogyPairs() {
		return analogyPairs;
	}

	public Set<TermPair> getRelatednessPairs() {
		return relatednessPairs;
	}

	public Set<TermPair> getSimilarityPairs() {
		return similarityPairs;
	}

	public Set<TermPair> getAllPairs() {
		return allPairs;
	}

	/*private static void clear(Set<String[]> allPairs) {
    	Set<String> pairStrings = new HashSet<>();
    	for(String[] s : allPairs){
    		pairStrings.add(s[0]+" "+s[1]);
    	}
    	allPairs.clear();
    	for(String s1 : pairStrings){
    		String[] s2 = {s1.substring(0, s1.indexOf(" ")), s1.substring(s1.indexOf(" ")+1)};
    		allPairs.add(s2);
    	}   	
    }*/
	
	private static Set<TermPair> loadAnalogyPairs(File analogyFile) throws IOException {
		Set<TermPair> pairs = new HashSet<>();
		CSVReader reader = new CSVReader(new FileReader(analogyFile));
		List<String[]> rows = reader.readAll();
		for (String[] row : rows) {
			pairs.add(new TermPair(row[0].toLowerCase(), row[1].toLowerCase()));
			pairs.add(new TermPair(row[2].toLowerCase(), row[3].toLowerCase()));
		}
		log.info("There are " + pairs.size() + " analogy Pairs.");
		return pairs;
	}

	private static Set<TermPair> loadRelatednessPairs(File relatednessFile) throws IOException {
		Set<TermPair> pairs = new HashSet<>();
		CSVReader reader = new CSVReader(new FileReader(relatednessFile));
		List<String[]> rows = reader.readAll();
	   	for (String[] row : rows) {
	   		pairs.add(new TermPair(row[2].toLowerCase(), row[3].toLowerCase()));
	   	}
		log.info("There are " + pairs.size() + " related Pairs.");
	   	return pairs;
	}

	private static Set<TermPair> loadSimilarityPairs(File similarityFile) throws IOException {
		Set<TermPair> pairs = new HashSet<>();
		CSVReader reader = new CSVReader(new FileReader(similarityFile));
		List<String[]> rows = reader.readAll();
		for (String[] row : rows) {
			pairs.add(new TermPair(row[2].toLowerCase(), row[3].toLowerCase()));
		}
		log.info("There are " + pairs.size() + " similar Pairs.");
		return pairs;
	}
}
