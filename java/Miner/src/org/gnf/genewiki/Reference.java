package org.gnf.genewiki;

import java.io.Serializable;

public class Reference implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String pmid;
	String url;
	String wikiText;
	String refName;
	int startIndex;
	int stopIndex;
	
	public static String regexP = "(?s)<ref {1,5}name {0,5}=[^\\/]{0,60}/>|<ref.+?</ref>";

	public String toShortString() {
		if(pmid!=null){
			return pmid;
		}else if(url!=null){
			if(url.startsWith("http://www.ncbi.nlm.nih.gov/sites/entrez?Db=gene")){
				return "NCBI Gene";
			}else{
				return url;
			}
		}
		return null;
	}
	
	public String toString(){
		String out = pmid+"\t"+refName+"\t"+url+"\t"+startIndex;
		if(pmid==null&&url==null){
			out+="\t"+wikiText;
		}
		return out;
	}
	
	public String toKeyString(){
		String out = pmid+"\t"+refName+"\t"+url;
		if(pmid==null&&url==null){
			out+="\t"+wikiText;
		}
		return out;
	}
	
	public String getPmid() {
		return pmid;
	}
	public void setPmid(String pmid) {
		this.pmid = pmid;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getWikiText() {
		return wikiText;
	}
	public void setWikiText(String wikiText) {
		this.wikiText = wikiText;
	}
	
	public int getStartIndex() {
		return startIndex;
	}
	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public String getRefName() {
		return refName;
	}

	public void setRefName(String refName) {
		this.refName = refName;
	}

	public int getStopIndex() {
		return stopIndex;
	}

	public void setStopIndex(int stopIndex) {
		this.stopIndex = stopIndex;
	}


	
	
}
