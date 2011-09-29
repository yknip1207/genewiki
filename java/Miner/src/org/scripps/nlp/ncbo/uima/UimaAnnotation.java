package org.scripps.nlp.ncbo.uima;

public class UimaAnnotation {

	String annotatorName;
	int startIndex;
	int stopIndex;
	String docId;
	String coveredText;
	String ontologyTermId;
	String ncboOntologyId;
	String matchType;
	
	public String toString(){
		String out = "";
		out+=annotatorName+"\t"+startIndex+"\t"+stopIndex+"\t"+docId+"\t" +
				coveredText+"\t"+ontologyTermId+"\t"+matchType+"\t"+ncboOntologyId;
		return out;
	}
	
	public String getAnnotatorName() {
		return annotatorName;
	}
	public void setAnnotatorName(String annotatorName) {
		this.annotatorName = annotatorName;
	}
	public int getStartIndex() {
		return startIndex;
	}
	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}
	public int getStopIndex() {
		return stopIndex;
	}
	public void setStopIndex(int stopIndex) {
		this.stopIndex = stopIndex;
	}
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public String getCoveredText() {
		return coveredText;
	}
	public void setCoveredText(String coveredText) {
		this.coveredText = coveredText;
	}
	public String getOntologyTermId() {
		return ontologyTermId;
	}
	public void setOntologyTermId(String ontologyTermId) {
		this.ontologyTermId = ontologyTermId;
	}
	public String getNcboOntologyId() {
		return ncboOntologyId;
	}
	public void setNcboOntologyId(String ncboOntologyId) {
		this.ncboOntologyId = ncboOntologyId;
	}
	public String getMatchType() {
		return matchType;
	}
	public void setMatchType(String matchType) {
		this.matchType = matchType;
	}
	
	
	
}
