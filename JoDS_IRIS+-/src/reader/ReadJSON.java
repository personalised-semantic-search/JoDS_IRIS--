package reader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
public class ReadJSON {

    final private String path = "data/"; //set directory path
    private JsonParser parser = new JsonParser();
    private int count = -1;


    //send json files to read one by one
    public void sendJSONFile() {

        File[] jsonfiles = getJSONFiles();

        if (jsonfiles.length > 0) {
            for (int i = 0; i <= count; i++) {

                try {
                    //prints json file names
                    System.out.println("File: \t" + jsonfiles[i]);
                    JsonElement jsonElement = parser.parse(new FileReader(jsonfiles[i]));
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    readJSONFile(jsonObject);
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
        }
    }

    //read a complete json file
    public void readJSONFile(JsonObject jsonObject) {

        for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {

            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if (value.isJsonObject()) {
                readJSONFile(value.getAsJsonObject());
            }
            else if (value.isJsonArray()) {
                 JsonArray jsonArray = value.getAsJsonArray();
           
                 if (jsonArray.size() == 1) {
                    readJSONFile((JsonObject) jsonArray.get(0));
                }
                 else {
                    //prints json array name
                  //  System.out.println(key);
                    Iterator<JsonElement> msg = jsonArray.iterator();
                   // while (msg.hasNext()) {
                        ////prints json array values
                      //  System.out.println(msg.next());
                   // }
                }
            }
            else {
                ////prints json object's keys and values
            	if( key.equals("name"))
                System.out.println(key + " - " + value);
            }
        }
    }

    //get only .json files from a directory
    private File[] getJSONFiles() {

        File folder = new File(path);
        File[] files = folder.listFiles();
        File[] jsonfiles = new File[files.length];

        for (int i = 0; i < files.length; i++) {

            if (files[i].isFile()) {
                if (files[i].getName().endsWith(".json") || files[i].getName().endsWith(".JSON")) {
                    jsonfiles[++count] = files[i];
                }
            }
        }

        return files;
    }
}