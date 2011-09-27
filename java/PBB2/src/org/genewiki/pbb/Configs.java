package org.genewiki.pbb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Properties;

import org.genewiki.pbb.exceptions.ConfigException;
import org.genewiki.pbb.exceptions.FatalException;

public enum Configs {
	
	INSTANCE;
	
	private HashMap<String, Boolean> flags   = new HashMap<String, Boolean>();
	private HashMap<String, String>	 strings = new HashMap<String, String>();
	
	private final String[] keys = {
			"username",			"password",			"commonsUsername",
			"commonsPassword",	"templatePrefix",	"templateName",
			"commonsRoot", 		"pywikipedia",		"pymol"
			};
	

	/**
	 * When the Configs object is created by the JRE, the configurations
	 * still need to be set by calling setFromFile(). Until then we can't
	 * use Configs.
	 */
	private Configs() {
		flags.put("initialized", false); // Unusable until initialized
	}
	
	/**
	 * Sets the bot properties from file 
	 * @param filename
	 */
	public void setFromFile(String filename) {
		/* This bit is sketchy. If it can't find the 
		 * bot configs from the specified filename,
		 * it'll also search in the bot's jar file directory.	
		 */
		if (!(new File(filename).exists())) {
			try {
				String root = Configs.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
				String[] exploded = root.split("/");
				StringBuffer newfile = new StringBuffer();
				for (String folder : exploded) {
					if (!folder.endsWith(".jar"))
						newfile.append(folder+"/");
				}
				filename = newfile.append(filename).toString();
			} catch (URISyntaxException e) {
				// oh well.
				e.printStackTrace();
			}
		}
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
			initialize();	// We've successfully loaded the bot properties
		} catch (FileNotFoundException e) {
			throw new FatalException("Configuration file not found.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Check whether the Configs have been set and are 
	 * ready to use.
	 * @return
	 */
	public boolean initialized() {
		return flags.get("initialized");
	}
	
	/**
	 * Indicate that the Configs object is ready-to-use.
	 */
	public void initialize() {
		flags.put("initialized", true);
	}
	
	/**
	 * Returns a boolean value (flag) for the specified key.
	 * @param key
	 * @return true or false
	 * @throws ConfigException if key is does not map to
	 * a boolean value, if the key is invalid, or if the Configs
	 * singleton has not been initialized. 
	 */
	public boolean flag(String key) {
		Boolean flag = flags.get(key);
		if (initialized() && flag != null) {
			return flag;
		} else if (initialized()) {
			throw new ConfigException(key);
		} else if (flags.get(key) == null) {
			return false;
		} else {
			throw new ConfigException();
		}
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
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
