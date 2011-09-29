package org.scripps.ontologies.go;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.genewiki.Config;
import org.genewiki.GeneWikiLink;
import org.genewiki.GeneWikiPage;
import org.genewiki.GeneWikiUtils;
import org.genewiki.Reference;
import org.genewiki.Sentence;
import org.genewiki.mapping.annotations.CandidateAnnotation;
import org.genewiki.mapping.annotations.CandidateAnnotations;
import org.scripps.nlp.ncbo.Ontologies;
import org.scripps.nlp.ncbo.web.AnnotatorClient;
import org.scripps.nlp.ncbo.web.NcboAnnotation;
import org.scripps.nlp.umls.UmlsRelationship;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;


public class GOmapper {

	//configuration for the local, auto mapping (first version).
	//all redirects and all synonym types
	public static int LOOSEST = 0;
	//exact matches plus redirects - everything except 'related' and 'broader' synonym types
	public static int SMARTEST = 1;
	//only exact matches from go xrefs (include redirects), title to preferred term or exact synonym,
	public static int TIGHTEST = 2;
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		//This constructs a table linking wiki page titles to go terms
		//	GOmapper.buildGwiki2go();
		//this produces a summary of the mapping success
		GOmapper.summarizeGwiki2go();
		//this generates a file that shows whether or not the annotations match go etc.
		//		String gw2gocompare = "C:\\Users\\bgood\\data\\genewiki\\gwiki2goannos_(all_with_panther_ortho)";
		//		try {
		//			GOmapper.compareLinksToGOannotation4(gw2gocompare);
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		//		//this generates some stats on how the candidate annotations compare to known annotations
		//		GOmapper.describeCandidateAnnotations(gw2gocompare);

	}

	public static void compareLinksToGOannotation4(String outfile) throws IOException{

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

		//read in gene to go ~term map
		file = Config.gene_go_file;
		Map<String, Set<GOterm>> geneid_go = GeneWikiUtils.readGene2GO(file, delimiter, skipprefix);
		System.out.println("read geneid_go map");

		Map<String, Set<GOterm>> panther_go = GeneWikiUtils.readGene2GO(Config.gene_panther_go_file, delimiter, skipprefix);
		System.out.println("read panther geneid_go map");

		//add ancestors to geneid_go map and panther_go map
		GOowl gol = new GOowl();
		gol.initFromFileRDFS(true);
		//go
		Map<String, Set<GOterm>> tmp_geneid_go = new HashMap<String, Set<GOterm>>();
		for(Entry<String, Set<GOterm>> entry : geneid_go.entrySet()){
			String geneid = entry.getKey();
			Set<GOterm> p = new HashSet<GOterm>();
			for(GOterm goterm : entry.getValue()){
				p.add(goterm);
				Set<GOterm> parents = gol.getSupers(goterm);
				if(parents!=null){
					for(GOterm parent : parents){
						if(parent != goterm){
							p.add(parent);
						}
					}
				}
			}
			tmp_geneid_go.put(geneid, p);
		}
		geneid_go = tmp_geneid_go;
		//panther

		Map<String, Set<GOterm>> tmp_panther_go = new HashMap<String, Set<GOterm>>();
		for(Entry<String, Set<GOterm>> entry : panther_go.entrySet()){
			String geneid = entry.getKey();
			Set<GOterm> p = new HashSet<GOterm>();
			for(GOterm goterm : entry.getValue()){
				p.add(goterm);
				Set<GOterm> parents = gol.getSupers(goterm);
				if(parents!=null){
					for(GOterm parent : parents){
						if(parent != goterm){
							p.add(parent);
						}
					}
				}
			}
			tmp_panther_go.put(geneid, p);
		}
		panther_go = tmp_panther_go;

		gol.close();
		//read in page to go map
		//note that there can be multiple identical page->go_acc links with different forms of evidence attached
		Map<String, HashSet<GOterm>> page_gos = loadGwiki2go(true);		

		//load all serialized page objects
		int limit = 100000;
		Map<String, GeneWikiPage> pages = GeneWikiUtils.loadSerializedDir(Config.gwikidir, limit);
		System.out.println("read in wikigene files");

		//prepare output
		List<CandidateAnnotation> cannolist = new ArrayList<CandidateAnnotation>();
		FileWriter writer = new FileWriter(outfile);
		writer.write(CandidateAnnotation.getHeader());


		for(GeneWikiPage page : pages.values()){
			article_count++;
			List<GeneWikiLink> links = page.getGlinks();
			// # links for this article
			link_count += links.size();

			String title = page.getTitle();
			if(article_geneid.get(title) == null){
				articles_with_no_gene_id++;
			}else{	

				for(String geneid : article_geneid.get(title)){
					Set<GOterm> goAs = geneid_go.get(geneid);
					if(goAs==null){
						articles_with_gene_no_go++;
					}else{
						avg_gos_gene+=goAs.size();
					}
					if(links !=null){
						for(GeneWikiLink glink : links){

							if(glink.getTarget_page().equals("protein")){
								continue;
							}
							// # links that match any go term
							String maybego = glink.getTarget_page();
							Set<GOterm> maybegos = page_gos.get(maybego);
							if(maybegos!=null){
								for(GOterm matchgo : maybegos){										
									//record candidate annotation
									//data about page source
									CandidateAnnotation canno = new CandidateAnnotation();
									canno.setEntrez_gene_id(geneid);
									canno.setSource_wiki_page_title(title);
									if(glink.getParagraph()!=null){
										canno.setParagraph_around_link(glink.getParagraph().replace("\t", " "));
									}
									canno.setSection_heading(glink.getSectionHeader());
									//TODO
									canno.setLink_author("unknown");
									//TODO
									canno.setPubmed_references(null);
									//data about target
									canno.setTarget_wiki_page_title(glink.getTarget_page());
									canno.setTarget_accession(matchgo.getAccession());
									canno.setTarget_preferred_term(matchgo.getTerm());
									canno.setTarget_vocabulary("GO");
									canno.setVocabulary_branch(matchgo.getRoot());
									//data about String mapping
									canno.setString_matching_method(matchgo.getEvidence());

									// # links that match a go term from the GOA database
									if(goAs !=null){
										for(GOterm go : goAs){
											if(matchgo.getAccession().equals(go.getAccession())){
												if(go.isInferred_parent()){
													canno.getEvidence().setMatches_parent_of_existing_annotation(true);
												}else{
													canno.getEvidence().setMatches_existing_annotation_directly(true);
												}
												break;
											}
										}
									}
									// links that match a go term inferred via panther database
									Set<GOterm> goPAs = panther_go.get(geneid);
									if(goPAs !=null){
										for(GOterm go : goPAs){
											if(matchgo.getAccession().equals(go.getAccession())){
												if(go.isInferred_parent()){
													canno.getEvidence().setMatches_parent_of_panther_go(true);
												}else{
													canno.getEvidence().setMatches_panther_go_directly(true);
												}
												break;
											}
										}
									}

									writer.write(canno.toString()+"\n");
									cannolist.add(canno);
								}
							}
						}
					}
				}
			}
		}
		writer.close();
		//summarize
		avg_links_article = link_count/article_count;
		avg_gomatched_links_article = link_match_count/link_count;

		System.out.println("\narticles: "+article_count+" link_count: "+link_count);
		System.out.println("avg_link_article: "+avg_links_article+" avg GOs/gene "+avg_gos_gene/article_count);
		System.out.println("no geneid "+ articles_with_no_gene_id+" articles with gene no go: "+articles_with_gene_no_go+" "+articles_with_gene_no_go/article_count);

	}

	public static void describeCandidateAnnotations(String file){
		CandidateAnnotations cannolist = new CandidateAnnotations();
		cannolist.loadCandidateAnnotations(file);
		List<CandidateAnnotation> cannos = cannolist.getCannos();
		System.out.println("\nMapping stringency\tinference\ttotal\tnovel\trediscovered\tnew-BP\tnew-MF\tnew-CC\told-BP\told-MF\told-CC");
		for(int i =0; i<3; i++){
			Set<String> unique_links = new HashSet<String>();
			float wlinks_to_go = 0;
			float wlinks_togo_old_noR = 0;
			float wlinks_togo_new_noR = 0;
			float wlinks_togo_old_withR = 0;
			float wlinks_togo_new_withR = 0;
			float panther_direct_match = 0;
			float panther_parent_match = 0;
			float panther_match_no_go_match = 0;
			float no_match = 0;
			float nmf_old = 0; float nbp_old = 0; float ncc_old = 0;
			float nmf_new = 0; float nbp_new = 0; float ncc_new = 0;
			float nmf_old_R = 0; float nbp_old_R  = 0; float ncc_old_R  = 0;
			float nmf_new_R  = 0; float nbp_new_R  = 0; float ncc_new_R  = 0;

			String mapping = "loose";
			if(i==1) mapping = "smart";
			else if(i==2) mapping = "tight";
			for(CandidateAnnotation canno : cannos){
				boolean skip = false;
				if((mapping.equals("tight"))&&
						(canno.getString_matching_method().contains("related")||
								canno.getString_matching_method().contains("broader")||
								canno.getString_matching_method().contains("redirect"))){
					skip = true;
				}else if((mapping.equals("smart"))&&
						(canno.getString_matching_method().contains("related")||
								canno.getString_matching_method().contains("broader"))){
					skip = true;
				}
				if(!skip){
					if(unique_links.add(canno.getEntrez_gene_id()+canno.getTarget_accession())){
						wlinks_to_go++;
						//panther
						if(canno.getEvidence().isMatches_panther_go_directly()){
							panther_direct_match++;
						}
						if(canno.getEvidence().isMatches_parent_of_panther_go()){
							panther_parent_match++;
						}
						if(canno.getEvidence().isMatches_panther_go_directly()||canno.getEvidence().isMatches_parent_of_panther_go()){
							if(!(canno.getEvidence().isMatches_existing_annotation_directly()||
									canno.getEvidence().isMatches_parent_of_existing_annotation())){
								panther_match_no_go_match++;
							}
						}
						//none
						if(!(canno.getEvidence().isMatches_existing_annotation_directly()||
								canno.getEvidence().isMatches_panther_go_directly()||
								canno.getEvidence().isMatches_parent_of_existing_annotation()||
								canno.getEvidence().isMatches_parent_of_panther_go())){
							no_match++;
						}

						//no ancestors
						if(canno.getEvidence().isMatches_existing_annotation_directly()){
							wlinks_togo_old_noR++;
							if(canno.getVocabulary_branch().equals("molecular_function")){
								nmf_old++;
							}else if(canno.getVocabulary_branch().equals("biological_process")){
								nbp_old++;
							}else if(canno.getVocabulary_branch().equals("cellular_component")){
								ncc_old++;
							}
						}else{
							wlinks_togo_new_noR++;
							if(canno.getVocabulary_branch().equals("molecular_function")){
								nmf_new++;
							}else if(canno.getVocabulary_branch().equals("biological_process")){
								nbp_new++;
							}else if(canno.getVocabulary_branch().equals("cellular_component")){
								ncc_new++;
							}
						}
						//with ancestors
						if(canno.getEvidence().isMatches_existing_annotation_directly()||canno.getEvidence().isMatches_parent_of_existing_annotation()){
							wlinks_togo_old_withR++;
							if(canno.getVocabulary_branch().equals("molecular_function")){
								nmf_old_R++;
							}else if(canno.getVocabulary_branch().equals("biological_process")){
								nbp_old_R++;
							}else if(canno.getVocabulary_branch().equals("cellular_component")){
								ncc_old_R++;
							}
						}else{
							wlinks_togo_new_withR++;
							if(canno.getVocabulary_branch().equals("molecular_function")){
								nmf_new_R++;
							}else if(canno.getVocabulary_branch().equals("biological_process")){
								nbp_new_R++;
							}else if(canno.getVocabulary_branch().equals("cellular_component")){
								ncc_new_R++;
							}
						}
					}
				}
			}

			System.out.println("\n\n"+mapping+"\tno parents\t"+wlinks_to_go+"\t"+wlinks_togo_new_noR+"\t"+wlinks_togo_old_noR+"\t"+nbp_new/wlinks_togo_new_noR+"\t"+nmf_new/wlinks_togo_new_noR+"\t"+ncc_new/wlinks_togo_new_noR+"\t"+nbp_old/wlinks_togo_old_noR+"\t"+nmf_old/wlinks_togo_old_noR+"\t"+ncc_old/wlinks_togo_old_noR);
			System.out.println(mapping+"\twith parents\t"+wlinks_to_go+"\t"+wlinks_togo_new_withR+"\t"+wlinks_togo_old_withR+"\t"+nbp_new_R/wlinks_togo_new_withR+"\t"+nmf_new_R/wlinks_togo_new_withR+"\t"+ncc_new_R/wlinks_togo_new_withR+"\t"+nbp_old_R/wlinks_togo_old_withR+"\t"+nmf_old_R/wlinks_togo_old_withR+"\t"+ncc_old_R/wlinks_togo_old_withR);

			System.out.println(mapping+"\tpanther_direct\t"+panther_direct_match);
			System.out.println(mapping+"\tpanther_parent\t"+panther_parent_match);
			System.out.println(mapping+"\tpanther_no_gene2go\t"+panther_match_no_go_match);
			System.out.println(mapping+"\tno_match\t"+no_match+"\t"+no_match/wlinks_to_go);


		}
	}



	/**
	 * Starts with go xrefs, then goes and looks for exact text matches to the GO, 
	 * If match not found for title, looks for matches to redirects
	 * Records GO match for each wiki page and evidence used for match 	
	 * output table structure = {Evidence, page title, GO_acc, GO root, GO_preferred_term}
	 */
	public static void buildGwiki2go(){
		//find number of NR interwikilinks
		float nrlink_count = 0;
		int limit = 1000000;
		//wiki pages linked from gene wiki pages
		Set<GeneWikiPage> links = GeneWikiUtils.getNRlinks(limit, Config.gwikidir);
		nrlink_count = links.size();
		// 21106
		System.out.println("Total NR links = "+nrlink_count);

		//how many match the GO?
		float go_match_count = 0;
		//which categories?
		float go_bp = 0; float go_mf = 0; float go_cc = 0; float other = 0;

		//get all GO terms and the xrefs to wikipedia
		List<GOterm> goterms = GeneWikiUtils.getGOTermsFromDbFiles(Config.go_dbdir);
		System.out.println("Got terms ");
		GOowl gowl = new GOowl();
		gowl.initFromFile(false);
		System.out.println("loaded GO owl for xref mapping");
		Map<String, GOterm> page_xref_2go = new HashMap<String, GOterm>();
		for(GOterm goterm : goterms){
			List<String> wikis = gowl.getGoTermWikiXref(gowl.makeGoUri(goterm.getAccession().substring(3)));
			if(wikis!=null&&wikis.size()>0){
				goterm.setWikixrefs(wikis);
				for(String wiki : wikis){
					page_xref_2go.put(GeneWikiUtils.normalize(wiki), goterm);
				}
			}
			page_xref_2go.put(goterm.getAccession(), goterm);
		}
		gowl.close();

		int n = 0;

		try {
			FileWriter writer = new FileWriter(Config.gwlink2go);

			for(GeneWikiPage page : links){
				String pagetitle = GeneWikiUtils.normalize(page.getTitle());
				n++;
				//allow for multiple matches
				List<GOterm> matches = new ArrayList<GOterm>();
				//look up a GO match from the xref table
				GOterm match = page_xref_2go.get(pagetitle);
				if(match!=null){
					match.setEvidence("xref_title");
					matches.add(match);
				}	
				//check for the redirects for the page
				for(String title : page.getWikisynset()){
					match = page_xref_2go.get(title);
					if(match!=null){
						match.setEvidence("xref_redirect");				
						matches.add(match);
					}
				}

				//now look for exact text matches
				List<GOterm> tmatches = GeneWikiUtils.allPresentInGOtermList(page, goterms);
				if(tmatches!=null){
					matches.addAll(tmatches);
				}

				match = null;//to avoid confusion
				//now store the matches

				if(matches!=null&&matches.size()>0){
					for(GOterm matched : matches){
						writer.write(matched.getEvidence()+"\t"+page.getTitle()+"\t"+matched.getAccession()+"\t"+matched.getRoot()+"\t"+matched.getTerm()+"\n");
						go_match_count++;
						if(matched.getRoot()==null){
							other++;
						}else if(matched.getRoot().equals("biological_process")){
							go_bp++;
						}else if(matched.getRoot().equals("molecular_function")){
							go_mf++;
						}else if(matched.getRoot().equals("cellular_component")){
							go_cc++;
						}else{
							other++;
						}
					}
				}
				if(n%100==0){
					System.out.println(n+" of "+nrlink_count+" "+page.getTitle());
				}
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Total NR links = "+nrlink_count);
		System.out.println("GO matches: "+go_match_count+" bp: "+go_bp+" mf:"+go_mf+" cc:"+go_cc+" other:"+other);

	}


	public static List<GOterm> getGoFromAnnos(List<NcboAnnotation> annos){
		List<GOterm> gos = new ArrayList<GOterm>();
		if(annos==null){
			return null;
		}
		for(NcboAnnotation anno : annos){
			if(anno.getConcept().getLocalOntologyId().equals(Ontologies.GO_ONT)){

			}
		}
		return gos;
	}

	/**
	 * Loads the table created by buildGwiki2go into a map
	 * key = wiki page title
	 * value = set of GOterms linked to the page
	 * identity for the set determined by combination of go accession and evidence
	 * @return
	 */
	public static Map<String, HashSet<GOterm>> loadGwiki2go(boolean record_evidence){
		GOowl gol = new GOowl();
		gol.initFromFile(false);
		Map<String, HashSet<GOterm>> page_term = new HashMap<String, HashSet<GOterm>>();
		BufferedReader f;
		int pairs = 0;
		try {
			f = new BufferedReader(new FileReader(Config.gwlink2go+"_ncbo"));
			String line = f.readLine().trim();
			while(line!=null){
				String[] item = line.split("\t");
				if(item!=null&&item.length>1){
					String evidence = item[0];
					String wikipage = item[1];
					String acc = item[2];
					String root = item[3];
					String goterm = item[4];
					GOterm t = new GOterm(null, acc, root, goterm, true);
					if(record_evidence){
						t.setEvidence(evidence);
					}else{
						t.setEvidence("all");
					}
					HashSet<GOterm> terms = page_term.get(wikipage);
					if(terms==null){
						terms = new HashSet<GOterm>();
					}					
					//is the term obsolete?
					Set<GOterm> repls = gol.getGoTermObsolete(t);
					if(repls==null){					
						terms.add(t);
					}else{
						terms.addAll(repls);
					}
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
		gol.close();
		return page_term;
	}

	public static void summarizeGwiki2go(){
		int total_links = 0;
		boolean by_evidence = true;
		Map<String, HashSet<GOterm>> page_terms = loadGwiki2go(by_evidence);
		//read evidence types
		Set<String> etypes = new HashSet<String>();
		for(Entry<String, HashSet<GOterm>> entry : page_terms.entrySet()){
			HashSet<GOterm> terms = entry.getValue();
			for(GOterm term : terms){
				etypes.add(term.getEvidence());
				total_links++;
			}
		}
		System.out.println("total links "+total_links);
		//collect stats for each
		String report = "evidence\tn_mapped\ttotal_distinct-page-go-evidence\tbp/n_mapped\tmf/n_mapped\tcc/n_mapped\t\n";
		report += summarizeGwiki2goByEvidenceType("all", page_terms);
		System.out.println(report);
		for(String evidence : etypes){
			String row = summarizeGwiki2goByEvidenceType(evidence, page_terms);
			report+= row;
		}
		System.out.println("\n"+report);

	}

	public static String summarizeGwiki2goByEvidenceType(String evidence, Map<String, HashSet<GOterm>> page_terms){
		String summary = "";
		float n_pages_in_map = page_terms.size(); float n_mapped = 0;
		float bp = 0; float mf = 0; float cc = 0; float other = 0;
		float total_possibly_redundant = 0;
		for(Entry<String, HashSet<GOterm>> entry : page_terms.entrySet()){
			String page = entry.getKey();
			HashSet<GOterm> terms = entry.getValue();
			boolean mapped = false;
			//since one page may match many terms, keep track of the fraction of each type
			float pct_bp = 0; float pct_mf = 0; float pct_cc = 0; float total_match = 0;
			for(GOterm term : terms){

				if(evidence.equals("all")||term.getEvidence().equals(evidence)){
					mapped = true;
					total_match++;
					total_possibly_redundant++;
					if(term.getRoot()==null){
						other++;
					}else if(term.getRoot().equals("biological_process")){
						pct_bp++;
					}else if(term.getRoot().equals("molecular_function")){
						pct_mf++;
					}else if(term.getRoot().equals("cellular_component")){
						pct_cc++;
					}else{
						other++;
					}
				}
			}

			if(mapped){
				pct_bp = pct_bp/total_match; pct_mf = pct_mf/total_match; pct_cc = pct_cc/total_match; 
				if(pct_bp+pct_mf+pct_cc!=1){
					System.out.println("problem");
				}

				bp+=pct_bp; mf+=pct_mf; cc+=pct_cc;
				n_mapped++;
			}
		}
		if(evidence==null){
			evidence = "all";
		}		
		summary = evidence+"\t"+n_mapped+"\t"+total_possibly_redundant+"\t"+bp/n_mapped+"\t"+
		mf/n_mapped+"\t"+cc/n_mapped+"\t\n";
		return summary;
	}


	public static void writeCandidateAnnotations(){

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
		Map<String, HashSet<GOterm>> page_gos = loadGwiki2go(true);		

		//load all serialized page objects
		int limit = 10000000;
		Map<String, GeneWikiPage> pages = GeneWikiUtils.loadSerializedDir(Config.gwikidir, limit);
		//		List<GeneWikiPage> pages = new ArrayList<GeneWikiPage>();
		//		GeneWikiPage test = GeneWikiUtils.deserializeGeneWikiPage(Config.gwikidir+"/10312");
		//		pages.add(test);
		System.out.println("read in wikigene files");

		//prepare output
		HashMap<String, CandidateAnnotation> cannolist = new HashMap<String, CandidateAnnotation>();

		for(GeneWikiPage page : pages.values()){			
			article_count++;
			List<GeneWikiLink> links = page.getGlinks();
			// # links for this article
			link_count += links.size();
			//make sure references are set
			page.setRefs( new ArrayList<Reference>());
			page.setReferences();
			page.parseAndSetSentences();

			int ignoreafterindex = page.getPageContent().lastIndexOf("References");

			String title = page.getTitle();
			if(article_geneid.get(title) == null){
				articles_with_no_gene_id++;
			}else{	
				for(String geneid : article_geneid.get(title)){
					if(links !=null){
						for(GeneWikiLink glink : links){
							if(glink.getTarget_page().equals("protein")){
								continue;
							}
							// # links that match any go term
							String maybego = glink.getTarget_page();
							Set<GOterm> maybegos = page_gos.get(maybego);
							if(maybegos!=null){
								//check for references attached to the sentence where this link was found
								Set<String> refs = new HashSet<String>();
								int linkstart = glink.getStartIndex();
								Sentence sentence = page.getSentenceByTextIndex(linkstart);

								if(sentence == null){
									System.out.println(
											page.getTitle()+" "
											+page.getNcbi_gene_id()+" "+
											glink.getStartIndex()+" "+
											glink.getTarget_page()+" ");
									//	"\nsentence missing:"+"linkstart,linkstart+10:\n"
									//	+page.getPageContent().substring(linkstart,linkstart+10));
								}

								//								if(glink.getTarget_page().getTitle().equals("antibodies")){
								//									System.out.println("starts at "+glink.getStartIndex());
								//									System.out.println(glink.getParagraph());
								//									System.out.println("sentence! "+sentence.getText());
								//								}	

								List<Reference> r = page.getRefsForSentence(sentence, ignoreafterindex);
								if(r!=null){
									for(Reference rf : r){
										//only include things with a pmid or a url
										if(rf.getPmid()!=null||rf.getUrl()!=null){
											refs.add(rf.toShortString()+",");
										}
									}
								}

								for(GOterm matchgo : maybegos){										
									//see if its new
									CandidateAnnotation ca = cannolist.get(geneid+" "+matchgo.getAccession());
									if(ca!=null){
										ca.setString_matching_method(ca.getString_matching_method()+" "+matchgo.getEvidence());
										cannolist.put(geneid+" "+matchgo.getAccession(),ca);
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
										canno.setTarget_accession(matchgo.getAccession());
										canno.setTarget_preferred_term(matchgo.getTerm());
										canno.setTarget_vocabulary("GO");
										canno.setVocabulary_branch(matchgo.getRoot());
										//data about String mapping
										canno.setString_matching_method(matchgo.getEvidence());							
										cannolist.put(geneid+" "+matchgo.getAccession(),canno);	
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

			writer.write(CandidateAnnotation.getHeader());
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

}