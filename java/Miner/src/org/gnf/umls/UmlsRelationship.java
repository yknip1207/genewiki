package org.gnf.umls;

public class UmlsRelationship {

	String source_concept_id;
	String relationship;
	String vocab;
	String target_concept_id;
	String target_concept_name;
		
	public UmlsRelationship(String sourceConceptId, String relationship,
			String vocab, String targetConceptId, String targetConceptName) {
		super();
		source_concept_id = sourceConceptId;
		this.relationship = relationship;
		this.vocab = vocab;
		target_concept_id = targetConceptId;
		target_concept_name = targetConceptName;
	}
	public String getSource_concept_id() {
		return source_concept_id;
	}
	public void setSource_concept_id(String sourceConceptId) {
		source_concept_id = sourceConceptId;
	}
	public String getRelationship() {
		return relationship;
	}
	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}
	public String getVocab() {
		return vocab;
	}
	public void setVocab(String vocab) {
		this.vocab = vocab;
	}
	public String getTarget_concept_id() {
		return target_concept_id;
	}
	public void setTarget_concept_id(String targetConceptId) {
		target_concept_id = targetConceptId;
	}
	public String getTarget_concept_name() {
		return target_concept_name;
	}
	public void setTarget_concept_name(String targetConceptName) {
		target_concept_name = targetConceptName;
	}
	
	
}
