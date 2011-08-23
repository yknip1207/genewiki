/**
 * 
 */
package org.genewiki.pbb.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;


import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author eclarke
 *
 */
public class ConfigParser {
	
	
	public static LinkedHashMap<String, String> parseProperties(String filename) {
		LinkedHashMap<String, String> properties = new LinkedHashMap<String, String>();
		String[] keys = {"name", "dryRun", "useCache", "strictChecking", "verbose", "debug", 
				"cache", "logs", "username", "password", "template_prefix", "template_name",
				"api_root", "logger_level"};
		Properties propertyFile = new Properties();
		try {
			propertyFile.load(new FileReader(filename));
			for (String key : keys) {
				if (propertyFile.containsKey(key)) {
					properties.put(key, propertyFile.getProperty(key)); // Add the property to our map
				} else {
					throw new IOException("Property file missing key: "+key);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Fail gracefully
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Fail gracefully - show what error was encountered?
			e.printStackTrace();
		}
		
		return properties;
	}

	public static void parse(String filename, HashMap<String, Boolean> flags,
			HashMap<String, String> strings) {
		if (fileExists(filename)) {
			File file = new File(filename);
			ObjectMapper mapper = new ObjectMapper();
			try {
				// Parse the configuration file
				JsonNode root = mapper.readValue(file, JsonNode.class);
				// TODO be consistent about names of parameters
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
