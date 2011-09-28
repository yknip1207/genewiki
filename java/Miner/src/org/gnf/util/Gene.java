package org.gnf.util;

import java.util.List;

public class Gene implements Comparable{

	String geneID;
	String geneSymbol;
	String geneDescription;
	String uniprot;
	boolean isPseudo;
	String genetype;
	List<String> altids;
	
	public String toString(){
		return(geneID+"\t"+geneSymbol+"\t"+geneDescription+"\tis pseudo\t"+isPseudo);
	}
	
	public String getGeneID() {
		return geneID;
	}
	public void setGeneID(String geneID) {
		this.geneID = geneID;
	}
	public String getGeneSymbol() {
		return geneSymbol;
	}
	public void setGeneSymbol(String geneSymbol) {
		this.geneSymbol = geneSymbol;
	}
	public String getGeneDescription() {
		return geneDescription;
	}
	public void setGeneDescription(String geneDescription) {
		this.geneDescription = geneDescription;
	}
	public List<String> getAltids() {
		return altids;
	}
	public void setAltids(List<String> altids) {
		this.altids = altids;
	}

	public boolean isPseudo() {
		return isPseudo;
	}

	public void setPseudo(boolean isPseudo) {
		this.isPseudo = isPseudo;
	}

	public String getGenetype() {
		return genetype;
	}

	public void setGenetype(String genetype) {
		this.genetype = genetype;
	}

	@Override
	public int compareTo(Object o) {
		Gene other = (Gene)o;
		return this.getGeneID().compareTo(other.getGeneID());
	}
	
	@Override
	public boolean equals(Object o){
		Gene other = (Gene)o;
		return this.getGeneID().equals(other.getGeneID());
	}

	public String getUniprot() {
		return uniprot;
	}

	public void setUniprot(String uniprot) {
		this.uniprot = uniprot;
	}

	
}
