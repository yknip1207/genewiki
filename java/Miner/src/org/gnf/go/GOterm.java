package org.gnf.go;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GOterm implements Comparable<GOterm>{

	String id;
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
	boolean isdirect;
	//especially in the case of text mining, we may get many occurrences of a term
	float numAppearances;
	
	
	
	public GOterm(String id, String accession, String root, String term, boolean isdirect) {
		super();
		this.isdirect = isdirect;
		this.id = id;
		this.accession = accession;
		this.term = term;
		this.root = root;
		this.synset_exact = new ArrayList<String>();
		synset_exact.add(term);
		this.synset_broader = new ArrayList<String>();
		this.synset_narrower = new ArrayList<String>();
		this.synset_related = new ArrayList<String>();
		this.inferred_parent = false;
		this.inferred_child = false;
		this.evidence = "";
	}
	
	/**
	 * Take a set of GOterms and make it so that it only has one value per accession - add multiple sources of evidence into the evidence field for the term
	 * @param gos
	 * @return
	 */
	public static Collection<GOterm> compressGOSet(Set<GOterm> gos){
		Map<String, GOterm> accs = new HashMap<String, GOterm>();
		for(GOterm go : gos){
			if(!accs.containsKey(go.getAccession())){
				accs.put(go.getAccession(), go);
			}else{
				GOterm got = accs.get(go.getAccession());
				if(!got.getEvidence().contains(go.getEvidence())){
					got.setEvidence(got.getEvidence()+"---"+go.getEvidence());
				}
				accs.put(go.getAccession(), got);
			}
		}
		return accs.values();
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}

	public String getAccession() {
		return accession;
	}
	public void setAccession(String accession) {
		this.accession = accession;
	}

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public List<String> getWikixrefs() {
		return wikixrefs;
	}

	public void setWikixrefs(List<String> wikixrefs) {
		this.wikixrefs = wikixrefs;
	}

	public List<String> getSynset_exact() {
		return synset_exact;
	}

	public void setSynset_exact(List<String> synsetExact) {
		synset_exact = synsetExact;
	}

	public List<String> getSynset_broader() {
		return synset_broader;
	}

	public void setSynset_broader(List<String> synsetBroader) {
		synset_broader = synsetBroader;
	}

	public List<String> getSynset_narrower() {
		return synset_narrower;
	}

	public void setSynset_narrower(List<String> synsetNarrower) {
		synset_narrower = synsetNarrower;
	}

	public List<String> getSynset_related() {
		return synset_related;
	}

	public void setSynset_related(List<String> synsetRelated) {
		synset_related = synsetRelated;
	}

	public String getEvidence() {
		return evidence;
	}

	public void setEvidence(String evidence) {
		this.evidence = evidence;
	}

	@Override
	public int compareTo(GOterm o) {
		GOterm in = (GOterm)o;
		return in.getAccession().compareTo(this.getAccession());
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof GOterm)){
			return false;
		}else{
			GOterm in = (GOterm)o;
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



public float getNumAppearances() {
	return numAppearances;
}

public void setNumAppearances(float numAppearances) {
	this.numAppearances = numAppearances;
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

public boolean isIsdirect() {
	return isdirect;
}

public void setIsdirect(boolean isdirect) {
	this.isdirect = isdirect;
}
}
