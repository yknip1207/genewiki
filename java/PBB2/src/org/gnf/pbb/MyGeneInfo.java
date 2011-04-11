/**
 * Methods relating to pulling data from mygene.info
 */
package org.gnf.pbb;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class MyGeneInfo {
	public static final String baseURL = "http://mygene.info/gene/";
	File tempfile;
	
	public static JsonNode getGeneWithId(int id) throws JsonParseException, JsonMappingException, IOException {
		URL geneURL;
		URLConnection connection;
		JsonNode node = null;
		geneURL = new URL(baseURL + Integer.toString(id)); // would look like "http://mygene.info/gene/410"
		try {
			connection = geneURL.openConnection();
		
			ObjectMapper mapper = new ObjectMapper();
			node = mapper.readValue(connection.getInputStream(),JsonNode.class);
		
		} catch (IOException e) {
			System.err.println("There was an error opening connection to " + geneURL.toString());
		}
		return node;
	}
	
	public static void main(String[] args) {
		try {
			JsonNode node = getGeneWithId(410);
			System.out.println(node.get("name").getTextValue());
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
