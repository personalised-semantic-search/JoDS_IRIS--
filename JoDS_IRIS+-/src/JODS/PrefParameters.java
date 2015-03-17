/**
 * 
 */
package JODS;

import group.models.PreferenceStrategy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * @author onsa
 * 
 */
public class PrefParameters {
	public PrefParameters(int nbNodes,
			double sparsity) throws IOException {
		
		this.nbNodes = nbNodes;
		this.startFromRes = 0;
		//this.strategy = st;
		this.sparsity = sparsity;

	}

	private final int nbNodes;
	private final int startFromRes;
	private final double sparsity;
	//private final PreferenceStrategy strategy;


	/**
	 * @return the nbNodes: select ... from employee limit nbNodes
	 */
	public int getNbNodes() {
		return nbNodes;
	}

	/**
	 * @return the startFromRes: select ... offset startFromRes
	 */
	public int getStartFromRes() {
		return startFromRes;
	}

	/**
	 * @return the constraintsSqlQuery:e.g where city=""
	 */
	// public String getConstraintsSqlQuery() {
	// return constraintsSqlQuery;
	// }
	//
	// /**
	// * @return the folderNamePrefs
	// */
	// public JsonArray getPrefs() {
	// return prefs;
	// }

	/**
	 * @return the strategy
	 */
//	public PreferenceStrategy getStrategy() {
//		return strategy;
//	}

	private final JsonArray loadFolderPrefs(final String folderName)
			throws IOException {
		final File folder = new File(folderName);
		JsonArray jsonList = new JsonArray();
		for (final File fileEntry : folder.listFiles()) {
			if (!fileEntry.isDirectory()) {
				JsonParser parser = new JsonParser();
				JsonElement jsonElement = parser
						.parse(new FileReader(fileEntry));
				jsonList.add(jsonElement.getAsJsonObject());
			}
		}
		return jsonList;
	}

	public String getAllParams() {
		return " " + nbNodes + " " + startFromRes + " ";
	}

	/**
	 * @return the sparsity
	 */
	public double getSparsity() {
		return sparsity;
	}

}
