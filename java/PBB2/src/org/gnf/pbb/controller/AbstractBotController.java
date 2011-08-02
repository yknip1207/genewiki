package org.gnf.pbb.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.gnf.pbb.Configs;
import org.gnf.pbb.exceptions.ExceptionHandler;
import org.gnf.pbb.exceptions.NoBotsException;
import org.gnf.pbb.exceptions.ExceptionHandler;
import org.gnf.pbb.exceptions.ValidationException;
import org.gnf.pbb.logs.DatabaseManager;
import org.gnf.pbb.logs.DatabaseManager;
import org.gnf.pbb.wikipedia.InfoboxParser;
import org.gnf.pbb.wikipedia.ProteinBox;
import org.gnf.pbb.wikipedia.WikipediaController;

public abstract class AbstractBotController implements Runnable {

	/* ---- Declarations ---- */
	protected final Logger logger;				
	protected final DatabaseManager dbManager;	// Manages SQLite db to track changes
	protected final ExceptionHandler botState;	// ExceptionHandler to communicate bot state
	
	protected 	WikipediaController wpControl;
	
	public		List<String> identifiers;
	protected 	List<String> completed;
	protected	List<String> failed;
	
	protected 	ProteinBox sourceData;
	protected 	ProteinBox wikipediaData;
	
	private 	int delay;
	
	
	/* ---- Constructors ---- */
	public AbstractBotController(List<String> identifiers, ExceptionHandler exh) {
		logger = Logger.getLogger(AbstractBotController.class.getName());
		dbManager = new DatabaseManager();
		botState = exh;
		
		this.wpControl = new WikipediaController(botState, Configs.GET);
		
		this.delay = 3;
		this.identifiers = identifiers;
		this.completed = new ArrayList<String>(0);
		this.failed = new ArrayList<String>(0);
	}
	
	public AbstractBotController(List<String> identifiers) {
		this(identifiers, ExceptionHandler.INSTANCE);
	}
	
	public AbstractBotController(List<String> identifiers, int delay) {
		this(identifiers);
		this.delay = delay;
	}
	
	/* ---- Main run() method (runs in thread) ---- */
	public void run() {
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
				boolean success = false;
				try {
					sourceData = importSourceData(id);
					wikipediaData = importWikipediaData(id);
				} catch (Exception e) {
					botState.recoverable(e);
				}
				
				if (botState.isFine()) {
					ProteinBox updatedData = wikipediaData.updateWith(sourceData);	
					success = this.update(updatedData);
				} else if (botState.canRecover()) {
					success = false;
				} else {
					throw new Exception("Bot state is unrecoverable, exiting.");
				}
				
				if (success) {
					completed.add(id);
					//XXX Debug hacks.
//					Runtime run = Runtime.getRuntime();
//					Process p = run.exec("C:\\Users\\eclarke\\AppData\\Local\\Mozilla Firefox\\firefox.exe -new-tab " +
//							"http://184.72.42.242/mediawiki/index.php?title=Template:PBB/"+id+"&action=history");
//					System.out.println("Once satisfied, press any key to continue.");
//					System.in.read();
//					p.destroy();
//					System.out.println("Moving along...");
				} else {
					failed.add(id);
					DatabaseManager.updateDb("failed", id, "");
				}
				reset();
			} catch (InterruptedException e) {
				e.printStackTrace();
				prepareReport();
				return;
			} catch (IOException e) {
				e.printStackTrace();
				prepareReport();
				return;
			} catch (Exception e) {
				e.printStackTrace();
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
	
	/* ---- Private methods ---- */
	
	/**
	 * Executes the push() method of the given update object, with safety
	 * checks in place
	 */
	private boolean update(ProteinBox update) throws Exception {
		if (botState.isFine() || Configs.GET.flag("dryrun")) {
			wpControl.putContent(update.toString(), update.getSingle("Hs_EntrezGene"), update.getSummary());
			DatabaseManager.updateDb("true", update.getId(), update.getChangedFields());
			return true;
		} else {
			logger.severe("Did not update Wikipedia due to errors encountered during processing. To force an update, turn strict checking off.");
			return false;
			}
	}
	
	/**
	 * Called each new iteration of the bot. Ensures that any recoverable errors
	 * and any data from the previous session is discarded.
	 */
	private void reset() {
		if (sourceData != null && wikipediaData != null) {
			sourceData.reset();
			wikipediaData.reset();
		}
		botState.reset();
		logger.info("Bot reset.");
	}
	
	/* ---- Private importer methods ---- */
	
	abstract protected ProteinBox importSourceData(String id);
	
	private ProteinBox importWikipediaData(String id) {
		String content = wpControl.getContentForId(id);
		InfoboxParser parser = InfoboxParser.factory(content);
		try {
			return parser.parse();
		} catch (NoBotsException e) {
			botState.recoverable(e);
			return null;
		} catch (ValidationException e) {
			botState.recoverable(e);
			return null;
		}
	}
	
	/* ---- Public methods ---- */
	
	abstract protected String prepareReport();
}
