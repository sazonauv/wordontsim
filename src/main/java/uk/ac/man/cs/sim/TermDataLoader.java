package uk.ac.man.cs.sim;

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
	
	public static Set<String[]> getAllPairs(File analogyFile,
		File relatednessFile, File similarityFile) throws IOException{
		log.info("Loading CSV files");
		Set<String[]> allPairs = new HashSet<>();
		Set<String[]> analogyPairs = getAnalogyPairs(analogyFile);
		Set<String[]> relatednessPairs = getRelatednessPairs(relatednessFile);
		Set<String[]> similarityPairs = getSimilarityPairs(similarityFile);

		allPairs = new HashSet<>(analogyPairs);
		allPairs.addAll(relatednessPairs);
		allPairs.addAll(similarityPairs);
		
		clear(allPairs);
		return allPairs;
	}
	
	private static void clear(Set<String[]> allPairs){
    	Set<String> allpair = new HashSet<>();
    	for(String[] s : allPairs){
    		allpair.add(s[0]+" "+s[1]);
    	}
    	allPairs.clear();
    	for(String s1 : allpair){
    		String[] s2= {s1.substring(0, s1.indexOf(" ")),s1.substring(s1.indexOf(" ")+1)};
    		allPairs.add(s2);
    	}   	
    } 
	
	private static Set<String[]> getAnalogyPairs(File analogyFile) throws IOException {
		Set<String[]> pairs = new HashSet<String[]>();
		CSVReader reader = new CSVReader(new FileReader(analogyFile));
		List<String[]> rows = reader.readAll();
		for (String[] row : rows) {
			pairs.add(new String[]{row[0].toLowerCase(), row[1].toLowerCase()});
			pairs.add(new String[]{row[2].toLowerCase(), row[3].toLowerCase()});
		}
		return pairs;
	}

	private static Set<String[]> getRelatednessPairs(File relatednessFile) throws IOException {
		Set<String[]> pairs = new HashSet<String[]>();
		CSVReader reader = new CSVReader(new FileReader(relatednessFile));
		List<String[]> rows = reader.readAll();
	   	for (String[] row : rows) {
	   		pairs.add(new String[]{row[2].toLowerCase(), row[3].toLowerCase()});
	   	}
	   	return pairs;
	}

	private static Set<String[]> getSimilarityPairs(File similarityFile) throws IOException {
		Set<String[]> pairs = new HashSet<String[]>();
		CSVReader reader = new CSVReader(new FileReader(similarityFile));
		List<String[]> rows = reader.readAll();
		for (String[] row : rows) {
			pairs.add(new String[]{row[2].toLowerCase(), row[3].toLowerCase()});
		}
		return pairs;
	}
}
