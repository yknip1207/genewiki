/**
 * 
 */
package org.scripps.nlp.umls;

/**
 * @author bgood
 *
 */
public class SemanticType {
	String stype_abbr;
	String stype_name;
	String sgroup_name;
	public String getStype_abbr() {
		return stype_abbr;
	}
	public void setStype_abbr(String stype_abbr) {
		this.stype_abbr = stype_abbr;
	}
	public String getStype_name() {
		return stype_name;
	}
	public void setStype_name(String stype_name) {
		this.stype_name = stype_name;
	}
	public String getSgroup_name() {
		return sgroup_name;
	}
	public void setSgroup_name(String sgroup_name) {
		this.sgroup_name = sgroup_name;
	}
	public SemanticType(String stype_abbr, String stype_name, String sgroup_name) {
		super();
		this.stype_abbr = stype_abbr;
		this.stype_name = stype_name;
		this.sgroup_name = sgroup_name;
	}
	
	
}
