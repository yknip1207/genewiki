/**
 * 
 */
package org.genewiki.mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.genewiki.GeneWikiPage;
import org.genewiki.GeneWikiUtils;
import org.scripps.nlp.ncbo.TextMapping;
import org.scripps.nlp.ncbo.web.AnnotatorClient;
import org.scripps.nlp.ncbo.web.NcboAnnotation;
import org.scripps.nlp.umls.metamap.MMannotation;
import org.scripps.nlp.umls.metamap.MetaMap;

/**
 * This class managed the process of mapping Wikipedia pages to terms from ontologies
 * @author bgood
 *
 */
public class Wiki2Concept {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String title = "mutation";//"cell surface";//"Reelin";//"brain";// "atherosclerosis"; //"diabetes";//"apoptosis"; //"cluster_of_differentiation";//"Dopamine_receptor_D1"; 
		GeneWikiPage prot = new GeneWikiPage(title);
		prot.defaultPopulate();
		Set<TextMapping> map = mapPage2Ncbo(prot);
		for(TextMapping m : map){
			System.out.println(m.input_text+"\t"+m.concept_id+"\t"+m.evidence+"\t"+m.concept_preferred_term+"\t"+m.ontology_id+"\t"+m.score);
		}
	}

	/**
	 * Given a GeneWikiPage, use its title and its synset (derived from all the redirects to it) to map to concepts using the annotator
	 * @param page
	 * @return
	 */
	public static Set<TextMapping> mapPage2Ncbo(GeneWikiPage page){
		Set<TextMapping> annos = new HashSet<TextMapping>();
		//ignore years
		if(page.getTitle().matches("^[0-9]{1,10}\b")){
			return null;
		}
		List<NcboAnnotation> title_annos = AnnotatorClient.ncboAnnotateText(page.getTitle().replaceAll("_"," "), false, true, true, true, true, false, false);		
		if(title_annos==null||title_annos.size()==0){
			for(String test : page.getWikisynset()){
				if(!test.equals(page.getTitle())){
					List<NcboAnnotation> redirect_annos = new ArrayList<NcboAnnotation>();
					redirect_annos.addAll(AnnotatorClient.ncboAnnotateText(test, false, true, true , true, true, false, false));
					for(NcboAnnotation anno : redirect_annos){
						TextMapping tm = new TextMapping("redirect",test,anno);	
						annos.add(tm);
					}
				}
			}
		}else{
			for(NcboAnnotation titleanno : title_annos){
				TextMapping tm = new TextMapping("title",page.getTitle(),titleanno);				
				annos.add(tm);
			}	
		}
		
		return annos;
	}
	
	/**
	 * Given a GeneWikiPage, use its title and its synset (derived from all the redirects to it) to map to concepts using a local metamap installation
	 * @param page
	 * @return
	 */
	public static Set<TextMapping> mapPage2MetamapGO(GeneWikiPage page){
		Set<TextMapping> annos = new HashSet<TextMapping>();
		//ignore years
		if(page.getTitle().matches("^[0-9]{1,10}\b")){
			return null;
		}
//		List<NcboAnnotation> title_annos = AnnotatorClient.ncboAnnotateTerm(page.getTitle().replaceAll("_"," "));		
		boolean singleTerm = true;
		List<MMannotation> title_annos = MetaMap.getCUIsFromText(page.getTitle().replaceAll("_", " "), "GO", singleTerm, "title");
		if(title_annos==null||title_annos.size()==0){
			for(String test : page.getWikisynset()){
				if(!test.equals(page.getTitle())){
					List<MMannotation> redirect_annos = new ArrayList<MMannotation>();
					redirect_annos.addAll(MetaMap.getCUIsFromText(test.replaceAll("_", " "), "GO", singleTerm, "redirect"));
					annos.addAll(MetaMap.getTextMappingsFromMMannos(redirect_annos, "GO"));
				}
			}
		}else{
			annos.addAll(MetaMap.getTextMappingsFromMMannos(title_annos, "GO"));	
		}
		
		return annos;
	}
	
}
