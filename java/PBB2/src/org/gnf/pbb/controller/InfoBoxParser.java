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
	 */
	public static void setFieldsFromText(String rawText) throws IOException {
		logger.finest("Starting parse...");
		System.out.println(rawText);
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
			if (startIsValid.group() != TEMPLATE_NAME && STRICT_CHECKING == true) {
				throw new IOException("Supplied template name ("+startIsValid.group()+") does not match specified template name \""+TEMPLATE_NAME+"\".");
			}
		}

		
		// returns the name of the field, i.e. " | Process = " => Process
		Pattern r_fieldName = Pattern.compile("(?<=^\\s|\\s)([\\w]*)(?=\\s\\=\\s)");
		
		// returns the entire string till the newline after the field's "=" sign
		Pattern r_fieldValues = Pattern.compile("(?<=\\=\\s)(.*)$");
		
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
			 
			fieldValueMatcher = r_fieldValues.matcher(rawValueString);
			fieldValueMatcher.find();
			valueBuffer = fieldValueMatcher.group();
			//System.out.println(valueBuffer);
			
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
	
	public void fieldsIntegrityCheck() {
		System.out.println("Fields map size: "+fields.size());
		for (String str : fields.keySet()) {
			System.out.println("Value for key " + str + ": " + fields.get(str));
		}
	}
	
	public static List<String> getField(String key) {
		if (fields.get(key) == null) {
			logger.warning("Value for key " + key + "returned null; this may indicate no such key exists.");
		}
		return fields.get(key);
	}

	public void newBoxFromTitle(WikipediaInterface wikiManager, String displayIdentifier, boolean useCache) {
		String rawText = wikiManager.retrieveArticle(displayIdentifier, useCache);	
		try {
			setFieldsFromText(rawText);
		} catch (IOException e) {
			logger.severe(e.getMessage());
			e.printStackTrace();
		}
	}

}
