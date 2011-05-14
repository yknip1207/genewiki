package org.gnf.pbb;

import java.util.HashMap;
import java.util.logging.Logger;

import org.gnf.pbb.util.MapUtils;

/**
 * Global bot settings. To get a global state, either use get(String state), 
 * is(String state), or for many states, config.some_state() (i.e configs.verbose()).
 * Setting variables after initialization is prohibited to avoid 
 * inconsistent operation, unless debug mode is specified 
 * during initialization. 
 * @author eclarke
 *
 */
public class Configuration {
//	Logger logger = Logger.getLogger(Configuration.class.getName());
//	// The individual configuration options
//	private static String stopExecution;
// 
//	private static final Configuration instance = new Configuration();
//	private static Exception failureCause;
//	
//	private Configuration() {
//		
//		
//		
//	}
//	
//	/**
//	 * Set the global configuration options. This can only be called once, and when it is,
//	 * it initializes the variables which can no longer be altered until
//	 * @param verbose
//	 * @param usecache
//	 * @param strict
//	 * @param dryrun
//	 * @param debug
//	 * @param _templatePrefix
//	 * @param _templateName
//	 */
//	public void setFlags(Boolean verbose, Boolean usecache, Boolean strict, Boolean dryrun, Boolean debug) {
//		if (!initialized()) {
//			set("verbose", verbose);
//			set("usecache", usecache);
//			set("strict", strict);
//			set("dryrun", dryrun);
//			set("debug", debug);
//		} else {
//			try {
//				throw new Exception("Global configurations have already been initialized. Use debug mode to dynamically alter configuration.");
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//
//	}
//	
//	public void setUpdateAbilityAs(boolean _canUpdate) {
//		flags.put("canUpdate", _canUpdate);
//	}
//	
//	/**
//	 * Retrieve any set flag. Most flags have syntactic sugar methods, allowing calls such as
//	 * configs.verbose() instead of configs.get("verbose")
//	 * @param key
//	 * @return boolean flag
//	 */
//	public boolean getF(String key) { return flags.get(key); }
//	/**
//	 * Retrieve any set string. Most often-used strings have syntactic sugar methods.
//	 * @param key 
//	 * @return value as string
//	 */
//	public String getS(String key) { return strings.get(key); }
//	/*
//	 * Syntactic sugar.
//	 */
//	public boolean is(String key) { return getF(key); }
//	public boolean verbose() { return getF("verbose"); }
//	public boolean usecache() { return getF("usecache"); }
//	public boolean strict() { return getF("strict"); }
//	public boolean dryrun() { return getF("dryrun"); }
//	public boolean debug() { return getF("debug"); }
//
//	public boolean canUpdate() { return getF("canUpdate"); }
//	public void canUpdate(boolean bool) { flags.put("canUpdate", bool); }
//	public boolean canCreate() { return getF("canCreate"); }
//	public void canCreate(boolean bool) { flags.put("canCreate", bool); }
//	public String templatePrefix() { return strings.get("templatePrefix"); }
//	public String templateName() { return strings.get("templateName"); }
//	
//	private static boolean initialized() { return flags.get("initialized"); }
//	private static boolean firstCall() { return flags.get("firstCall"); }
//	
//	public boolean botHasFailed() { 
//		return getF("failure"); 
//	}
//	
//	/**
//	 * Set a particular state within the configs hashmap. Fails if not
//	 * in debug mode or the variables haven't been initialized.
//	 * @param key
//	 * @param value
//	 */
//	public void set(String key, Boolean value) {
//		if (debug() || !initialized()) {
//			flags.put(key, value);
//		} else {
//			logger.warning("Error: cannot set individual fields unless debug mode is specified.");
//		}
//	}
//	
//	/**
//	 * Set the template URL prefix (what comes after wikipedia.org/w/ and the unique template identifier).
//	 * Fails if not in debug mode or variables have been initialized.
//	 * @param prefix
//	 */
//	public void setPrefix(String prefix) {
//		if (debug() || !initialized() || flags.get("sandbox")) {
//			strings.put("templatePrefix", prefix);
//		} else {
//			logger.warning("Error: cannot set individual fields unless debug mode is specified.");
//		}
//	}
//	
//	/**
//	 * Set the template name. Fails if not in debug mode, or if variables are already initialized.
//	 * @param name
//	 */
//	public void setName(String name) {
//		if (debug() || !initialized()) {
//			strings.put("templateName", name);
//		} else {
//			logger.warning("Error: cannot set individual fields unless debug mode is specified.");
//		}
//	}
//	
//	/**
//	 * Appends or sets the stopExecution string with an error. Anything besides false should cause the
//	 * bot to return without updating or finishing, output a cause to the log/cache, and move on to the
//	 * next task, if available.
//	 * @param error for logging purposes
//	 */
//	private void stopExecution(String error) {
//		if (error == null) error = "Unspecified execution stop. "; 
//		if (stopExecution.equals("false")) {
//			stopExecution = error;
//		} else {
//			stopExecution = stopExecution + " | " + error;
//		}
//		logger.severe("StopExecution flag set: next check of flag will cause bot to end current update.");
//		flags.put("canUpdate", false);
//	}
//	
//	public void stopExecution(String string, StackTraceElement[] stackTrace) {
//		stopExecution(string);
//		StringBuilder sb = new StringBuilder();
//		
//		for (StackTraceElement ste : stackTrace) {
//			sb.append(ste.toString()+"\n");
//		}
//		logger.severe("Additionally, a stack trace was provided: \n"+sb.toString());
//	}
//	
//	/**
//	 * Call this method to check the execution state. Mandatory for bot controller implementations.
//	 * @return true if execution can continue
//	 */
//	public boolean canExecute() {
//		if (stopExecution.equals("false")) {
//			return true;
//		} else {
//			logger.severe("Stopped execution. Cause: "+stopExecution);
//			return false;
//		}
//	}
//	public void canExecute(boolean bool) {
//		if (bool) {
//			stopExecution = "false";
//		} else {
//			stopExecution = "Unspecified execution stop.";
//		}
//	}
//	
//	/**
//	 * Should be called at the beginning of most infobox bot classes; provides the singleton global configs
//	 * instance.
//	 * @return global configs
//	 */
//	public static Configuration getInstance() {
//		if (initialized() || firstCall() || flags.get("debug")) {
//			flags.put("firstCall", false);
//			return instance;
//		} else {
//			try {
//				throw new Exception("Error: configuration options have not been initialized!");
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			return null;
//		}
//	
//	}
//	
//	public String toString() {
//		return MapUtils.toString(flags)+"\nExecution stopped: "+stopExecution;
//	}
//
//	public void setApiLocation(String apiLocation) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	public void setLoggerLevel(String loggerLevel) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	public void setCredentials(String username, String password) {
//		if(!initialized()) {
//			strings.put("username", username);
//			strings.put("password", password);
//			
//		} else {
//			logger.warning("Error: Cannot change credentials after initialization.");
//		}
//		
//	}
//
//	public void setLogHandlerLocation(String logHandlerLocation) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	public void setCacheLocation(String cacheLocation) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	public void setStrings(String templatePrefix2, String templateName2,
//			String apiLocation, String loggerLevel,	String logHandlerLocation, 
//			String cacheLocation, String dbName) {
//		if(!initialized()){
//			strings.put("templatePrefix", templatePrefix2);
//			strings.put("templateName", templateName2);
//			strings.put("apiLocation", apiLocation);
//			strings.put("loggerLevel", loggerLevel);
//			strings.put("logHandlerLocation", logHandlerLocation);
//			strings.put("cacheLocation", cacheLocation);
//			strings.put("dbName", dbName);
//		} else {
//			logger.warning("Error: Cannot update string values after initialization to prevent inconsistent bot state.");
//		}
//	}
//	
//	public boolean initialize() {
//		if (strings.get("username").equals("")) {
//			logger.warning("Username not set; future authentication methods will fail! Initialization failed.");
//		}
//		logger.fine("Initializing global variables; general alteration prohibited going forward.");
//		flags.put("initialized", true);
//		return true;
//	}
//
//	public String dbName() {
//		return strings.get("dbName");
//	}
//
//	/**
//	 * Set the exception that caused a non-recoverable error and
//	 * indicate to the monitor of the controller thread to interrupt it.
//	 * @param e exception
//	 */
//	public void fail(Exception e) {
//		failureCause = e;
//		flags.put("failure", true);
//		logger.severe("Fatal operational failure caused by: " + failureCause.getClass().getName() +": "+failureCause.getMessage());
//	}
//
//	/*
//	 * Changes every time the bot goes to update a new infobox
//	 */
//	public void setId(String identifier) {
//		strings.put("id", identifier);
//	}
//	
//	/*
//	 * Returns the current working id
//	 */
//	public String getId() {
//		return getS("id");
//	}
//


}
