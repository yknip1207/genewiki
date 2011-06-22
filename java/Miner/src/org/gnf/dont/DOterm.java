package org.gnf.dont;

import java.util.List;

public class DOterm implements Comparable<DOterm>{
	
	String accession;
	String term;
	String root;
	String evidence;
	List<String> synset_exact;
	List<String> synset_broader;
	List<String> synset_narrower;
	List<String> synset_related;
	List<String> wikixrefs;
	//true if this annotation was inferred through a hierarchical relationship rather than a direct assertion
	boolean inferred_parent;
	boolean inferred_child;
	//especially in the case of text mining, we may get many occurrences of a term
	float numAppearances;
	
	
	@Override
	public int compareTo(DOterm o) {
		DOterm in = (DOterm)o;
		return in.getAccession().compareTo(this.getAccession());
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof DOterm)){
			return false;
		}else{
			DOterm in = (DOterm)o;
			if(in.getEvidence().equals(this.getEvidence())){
				return in.getAccession().equals(this.getAccession());
			}else{
				return false;
			}	
		}
	}
	
	public int hashCode() {
		int result = 17;
		if(this.getAccession()!=null){
			result = this.getAccession().hashCode();
		}
		if(this.getEvidence()!=null){
			result = result +this.getEvidence().hashCode();
		}
		return result;
		}
	
 public String toString(){
	 return this.getAccession()+"\t"+this.getTerm()+"\t"+this.getRoot();
 }

	
	
	public String getAccession() {
		return accession;
	}
	public void setAccession(String accession) {
		this.accession = accession;
	}
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public String getRoot() {
		return root;
	}
	public void setRoot(String root) {
		this.root = root;
	}
	public String getEvidence() {
		return evidence;
	}
	public void setEvidence(String evidence) {
		this.evidence = evidence;
	}
	public List<String> getSynset_exact() {
		return synset_exact;
	}
	public void setSynset_exact(List<String> synset_exact) {
		this.synset_exact = synset_exact;
	}
	public List<String> getSynset_broader() {
		return synset_broader;
	}
	public void setSynset_broader(List<String> synset_broader) {
		this.synset_broader = synset_broader;
	}
	public List<String> getSynset_narrower() {
		return synset_narrower;
	}
	public void setSynset_narrower(List<String> synset_narrower) {
		this.synset_narrower = synset_narrower;
	}
	public List<String> getSynset_related() {
		return synset_related;
	}
	public void setSynset_related(List<String> synset_related) {
		this.synset_related = synset_related;
	}
	public List<String> getWikixrefs() {
		return wikixrefs;
	}
	public void setWikixrefs(List<String> wikixrefs) {
		this.wikixrefs = wikixrefs;
	}
	public boolean isInferred_parent() {
		return inferred_parent;
	}
	public void setInferred_parent(boolean inferred_parent) {
		this.inferred_parent = inferred_parent;
	}
	public boolean isInferred_child() {
		return inferred_child;
	}
	public void setInferred_child(boolean inferred_child) {
		this.inferred_child = inferred_child;
	}
	public float getNumAppearances() {
		return numAppearances;
	}
	public void setNumAppearances(float numAppearances) {
		this.numAppearances = numAppearances;
	}

	

}
