package uk.ac.man.cs.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVReader;

public class CoverageData {
	private static File coverageFile;
	private static String analogies = "owlfile/Analogies.csv";
	public static void main(String[] args) throws IOException{
		File analogyFile = new File(args[0]);
		File reasonerTestFile = new File(analogyFile.getParentFile(), "ontology_reasoner_test.csv");
		Map<String, String> reasonerData = new CoverageData(reasonerTestFile).getCoverageData();
		
	}
	
	public CoverageData(File coverageFile){
		this.coverageFile = coverageFile;
	}
	public static Map<String, String> getCoverageData() throws IOException {
		Map<String, String> coverageData = new HashMap<>();
		CSVReader reader = new CSVReader(new FileReader(coverageFile));
		List<String[]> rows = reader.readAll();
		rows.remove(rows.get(0));
		for (String[] row : rows) {
			if(row.length<2){
				coverageData.put(row[0], "null");
			}
			else{
				coverageData.put(row[0], row[1]);
			}
		}
		return coverageData;
	}
	
}
