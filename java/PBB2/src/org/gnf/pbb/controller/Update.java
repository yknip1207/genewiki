package org.gnf.pbb.controller;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gnf.pbb.AbstractUpdate;
import org.gnf.pbb.Configs;

import org.gnf.pbb.exceptions.ConfigException;
import org.gnf.pbb.exceptions.ExceptionHandler;
import org.gnf.pbb.exceptions.PbbExceptionHandler;
import org.gnf.pbb.logs.DatabaseManager;
import org.gnf.pbb.util.ListUtils;
import org.gnf.pbb.wikipedia.WikipediaController;

public class Update {
	/* ---- Declarations ---- */
	private final Logger logger;				
	private final DatabaseManager dbManager;	// Manages SQLite db to track changes
	private final ExceptionHandler botState;	// ExceptionHandler to communicate bot state
	
	private boolean strict;			// Checks for valid template name & nobots flag
	private boolean verbose;		// Sets base logger level to INFO
	private boolean initialized;	// Indicates that there's two sets of info to merge
	
	private LinkedHashMap<String, List<String>> sourceData, wikipediaData;
	
	private String id;
	private String editMessage;
	private String editSummary;
	private String formattedOutput;
	
	
	/* ---- Constructors ---- */
	
	public Update(ExceptionHandler exh) {
		logger = Logger.getLogger(AbstractUpdate.class.getName());
		dbManager = new DatabaseManager();
		botState = exh;
		
		try {							// Set relevant configurations from file
			strict = Configs.GET.flag("strict");
			verbose = Configs.GET.flag("verbose");
		} catch (ConfigException e) {	// Failure to set these = a fatal error
			botState.fatal(e);
		}
		
		if (verbose) {
			logger.setLevel(Level.INFO);
		} else {
			logger.setLevel(Level.SEVERE);
		}
		
		initialized = false;
	}
	
	public Update(ExceptionHandler exh, String id, 
			LinkedHashMap<String, List<String>> sourceData, 
			LinkedHashMap<String, List<String>> wikipediaData) {
		this(exh);
		this.sourceData = sourceData;
		this.wikipediaData = wikipediaData;
		this.id = id;
		this.initialized = true;
	}
	
	public Update(String id, 
			LinkedHashMap<String, List<String>> sourceData, 
			LinkedHashMap<String, List<String>> wikipediaData) {
		this(PbbExceptionHandler.INSTANCE, id, sourceData, wikipediaData);
		this.initialized = true;
	}
	
	
	/* ---- Public methods ---- */
	
	/**
	 * For each field in sourceData, check to see if a corresponding field exists in 
	 * the wikipediaData map. If it does and a) the wiki data is equal to it, do nothing;
	 * b) the wiki data is either missing or not equal, overwrite it. If the information
	 * exists only in the wiki data, preserve it.
	 * @param sourceData "new" information
	 * @param wikipediaData "old" information
	 * @return fields with data merged according to rules described
	 */
	public LinkedHashMap<String, List<String>> merge(
			LinkedHashMap<String, List<String>> sourceData, 
			LinkedHashMap<String, List<String>> wikipediaData) {
		StringBuilder _editMessage = new StringBuilder();
		LinkedHashMap<String, List<String>> mergedData = wikipediaData;
		int updated = 0;
		
		for (String key : sourceData.keySet()) {
			List<String> sourceValues = sourceData.get(key);
			List<String> wikiValues = wikipediaData.get(key);	// This may be null
			
			/* ---- Testing for conditions where we would not update ---- */
			
			// If wiki values match, don't update 
			if (wikiValues != null) {
				if (wikiValues.equals(sourceValues))
					continue;	// 'continue' allows wiki data to remain as-is, no update
				if (wikiValues.containsAll(sourceValues))
					continue;
			}
			
			// If source values are empty or null, don't update
			if (sourceValues.isEmpty() || sourceValues == null) {
				continue;
			}
			
			// We don't yet gather information to update these fields
			// TODO: add image collection
			if (key.equals("image") || key.equals("image_source")) {
				continue;
			}
			
			// A highly redundant way of checking for all sourceValues being
			// in the wikiValues; also prevents random shuffling from parsers
			// TODO: clean this up
			if (sourceValues != null && wikiValues != null) {
				int matches = 0;
				Set<String> srcSet = new HashSet<String>(sourceValues);
				Set<String> infoSet = new HashSet<String>(wikiValues);
				for (String src : srcSet) {
					if (infoSet.contains(src))
						matches++;
				}
				if (matches == srcSet.size())
					continue;
				Collections.sort(wikiValues);
				Collections.sort(sourceValues);
				if (wikiValues.containsAll(sourceValues)) 
					continue;
			}
			
			/* ---- Overwrite the field and log the change ---- */
			
			mergedData.put(key, sourceValues);
			_editMessage.append(key+", ");
			try {
				dbManager.addChange(id, key, ListUtils.toString(wikiValues), ListUtils.toString(sourceValues));
			} catch (SQLException e) {
				botState.fatal(e);
			}
		}
		// Finalize the edit message
		if (_editMessage.lastIndexOf(", ") != -1)
			_editMessage.deleteCharAt(_editMessage.lastIndexOf(","));
		_editMessage.append("\n"+(wikipediaData.size()-updated)+" fields left unchanged.");
		this.editMessage = _editMessage.toString();
		
		// The edit summary is used when updating Wikipedia
		this.editSummary = "ProteinBoxBot2 updated "+updated+" fields.";
		
		return mergedData;
	}
	
	/**
	 * Formats the updated data as a proper ProteinBox template
	 * @param updatedData
	 * @return
	 */
	public String format(LinkedHashMap<String, List<String>> updatedData) {
		String outputString = "";
		try {
			String nl = System.getProperty("line.separator");
			StringBuilder out = new StringBuilder();
			out.append("{{GNF_Protein_box"+nl);
			Set<String> keys = updatedData.keySet();
			
			for (String key: keys) {
				// We don't handle these keys in this loop, so we skip them
				if (key.equals("TextBeforeTemplate") || key.equals("TextAfterTemplate")) {
					continue;
				} else if (key.equalsIgnoreCase("Function") || 
						key.equalsIgnoreCase("Process") || 		
						key.equalsIgnoreCase("Component") ||	
						key.equalsIgnoreCase("pdb")) {			
					out.append(" | "+key+" = ");
					for (String link : updatedData.get(key)) {
						if (link.length() != 0) {
							out.append("{{");
							// PDB ids require a standard PDB2| prefix in their tags
							if (key.equalsIgnoreCase("pdb")) { 
								out.append("PDB2|"+link+"}}, ");
							} else {
								out.append(link+"}} ");
							}
						}
					}
					// Removes the trailing comma from a list of template links
					if (out.charAt(out.length()-2) == ',')
						out.deleteCharAt(out.length()-2); 
					out.append(nl);
				} else if (updatedData.get(key).size() > 0) {
					String value = updatedData.get(key).get(0);
					if (key.equals("Name")) {
						value = Character.toUpperCase(value.charAt(0)) + value.substring(1);
					}

					if (key.equals("AltSymbols")) {
						// format of this field requires a leading semicolon
						out.append(" | "+key+" ="+value+nl);
					} else {
						out.append(" | "+key+" = "+value+nl);
					}
				}
			}
			out.append("}}");
			
			// Handle any text that may have appeared before or after the template,
			// like reference tags or something (usually after template).
			if (updatedData.get("TextAfterTemplate") != null) {
				out.append((updatedData.get("TextAfterTemplate")).get(0));
			}
			if (updatedData.get("TextBeforeTemplate") != null)
				out.insert(0, updatedData.get("TextBeforeTemplate"));
			
			outputString = out.toString();
		} catch (Exception e) {
			botState.recoverable(e);
		}

		return outputString;
	}
	
	/**
	 * Tests the given linked hash map for valid data according to a set of assertions.
	 * Sets bot state to Recoverable and returns false if any validation errors occured.
	 * 
	 * @param mergedData
	 * @return
	 */
	public boolean validateData(LinkedHashMap<String, List<String>> mergedData) {
		try {
			assert !mergedData.isEmpty();
			assert !(mergedData == null);
			assert !(mergedData.size() < 10);
			assert !(mergedData.get("Name").equals("") 
					|| mergedData.get("Name").equals(" ") 
					|| mergedData.get("Name").equals(null));

		} catch (Exception e) {
			botState.recoverable(e);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Tests the given string (usually what's about to be pushed to Wikipedia) for proper
	 * formatting and infobox structure.
	 * @param output
	 * @return
	 */
	public boolean validateOutputString(String output){
		try {
			/*
			 * TODO Need some assertions...
			 */
		} catch (Exception e) {
			botState.recoverable(e);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Pushes information to Wikipedia or cache directory, if it's a dryrun.
	 * This method hinges on the botState being NONE, indicating no errors or
	 * exceptions were passed to the ExceptionHandler. If it is above Severity.NONE,
	 * the method returns false.
	 * @param wpControl
	 * @param update
	 * @param id
	 * @param summary
	 */
	public boolean push(WikipediaController wpControl, String update, 
			String id, String summary) {
		if (botState.isFine()) {
			try {
				wpControl.putContent(update, id, summary);
				logger.info("Summary: "+summary);
				return true;
			} catch (Exception e) {
				botState.recoverable(e);
				return false;
			}
		} else {
			logger.severe("Bot state for id "+id+" is above Severity.NONE; " +
					"push to Wikipedia cancelled.");
			return false;
		}
	}
	
	/* ---- Public convenience methods ---- */
	public void merge() {
		if (this.initialized) {
			this.formattedOutput = format(merge(this.sourceData, this.wikipediaData));
		} else {
			botState.recoverable(new Exception("Update not initialized with two data sets!"));
		}
	}
	public void push(WikipediaController wpControl) {
		if (this.initialized) {
			this.push(wpControl, this.formattedOutput, this.id, this.editSummary);
		}
	}
	
	
	
	
	
}