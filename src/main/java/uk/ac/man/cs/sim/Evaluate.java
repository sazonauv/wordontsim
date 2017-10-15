package uk.ac.man.cs.sim;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import uk.ac.man.cs.io.Out;
import uk.ac.man.cs.io.TermDataLoader;
import uk.ac.man.cs.io.TermPair;

public class Evaluate {
	
	public static void main(String[] args) throws IOException{
		File analogyFile = new File(args[0]);
		File relatednessFile = new File(args[1]);
		File similarityFile = new File(args[2]);
		
		File file = new File(args[3]);
		File fileSim = new File(args[4]);
		File fileRel = new File(args[5]);
		File fileAna = new File(args[6]);
		File file2 = new File(args[7]);
		
		TermDataLoader tdLoader = new TermDataLoader(analogyFile, relatednessFile, similarityFile);		
		
		transform(file, file2);
		transformPart(fileSim, file2, tdLoader.getSimilarityPairs());
		transformPart(fileRel, file2, tdLoader.getRelatednessPairs());
		transformPart(fileAna, file2, tdLoader.getAnalogyPairs());
		Out.p(getTermsCount(file2));
		Set<String> strings = new HashSet();
		for(TermPair pair:tdLoader.getAnalogyPairs()){
			strings.add(pair.first);
			strings.add(pair.second);
		}
		Out.p(strings.size());
		
		Out.p(getTermsCountPart(file2, tdLoader.getSimilarityPairs()));
		Out.p(getTermsCountPart(file2, tdLoader.getRelatednessPairs()));
		Out.p(getTermsCountPart(file2, tdLoader.getAnalogyPairs()));
	}
	
	public static void transform(File file, File file2) throws IOException{
		CSVWriter writer = new CSVWriter(new FileWriter(file));
		CSVReader reader = new CSVReader(new FileReader(file2));
		
		List<String[]> rows = reader.readAll();
		String[] name = rows.get(0);	
		String[] maxValue = rows.get(rows.size()-1);
		for(int i=1;i<name.length;i++){
			String[] newRow = {name[i], maxValue[i]};
			writer.writeNext(newRow);
		}
		writer.close();
	}
	
	public static void transformPart(File file, File file2, Set<TermPair> pairs) throws IOException{
		CSVWriter writer = new CSVWriter(new FileWriter(file));
		CSVReader reader = new CSVReader(new FileReader(file2));
		List<String> strings = new ArrayList();
		for(TermPair pair:pairs){
			strings.add(pair.toString());
		}
		
		List<String[]> rows = reader.readAll();
		String[] name = rows.get(0);	
		String[] maxValue = rows.get(rows.size()-1);
		for(int i=1;i<name.length;i++){
			if(strings.contains(name[i])){
				String[] newRow = {name[i], maxValue[i]};
				writer.writeNext(newRow);
			}
		}
		writer.close();
	}
	
	public static int getTermsCountPart(File file, Set<TermPair> termPairs) throws IOException{
		Set<String> pairs = new HashSet();
		List<String> strings = new ArrayList();
		for(TermPair pair:termPairs){
			strings.add(pair.toString());
		}
		CSVReader reader = new CSVReader(new FileReader(file));		
		List<String[]> rows = reader.readAll();
		String[] name = rows.get(0);	
		String[] maxValue = rows.get(rows.size()-1);
		for(int i=1;i<name.length;i++){
			if(!maxValue[i].equals("0")&&strings.contains(name[i])){
				String first = name[i].substring(name[i].indexOf("(")+1, name[i].indexOf(","));
				String second = name[i].substring(name[i].indexOf(",")+2, name[i].indexOf(")"));
				pairs.add(first);
				pairs.add(second);
			}
		}
		
		return pairs.size();
	}
	
	public static int getTermsCount(File file) throws IOException{
		Set<String> pairs = new HashSet();
		CSVReader reader = new CSVReader(new FileReader(file));		
		List<String[]> rows = reader.readAll();
		String[] name = rows.get(0);	
		String[] maxValue = rows.get(rows.size()-1);
		for(int i=1;i<name.length;i++){
			if(!maxValue[i].equals("0")){
				String first = name[i].substring(name[i].indexOf("(")+1, name[i].indexOf(","));
				String second = name[i].substring(name[i].indexOf(",")+2, name[i].indexOf(")"));
				pairs.add(first);
				pairs.add(second);
			}
		}
		
		return pairs.size();
	}
	
}
