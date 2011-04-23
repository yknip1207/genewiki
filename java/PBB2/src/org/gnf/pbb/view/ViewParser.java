/**
 * 
 */
package org.gnf.pbb.view;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author eclarke
 *
 */
public interface ViewParser {

	LinkedHashMap<String, List<String>> getViewDataAsMap(ViewController controller, String identifier, boolean useCache);
	
	void parseDataFromText(String rawText) throws Exception;
	
	void forceDataIntegrityCheck();
}
