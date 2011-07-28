package org.gnf.genewiki.associations;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.gnf.crawl.GoGeneCrawler;
import org.gnf.dont.DOmapping;
import org.gnf.dont.DOowl;
import org.gnf.dont.DOterm;
import org.gnf.genewiki.Config;
import org.gnf.genewiki.GeneWikiLink;
import org.gnf.genewiki.GeneWikiPage;
import org.gnf.genewiki.GeneWikiUtils;
import org.gnf.go.Annotation;
import org.gnf.go.GObayes;
import org.gnf.go.GOmapper;
import org.gnf.go.GOowl;
import org.gnf.go.GOsurpriser;
import org.gnf.go.GOterm;
import org.gnf.search.YahooBOSS;
import org.gnf.util.BioInfoUtil;
import org.gnf.util.Gene;

import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.RDFS;

public class CandiAnnoSorter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
				CandidateAnnotations cannolist = new CandidateAnnotations();
				String annos = Config.text_mined_annos;
				cannolist.loadAndFilterCandidateGOAnnotations(annos, false);
				List<CandidateAnnotation> testcannos = cannolist.getCannos();
				System.out.println("Loaded candidate GO annotations "+testcannos.size());
				testcannos = addAllEvidenceForGOAnnotations(testcannos);
				cannolist.setCannos(testcannos);
				cannolist.writeGOCandiListWithEvidence("/Users/bgood/data/genewiki/output/no_IEA.txt");
				System.out.println("Wrote output");

		//CandiAnnoSorter.rankAndSaveGOAnnotations(Config.text_mined_annos);
		//CandiAnnoSorter.rankAndSaveDOAnnotations(Config.text_mined_annos);
		
	}


	/**
	 * FMA-specific features and ranking
	 */
	public static void rankAndSaveFMAAnnotations(String annos){
		CandidateAnnotations cannolist = new CandidateAnnotations();
		cannolist.loadAndFilterCandidateFMAAnnotations(annos);
		List<CandidateAnnotation> testcannos = cannolist.getCannos();		
		System.out.println("Loaded candidate FMA annotations "+testcannos.size());

		testcannos = setYahooCoFromCache(testcannos);
		System.out.println("Set Yahoo co-occurrence information");	

		testcannos = rankPredictions(testcannos);
		System.out.println("Completed ranking");

		cannolist.setCannos(testcannos);
		cannolist.writeCandiListWithEvidence(Config.merged_mined_ranked_annos_fma);
		System.out.println("Wrote output to: "+Config.merged_mined_ranked_annos_fma);
	}

	/**
	 * DO-specific features and ranking
	 */
	public static void rankAndSaveDOAnnotations(String annos){
		CandidateAnnotations cannolist = new CandidateAnnotations();
		cannolist.loadAndFilterCandidateDOAnnotations(annos);
		List<CandidateAnnotation> testcannos = cannolist.getCannos();		
		System.out.println("Loaded candidate DO annotations "+testcannos.size());

		testcannos = addAllEvidenceForDOAnnotations(testcannos);

		cannolist.setCannos(testcannos);
		cannolist.writeCandiListWithEvidence(Config.merged_mined_ranked_annos_do);
		System.out.println("Wrote output to: "+Config.merged_mined_ranked_annos_do);
	}

	public static List<CandidateAnnotation> addAllEvidenceForDOAnnotations(List<CandidateAnnotation> testcannos){
		testcannos = setYahooCoFromCache(testcannos);
		System.out.println("Set Yahoo co-occurrence information");	

		testcannos = setKnownHumanAnnotationsDO(testcannos);
		System.out.println("Set predicted by generifs");

		testcannos = rankPredictions(testcannos);
		System.out.println("Completed ranking");

		return testcannos;
	}


	/**
	 * GO-specific annotation ranking scheme
	 */
	public static void rankAndSaveGOAnnotations(String annos){
		CandidateAnnotations cannolist = new CandidateAnnotations();
		cannolist.loadAndFilterCandidateGOAnnotations(annos, false);
		List<CandidateAnnotation> testcannos = cannolist.getCannos();
		System.out.println("Loaded candidate GO annotations "+testcannos.size());
		testcannos = addAllEvidenceForGOAnnotations(testcannos);
		cannolist.setCannos(testcannos);
		String o = Config.merged_mined_ranked_annos_go;
		o = "/Users/bgood/data/genewiki_jan_2011/output/candidate_go_annotations.txt";
		cannolist.writeGOCandiListWithEvidence(o);
		System.out.println("Wrote output to: "+o);
	}

	public static List<CandidateAnnotation> addAllEvidenceForGOAnnotations(List<CandidateAnnotation> testcannos){

		boolean usesimplified = true;
		//get a reasoning and non reasoning version of the GO ready
		GOowl gol = new GOowl();
		gol.initFromFileRDFS(usesimplified);
		GOowl gol_no_infer = new GOowl();
		gol_no_infer.initFromFile(usesimplified);
		System.out.println("Gene Ontology loaded");	

//		setFuncBaseScoreGO(testcannos, gol, gol_no_infer);
//		trytofreeMemory();
//		System.out.println("Set FuncBase score for GO terms");	
//
//		setYahooCoFromCache(testcannos);
//		System.out.println("Set Yahoo co-occurrence information");	
//
//		setObsoleteGO(testcannos, true, gol_no_infer);
//		System.out.println("Set Obsolete GO terms");
//		//
//		//		setPriorConfidenceGO(testcannos, gol);
//		//		trytofreeMemory();
//		//		System.out.println("Set prior probility for GO terms");		
//
//		setGeneGOPredictions(testcannos, gol, gol_no_infer);
//		trytofreeMemory();
//		System.out.println("Set GeneGo predictions for GO terms");		
//
//		setPantherOrthoSupportGO(testcannos, gol, gol_no_infer);
//		trytofreeMemory();
//		System.out.println("Set panther family support");

		setKnownHumanAnnotationsGO(testcannos, true, gol, gol_no_infer);
		trytofreeMemory();
		System.out.println("Set known human");

		rankPredictions(testcannos);
		System.out.println("Completed ranking");
		return testcannos;
	}

	public static void trytofreeMemory(){
		Runtime runtime = Runtime.getRuntime();
		long m = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Available before gc mb "+m/1000000);		
		m = runtime.totalMemory() - runtime.freeMemory();
		System.gc();
		m = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Available after gc mb "+m/1000000);		
	}

	public static void getRealCandidateAnnotations(){
		CandidateAnnotations c = new CandidateAnnotations();
		c.loadAndFilterCandidateGOAnnotations(Config.merged_mined_ranked_annos_go, false);
		System.out.println("all "+c.cannos.size());
		c.filterPBB();
		System.out.println("no pbb "+c.cannos.size());
		c.filterCommonGOMissMappings();
		System.out.println("filtered "+c.cannos.size());
		c.filterKnownHuman();
		System.out.println("Existing matches removed "+c.cannos.size());
		c.writeGOCandiListWithEvidence(Config.merged_mined_ranked_filtered_annos_go);
	}


	/**
	 * Check to see if the candidates that emerged from link mining have links coming back to them from the pages that they link to
	 * @param cannos
	 * @return
	 */
	public static List<CandidateAnnotation> addBackLinksAndFlagPBB(List<CandidateAnnotation> cannos){
		int c = 0; int n = 0; int pb = 0;
		Filter f = new Filter();
		Map<String, GeneWikiPage> pages = new HashMap<String, GeneWikiPage>();
		for(CandidateAnnotation canno : cannos){
			if(pages.get(canno.getEntrez_gene_id())==null){
				GeneWikiPage p = GeneWikiUtils.deserializeGeneWikiPage(Config.gwikidir+canno.getEntrez_gene_id());
				pages.put(canno.getEntrez_gene_id(), p);
			}
			//			if(hasBackLink(pages.get(canno.getEntrez_gene_id()), canno)){
			//				canno.setHasBackLink(true);
			//				c++;
			//			}
			if(f.isProteinBoxBotSummary(pages.get(canno.getEntrez_gene_id()),canno)){
				canno.setPBB(true);
				pb++;
			}else{
				canno.setPBB(false);
			}
			n++;
			if(n%100==0){
				System.out.print(n+"\t");
				if(n%1000==0){
					System.out.println();
				}
			}
		}
		System.out.println("Found "+c+" backlinks. "+pb+" pbbs");
		return cannos;
	}

	/**
	 * For this candidate, check to see if there is a backlink to the gene page from the page that the gene page linked to and was mapped to GO..
	 * @param page
	 * @param canno
	 * @return
	 */
	//	public static boolean hasBackLink(GeneWikiPage page, CandidateAnnotation canno){
	//		if(!page.getNcbi_gene_id().equals(canno.getEntrez_gene_id())||
	//				!((canno.getString_matching_method().contains("redirect"))||(canno.getString_matching_method().contains("title")))){
	//			return false;
	//		}
	//		for(GeneWikiLink glink : page.getInglinks()){
	//			GeneWikiPage linked = glink.getTarget_page();
	//			String title = linked.getTitle();
	//			if(title.trim().equalsIgnoreCase(canno.getTarget_preferred_term())){
	//				return true;				
	//			}
	//		}
	//		return false;
	//	}

	/**
	 * Check to see if the candidates came from a PBB summary template	
	 * @param cannos
	 * @return
	 */
	public static List<CandidateAnnotation> flagPBBsummary(List<CandidateAnnotation> cannos){
		Filter f = new Filter();
		int c = 0; int p = 0;
		Map<String, GeneWikiPage> pages = new HashMap<String, GeneWikiPage>();
		for(CandidateAnnotation canno : cannos){
			if(pages.get(canno.getEntrez_gene_id())==null){
				GeneWikiPage page = GeneWikiUtils.deserializeGeneWikiPage(Config.gwikidir+canno.getEntrez_gene_id());
				pages.put(canno.getEntrez_gene_id(), page);
			}			
			if(f.isProteinBoxBotSummary(pages.get(canno.getEntrez_gene_id()),canno)){
				canno.setPBB(true);
				c++;
			}else{
				canno.setPBB(false);
			}
			p++;
			if(p%100==0){
				System.out.print(p+"\t");
				if(p%1000==0){
					System.out.println();
				}
			}
		}

		System.out.println("flagged "+c+" from PBB summary");
		return cannos;
	}

	/**
	 * Push novel ones with support to the top, weight panther more heavily	
	 * @param cannos
	 * @return
	 */
	public static List<CandidateAnnotation> rankPredictions(List<CandidateAnnotation> cannos){
		for(CandidateAnnotation canno : cannos){
			double c = 0;
			//comparison to known human annotations
			if(canno.getEvidence().isMatches_existing_annotation_directly()||canno.getEvidence().isMatches_parent_of_existing_annotation()){
				c = c - 500;
			}
			//			if(canno.getEvidence().isMatches_child_of_existing_annotation()){
			//				c+=20;
			//			}
			//comparison to Panther 
			if(canno.getEvidence().isMatches_panther_go_directly()||
					canno.getEvidence().isMatches_child_of_panther_go()){
				c += 15;
			}
			if(canno.getEvidence().isMatches_parent_of_panther_go()){
				c += 10;
			}
			//comparison to genego
			if(canno.getEvidence().isMatches_genego_directly()||
					canno.getEvidence().isMatches_child_of_genego()){
				c += 10;
			}
			if(canno.getEvidence().isMatches_parent_of_genego()){
				c += 5;
			}
			//funcbasee
			if(canno.getEvidence().getFuncbase_score()>0){
				c += 3+canno.getEvidence().getFuncbase_score();
				if(canno.getEvidence().isMatches_parent_of_funcbase()||
						canno.getEvidence().isMatches_child_of_funcbase()){
					c+=1;
				}
				if(canno.getEvidence().isMatches_funcbase_directly()){
					c+=3;
				}
			}
			//Yahoo 
			if(canno.getEvidence().getSect()!=null){
				c += 10*canno.getEvidence().getSect().getNormalizedYahooRank();
			}
			//Prior
			c += -10000*canno.getEvidence().getPriorGO();
			//original term detection criteria
			if(canno.getString_matching_method().contains("title")){
				c+=10;
			}
			//			if(canno.getString_matching_method().contains("redirect")){
			//				c -= 5;
			//			}
			//			if(canno.getString_matching_method().contains("text")){
			//				c -= 10;
			//			}
			//whether term is obsolete
			if(canno.getEvidence().isGoObsolete()){
				c -= 20;
			}
			//Whether there are any references
			if(canno.getCsvrefs()!=null&&canno.getCsvrefs().length()>3){
				c+= 25;
			}
			canno.getEvidence().setConfidence(c);
		}
		Collections.sort(cannos);
		return cannos;
	}

	/**
	 * Indicate whether a DO annotation matches one from before (where these are drawn from the geneRIF-DO study)
	 * @param cannos
	 * @return
	 */
	public static List<CandidateAnnotation> setKnownHumanAnnotationsDO(List<CandidateAnnotation> cannos){
		Map<String, Set<DOterm>> genedo = DOmapping.loadGeneRifs2DO();
		//Map<String, Set<DOterm>> genedo = DOmapping.merge();

		//adds parents and children
		System.out.println("generif Predicted DO loaded");	
		DOowl dowl = new DOowl();
		dowl.initFromFileRDFS();
		DOowl do_dumb = new DOowl();
		do_dumb.initFromFile();

		System.out.println("Known human loaded, checking for matches ");		
		for(CandidateAnnotation canno : cannos){			
			Set<DOterm> dos = genedo.get(canno.getEntrez_gene_id());
			if(dos!=null){
				//first check for exact matches
				for(DOterm da : dos){
					if(da.getAccession().equals(canno.getTarget_accession())){
						canno.getEvidence().setMatches_existing_annotation_directly(true);
						//match found
						break;
					}
				}
				if(canno.getEvidence().isMatches_existing_annotation_directly()){
					//go to the next candidate annotation
					continue;
				}
				//no match found so check if it matches the parent of a reference annotation
				//check parents
				for(DOterm da : dos){
					boolean stop = false;
					Set<DOterm> more = dowl.getSupers(da);
					if(more!=null){
						for(DOterm parent : more){
							if(parent.getAccession().equals(canno.getTarget_accession())){
								canno.getEvidence().setMatches_parent_of_existing_annotation(true);
								//match found, go to the next candidate annotation
								stop = true;
								break;
							}
						}
						if(stop){
							break;
						}
					}
				}
				if(canno.getEvidence().isMatches_parent_of_existing_annotation()){
					//go to the next candidate annotation
					continue;
				}

				//still no match found so
				//check children
				for(DOterm da : dos){
					boolean stop = false;
					Set<DOterm> more = do_dumb.getDirectChildren(da);
					if(more!=null){
						for(DOterm child : more){
							if(child.getAccession().equals(canno.getTarget_accession())){
								canno.getEvidence().setMatches_child_of_existing_annotation(true);
								//match found
								stop = true;
								break;
							}
						}
					}
					if(stop){
						break;
					}
				}
			}
		}
		
//		System.out.println("generif Predicted DO expanded");
//		for(CandidateAnnotation canno : cannos){			
//			Set<DOterm> doroot = genedo.get(canno.getEntrez_gene_id());
//			if(doroot!=null){
//				Set<DOterm> dos = new HashSet<DOterm>(doroot);
//				//expand
//				for(DOterm dt : doroot){
//					Set<DOterm> fam = dowl.getSupers(dt);
//					if(fam!=null){
//						dos.addAll(fam);
//					}
//					fam = do_dumb.getSubs(dt);
//					if(fam!=null){
//						dos.addAll(fam);
//					}
//				}
//
//				if(dos!=null){
//					for(DOterm dot : dos){
//						if(dot.getAccession().equals(canno.getTarget_accession())){
//							if(dot.isInferred_parent()){
//								canno.getEvidence().setMatches_parent_of_existing_annotation(true);
//							}else if(dot.isInferred_child()){
//								canno.getEvidence().setMatches_child_of_existing_annotation(true);
//							}else{
//								canno.getEvidence().setMatches_existing_annotation_directly(true);
//							}
//
//						}
//					}
//				}
//			}
//		}
		return cannos;
	}

	public static List<CandidateAnnotation> setKnownHumanAnnotationsGO(List<CandidateAnnotation> cannos, boolean skipIEA, GOowl gol, GOowl gol_no_infer){
		//	Map<String, Set<GOterm>> genego = GeneWikiUtils.readGene2GO(Config.gene_go_file, "\t", "#");
		Map<String, Set<GOterm>> genego = Annotation.readGene2GO(Config.gene_go_file, skipIEA);
		//adds parents
		System.out.println("Known human loaded, checking for matches ");		
		for(CandidateAnnotation canno : cannos){			
			Set<GOterm> gos = genego.get(canno.getEntrez_gene_id());
			if(gos!=null){
				//first check for exact matches
				for(GOterm go : gos){
					if(go.getAccession().equals(canno.getTarget_accession())){
						canno.getEvidence().setMatches_existing_annotation_directly(true);
						//match found
						break;
					}
				}
				if(canno.getEvidence().isMatches_existing_annotation_directly()){
					//go to the next candidate annotation
					continue;
				}
				//no match found so check if it matches the parent of a reference annotation
				//check parents
				for(GOterm go : gos){
					boolean stop = false;
					Set<GOterm> more = gol.getSupers(go);
					if(more!=null){
						for(GOterm parent : more){
							if(parent.getAccession().equals(canno.getTarget_accession())){
								canno.getEvidence().setMatches_parent_of_existing_annotation(true);
								//match found, go to the next candidate annotation
								stop = true;
								break;
							}
						}
						if(stop){
							break;
						}
					}
				}
				if(canno.getEvidence().isMatches_parent_of_existing_annotation()){
					//go to the next candidate annotation
					continue;
				}

				//still no match found so
				//check children
				for(GOterm go : gos){
					boolean stop = false;
					Set<GOterm> more = gol_no_infer.getDirectChildren(go);
					if(more!=null){
						for(GOterm child : more){
							if(child.getAccession().equals(canno.getTarget_accession())){
								canno.getEvidence().setMatches_child_of_existing_annotation(true);
								//match found
								stop = true;
								break;
							}
						}
					}
					if(stop){
						break;
					}
				}
			}
		}


		return cannos;
	}

	//	public static List<CandidateAnnotation> setKnownHumanAnnotationsGO(List<CandidateAnnotation> cannos){
	//		//	Map<String, Set<GOterm>> genego = GeneWikiUtils.readGene2GO(Config.gene_go_file, "\t", "#");
	//		Map<String, Set<GOterm>> genego = Annotation.readGene2GOtrackEvidence(Config.gene_go_file);
	//		//adds parents
	//		System.out.println("Known human loaded");		
	//		genego = BioInfoUtil.expandGoMap(genego, null, true);
	//		System.out.println("Known human expanded");
	//		for(CandidateAnnotation canno : cannos){			
	//			Set<GOterm> gos = genego.get(canno.getEntrez_gene_id());
	//			if(gos!=null){
	//				for(GOterm go : gos){
	//					if(go.getAccession().equals(canno.getTarget_accession())){
	//						canno.getEvidence().setGo_evidence_type(go.getEvidence());
	//						if(go.isInferred_parent()){
	//							canno.getEvidence().setMatches_parent_of_existing_annotation(true);
	//						}else if(go.isInferred_child()){
	//							canno.getEvidence().setMatches_child_of_existing_annotation(true);
	//						}else{
	//							canno.getEvidence().setMatches_existing_annotation_directly(true);
	//						}
	//
	//					}
	//				}
	//			}
	//		}
	//
	//		return cannos;
	//	}

	public static List<CandidateAnnotation> setYahooCoFromCache(List<CandidateAnnotation> cannos){
		cannos = YahooBOSS.setIntersects(cannos);
		return cannos;
	}


	//set prior probability for each GO term (independent of gene)
	public static List<CandidateAnnotation> setPriorConfidenceGO(List<CandidateAnnotation> cannos, GOowl gol){
		boolean addParents = true;
		GObayes bay = new GObayes(addParents, gol);
		for(CandidateAnnotation canno : cannos){
			canno.getEvidence().setPriorGO(
					bay.priorForGO(
							bay.getGObyAccN(
									canno.getTarget_accession().substring(3), 
									canno.getTarget_preferred_term())));
		}
		bay.cleanup();
		bay = null;
		return cannos;
	}

	/**
	 * Indicate whether ontology term is obsolete or not	
	 * @param cannos
	 * @return
	 */
	public static List<CandidateAnnotation> setObsoleteGO(List<CandidateAnnotation> cannos, boolean tagDepth, GOowl gol){
		for(CandidateAnnotation canno : cannos){
			if(gol.isGoTermObsolete(canno.getTarget_accession())){
				canno.getEvidence().setGoObsolete(true);
			}else{
				canno.getEvidence().setGoObsolete(false);
			}
			//
			if(tagDepth){
				double d = gol.getIsaDepth(canno.getTarget_accession());
				canno.getEvidence().setGoDepth(d);
			}
		}
		return cannos;
	}

	/**
	 * Remove annotations to obsolete terms
	 * @param cannos
	 * @return
	 */
	public static List<CandidateAnnotation> removeObsoleteGO(List<CandidateAnnotation> cannos, GOowl gol){
		cannos = setObsoleteGO(cannos, false, gol);
		List<CandidateAnnotation> cleancannos = new ArrayList<CandidateAnnotation>();
		for(CandidateAnnotation canno : cannos){
			if(!canno.getEvidence().isGoObsolete()){
				cleancannos.add(canno);
			}
		}
		return cleancannos;
	}	

	/**
	 * Add support from panther family orthologs
	 * @param cannos
	 * @param gol_no_infer 
	 * @param gol 
	 * @return
	 */
	public static List<CandidateAnnotation> setPantherOrthoSupportGO(List<CandidateAnnotation> cannos, GOowl gol, GOowl gol_no_infer){
		Map<String, Set<GOterm>> genego = PantherMapper.getPantherData();
		System.out.println("Panther loaded, checking for matches");
		for(CandidateAnnotation canno : cannos){
			Set<GOterm> gos = genego.get(canno.getEntrez_gene_id());
			if(gos!=null){
				//first check for exact matches
				for(GOterm go : gos){
					if(go.getAccession().equals(canno.getTarget_accession())){
						canno.getEvidence().setMatches_panther_go_directly(true);
						//match found
						break;
					}
				}
				if(canno.getEvidence().isMatches_panther_go_directly()){
					//go to the next candidate annotation
					continue;
				}
				//no match found so check if it matches the parent of a reference annotation
				//check parents
				for(GOterm go : gos){
					boolean stop = false;
					Set<GOterm> more = gol.getSupers(go);
					if(more!=null){
						for(GOterm parent : more){
							if(parent.getAccession().equals(canno.getTarget_accession())){
								canno.getEvidence().setMatches_parent_of_panther_go(true);
								//match found, go to the next candidate annotation
								stop = true;
								break;
							}
						}
						if(stop){
							break;
						}
					}
				}
				if(canno.getEvidence().isMatches_parent_of_panther_go()){
					//go to the next candidate annotation
					continue;
				}

				//still no match found so
				//check children
				for(GOterm go : gos){
					boolean stop = false;
					Set<GOterm> more = gol_no_infer.getDirectChildren(go);
					if(more!=null){
						for(GOterm child : more){
							if(child.getAccession().equals(canno.getTarget_accession())){
								canno.getEvidence().setMatches_child_of_panther_go(true);
								//match found
								stop = true;
								break;
							}
						}
					}
					if(stop){
						break;
					}
				}
			}
		}

		//		for(CandidateAnnotation canno : cannos){			
		//			Set<GOterm> gos = genego.get(canno.getEntrez_gene_id());
		//			Set<GOterm> gosfamily = new HashSet<GOterm>();
		//			//add parents
		//			if(gos!=null){
		//				for(GOterm go : gos){
		//					gosfamily.add(go);
		//					Set<GOterm> more = gol.getSupers(go);
		//					if(more!=null){
		//						gosfamily.addAll(more);
		//					}
		//					more = gol_no_infer.getDirectChildren(go);
		//					if(more!=null){
		//						gosfamily.addAll(more);
		//					}
		//				}
		//				if(gosfamily!=null){
		//					for(GOterm go : gosfamily){
		//						if(go.getAccession().equals(canno.getTarget_accession())){
		//							if(go.isInferred_parent()){
		//								canno.getEvidence().setMatches_parent_of_panther_go(true);
		//							}else if(go.isInferred_child()){
		//								canno.getEvidence().setMatches_child_of_panther_go(true);
		//							}else{
		//								canno.getEvidence().setMatches_panther_go_directly(true);
		//							}
		//						}
		//					}
		//				}
		//			}
		//		}

		return cannos;
	}

	/**
	 * Add support from cached GoGenes for candidates
	 * @param cannos
	 * @param gol_no_infer 
	 * @param gol 
	 * @return
	 */
	public static List<CandidateAnnotation> setGeneGOPredictions(List<CandidateAnnotation> cannos, GOowl gol, GOowl gol_no_infer){
		Map<String, Set<GOterm>> genego = GoGeneCrawler.getGoGeneData();

		System.out.println("GOgenes loaded, checking for matches");
		for(CandidateAnnotation canno : cannos){
			Set<GOterm> gos = genego.get(canno.getEntrez_gene_id());
			if(gos!=null){
				//first check for exact matches
				for(GOterm go : gos){
					if(go.getAccession().equals(canno.getTarget_accession())){
						canno.getEvidence().setMatches_genego_directly(true);
						//match found
						break;
					}
				}
				if(canno.getEvidence().isMatches_genego_directly()){
					//go to the next candidate annotation
					continue;
				}
				//no match found so check if it matches the parent of a reference annotation
				//check parents
				for(GOterm go : gos){
					boolean stop = false;
					Set<GOterm> more = gol.getSupers(go);
					if(more!=null){
						for(GOterm parent : more){
							if(parent.getAccession().equals(canno.getTarget_accession())){
								canno.getEvidence().setMatches_parent_of_genego(true);
								//match found, go to the next candidate annotation
								stop = true;
								break;
							}
						}
						if(stop){
							break;
						}
					}
				}
				if(canno.getEvidence().isMatches_parent_of_genego()){
					//go to the next candidate annotation
					continue;
				}

				//still no match found so
				//check children
				for(GOterm go : gos){
					boolean stop = false;
					Set<GOterm> more = gol_no_infer.getDirectChildren(go);
					if(more!=null){
						for(GOterm child : more){
							if(child.getAccession().equals(canno.getTarget_accession())){
								canno.getEvidence().setMatches_child_of_genego(true);
								//match found
								stop = true;
								break;
							}
						}
					}
					if(stop){
						break;
					}
				}
			}
		}


		//		for(CandidateAnnotation canno : cannos){			
		//			Set<GOterm> gos = genego.get(canno.getEntrez_gene_id());
		//			Set<GOterm> gosfamily = new HashSet<GOterm>();
		//			//add parents
		//			if(gos!=null){
		//				for(GOterm go : gos){
		//					gosfamily.add(go);
		//					Set<GOterm> more = gol.getSupers(go);
		//					if(more!=null){
		//						gosfamily.addAll(more);
		//					}
		//					more = gol_no_infer.getDirectChildren(go);
		//					if(more!=null){
		//						gosfamily.addAll(more);
		//					}
		//				}
		//				if(gosfamily!=null){
		//					for(GOterm go : gosfamily){
		//						if(go.getAccession().equals(canno.getTarget_accession())){
		//							if(go.isInferred_parent()){
		//								canno.getEvidence().setMatches_parent_of_genego(true);
		//							}else if(go.isInferred_child()){
		//								canno.getEvidence().setMatches_child_of_genego(true);
		//							}else{
		//								canno.getEvidence().setMatches_genego_directly(true);
		//							}
		//						}
		//					}
		//				}
		//			}
		//		}
		genego.clear();
		genego = null;
		return cannos;
	}

	/**
	 * Add support from FuncBase
	 * @param cannos
	 * @param gol_no_infer 
	 * @param gol 
	 * @return
	 */
	public static List<CandidateAnnotation> setFuncBaseScoreGO(List<CandidateAnnotation> cannos, GOowl gol, GOowl gol_no_infer){
		//load funcbase
		//only load genes we care about
		Set<String> genes = new HashSet<String>();
		for(CandidateAnnotation canno : cannos){
			genes.add(canno.getEntrez_gene_id());
		}

		Map<String, Double> genego_score = new HashMap<String, Double>();
		Map<String, Set<GOterm>> genego = new HashMap<String, Set<GOterm>>();
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(Config.funcbase));
			String line = f.readLine();
			while(line!=null){
				String[] item = line.split("\t");
				String geneid = item[0];
				if(!genes.contains(geneid)){
					line = f.readLine();
					continue;
				}
				String goacc = item[1];
				if(!item[2].equals("None")){
					double score = Double.parseDouble(item[2]);
					genego_score.put(geneid+goacc, score);
					GOterm term = new GOterm("",goacc,"","", true);

					Set<GOterm> terms = genego.get(geneid);
					if(terms==null){
						terms = new HashSet<GOterm>();
					}
					terms.add(term);
					genego.put(geneid, terms);
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

		System.out.println("funcbase hits loaded, checking for matches");
		for(CandidateAnnotation canno : cannos){			
			Double score = genego_score.get(canno.getEntrez_gene_id()+canno.getTarget_accession());

			Set<GOterm> gos = genego.get(canno.getEntrez_gene_id());
			if(gos!=null){
				//first check for exact matches
				for(GOterm go : gos){
					if(go.getAccession().equals(canno.getTarget_accession())){
						canno.getEvidence().setMatches_funcbase_directly(true);
						if(score!=null){
							canno.getEvidence().setFuncbase_score(score);
						}
						//match found
						break;
					}
				}
				if(canno.getEvidence().isMatches_funcbase_directly()){
					//go to the next candidate annotation
					continue;
				}
				//no match found so check if it matches the parent of a reference annotation
				//check parents
				for(GOterm go : gos){
					boolean stop = false;
					Set<GOterm> more = gol.getSupers(go);
					if(more!=null){
						for(GOterm parent : more){
							if(parent.getAccession().equals(canno.getTarget_accession())){
								canno.getEvidence().setMatches_parent_of_funcbase(true);
								//match found, go to the next candidate annotation
								stop = true;
								break;
							}
						}
						if(stop){
							break;
						}
					}
				}
				if(canno.getEvidence().isMatches_parent_of_funcbase()){
					//go to the next candidate annotation
					continue;
				}

				//still no match found so
				//check children
				for(GOterm go : gos){
					boolean stop = false;
					Set<GOterm> more = gol_no_infer.getDirectChildren(go);
					if(more!=null){
						for(GOterm child : more){
							if(child.getAccession().equals(canno.getTarget_accession())){
								canno.getEvidence().setMatches_child_of_funcbase(true);
								//match found
								stop = true;
								break;
							}
						}
					}
					if(stop){
						break;
					}
				}
			}

		}
		//		for(CandidateAnnotation canno : cannos){
		//			Double score = genego_score.get(canno.getEntrez_gene_id()+canno.getTarget_accession());
		//			if(score!=null){
		//				canno.getEvidence().setFuncbase_score(score);
		//				Set<GOterm> gos = genego_terms.get(canno.getEntrez_gene_id());
		//				Set<GOterm> gosfamily = new HashSet<GOterm>();
		//				//add parents
		//				for(GOterm go : gos){
		//					gosfamily.add(go);
		//					Set<GOterm> more = gol.getSupers(go);
		//					if(more!=null){
		//						gosfamily.addAll(more);
		//					}
		//					more = gol_no_infer.getDirectChildren(go);
		//					if(more!=null){
		//						gosfamily.addAll(more);
		//					}
		//				}
		//				for(GOterm go : gosfamily){
		//					if(go.getAccession().equals(canno.getTarget_accession())){
		//						if(go.isInferred_parent()){
		//							canno.getEvidence().setMatches_parent_of_funcbase(true);
		//						}else if(go.isInferred_child()){
		//							canno.getEvidence().setMatches_child_of_funcbase(true);
		//						}else{
		//							canno.getEvidence().setMatches_funcbase_directly(true);
		//						}
		//					}
		//				}
		//			}else{
		//				canno.getEvidence().setFuncbase_score(0);
		//				c++;
		//			}
		//		}
		return cannos;
	} 

	/**
	 * This was an interesting idea but didn't work for crap...
	 * @param cannos
	 * @return
	 */
	//rank by surprise P value 
	public static List<CandidateAnnotation> setSurpriseConfidenceGO(List<CandidateAnnotation> cannos){
		//get the anti-associated list
		boolean addParents = false;
		GOsurpriser surpriser = new GOsurpriser(addParents);
		float n = 0; float t = 0;
		for(CandidateAnnotation canno : cannos){
			canno.getEvidence().setConfidence(surpriser.getSurprise(canno));
			if(canno.getEvidence().getConfidence()==1){
				n++;
			}
			t++;
		}
		System.out.println("n no data "+n/t);
		return cannos;
	}

	/**
	 * Set the confidence to a random number for testing
	 * @param cannos
	 * @return
	 */
	public static List<CandidateAnnotation> setRandomConfidence(List<CandidateAnnotation> cannos){
		for(CandidateAnnotation canno : cannos){
			canno.getEvidence().setConfidence(Math.random());
		}
		return cannos;
	}

	/************************************
	Descriptive Statistics for distribution of confidence assignments
	 *************************************/

	/**
	 * Get description of confidence estimates for tight matches from link mining
	 */
	public static DescriptiveStatistics confidenceForTightKnown(List<CandidateAnnotation> cannos){
		DescriptiveStatistics stats = new DescriptiveStatistics();
		//only count each pair once (regardless of mapping methods)
		Set<String> unique = new HashSet<String>();
		for(CandidateAnnotation canno : cannos){
			if(unique.add(canno.getEntrez_gene_id()+canno.getTarget_accession())){
				if(canno.getEvidence().isMatches_existing_annotation_directly()&&canno.isTightMatch()){
					stats.addValue(canno.getEvidence().getConfidence());
				}
			}
		}
		return stats;
	}

	/**
	 * Get description of confidence estimates for all predicted annotations
	 * @param cannos
	 * @return
	 */
	public static DescriptiveStatistics confidenceForAll(List<CandidateAnnotation> cannos){
		DescriptiveStatistics stats = new DescriptiveStatistics();
		//only count each pair once (regardless of mapping methods)
		Set<String> unique = new HashSet<String>();
		for(CandidateAnnotation canno : cannos){
			if(unique.add(canno.getEntrez_gene_id()+canno.getTarget_accession())){
				stats.addValue(canno.getEvidence().getConfidence());
			}
		}
		return stats;
	}

	/**
	 * Get the average confidence for the known annotations (the 'true positives') 
	 * @param cannos
	 * @param stringency
	 * @return
	 */
	public static double confidenceForKnownByRank(List<CandidateAnnotation> cannos, String stringency){
		DescriptiveStatistics stats = new DescriptiveStatistics();
		//only count each pair once (regardless of mapping methods)
		Set<String> unique = new HashSet<String>();
		int index = 1;
		for(CandidateAnnotation canno : cannos){
			if(unique.add(canno.getEntrez_gene_id()+canno.getTarget_accession())){
				if(canno.getEvidence().isMatches_existing_annotation_directly()){
					if(stringency.equals("tight")){
						if(canno.isTightMatch()){
							stats.addValue(index);
						}
					}else if(stringency.equals("medium")){
						if(canno.isSmartMatch()){
							stats.addValue(index);
						}
					}else{
						stats.addValue(index);
					}
				}
			}
			index++;
		}
		System.out.println("N known: "+stats.getN());
		return stats.getMean()/index;
	}


	public static double liftForKnown(List<CandidateAnnotation> cannos){
		return confidenceForTightKnown(cannos).getMean()-confidenceForAll(cannos).getMean();
	}

	public static void fractionTopNplot(
			List<CandidateAnnotation> testcannos, String stringency,
			String outfile) {
		try {
			FileWriter out = new FileWriter(outfile);
			for(double i=1; i<10; i++){
				double cutpoint = i/10;
				double f = fractionTopN(testcannos, stringency, cutpoint);
				double cp = (double)testcannos.size()-(double)testcannos.size()*cutpoint;
				out.write(cp+"\t"+(1-cutpoint)+"\t"+f+"\n");
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void fractionTopNROC(
			List<CandidateAnnotation> cannos, String outfile, String go_acc_skip) {
		try {
			Collections.reverse(cannos);
			FileWriter out = new FileWriter(outfile);
			float ymax = 0; float xmax = cannos.size();
			float y = 0;
			float x = 0;
			for(CandidateAnnotation canno : cannos){
				if(!canno.getTarget_accession().equals(go_acc_skip)){
					//loose, exact match no reasoning = true positive 
					if(canno.getEvidence().isMatches_existing_annotation_directly()){
						ymax++;
					}
				}
			}
			out.write("FP\tTP\n");
			for(CandidateAnnotation canno : cannos){
				//loose, exact match no reasoning = true positive 
				if(!canno.getTarget_accession().equals(go_acc_skip)){
					if(canno.getEvidence().isMatches_existing_annotation_directly()){
						y++;
					}
					out.write(x/xmax+"\t"+y/ymax+"\n");
					x++;
				}
			}


			System.out.println("y is "+y+" for ROC");
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static double liftForKnownByRank(List<CandidateAnnotation> cannos, String stringency){
		Collections.sort(cannos);
		Collections.reverse(cannos);
		return confidenceForKnownByRank(cannos, stringency);
	}

	public static double fractionTopN(List<CandidateAnnotation> cannos, String stringency, double cutpoint){
		Collections.sort(cannos);
		//only count each pair once (regardless of mapping methods)
		Set<String> unique = new HashSet<String>();
		int index = 0;
		double middle = cannos.size()*cutpoint;
		double above = 0; double yes = 0;
		for(CandidateAnnotation canno : cannos){
			if(unique.add(canno.getEntrez_gene_id()+canno.getTarget_accession())){
				if(canno.getEvidence().isMatches_existing_annotation_directly()){
					if(stringency.equals("tight")){
						if(canno.isTightMatch()){
							yes++;
							if(index<=middle){
								above++;
							}
						}
					}else if(stringency.equals("medium")){
						if(canno.isSmartMatch()){
							yes++;
							if(index<=middle){
								above++;
							}
						}
					}else{
						yes++;
						if(index<=middle){
							above++;
						}
					}
				}
				index++;
			}

		}
		//		System.out.println(cutpoint+" middle "+middle+" above "+above+" total TP "+yes);
		return above/yes;
	}


	public static void randomBatchTest(List<CandidateAnnotation> testcannos, int n, String stringency){
		double mean_lift = 0; double mean_tophalf = 0;
		for(int i =0; i<n; i++){
			testcannos = setRandomConfidence(testcannos);
			mean_lift += liftForKnownByRank(testcannos, stringency);
			mean_tophalf += fractionTopN(testcannos, stringency, 0.5);
		}
		System.out.println("N "+n+" mean_rank_lift "+mean_lift/n+" "+mean_tophalf/n);
	}
}
