package org.gnf.pbb;

import java.util.HashMap;

import org.gnf.pbb.exceptions.ConfigException;
import org.gnf.pbb.util.ConfigParser;

public enum Configs {
	GET;
	private HashMap<String, Boolean> flags = new HashMap<String, Boolean>();
	private HashMap<String, String> strings = new HashMap<String, String>();
	
	Configs() {
		flags.put("initialized", false);
	}
	/**
	 * Read a bot configuration file and set internal configuration state from it
	 * @param filename
	 */
	public void setConfigsFromFile(String filename) {
		ConfigParser.parse(filename, flags, strings);
	}
	
	/**
	 * If the initialization state is true or false (should generally always be true;
	 * use the {@link setConfigsFromFile} method to initialize 
	 * @return
	 */
	public boolean initialized() {
		return flags.get("initialized");
	}

	/**
	 * Returns boolean state for specified flag
	 * @param key name of flag
	 * @return boolean state
	 */
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
	
	/**
	 * Returns the string associated for specified key
	 * @param key name of the string
	 * @return corresponding string
	 */
	public String str(String key) {
		String str = strings.get(key);
		if (initialized() && str != null) {
			return str;
		} else if (initialized()) {
			throw new ConfigException(key);
		} else {
			throw new ConfigException();
		}
	}
	
	public void set(String key, Boolean flag) {
		if (!initialized()) {
			flags.put(key, flag);
		} else {
			throw new ConfigException("Configuration has already been set.");
		}
	}
	
}
