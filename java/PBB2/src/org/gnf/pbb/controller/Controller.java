package org.gnf.pbb.controller;

import java.util.LinkedHashMap;
import java.util.List;

public interface Controller {
	/**
	 * Imports data from an external API or data source and builds an
	 * object representation of the data. Ideally serializable for efficient
	 * storage in case of controller restart.
	 *  
	 * @param objectIdentifier
	 * 			some unique identifier for the object
	 * @return the object
	 */
	Object importObjectData(String objectIdentifier);
	
	/**
	 * Converts a "display" format (for instance, a Wikipedia info box) with 
	 * fields and values into a linked hash map where the fields are the keys.
	 * @param displayIdentifier
	 * @return
	 */
	LinkedHashMap<String, List<String>> importDisplayData(String displayIdentifier);

	/**
	 * Conducts equality comparisons between the terms specified. Translates between
	 * object terms and display terms if they vary by use of a term mapper object.
	 * @param objectTerm
	 * @param displayTerm
	 * @return boolean result of comparison
	 */
	boolean compareTerms(String objectTerm, String displayTerm);
	
	/**
	 * Compares all the display terms with their equivalent terms in the data object
	 * and updates the display terms if they differ from the object's values.
	 * 
	 * @param object
	 * @param display
	 * @return updated linked hash map with appropriate values
	 */
	LinkedHashMap<String, List<String>> updateDisplayTerms(Object object, LinkedHashMap<String, List<String>> display);

	/**
	 * Outputs a linked hash map in the original display format as a String object.
	 * @param display
	 * @return
	 */
	String outputDisplay(LinkedHashMap<String, List<String>> display);
	
	
	
	
}
