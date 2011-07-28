/**
 * 
 */
package org.json.google;

/**
 * {"type":"string","label":"Fruit","pattern":null,"id":"Fruit"}
 * @author bgood
 *
 */
public class Col {
	String type;
	String label;
	String pattern;
	String id;
	

	
	public String getAsJSON(){
		String c = "{\"type\":\""+type+"\",\"label\":\""+label+"\",\"id\":\""+id+"\"}";
		return c;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	
}
