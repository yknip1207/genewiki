package org.gnf.pbb.wikipedia;

import info.bliki.wiki.filter.AbstractParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
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
	
	public InfoboxParser(String rawText, boolean strict, boolean canCreate, boolean verbose, String templateName, ExceptionHandler exh) {
		super(rawText);
		botState = exh;
		this.rawText = rawText;
		this.strict = strict;
		this.canCreate = canCreate;
		this.verbose = verbose;
		this.templateName = templateName;
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
		try {
			if (strict) {
				findNoBotsFlag();
				validateTemplateName();
			}
			LinkedHashMap<String, List<String>> fields = parseFieldValues(parseFields(returnContentOfTemplate()));
			if (fields.isEmpty() && !canCreate) {
				throw new ValidationException("Infobox fields map is empty, probably due to a parsing error. Bot cannot continue.");
			} else {
				return fields;
			}
		} catch (MalformedWikitextException e) {
			e.printStackTrace();
			return null;
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
	
	protected String returnContentOfTemplate() throws MalformedWikitextException {
		char ch;
		int position = fCurrentPosition;
		boolean firstOpeningBracketFound = false;
		int start = 0;
		int end = 0;
		try {
			while(true) {
				ch = fSource[position++];
				if (ch == '{' && fSource[position] == '{') {
					if (!firstOpeningBracketFound) {
						firstOpeningBracketFound = true;
						position++;
						start = position;
					} else {
						position++;
					}
				} else if (ch == '}' && fSource[position]== '}') {
					if (!firstOpeningBracketFound) {
						throw new MalformedWikitextException("Closing '}}' before '{{'.");
					} else if (position > end) {
						// Note the position of the }}
						end = position;
					} else {
						// If this was the last }} found, return the appropriate substring
						
					}
				}
			}
		} catch (IndexOutOfBoundsException e) {
			return rawText.substring(start, end);
		}
	}
	
	private LinkedHashMap<String,String> parseFields(String abridgedContent) {
		char[] src  = abridgedContent.toCharArray();
		char ch;
		int position = 0;
		int level = 0;			// bracket/link level
		int fStart = 0;
		int fEnd = 0;
		int fNameEnd = 0;
		boolean insideBrackets = false;
		boolean insideCurlyBrackets= false;
		boolean insideStraightBrackets = false;
		boolean insideField = false;
		boolean fieldNameFound = false;
		String strSource = abridgedContent;
		String fieldName = "";
		String fieldValue = "";
		LinkedHashMap<String, String> fields = new LinkedHashMap<String, String>();
		try {
			while (true) {
				ch = src[position++];
				if ((ch == '{' && src[position] == '{') || (ch == '[' && src[position] == '[')) {
					if (ch == '{') 
						insideCurlyBrackets = true;
					if (ch == '[')
						insideStraightBrackets = true;
					if (insideCurlyBrackets || insideStraightBrackets) 
						insideBrackets = true;
					level++;
					position++;
				} else if ((ch == '}' && src[position] == '}') || (ch == ']' && src[position] == ']')) {
					if (ch == '}')
						insideCurlyBrackets = false;
					if (ch == ']')
						insideStraightBrackets = false;
					level--;
					if (level == 0) 
						insideBrackets = false;
					position++;
				} else if (ch == '|') {
					if (!insideBrackets) {
						if (!insideField) {
							fStart = position++;
							insideField = true;
						} else {
							fEnd = position;
							if (fieldNameFound) {
								// Return the substring after the "=" (which is two steps away from the fNameEnd position?)
								fieldValue = strSource.substring(fNameEnd+1, fEnd);
								fieldValue = fieldValue.substring(0, fieldValue.indexOf("\n")).trim();
								if (verbose) 
									logger.fine(String.format("Field found: %s : %s", fieldName, fieldValue));
								fields.put(fieldName, fieldValue);
							}
							insideField = false;
							position = position-2;
						}	
					}
					position++;
				} else if (ch == '=') {
					if (!insideBrackets && insideField) {
						fNameEnd = position;
						fieldName = strSource.substring(fStart, fNameEnd);
						fieldName = fieldName.substring(0, fieldName.indexOf('=')).trim();
						fieldNameFound = true;

					}
					position++;
				} 
			}
		} catch (IndexOutOfBoundsException e) {
			logger.fine("Parsed "+fields.size()+" fields from given wikitext.");
			return fields;
		}
	}
	
	private LinkedHashMap<String, List<String>> parseFieldValues(LinkedHashMap<String, String> map) {
		LinkedHashMap<String, List<String>> newFields= new LinkedHashMap<String, List<String>>();
		
		Set<String> keys = map.keySet();
		for (String key : keys) {
			String value = map.get(key);
			String valueBuffer = "";
			List<String> valueList = new ArrayList<String>(0);
			if (value.split("\\}\\}").length > 1 ) {
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
