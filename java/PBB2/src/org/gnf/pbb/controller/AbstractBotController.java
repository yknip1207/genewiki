package org.gnf.pbb.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import org.gnf.pbb.Configs;
import org.gnf.pbb.Update;
import org.gnf.pbb.exceptions.ConfigException;
import org.gnf.pbb.exceptions.ExceptionHandler;
import org.gnf.pbb.exceptions.NoBotsException;
import org.gnf.pbb.exceptions.PbbExceptionHandler;
import org.gnf.pbb.exceptions.ValidationException;
import org.gnf.pbb.wikipedia.InfoboxParser;
import org.gnf.pbb.wikipedia.WikipediaController;

public abstract class AbstractBotController implements Runnable {
	// Initializing the singleton logger and config objects
	protected final static Logger logger = Logger.getLogger(AbstractBotController.class.getName());
	protected ExceptionHandler botState;
	protected Update updatedData;
	protected WikipediaController wpControl;
	protected LinkedHashMap<String, List<String>> sourceData;
	protected LinkedHashMap<String, List<String>> wikipediaData;
	public final List<String> identifiers;
	protected List<String> completed;
	protected List<String> failed;

	/**
	 * This constructor should not be used.
	 */
	@SuppressWarnings("unused")
	private AbstractBotController() {
		this.identifiers = null;
	}
	
	/**
	 * Create a new AbstractBotController with a list of identifiers and a specified ExceptionHandler
	 * @param identifiers
	 * @param exhandler
	 */
	public AbstractBotController(List<String> identifiers, ExceptionHandler exhandler) {
		this.botState = exhandler;
		wpControl = new WikipediaController(botState, Configs.GET);
		sourceData = new LinkedHashMap<String,List<String>>();
		wikipediaData = new LinkedHashMap<String,List<String>>();
		this.identifiers = identifiers;
		this.completed = new ArrayList<String>(0);
		this.failed = new ArrayList<String>(0);
	}
	
	/**
	 * Create a new AbstractBotController with a list of identifiers. Uses the singleton instance of 
	 * PbbExceptionHandler.
	 * @param identifiers
	 */
	public AbstractBotController(List<String> identifiers) {
		this(identifiers, PbbExceptionHandler.INSTANCE);
	}

	public void run() {
		int delay = 3; // Seconds to delay between updates
		for (String id : identifiers) {
			try {
				System.out.print(String.format("Executing update for id: "+id+" in %d...\n", delay+1));
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
					//XXX Debug hacks.
					Runtime run = Runtime.getRuntime();
					Process p = run.exec("C:\\Users\\eclarke\\AppData\\Local\\Mozilla Firefox\\firefox.exe -new-tab " +
							"http://184.72.42.242/mediawiki/index.php?title=Template:PBB/"+id+"&action=history");
					System.out.println("Once satisfied, press any key to continue.");
					System.in.read();
					p.destroy();
					System.out.println("Moving along...");
				} else {
					failed.add(id);
				}
			} catch (InterruptedException e) {
				prepareReport();
				return;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		botState.reset();
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
			updatedData = createUpdate(identifier, updatedData);
		} catch (NoBotsException e) {
			logger.severe("{{nobots}} flag found in template and strict checking set: live updates disabled.");
			botState.minor(e);
		} catch (Exception e) {
			botState.recoverable(e);
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
		if (botState.canExecute()){
			try {
				update();
			} catch (ConfigException e) {
				botState.fatal(e);
				return false;
			}
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
		InfoboxParser parser = InfoboxParser.factory(wpControl.getContentForId(id));
		this.wikipediaData = parser.parse();
	}
	
	public LinkedHashMap<String, List<String>> getSourceData() { return sourceData; }
	public LinkedHashMap<String, List<String>> getWikipediaData() { return wikipediaData; }
	public Update getUpdateObject() { return updatedData; }
	
	/**
	 * Attempts to call the update object's internal methods to push its data to Wikipedia (or the cache). If 
	 * it is not a dry run, and something happened along the way that flipped the bot's internal canUpdate bit
	 * to false, it will not send the update to Wikipedia. That bit is often flipped by detecting a {{nobots}} flag.
	 * @throws ConfigException 
	 */
	public boolean update() throws ConfigException {
		if (botState.isFine() || Configs.GET.flag("dryrun")) {
			updatedData.update(wpControl);
			return true;
		} else {
			logger.severe("Did not update Wikipedia due to errors encountered during processing. To force an update, turn strict checking off.");
			return false;
			}
	}
	
	abstract protected Update createUpdate(String id, Update update);
	
	abstract public String prepareReport();

	
}
