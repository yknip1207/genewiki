package org.gnf.pbb.controller;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gnf.pbb.Configs;
import org.gnf.pbb.Update;
import org.gnf.pbb.exceptions.ConfigException;
import org.gnf.pbb.exceptions.PbbExceptionHandler;
import org.gnf.pbb.exceptions.ValidationException;
import org.gnf.pbb.logs.DatabaseManager;
import org.gnf.pbb.util.ListUtils;
import org.gnf.pbb.wikipedia.WikipediaController;

public class PbbUpdate implements Update {
	private final static Logger logger = Logger.getLogger(Update.class.getName());
	private DatabaseManager db;
	private static PbbExceptionHandler botState;
	private LinkedHashMap<String, List<String>> updatedData;
	private String status;
	private String id;
	private String editMessage;
	private String editSummary;
	private String formattedContent;
	private boolean strict;
	private boolean verbose;
	private boolean isValidated;
	
	
	private PbbUpdate(PbbExceptionHandler exh) {
		botState = exh;
		try {
			strict = Configs.GET.flag("strict");
			verbose = Configs.GET.flag("verbose");
		} catch (ConfigException e) {
			botState.fatal(e);
		}
		if (verbose) {
			logger.setLevel(Level.FINE);
		}
	}
	
	/**
	 * Constructs a new PbbUpdate
	 * @param sourceData
	 * @param wikipediaData
	 */
	public static PbbUpdate PbbUpdateFactory(String id, LinkedHashMap<String, List<String>> sourceData,
			LinkedHashMap<String, List<String>> wikipediaData, PbbExceptionHandler _exhandler) {
		PbbUpdate update = new PbbUpdate(_exhandler);
		update.db = new DatabaseManager();
		update.id = id;
		update.updateData(sourceData, wikipediaData);
		if (botState.isFine()) {
			update.formattedContent = update.asFormattedString();
			logger.fine("Created new PbbUpdate object");
		} else if (botState.canExecute()) {
			update.formattedContent = update.asFormattedString();
			logger.warning("Created new PbbUpdate object but validation failed...");
		}
		logger.fine("Created new PbbUpdate object.");
		return update;
	}

	public static Update PbbUpdateFactory(String id,
			LinkedHashMap<String, List<String>> sourceData,
			LinkedHashMap<String, List<String>> wikipediaData) {
		return PbbUpdateFactory(id, sourceData, wikipediaData, PbbExceptionHandler.INSTANCE);
	}

	
	@Override
	public boolean getValidation() {
		return isValidated;
	}
	@Override
	public String getEditMessage() {
		return editMessage;
	}
	public String getEditSummary() {
		return editSummary;
	}
	@Override
	public String getStatus() {
		return status;
	}
	
	/**
	 * Outputs the updated data formatted for upload
	 * @return formatted string
	 * @throws ValidationException
	 */
	public String asFormattedString() {
		String _update = "";
		try {
			String nl = System.getProperty("line.separator");
			StringBuffer out = new StringBuffer();
			out.append("{{GNF_Protein_box"+nl);
			Set<String> keys = updatedData.keySet();
			for (String key: keys) {
				if (key.equals("TextBeforeTemplate") || key.equals("TextAfterTemplate")) {
					continue;
				} else if (key.equalsIgnoreCase("Function") || 
						key.equalsIgnoreCase("Process") || 
						key.equalsIgnoreCase("Component") ||
						key.equalsIgnoreCase("pdb")) {
					out.append(" | "+key+" = ");
					Iterator<String> templateLinks = updatedData.get(key).iterator();
					while (templateLinks.hasNext()) {
						String next = templateLinks.next();
						if (next.length() != 0) {
							out.append("{{");
							if (key.equalsIgnoreCase("pdb")) { // pdb ids require a standard PDB2| prefix in their template tags
								out.append("PDB2|"+next+"}}, ");
							} else {
								out.append(next+"}} ");
							}
						}
					}
					if (out.charAt(out.length()-2) == ',')
						out.deleteCharAt(out.length()-2); // Removes the trailing comma from a list of template links
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
			if (updatedData.get("TextAfterTemplate") != null) {
				out.append((updatedData.get("TextAfterTemplate")).get(0));
			}
			if (updatedData.get("TextBeforeTemplate") != null)
				out.insert(0, updatedData.get("TextBeforeTemplate"));
			_update = out.toString();
		} catch (Exception e) {
			// If this method is called without checking canUpdate flag, 
			// it will likely throw an exception and fail.
			botState.recoverable(e);
		}
		
		return _update;
	}
	
	@Override
	public String toString() {
		StringBuffer out = new StringBuffer();
		out.append("Fields map size: "+updatedData.size());
		for (String str : updatedData.keySet()) {
			out.append("Value for key " + str + ": " + updatedData.get(str));
		}
		
		return out.toString();
	}
	
	@Override
	public LinkedHashMap<String, List<String>> toMap() {
		return this.updatedData;
	}
	
	/**
	 * 
	 * @param oldInfobox
	 * @param newInfobox
	 * @return
	 */
	private boolean validateUpdate(LinkedHashMap<String,List<String>> oldInfobox, LinkedHashMap<String,List<String>> newInfobox) {
		StringBuffer _status = new StringBuffer();
		this.isValidated = true; // an optimistic beginning
		
		try {
			if (newInfobox == null) {
				throw new Exception("Update object is null.");
			}
			if (newInfobox.size() < 10) {
				_status.append("Validation warning: update size is less than 10 fields. \n");
				this.isValidated = false;
			} 
			if (oldInfobox.size() > newInfobox.size()) {
				_status.append("Validation warning: update size is smaller than view size, data may have been lost. " +
						"Verification recommended. \n");
			} 
			if (newInfobox.get("Name").equals("") || newInfobox.get("Name").equals(" ") || newInfobox.get("Name").equals(null)) {
				_status.append("Validation warning: updated name field is empty; parse error? \n");
				this.isValidated = false;
			}
		} catch (Exception e) {
			logger.severe("Validation failed with exception: "+e.getMessage());
			botState.recoverable(e);
		}
		
		this.status = _status.toString();
		if (this.status.length() > 0 ) {
			logger.warning(status);
		}
		if (strict && !this.isValidated) {
			botState.recoverable(new Exception("Failed validation with strict checking enabled."));
		}
		return this.isValidated;
	}

	/**
	 * For each field in the source data, check to see if a corresponding field exists in the
	 * infobox data; if it does and the infobox data is equal to the source, do nothing. 
	 * Otherwise overwrite or create the field in the new infobox data and count it as an updated field.
	 * @param sourceData
	 * @param infoboxData
	 */
	private void updateData(LinkedHashMap<String, List<String>> sourceData, LinkedHashMap<String, List<String>> infoboxData) {
		StringBuffer _editMessage = new StringBuffer();
		LinkedHashMap<String, List<String>> newInfoboxData = infoboxData;
		try {
			int updated = 0;
			
			// The comparison:
			for (String key : sourceData.keySet()) {
				
				List<String> sourceValues = sourceData.get(key), infoboxValues = infoboxData.get(key);
				// make sure we even have this field in the infobox (if we don't, we'll hit a NullPointerException)
				boolean update = true;
				
				// This logic exists because we need to test for the non-nullity of infoboxValues before
				// we can execute a comparison with it. Writing it out without the update boolean would 
				// result in code duplication.
				if (infoboxValues != null) {
					if (infoboxValues.equals(sourceValues))
						continue;
					if (infoboxValues.containsAll(sourceValues))
						continue;
				}
				if (sourceValues.isEmpty() || sourceValues == null) {
					continue;
				}
				
				// Don't yet have the capability to handle image and image sources - 5/16/11
				if (key.equals("image") || key.equals("image_source")) {
					continue;
				}
				
				if (sourceValues != null && infoboxValues != null) {
					int matches = 0;
					Set<String> srcSet = new HashSet<String>(sourceValues);
					Set<String> infoSet = new HashSet<String>(infoboxValues);
					for (String src : srcSet) {
						if (infoSet.contains(src))
							matches++;
					}
					if (matches == srcSet.size())
						continue;
					Collections.sort(infoboxValues);
					Collections.sort(sourceValues);
					if (infoboxValues.containsAll(sourceValues)) 
						continue;
				}
				
				
				if (update) {
					logger.fine("Values for "+key+" updated.");
					db.addChange(id, key, ListUtils.toString(infoboxValues), ListUtils.toString(sourceValues));
					if (verbose)
						logger.fine("DIFF: original = "+infoboxValues+"; new = "+sourceValues);
					newInfoboxData.put(key, sourceValues);
					_editMessage.append(key+", ");
					updated++;
				} else {
					logger.fine("Values for "+key+" were already equal.");
				}
				
			}
			if (_editMessage.lastIndexOf(", ") != -1)
				_editMessage.deleteCharAt(_editMessage.lastIndexOf(","));
			_editMessage.append("\n"+(infoboxData.size()-updated)+" fields left unchanged.");
			
			editSummary = "ProteinBoxBot2 updated "+updated+" fields.";
		} catch(NullPointerException npe) {
			botState.recoverable(npe);
			return;
		} catch (Exception e) {
			botState.recoverable(e);
			return;
		}
		
		// Make sure nothing insane happened (empty map, no name, loss of data, etc)
		if (validateUpdate(infoboxData, newInfoboxData)) {
			// Sets the object's edit summary
			this.editMessage = _editMessage.toString();
			// Sets the object's updatedData map
			this.updatedData = newInfoboxData;
			// Sets the object's identifier
			this.id = newInfoboxData.get("Hs_EntrezGene").get(0);
			logger.fine("New view data set for gene id: "+this.id);
		} else {
			// Less severe failure case; an exception during validation would set the stopExecute flag anyway.
			// Since none of the appropriate fields are set, anything that might call them will fail and so
			// should check
			botState.minor(new Exception("Failed validation, but not fatally. Bot will be unable to update to WP."));
			return; 
		}
		
		
	}

	@Override
	public void update(WikipediaController wpControl) {
		if (botState.isFine()) {
			try {
				if (verbose)
					logger.fine("Handing over the reigns to "+wpControl.getClass().getName()+" with updated data...");
				logger.info("Summary: "+getEditSummary());
				wpControl.putContent(formattedContent, getId(), getEditSummary());
				
			} catch (Exception e) {
				logger.severe(e.getMessage());
				e.printStackTrace();
			}
		} else {
			return;
		}
		
	}

	@Override
	public String getId() {
		return this.id;
	}

}
