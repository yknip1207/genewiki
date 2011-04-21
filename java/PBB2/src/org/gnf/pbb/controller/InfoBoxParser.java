/**
 * Part of a Model - View - Controller system.
 * 
 * Parses Wikipedia info boxes and creates a LinkedHashMap of the box fields; 
 * can also write a LinkedHashMap back as a info box in proper Wikitext.
 * 
 * Made to be used with a proper data representation of source data and 
 * a Controller class that can compare the Object values to the Box's values.
 */
package org.gnf.pbb.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gnf.pbb.exceptions.NoBotsException;

/**
 * @author eclarke
 * 
 * If you enable strict checking and specify a name for the template (recommended),
 * the parser will reject any templates it may pull up with altered or non-matching names.
 * 
 */
public class InfoBoxParser {
	private final static String TEMPLATE_NAME = "GNF_Protein_box";
	private static boolean STRICT_CHECKING = true;
	
	private final static Logger logger = Logger.getLogger(InfoBoxParser.class.getName());
	
	private static LinkedHashMap<String, List<String>> fields = new LinkedHashMap<String, List<String>>();

	/**
	 * Takes raw Wikitext and parses it under a few assumptions:
	 * 1) Boxes start with "{{TEMPLATE_NAME" and end with a newline, then "}}" 
	 * 2) Fields are in the form " | FIELD_NAME = STRING_ARRAY "
	 * 	  where the array elements are separated by either "}}" or "," 
	 * 
	 * @param rawText
	 * 				 the raw source text passed as a string object
	 * @throws IOException 
	 * @throws NoBotsException 
	 */
	public static void setFieldsFromText(String rawText) throws IOException, NoBotsException {
		logger.fine("Starting parse...");
		//System.out.println(rawText);
		String nl = System.getProperty("line.separator");
		String rawValueString;
		String valueBuffer;
		String fieldName = "";
		boolean templateFinderSuccess;


		//// Some regular expressions
		/* As an example (similar methods used in the other regexes):
		 * wikiTemplateNonExpanded uses a regex to find the text
		 * inside a template demarcation: {{example}} => example
		 * Regex: (lookahead: {{)(a-z, A-Z, whitespace)(lookbehind: }})
		 */
		Pattern r_wikiTemplateNonExpanded = Pattern.compile("(?<=\\{\\{)(.[^\\{\\}]*)(?=\\}\\})");
		
		// returns the template name
		Pattern r_startOfTemplate = Pattern.compile("(?<=^\\{\\{)(.*)");
		Matcher startIsValid = r_startOfTemplate.matcher(rawText);
		if (startIsValid.find()) {
			String foundTemplateName = startIsValid.group();
			if (!(foundTemplateName.equals(TEMPLATE_NAME)) && STRICT_CHECKING == true) {
				throw new IOException("Supplied template name ("+foundTemplateName+") does not match specified template name \""+TEMPLATE_NAME+"\".");
			}
		}

		
		// returns the name of the field, i.e. " | Process = " => Process
		//Pattern r_fieldName = Pattern.compile("(?<=^\\s|\\s)([\\w]*)(?=\\s\\=\\s)");
		Pattern r_fieldName = Pattern.compile("(?<=^\\s|\\s)([\\w]*)(?=\\s\\=)");
		// returns the entire string till the newline after the field's "=" sign
		//Pattern r_fieldValues = Pattern.compile("(?<=\\=\\s)(.*)$");
		Pattern r_fieldValues = Pattern.compile("(?<=\\=)(.*)$");
		
		Pattern r_nobots = Pattern.compile("\\{\\{nobots\\}\\}");
		Matcher nobotsFinder = r_nobots.matcher(rawText);
		if (nobotsFinder.find()) {
			throw new NoBotsException(nobotsFinder.start(), nobotsFinder.end());
		}
		
		Matcher fieldMatcher = r_fieldName.matcher(rawText);
		Matcher fieldValueMatcher;
		Matcher templateFinder;
		while (fieldMatcher.find()) {
			List<String> fieldValues = new ArrayList<String>();
			List<String> templateValues = new ArrayList<String>();
			
			//System.out.println(fieldMatcher.group());
			fieldName = fieldMatcher.group();
			
			// pulls the rest of the line from the end of the fieldMatcher result
			// System.out.printf("start: %d | end: %d \n", fieldMatcher.end(), rawText.indexOf(nl, fieldMatcher.end()));
			rawValueString = rawText.substring(fieldMatcher.end(), rawText.indexOf(nl, fieldMatcher.end()));
			
			// sometimes fields start with a semicolon. they shouldn't and they interfere with parsing, so let's strip it out
			if (rawValueString.startsWith(";")) {
				String test = rawValueString.substring(2);
				System.out.println(test);
			}
			
			fieldValueMatcher = r_fieldValues.matcher(rawValueString.trim());
			fieldValueMatcher.find();
			valueBuffer = fieldValueMatcher.group();
			valueBuffer = valueBuffer.trim();
			// One particular case: a field starts with a ; for formatting reasons. Better ways to do this, so we 
			// are just going to strip it out and parse the rest of the content.
			if (valueBuffer.startsWith(";")) { 
				// System.out.println(":( " + valueBuffer);
				valueBuffer = valueBuffer.substring(2);
				// System.out.println(":) " + valueBuffer);
			}
			
			
			// Template search: If the values are of the form {{..}}, {{..}}, ..., we can pull each
			// element out into a list.
			templateFinder = r_wikiTemplateNonExpanded.matcher(valueBuffer);
			templateFinderSuccess = false;
			while (templateFinder.find()) {
				templateFinderSuccess = true;
				templateValues.add(templateFinder.group());
				//System.out.println(templateFinder.group());
			}
			
			// if we got something back from the template search, we'll use that for our field values
			// and if not, we use what we got from the original fieldValueMatcher regex.
			if (templateFinderSuccess) {
				fieldValues = templateValues;
			} else {
				fieldValues.add(valueBuffer);
			}
			
			// Got everything we could out of this round, let's set this key-value pair
			setField(fieldName, fieldValues);
						
		}
		
	}
	
	public static void setField(String key, List<String> fieldValues) {
		logger.fine("Setting key: "+key+" and values: "+fieldValues);
		fields.put(key, fieldValues);
 	}

	public LinkedHashMap<String, List<String>> getFields() {
		return fields;
	}
	
	/**
	 * Call this to see an output of all the fields and their values in the fields map.
	 */
	public void fieldsIntegrityCheck() {
		System.out.println("Fields map size: "+fields.size());
		for (String str : fields.keySet()) {
			System.out.println("Value for key " + str + ": " + fields.get(str));
		}
	}
	/**
	 * @deprecated
	 * @param key
	 * @return
	 */
	@Deprecated
	public static List<String> getField(String key) {
		if (fields.get(key) == null) {
			logger.warning("Value for key " + key + "returned null; this may indicate no such key exists.");
		}
		return fields.get(key);
	}

	/**
	 * Uses an existing Wikipedia interface to pull an article from the cache, if it exists, or from Wikipedia.
	 * To force a refresh, set the useCache flag to false.
	 * @param wikiInterface
	 * @param displayIdentifier
	 * @param useCache
	 * @throws NoBotsException 
	 */
	public void newDisplayDataMap(WpController wikiInterface, String displayIdentifier, boolean useCache) throws NoBotsException {
		String wikiTitle = "Template:PBB/" + displayIdentifier;
		String rawText = wikiInterface.retrieveContent(wikiTitle, useCache);	
		try {
			setFieldsFromText(rawText);
		} catch (IOException e) {
			logger.severe(e.getMessage());
			e.printStackTrace();
		}
	}

}
