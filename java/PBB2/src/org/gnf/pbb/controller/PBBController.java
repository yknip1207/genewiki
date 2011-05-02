package org.gnf.pbb.controller;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.gnf.pbb.exceptions.NoBotsException;
import org.gnf.pbb.model.GeneObject;
import org.gnf.pbb.model.JsonParser;
import org.gnf.pbb.view.PbbUpdate;
import org.gnf.pbb.view.Update;
import org.gnf.pbb.view.WikitextParser;
import org.gnf.pbb.view.WpController;

/**
 * Main controller class for the MVC system of parsing and manipulating Wikipedia infoboxes.
 * Handles a wikipedia interface, update object, importing and parsing data, and logging.
 * Can both return imported data and maintain it internally; helper methods use internal
 * representations.
 * @author eclarke
 *
 */


public class PBBController implements Controller {
	private final static Logger logger = Logger.getLogger(PBBController.class.getName());
	private WpController viewController;
	private LinkedHashMap<String, List<String>> geneInfoData, wikipediaData = new LinkedHashMap<String, List<String>>();
	private final LinkedHashMap<String, Boolean> configs = new LinkedHashMap<String, Boolean>();
	
	public PBBController() {
		viewController = new WpController();
		configs.put("DRYRUN", true);
		configs.put("CACHE", true);
		configs.put("STRICT", true);
		configs.put("VERBOSE", true);
	}
	
	public PBBController(boolean dryrun, boolean usecache, boolean strictchecking, boolean verbose) {
		viewController = new WpController();
		configs.put("DRYRUN", dryrun);
		configs.put("CACHE", usecache);
		configs.put("STRICT", strictchecking);
		configs.put("VERBOSE", verbose);
	}
	
	public void runFullUpdateForId(String identifier) {
		importSourceData(identifier);
		getExternalData(identifier);
		Update up = updateValues(geneInfoData, wikipediaData, true);
		updateRemoteView(up, configs.get("DRYRUN"));
	}
	
	@Override
	public void getExternalData(String identifier) {
		importSourceData(identifier);
		importTargetData(identifier);
	}
	
	public GeneObject importSourceData(String identifier) {
		JsonParser jsonParser = new JsonParser();
		GeneObject gene = null;
		
		try {
			gene = jsonParser.newGeneFromId(Integer.parseInt(identifier));
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
		// We're going to use hash maps as the internal representation of our data
		geneInfoData = gene.getGeneDataAsMap();
		return gene;
	}

	/**
	 * Imports view data from external source and uses cache by default.
	 */
	public LinkedHashMap<String, List<String>> importTargetData(String displayIdentifier) {
		WikitextParser parser = new WikitextParser("GNF_Protein_box", this.configs);
		
		return wikipediaData = parser.getViewDataAsMap(viewController, displayIdentifier, true);
		
	}
	
	/**
	 * Specify whether or not the method should use a cached version,
	 * if available.
	 * @param displayIdentifier
	 * @param useCache
	 * @return
	 */
	public LinkedHashMap<String, List<String>> importViewData(
			String displayIdentifier, boolean useCache) throws NoBotsException {
		WikitextParser parser = new WikitextParser("GNF_Protein_box", this.configs);
		return wikipediaData = parser.getViewDataAsMap(viewController, displayIdentifier, this.configs.get("CACHE"));
	}


	
	@Override
	public String outputDisplay(Update update) {
		return update.toString();
	}

	@Override
	public Update updateValues(
			LinkedHashMap<String, List<String>> modelData,
			LinkedHashMap<String, List<String>> viewData, boolean overwrite) {
		PbbUpdate update = PbbUpdate.PbbUpdateFactory(modelData, viewData, configs);
		return update;
	}
	
	@Override
	public void updateRemoteView(Update update, boolean DRY_RUN) {
		update.updateView(this.viewController, DRY_RUN);
	}

	@Override
	public void verifyInternalData() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Update updateTargetFromSource(Object target, Object source) {
		// TODO Auto-generated method stub
		return null;
	}


}
