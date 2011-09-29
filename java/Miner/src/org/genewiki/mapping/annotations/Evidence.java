/**
 * 
 */
package org.genewiki.mapping.annotations;

import org.scripps.nlp.ncbo.TextMapping;
import org.scripps.search.SearchEngineIntersect;

/**
 * @author bgood
 *
 */
public class Evidence {

	
	//computed value for sorting
	double confidence; 
	
	//This records the result of comparing search results using the gene, using the preferred term of the matching concept, and of both
	//It contains the 'normalized google distance' for the two terms (though we are using Yahoo's data because they have a nicer API)
	SearchEngineIntersect sect; 
	//if we've already computed the ngd we can ctch it here
	double normalizedGoogleDistance;
	
	//These record whether the candidate annotation agrees with an annotation from an existing standard annotation database (like GOA for the GO)
	boolean matches_existing_annotation_directly;
	boolean matches_parent_of_existing_annotation;
	boolean matches_child_of_existing_annotation;
	
	String go_evidence_type;
	
	///////////////
	//Optional parameters specific to Gene Ontology annotations
	///////////////
	
	//Panther family (functional orthology) analysis
	boolean matches_panther_go_directly;
	boolean matches_parent_of_panther_go;
	boolean matches_child_of_panther_go;

	//GeneGo (PubMed co-occurrence) analysis
	boolean matches_genego_directly;
	boolean matches_parent_of_genego;
	boolean matches_child_of_genego;
	
	//FuncBase (aggregated computational predictors)
	double funcbase_score;
	boolean matches_funcbase_directly;
	boolean matches_parent_of_funcbase;
	boolean matches_child_of_funcbase;
	
	//the prior probability of seeing this gene annotation
	double priorGO;

	//Whether the Gene Ontology term has been declared obsolete
	boolean goObsolete;
	
	//How deep in the tree
	double goDepth;
		
	public Evidence() {
		confidence = 0;
		normalizedGoogleDistance = 0;
		matches_existing_annotation_directly = false;
		matches_parent_of_existing_annotation = false;
		matches_child_of_existing_annotation = false;

		matches_panther_go_directly = false;
		matches_parent_of_panther_go = false;
		matches_child_of_panther_go = false;

		matches_genego_directly = false;
		matches_parent_of_genego = false;
		matches_child_of_genego = false;
		
		funcbase_score = 0;
		matches_funcbase_directly = false;
		matches_parent_of_funcbase = false;
		matches_child_of_funcbase = false;
		
		priorGO = 0;
		goObsolete = false;
		
		goDepth = 0;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	
	public static String getHeaderString(){
		String out =  
		"Confidence\t" +
		"Matches_existing_annotation\t" +
		"Normalized_Yahoo_Similarity\t" +
		"Gene_Yahoo_count\t" +
		"OntTerm_Yahoo_count\t";		
		return out;
	} 

	public String toString(){
		String out = 
			this.getConfidence()+"\t";
		if(this.isMatches_existing_annotation_directly()){
			out+="direct\t";
		}else if(this.isMatches_child_of_existing_annotation()){
			out+="child\t";
		}else if(this.isMatches_parent_of_existing_annotation()){
			out+="parent\t";
		}else{
			out+="none\t";
		}
			if(this.getSect()!=null){
				out+=this.getSect().getNormalizedYahooRank()+"\t"+this.getSect().getTerm1_hits()+"\t"+this.getSect().getTerm2_hits()+"\t";
			}else{
				out+= this.getNormalizedGoogleDistance()+"\t";
			}
			return out;
	}

	public static String getGOheaderString(){
		String h = getHeaderString();
		h+="Panther\t"+
		"GeneGo\t"+
		"FuncBase_Score\t"+
		"FuncBase_hit\t"+
		"GOA_prior\t"+
		"GO_Obsolete\t"+
		"GO_Depth\t";
		return h;
	}
	
	public String toGOstring(){
		String s = toString();
			if(this.isMatches_panther_go_directly()){
				s+="direct\t";
			}else if(this.isMatches_child_of_panther_go()){
				s+="child\t";
			}else if(this.isMatches_parent_of_panther_go()){
				s+="parent\t";
			}else{
				s+="none\t";
			}
			if(this.isMatches_genego_directly()){
				s+="direct\t";
			}else if(this.isMatches_child_of_genego()){
				s+="child\t";
			}else if(this.isMatches_parent_of_genego()){
				s+="parent\t";
			}else{
				s+="none\t";
			}
			s+=this.getFuncbase_score()+"\t";
			if(this.isMatches_funcbase_directly()){
				s+="direct\t";
			}else if(this.isMatches_child_of_funcbase()){
				s+="child\t";
			}else if(this.isMatches_parent_of_funcbase()){
				s+="parent\t";
			}else{
				s+="none\t";
			}
		s+=this.getPriorGO()+"\t";
		if(this.isGoObsolete()){
			s+="1\t";
		}else{
			s+="0\t";
		}
		s+=this.getGoDepth()+"\t";
		return s;
	}
	
	public static String getGOheaderStringv1(){
		String h = getHeaderString();
		h+="Panther direct\t"+
		"Panther parent\t" +
		"Panther child\t" +
		"GeneGo direct\t" +
		"GeneGo parent\t" +
		"GeneGo child\t" +
		"FuncBase direct\t"+
		"FuncBaseScore\t"+
		"FuncBase parent\t"+
		"FuncBase child\t" +
		"GOA prior\t"+
		"GO Obsolete\t";
		return h;
	}
	
	public String toGOstringv1(){
		String s = toString()+
		this.isMatches_panther_go_directly()+"\t"+
		this.isMatches_parent_of_panther_go()+"\t"+
		this.isMatches_child_of_panther_go()+"\t"+
		this.isMatches_genego_directly()+"\t"+
		this.isMatches_parent_of_genego()+"\t"+
		this.isMatches_child_of_genego()+"\t"+
		this.getFuncbase_score()+"\t"+
		this.isMatches_funcbase_directly()+"\t"+
		this.isMatches_parent_of_funcbase()+"\t"+
		this.isMatches_child_of_funcbase()+"\t"+
		this.getPriorGO()+"\t"+
		this.isGoObsolete()+"\t";
		
		return s;
	}
	
	public double getConfidence() {
		return confidence;
	}


	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}


	public SearchEngineIntersect getSect() {
		return sect;
	}


	public void setSect(SearchEngineIntersect sect) {
		this.sect = sect;
	}


	public boolean isMatches_existing_annotation_directly() {
		return matches_existing_annotation_directly;
	}


	public void setMatches_existing_annotation_directly(
			boolean matches_existing_annotation_directly) {
		this.matches_existing_annotation_directly = matches_existing_annotation_directly;
	}


	public boolean isMatches_parent_of_existing_annotation() {
		return matches_parent_of_existing_annotation;
	}


	public void setMatches_parent_of_existing_annotation(
			boolean matches_parent_of_existing_annotation) {
		this.matches_parent_of_existing_annotation = matches_parent_of_existing_annotation;
	}


	public boolean isMatches_child_of_existing_annotation() {
		return matches_child_of_existing_annotation;
	}


	public void setMatches_child_of_existing_annotation(
			boolean matches_child_of_existing_annotation) {
		this.matches_child_of_existing_annotation = matches_child_of_existing_annotation;
	}


	public int compareTo(Evidence evidence) {
		if(evidence.getConfidence()>this.getConfidence()){
			return -1;
		}else if(evidence.getConfidence()<this.getConfidence()){
			return 1;
		}else {
			return 0;
		}
	}


	public boolean isMatches_panther_go_directly() {
		return matches_panther_go_directly;
	}


	public void setMatches_panther_go_directly(boolean matches_panther_go_directly) {
		this.matches_panther_go_directly = matches_panther_go_directly;
	}


	public boolean isMatches_parent_of_panther_go() {
		return matches_parent_of_panther_go;
	}


	public void setMatches_parent_of_panther_go(boolean matches_parent_of_panther_go) {
		this.matches_parent_of_panther_go = matches_parent_of_panther_go;
	}


	public boolean isMatches_child_of_panther_go() {
		return matches_child_of_panther_go;
	}


	public void setMatches_child_of_panther_go(boolean matches_child_of_panther_go) {
		this.matches_child_of_panther_go = matches_child_of_panther_go;
	}


	public boolean isMatches_genego_directly() {
		return matches_genego_directly;
	}


	public void setMatches_genego_directly(boolean matches_genego_directly) {
		this.matches_genego_directly = matches_genego_directly;
	}


	public boolean isMatches_parent_of_genego() {
		return matches_parent_of_genego;
	}


	public void setMatches_parent_of_genego(boolean matches_parent_of_genego) {
		this.matches_parent_of_genego = matches_parent_of_genego;
	}


	public boolean isMatches_child_of_genego() {
		return matches_child_of_genego;
	}


	public void setMatches_child_of_genego(boolean matches_child_of_genego) {
		this.matches_child_of_genego = matches_child_of_genego;
	}


	public double getFuncbase_score() {
		return funcbase_score;
	}


	public void setFuncbase_score(double funcbase_score) {
		this.funcbase_score = funcbase_score;
	}


	public boolean isMatches_funcbase_directly() {
		return matches_funcbase_directly;
	}


	public void setMatches_funcbase_directly(boolean matches_funcbase_directly) {
		this.matches_funcbase_directly = matches_funcbase_directly;
	}


	public boolean isMatches_parent_of_funcbase() {
		return matches_parent_of_funcbase;
	}


	public void setMatches_parent_of_funcbase(boolean matches_parent_of_funcbase) {
		this.matches_parent_of_funcbase = matches_parent_of_funcbase;
	}


	public boolean isMatches_child_of_funcbase() {
		return matches_child_of_funcbase;
	}


	public void setMatches_child_of_funcbase(boolean matches_child_of_funcbase) {
		this.matches_child_of_funcbase = matches_child_of_funcbase;
	}


	public double getPriorGO() {
		return priorGO;
	}


	public void setPriorGO(double priorGO) {
		this.priorGO = priorGO;
	}


	public boolean isGoObsolete() {
		return goObsolete;
	}


	public void setGoObsolete(boolean goObsolete) {
		this.goObsolete = goObsolete;
	}


	public double getNormalizedGoogleDistance() {
		return normalizedGoogleDistance;
	}


	public void setNormalizedGoogleDistance(double normalizedGoogleDistance) {
		this.normalizedGoogleDistance = normalizedGoogleDistance;
	}


	public double getGoDepth() {
		return goDepth;
	}


	public void setGoDepth(double goDepth) {
		this.goDepth = goDepth;
	}


	public String getGo_evidence_type() {
		return go_evidence_type;
	}


	public void setGo_evidence_type(String go_evidence_type) {
		this.go_evidence_type = go_evidence_type;
	}

	
	
}
