package org.scripps.nlp.ncbo.web;

public class Term {

	String name;
	String localConceptId;
	int isPreferred;
	
	
	
	public Term(String name, String localConceptId, int isPreferred) {
		super();
		this.name = name;
		this.localConceptId = localConceptId;
		this.isPreferred = isPreferred;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLocalConceptId() {
		return localConceptId;
	}
	public void setLocalConceptId(String localConceptId) {
		this.localConceptId = localConceptId;
	}
	public int getIsPreferred() {
		return isPreferred;
	}
	public void setIsPreferred(int isPreferred) {
		this.isPreferred = isPreferred;
	}
	
	
}
