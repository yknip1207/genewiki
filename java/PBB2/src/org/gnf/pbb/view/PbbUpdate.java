package org.gnf.pbb.view;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang.WordUtils;

import org.gnf.pbb.controller.ExternalSystemInterface;
import org.gnf.pbb.exceptions.ValidationException;

public class PbbUpdate implements Update {
	private final static Logger logger = Logger.getLogger(Update.class.getName());
	private final LinkedHashMap<String, Boolean> configs;
	private LinkedHashMap<String, List<String>> updatedData;
	private String status;
	private String id;
	private String editMessage;
	private String editSummary;
	private String formattedContent;
	private boolean isValidated;
	

	private PbbUpdate(LinkedHashMap<String, Boolean> configs) {
		this.configs = configs;
	}
	
	/**
	 * Constructs a new PbbUpdate that self-checks its validity.
	 * @param model
	 * @param view
	 * @param forceUpdate
	 */
	public static PbbUpdate PbbUpdateFactory(LinkedHashMap<String, List<String>> model,
			LinkedHashMap<String, List<String>> view, LinkedHashMap<String, Boolean> configs) {
		PbbUpdate update = new PbbUpdate(configs);
		update.updateData(model, view);
		try {
			update.formattedContent = update.asFormattedString();
		} catch (ValidationException e) {
			e.getError();
			e.getMessage();
			e.printStackTrace();
		}
		logger.fine("Created new PbbUpdate object.");
		return update;
	}
	
	/**
	 * Constructs a new PbbUpdate with the validation manually overriden.
	 * @param model
	 * @param view
	 * @param skipValidation
	 */
	public static PbbUpdate PbbUpdateFactory(LinkedHashMap<String, List<String>> model, 
			LinkedHashMap<String, List<String>> view, LinkedHashMap<String, Boolean> configs, boolean skipValidation) {
		
		PbbUpdate update = PbbUpdateFactory(model, view, configs);
		
		// Passing the skipValidation flag as true overrides any validation checks (not recommended)
		update.isValidated = skipValidation;
		
		// Setting the name of this update
		update.id = update.updatedData.get("Name").get(0);
		logger.warning("Created new PbbUpdate object with validation state manually overriden.");
		return update;
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
	private String asFormattedString() throws ValidationException {
		String _update = "";
		if (isValidated) {
			String nl = System.getProperty("line.separator");
			StringBuffer out = new StringBuffer();
			out.append("{{GNF_Protein_box"+nl);
			Set<String> keys = updatedData.keySet();
			for (String key: keys) {
				if (key.equalsIgnoreCase("Function") || 
						key.equalsIgnoreCase("Process") || 
						key.equalsIgnoreCase("Component") ||
						key.equalsIgnoreCase("pdb")) {
					out.append(" | "+key+" = ");
					Iterator<String> templateLinks = updatedData.get(key).iterator();
					while (templateLinks.hasNext()) {
						String next = templateLinks.next();
						if (next.length() != 0) {
							out.append("{{");
							if (key.equalsIgnoreCase("pdb")) // pdb ids require a standard PDB2| prefix in their template tags
								out.append("PDB2|");
							out.append(next+"}} ");
						}	
					}
					out.append(nl);
				} else if (updatedData.get(key).size() > 0) {
					String value = updatedData.get(key).get(0);
					if (key.equals("Name")) {
						value = Character.toUpperCase(value.charAt(0)) + value.substring(1);
					}
					if (key.equals("AltSymbols")) {
						// format of this field requires a leading semicolon
						out.append(" | "+key+" =; "+value+nl);
					} else {
						out.append(" | "+key+" = "+value+nl);
					}
				}
			}
			out.append("}}");
			_update = out.toString();
		} else {
			logger.warning("Validation state is false; this method refuses to return possibly damaged data to updater.");
			throw new ValidationException(status);
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
	
	// Probably could use some more validation checks, this is all I could think of at the moment.
	private void validateUpdate(LinkedHashMap<String,List<String>> view, LinkedHashMap<String,List<String>> update) {
		StringBuffer _status = new StringBuffer();
		this.isValidated = true; // an optimistic beginning
		
		if (update.size() < 10) {
			_status.append("ERROR: update size is less than 10 fields. \n");
			this.isValidated = false;
		} 
		if (view.size() > update.size()) {
			_status.append("WARNING: update size is smaller than view size, data may have been lost. Verification recommended. \n");
		} 
		if (update.get("Name").equals("") || update.get("Name").equals(" ") || update.get("Name").equals(null)) {
			_status.append("ERROR: updated name field is empty; parse error? \n");
			this.isValidated = false;
		}
		if (_status.length() > 0)
			logger.warning(_status.toString());
		
		this.status = _status.toString();
	}

	private void updateData(LinkedHashMap<String, List<String>> model,
			LinkedHashMap<String, List<String>> view) {
		
		StringBuffer _editMessage = new StringBuffer();
		Set<String> objectKeys = model.keySet();
		LinkedHashMap<String, List<String>> newDisplayData = view;
		int matches = 0;
		if (model != null && view != null) {
			_editMessage.append("Updated fields: ");
			for (String key : objectKeys) {
				List<String> objectValues = model.get(key), displayValues = view.get(key);
				
				// I've wrapped this in two separate "if" blocks because evaluating the second if the first is null
				// returns a NullPointerException
				if (objectValues != null && displayValues != null) {
					Set<String> dispSet = new HashSet<String>(displayValues);
					Set<String> objSet = new HashSet<String>(objectValues);
					for (String val : objSet) {
						if (dispSet.contains(val)) {
							logger.finer("SAME: For key "+key+", "+displayValues+" is equal to "+objectValues+".");
							matches++;
						} else {
							newDisplayData.put(key, objectValues);
							System.out.println("DIFF: For key "+key+", "+displayValues+" was replaced by "+objectValues+".");
							_editMessage.append(key+", ");
						}
					}
				}
			}
			if (_editMessage.lastIndexOf(", ") != -1)
				_editMessage.deleteCharAt(_editMessage.lastIndexOf(","));
			_editMessage.append("\n"+matches+" fields left unchanged.");
			
			editSummary = "Protein Box Bot 2 updated "+ (view.size() - matches) + "/" + view.size()+" fields.";
		}
		// Make sure nothing insane happened (empty map, no name, loss of data, etc)
		validateUpdate(view, newDisplayData);
		
		// Sets the object's edit summary
		this.editMessage = _editMessage.toString();
		// Sets the object's updatedData map
		this.updatedData = newDisplayData;
		// Sets the object's identifier
		this.id = newDisplayData.get("Hs_EntrezGene").get(0);
		
		logger.fine("New view data set for gene id: "+this.id);
	}

	@Override
	public void updateView(ExternalSystemInterface viewControl, boolean DRY_RUN) {
		
		try {
			logger.fine("Handing over the reigns to "+viewControl.getClass().getName()+" with updated data...");
			logger.fine("Summary: "+getEditSummary());
			viewControl.putContent(formattedContent, getId(), getEditSummary(), DRY_RUN);
			
		} catch (Exception e) {
			logger.severe(e.getCause().getMessage());
			e.printStackTrace();
		}
		
	}

	@Override
	public String getId() {
		return this.id;
	}

	public LinkedHashMap<String, Boolean> getConfigs() {
		return configs;
	}

}
