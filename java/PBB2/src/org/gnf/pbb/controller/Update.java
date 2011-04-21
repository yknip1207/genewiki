package org.gnf.pbb.controller;

import java.util.LinkedHashMap;
import java.util.List;


/**
 * 
 * An update object holds protected methods to conduct an update, given
 * two sets of linked hash maps representing the model and view data.
 * The object can be set to prevent updates until verification checks are
 * passed, which is important if the program is running in batch mode.
 * @author eclarke
 *
 */
public interface Update {

	/**
	 * Get validation state of update. Anything besides "true" prevents
	 * the return of the getUpdateAsString method, so it's good to check this.
	 * @return validation as true or false
	 */
	public boolean getValidation();
	
	/**
	 * Returns a brief summary of the edits made during the update to post on 
	 * Wikipedia.
	 * @return edit summary
	 */
	public String getEditMessage();
	
	/**
	 * Returns the status of the update (any warnings, errors, etc)
	 * @return status of update
	 */
	public String getStatus();
	
	/**
	 * Returns a list of the updated keys and values
	 * @return updated values as string
	 */
	public String toString();
	
	/**
	 * Returns the update in its original map format; useful for debugging any potential
	 * errors in validation.
	 * @return linked hash map of updated values
	 */
	public LinkedHashMap<String, List<String>> toMap();
	
	/**
	 * Updates remote view with updated data map given a specified view controller
	 * @param updates
	 * @param view controller
	 * @param DRY_RUN
	 */
	public void updateView(ViewController viewControl, boolean DRY_RUN);

	/**
	 * Returns the unique identifier for this update (title, gene ID, etc)
	 * @return unique identifier
	 */
	public String getId();


}
