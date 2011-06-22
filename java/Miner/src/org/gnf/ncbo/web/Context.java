package org.gnf.ncbo.web;

public class Context {

	String contextClass; //mregpContextBean, isa closure, mapping
	//MGREP
	String contextName;
	boolean isDirect;
	int from;
	int to;
	Term term;
	String matched_text;
	//isa_closure
	String childConceptId;
	int level;
	//mapping
	String mappingConceptId;
	String mappingType;
	//optional for concepts derived from mappings
	Concept concept;
	
	public String getContextClass() {
		return contextClass;
	}
	public void setContextClass(String contextClass) {
		this.contextClass = contextClass;
	}
	public String getContextName() {
		return contextName;
	}
	public void setContextName(String contextName) {
		this.contextName = contextName;
	}
	public boolean isDirect() {
		return isDirect;
	}
	public void setDirect(boolean isDirect) {
		this.isDirect = isDirect;
	}
	public int getFrom() {
		return from;
	}
	public void setFrom(int from) {
		this.from = from;
	}
	public int getTo() {
		return to;
	}
	public void setTo(int to) {
		this.to = to;
	}
	public Term getTerm() {
		return term;
	}
	public void setTerm(Term term) {
		this.term = term;
	}
	public String getChildConceptId() {
		return childConceptId;
	}
	public void setChildConceptId(String childConceptId) {
		this.childConceptId = childConceptId;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public String getMappingConceptId() {
		return mappingConceptId;
	}
	public void setMappingConceptId(String mappingConceptId) {
		this.mappingConceptId = mappingConceptId;
	}
	public String getMappingType() {
		return mappingType;
	}
	public void setMappingType(String mappingType) {
		this.mappingType = mappingType;
	}
	public Concept getConcept() {
		return concept;
	}
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	public String getMatched_text() {
		return matched_text;
	}
	public void setMatched_text(String matched_text) {
		this.matched_text = matched_text;
	}
	
	
}
