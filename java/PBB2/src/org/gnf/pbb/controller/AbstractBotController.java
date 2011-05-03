package org.gnf.pbb.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import org.gnf.pbb.Config;
import org.gnf.pbb.Update;
import org.gnf.pbb.exceptions.NoBotsException;
import org.gnf.pbb.exceptions.ValidationException;
import org.gnf.pbb.wikipedia.InfoboxParser;
import org.gnf.pbb.wikipedia.WikipediaController;

public abstract class AbstractBotController {
	// Initializing the singleton logger and config objects
	protected final static Logger logger = Logger.getLogger(AbstractBotController.class.getName());
	protected final static Config configs = Config.getConfigs();
	protected Update updatedData;
	protected WikipediaController wpControl;
	protected LinkedHashMap<String, List<String>> sourceData;
	protected LinkedHashMap<String, List<String>> wikipediaData;

	@SuppressWarnings("unused")
	private AbstractBotController() {
		// XXX: Do not use this; configuration needs to be set first.
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
	public AbstractBotController(boolean verbose, boolean usecache, boolean strict, boolean dryrun, String templateURLPrefix, String templateName) {
		configs.setConfigs(this.getClass(), verbose, usecache, strict, dryrun, templateURLPrefix, templateName);
		wpControl = new WikipediaController();
	}
	
	/**
	 * Instructs the bot to import data corresponding to the specified identifier
	 * @param identifier
	 */
	public void prepareUpdateForId(String identifier) {
		try {
			importSourceData(identifier);
			importWikipediaData(identifier);
		} catch (NoBotsException e) {
			logger.severe("{{nobots}} flag found in template and strict checking set: live updates disabled.");
			configs.setUpdateAbilityAs(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Attempts to update Wikipedia (or the cache, if the dryrun flag is set). If the data maps are empty,
	 * it runs prepareUpdateForId, though this does not help if a parsing error is returning empty maps.
	 * @param identifier
	 */
	public void executeUpdateForId(String identifier) {
		if (sourceData.isEmpty() || wikipediaData.isEmpty()) {
			prepareUpdateForId(identifier);
		}
		update();
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
	public void update() {
		if (configs.canUpdate() || configs.dryrun()) {
			updatedData.update(wpControl);
		} else {
			logger.severe("Did not update Wikipedia due to errors encountered during processing. To force an update, turn strict checking off.");
		}
	}
	
	abstract protected void createUpdate();
}
