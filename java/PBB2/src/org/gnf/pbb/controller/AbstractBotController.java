package org.gnf.pbb.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import org.gnf.pbb.Global;
import org.gnf.pbb.Update;
import org.gnf.pbb.exceptions.NoBotsException;
import org.gnf.pbb.exceptions.ValidationException;
import org.gnf.pbb.wikipedia.InfoboxParser;
import org.gnf.pbb.wikipedia.WikipediaController;

public abstract class AbstractBotController implements Runnable {
	// Initializing the singleton logger and config objects
	protected final static Logger logger = Logger.getLogger(AbstractBotController.class.getName());
	public Global global = Global.getInstance();
	protected Update updatedData;
	protected WikipediaController wpControl;
	protected LinkedHashMap<String, List<String>> sourceData;
	protected LinkedHashMap<String, List<String>> wikipediaData;
	public final List<String> identifiers;
	protected List<String> completed = new ArrayList<String>();
	protected List<String> failed = new ArrayList<String>();

	/**
	 * This constructor should not be used.
	 */
	@SuppressWarnings("unused")
	private AbstractBotController() {
		this.identifiers = null;
	}
	
	/**
	 * Create a new BotController.
	 * @param verbose
	 * 			output lots of logger info
	 * @param usecache
	 * 			use cache files (see WikipediaController for cache implementation)
	 * @param strict
	 * 			check that the template name matches what's specified, and that there are no {{nobots}} flags
	 * @param dryrun
	 * 			output final run to the cache folder (this works regardless of nobots flag detection)
	 * @param templateURLPrefix
	 * 			The text forms the URL between http://en.wikipedia.org/w/ and the unique id of this particular template
	 * @param templateName
	 * 			The general name for the template (not necessarily the URL prefix) that appears in the opening; i.e. {{template_name 
	 */
	public AbstractBotController(boolean verbose, boolean usecache, boolean strict, 
			boolean dryrun, boolean debug, String templateURLPrefix, String templateName, List<String> identifiers) {
		global.setConfigs(verbose, usecache, strict, dryrun, debug, templateURLPrefix, templateName);
		wpControl = new WikipediaController();
		sourceData = new LinkedHashMap<String,List<String>>();
		wikipediaData = new LinkedHashMap<String,List<String>>();
		this.identifiers = identifiers;
	}
	
	public void run() {
		int delay = 6; // Seconds to delay between updates
		for (String id : identifiers) {
			try {
				System.out.print(String.format("Executing update for id: "+id+" in %d...\n", delay));
				for (int i = delay; i > 0; i--) {
					System.out.print(String.format("%d...\n", i));
					Thread.sleep(1000);
					if (Thread.interrupted()) {
						prepareReport();
						return;
					}
				}
				boolean success = this.resetAndExecuteUpdateForId(id);
				if (success) {
					completed.add(id);
				} else {
					failed.add(id);
				}
			} catch (InterruptedException e) {
				prepareReport();
				return;
			}
			if (Thread.interrupted()) {
				prepareReport();
				return;
			}
		}
		prepareReport();
		return;
	}
	
	public void reset() {
		sourceData = new LinkedHashMap<String, List<String>>();
		wikipediaData = new LinkedHashMap<String, List<String>>();
		global.canCreate(true);
		global.canExecute(true);
		global.canUpdate(true);
		logger.info("Bot reset.");
	}
	
	/**
	 * Instructs the bot to import data corresponding to the specified identifier
	 * @param identifier
	 */
	public void prepareUpdateForId(String identifier) {
		try {
			importSourceData(identifier);
			importWikipediaData(identifier);
			createUpdate();
		} catch (NoBotsException e) {
			logger.severe("{{nobots}} flag found in template and strict checking set: live updates disabled.");
			global.setUpdateAbilityAs(false);
		} catch (Exception e) {
			global.stopExecution("Failure during update. " + e.getMessage());
			return;
		}
	}
	
	/**
	 * Attempts to update Wikipedia (or the cache, if the dryrun flag is set). If the data maps are empty,
	 * it runs prepareUpdateForId, though this does not help if a parsing error is returning empty maps.
	 * @param identifier
	 */
	public boolean resetAndExecuteUpdateForId(String identifier) {
		reset();
		logger.info("Executing new update for "+identifier);
		prepareUpdateForId(identifier);
		if (global.canExecute()) {
			update();
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Imports data from non-Wikipedia source. Must set the internal sourceData map as the final target.
	 * @param identifier
	 */
	abstract void importSourceData(String identifier);
	
	/**
	 * Imports wikipedia infobox data for a given id and sets it to the internal wikipediaData map.
	 * @param id
	 * @throws NoBotsException
	 * @throws ValidationException
	 */
	public void importWikipediaData (String id) throws NoBotsException, ValidationException {
		InfoboxParser parser = new InfoboxParser(wpControl.getContentForId(id));
		this.wikipediaData = parser.parse();
	}
	
	public LinkedHashMap<String, List<String>> getSourceData() { return sourceData; }
	public LinkedHashMap<String, List<String>> getWikipediaData() { return wikipediaData; }
	public Update getUpdateObject() { return updatedData; }
	
	/**
	 * Attempts to call the update object's internal methods to push its data to Wikipedia (or the cache). If 
	 * it is not a dry run, and something happened along the way that flipped the bot's internal canUpdate bit
	 * to false, it will not send the update to Wikipedia. That bit is often flipped by detecting a {{nobots}} flag.
	 */
	public boolean update() {
		if (global.canExecute()) {
			if (global.canUpdate() || global.dryrun()) {
				updatedData.update(wpControl);
				return true;
			} else {
				logger.severe("Did not update Wikipedia due to errors encountered during processing. To force an update, turn strict checking off.");
				return false;
			}
		} else {
			return false;
		}
	}
	
	abstract protected boolean createUpdate();
	
	abstract public String prepareReport();

	
}
