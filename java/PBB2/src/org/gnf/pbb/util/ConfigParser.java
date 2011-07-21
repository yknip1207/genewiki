/**
 * 
 */
package org.gnf.pbb.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;


import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author eclarke
 *
 */
public class ConfigParser {

	public static void parse(String filename, HashMap<String, Boolean> flags,
			HashMap<String, String> strings) {
		if (fileExists(filename)) {
			File file = new File(filename);
			ObjectMapper mapper = new ObjectMapper();
			try {
				// Parse the configuration file
				JsonNode root = mapper.readValue(file, JsonNode.class);
				
				JsonNode defaults = root.path("defaults");
				flags.put("dryrun", defaults.path("dryrun").getBooleanValue());
				flags.put("usecache", defaults.path("usecache").getBooleanValue());
				flags.put("strict", defaults.path("strictchecking").getBooleanValue());
				flags.put("verbose", defaults.path("verbose").getBooleanValue());
				flags.put("debug", defaults.path("debugmode").getBooleanValue());
				flags.put("canCreate", defaults.path("canCreate").getBooleanValue());
				
				strings.put("cacheLocation", root.path("CacheLocation").getTextValue());
				strings.put("logLocation", root.path("LogHandlerLocation").getTextValue());
				strings.put("username", root.path("credentials").path("username").getTextValue());
				strings.put("password", root.path("credentials").path("password").getTextValue());
				strings.put("templatePrefix", root.path("TemplatePrefix").getTextValue());
				strings.put("templateName", root.path("TemplateName").getTextValue());
				strings.put("hostLocation", root.path("HostLocation").getTextValue());
				strings.put("dbName", root.path("DatabaseName").getTextValue());
				strings.put("loggerLevel", root.path("LoggerLevel").getTextValue());
				
				// mark the configs as initialized
				flags.put("initialized", true);
				
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
	
	private static boolean fileExists(String filename) {
		return (new File(filename)).exists();
	}
	
}
