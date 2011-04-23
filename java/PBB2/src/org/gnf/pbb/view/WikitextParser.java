/**
 * 
 */
package org.gnf.pbb.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gnf.pbb.exceptions.NoBotsException;
import org.gnf.pbb.exceptions.NoLinksFound;

/**
 * Parses wikipedia text (specifically info-box type formats) and extracts
 * its fields as key:value pairs in a linked hash map. The method also
 * allows for strict checking of the template name being parsed as matching
 * one passed to it during construction.
 * Checks the template name and obeys the {{nobots}} flag when the STRICT
 * config flag is true.
 * 
 * @author eclarke
 *
 */
public class WikitextParser implements ViewParser {
	private final LinkedHashMap<String, Boolean> configs;
	private final String TEMPLATE_NAME;
	public boolean NOBOTS;
	private LinkedHashMap<String, List<String>> viewData = new LinkedHashMap<String, List<String>>();
	private final static Logger logger = Logger.getLogger(WikitextParser.class.getName());
	
	public WikitextParser(String templateName, LinkedHashMap<String, Boolean> configs) {
		this.configs = configs;
		this.TEMPLATE_NAME = templateName;
		this.NOBOTS = false; // until proven otherwise
		logger.fine("Created new WikitextParser for template name: "+this.TEMPLATE_NAME);
	}
	
	
	@Override
	public LinkedHashMap<String, List<String>> getViewDataAsMap(ViewController controller,
			String identifier, boolean useCache) {

		String wikiTitle = "Template:PBB/" + identifier;
		String rawText = controller.retrieveContent(wikiTitle, configs.get("CACHE"));
		
		try {
			parseDataFromText(rawText);
		} catch (NoBotsException e) {
			logger.severe(e.getFlagLocation());
			NOBOTS = true;
			if (configs.get("STRICT")) // fail if strict checking is enabled.
				e.printStackTrace();
		}
		return viewData;
	}

	@Override
	public void parseDataFromText(String rawText) throws NoBotsException {
		
		// First, confirm that the text matches the template our parser is built for
		if (configs.get("STRICT"))
			try {
				confirmTemplateNameMatch(rawText);
			} catch (IOException e) {
				logger.severe(e.getMessage());
				e.printStackTrace();
			}
		
		// Next, test for the presence of the {{nobots}} flag within the text
		detectNoBotsFlag(rawText);
		
		// Finally, begin parsing data from the text
		extractFields(rawText);
		
	}

	private void confirmTemplateNameMatch(String rawText) throws IOException {
		Pattern templateName = Pattern.compile("(?<=^\\{\\{)(.*)");
		Matcher matcher = templateName.matcher(rawText);
		if (matcher.find()) {
			String foundName = matcher.group();
			if (!(foundName.equals(TEMPLATE_NAME)))
				throw new IOException("Detected template name ("+foundName+") does not match specified template name \""+TEMPLATE_NAME+"\".");
		}
		
	}

	/**
	 * Throws an exception if it finds a {{nobots}} flag.
	 * @param rawText
	 * @throws NoBotsException
	 */
	private void detectNoBotsFlag(String rawText) throws NoBotsException {
		Pattern nobots = Pattern.compile("\\{\\{nobots\\}\\}");
		Matcher nobotsFinder = nobots.matcher(rawText);
		if (nobotsFinder.find()) {
			throw new NoBotsException(nobotsFinder.start(), nobotsFinder.end());
		}
		
	}

	/**
	 * Iteratively passes over each field in a template or info box,
	 * extracts the field name, then passes the line to a method that
	 * extracts the field value(s).
	 * @param rawText
	 */
	private void extractFields(String rawText) {
		Pattern fieldStart = Pattern.compile("(?<=^\\s|\\s)([\\w]*)(?=\\s\\=)");
		Matcher fieldStartFinder = fieldStart.matcher(rawText);
		
		String nl = System.getProperty("line.separator");
		while (fieldStartFinder.find()) {
			String substring = rawText.substring(fieldStartFinder.end(), rawText.indexOf(nl, fieldStartFinder.end()));
			String fieldName = fieldStartFinder.group();
			logger.finer("Field found: "+fieldName);
			List<String> fieldValue = extractFieldValue(substring);
			
			logger.finer("Added field to data map: "+fieldName+" = "+fieldValue);
			viewData.put(fieldName, fieldValue);
		}
		
	}

	/**
	 * Extracts the value of a field from a substring immediately following the " =" part 
	 * of the field name. Also tests for the presence of template links and handles
	 * those links appropriately.
	 * @param substring
	 * @return list of values (generally only one entry in list)
	 */
	private List<String> extractFieldValue(String substring) {		
		Pattern fieldValues = Pattern.compile("(?<=\\=)(.*)$");
		Matcher fieldValuesFinder = fieldValues.matcher(substring);
		
		fieldValuesFinder.find();
		String fieldValue = fieldValuesFinder.group();
		// Clean up our results
		fieldValue = fieldValue.trim();
		if (fieldValue.startsWith(";"))
			fieldValue = fieldValue.substring(2);
		// Test for the presence of non-expanded template links / {{example}}
		List<String> fieldValueList = new ArrayList<String>();
		try {
			fieldValueList = getNonExpandedTemplateLinks(fieldValue);
		} catch (NoLinksFound n) {
			fieldValueList.add(fieldValue);
		}
		return fieldValueList;
	}

	/**
	 * Iteratively adds any non-expanded template links of the form {{tlx}} to a list
	 * and returns it. If none are found, a non-fatal exception is thrown to indicate
	 * no links.
	 * @param fieldValue
	 * @return
	 * @throws NoLinksFound
	 */
	private List<String> getNonExpandedTemplateLinks(String fieldValue) throws NoLinksFound {
		Pattern templateLinks = Pattern.compile("(?<=\\{\\{)(.[^\\{\\}]*)(?=\\}\\})");
		Matcher templateLinksFinder = templateLinks.matcher(fieldValue);
		boolean success = false;
		List<String> linksBuffer = new ArrayList<String>();
		while (templateLinksFinder.find()) {
			success = true;
			linksBuffer.add(templateLinksFinder.group());
		}
		if (!success) throw new NoLinksFound();
		return linksBuffer;
	}


	@Override
	public void forceDataIntegrityCheck() {
		// TODO Auto-generated method stub
		
	}

}
