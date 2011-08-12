package org.gnf.pbb;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import org.gnf.pbb.exceptions.ConfigException;
import org.gnf.pbb.exceptions.FatalException;

public enum Configs {
	
	GET;	 // Not a method, this is the singleton instance of Config. Use it as such:
			 // Config configs = Config.GET
	
	private HashMap<String, Boolean> flags   = new HashMap<String, Boolean>();
	private HashMap<String, String>	 strings = new HashMap<String, String>();
	
	private final String[] keys = {"name", "dryRun", "useCache", "strictChecking", 
			"verbose", "debug", "cacheLocation", "logs", "username", "password",
			"commonsUsername", "commonsPassword", "templatePrefix", "templateName", "api_root", 
			"commonsRoot", "loggerLevel", "pymol"};
	

	/* ---- Initialization Code ---- */
	Configs() {
		flags.put("initialized", false); // Unusable until initialized
	}
	
	/**
	 * Sets the bot properties from file 
	 * @param filename
	 */
	public void setFromFile(String filename) {
		Properties propFile = new Properties();
		try {
			propFile.load(new FileReader(filename));
			for (String key : keys) {
				
				if (propFile.containsKey(key)) {
					String value = propFile.getProperty(key);
					if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
						flags.put(key, Boolean.valueOf(value));
					} else {
						strings.put(key, value);
					}
				} else {
					throw new IOException("In property file \""+filename+"\", " +
							"missing key \""+key+"\".");
				}
			}
			flags.put("initialized", true);	// We've successfully loaded the bot properties
		} catch (FileNotFoundException e) {
			throw new FatalException("Configuration file not found.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* ---- Public Code ---- */
	public boolean initialized() {
		return flags.get("initialized");
	}
	
	public boolean flag(String key) {
		Boolean flag = flags.get(key);
		if (initialized() && flag != null) {
			return flag;
		} else if (initialized()) {
			throw new ConfigException(key);
		} else {
			throw new ConfigException();
		}
	}
	
	public String str(String key) {
		String str = strings.get(key);
		if (initialized() && str != (null)) {
			return str;
		} else if (initialized()) {
			throw new ConfigException(key);
		} else {
			throw new ConfigException();
		}
	}
	
	public void set(String key, Boolean flag) {
		flags.put(key, flag);
	}
	
	public void set(String key, String str) {
		strings.put(key, str);
	}
}
