package org.gnf.pbb.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang.WordUtils;
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
	public WpController wiki;
	
	public PBBController() {
		wiki = new WpController();
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
		infoBoxParser.newDisplayDataMap(wiki, displayIdentifier, true);
		//infoBoxParser.fieldsIntegrityCheck();
		return infoBoxParser.getFields();
	}


	@Override
	public String outputDisplay(LinkedHashMap<String, List<String>> display) {
		String nl = System.getProperty("line.separator");
		StringBuffer out = new StringBuffer();
		out.append("{{GNF_Protein_box"+nl);
		Set<String> keys = display.keySet();
		for (String key: keys) {
			if (display.get(key).size() > 1 && (
					key.equalsIgnoreCase("Function") || 
					key.equalsIgnoreCase("Process") || 
					key.equalsIgnoreCase("Component") ||
					key.equalsIgnoreCase("pdb"))) {
				out.append(" | "+key+" = ");
				Iterator<String> templateLinks = display.get(key).iterator();
				while (templateLinks.hasNext()) {
					out.append("{{");
					if (key.equalsIgnoreCase("pdb")) // pdb ids require a standard PDB2| prefix in their template tags
						out.append("PDB2|");
					out.append(templateLinks.next()+"}} ");
				}
				out.append(nl);
			} else if (display.get(key).size() > 0) {
				String value = display.get(key).get(0);
				value = WordUtils.capitalize(value);
				out.append(" | "+key+" = "+value+nl);
			}
		}
		out.append("}}");
		return out.toString();
	}

	@Override
	public LinkedHashMap<String, List<String>> updateValues(
			LinkedHashMap<String, List<String>> objectData,
			LinkedHashMap<String, List<String>> displayData, boolean overwrite) {
		
		Set<String> objectKeys = objectData.keySet();
		LinkedHashMap<String, List<String>> newDisplayData = displayData;
		int matches = 0;
		for (String key : objectKeys) {
			List<String> objectValues = objectData.get(key), displayValues = displayData.get(key);
			// I've wrapped this in two if blocks because evaluating the second if the first is null
			// returns a NullPointerException
			if (objectValues != null && displayValues != null) {
				if (displayValues.containsAll(objectValues)) {
					//System.out.println("Values for "+key+" are equal.");
					matches++;
				} else if (overwrite) {
					newDisplayData.put(key, objectValues);
				} else {
					//System.out.printf("Values for %s differ: %s in object; \n %s in display.\n", key, objectValues, displayValues);
				}
			}
		}
		logger.fine("Number of keys with already up-to-date values: " + matches +"/"+objectKeys.size());
		return newDisplayData;
	}


}
