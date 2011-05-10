package org.gnf.pbb.controller;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;

import org.codehaus.jackson.map.JsonMappingException;
import org.gnf.pbb.mygeneinfo.GeneObject;
import org.gnf.pbb.mygeneinfo.JsonParser;

/**
 * The PBBController extends the AbstractBotController, and is responsible for importing the parsed data from mygene.info
 * and the Protein Boxes from Wikipedia. Must be instantiated with configuration options set; see the Config class for
 * information regarding the global configuration object.
 * @author eclarke
 *
 */
public class PBBController extends AbstractBotController {
	
	public PBBController(boolean dryrun, boolean usecache, boolean strictchecking, boolean verbose, boolean debug) {
		super(verbose, usecache, strictchecking, dryrun, debug, "Template:PBB/", "GNF_Protein_box");
	}
	

	/**
	 * Calls the appropriate classes to pull info from mygene.info and parse it; sets the internal linked hash map
	 * sourceData from the result. Does not currently do anything with the gene object; object is only used internally for
	 * validation purposes.
	 */
	public void importSourceData(String identifier) {
		JsonParser jsonParser = new JsonParser();
		GeneObject gene = null;
		
		try {
			gene = jsonParser.newGeneFromId(identifier);
			sourceData = gene.getGeneDataAsMap();
		} catch (JsonParseException e) {
			logger.severe("Fatal error parsing json file.");
			global.stopExecution(e.getMessage());
		} catch (JsonMappingException e) {
			logger.severe("Fatal error mapping json values.");
			global.stopExecution(e.getMessage());
		} catch (NumberFormatException e) {
			logger.severe("Error parsing object identifier. Identifier must be Entrez id, consisting only of numbers.");
			global.stopExecution(e.getMessage());
		} catch (IOException e) {
			logger.severe("Fatal input/output error.");
			global.stopExecution(e.getMessage());
		}
	}

	/**
	 * Sets the internal Update object, updatedData, from a call to the PbbUpdateFactory.
	 */
	public boolean createUpdate () {
		if (global.canExecute()) {
			updatedData = PbbUpdate.PbbUpdateFactory(sourceData, wikipediaData);
			return true;
		} else {
			return false;
		}
	}



}
