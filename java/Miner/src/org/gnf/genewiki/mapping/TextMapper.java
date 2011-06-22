package org.gnf.genewiki.mapping;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gnf.genewiki.Config;
import org.gnf.genewiki.GeneWikiLink;
import org.gnf.genewiki.GeneWikiPage;
import org.gnf.genewiki.GeneWikiUtils;
import org.gnf.genewiki.Heading;
import org.gnf.genewiki.Reference;
import org.gnf.genewiki.Sentence;
import org.gnf.genewiki.associations.CandidateAnnotation;
import org.gnf.genewiki.associations.CandidateAnnotations;
import org.gnf.genewiki.associations.Evidence;
import org.gnf.go.GOowl;
import org.gnf.go.GOterm;
import org.gnf.ncbo.Ontologies;
import org.gnf.ncbo.web.AnnotatorClient;
import org.gnf.ncbo.web.NcboAnnotation;
import org.gnf.umls.UmlsDb;
import org.gnf.umls.metamap.MMannotation;
import org.gnf.umls.metamap.MetaMap;

public class TextMapper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//buildGwikilinks2ConceptsWithNCBOAnnotator();
		//summarizeMinedConcepts();
		//writeCandidateAnnotationsFromLinks();
		//getGwikitext2ConceptsWithNCBOAnnotator();
		//filterCandidateAnnotationsFromText();
		//mergeTextAndLink();

		//buildGwikilinks2GOConceptsWithMetamap();
		//writeCandidateAnnotationsFromLinksMetamap();
		//getGwikiText2GOWithMetaMap();
		//mergeTextAndLink();
	}


	/**
	 * Merge text and link-mined annotations from both NCBO and MetaMap
	 */
	public static void mergeTextAndLink(){
		Map<String, CandidateAnnotation> distinct = new HashMap<String, CandidateAnnotation>();

		////////NCBO//////////////
		CandidateAnnotations textannos = new CandidateAnnotations();
		textannos.loadCandidateAnnotations(Config.text_mined_annos);
		int isas = 0; int dir = 0;
		for(CandidateAnnotation anno : textannos.getCannos()){
			String m = anno.getString_matching_method();
			m = m.replaceAll("text--text", "ncbo--text");
			anno.setString_matching_method(m);
			CandidateAnnotation ca = distinct.get(anno.getEntrez_gene_id()+" "+anno.getTarget_accession());
			if(ca!=null){
				ca.setString_matching_method(ca.getString_matching_method()+"---"+anno.getString_matching_method());
				distinct.put(anno.getEntrez_gene_id()+" "+anno.getTarget_accession(),ca);
			}else{
				//only moving direct annotations (no parents) into the main merge file
				if(!anno.getString_matching_method().contains("ISA")){
					distinct.put(anno.getEntrez_gene_id()+" "+anno.getTarget_accession(),anno);
					dir++;
				}else{
					isas++;
				}
			}
		}
		System.out.println("skipped "+isas+" from text mining kept "+dir);
		isas = 0; dir = 0;
		CandidateAnnotations linkannos = new CandidateAnnotations();
		linkannos.loadCandidateAnnotations(Config.link_mined_annos);

		for(CandidateAnnotation anno : linkannos.getCannos()){
			String m = anno.getString_matching_method();
			m = m.replaceAll("title_","ncbo_title_");
			m = m.replaceAll("redirect_","ncbo_redirect_");
			anno.setString_matching_method(m);
			CandidateAnnotation ca = distinct.get(anno.getEntrez_gene_id()+" "+anno.getTarget_accession());
			if(ca!=null){
				ca.setString_matching_method(ca.getString_matching_method()+"---"+anno.getString_matching_method());
				distinct.put(anno.getEntrez_gene_id()+" "+anno.getTarget_accession(),ca);
			}else{
				//only moving direct annotations (no parents) into the main merge file
				if(!anno.getString_matching_method().contains("ISA")){
					distinct.put(anno.getEntrez_gene_id()+" "+anno.getTarget_accession(),anno);
					dir++;
				}else{
					isas++;
				}
			}
		}
		System.out.println("skipped "+isas+" from link mining kept "+dir);
		/////////MetaMap
		//// only contains GO annotations in this rendition
		/////////////
		CandidateAnnotations textannosmm = new CandidateAnnotations();
		textannosmm.loadCandidateAnnotations(Config.text_mined_annos_mm);

		for(CandidateAnnotation anno : textannosmm.getCannos()){
			CandidateAnnotation ca = distinct.get(anno.getEntrez_gene_id()+" 44171/"+anno.getTarget_accession());
			if(ca!=null){
				ca.setString_matching_method(ca.getString_matching_method()+"---"+anno.getString_matching_method());
				distinct.put(anno.getEntrez_gene_id()+" 44171/"+anno.getTarget_accession(),ca);
			}else{
				distinct.put(anno.getEntrez_gene_id()+" 44171/"+anno.getTarget_accession(),anno);
			}
		}

		CandidateAnnotations linkannosmm = new CandidateAnnotations();
		linkannosmm.loadCandidateAnnotations(Config.link_mined_annos_mm);

		for(CandidateAnnotation anno : linkannosmm.getCannos()){
			CandidateAnnotation ca = distinct.get(anno.getEntrez_gene_id()+" 44171/"+anno.getTarget_accession());
			if(ca!=null){
				ca.setString_matching_method(ca.getString_matching_method()+"---"+anno.getString_matching_method());
				distinct.put(anno.getEntrez_gene_id()+" 44171/"+anno.getTarget_accession(),ca);
			}else{
				distinct.put(anno.getEntrez_gene_id()+" 44171/"+anno.getTarget_accession(),anno);
			}
		}

		FileWriter writer;
		try {
			writer = new FileWriter(Config.merged_mined_annos);
			writer.write(CandidateAnnotation.getHeader()+"\n");
			int n = 0;
			for(CandidateAnnotation canno : distinct.values()){
				n++;
				writer.write(canno.toString()+"\n");
				if(n%1000==0){
					System.out.println(n +" of "+distinct.values().size());
				}
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void filterCandidateAnnotationsFromText() {
		CandidateAnnotations annos = new CandidateAnnotations();
		annos.loadCandidateAnnotations(Config.text_mined_annos);
		FileWriter writer;
		try {
			writer = new FileWriter(Config.text_mined_annos+"filtered");
			writer.write(CandidateAnnotation.getHeader()+"\n");
			int n = 0;
			for(CandidateAnnotation canno : annos.getCannos()){
				n++;
				if(canno.getString_matching_method().contains("text_direct")){
					writer.write(canno.toString()+"\n");
				}
				if(n%1000==0){
					System.out.println(n +" of "+annos.getCannos().size());
				}
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/***
	 * Uses the annotator web service to map the pages linked to by gwiki pages to concepts
	 */
	public static void buildGwikilinks2GOConceptsWithMetamap(){
		//load already processed
		Map<String, List<TextMapping>> page2concepts = loadGwikiLinks2Concepts(true, false, "metamap");
		//find number of NR interwikilinks
		float nrlink_count = 0;
		int limit = 100000000;
		//wiki pages linked from gene wiki pages
		Set<GeneWikiPage> links = GeneWikiUtils.getNRlinks(limit, Config.gwikidir);
		nrlink_count = links.size();
		// 21106
		System.out.println("Total NR linked pages = "+nrlink_count);
		//how many match an ont concept?
		float match_count = 0;
		float n = 1;
		try {		
			for(GeneWikiPage page : links){
				if(!page2concepts.containsKey(page.getTitle())){
					Set<TextMapping> cmap = Wiki2Concept.mapPage2MetamapGO(page);
					if(cmap!=null&&cmap.size()>0){
						match_count++;
						FileWriter writer = new FileWriter(Config.gwlink2concept_mm, true);
						for(TextMapping m : cmap){
							writer.write(page.getTitle()+"\t"+m.toString()+"\n");
						}
						writer.close();
					}
					if(n%10==0){
						System.out.println(n+" "+page.getTitle());
					}
					n++;
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Total NR links = "+nrlink_count);
		System.out.println("Concepts with a match: "+match_count+" ("+match_count/nrlink_count+")");

	}

	/***
	 * Uses the annotator web service to map the pages linked to by gwiki pages to concepts
	 */
	public static void buildGwikilinks2ConceptsWithNCBOAnnotator(){
		//find number of NR interwikilinks
		float nrlink_count = 0;
		int limit = 100000000;		
		
		//see what we already have
		boolean record_evidence = true;
		boolean limit2direct = true;
		Map<String, List<TextMapping>> page_terms = loadGwikiLinks2Concepts(record_evidence, limit2direct, "ncbo");
		
		//wiki pages linked from gene wiki pages
		Set<GeneWikiPage> links = GeneWikiUtils.getNRlinks(limit, Config.gwikidir);
		nrlink_count = links.size();
		// 21106
		System.out.println("Total NR linked pages = "+nrlink_count);
		//how many match an ont concept?
		float match_count = 0;
		float n = 1;
		try {			
			for(GeneWikiPage page : links){
				if(page_terms.containsKey(page.getTitle())){
					n++;
					continue;
				}
				Set<TextMapping> cmap = Wiki2Concept.mapPage2Ncbo(page);
				if(cmap!=null&&cmap.size()>0){
					match_count++;
					FileWriter writer = new FileWriter(Config.gwlink2concept, true);
					for(TextMapping m : cmap){
						writer.write(page.getTitle()+"\t"+m.toString()+"\n");
					}
					writer.close();
				}
				if(n%10==0){
					System.out.println(n+" "+page.getTitle());
				}
				n++;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Total NR links = "+nrlink_count);
		System.out.println("Concepts with a match: "+match_count+" ("+match_count/nrlink_count+")");

	}

	/***
	 * Uses the NCBO annotator to map the text of gwiki pages to concepts
	 */
	public static void getGwikitext2ConceptsWithNCBOAnnotator(boolean allowSynonyms){
		int limit = 1000000;

		//check which files have already been processed
		Set<String> donegeneids = GeneWikiUtils.getDoneGeneIds(Config.text_mined_annos);

		List<GeneWikiPage> pages = GeneWikiUtils.loadSerializedDir(Config.gwikidir, limit);
		//	List<GeneWikiPage> pages = new ArrayList<GeneWikiPage>();
		//	GeneWikiPage test = GeneWikiUtils.deserializeGeneWikiPage(Config.gwikidir+"/55");
		//	pages.add(test);
		int n = 0;
		try {	
			FileWriter writer = new FileWriter(Config.text_mined_annos, true);
			writer.write(CandidateAnnotation.getHeader()+"\n");
			writer.close();
			for(GeneWikiPage page : pages){
				if(donegeneids.contains(page.getNcbi_gene_id())){
					n++;
					continue;
				}
				int ignoreafterindex = page.getPageContent().lastIndexOf("References");
		/*		
		 //should already be done		
		 		page.parseAndSetSentences();
				page.setRefs(new ArrayList<Reference>());
				page.setReferences();
				page.setHeadings();
		*/
				List<Sentence> sentences = page.getSentences();
				if(sentences==null||sentences.size()==0){
					continue;
				}
				for(Sentence s : sentences){
					Heading heading = page.getHeadingByTextIndex(s.getStartIndex());
					String text = s.getPrettyText();
					if(text==null||text.length()<5){
						continue;
					}
					List<NcboAnnotation> annos = AnnotatorClient.ncboAnnotateText(s.getPrettyText(), allowSynonyms, true , true, true, true, false);
					if(annos==null||annos.size()==0){
						continue;
					}
					Set<String> refs = new HashSet<String>();
					List<Reference> r = page.getRefsForSentence(s, ignoreafterindex);
					if(r!=null){
						for(Reference rf : r){
							//only include things with a pmid or a url
							if(rf.getPmid()!=null||rf.getUrl()!=null){
								refs.add(rf.toShortString()+",");
							}
						}
					}
					writer = new FileWriter(Config.text_mined_annos, true);
					for(NcboAnnotation anno : annos){
						CandidateAnnotation canno = new CandidateAnnotation();
						canno.setEntrez_gene_id(page.getNcbi_gene_id());
						canno.setSource_wiki_page_title(page.getTitle());
						canno.setSection_heading(heading.getPrettyText()); 
						//TODO
						canno.setLink_author("unknown");
						//reference and context info						
						canno.setPubmed_references(refs);
						canno.setParagraph_around_link(s.getPrettyText());						

						//data about target
						canno.setTarget_wiki_page_title("");
						TextMapping tm = new TextMapping("ncbo_text", anno.getContext().getMatched_text(), anno);	
						canno.setTarget_accession(tm.getConcept_id());
						canno.setTarget_preferred_term(tm.getConcept_preferred_term());
						canno.setTarget_vocabulary(tm.getOntology_id());
						canno.setVocabulary_branch("");
						//data about String mapping
						canno.setString_matching_method(tm.getProvenance());		
						if(filter(canno)){
							continue;
						}
						writer.write(canno.toString()+"\n");
					}
					writer.close();
				}			
				if(n%10==0){
					System.out.println(n+" "+page.getTitle());
				}
				n++;				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/***
	 * Uses locla metamap to map the text of gwiki pages to concepts
	 */
	public static void getGwikiText2GOWithMetaMap(){
		int limit = 1000000;
		UmlsDb umls = new UmlsDb();

		//check which files have already been processed
		Set<String> donegeneids = GeneWikiUtils.getDoneGeneIds(Config.text_mined_annos_mm);

		List<GeneWikiPage> pages = GeneWikiUtils.loadSerializedDir(Config.gwikidir, limit);
		//	List<GeneWikiPage> pages = new ArrayList<GeneWikiPage>();
		//	GeneWikiPage test = GeneWikiUtils.deserializeGeneWikiPage(Config.gwikidir+"/55");
		//	pages.add(test);
		int n = 0;
		try {	
			FileWriter writer = new FileWriter(Config.text_mined_annos_mm, true);
			writer.write(CandidateAnnotation.getHeader()+"\n");
			writer.close();
			for(GeneWikiPage page : pages){
				if(donegeneids.contains(page.getNcbi_gene_id())){
					n++;
					continue;
				}
				
				Set<String> distinct = new HashSet<String>();
				int ignoreafterindex = page.getPageContent().lastIndexOf("References");
				page.parseAndSetSentences();
				page.setRefs(new ArrayList<Reference>());
				page.setReferences();
				page.setHeadings();

				//work on the text 
				List<Sentence> sentences = page.getSentences();
				if(!(sentences==null||sentences.size()==0)){

					for(Sentence s : sentences){
						Heading heading = page.getHeadingByTextIndex(s.getStartIndex());
						String text = s.getPrettyText();
						if(text==null||text.length()<5){
							continue;
						}
						boolean singleTerm = false;
						List<MMannotation> annos = MetaMap.getCUIsFromText(s.getPrettyText(), "GO", singleTerm, "text");
						if(annos==null||annos.size()==0){
							continue;
						}
						Set<CandidateAnnotation> goids = new HashSet<CandidateAnnotation>();
						Set<String> refs = new HashSet<String>();
						List<Reference> r = page.getRefsForSentence(s, ignoreafterindex);
						if(r!=null){
							for(Reference rf : r){
								//only include things with a pmid or a url
								if(rf.getPmid()!=null||rf.getUrl()!=null){
									refs.add(rf.toShortString()+",");
								}
							}
						}
						writer = new FileWriter(Config.text_mined_annos_mm, true);
						for(MMannotation anno : annos){
							for(String goid : umls.getIdsFromSourceForCUI(anno.getCui(), "GO")){
								if(distinct.add(goid)){
									CandidateAnnotation canno = new CandidateAnnotation();
									canno.setString_matching_method("metamap_text");
									canno.setTarget_accession(goid);
									canno.setTarget_preferred_term(anno.getTermName());
									canno.setEntrez_gene_id(page.getNcbi_gene_id());
									canno.setSource_wiki_page_title(page.getTitle());
									canno.setSection_heading(heading.getPrettyText()); 
									//TODO
									canno.setLink_author("unknown");
									//reference and context info						
									canno.setPubmed_references(refs);
									canno.setParagraph_around_link(s.getPrettyText());						
									//data about target
									canno.setTarget_wiki_page_title("");
									canno.setTarget_vocabulary("GO");
									canno.setVocabulary_branch("");
									if(!filter(canno)){
										writer.write(canno.toString()+"\n");
									}
								}
							}
						}
						writer.close();
					}			
				}
				if(n%10==0){
					System.out.print(n+" "+page.getTitle());
				}else if(n%101==0){
					System.out.println(n+" "+page.getTitle());
				}
				n++;				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Loads the table created by buildGwikilinks2ConceptsWithNCBOAnnotator into a map
	 * key = wiki page title
	 * value = list of Mapped Terms linked to the page
	 * @return
	 */
	public static Map<String, List<TextMapping>> loadGwikiLinks2Concepts(boolean record_evidence, boolean limit2direct, String metaOrNcbo){
		Map<String, List<TextMapping>> page_term = new HashMap<String, List<TextMapping>>();
		BufferedReader f = null;
		try {
			if(metaOrNcbo.equals("ncbo")){
				f = new BufferedReader(new FileReader(Config.gwlink2concept));
			}else if(metaOrNcbo.equals("metamap")){
				f = new BufferedReader(new FileReader(Config.gwlink2concept_mm));
			}
			String line = f.readLine().trim();
			while(line!=null){
				String[] item = line.split("\t");
				if(item!=null&&item.length>1){
					String wikipage = item[0];
					String evidence = item[1];
					String input_term = item[2];
					String concept_id = item[3];
					String concept_preferred_term = item[4];
					String score = item[5];

					if(metaOrNcbo.equals("ncbo")&&limit2direct&&!(evidence.contains("title_direct")||evidence.equals("redirect_direct"))){
						line = f.readLine();
						continue;
					}

					TextMapping t = new TextMapping(input_term, concept_id, concept_preferred_term, concept_id.substring(0,5), "", Double.parseDouble(score));
					if(record_evidence){
						t.setEvidence(evidence);
					}else{
						t.setEvidence("all");
					}
					List<TextMapping> terms = page_term.get(wikipage);
					if(terms==null){
						terms = new ArrayList<TextMapping>();
					}					
					terms.add(t);
					page_term.put(wikipage, terms);
				}
				line = f.readLine();
			}
			f.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return page_term;
	}

	/**
	 * Describe the concepts that match up with the pages that are linked to by articles in the Gene Wiki
	 */
	public static void summarizeMinedConcepts() {
		boolean record_evidence = true;
		boolean limit2direct = true;
		Map<String, List<TextMapping>> page_terms = loadGwikiLinks2Concepts(record_evidence, limit2direct, "metamap");
		//count total number of linked pages with a match
		Set<String> matched_pages_title = new HashSet<String>();
		Set<String> matched_pages_redirect = new HashSet<String>();
		//count pages with multiple matches 
		float n_multi_match = 0;
		//count number of distinct matches for pages to different categories.  
		//Only attend to direct hits (no isa reasoning) 
		float n_go_l0 = 0; float n_pro_l0 = 0; float n_fma_l0 = 0; float n_do_l0 = 0;

		for(Entry<String, List<TextMapping>> page_term : page_terms.entrySet()){
			int n_matches_term = 0;
			for(TextMapping concept : page_term.getValue()){			
				n_matches_term++;
				if(concept.evidence.contains("title")){
					matched_pages_title.add(page_term.getKey());
				}else if(concept.evidence.contains("redirect")){
					matched_pages_redirect.add(page_term.getKey());
				} 
				if(concept.getOntology_id().equals(Ontologies.GO_ONT)||concept.getOntology_id().startsWith("GO")){
					n_go_l0++;
				}else if(concept.getOntology_id().equals(Ontologies.PRO_ONT)){
					n_pro_l0++;
				}else if(concept.getOntology_id().equals(Ontologies.FMA_ONT)){
					n_fma_l0++;
				}else if(concept.getOntology_id().equals(Ontologies.HUMAN_DISEASE_ONT)){
					n_do_l0++;
				} 				
			}
			if(n_matches_term>1){
				n_multi_match++;
			}
		}

		//format output
		String output = "Summary of pages linked to by Gene Wiki pages\n" +
		"GO\tPRO\tFMA\tDO\n";
		output+=n_go_l0+"\t"+n_pro_l0+"\t"+n_fma_l0+"\t"+n_do_l0+"\n";
		output+="\nTotal direct matches on title: "+matched_pages_title.size()+"\n";
		output+="\nTotal direct matches on redirect: "+matched_pages_redirect.size()+"\n";
		output+="\nMultiple direct matches: "+n_multi_match+"\n";
		System.out.println(output);
	}

	public static void writeCandidateAnnotationsFromLinksMetamap() {

		//things to catch
		float avg_links_article = 0; float avg_gomatched_links_article = 0;
		float article_count = 0; float link_count = 0; float link_match_count = 0;
		float avg_gos_gene = 0;
		float articles_with_gene_no_go = 0;
		float articles_with_no_gene_id = 0;

		//read in article_gene map;
		String file = Config.article_gene_map_file;
		String delimiter = "\t";
		String skipprefix = "#";
		boolean reverse = true;
		//could be more than one!
		Map<String, Set<String>> article_geneid = GeneWikiUtils.readMapFile(file, delimiter, skipprefix, reverse);
		System.out.println("read article gene map ");

		//note that there can be multiple identical page->go_acc links with different forms of evidence attached
		boolean limit2direct = true;
		boolean record_evidence = true;
		Map<String, List<TextMapping>> page_terms = loadGwikiLinks2Concepts(record_evidence, limit2direct, "metamap");		

		//load all serialized page objects
		int limit = 10000000;
		List<GeneWikiPage> pages = GeneWikiUtils.loadSerializedDir(Config.gwikidir, limit);
		//		List<GeneWikiPage> pages = new ArrayList<GeneWikiPage>();
		//		GeneWikiPage test = GeneWikiUtils.deserializeGeneWikiPage(Config.gwikidir+"/6469");
		//		pages.add(test);
		System.out.println("read in wikigene files");

		//prepare output
		HashMap<String, CandidateAnnotation> cannolist = new HashMap<String, CandidateAnnotation>();

		Ontologies onts = new Ontologies();

		for(GeneWikiPage page : pages){			
			article_count++;
			List<GeneWikiLink> links = page.getGlinks();
			// # links for this article
			link_count += links.size();
			int ignoreafterindex = page.getPageContent().lastIndexOf("References");
			String title = page.getTitle();

			page.setRefs(new ArrayList<Reference>());
			page.setReferences();
			page.parseAndSetSentences();

			if(article_geneid.get(title) == null){
				articles_with_no_gene_id++;
			}else{	
				for(String geneid : article_geneid.get(title)){
					if(links !=null){
						for(GeneWikiLink glink : links){
							if(filter(page, glink)){
								continue;
							}
							// # links that match any go term
							String maybego = glink.getTarget_page();
							List<TextMapping> matches = page_terms.get(maybego);
							if(matches!=null&&matches.size()>0){
								//check for references attached to the sentence where this link was found
								Set<String> refs = new HashSet<String>();
								int linkstart = glink.getStartIndex();
								Sentence sentence = page.getSentenceByTextIndex(linkstart);

								List<Reference> r = page.getRefsForSentence(sentence, ignoreafterindex);
								if(r!=null){
									for(Reference rf : r){
										//only include things with a pmid or a url
										if(rf.getPmid()!=null||rf.getUrl()!=null){
											refs.add(rf.toShortString()+",");
										}
									}
								}

								for(TextMapping mapping : matches){										
									//see if its new
									CandidateAnnotation ca = cannolist.get(geneid+" "+mapping.getConcept_id());
									if(ca!=null){
										ca.setString_matching_method(ca.getString_matching_method()+"---"+mapping.getProvenance());
										cannolist.put(geneid+" "+mapping.getConcept_id(),ca);
									}else{
										//record candidate annotation
										//data about page source
										CandidateAnnotation canno = new CandidateAnnotation();
										canno.setEntrez_gene_id(geneid);
										canno.setSource_wiki_page_title(title);
										canno.setSection_heading(glink.getSectionHeader());
										//TODO
										canno.setLink_author("unknown");
										//reference and context info						
										canno.setPubmed_references(refs);
										if(sentence!=null){
											canno.setParagraph_around_link(sentence.getPrettyText());						
										}
										//data about target
										canno.setTarget_wiki_page_title(glink.getTarget_page());
										canno.setTarget_accession(mapping.getConcept_id());
										canno.setTarget_preferred_term(mapping.getConcept_preferred_term());
										canno.setTarget_vocabulary(onts.ont_names.get(mapping.getOntology_id()));
										canno.setVocabulary_branch("");
										//data about String mapping
										canno.setString_matching_method(mapping.getProvenance());							
										cannolist.put(geneid+" "+mapping.getConcept_id(),canno);	
									}
								}
							}
						}
					}
				}
			}
		}

		FileWriter writer;
		try {
			writer = new FileWriter(Config.link_mined_annos_mm);

			writer.write(CandidateAnnotation.getHeader()+"\n");
			for(CandidateAnnotation canno : cannolist.values()){
				writer.write(canno.toString()+"\n");
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//summarize
		avg_links_article = link_count/article_count;
		avg_gomatched_links_article = link_match_count/link_count;

		System.out.println("\narticles: "+article_count+" link_count: "+link_count);
		System.out.println("avg_link_article: "+avg_links_article+" avg GOs/gene "+avg_gos_gene/article_count);
		System.out.println("no geneid "+ articles_with_no_gene_id+" articles with gene no go: "+articles_with_gene_no_go+" "+articles_with_gene_no_go/article_count);
	}

	public static void writeCandidateAnnotationsFromLinksNCBO() {

		//things to catch
		float avg_links_article = 0; float avg_gomatched_links_article = 0;
		float article_count = 0; float link_count = 0; float link_match_count = 0;
		float avg_gos_gene = 0;
		float articles_with_gene_no_go = 0;
		float articles_with_no_gene_id = 0;

		//read in article_gene map;
		String file = Config.article_gene_map_file;
		String delimiter = "\t";
		String skipprefix = "#";
		boolean reverse = true;
		//could be more than one!
		Map<String, Set<String>> article_geneid = GeneWikiUtils.readMapFile(file, delimiter, skipprefix, reverse);
		System.out.println("read article gene map ");

		//note that there can be multiple identical page->go_acc links with different forms of evidence attached
		boolean limit2direct = true;
		boolean record_evidence = true;
		Map<String, List<TextMapping>> page_terms = loadGwikiLinks2Concepts(record_evidence, limit2direct, "ncbo");		

		//load all serialized page objects
		int limit = 10000000;
		List<GeneWikiPage> pages = GeneWikiUtils.loadSerializedDir(Config.gwikidir, limit);
		//		List<GeneWikiPage> pages = new ArrayList<GeneWikiPage>();
		//		GeneWikiPage test = GeneWikiUtils.deserializeGeneWikiPage(Config.gwikidir+"/6469");
		//		pages.add(test);
		System.out.println("read in wikigene files");

		//prepare output
		HashMap<String, CandidateAnnotation> cannolist = new HashMap<String, CandidateAnnotation>();

		Ontologies onts = new Ontologies();

		for(GeneWikiPage page : pages){			
			article_count++;
			List<GeneWikiLink> links = page.getGlinks();
			// # links for this article
			link_count += links.size();
			int ignoreafterindex = page.getPageContent().lastIndexOf("References");
			String title = page.getTitle();

			page.setRefs(new ArrayList<Reference>());
			page.setReferences();
			page.parseAndSetSentences();

			if(article_geneid.get(title) == null){
				articles_with_no_gene_id++;
			}else{	
				for(String geneid : article_geneid.get(title)){
					if(links !=null){
						for(GeneWikiLink glink : links){
							if(filter(page, glink)){
								continue;
							}
							// # links that match any go term
							String maybego = glink.getTarget_page();
							List<TextMapping> matches = page_terms.get(maybego);
							if(matches!=null&&matches.size()>0){
								//check for references attached to the sentence where this link was found
								Set<String> refs = new HashSet<String>();
								int linkstart = glink.getStartIndex();
								Sentence sentence = page.getSentenceByTextIndex(linkstart);

								List<Reference> r = page.getRefsForSentence(sentence, ignoreafterindex);
								if(r!=null){
									for(Reference rf : r){
										//only include things with a pmid or a url
										if(rf.getPmid()!=null||rf.getUrl()!=null){
											refs.add(rf.toShortString()+",");
										}
									}
								}

								for(TextMapping mapping : matches){										
									//see if its new
									CandidateAnnotation ca = cannolist.get(geneid+" "+mapping.getConcept_id());
									if(ca!=null){
										ca.setString_matching_method(ca.getString_matching_method()+"---"+mapping.getProvenance());
										cannolist.put(geneid+" "+mapping.getConcept_id(),ca);
									}else{
										//record candidate annotation
										//data about page source
										CandidateAnnotation canno = new CandidateAnnotation();
										canno.setEntrez_gene_id(geneid);
										canno.setSource_wiki_page_title(title);
										canno.setSection_heading(glink.getSectionHeader());
										//TODO
										canno.setLink_author("unknown");
										//reference and context info						
										canno.setPubmed_references(refs);
										if(sentence!=null){
											canno.setParagraph_around_link(sentence.getPrettyText());						
										}
										//data about target
										canno.setTarget_wiki_page_title(glink.getTarget_page());
										canno.setTarget_accession(mapping.getConcept_id());
										canno.setTarget_preferred_term(mapping.getConcept_preferred_term());
										canno.setTarget_vocabulary(onts.ont_names.get(mapping.getOntology_id()));
										canno.setVocabulary_branch("");
										//data about String mapping
										canno.setString_matching_method(mapping.getProvenance());							
										cannolist.put(geneid+" "+mapping.getConcept_id(),canno);	
									}
								}
							}
						}
					}
				}
			}
		}

		FileWriter writer;
		try {
			writer = new FileWriter(Config.link_mined_annos);

			writer.write(CandidateAnnotation.getHeader()+"\n");
			for(CandidateAnnotation canno : cannolist.values()){
				writer.write(canno.toString()+"\n");
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//summarize
		avg_links_article = link_count/article_count;
		avg_gomatched_links_article = link_match_count/link_count;

		System.out.println("\narticles: "+article_count+" link_count: "+link_count);
		System.out.println("avg_link_article: "+avg_links_article+" avg GOs/gene "+avg_gos_gene/article_count);
		System.out.println("no geneid "+ articles_with_no_gene_id+" articles with gene no go: "+articles_with_gene_no_go+" "+articles_with_gene_no_go/article_count);
	}

	/**
	 * Return true if we should ignore this link
	 * @param glink
	 * @return
	 */
	public static boolean filter(GeneWikiPage page, GeneWikiLink glink){
		//ignore it if its just protein or gene
		if(glink.getTarget_page().equals("protein")||
				glink.getTarget_page().equals("Protein-protein_interaction")||
				glink.getTarget_page().equals("gene")){
			return true;
		}
		//ignore it if its embedded in a reference
		if(page.getReferenceByTextIndex(glink.getStartIndex())!=null){
			return true;
		}
		//ignore it if its in the interactions section
		if(glink.getSectionHeader().equals("Interactions")){
			return true;
		}
		return false;
	}

	/**
	 * Return true if we should ignore this link
	 * @param glink
	 * @return
	 */
	public static boolean filter(CandidateAnnotation canno){
		//ignore it if its just protein or gene
		if(canno.getTarget_preferred_term().equalsIgnoreCase("protein")||
				canno.getTarget_preferred_term().equalsIgnoreCase("gene")){
			return true;
		}
		//ignore it if its in the interactions section
		if(canno.getSection_heading().equals("Interactions")){
			return true;
		}
		return false;
	}


}
