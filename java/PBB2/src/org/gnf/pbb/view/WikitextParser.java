/**
 * 
 */
package org.gnf.pbb.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gnf.pbb.controller.ExternalSystemInterface;
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
public class WikitextParser {
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
	
	
	public LinkedHashMap<String, List<String>> getViewDataAsMap(ExternalSystemInterface controller,
			String identifier, boolean useCache) {

		String wikiTitle = "Template:PBB/" + identifier;
		String rawText = controller.getContent(wikiTitle, configs.get("CACHE"));
		InfoboxParser.parse(rawText);
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
	 * Obfuscates the text, non-permanently, in template tags
	 * as they usually contain fields of their own that interfere
	 * with the parser. Should call expandTemplateLinkContent before
	 * returning the final parsed results.
	 * @param rawText
	 * @return
	 */
	private String collapseLinkContent(String rawText, Map<String,String> collapsedTemplateLinks) {
		Pattern template = Pattern.compile("(?<=\\{\\{)(.[^\\}\\{]*)(?=\\}\\})");
		Pattern interwiki = Pattern.compile("(?<=\\[\\[)(.[^\\[\\]]*)(?=\\]\\])");
		Matcher templateFinder = template.matcher(rawText);
		Matcher interwikiFinder = interwiki.matcher(rawText);
		int collapsedLinkCount = 0;
		StringBuilder sb = new StringBuilder();
		sb.append(rawText);
		obfuscate(templateFinder, sb, collapsedLinkCount, collapsedTemplateLinks);
		obfuscate(interwikiFinder, sb, collapsedLinkCount, collapsedTemplateLinks);
		logger.info(collapsedLinkCount + " links collapsed.");
		Set<String> keyset = collapsedTemplateLinks.keySet();
		for (String key : keyset) {
			System.out.println(key +" : "+collapsedTemplateLinks.get(key));
		}
		return sb.toString();
	}
	
	private void obfuscate(Matcher finder, StringBuilder sb, int collapsedLinkCount, Map<String, String> map) {
		while (finder.find()) {
			collapsedLinkCount++;
			String templateContent = finder.group();
			StringBuilder collapsedNameBuffer = new StringBuilder();
			collapsedNameBuffer.append("TL"+collapsedLinkCount);
			for (int i = 0; i < templateContent.length(); i++) {
				collapsedNameBuffer.append("`");
			}
			if (collapsedNameBuffer.length() > 2) {
				while (collapsedNameBuffer.length() > templateContent.length()) {
					collapsedNameBuffer.deleteCharAt(collapsedNameBuffer.length()-1);
				}
			}
			String collapsedName = collapsedNameBuffer.toString();
			map.put(collapsedName, templateContent);
			sb.replace(finder.start(), finder.end(), collapsedName);
		}
	}

	/**
	 * Iteratively passes over each field in a template or info box,
	 * extracts the field name, then passes the line to a method that
	 * extracts the field value(s).
	 * @param rawText
	 */
	private void extractFields(String rawText) {
		Map<String, String> collapsedLinkContent = new LinkedHashMap<String, String>();
		rawText = collapseLinkContent(rawText, collapsedLinkContent);
		Set<String> keyset = collapsedLinkContent.keySet();
		for (String key : keyset) {
			System.out.println(key +" : "+collapsedLinkContent.get(key));
		}
		Pattern fieldStart = Pattern.compile("(?<=^\\s|\\s)([\\w]*)(?=\\s\\=)");
		
		// FIXME: This regex will grab everything between the first ref open tag
		// and the last ref open tag- fine if there's only one ref block but
		// problematic if there's two.
		Pattern refBrackets = Pattern.compile("(?<=<ref)(.*)(?=/ref>)");
		
		Matcher fieldStartFinder = fieldStart.matcher(rawText);
		List<String> fieldValue = new ArrayList<String>();
		String nl = System.getProperty("line.separator");
		
		// NOTE: Reference tags are problematic because they generally
		// enclose citation information, which is not part of the template
		// info we're parsing but looks just like it. This hack basically
		// checks to see if a found field is inside those tags, and if it is,
		// the parser ignores it. 
		int start = rawText.length(), end = rawText.length();
		Matcher refFinder = refBrackets.matcher(rawText);
		try {
			refFinder.find();
			start = refFinder.start();
			end = refFinder.end();
		} catch (Exception e) {
			// If the parser didn't find anything, that's fine. Ignore the 
			// MatchNotFound exception.
		}
		while (fieldStartFinder.find()) {
			String substring = rawText.substring(fieldStartFinder.end(), rawText.indexOf(nl, fieldStartFinder.end()));
			String fieldName = fieldStartFinder.group();
			boolean invalidFieldValue = false;
			if (start < fieldStartFinder.start() && fieldStartFinder.end() < end) {
				logger.info("Field "+fieldName+" invalid; inside <ref></ref> tags.");
				invalidFieldValue = true;
			} else if (refBrackets.matcher(substring).find() && !invalidFieldValue) {
				fieldValue = extractFieldValue(substring, false);
			} else if (!invalidFieldValue) {
				fieldValue = extractFieldValue(substring, true);
			}
			if (!invalidFieldValue) {
				logger.finer("Field found: "+fieldName);	
				logger.finer("Added field to data map: "+fieldName+" = "+fieldValue);
				viewData.put(fieldName, fieldValue);
			}
				
		}		
	}

	/**
	 * Extracts the value of a field from a substring immediately following the " =" part 
	 * of the field name. Also tests for the presence of template links and handles
	 * those links appropriately.
	 * @param substring
	 * @return list of values (generally only one entry in list)
	 */
	private List<String> extractFieldValue(String substring, boolean findTemplateLinks) {		
		Pattern fieldValues = Pattern.compile("(?<=\\=)(.*)$");
		Matcher fieldValuesFinder = fieldValues.matcher(substring);
		
		fieldValuesFinder.find();
		String fieldValue = fieldValuesFinder.group();
		// TODO: This is specific for the Gene Wiki project and should be handled elsewhere
		fieldValue = fieldValue.trim();
		if (fieldValue.startsWith(";") && fieldValue.length() > 2)
			fieldValue = fieldValue.substring(2);
		
		// Test for the presence of non-expanded template links / {{example}}
		List<String> fieldValueList = new ArrayList<String>(), fieldValueList2 = new ArrayList<String>();
		
		if (findTemplateLinks) {
			try {
				fieldValueList2 = getListOfLinks(fieldValue, "template");
				System.out.println(fieldValueList2);
			} catch (NoLinksFound n) {
				fieldValueList.add(fieldValue);
			}
		} else {
			fieldValueList.add(fieldValue);
		}
		return fieldValueList;
	}

	/**
	 * Iteratively adds any links of a specified type to a list and returns it.
	 * If none are found, a non-fatal exception is thrown to indicate that there were no links.
	 * @param fieldValue
	 * @param type
	 * 			'interwiki' [[...]]; 'template' {{...}}; 'parameter' {{{...}}}
	 * @return list of links
	 * @throws NoLinksFound
	 */
	private List<String> getListOfLinks(String fieldValue, String type) throws NoLinksFound {
		// This "UNINITIALIZED" pattern is never used; if it's not replaced by a real regex the method
		// throws a warning 
		// and exits.
		Pattern links = Pattern.compile("UNINITIALIZED");
		
		if (type.equalsIgnoreCase("template")) {
			 links = Pattern.compile("(?<=\\{\\{)(.[^\\{\\}]*)(?=\\}\\})");
		} else if (type.equalsIgnoreCase("interwiki")) {
			 links = Pattern.compile("(?<=\\[\\[)(.[^\\[\\]*)(?=\\]\\])");
		} else if (type.equalsIgnoreCase("parameter")) {
			 links = Pattern.compile("(?<=\\{\\{\\{)(.[^\\{\\}]*)(?=\\}\\}\\})");
		} else {
			logger.warning("Unrecognized link type \""+type+"\". Valid link types are template ({{..}}), interwiki ([[..]]), and parameter ({{{..}}}).");
			throw new NoLinksFound();
		}
		Matcher linksFinder = links.matcher(fieldValue);
		boolean success = false;
		List<String> linksBuffer = new ArrayList<String>();
		while (linksFinder.find()) {
			success = true;
			linksBuffer.add(linksFinder.group());
		}
		if (!success) throw new NoLinksFound();
		return linksBuffer;
	}


	public void forceDataIntegrityCheck() {
		// TODO Auto-generated method stub
		
	}

}
