package JODS;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProbabilisticModel {

	public static Map<String, Double> get(String fileNameProbModel) {
		java.io.BufferedReader br = null;
		try {
			br = new java.io.BufferedReader(new FileReader(fileNameProbModel));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line;
		Map<String, Double> probModel = new HashMap<String, Double>();
		try {
			while ((line = br.readLine()) != null) {
				String[] lines = line.split(",");
				probModel.put(lines[0], Double.parseDouble(lines[1]));
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return probModel;
	}
}
