/**
 * 
 */
package org.gnf.pbb.util;

import java.io.File;
import java.io.IOException;


import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.gnf.pbb.Global;

/**
 * @author eclarke
 *
 */
public class ConfigParser {

	public static void setGlobalConfigsFromFile(File file, Global global) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			
			// Parse the configuration file
			JsonNode root = mapper.readValue(file, JsonNode.class);
			
			JsonNode defaults = root.path("defaults");
			boolean dryrun = defaults.path("dryrun").getBooleanValue();
			boolean usecache = defaults.path("usecache").getBooleanValue();
			boolean strict = defaults.path("strictchecking").getBooleanValue();
			boolean verbose = defaults.path("verbose").getBooleanValue();
			boolean debug = defaults.path("debugmode").getBooleanValue();
			
			String cacheLocation = root.path("CacheLocation").getTextValue();
			String logHandlerLocation = root.path("LogHandlerLocation").getTextValue();
			String username = root.path("credentials").path("username").getTextValue();
			String password = root.path("credentials").path("password").getTextValue();
			String templatePrefix = root.path("TemplatePrefix").getTextValue();
			String templateName = root.path("TemplateName").getTextValue();
			String apiLocation = root.path("ApiLocation").getTextValue();
			String dbName = root.path("DatabaseName").getTextValue();
			String loggerLevel = root.path("LoggerLevel").getTextValue();
			
			// Bind the variables to the global object

			global.setFlags(verbose, usecache, strict, dryrun, debug);
			global.setStrings(templatePrefix, templateName, apiLocation, loggerLevel, logHandlerLocation, cacheLocation, dbName);
			// Lock the variables to prevent later editing
			global.initialize();
			
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
