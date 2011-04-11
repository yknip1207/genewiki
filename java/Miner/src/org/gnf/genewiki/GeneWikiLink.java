package org.gnf.genewiki;

import java.io.Serializable;
import java.util.List;

import info.bliki.api.Link;

/**
 * @author bgood
 *
 */
public class GeneWikiLink implements Serializable {

	//from Link we get Wikipedia namespace and title
	//adding:

	/**
	 * 
	 */
	private static final long serialVersionUID = 1246448736648188326L;
	//the article that is being linked to
//	GeneWikiPage target_page;
	String target_page;
	//snippet
	String snippet;
	//text before the link
	String pretext;
	//text after link
	String posttext;
	//for context
	String sectionHeader;
	//predicate (this is a goal..)
	String predicate;
	//sentence where the link occurred
	String sentence;
	//paragraph that contained the sentence
	String paragraph;
	//citations following sentence
	List<String> citations;
	//pmids for citation
	List<String> pmids;
	//where in the text this was found
	int startIndex;
	
	public String getSectionHeader() {
		return sectionHeader;
	}

	public void setSectionHeader(String sectionHeader) {
		this.sectionHeader = sectionHeader;
	}


	
	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

	public String getPretext() {
		return pretext;
	}

	public void setPretext(String pretext) {
		this.pretext = pretext;
	}

	public String getPosttext() {
		return posttext;
	}

	public void setPosttext(String posttext) {
		this.posttext = posttext;
	}

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

//	public String toString(){
//		return predicate+" : "+this.getTarget_page().getTitle()+"\n\t"+this.getSnippet();
//	}
//
//	public GeneWikiPage getTarget_page() {
//		return target_page;
//	}
//
//	public void setTarget_page(GeneWikiPage targetPage) {
//		target_page = targetPage;
//	}

	public String getSentence() {
		return sentence;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	public String getParagraph() {
		return paragraph;
	}

	public void setParagraph(String paragraph) {
		this.paragraph = paragraph;
	}

	public List<String> getCitations() {
		return citations;
	}

	public void setCitations(List<String> citations) {
		this.citations = citations;
	}

	public List<String> getPmids() {
		return pmids;
	}

	public void setPmids(List<String> pmids) {
		this.pmids = pmids;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public String getTarget_page() {
		return target_page;
	}

	public void setTarget_page(String target_page) {
		this.target_page = target_page;
	}


}
