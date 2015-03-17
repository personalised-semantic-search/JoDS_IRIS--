package reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonParse {

	final private String path = "data/"; // set directory path
	private JsonParser parser = new JsonParser();
	private int count = -1;

	// send json files to read one by one
	public void sendJSONFile( HashMap<String, ArrayList<Integer>> v) {

        File[] jsonfiles = getJSONFiles();
        HashMap<String, ArrayList<Integer>> values= (HashMap<String, ArrayList<Integer>>) v.clone();
        if (jsonfiles.length > 0) {
            for (int i = 0; i <= count; i++) {

                try {
                    //prints json file names
                    //System.out.println("File: \t" + jsonfiles[i]);
                    Pattern pattern = Pattern.compile("[0-9]+");
                    Matcher matcher = pattern.matcher( jsonfiles[i].toString());
                    String id_user="", id_exp="";
                    if (matcher.find()) id_user = matcher.group();
                    if (matcher.find()) id_exp = matcher.group();
                    JsonElement jsonElement = parser.parse(new FileReader(jsonfiles[i]));
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    String o= getOwner(jsonObject);
                    if (!values.containsKey(id_user+"_"+o))
                    {
                    	values.put(id_user+"_"+o, new ArrayList<Integer>());
                    }
                	values.get(id_user+"_"+o).add(Integer.parseInt(id_exp));
                }
                catch (FileNotFoundException e) {
                    System.out.println(e.getMessage());
                    //e.getStackTrace();
                }
                catch (IOException e) {
                    System.out.println(e.getMessage());
                    //e.getStackTrace();
                }
                catch (Exception e) {
                    System.out.println(e.getMessage());
                    //e.getStackTrace();
                }

            }
            for (String key : values.keySet()) {
            	if (values.get(key).size()<9)
            	 for (Integer i : values.get(key)) {
     				System.out.println(key+" "+i);
     			}
			}
        }
    }

	// read a complete json file
	public String getOwner(JsonObject jsonObject) {
		String message = "";
		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {

			String key = entry.getKey();
			JsonElement value = entry.getValue();

			if (value.isJsonObject()) {
				getOwner(value.getAsJsonObject());
			} else if (value.isJsonArray()) {
				JsonArray jsonArray = value.getAsJsonArray();

				if (jsonArray.size() == 1) {
					getOwner((JsonObject) jsonArray.get(0));
				}
			} else {
				if (key.equals("name"))
					message += value;
			}
		}
		return message;
	}

	// get only .json files from a directory
	private File[] getJSONFiles() {

		File folder = new File(path);
		File[] files = folder.listFiles();
		File[] jsonfiles = new File[files.length];

		for (int i = 0; i < files.length; i++) {

			if (files[i].isFile()) {
				if (files[i].getName().endsWith(".json")
						|| files[i].getName().endsWith(".JSON")) {
					jsonfiles[++count] = files[i];
				}
			}
		}

		return files;
	}
}