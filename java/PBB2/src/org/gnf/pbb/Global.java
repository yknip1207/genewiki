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
public class Global {
	Logger logger = Logger.getLogger(Global.class.getName());
	// The individual configuration options
	private static String templatePrefix;
	private static String templateName;
	private static String stopExecution;
	private static HashMap<String, Boolean> configs = new HashMap<String, Boolean>(8);
	private static final Global instance = new Global();
	
	private Global() {
		configs.put("verbose",     true);
		configs.put("usecache",    true);
		configs.put("strict",      true);
		configs.put("dryrun",      true);
		configs.put("canUpdate",   true);
		configs.put("canCreate",   true);
		configs.put("initialized", false);
		configs.put("firstCall",   true);
		configs.put("debug",       true);
		
		// This sits in place of thread.stop() and should be checked routinely to ensure
		// the bot is in a consistent state. If it is anything but false, something has 
		// gone wrong but an error was not thrown (i.e. a null object was returned from
		// something that shouldn't have a null object thrown, etc.
		stopExecution = "false";
	}
	
	/**
	 * Set the global configuration options. This can only be called once, and when it is,
	 * it initializes the variables which can no longer be altered until
	 * @param verbose
	 * @param usecache
	 * @param strict
	 * @param dryrun
	 * @param debug
	 * @param _templatePrefix
	 * @param _templateName
	 */
	public void setConfigs(Boolean verbose, Boolean usecache, Boolean strict, Boolean dryrun, Boolean debug,
			String _templatePrefix, String _templateName) {
		if (!initialized()) {
			set("verbose", verbose);
			set("usecache", usecache);
			set("strict", strict);
			set("dryrun", dryrun);
			set("debug", debug);
			templatePrefix = _templatePrefix;
			templateName = _templateName;
			set("initialized", true);
		} else {
			try {
				throw new Exception("Global configurations have already been initialized. Use debug mode to dynamically alter configuration.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	
	public void setUpdateAbilityAs(boolean _canUpdate) {
		configs.put("canUpdate", _canUpdate);
	}
	
	/**
	 * Retrieve any configuration state. Most states have syntactic sugar methods, allowing calls such as
	 * configs.verbose() instead of configs.get("verbose")
	 * @param key
	 * @return
	 */
	public boolean get(String key) { return configs.get(key); }
	
	/*
	 * Syntactic sugar.
	 */
	public boolean is(String key) { return get(key); }
	public boolean verbose() { return get("verbose"); }
	public boolean usecache() { return get("usecache"); }
	public boolean strict() { return get("strict"); }
	public boolean dryrun() { return get("dryrun"); }
	public boolean debug() { return get("debug"); }

	public boolean canUpdate() { return get("canUpdate"); }
	public void canUpdate(boolean bool) { configs.put("canUpdate", bool); }
	public boolean canCreate() { return get("canCreate"); }
	public void canCreate(boolean bool) { configs.put("canCreate", bool); }
	public String templatePrefix() { return templatePrefix; }
	public String templateName() { return templateName; }
	
	private static boolean initialized() { return configs.get("initialized"); }
	private static boolean firstCall() { return configs.get("firstCall"); }
	
	
	
	/**
	 * Set a particular state within the configs hashmap. Fails if not
	 * in debug mode or the variables haven't been initialized.
	 * @param key
	 * @param value
	 */
	public void set(String key, Boolean value) {
		if (debug() || !initialized()) {
			configs.put(key, value);
		} else {
			logger.warning("Error: cannot set individual fields unless debug mode is specified.");
		}
	}
	
	/**
	 * Set the template URL prefix (what comes after wikipedia.org/w/ and the unique template identifier).
	 * Fails if not in debug mode or variables have been initialized.
	 * @param prefix
	 */
	public void setPrefix(String prefix) {
		if (debug() || !initialized()) {
			templatePrefix = prefix;
		} else {
			logger.warning("Error: cannot set individual fields unless debug mode is specified.");
		}
	}
	
	/**
	 * Set the template name. Fails if not in debug mode, or if variables are already initialized.
	 * @param name
	 */
	public void setName(String name) {
		if (debug() || !initialized()) {
			templateName = name;
		} else {
			logger.warning("Error: cannot set individual fields unless debug mode is specified.");
		}
	}
	
	/**
	 * Appends or sets the stopExecution string with an error. Anything besides false should cause the
	 * bot to return without updating or finishing, output a cause to the log/cache, and move on to the
	 * next task, if available.
	 * @param error for logging purposes
	 */
	public void stopExecution(String error) {
		if (stopExecution.equals("false")) {
			stopExecution = error;
		} else {
			stopExecution = stopExecution + " | " + error;
		}
		logger.severe("StopExecution flag set: next check of flag will cause bot to exit.");
		configs.put("canUpdate", false);
	}
	
	/**
	 * Call this method to check the execution state. Mandatory for bot controller implementations.
	 * @return true if execution can continue
	 */
	public boolean canExecute() {
		if (stopExecution.equals("false")) {
			return true;
		} else {
			logger.severe("Stopped execution. Cause: "+stopExecution);
			return false;
		}
	}
	public void canExecute(boolean bool) {
		if (bool) {
			stopExecution = "false";
		} else {
			stopExecution = "Unspecified execution stop.";
		}
	}
	
	/**
	 * Should be called at the beginning of most infobox bot classes; provides the singleton global configs
	 * instance.
	 * @return global configs
	 */
	public static Global getInstance() {
		if (initialized() || firstCall() || configs.get("debug")) {
			configs.put("firstCall", false);
			return instance;
		} else {
			try {
				throw new Exception("Error: configuration options have not been initialized!");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	
	}
	
	public String toString() {
		return MapUtils.toString(configs)+"\nExecution stopped: "+stopExecution;
	}

}
