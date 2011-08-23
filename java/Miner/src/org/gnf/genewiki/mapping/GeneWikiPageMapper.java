/**
 * 
 */
package org.gnf.genewiki.mapping;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gnf.genewiki.Config;
import org.gnf.genewiki.GeneWikiPage;
import org.gnf.genewiki.GeneWikiUtils;
import org.gnf.genewiki.Heading;
import org.gnf.genewiki.Reference;
import org.gnf.genewiki.Sentence;
import org.gnf.genewiki.associations.CandidateAnnotation;
import org.gnf.genewiki.associations.Filter;
import org.gnf.genewiki.trust.GeneWikiTrust;
import org.gnf.genewiki.trust.WikiTrustBlock;
import org.gnf.genewiki.trust.WikiTrustClient;
import org.gnf.ncbo.Ontologies;
import org.gnf.ncbo.web.AnnotatorClient;
import org.gnf.ncbo.web.NcboAnnotation;

/**
 * @author bgood
 *
 */
public class GeneWikiPageMapper {

	Ontologies onts;
	
	
	public GeneWikiPageMapper(Ontologies onts) {
		this.onts = onts;
	}

	public GeneWikiPageMapper() {
		this.onts = new Ontologies();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GeneWikiPageMapper m = new GeneWikiPageMapper();
		System.out.println(CandidateAnnotation.getHeader());
		GeneWikiPage page = new GeneWikiPage();
		page.setTitle("NAP1L1");
		page.defaultPopulateWikiTrust();
		List<CandidateAnnotation> annos = m.annotateArticleNCBO(page, true);
		int i = 0;
		for(CandidateAnnotation anno : annos){
			i++;
			if(i<20){
				System.out.println(anno);
			}
		}
		System.out.println("Found "+i);
	}
	
	public void annotateGeneWikiArticlesWithNCBO(int limit, boolean allowSynonyms, boolean useGO, boolean useDO, boolean useFMA, boolean usePRO){
		File folder = new File(Config.gwikidir_wt);
		//check which files have already been processed
		Set<String> donegeneids = GeneWikiUtils.getDoneGeneIds(Config.text_mined_annos);
		
		if(folder.isDirectory()){
			int n = 0;
			for(String title : folder.list()){
				n++;
				if(n%100==0){
					System.out.print("finished annotating "+n+"\t");
				}
				if(n%1000==0){
					System.out.println("");
				}
				if(n>limit){
					break;
				}
				if(donegeneids.contains(title)){
					continue;
				}
				GeneWikiPage page = GeneWikiUtils.deserializeGeneWikiPage(Config.gwikidir_wt+title);				
				List<CandidateAnnotation> annos = annotateArticleNCBO(page, allowSynonyms, useGO, useDO, useFMA, usePRO);
				try {
					FileWriter w = new FileWriter(Config.text_mined_annos, true);
					for(CandidateAnnotation anno : annos){
						w.write(anno.toString()+"\n");
					}
					w.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}
	
	public List<CandidateAnnotation> annotateArticleNCBO(GeneWikiPage page, boolean allowSynonyms){
		List<CandidateAnnotation> cannos = new ArrayList<CandidateAnnotation>();
		int ignoreafterindex = page.getPageContent().lastIndexOf("References");
		List<Sentence> sentences = page.getSentences();
		if(sentences==null||sentences.size()==0){
			return null;
		}
		//assumes that the text used to feed the annotator was marked up with WikITrust
		List<WikiTrustBlock> blocks = WikiTrustClient.getTrustBlocks(page.getPageContent());
		//used to tag candidates for problems such as being from the PBB
		Filter f = new Filter();
		for(Sentence s : sentences){
			Heading heading = page.getHeadingByTextIndex(s.getStartIndex());
			String text = s.getPrettyText();
			if(text==null||text.length()<5){
				continue;
			}
	//		System.out.println(text);
			List<NcboAnnotation> annos = AnnotatorClient.ncboAnnotateText(s.getPrettyText(), allowSynonyms);
			if(annos==null||annos.size()==0){
				continue;
			}
			Set<String> refs = new HashSet<String>();
			//record references linked to this sentence and hence the annotations that fall out of it
			List<Reference> r = page.getRefsForSentence(s, ignoreafterindex);
			if(r!=null){
				for(Reference rf : r){
					//only include things with a pmid or a url
					if(rf.getPmid()!=null||rf.getUrl()!=null){
						refs.add(rf.toShortString()+",");
					}
				}
			}
			//get wikitrust blocks overlapping with this sentence
			List<WikiTrustBlock> wt_blocks = GeneWikiTrust.getTrustBlocksForSentence(blocks, s);
			
			for(NcboAnnotation anno : annos){
				//find the wikitrust signature
				String matched_text = anno.getContext().getMatched_text();
				boolean wt = false;
				WikiTrustBlock matched_wt = null;
				//check the overlapping wikitrust blocks for the text matching the annotation
				for(WikiTrustBlock block : wt_blocks){
					if(block.getText().contains(matched_text)){
						wt = true;
						matched_wt = block;
						break;
					}else{
						wt = false;
					}
				}				
				//build the annotation
				CandidateAnnotation canno = new CandidateAnnotation();
				canno.setEntrez_gene_id(page.getNcbi_gene_id());
				canno.setContentLength(page.getSize());
				canno.setSource_wiki_page_title(page.getTitle());
				canno.setSection_heading(heading.getPrettyText()); 
				//trust related
				canno.setWikitrust_page(page.getPageTrust());
				if(wt){
					canno.setLink_author(matched_wt.getEditor());
					canno.setWikitrust_sentence(matched_wt.getTrust());
					canno.setWt_block(matched_wt.getText());
				}else{
					canno.setLink_author("unknown");
					canno.setWikitrust_sentence(0);
				}
				//reference and context info						
				canno.setPubmed_references(refs);
				canno.setParagraph_around_link(s.getPrettyText());						

				//data about target (target wiki page is a legacy from when this began and we were only looking at wikilinks)
				canno.setTarget_wiki_page_title("");
				TextMapping tm = new TextMapping("ncbo_text", anno.getContext().getMatched_text(), anno);	
				canno.setAnnotationScore(tm.getScore());
				canno.setTarget_accession(tm.getConcept_id().substring(tm.getConcept_id().indexOf("/")+1));
				canno.setTarget_preferred_term(tm.getConcept_preferred_term());
				canno.setTarget_vocabulary_id(tm.getOntology_id());
				canno.setTarget_vocabulary(onts.ont_names.get(tm.getOntology_id()));
				canno.setTarget_uri(tm.getPurl());
				canno.setVocabulary_branch("");
				//summary of String mapping
				canno.setString_matching_method(tm.getProvenance());		

				if(f.isProteinBoxBotSummary(page,canno)){
					canno.setPBB(true);
				}
		//		System.out.println(canno.getEntrez_gene_id()+"\t"+canno.getSource_wiki_page_title()+"\t"+canno.getSection_heading()+"\t"+canno.getLink_author()+"\t"+canno.getAnnotationScore()+"\t"+canno.getTarget_accession()+"\t"+canno.getTarget_preferred_term()+"\t"+canno.getParagraph_around_link()+"\t"+canno.getPubmed_references());
				cannos.add(canno);
			}
			
		}
		return cannos;
	}
	
	
	/**
	 * Kept around in case its needed for upcoming revisions to article.  Consider using the newer, probably better all ontologies method
	 * @param page
	 * @param allowSynonyms
	 * @param useGO
	 * @param useDO
	 * @param useFMA
	 * @param usePRO
	 * @return
	 */
	public List<CandidateAnnotation> annotateArticleNCBO(GeneWikiPage page, boolean allowSynonyms, boolean useGO, boolean useDO, boolean useFMA, boolean usePRO){
		List<CandidateAnnotation> cannos = new ArrayList<CandidateAnnotation>();
		int ignoreafterindex = page.getPageContent().lastIndexOf("References");
		List<Sentence> sentences = page.getSentences();
		if(sentences==null||sentences.size()==0){
			return null;
		}
		//assumes that the text used to feed the annotator was marked up with WikITrust
		List<WikiTrustBlock> blocks = WikiTrustClient.getTrustBlocks(page.getPageContent());
		//used to tag candidates for problems such as being from the PBB
		Filter f = new Filter();
		for(Sentence s : sentences){
			Heading heading = page.getHeadingByTextIndex(s.getStartIndex());
			String text = s.getPrettyText();
			if(text==null||text.length()<5){
				continue;
			}
	//		System.out.println(text);
			List<NcboAnnotation> annos = AnnotatorClient.ncboAnnotateText(s.getPrettyText(), allowSynonyms, useGO, useDO, useFMA, usePRO, false, false);
			if(annos==null||annos.size()==0){
				continue;
			}
			Set<String> refs = new HashSet<String>();
			//record references linked to this sentence and hence the annotations that fall out of it
			List<Reference> r = page.getRefsForSentence(s, ignoreafterindex);
			if(r!=null){
				for(Reference rf : r){
					//only include things with a pmid or a url
					if(rf.getPmid()!=null||rf.getUrl()!=null){
						refs.add(rf.toShortString()+",");
					}
				}
			}
			//get wikitrust blocks overlapping with this sentence
			List<WikiTrustBlock> wt_blocks = GeneWikiTrust.getTrustBlocksForSentence(blocks, s);
			
			for(NcboAnnotation anno : annos){
				//find the wikitrust signature
				String matched_text = anno.getContext().getMatched_text();
				boolean wt = false;
				WikiTrustBlock matched_wt = null;
				//check the overlapping wikitrust blocks for the text matching the annotation
				for(WikiTrustBlock block : wt_blocks){
					if(block.getText().contains(matched_text)){
						wt = true;
						matched_wt = block;
						break;
					}else{
						wt = false;
					}
				}				
				//build the annotation
				CandidateAnnotation canno = new CandidateAnnotation();
				canno.setEntrez_gene_id(page.getNcbi_gene_id());
				canno.setContentLength(page.getSize());
				canno.setSource_wiki_page_title(page.getTitle());
				canno.setSection_heading(heading.getPrettyText()); 
				//trust related
				canno.setWikitrust_page(page.getPageTrust());
				if(wt){
					canno.setLink_author(matched_wt.getEditor());
					canno.setWikitrust_sentence(matched_wt.getTrust());
					canno.setWt_block(matched_wt.getText());
				}else{
					canno.setLink_author("unknown");
					canno.setWikitrust_sentence(0);
				}
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

				if(f.isProteinBoxBotSummary(page,canno)){
					canno.setPBB(true);
				}
		//		System.out.println(canno.getEntrez_gene_id()+"\t"+canno.getSource_wiki_page_title()+"\t"+canno.getSection_heading()+"\t"+canno.getLink_author()+"\t"+canno.getAnnotationScore()+"\t"+canno.getTarget_accession()+"\t"+canno.getTarget_preferred_term()+"\t"+canno.getParagraph_around_link()+"\t"+canno.getPubmed_references());
				cannos.add(canno);
			}
			
		}
		return cannos;
	}

}
