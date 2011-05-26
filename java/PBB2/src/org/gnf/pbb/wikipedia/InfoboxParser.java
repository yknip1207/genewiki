package org.gnf.pbb.wikipedia;

import info.bliki.wiki.filter.AbstractParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gnf.pbb.Configs;
import org.gnf.pbb.Custom;
import org.gnf.pbb.exceptions.ConfigException;
import org.gnf.pbb.exceptions.ExceptionHandler;
import org.gnf.pbb.exceptions.MalformedWikitextException;
import org.gnf.pbb.exceptions.NoBotsException;
import org.gnf.pbb.exceptions.PbbExceptionHandler;
import org.gnf.pbb.exceptions.ValidationException;

/**
 * Processes info boxes and templates, scanning for fields of type |fieldName = fieldValue.
 * Does not recurse into any other bracketed links (as these often have their own fieldName/fieldValue pairs)
 * and returns a linked hash map of field names keyed to field values.
 * @author eclarke
 *
 */
public class InfoboxParser extends AbstractParser {
	private static ExceptionHandler botState;
	private final static Logger logger = Logger.getLogger(InfoboxParser.class.getName());
	private String rawText = "";
	private boolean strict;
	private boolean canCreate;
	private boolean verbose;
	private String templateName;
	private String textBeforeTemplate;
	private String textAfterTemplate;
	
	public InfoboxParser(String rawText, boolean strict, boolean canCreate, boolean verbose, String templateName, ExceptionHandler exh) {
		super(rawText);
		botState = exh;
		this.rawText = rawText;
		this.strict = strict;
		this.canCreate = canCreate;
		this.verbose = verbose;
		this.templateName = templateName;
		textBeforeTemplate = "";
		textAfterTemplate = "";
		if (this.verbose) {
			logger.setLevel(Level.FINE);
		}
	}
	
	public static InfoboxParser factory(String rawText) {
		try {
			return new InfoboxParser(rawText, Configs.GET.flag("strict"), 
					Configs.GET.flag("canCreate"), 
					Configs.GET.flag("verbose"),
					Configs.GET.str("templateName"),
					PbbExceptionHandler.INSTANCE);		
		} catch (ConfigException e) {
			PbbExceptionHandler.INSTANCE.fatal(e);
			return null;
		}

	}
	
	/**
	 * Parses the raw text passed to the instance on initialization
	 * @return linked hash map of field names and corresponding values
	 * @throws NoBotsException 
	 * @throws ValidationException 
	 */
	public LinkedHashMap<String,List<String>> parse() throws NoBotsException, ValidationException {
		if (strict) {
			findNoBotsFlag();
			validateTemplateName();
		}
		LinkedHashMap<String, List<String>> fields = postprocessFields(parseFields(extractTemplate(rawText)));
		textBeforeTemplate = preTemplateContent(rawText);
		textAfterTemplate = postTemplateContent(rawText);
		if (textBeforeTemplate != null) {
			String[] arraybuffer = {textBeforeTemplate};
			fields.put("TextBeforeTemplate", Arrays.asList(arraybuffer));
		}
		if (textAfterTemplate != null) {
			String[] arraybuffer = {textAfterTemplate};
			fields.put("TextAfterTemplate", Arrays.asList(arraybuffer));
		}
		if (fields.isEmpty() && !canCreate) {	
			throw new ValidationException("Infobox fields map is empty, probably due to a parsing error. Bot cannot continue.");
		} else {	// if canCreate flag is set, it's fine that the fields are empty- the bot will populate them.
			return fields;
		}
	}

	private boolean validateTemplateName() throws ValidationException {
		Pattern templateNamePattern = Pattern.compile("(?<=^\\{\\{)(.*)");
		Matcher matcher = templateNamePattern.matcher(rawText);
		if (matcher.find()) {
			String foundName = matcher.group();
			if (!(foundName.contains(templateName)))
				throw new ValidationException("Detected template name ("+foundName+") does not match specified template name \""+templateName+"\".");
		}
		return false;
	}

	@Override
	public void setNoToC(boolean noToC) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void runParser() {
		// TODO Auto-generated method stub
		
	}
	
	private void findNoBotsFlag() throws NoBotsException {
		if (readUntil("{{nobots}}")) 
			throw new NoBotsException();
	}
	
	/**
	 * This method finds the template specified by the global TemplateName variable
	 * and returns its content, omitting any other text in the source. 
	 * @return extracted template content
	 * @throws MalformedWikitextException
	 */
	public String extractTemplate(String source) {
		int startIndex = source.indexOf("{{"+this.templateName);
		int level = 0;
		int firstOpen = -1;
		int lastClose = -1;
		char[] src = source.toCharArray();
		for (int i = startIndex; i < src.length; i++) {
			char ch = src[i];
			char prev = ' ';
			if (i > 0) {
				prev = src[i-1];
			}
			
			if (ch == '{' && prev == '{') {
				level++;
				if (firstOpen == -1) firstOpen = i-1;
			} else if (ch == '}' && prev == '}'){
				level--;
				if (lastClose < i) lastClose = i+1;
			}
			
			if (level == 0 && firstOpen != -1)
				break;
		}
		String result = source.substring(firstOpen, lastClose);
		return result;
	}
	
	/**
	 * Returns any content before the template
	 * @param source
	 * @return
	 */
	public String preTemplateContent(String source) {
		String template = extractTemplate(source);
		if (source.equals(template)) {
			return null;
		} else {
			String result = source.substring(0, source.indexOf(template));
			if (result.equals(""))
				return null;
			return result;
		}
	}
	
	/**
	 * Returns any content after the template
	 * @param source
	 * @return
	 */
	public String postTemplateContent(String source) {
		String template = extractTemplate(source);
		String result = null;
		if (source.equals(template)) {
			return result;
		} else {
			String pre = preTemplateContent(source);
			if (pre != null) {
				result = source.substring(0, source.indexOf(pre));	// remove the pre-template content
				result = result.replace(template, "");
			} else {
				result = source.replace(template, "");
			}
			return result;
		}
	}
	
	/**
	 * Parses fields (of form |name = value) from a the content of a template passed to it.
	 * It avoids parsing below the top-level, leaving things like reference tags, fields 
	 * inside fields, etc unparsed.
	 * @param content
	 * @return a representation of the fields as a map of name:value pairs
	 */
	public LinkedHashMap<String,String> parseFields(String content) {
		// Preprocess the content to ensure it doesn't begin with any brackets
		if (content.indexOf("{{") == 0) {
			content = content.substring(2);
		}
		if (content.lastIndexOf("}}") == content.length()-1) {
			content = content.substring(0, content.length()-3);
		}
		char[] src = content.toCharArray();
		char ch;		// The current character
		char prev;		// The previous character
		
		int nameStart = 0;		// Start of the field name
		int nameEnd = 0;		// End of the field name
		int valueStart = 0;		// Start of field value
		int valueEnd = 0;		// End of field value
		int level = 0;			// Depth of non-parsing brackets (increases with nesting)
		
		boolean inBrackets = false;   	// Are we inside any kind of non-parsing brackets ({{, [[, <, etc)
		boolean inTag = false;			// Are we inside two <..> <..> tags?
		boolean inField = false;		// Are we parsing a field?
		boolean nameParsed = false;		// At the end of the loop, did we successfully parse a name?
		boolean valueParsed = false;	// At the end of the loop, did we successfully parse a value?
		
		LinkedHashMap<String,String> results = new LinkedHashMap<String,String>();
		
		// We are not going to parse anything within the angle brackets (usually contain ref tags that have
		// formatting similar to our infobox)
		int firstAngleBracket = content.indexOf("<");
		int lastAngleBracket = content.lastIndexOf(">");
		
		try {
			for (int i = 0; i < src.length; i++) {
				ch = src[i];
				// Can't assign previous if we're at the start of the array
				if (i > 0) { prev = src[i-1]; 
				} else { prev = ch; }
				// If we're within previously determined angle brackets, 
				// we won't parse anything
				if (firstAngleBracket <= i && i <= lastAngleBracket) {
					inTag = true;
					inBrackets = true;
				} else if (inTag) {
					inTag = false;
					if (level == 0)		// i.e. if we're out of both the <> tags and any other brackets
						inBrackets = false;
				}
				
				if ((ch == '{' && prev == '{') || (ch == '[' && prev == '[')) {
					level++;
					inBrackets = true;
				} else if ((ch == '}' && prev == '}') || (ch == ']' && prev == ']')) {
					level--;
					if (!inTag && level == 0) {
						inBrackets = false;
					}
				} else if (ch == '|' && !inBrackets) {
					if (!inField) {			
						inField = true;		
						nameStart = i+1;	// The field name follows immediately
					} else {				
						inField = false;	
						valueEnd = i;		// A new | indicates the end of the field and the start of a new one
						valueParsed = true;
						i--;				// So we step backwards to allow a chance to parse the new field
					}
				} else if (ch == '=' && !inBrackets && inField) {
					nameEnd = i;
					valueStart = i+1;
					nameParsed = true;
				}
				if (i == src.length-1) {
					valueEnd = i+1;			// When it's the end of the string, we can't rely on the last | to
											// indicate that we're done with this field, so have to explicitly
											// specify it
					valueParsed = true;
				}
				
				if (nameParsed && valueParsed) {
					String name = content.substring(nameStart, nameEnd).trim();
					String value = content.substring(valueStart, valueEnd).trim();
					results.put(name, value);
					logger.fine(String.format("Added field: %s : %s", name, value));
					nameParsed = false; 	// reset these values
					valueParsed = false;
				}
				
			}
		} catch (Exception e) {
			botState.recoverable(e);
		}
		return results;
	}
	
	/**
	 * Converts the field map from parseFields into a map of string:list objects
	 * by converting any field value with certain delimiters into a list.
	 * @param map of fields
	 * @return field map in the form string:list
	 */
	private LinkedHashMap<String, List<String>> postprocessFields(LinkedHashMap<String, String> map) {
		LinkedHashMap<String, List<String>> newFields= new LinkedHashMap<String, List<String>>();
		
		Set<String> keys = map.keySet();
		for (String key : keys) {
			String value = map.get(key);
			String valueBuffer = "";
			List<String> valueList = new ArrayList<String>(0);
			// We only make a list if they're collections of {{..}} and if they don't have < (this usually
			// indicates that they have some sort of HTML tag, and who know's what's inside those...)
			if ((value.split("\\}\\}").length > 1 && !value.contains("<")) || (key.equals("PDB") || 
					key.equals("Function") || key.equals("Process") || key.equals("Component"))) {
				valueBuffer = value.replaceAll("\\}\\}", ", ");
				valueBuffer = valueBuffer.replaceAll("\\{\\{", "");
				valueList = Arrays.asList(valueBuffer.split(", "));
			} else {
				valueList.add(value);
			}
			// Some custom find/replace calls are made here; this code can be altered to suit
			valueList = Custom.valueParse(valueList);
			newFields.put(key, valueList);
		}
		
		return newFields;
	}
	
	
}
