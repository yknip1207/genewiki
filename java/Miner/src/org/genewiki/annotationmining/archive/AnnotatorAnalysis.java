package org.genewiki.annotationmining.archive;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.genewiki.GeneWikiLink;
import org.genewiki.GeneWikiPage;
import org.genewiki.GeneWikiUtils;
import org.genewiki.annotationmining.Config;
import org.genewiki.annotationmining.annotations.CandidateAnnotation;
import org.genewiki.annotationmining.annotations.CandidateAnnotations;
import org.scripps.datasources.GoGeneCrawler;
import org.scripps.nlp.ncbo.uima.ParseUIMA;
import org.scripps.nlp.ncbo.uima.UimaAnnotation;
import org.scripps.ontologies.go.GOmapper;
import org.scripps.ontologies.go.GOowl;
import org.scripps.ontologies.go.GOterm;
import org.scripps.util.BioInfoUtil;
import org.scripps.util.MapComparison;

import com.hp.hpl.jena.ontology.OntClass;

public class AnnotatorAnalysis {

	public static String anno2gofile = "/Users/bgood/data/genewiki/NCBO-output/ncbo-go-anno-output.txt";
	public static String linkedannos = "/Users/bgood/data/genewiki/gwiki2goannos_(all_with_panther_ortho)";
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Map<String, HashSet<GOterm>> page_gos = loadAnno2GO();		
		Map<String, Set<GOterm>> geneid_gos = GeneWikiUtils.convertWikiTitlesToGeneIds(page_gos);
		System.out.println(geneid_gos.get("6873"));
		//System.out.println(geneid_gos.get("6872"));
		///////////////////////////////////
		//compare to annos dug out of links
		///////////////////////////////////
	/*	boolean r = false;
		 MapComparison ac = compareTextMined2LinkMined(geneid_gos, r);
		System.out.println("no reasoning\n"+ac.toString());
		 r = true;
		ac = compareTextMined2LinkMined(geneid_gos, r);
		System.out.println("with reasoning\n"+ac.toString());
	*/
		
		///////////////////////////////////
		//compare text mined to go
		///////////////////////////////////
	/*	
		compareAnnotations2GO(geneid_gos);
	*/
		///////////////////////////////////
		//compare merged to go
		///////////////////////////////////		
	/*	geneid_gos = mergeTextMined2LinkMined(geneid_gos, false);
		System.out.println("after merge "+geneid_gos.get("6873"));
		compareAnnotations2GO(geneid_gos);
	*/
		///////////////////////////////////
		//compare merged annos to gene2go
		///////////////////////////////////
	/*	geneid_gos = mergeTextMined2LinkMined(geneid_gos, false);
		Map<String, Set<GOterm>> genegos = GoGeneCrawler.getGoGeneData();
		boolean reasoning_on = false;
		MapComparison ac = BioInfoUtil.compareGOMaps(geneid_gos, genegos, reasoning_on);
		System.out.println("no reasoning\n"+ac.toString());
		reasoning_on = true;
		ac = BioInfoUtil.compareGOMaps(geneid_gos, genegos, reasoning_on);
		System.out.println("with reasoning\n"+ac.toString());
	*/	
		///////////////////////////////////
		//Generate merged candidate annotation list for downstream processing and review
		///////////////////////////////////	
	/*	String cannofile = "/Users/bgood/Desktop/MergedAnnosNRwithkids.txt";
		geneid_gos = mergeTextMined2LinkMined(geneid_gos, false);
		boolean addChildren = true;
	*/	
		boolean addChildren = false;
		String cannofile = "/Users/bgood/data/genewiki/NCBO-output/MinedGOAnnos.txt";
		getCandidateAnnotationsWithGoaAndPanther(geneid_gos,cannofile, addChildren);
	
	}


	public static List<CandidateAnnotation> getCandidateAnnotationsWithGoaAndPanther(
			Map<String, Set<GOterm>> pred_gos, String outfile, 	boolean addchildren){
		
		List<CandidateAnnotation> cannos = new ArrayList<CandidateAnnotation>();
		//load goa and panther maps
		//read in gene to go ~term map
		Map<String, Set<GOterm>> geneid_go = GeneWikiUtils.readGene2GO(Config.gene_go_file, "\t", "#");
		System.out.println("read geneid_go map");

		Map<String, Set<GOterm>> panther_go = GeneWikiUtils.readGene2GO(Config.gene_panther_go_file, "\t", "#");
		System.out.println("read panther geneid_go map");

		//add ancestors to geneid_go map and panther_go map
		//go
		geneid_go = BioInfoUtil.expandGoMap(geneid_go, null, false);
		//panther
		panther_go = BioInfoUtil.expandGoMap(panther_go, null, false);

		System.out.println("Got expanded maps");
		int c = 0;
		for(String geneid : pred_gos.keySet()){
			c++;
			if(c%100==0){
				System.out.print(c+" ");
			}
			if(c%1000==0){
				System.out.println();
			}
			Set<GOterm> maybegos = pred_gos.get(geneid);
			if(maybegos!=null){
				for(GOterm matchgo : maybegos){										
					//record candidate annotation
					//data about page source
					CandidateAnnotation canno = new CandidateAnnotation();
					canno.setEntrez_gene_id(geneid);

					//data about target
					canno.setTarget_accession(matchgo.getAccession());
					canno.setTarget_preferred_term(matchgo.getTerm());
					canno.setTarget_vocabulary("GO");
					canno.setVocabulary_branch(matchgo.getRoot());
					//data about String mapping
					canno.setString_matching_method(matchgo.getEvidence());

					// # links that match a go term from the GOA database
					Set<GOterm> goAs = geneid_go.get(geneid);
					if(goAs !=null){
						for(GOterm go : goAs){
							if(matchgo.getAccession().equals(go.getAccession())){
								if(go.isInferred_parent()){
									canno.getEvidence().setMatches_parent_of_existing_annotation(true);
								}else if(go.isInferred_child()){
									canno.getEvidence().setMatches_child_of_existing_annotation(true);
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
								}else if(go.isInferred_child()){
									canno.getEvidence().setMatches_child_of_panther_go(true);
								}else{
									canno.getEvidence().setMatches_panther_go_directly(true);
								}
								break;
							}
						}
					}
					cannos.add(canno);
				}
			}
		}
		
		try {
			FileWriter f = new FileWriter(outfile);
			f.write(CandidateAnnotation.getHeader()+"\n");
			for(CandidateAnnotation canno : cannos){
				f.write(canno.toString()+"\n");
			}
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return cannos;
	}

	
	public static Map<String, Set<GOterm>> mergeTextMined2LinkMined(
			Map<String, Set<GOterm>> geneid_gos, boolean keep_evidence) {
		CandidateAnnotations cannolist = new CandidateAnnotations();
		cannolist.loadCandidateAnnotations(linkedannos);
		List<CandidateAnnotation> testcannos = cannolist.getCannos();
//		Map<String, Set<GOterm>> linked_gos = new HashMap<String, Set<GOterm>>();
		GOowl gol = new GOowl();
		gol.initFromFile(false);
		for(CandidateAnnotation canno : testcannos){
			String gene = canno.getEntrez_gene_id();
			Set<GOterm> gos = geneid_gos.get(gene);
			if(gos == null){
				gos = new HashSet<GOterm>();
			}
			String go_acc = canno.getTarget_accession();
			GOterm go = gol.makeGOterm(go_acc.substring(3));
			if(keep_evidence){
				go.setEvidence(canno.getString_matching_method());
			}else{
				go.setEvidence("unknown");//makes this non-redundant
			}
			gos.add(go);
			geneid_gos.put(gene, gos);	
		}
		return geneid_gos;
	}
	
		public static MapComparison compareTextMined2LinkMined(
				Map<String, Set<GOterm>> geneid_gos, boolean reasoning_on) {
			CandidateAnnotations cannolist = new CandidateAnnotations();
			cannolist.loadCandidateAnnotations(linkedannos);
			List<CandidateAnnotation> testcannos = cannolist.getCannos();
			Map<String, Set<GOterm>> linked_gos = new HashMap<String, Set<GOterm>>();
			Map<String, Set<GOterm>> linked_gos_in_i = new HashMap<String, Set<GOterm>>();
			for(CandidateAnnotation canno : testcannos){
				String gene = canno.getEntrez_gene_id();
				Set<GOterm> gos = linked_gos.get(gene);
				if(gos == null){
					gos = new HashSet<GOterm>();
				}
				String go_acc = canno.getTarget_accession();
				GOterm go = new GOterm("",go_acc,"","", true);
				go.setEvidence(canno.getString_matching_method());
				gos.add(go);
				linked_gos.put(gene, gos);
				if(geneid_gos.containsKey(gene)){
					linked_gos_in_i.put(gene, gos);
				}
			}
			MapComparison ac = BioInfoUtil.compareGOMaps(geneid_gos, linked_gos, reasoning_on);
			return ac;
		}

		public static MapComparison compareAnnotations2GO(Map<String, Set<GOterm>> test_annos) {
			//read in gene to go ~term map
			String file = Config.gene_go_file;
			Map<String, Set<GOterm>> geneid_go = GeneWikiUtils.readGene2GO(file, "\t", "#");
			System.out.println("read geneid_go map, doing no-reasoning comparison");
			MapComparison no_reasoning = BioInfoUtil.compareGOMaps(test_annos, geneid_go, false);
			System.out.println("no reasoning\n"+no_reasoning);
			MapComparison reasoning = BioInfoUtil.compareGOMaps(test_annos, geneid_go, true);
			System.out.println("with reasoning\n"+reasoning);
			return reasoning;
		}


		public static String summarizeAnno2go(String evidence, Map<String, HashSet<GOterm>> page_terms){
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
						float n = pct_bp+pct_mf+pct_cc;
						System.out.println("problem "+n+" "+total_match+" "+pct_bp+" "+pct_mf+" "+pct_cc);
					}

					bp+=pct_bp; mf+=pct_mf; cc+=pct_cc;
					n_mapped++;
				}
			}
			if(evidence==null){
				evidence = "all";
			}		
			summary = "total pages:"+n_pages_in_map+"\ttotal potential anntotations"+total_possibly_redundant+"\n" +
			""+bp/n_mapped+"\t"+mf/n_mapped+"\t"+cc/n_mapped+"\t\n";
			return summary;
		}

		/**
		 * Loads the table created by buildGwiki2go into a map
		 * key = wiki page title
		 * value = set of GOterms linked to the page
		 * identity for the set determined by combination of go accession and evidence
		 * @return
		 */
		public static Map<String, HashSet<GOterm>> loadAnno2GO(){
			String file = anno2gofile;
			List<UimaAnnotation> uimas = ParseUIMA.parseNCBOOut(file);
			if(uimas==null||uimas.size()<1){
				return null;
			}
			GOowl gol = new GOowl();
			gol.initFromFile(false);

			Map<String, HashSet<GOterm>> page_term = new HashMap<String, HashSet<GOterm>>();

			for(UimaAnnotation uima : uimas){
				String wikipage = uima.getDocId();
				String acc = uima.getOntologyTermId();
				GOterm t = gol.makeGOterm(acc.substring(3));

				HashSet<GOterm> terms = page_term.get(wikipage);
				if(terms==null){
					t.setNumAppearances(1);
					terms = new HashSet<GOterm>();
				}else{
					if(terms.contains(t)){
						t.setNumAppearances(t.getNumAppearances()+1);
					}
				}
				terms.add(t);
				
				if(wikipage.equals("TAF2")){
					System.out.println(wikipage+" "+acc+" "+t.getTerm());
				}
				
				//is the term obsolete?
				/*			Set<GOterm> repls = gol.getGoTermObsolete(t);
			if(repls==null){					
				terms.add(t);
			}else{
				terms.addAll(repls);
			}
				 */
				page_term.put(wikipage, terms);

			}		
			gol.close();
			return page_term;
		}
	}
