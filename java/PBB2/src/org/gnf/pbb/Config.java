package org.gnf.pbb;

import java.util.Arrays;

/**
 * Singleton instance of global bot settings. Any state can be set by calling boolean $state = Config.state();
 * for example to set verbosity: <br />
 * &nbsp;<code> boolean verbose = Configs.verbose(); </code>
 * @author eclarke
 *
 */
public class Config {
	// The individual configuration options
	private static Boolean verbose;
	private static Boolean usecache;
	private static Boolean strict;
	private static Boolean dryrun;
	private static Boolean canUpdate;
	private static String templatePrefix;
	private static String templateName;
	
	private static Boolean configsSet;
	private static final Config instance = new Config();
	
	private Config() {
		verbose = true;
		usecache = true;
		strict = true;
		dryrun = true;
		canUpdate = true;
		configsSet = false;
	}
	
	public void setConfigs(Class<?> callerClass, Boolean _verbose, Boolean _usecache, Boolean _strict, Boolean _dryrun, 
			String _templatePrefix, String _templateName) {
		String interfaces = Arrays.toString(callerClass.getInterfaces());
		if (interfaces.contains("Controller")) {
			verbose = _verbose;
			usecache = _usecache;
			strict = _strict;
			dryrun = _dryrun;
			templatePrefix = _templatePrefix;
			templateName = _templateName;
			configsSet = true;
		} else {
			try {
				throw new Exception("Cannot set configuration from any object besides an object that implements the Controller interface.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	
	public void setUpdateAbilityAs(boolean _canUpdate) {
		canUpdate = _canUpdate;
	}
	
	public boolean verbose() { return verbose; }
	public boolean usecache() { return usecache; }
	public boolean strict() { return strict; }
	public boolean dryrun() { return dryrun; }
	public boolean canUpdate() { return canUpdate; }
	public String templatePrefix() { return templatePrefix; }
	public String templateName() { return templateName; }
	
	public static Config getConfigs() {
		if (configsSet) {
			return instance;
		} else {
			System.out.println("WARNING: configuration options have not been altered from defaults. This may cause null pointer exceptions.");
			return instance;
		}
	
	}

}
