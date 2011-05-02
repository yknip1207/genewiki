package org.gnf.pbb.controller;

import java.util.LinkedHashMap;
import java.util.List;

import org.gnf.pbb.exceptions.NoBotsException;
import org.gnf.pbb.exceptions.ValidationException;
import org.gnf.pbb.view.Update;

public interface Controller {

	
	/**
	 * Imports data from an external API or source.
	 * On a primary (top-level) controller, it usually calls two methods on sub-controllers that manage the source and 
	 * target data sources, and sets internal variables based on those calls.
	 * @param uniqueIdentifier
	 */
	void getExternalData(String uniqueIdentifier);
	
	void verifyInternalData();
	
	Update updateTargetFromSource(Object target, Object source);
	
	/**
	 * Returns a string from an object encapsulating the display data.
	 * @param display
	 * @return
	 * @throws ValidationException 
	 */
	String outputDisplay(Update updatedData) throws ValidationException; // I want this to be more general...

	/**
	 * Compares two linked hash maps of keys and values, and checks if the list of values
	 * for a key in the object map is equal to the values in the display map.
	 * If the overwrite flag is true and the values differ in any way besides sequence,
	 * the object's values wholesale replace the display values.
	 * @param key
	 * @param overwrite display values with object values for key 
	 * @return updated display key:value map for output
	 */
	Update updateValues(LinkedHashMap<String, List<String>> objectData, 
			LinkedHashMap<String, List<String>> displayData, boolean overwrite);
	
	/**
	 * Uploads the altered content to the remote view (Wikipedia, etc). (uses internal view controller object)
	 * @param update object
	 */
	void updateRemoteView(Update update, boolean DRY_RUN);
	

	
}
