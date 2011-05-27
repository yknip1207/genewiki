/**
 * 
 */
package org.gnf.genewiki.mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gnf.genewiki.Heading;
import org.gnf.genewiki.Sentence;
import org.gnf.genewiki.associations.CandidateAnnotation;
import org.gnf.genewiki.lingpipe.SentenceSplitter;
import org.gnf.ncbo.web.AnnotatorClient;
import org.gnf.ncbo.web.NcboAnnotation;

/**
 * @author bgood
 *
 */
public class GenericTextToAnnotation {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	
	public static List<CandidateAnnotation> annotateTextWithNCBO(String source, String geneid, String text, boolean allowSynonyms, boolean useGO, boolean useDO, boolean useFMA, boolean usePRO){
		if(text==null){
			return null;
		}
		List<CandidateAnnotation> cannos = new ArrayList<CandidateAnnotation>();

		SentenceSplitter splitter = new SentenceSplitter();
		List<Sentence> sentences = splitter.splitWikiSentences(text);
		if(sentences==null||sentences.size()==0){
			return null;
		}
		for(Sentence s : sentences){
			Heading heading = new Heading();
			heading.setText("PubmedAbstract");
			List<NcboAnnotation> annos = AnnotatorClient.ncboAnnotateText(s.getPrettyText(), allowSynonyms, useGO, useDO, useFMA, usePRO, false);
			if(annos==null||annos.size()==0){
				continue;
			}
			Set<String> refs = new HashSet<String>();
			refs.add(source);
		
			for(NcboAnnotation anno : annos){
				//find the wikitrust signature
				String matched_text = anno.getContext().getMatched_text();				
				//build the annotation
				CandidateAnnotation canno = new CandidateAnnotation();
				canno.setEntrez_gene_id(geneid);
				canno.setContentLength(text.length());
				canno.setSource_wiki_page_title("");
				canno.setSection_heading(heading.getPrettyText()); 
				//trust related
				//NA
				//reference and context info						
				canno.setPubmed_references(refs);
				canno.setParagraph_around_link(s.getPrettyText());						

				//data about target
				canno.setTarget_wiki_page_title("");
				TextMapping tm = new TextMapping("ncbo_text", anno.getContext().getMatched_text(), anno);	
				canno.setAnnotationScore(tm.getScore());
				canno.setTarget_accession(tm.getConcept_id());
				canno.setTarget_preferred_term(tm.getConcept_preferred_term());
				canno.setTarget_vocabulary(tm.getOntology_id());
				canno.setVocabulary_branch("");
				//summary of String mapping
				canno.setString_matching_method(tm.getProvenance());		
			
				cannos.add(canno);
			}
			
		}
		return cannos;
	}
}
