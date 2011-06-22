package org.gnf.ncbo.web;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

public class Concept {
	String id;
	String localConceptId;
	String fullId;
	String localOntologyId;
	boolean isTopLevel;
	String preferredName;
	List<String> synonyms;
	List<SemanticType> semanticTypes;
		
	
	public Concept(String id, String localConceptId, String fullId,
			String localOntologyId, boolean isTopLevel, String preferredName,
			List<String> synonyms, List<SemanticType> semanticTypes) {
		super();
		this.id = id;
		this.localConceptId = localConceptId;
		this.fullId = fullId;
		this.localOntologyId = localOntologyId;
		this.isTopLevel = isTopLevel;
		this.preferredName = preferredName;
		this.synonyms = synonyms;
		this.semanticTypes = semanticTypes;
	}
	public Concept() {
		// TODO Auto-generated constructor stub
	}
	
	public Concept(Element concept){
		setId(concept.getChildText("id"));
		setLocalConceptId(concept.getChildText("localConceptId"));
		setTopLevel(Boolean.parseBoolean(concept.getChildText("isTopLevel")));
		setLocalOntologyId(concept.getChildText("localOntologyId"));
		setPreferredName(concept.getChildText("preferredName"));
		List<String> syns = new ArrayList<String>();
		List<Element> synlist = concept.getChildren("synonyms");
		for(Element syn : synlist){
			syns.add(syn.getText());
		}
		setSynonyms(syns);
		List<Element> stypes = concept.getChildren("semanticTypes");
		List<SemanticType> semtypes = new ArrayList<SemanticType>(stypes.size());
		for(Element stype : stypes){
			Element stypebean = stype.getChild("semanticTypeBean");
			SemanticType st = new SemanticType(stypebean.getChildText("id"),stypebean.getChildText("semanticType"),stypebean.getChildText("description"));
			semtypes.add(st);
		}
		setSemanticTypes(semtypes);
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLocalConceptId() {
		return localConceptId;
	}
	public void setLocalConceptId(String localConceptId) {
		this.localConceptId = localConceptId;
	}
	public String getLocalOntologyId() {
		return localOntologyId;
	}
	public void setLocalOntologyId(String localOntologyId) {
		this.localOntologyId = localOntologyId;
	}
	public boolean isTopLevel() {
		return isTopLevel;
	}
	public void setTopLevel(boolean isTopLevel) {
		this.isTopLevel = isTopLevel;
	}
	public String getPreferredName() {
		return preferredName;
	}
	public void setPreferredName(String preferredName) {
		this.preferredName = preferredName;
	}
	public List<String> getSynonyms() {
		return synonyms;
	}
	public void setSynonyms(List<String> synonyms) {
		this.synonyms = synonyms;
	}
	public List<SemanticType> getSemanticTypes() {
		return semanticTypes;
	}
	public void setSemanticTypes(List<SemanticType> semanticTypes) {
		this.semanticTypes = semanticTypes;
	}
	public String getFullId() {
		return fullId;
	}
	public void setFullId(String fullId) {
		this.fullId = fullId;
	}
	
	
}
