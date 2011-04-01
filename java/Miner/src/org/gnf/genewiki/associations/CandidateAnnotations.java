package org.gnf.genewiki.associations;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gnf.genewiki.Config;
import org.gnf.go.GOowl;
import org.gnf.ncbo.Ontologies;
import org.gnf.util.BioInfoUtil;
import org.gnf.util.Gene;

public class CandidateAnnotations {

	List<CandidateAnnotation> cannos;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CandidateAnnotations c = new CandidateAnnotations();
		c.loadCandidateAnnotations("/Users/bgood/Desktop/testannos.txt");
		System.out.println("all "+c.cannos.size());
		System.out.println("all "+c.cannos.size());
		//		c.loadHighConfidenceCandidateGOAnnotations(Config.merged_mined_ranked_annos_go);
		//		System.out.println("all "+c.cannos.size());
		//		c.filterPBB();
		//		System.out.println("no pbb "+c.cannos.size());
		//		c.filterCommonGOMissMappings();
		//		System.out.println("filtered common errors "+c.cannos.size());
		//		c.filterRedirects();
		//		System.out.println("filtered redirects "+c.cannos.size());
		//		c.filterTitleSentence();
		//		System.out.println("filtered occurrences in title sentences "+c.cannos.size());
		//		c.tagProvenance();
		//		System.out.println("tagged provenance info. "+c.cannos.size());
		//		c.cannos = CandiAnnoSorter.addAllEvidenceForGOAnnotations(c.cannos);
		//		System.out.println("added evidence "+c.cannos.size());
		//		c.writeGOCandiListWithEvidence(Config.merged_mined_ranked_filtered_annos_go);
	}

	public void filterKnownHuman(){
		List<CandidateAnnotation> filtered = new ArrayList<CandidateAnnotation>();
		for(CandidateAnnotation canno : cannos){
			if(!canno.getEvidence().isMatches_existing_annotation_directly()&&
					!canno.getEvidence().isMatches_parent_of_existing_annotation()){
				filtered.add(canno);
			}
		}
		setCannos(filtered);
	}

	public void filterTitleSentence(){
		List<CandidateAnnotation> filtered = new ArrayList<CandidateAnnotation>();
		for(CandidateAnnotation canno : cannos){
			if(!canno.getParagraph_around_link().trim().startsWith("'''")){
				filtered.add(canno);
			}
		}
		setCannos(filtered);
	}

	public void filterPBB(){
		List<CandidateAnnotation> filtered = new ArrayList<CandidateAnnotation>();
		for(CandidateAnnotation canno : cannos){
			if(!canno.isPBB){
				filtered.add(canno);
			}
		}
		setCannos(filtered);
	}

	public void tagProvenance(){
		for(CandidateAnnotation canno : cannos){
			String ev = canno.getString_matching_method();
			if(ev.contains("ncbo")){
				if(ev.contains("metamap")){
					canno.setAnnotationTool("ncbo_metamap");
				}else{
					canno.setAnnotationTool("ncbo");
				}
			}else{
				canno.setAnnotationTool("metamap");
			}
			if(ev.contains("text")){
				if(ev.contains("title")){
					canno.setAnnotationMethod("text_title");
				}else if(ev.contains("redirect")){
					canno.setAnnotationMethod("text_redirect");
				}else{
					canno.setAnnotationMethod("text");
				}
			}else{
				if(ev.contains("title")){
					canno.setAnnotationMethod("title");
				}else if(ev.contains("redirect")){
					canno.setAnnotationMethod("redirect");
				}
			}
		}
	}

	public void filterRedirects(){
		List<CandidateAnnotation> filtered = new ArrayList<CandidateAnnotation>();
		for(CandidateAnnotation canno : cannos){
			String ev = canno.getString_matching_method();
			if(!ev.contains("redirect")){
				filtered.add(canno);
			}
		}
		setCannos(filtered);
	}

	public void filterCommonGOMissMappings(){
		List<CandidateAnnotation> filtered = new ArrayList<CandidateAnnotation>();
		for(CandidateAnnotation canno : cannos){
			String term = canno.getTarget_preferred_term();
			if(!term.equalsIgnoreCase("Splicing")&&
					!term.equalsIgnoreCase("Birth")&&
					!term.equalsIgnoreCase("chromosome")&&
					!term.equalsIgnoreCase("Reproduction")&&
					!term.equalsIgnoreCase("Antibodies")&&
					!term.equalsIgnoreCase("virus")&&
					!term.equalsIgnoreCase("SerA")&&
					!term.equalsIgnoreCase("soma")&&
					!term.equalsIgnoreCase("Nucleic")&&
					!term.equalsIgnoreCase("\"RNA splicing, via endonucleolytic cleavage and ligation\"")&&
					!term.equalsIgnoreCase("Group II intron splicing")&&
					!term.equalsIgnoreCase("Group III intron splicing")&&
					!term.equalsIgnoreCase("Group I intron splicing")&&
					!term.equalsIgnoreCase("\"nuclear mRNA splicing, via spliceosome\"")&&
					!term.equalsIgnoreCase("seeing")){
				filtered.add(canno);
			}
		}
		setCannos(filtered);
	}

	/**
	 * Reads the Disease Ontology anntotations from an annotation file that may contain annotations from many different
	 * ontologies.  Filters out annotations from terms we disapprove of or from wikitext sections that we want to ignore.
	 * Ensures that the annotations are not redundant
	 * @param file
	 */
	public void loadAndFilterCandidateDOAnnotations(String file){
		cannos = new ArrayList<CandidateAnnotation>();
		Set<String> unique_annos = new HashSet<String>();
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(file));
			String line = f.readLine();
			line = f.readLine();//skip header
			while(line!=null){
				String[] item = line.split("\t");
				String acc = item[9];
				if(acc.startsWith(Ontologies.HUMAN_DISEASE_ONT)){// //"44171/")
					CandidateAnnotation canno = new CandidateAnnotation(item);
					canno.setTarget_vocabulary("Disease Ontology");
					canno.setVocabulary_branch("disease");
					//remove unwanted matches
					if(!Filter.fromUnwantedWikiSection(canno)
							&&!Filter.containsUnwantedDOTerm(canno)){
						canno.setTarget_accession(acc.substring(Ontologies.HUMAN_DISEASE_ONT.length()+1));
						if(unique_annos.add(canno.getEntrez_gene_id()+canno.getTarget_accession())){
							cannos.add(canno);
						}
					}
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
	}

/**
 * Reads a candidate annotation text file and filters out non-GO annotations as well as annotations that are from unwanted areas of the gene wiki page
 * or that include uninteresting GO terms
 * @param file
 */
	public void loadAndFilterCandidateGOAnnotations(String file){
		cannos = new ArrayList<CandidateAnnotation>();
		Set<String> unique_annos = new HashSet<String>();		
		GOowl gol = new GOowl();
		gol.initFromFile(false);
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(file));
			String line = f.readLine();
			line = f.readLine();//skip header
			while(line!=null){
				String[] item = line.split("\t");
				String acc = item[9];
				if(acc.startsWith(Ontologies.GO_ONT)||acc.startsWith("GO:")){// //"44171/")
					CandidateAnnotation canno = new CandidateAnnotation(item);
					String a = acc.substring(Ontologies.GO_ONT.length()+4);
					if(acc.startsWith("GO:")){
						a = acc.substring(3);
					}
					a = Filter.replaceOldGoAcc(a);
					//remove unwanted matches
					if(!Filter.fromUnwantedWikiSection(canno)
							&&!Filter.containsUnwantedGOTerm(canno)){		
						if(unique_annos.add(canno.getEntrez_gene_id()+canno.getTarget_accession())){
							canno.setTarget_accession("GO:"+a);
							canno.setVocabulary_branch(gol.getGoBranch(a));
							cannos.add(canno);
						}
					}
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
	}

	/**
	 * Reads a candidate annotation text file and filters out non-FMA annotations as well as annotations that are from unwanted areas of the gene wiki page
	 * or that include uninteresting FMA terms
	 * @param file
	 */
	public void loadAndFilterCandidateFMAAnnotations(String file){
		cannos = new ArrayList<CandidateAnnotation>();
		Set<String> unique_annos = new HashSet<String>();
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(file));
			String line = f.readLine();
			line = f.readLine();//skip header
			while(line!=null){
				String[] item = line.split("\t");
				String acc = item[9];
				if(acc.startsWith(Ontologies.FMA_ONT)){// //"44171/")
					CandidateAnnotation canno = new CandidateAnnotation(item);
					canno.setTarget_vocabulary("FMA");
					canno.setVocabulary_branch("Anatomy");
					//remove unwanted matches
					if(!Filter.fromUnwantedWikiSection(canno)
							&&!Filter.containsUnwantedFMATerm(canno)){
						canno.setTarget_accession(acc.substring(Ontologies.FMA_ONT.length()+1));
						if(unique_annos.add(canno.getEntrez_gene_id()+canno.getTarget_accession())){
							cannos.add(canno);
						}
					}
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
	}
	
	/***
	 * Load annotations from tab-delimited file
	 * @param file
	 */
	public void loadCandidateAnnotations(String file){
		cannos = new ArrayList<CandidateAnnotation>();
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(file));
			String line = f.readLine();
			line = f.readLine();//skip header
			while(line!=null){
				String[] item = line.split("\t");
				cannos.add(new CandidateAnnotation(item));
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
	}


	public void loadHighConfidenceCandidateGOAnnotations(String file){
		cannos = new ArrayList<CandidateAnnotation>();
		GOowl gol = new GOowl();
		gol.initFromFile(false);
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(file));
			String line = f.readLine();
			line = f.readLine();//skip header
			while(line!=null){
				String[] item = line.split("\t");
				String acc = item[4];
				if(acc.startsWith(Ontologies.GO_ONT)||acc.startsWith("GO:")){// //"44171/")
					CandidateAnnotation canno = new CandidateAnnotation(item);
					//skip loading 'cell' and 'protein'
					//skip 'further reading'
					if(!(acc.contains("GO:0005623")||acc.contains("GO:0003675")||
							canno.getSection_heading().equalsIgnoreCase("Further reading")))
					{
						String a = acc.substring(Ontologies.GO_ONT.length()+4);
						if(acc.startsWith("GO:")){
							a = acc.substring(3);
						}
						//double check and get rid of all obsolete terms
						if(!gol.isGoTermObsolete(acc)){
							//fix 'same as' terms..
							if(a.equals("0031202")){
								a = "0000375";
							}else if(a.equals("0000119")){
								a = "0016592";
							}else if(a.equals("0008367")){
								a = "0051635";
							}else if(a.equals("0015978")){
								a = "0015976";
							}else if(a.equals("0007025")){
								a = "0006457";
							}else if(a.equals("0007242")){
								a = "0023034";
							}else if(a.equals("0019642")){
								a = "0006096";
							}
							canno.setTarget_accession("GO:"+a);
							canno.setVocabulary_branch(gol.getGoBranch(a));
							cannos.add(canno);
						}
					}
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
	}

	/**
	 * Write out this list
	 * @param file
	 */
	public void writeCandiListWithEvidence(String file){
		try {
			FileOutputStream fos = new FileOutputStream(file);
			OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8"); 
			out.write(CandidateAnnotation.getHeader()+Evidence.getHeaderString()+"\n");
			for(CandidateAnnotation candi : this.getCannos()){
				out.write(candi.toString()+candi.getEvidence().toString()+"\n");
			}
			out.close();
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	/**
	 * Write out this list
	 * @param file
	 */
	public void writeGOCandiListWithEvidence(String file){
		try {
			FileOutputStream fos = new FileOutputStream(file);
			OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8"); 
			out.write(CandidateAnnotation.getHeader()+Evidence.getGOheaderString()+"\n");
			for(CandidateAnnotation candi : this.getCannos()){
				out.write(candi.toString()+candi.getEvidence().toGOstring()+"\n");
			}
			out.close();
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	


	/**
	 * Set wiki page titles to GeneSymbols based on Entrez Gene ID
	 */
	public void addWikiTitles(){
		Map<String, Gene> gi = BioInfoUtil.loadHumanGeneInfo("geneid");
		for(CandidateAnnotation canno : this.getCannos()){
			canno.setSource_wiki_page_title(gi.get(canno.getEntrez_gene_id()).getGeneSymbol());
		}
	}

	/***
	 * Prepare summary report on candidate annotations
	 * @return
	 */

	public void summarizeAnnotations(){
		//count
		float n_human_exact = 0; float n_human_parent = 0; float n_panther_exact = 0; float n_panther_parent = 0;
		float n_funcbase = 0;
		float n_any = 0;
		float n_any_human_or_panther = 0;
		float n_new_supported = 0;
		float n = 0;
		float n_genego_exact = 0;
		float n_genego_inferred = 0;
		float n_goa_child = 0;

		float n_new_panther_any = 0;
		float n_new_funcbase_any = 0;
		float n_new_genego_any = 0;
		float n_new = 0;
		float n_new_well_supported = 0;

		Set<String> u = new HashSet<String>();
		for(CandidateAnnotation canno : this.getCannos()){
			if(u.add(canno.getEntrez_gene_id()+canno.getTarget_accession())){
				n++;
				//for the novel ones
				if(!(canno.getEvidence().isMatches_existing_annotation_directly()||
						canno.getEvidence().isMatches_parent_of_existing_annotation())){
					n_new++;					
					if(canno.getEvidence().isMatches_panther_go_directly()||canno.getEvidence().isMatches_parent_of_panther_go()){
						n_new_panther_any++;
					}
					if(canno.getEvidence().isMatches_genego_directly()||canno.getEvidence().isMatches_parent_of_genego()){
						n_new_genego_any++;
					}
					if(canno.getEvidence().getFuncbase_score()>0){
						n_new_funcbase_any++;
					}
					if(canno.getEvidence().isMatches_panther_go_directly()||
							canno.getEvidence().isMatches_parent_of_panther_go()||
							canno.getEvidence().getFuncbase_score()>0||
							canno.getEvidence().isMatches_genego_directly()|| 
							canno.getEvidence().isMatches_parent_of_genego()){
						n_new_supported++;
					}
					if((canno.getEvidence().isMatches_panther_go_directly()||
							canno.getEvidence().isMatches_parent_of_panther_go()) &&
							canno.getEvidence().getFuncbase_score()>0 &&
							(canno.getEvidence().isMatches_genego_directly()|| 
									canno.getEvidence().isMatches_parent_of_genego())){
						n_new_well_supported++;
						System.out.println(canno);
					}
				}


				if(canno.getEvidence().isMatches_existing_annotation_directly()||
						canno.getEvidence().isMatches_panther_go_directly()||
						canno.getEvidence().isMatches_parent_of_existing_annotation()||
						canno.getEvidence().isMatches_parent_of_panther_go()||
						canno.getEvidence().isMatches_child_of_existing_annotation()){
					n_any_human_or_panther++;
				}
				if(canno.getEvidence().isMatches_existing_annotation_directly()||
						canno.getEvidence().isMatches_panther_go_directly()||
						canno.getEvidence().isMatches_parent_of_existing_annotation()||
						canno.getEvidence().isMatches_child_of_existing_annotation()||
						canno.getEvidence().isMatches_parent_of_panther_go()||
						canno.getEvidence().getFuncbase_score()!=0||
						canno.getEvidence().isMatches_genego_directly()||
						canno.getEvidence().isMatches_parent_of_genego()){
					n_any++;
				}

				if(canno.getEvidence().isMatches_child_of_existing_annotation()){
					n_goa_child++;
				}

				if(canno.getEvidence().isMatches_existing_annotation_directly()){
					n_human_exact++;
				}
				if(canno.getEvidence().isMatches_parent_of_existing_annotation()){
					n_human_parent++;
				}
				if(canno.getEvidence().isMatches_panther_go_directly()){
					n_panther_exact++;
				}
				if(canno.getEvidence().isMatches_parent_of_panther_go()){
					n_panther_parent++;
				}
				if(canno.getEvidence().getFuncbase_score()!=0){
					n_funcbase++;
				}
				if(canno.getEvidence().isMatches_genego_directly()){
					n_genego_exact++;
				}
				if(canno.getEvidence().isMatches_parent_of_genego()){
					n_genego_inferred++;
				}



			}
		}
		System.out.println("n\t"+n);
		System.out.println("n_any_support\t"+n_any+"\t"+n_any/n);
		System.out.println("n_funcbase_support\t"+n_funcbase+"\t"+n_funcbase/n);
		System.out.println("n_any_human_or_panther\t"+n_any_human_or_panther+"\t"+n_any_human_or_panther/n);
		System.out.println("n_human_support\t"+(n_human_exact+n_human_parent)+"\t"+(n_human_exact+n_human_parent)/n);
		System.out.println("n_panther_support\t"+(n_panther_exact+n_panther_parent)+"\t"+(n_panther_exact+n_panther_parent)/n);
		System.out.println("n_genego_support\t"+(n_genego_exact+n_genego_inferred)+"\t"+(n_genego_exact+n_genego_inferred)/n);
		System.out.println("n not in GOA but with some support\t"+n_new_supported+"\t"+n_new_supported/n);
		System.out.println("n not in GOA but matches child\t"+n_goa_child+"\t"+n_goa_child/n);
		System.out.println("n novel\t"+n_new);
		System.out.println("n novel with panther support\t"+n_new_panther_any+"\t"+n_new_panther_any/n_new);
		System.out.println("n novel with GeneGO support\t"+n_new_genego_any+"\t"+n_new_genego_any/n_new);
		System.out.println("n novel with funcbase support\t"+n_new_funcbase_any+"\t"+n_new_funcbase_any/n_new);
		System.out.println("n novel with any support\t"+n_new_supported+"\t"+n_new_supported/n_new);
		System.out.println("n novel with support fromn all\t"+n_new_well_supported+"\t"+n_new_well_supported/n_new);
	}



	public List<CandidateAnnotation> getCannos() {
		return cannos;
	}

	public void setCannos(List<CandidateAnnotation> cannos) {
		this.cannos = cannos;
	}


}
