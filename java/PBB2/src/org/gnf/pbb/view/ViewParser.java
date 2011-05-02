/**
 * 
 */
package org.gnf.pbb.view;

import java.util.LinkedHashMap;
import java.util.List;

import org.gnf.pbb.controller.ExternalSystemInterface;

/**
 * @author eclarke
 *
 */
public interface ViewParser {

	LinkedHashMap<String, List<String>> getViewDataAsMap(ExternalSystemInterface controller, String identifier, boolean useCache);
	
	void parseDataFromText(String rawText) throws Exception;
	
	void forceDataIntegrityCheck();
}
