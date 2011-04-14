package org.gnf.pbb.controller;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.gnf.pbb.GeneObject;

/**
 * Main controller class for the MVC system of parsing and manipulating Wikipedia infoboxes.
 * Unique functions include a mapping of data terms to field names and comparison handling.
 * 
 * @author eclarke
 *
 */


public class PBBController implements Controller {
	private final static Logger logger = Logger.getLogger(PBBController.class.getName());
	WikipediaInterface wiki;
	
	public PBBController() {
		wiki = new WikipediaInterface();
	}
	
	@Override
	public GeneObject importObjectData(String objectIdentifier) {
		JsonParser jsonParser = new JsonParser();
		GeneObject gene = null;
		
		try {
			gene = jsonParser.newGeneFromId(Integer.parseInt(objectIdentifier));
		} catch (JsonParseException e) {
			logger.severe("Fatal error parsing json file.");
			e.printStackTrace();
		} catch (JsonMappingException e) {
			logger.severe("Fatal error mapping json values.");
			e.printStackTrace();
		} catch (NumberFormatException e) {
			logger.severe("Error parsing object identifier. Identifier must be Entrez id, consisting only of numbers.");
			e.printStackTrace();
		} catch (IOException e) {
			logger.severe("Fatal input/output error.");
			e.printStackTrace();
		}
		
		return gene;
	}

	@Override
	public LinkedHashMap<String, List<String>> importDisplayData(
			String displayIdentifier) {
		InfoBoxParser infoBoxParser = new InfoBoxParser();
		infoBoxParser.newBoxFromTitle(wiki, displayIdentifier, true);
		//infoBoxParser.fieldsIntegrityCheck();
		return infoBoxParser.getFields();
	}

	@Override
	public boolean compareTerms(String objectTerm, String displayTerm) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LinkedHashMap<String, List<String>> updateDisplayTerms(
			Object object, LinkedHashMap<String, List<String>> display) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String outputDisplay(LinkedHashMap<String, List<String>> display) {
		// TODO Auto-generated method stub
		return null;
	}

}
