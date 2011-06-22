package org.gnf.ncbo.web;

public class SemanticType {
	String id;
	String semanticType;
	String description;
	
	public SemanticType(String id, String semanticType, String description) {
		super();
		this.id = id;
		this.semanticType = semanticType;
		this.description = description;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSemanticType() {
		return semanticType;
	}
	public void setSemanticType(String semanticType) {
		this.semanticType = semanticType;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	
}
