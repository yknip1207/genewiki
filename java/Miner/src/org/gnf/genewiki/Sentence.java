package org.gnf.genewiki;

import java.io.Serializable;

public class Sentence implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int startIndex;
	int stopIndex;
	int nextStartIndex;
	String text;
	
	public String getPrettyText(){
		return makePrettyText(this.getText());
	}
	
	public static String makePrettyText(String input){
		String pretty = input;
		if(input!=null){
			//remove repeated spaces
			pretty = pretty.replaceAll("\\s+", " ");
			//remove wikitrust information blocks
			pretty = pretty.replaceAll("\\{\\{\\#.+?}}", "");		
			//remove whole templates
			pretty = pretty.replaceAll("\\{\\{.{1,100}\\}\\}", "");			
			//remove comments
			pretty = pretty.replaceAll("<!--[^>]*-->", "");
			//remove dangling comments
			pretty = pretty.replaceAll("<!--[^>]*", "");
			pretty = pretty.replaceAll("[^>]*-->", "");
			//remove PBB template leftovers
			pretty = pretty.replaceAll("PBB\\|geneid=\\d+", "");
			pretty = pretty.replaceAll("PBB_Summary \\| section_title = \\| summary_text = ", "");
			pretty = pretty.replaceAll("orphan\\|date=\\w+ \\d+", "");
			pretty = pretty.replaceAll("Orphan\\|date=\\w+ \\d+", "");
			//remove anything after we should be done..
//			/Orphan|date=February 2009
			pretty = pretty.replaceAll("==", "");
		}
		return pretty;
	}
	
	public static String cleanUpPBBSummaryTemplateText(String text){
		text = text.replace("{{PBB_Summary | section_title = | summary_text = ", "");
		text = text.replace("<ref name=\"entrez\"/>", "");
		text = text.replace("}}", "");
		return text;
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
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getNextStartIndex() {
		return nextStartIndex;
	}
	public void setNextStartIndex(int nextStartIndex) {
		this.nextStartIndex = nextStartIndex;
	}
	
	
	
}
