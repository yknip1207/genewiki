
package org.gnf.pbb.view;

/**
 * Manages authentication, view representation (including view retrieval and updating), cache system, and 
 * logging.
 * @author eclarke
 */
public interface ViewController {

	/**
	 * Retrieves the specified content from the view source (or, if available,
	 * the cached version of the content).
	 * @param contentId
	 * @param useCache
	 * @return string representation of the content
	 */
	String retrieveContent(String contentId, boolean useCache);
	
	/**
	 * Tests authentication state of the ViewController.
	 * @return true if logged in
	 */
	boolean isAuthenticated();
	
	/**
	 * Logs ViewController in. Should call isAuthenticated() before attempting to login.
	 * @param credentials file path
	 * @throws Exception 
	 */
	void authenticate(String credentials) throws Exception;
	
	/**
	 * Updates view source with the content. Should run verification checks on content format.
	 * Should obey the dry run flag to write output to cache directory for testing.
	 * @param content as appropriate object representation of the data (with verification abilities).
	 * @param dryRun flag.
	 * @return any response codes from view source indicating update success or status.
	 * @throws Exception 
	 */

	String update(String content, String title, String changes, boolean dryRun)
			throws Exception;
	
	
}