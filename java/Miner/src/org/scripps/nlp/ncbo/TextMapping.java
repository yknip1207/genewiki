package org.scripps.nlp.ncbo;

import org.scripps.nlp.ncbo.web.NcboAnnotation;
import org.scripps.nlp.umls.metamap.MMannotation;

public class TextMapping implements Comparable{

	public String input_text;
	public String concept_id;
	public String concept_preferred_term;
	public String ontology_id;
	public String ontology_name;
	public String purl;
	public String evidence;
	public double score;
	
	public TextMapping(String input_text, String concept_id, String concept_preferred_term,
			String ontology_id, String evidence, double score) {
		super();
		this.input_text = input_text;
		this.concept_id = concept_id;
		this.concept_preferred_term = concept_preferred_term;
		this.ontology_id = ontology_id;
		this.evidence = evidence;
		this.score = score;
	}
	
	public TextMapping(String evidence_context, String input_text, NcboAnnotation anno){
		if(anno.getContext().isDirect()){
			this.input_text = input_text;
			this.concept_id = anno.getConcept().getLocalConceptId();
			this.concept_preferred_term = anno.getConcept().getPreferredName();
			this.ontology_id = anno.getConcept().getLocalOntologyId();
			this.purl = anno.getConcept().getFullId();
			this.evidence = evidence_context+"_direct";
			this.score = anno.getScore();
		}else{
			this.input_text = input_text;
			this.concept_id = anno.getContext().getConcept().getLocalConceptId();
			this.concept_preferred_term = anno.getContext().getConcept().getPreferredName();
			this.ontology_id = anno.getContext().getConcept().getLocalOntologyId();
			if(anno.getContext().getLevel()>0){
				this.evidence = evidence_context+"_"+anno.getContext().getContextName()+"_"+anno.getContext().getLevel(); 
			}else{
				this.evidence = evidence_context;
			}
			this.score = anno.getScore();
		}
	}
	
	public TextMapping() {
		// TODO Auto-generated constructor stub
	}

	public String toString(){
		String out = "";
		out = this.evidence+"---"+this.input_text+"---"+this.concept_id+"---"+this.concept_preferred_term+"---"+this.score;
		out = out.replaceAll("\t", "");
		out = out.replaceAll("\n", "");
		out = out.replaceAll("---", "\t");
		return out;
	}
	
	public String getProvenance(){
		return this.input_text+"--"+this.evidence+"--"+this.score;
	}
	
	public String getInput_text() {
		return input_text;
	}
	public void setInput_text(String input_text) {
		this.input_text = input_text;
	}
	public String getConcept_id() {
		return concept_id;
	}
	public void setConcept_id(String concept_id) {
		this.concept_id = concept_id;
	}
	public String getOntology_id() {
		return ontology_id;
	}
	public void setOntology_id(String ontology_id) {
		this.ontology_id = ontology_id;
	}
	public String getEvidence() {
		return evidence;
	}
	public void setEvidence(String evidence) {
		this.evidence = evidence;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	
	@Override
	public int compareTo(Object o) {
		if(o.getClass()!=TextMapping.class){
			return -1;
		}
		TextMapping compareto = (TextMapping)o;
		if((compareto.concept_id.equals(this.concept_id))&&(compareto.evidence.equals(this.evidence))){
			return 0;
		}
		return -1;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o.getClass()!=TextMapping.class){
			return false;
		}
		TextMapping compareto = (TextMapping)o;
		if((compareto.concept_id.equals(this.concept_id))&&(compareto.evidence.equals(this.evidence))){
			return true;
		}
		return false;
	}
	
	@Override
	 public int hashCode() {
	    int code = concept_id.hashCode()+evidence.hashCode();
	    code = 31 * code;
	    return code;
	  }

	public String getConcept_preferred_term() {
		return concept_preferred_term;
	}

	public void setConcept_preferred_term(String concept_preferred_term) {
		this.concept_preferred_term = concept_preferred_term;
	}

	public String getOntology_name() {
		return ontology_name;
	}

	public void setOntology_name(String ontology_name) {
		this.ontology_name = ontology_name;
	}

	public String getPurl() {
		return purl;
	}

	public void setPurl(String purl) {
		this.purl = purl;
	}
	
}
