package org.gnf.pbb.controller;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.WordUtils;

import org.gnf.pbb.exceptions.ValidationException;

public class PbbUpdate implements Update {
	private LinkedHashMap<String, List<String>> updatedData;
	private String status;
	private String id;
	private String editMessage;
	private String editSummary;
	private String formattedContent;
	private boolean isValidated;
	
	@SuppressWarnings("unused")
	private PbbUpdate() {
		// Do not call this; updatedData map should always be initialized with
		// the appropriate comparison routines
	}
	
	/**
	 * Constructs a new PbbUpdate that self-checks its validity.
	 * @param model
	 * @param view
	 * @param forceUpdate
	 */
	public PbbUpdate(LinkedHashMap<String, List<String>> model,
			LinkedHashMap<String, List<String>> view, boolean forceUpdate) {
		updateData(model, view, forceUpdate);
		try {
			formattedContent = this.asFormattedString();
		} catch (ValidationException e) {
			e.getError();
			e.getMessage();
			e.printStackTrace();
		}
	}
	
	/**
	 * Constructs a new PbbUpdate with the validation manually overriden.
	 * @param model
	 * @param view
	 * @param forceUpdate
	 * @param skipValidation
	 */
	public PbbUpdate(LinkedHashMap<String, List<String>> model, 
			LinkedHashMap<String, List<String>> view, boolean forceUpdate, boolean skipValidation) {
		// This method runs validation checks and sets the validation flag
		updateData(model, view, forceUpdate);
		try {
			formattedContent = this.asFormattedString();
		} catch (ValidationException e) {
			e.getError();
			e.getMessage();
			e.printStackTrace();
		}
		// Passing the skipValidation flag as true overrides any validation checks (not recommended)
		isValidated = skipValidation;
		
		// Setting the name of this update
		this.id = updatedData.get("Name").get(0);
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
				if (updatedData.get(key).size() > 1 && (
						key.equalsIgnoreCase("Function") || 
						key.equalsIgnoreCase("Process") || 
						key.equalsIgnoreCase("Component") ||
						key.equalsIgnoreCase("pdb"))) {
					out.append(" | "+key+" = ");
					Iterator<String> templateLinks = updatedData.get(key).iterator();
					while (templateLinks.hasNext()) {
						out.append("{{");
						if (key.equalsIgnoreCase("pdb")) // pdb ids require a standard PDB2| prefix in their template tags
							out.append("PDB2|");
						out.append(templateLinks.next()+"}} ");
					}
					out.append(nl);
				} else if (updatedData.get(key).size() > 0) {
					String value = updatedData.get(key).get(0);
					if (key.equals("Name")) 
						value = WordUtils.capitalize(value);
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
			_status.append("WARNING: update size is smaller than view size, data may be lost. Verification recommended. \n");
		} 
		if (update.get("Name").equals("") || update.get("Name").equals(" ") || update.get("Name").equals(null)) {
			_status.append("ERROR: updated name field is empty; parse error? \n");
			this.isValidated = false;
		}
		
		
		
	}

	private void updateData(LinkedHashMap<String, List<String>> model,
			LinkedHashMap<String, List<String>> view, boolean forceUpdate) {
		
		StringBuffer _editMessage = new StringBuffer();
		Set<String> objectKeys = model.keySet();
		LinkedHashMap<String, List<String>> newDisplayData = view;
		int matches = 0;
		
		_editMessage.append("Updated fields: ");
		for (String key : objectKeys) {
			List<String> objectValues = model.get(key), displayValues = view.get(key);
			
			// I've wrapped this in two separate "if" blocks because evaluating the second if the first is null
			// returns a NullPointerException
			if (objectValues != null && displayValues != null) {
				if (displayValues.containsAll(objectValues)) {
					// XXX
					// System.out.println("SAME: For key "+key+", "+displayValues+" is equal to "+objectValues+".");
					matches++;
				} else {
					newDisplayData.put(key, objectValues);
					// System.out.println("DIFF: For key "+key+", "+displayValues+" was replaced by "+objectValues+".");
					_editMessage.append(key+", ");
				}
			}
	
		}
		_editMessage.deleteCharAt(_editMessage.lastIndexOf(","));
		_editMessage.append("\n"+matches+" fields left unchanged.");
		
		editSummary = "Protein Box Bot 2 updated "+ (view.size() - matches) + "/" + view.size()+" fields.";
		
		// Make sure nothing insane happened (empty map, no name, loss of data, etc)
		validateUpdate(view, newDisplayData);
		
		// Sets the object's edit summary
		this.editMessage = _editMessage.toString();
		// Sets the object's updatedData map
		this.updatedData = newDisplayData;
		// Sets the object's identifier
		this.id = newDisplayData.get("Hs_EntrezGene").get(0);
	}

	@Override
	public void updateView(ViewController viewControl, boolean DRY_RUN) {
		
		try {
			viewControl.update(formattedContent, getId(), getEditSummary(), DRY_RUN);
		} catch (Exception e) {
			System.out.println(e.getCause().getMessage());
			e.printStackTrace();
		}
		
	}

	@Override
	public String getId() {
		return this.id;
	}

}
