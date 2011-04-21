package org.gnf.pbb.controller;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.gnf.pbb.GeneObject;
import org.gnf.pbb.PbbUpdate;
import org.gnf.pbb.Update;
import org.gnf.pbb.exceptions.NoBotsException;

/**
 * Main controller class for the MVC system of parsing and manipulating Wikipedia infoboxes.
 * Handles a wikipedia interface, update object, importing and parsing data, and logging.
 * 
 * @author eclarke
 *
 */


public class PBBController implements Controller {
	private final static Logger logger = Logger.getLogger(PBBController.class.getName());
	private WpController viewController;
	
	
	public PBBController() {
		viewController = new WpController();
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
			String displayIdentifier) throws NoBotsException {
		InfoBoxParser infoBoxParser = new InfoBoxParser();
		infoBoxParser.newDisplayDataMap(viewController, displayIdentifier, true);
		return infoBoxParser.getFields();
	}
	
	/**
	 * Specify whether or not the method should use a cached version,
	 * if available.
	 * @param displayIdentifier
	 * @param useCache
	 * @return
	 * @throws NoBotsException 
	 */
	public LinkedHashMap<String, List<String>> importDisplayData(
			String displayIdentifier, boolean useCache) throws NoBotsException {
		InfoBoxParser ibp = new InfoBoxParser();
		ibp.newDisplayDataMap(viewController, displayIdentifier, useCache);
		return ibp.getFields();
	}


	
	@Override
	public String outputDisplay(Update update) {
		return update.toString();
	}

	@Override
	public Update updateValues(
			LinkedHashMap<String, List<String>> objectData,
			LinkedHashMap<String, List<String>> displayData, boolean overwrite) {
		
		return new PbbUpdate(objectData, displayData, false);
	}
	
	@Override
	public void updateRemoteView(Update update, boolean DRY_RUN) {
		update.updateView(this.viewController, DRY_RUN);
	}


}
