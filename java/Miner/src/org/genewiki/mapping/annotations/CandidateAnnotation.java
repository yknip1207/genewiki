package org.genewiki.mapping.annotations;

import java.util.Set;

import org.scripps.nlp.ncbo.Ontologies;
import org.scripps.ontologies.go.GOterm;
import org.scripps.search.SearchEngineIntersect;
import org.scripps.util.BioInfoUtil;

/**
 * Holds the data needed to present a candidate annotation mined from the Gene Wiki
 * @author bgood
 *
 */
public class CandidateAnnotation implements Comparable{

	//gene and wiki page information
	String entrez_gene_id;
	String source_wiki_page_title;
	String paragraph_around_link;
	String section_heading;
	String link_author;
	Set<String> pubmed_references;
	String csvrefs;
	int	contentLength;
	
	//target page and vocabulary term information
	String target_wiki_page_title;
	String target_accession;
	String target_preferred_term;
	String target_vocabulary;
	String target_vocabulary_id;
	String vocabulary_branch; //if available - e.g. biological process, molecular function, cellular component from GO
	String target_uri;
	String target_semantic_types;
	
	//information about the process used to establish the map from the target page to the vocabulary concept
	String string_matching_method; //for GO link-mined, its one of {target_page_title||target_page_redirect --to-- term_preferred||term_syn_exact||term_syn_broader||term_syn_narrower||term_syn_related} 	
	String matched_text;
	
	String annotationTool; //ncbo, metamap, local
	String annotationMethod; //link or text
	double annotationScore; //from the annotation tool
	
	//does it look we put it in with the PBB?
	boolean isPBB;
	boolean hasBackLink;
	
	//wikitrust scores
	double wikitrust_page;
	double wikitrust_sentence;
	String wt_block;
	
	Evidence evidence;

	public CandidateAnnotation(String[] row){
		super();
		this.setEntrez_gene_id(row[0]);
		this.setSource_wiki_page_title(row[1]);
		this.setSection_heading(row[2].trim());
		
		this.setLink_author(row[3]);
		this.setWikitrust_page(Double.parseDouble(row[4]));
		this.setWikitrust_sentence(Double.parseDouble(row[5]));
		this.setWt_block(row[6]);
		this.setAnnotationScore(Double.parseDouble(row[7]));
		this.setContentLength(Integer.parseInt(row[8]));
		
//		this.setTarget_wiki_page_title(row[8]);
		this.setTarget_accession(row[9]);
		this.setTarget_preferred_term(row[10]);
		this.setTarget_vocabulary(row[11]);
		this.setVocabulary_branch(row[12]);
		this.setString_matching_method(row[13]);
		if(row.length>13){
			this.setParagraph_around_link(row[14]);
		}
		if(row.length>14){
			this.setCsvrefs(row[15]);
		}
		if(row.length>15){
			this.setPBB(Boolean.parseBoolean(row[16]));
		}
//		if(row.length>12){
//			this.setHasBackLink(Boolean.parseBoolean(row[12]));
//		}
		this.setEvidence(new Evidence());
		if(row.length==25){ //then we've already recorded evidence
			this.getEvidence().setConfidence(Double.parseDouble(row[17]));
			String match = row[18];
			if(match.equals("parent")){
				this.getEvidence().setMatches_parent_of_existing_annotation(true);
			}else if(match.equals("child")){
				this.getEvidence().setMatches_child_of_existing_annotation(true);
			}else if(match.equals("direct")){
				this.getEvidence().setMatches_existing_annotation_directly(true);
			}
			this.getEvidence().setNormalizedGoogleDistance(Double.parseDouble(row[19]));
			match = row[20];
			if(match.equals("parent")){
				this.getEvidence().setMatches_parent_of_panther_go(true);
			}else if(match.equals("child")){
				this.getEvidence().setMatches_child_of_panther_go(true);
			}else if(match.equals("direct")){
				this.getEvidence().setMatches_panther_go_directly(true);
			}
			match = row[21];
			if(match.equals("parent")){
				this.getEvidence().setMatches_parent_of_genego(true);
			}else if(match.equals("child")){
				this.getEvidence().setMatches_child_of_genego(true);
			}else if(match.equals("direct")){
				this.getEvidence().setMatches_genego_directly(true);
			}
			this.getEvidence().setFuncbase_score(Double.parseDouble(row[22]));
			match = row[23];
			if(match.equals("parent")){
				this.getEvidence().setMatches_parent_of_funcbase(true);
			}else if(match.equals("child")){
				this.getEvidence().setMatches_child_of_funcbase(true);
			}else if(match.equals("direct")){
				this.getEvidence().setMatches_funcbase_directly(true);
			}
			this.getEvidence().setPriorGO(Double.parseDouble(row[24]));
			this.getEvidence().setGoObsolete(Boolean.parseBoolean(row[25]));
		}
		
	}

	public CandidateAnnotation() {
		this.setEvidence(new Evidence());
	}

	public static String getHeader(){
		return "Entrez_gene_id\t" +
		"Source_wiki_page_title\t" +
		"Section_heading\t"+
		"Most recent Author\t"+
		"WikiTrust-Whole_Article\t"+
		"WikiTrust_Annotation_context\t"+
//		"WT_block\t"+
		"Annotator_score\t"+
//		"Article_length\t"+
//		"Target_wiki_page_title\t" +
		"Target_accession\t" +
		"Target_uri\t"+
		"Target_preferred_term\t"+
		"Target_semantic types\t" +
		"Target_vocabulary\t"+
		"Target_vocabulary_id\t"+
//		"Vocabulary_branch\t" +
//		"Evidence\t"+
		"Matched text\t"+
		"Surrounding sentence\t"+
		"References\t";
//		"Is PBB\t";
//		"has backlink\t";
//		"Tool\t" +
//		"Method\t";

	}


	public String toString(){
		String out = 
			this.getEntrez_gene_id()+"\t"+
			this.getSource_wiki_page_title()+"\t"+
			this.getSection_heading()+"\t"+
			//WT
			this.getLink_author()+"\t"+
			this.getWikitrust_page()+"\t"+
			this.getWikitrust_sentence()+"\t";
//			if(this.getWt_block()!=null&&this.getWt_block().length()>0){
//				out+= this.getWt_block().replaceAll("\t", " ").replaceAll("\n", " ")+"\t";
//			}else{
//				out+= "\t";
//			}
			out+=this.getAnnotationScore()+"\t"+
	//		this.getContentLength()+"\t"+
	//		this.getTarget_wiki_page_title()+"\t"+
			this.getTarget_accession()+"\t"+
			this.getTarget_uri()+"\t"+
			this.getTarget_preferred_term()+"\t"+
			this.getTarget_semantic_types()+"\t"+
			this.getTarget_vocabulary()+"\t"+
			this.getTarget_vocabulary_id()+"\t"+
			this.getMatched_text()+"\t";
	//		this.getVocabulary_branch()+"\t"+
	//		this.getString_matching_method()+"\t";

		if(this.getParagraph_around_link()!=null){
			out = out+this.getParagraph_around_link()+"\t";
		}else{
			out+= "\t";
		}
		if(csvrefs==null&&this.getPubmed_references()!=null){
				for(String pmid : this.getPubmed_references()){
					out = out+pmid+" ";
				}
				out += "\t";
		}else if(csvrefs!=null){
			out+= csvrefs+"\t";
		}else{
			out+= "\t";
		}
//		out+=this.isPBB()+"\t";
//		out+=this.isHasBackLink()+"\t";
//		out+=this.getAnnotationTool()+"\t";
//		out+=this.getAnnotationMethod()+"\t";
		return out;
	}

	@Override
	public int compareTo(Object o) {
		if(!(o instanceof CandidateAnnotation)){
			return -1;
		}else{
			CandidateAnnotation in = (CandidateAnnotation)o;
			return(in.getEvidence().compareTo(this.getEvidence()));
		}
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof CandidateAnnotation)){
			return false;
		}else{
			CandidateAnnotation in = (CandidateAnnotation)o;
			return(in.toString().equals(this.toString()));
		}
	}

	public int hashCode() {
		return this.toString().hashCode();
	}


	public boolean isTightMatch(){
		if(!(this.getString_matching_method().contains("related")||
				this.getString_matching_method().contains("broader")||
				this.getString_matching_method().contains("redirect"))){
			return true;
		}else{
			return false;
		}

	}

	public boolean isSmartMatch(){
		if(!(this.getString_matching_method().contains("related")||
				this.getString_matching_method().contains("broader"))){
			return true;
		}else{
			return false;
		}
	}

	public String getEntrez_gene_id() {
		return entrez_gene_id;
	}

	public void setEntrez_gene_id(String entrezGeneId) {
		entrez_gene_id = entrezGeneId;
	}

	public String getSource_wiki_page_title() {
		return source_wiki_page_title;
	}

	public void setSource_wiki_page_title(String sourceWikiPageTitle) {
		source_wiki_page_title = sourceWikiPageTitle;
	}

	public String getParagraph_around_link() {
		return paragraph_around_link;
	}

	public void setParagraph_around_link(String paragraphAroundLink) {
		paragraph_around_link = paragraphAroundLink;
	}

	public String getSection_heading() {
		return section_heading;
	}

	public void setSection_heading(String sectionHeading) {
		section_heading = sectionHeading;
	}

	public String getLink_author() {
		return link_author;
	}

	public void setLink_author(String linkAuthor) {
		link_author = linkAuthor;
	}

	public Set<String> getPubmed_references() {
		return pubmed_references;
	}

	public void setPubmed_references(Set<String> pubmedReferences) {
		pubmed_references = pubmedReferences;
	}

	public String getTarget_wiki_page_title() {
		return target_wiki_page_title;
	}

	public void setTarget_wiki_page_title(String targetWikiPageTitle) {
		target_wiki_page_title = targetWikiPageTitle;
	}

	public String getTarget_accession() {
		return target_accession;
	}

	public void setTarget_accession(String targetAccession) {
		target_accession = targetAccession;
	}

	public String getTarget_vocabulary() {
		return target_vocabulary;
	}

	public void setTarget_vocabulary(String targetVocabulary) {
		target_vocabulary = targetVocabulary;
	}

	public String getVocabulary_branch() {
		return vocabulary_branch;
	}

	public void setVocabulary_branch(String vocabularyBranch) {
		vocabulary_branch = vocabularyBranch;
	}

	public String getString_matching_method() {
		return string_matching_method;
	}

	public void setString_matching_method(String stringMatchingMethod) {
		string_matching_method = stringMatchingMethod;
	}

	public String getTarget_preferred_term() {
		return target_preferred_term;
	}

	public void setTarget_preferred_term(String targetPreferredTerm) {
		target_preferred_term = targetPreferredTerm;
	}

	public String getCsvrefs() {
		return csvrefs;
	}

	public void setCsvrefs(String csvrefs) {
		this.csvrefs = csvrefs;
	}

	public Evidence getEvidence() {
		return evidence;
	}

	public void setEvidence(Evidence evidence) {
		this.evidence = evidence;
	}

	public boolean isPBB() {
		return isPBB;
	}

	public void setPBB(boolean isPBB) {
		this.isPBB = isPBB;
	}

	public boolean isHasBackLink() {
		return hasBackLink;
	}

	public void setHasBackLink(boolean hasBackLink) {
		this.hasBackLink = hasBackLink;
	}

	public String getAnnotationTool() {
		return annotationTool;
	}

	public void setAnnotationTool(String annotationTool) {
		this.annotationTool = annotationTool;
	}

	public String getAnnotationMethod() {
		return annotationMethod;
	}

	public void setAnnotationMethod(String annotationMethod) {
		this.annotationMethod = annotationMethod;
	}

	public double getWikitrust_page() {
		return wikitrust_page;
	}

	public void setWikitrust_page(double wikitrust_page) {
		this.wikitrust_page = wikitrust_page;
	}

	public double getWikitrust_sentence() {
		return wikitrust_sentence;
	}

	public void setWikitrust_sentence(double wikitrust_sentence) {
		this.wikitrust_sentence = wikitrust_sentence;
	}

	public double getAnnotationScore() {
		return annotationScore;
	}

	public void setAnnotationScore(double annotationScore) {
		this.annotationScore = annotationScore;
	}

	public String getWt_block() {
		return wt_block;
	}

	public void setWt_block(String wt_block) {
		this.wt_block = wt_block;
	}

	public int getContentLength() {
		return contentLength;
	}

	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}

	public String getTarget_uri() {
		return target_uri;
	}

	public void setTarget_uri(String target_uri) {
		this.target_uri = target_uri;
	}

	public String getTarget_vocabulary_id() {
		return target_vocabulary_id;
	}

	public void setTarget_vocabulary_id(String target_vocabulary_id) {
		this.target_vocabulary_id = target_vocabulary_id;
	}

	public String getTarget_semantic_types() {
		return target_semantic_types;
	}

	public void setTarget_semantic_types(String target_semantic_types) {
		this.target_semantic_types = target_semantic_types;
	}

	public String getMatched_text() {
		return matched_text;
	}

	public void setMatched_text(String matched_text) {
		this.matched_text = matched_text;
	}

}
