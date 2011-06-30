package org.gnf.umls.metamap;

import java.util.List;

public class MMannotation {

	int start;
	int stop;
	String cui;
	String termName; 
	float score;
	String inputText;
	String evidence;
	List<String> sources;
	List<String> semanticTypes;
	
	public String toString(){
		return cui+"\t"+termName+"\t"+score+"\t"+start+"\t"+stop;
	}
	
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public String getCui() {
		return cui;
	}
	public void setCui(String cui) {
		this.cui = cui;
	}
	public float getScore() {
		return score;
	}
	public void setScore(float score) {
		this.score = score;
	}
	public int getStop() {
		return stop;
	}
	public void setStop(int stop) {
		this.stop = stop;
	}

	public String getTermName() {
		return termName;
	}

	public void setTermName(String termName) {
		this.termName = termName;
	}

	public String getInputText() {
		return inputText;
	}

	public void setInputText(String inputText) {
		this.inputText = inputText;
	}

	public String getEvidence() {
		return evidence;
	}

	public void setEvidence(String evidence) {
		this.evidence = evidence;
	}

	public List<String> getSources() {
		return sources;
	}

	public void setSources(List<String> sources) {
		this.sources = sources;
	}

	public List<String> getSemanticTypes() {
		return semanticTypes;
	}

	public void setSemanticTypes(List<String> semanticTypes) {
		this.semanticTypes = semanticTypes;
	}
	
	
}
