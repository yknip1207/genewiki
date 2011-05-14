package org.gnf.pbb.exceptions;

public class ConfigException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ConfigException(String key) {
		super("Configuration not set for key: "+key);
	}
	
	/**
	 * Default exception thrown when configuration variables are accessed without being
	 * initialized.
	 */
	public ConfigException() {
		super("Configuration not initialized.");
	}

}
