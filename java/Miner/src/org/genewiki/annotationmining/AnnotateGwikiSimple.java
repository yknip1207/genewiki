/**
 * 
 */
package org.genewiki.annotationmining;

import java.util.List;

import org.genewiki.GeneWikiPage;
import org.genewiki.annotationmining.annotations.CandidateAnnotation;

/**
 * Provides a simple command-line program that will generate candidate gene annotations for a Gene Wiki article
 * @author bgood
 *
 */
public class AnnotateGwikiSimple {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args==null||args.length<1){
			System.out.println(getUsage());
			return;
		}
		GeneWikiPageMapper m = new GeneWikiPageMapper();
		String title = args[0];
		//get the page
		GeneWikiPage page = new GeneWikiPage(title);
		System.out.println("#Getting Gene Wiki data from Wikipedia via WikiTrust (http://www.wikitrust.net/) for: "+title);
		boolean worked = page.defaultPopulateWikiTrust();
		page.parseAndSetNcbiGeneId();
		System.out.println("#Sentences:\t"+page.getSentences().size());
		System.out.println("#Bytes:\t"+page.getSize());
		boolean allowSynonyms = false; boolean useGO = true;  boolean useDO = true; boolean useFMA = false; boolean usePRO = false;
		if(worked){
			System.out.println("#Processing text with NCBO Annotator...\n");
			List<CandidateAnnotation> annos = m.annotateArticleNCBO(page, allowSynonyms, useGO, useDO, useFMA, usePRO);
			if(annos!=null&&annos.size()>0){
				System.out.println("#Entrez_gene\tArticle_title\tSection_header\tMost_recent_editor\tScore_from_Annotator\tNCBO_ont_id/Term_id\tTerm_name\tSurrounding_text\tInline-references");
				for(CandidateAnnotation anno : annos){
					System.out.println(anno.getEntrez_gene_id()+"\t"+anno.getSource_wiki_page_title()+"\t"+anno.getSection_heading()+"\t"+anno.getLink_author()+"\t"+anno.getAnnotationScore()+"\t"+anno.getTarget_accession()+"\t"+anno.getTarget_preferred_term()+"\t"+anno.getParagraph_around_link()+"\t"+anno.getPubmed_references());
				}
			}else{
				System.out.println("#No GO or DO annotations found for "+title);
			}
		}else{
			System.out.println("#An error occurred when gathering data from Wikipedia.  Please check that the article title you requested exists.");
		}
	}

	public static String getUsage(){
		String use = "Please provide the title of a Wikipedia page that you would like to derive annotations from.\n For example \n" +
		"java -jar minegwikipage.jar FGD1";
		return use;
	}

}
