package org.genewiki.pbb.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.genewiki.pbb.Configs;
import org.genewiki.pbb.db.DatabaseManager;
import org.genewiki.pbb.exceptions.ExceptionHandler;
import org.genewiki.pbb.exceptions.NoBotsException;
import org.genewiki.pbb.exceptions.ValidationException;
import org.genewiki.pbb.mygeneinfo.MyGeneInfoParser;
import org.genewiki.pbb.wikipedia.InfoboxParser;
import org.genewiki.pbb.wikipedia.ProteinBox;
import org.genewiki.pbb.wikipedia.WikipediaInterface;

import com.google.common.base.Preconditions;
/**
 * <p>BotController iterates through a list of identifiers (Entrez gene ids)
 * and updates the corresponding template on Wikipedia. 
 * <p> It collects structured, authoritative information from
 * http://mygene.info and parses it into a ProteinBox object. It then does
 * the same for the GNF_Protein_box template specified by the identifier on
 * Wikipedia. Once the two ProteinBoxes are created, they are compared and 
 * a new ProteinBox is formed using the updated information.
 * <p> That information is then posted to Wikipedia through the BotController's
 * WikipediaInterface, the update is marked as completed, and the bot moves on
 * to the next update. If any recoverable exception is flagged in the ExceptionHandler,
 * the bot skips that identifier and moves on, preferring to simply drop problematic
 * templates rather than failing completely. 
 * <p> If the bot finishes, or a fatal exception is flagged, the bot exits and prints 
 * a summary report that includes any identifiers the bot was unable to process. 
 * @author eclarke@scripps.edu
 *
 */
public class BotController implements Runnable {

	/* ---- Declarations ---- */
	protected final Logger logger;				
	protected final DatabaseManager dbManager;	// Manages SQLite db to track changes
	protected final ExceptionHandler botState;	// ExceptionHandler to communicate bot state
	
	protected	WikipediaInterface	wpi;
	
	public		List<String> identifiers;
	protected 	List<String> completed;
	protected	List<String> failed;
	
	protected 	ProteinBox sourceData;
	protected 	ProteinBox wikipediaData;
	
	private 	int delay;
	
	
	/* ---- Constructors ---- */
	public BotController(List<String> identifiers, ExceptionHandler exh) {
		logger = Logger.getLogger(BotController.class.getName());
		dbManager = new DatabaseManager();
		botState = exh;
		
		this.wpi = new WikipediaInterface(botState, Configs.INSTANCE);
		
		this.delay = 3;
		this.identifiers = new ArrayList<String>();
		for (String id : identifiers) {
			try {
				Integer.parseInt(id);
				this.identifiers.add(id);
			} catch (NumberFormatException e) {
//				System.out.println("Identifier \""+e+"\" is not a valid Entrez ID, omitting.");
			}
		}
		this.completed = new ArrayList<String>(0);
		this.failed = new ArrayList<String>(0);
	}
	
	public BotController(List<String> identifiers) {
		this(identifiers, ExceptionHandler.INSTANCE);
	}
	
	public BotController(List<String> identifiers, int delay) {
		this(identifiers);
		this.delay = delay;
	}
	
	/* ---- Main method (runs in thread) ---- */
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
	
	
	
	/**
	 * Updates using the given ProteinBox object.
	 */
	public boolean update(ProteinBox update) throws Exception {
		if (botState.isFine()) {
			wpi.putTemplate(update.toString(), update.getId(), update.getSummary());
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
	public void reset() {
		if (sourceData != null && wikipediaData != null) {
			sourceData.reset();
			wikipediaData.reset();
		}
		botState.reset();
		logger.fine("Bot reset.");
	}
	
	/* ---- Private importer methods ---- */
	
	private ProteinBox importSourceData(String id) {
		MyGeneInfoParser parser = new MyGeneInfoParser();
		return Preconditions.checkNotNull(parser.parse(id));
	}
	
	private ProteinBox importWikipediaData(String id) {
		String content = wpi.getContentForId(id);
		InfoboxParser parser = InfoboxParser.factory(content);
		try {
			ProteinBox result = parser.parse();
			result.setId(id);
			return result;
		} catch (NoBotsException e) {
			botState.recoverable(e);
			return null;
		} catch (ValidationException e) {
			botState.recoverable(e);
			return null;
		}
	}
	
	/* ---- Public methods ---- */
	
	public String prepareReport() {
		StringBuilder sb = new StringBuilder();
		sb.append(				"| Completion report: \n");
		sb.append(				"|------------------------------- \n");
		sb.append(String.format("|  Completed updates: %d/%d \n", this.completed.size(), this.identifiers.size()));
		sb.append(String.format("|  Failed updates:    %d/%d \n", this.failed.size(), this.identifiers.size()));
		sb.append(				"|  \n");
		sb.append(				"|  Protein boxes updated: \n");
		for (String str : completed) {
			sb.append(			"|   "+str+"\n");
		}
		sb.append(				"|  Failed to update: \n");
		for (String str : failed) {
			sb.append(			"|   "+str+"\n");
		}
		String report = sb.toString();
		System.out.println(report);
		
		return report;
	}
}
