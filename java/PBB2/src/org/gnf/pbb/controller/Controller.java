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
	 * Outputs a linked hash map in the original display format as a String object.
	 * @param display
	 * @return
	 */
	String outputDisplay(LinkedHashMap<String, List<String>> display);

	/**
	 * Compares two linked hash maps of keys and values, and checks if the list of values
	 * for a key in the object map is equal to the values in the display map.
	 * If the overwrite flag is true and the values differ in any way besides sequence,
	 * the object's values wholesale replace the display values.
	 * @param key
	 * @param overwrite display values with object values for key 
	 * @return updated display key:value map for output
	 */
	LinkedHashMap<String, List<String>> updateValues(LinkedHashMap<String, List<String>> objectData, 
			LinkedHashMap<String, List<String>> displayData, boolean overwrite);
	

	
}
