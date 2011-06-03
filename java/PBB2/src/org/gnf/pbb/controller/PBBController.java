package org.gnf.pbb.controller;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import org.codehaus.jackson.JsonParseException;

import org.codehaus.jackson.map.JsonMappingException;
import org.gnf.pbb.exceptions.Severity;
import org.gnf.pbb.mygeneinfo.GeneObject;
import org.gnf.pbb.mygeneinfo.JsonParser;
import org.gnf.pbb.wikipedia.ProteinBox;

/**
 * The PBBController extends the AbstractBotController, and is responsible for importing the parsed data from mygene.info
 * and the Protein Boxes from Wikipedia. Must be instantiated with configuration options set; see the Config class for
 * information regarding the global configuration object.
 * @author eclarke
 *
 */
public class PBBController extends AbstractBotController {
	
	public PBBController(List<String> ids) {
		super(ids);
	}
	
	/**
	 * Calls the appropriate classes to pull info from mygene.info and parse it; sets the internal linked hash map
	 * sourceData from the result. Does not currently do anything with the gene object; object is only used internally for
	 * validation purposes.
	 * @return 
	 */
	public ProteinBox importSourceData(String identifier) {
		ProteinBox gene = null;
		
		try {
			gene = JsonParser.newGeneFromId(identifier);
			return gene;
		} catch (JsonParseException e) {
			logger.severe("Error parsing json file.");
			botState.recoverable(e);
			return null;
		} catch (JsonMappingException e) {
			logger.severe("Error mapping json values.");
			botState.recoverable(e);
			return null;
		} catch (NumberFormatException e) {
			logger.severe("Error parsing object identifier. Identifier must be Entrez id, consisting only of numbers.");
			botState.recoverable(e);
			return null;
		} catch (IOException e) {
			botState.recoverable(e);
			return null;
		}
	}

	public String prepareReport() {
		StringBuilder sb = new StringBuilder();
		sb.append(				"| Completion report: \n");
		sb.append(				"|------------------------------- \n");
		sb.append(String.format("|  Completed updates: %d/%d \n", this.completed.size(), this.identifiers.size()));
		sb.append(String.format("|  Failed updates:    %d/%d \n", this.failed.size(), this.identifiers.size()));
		sb.append(				"|  \n");
		sb.append(				"|  Protein boxes updated: \n");
		for (String str : completed) {
			sb.append(			"|   "+str+"\n");
		}
		sb.append(				"|  Failed to update: \n");
		for (String str : failed) {
			sb.append(			"|   "+str+"\n");
		}
		String report = sb.toString();
		System.out.println(report);
		wpControl.writeReport(report);
		return report;
	}

}
