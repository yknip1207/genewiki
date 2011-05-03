package org.gnf.pbb.controller;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.gnf.pbb.Update;
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
	
	public PBBController(boolean dryrun, boolean usecache, boolean strictchecking, boolean verbose) {
		super(verbose, usecache, strictchecking, dryrun, "Template:PBB/", "GNF_Protein_box");
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
		sourceData = gene.getGeneDataAsMap();
	}

	/**
	 * Sets the internal Update object, updatedData, from a call to the PbbUpdateFactory.
	 */
	public void createUpdate () {
		updatedData = PbbUpdate.PbbUpdateFactory(sourceData, wikipediaData);
	}



}
