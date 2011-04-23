package org.gnf.umls;
import java.io.BufferedReader;
import java.io.File;
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

import org.gnf.genewiki.Config;
import org.gnf.genewiki.GeneWikiLink;
import org.gnf.genewiki.GeneWikiPage;
import org.gnf.genewiki.GeneWikiUtils;
import org.gnf.genewiki.associations.CandidateAnnotation;
import org.gnf.go.GOmapper;
import org.gnf.go.GOowl;
import org.gnf.go.GOterm;


public class UMLSmapper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	}

	public static Map<String, List<UmlsRelationship>> getGene2Umls(){
		String umlsdir = "C:/Users/bgood/data/genewiki/umls_tab/";
		String file = Config.article_gene_map_file;
		String delimiter = "\t";
		String skipprefix = "#";
		boolean reverse = true;
		Map<String, Set<String>> article_geneid = GeneWikiUtils.readMapFile(file, delimiter, skipprefix, reverse);
		System.out.println("read article gene map ");

		//load all serialized page objects
		int limit = 100000;
		List<GeneWikiPage> pages = GeneWikiUtils.loadSerializedDir(Config.gwikidir, limit);
		System.out.println("read in wikigene files");

		//prepare output
		Map<String, List<UmlsRelationship>> linkRel = new HashMap<String, List<UmlsRelationship>>();
		float link_count = 0;
		float mapped_count = 0;
		
		for(GeneWikiPage page : pages){
			List<GeneWikiLink> links = page.getGlinks();
			// # links for this article
			link_count += links.size();
			String title = page.getTitle();
			if(article_geneid.get(title) == null){
				continue;
			}else{
				for(String geneid : article_geneid.get(title)){
				for(GeneWikiLink link : links){
					//look up relations if any
					List<UmlsRelationship> rels = getRelations(umlsdir+link.getTarget_page());
					if(rels!=null&&rels.size()>0){
						linkRel.put(geneid, rels);
					}
				}
				}
			}
		}
		
		return linkRel;
	}
	
	public static void mapDisease(){
/*		//gather and count number of genes that link to a disease page		
		//for each of these links, check if there is any direct support in the  UMLS
		String outfile = "C:/Users/bgood/data/genewiki/disease/umlsMap";

		//things to catch
		float avg_links_article = 0; float avg_matched_links_article = 0;
		float article_count = 0; float link_count = 0; float link_match_count = 0;
		float avg_disease_gene = 0;
	
		//read in article_gene map;
		String file = GOmapper.article_gene_map_file;
		String delimiter = "\t";
		String skipprefix = "#";
		boolean reverse = true;
		//could be more than one!
		Map<String, Set<String>> article_geneid = GeneWikiUtils.readMapFile(file, delimiter, skipprefix, reverse);
		System.out.println("read article gene map ");

		//load all serialized page objects
		int limit = 100000;
		List<GeneWikiPage> pages = GeneWikiUtils.loadSerializedDir(GOmapper.gwikidir, limit);
		System.out.println("read in wikigene files");

		//prepare output
		List<CandiAnnoFromLink> cannolist = new ArrayList<CandiAnnoFromLink>();
		FileWriter writer = new FileWriter(outfile);
		writer.write(CandiAnnoFromLink.getHeader());

		for(GeneWikiPage page : pages){
			GeneWikiPageData data = page.getData();
			article_count++;
			List<GeneWikiLink> links = data.getGlinks();
			// # links for this article
			link_count += links.size();

			String title = data.getTitle();
			if(article_geneid.get(title) == null){
				continue;
			}else{	

				for(String geneid : article_geneid.get(title)){
					List<UmlsRelationship> goAs = geneid_go.get(geneid);
					if(goAs==null){
						articles_with_gene_no_go++;
					}else{
						avg_gos_gene+=goAs.size();
					}
					if(links !=null){
						for(GeneWikiLink glink : links){

							if(glink.getTitle().equals("protein")){
								continue;
							}
							// # links that match any go term
							String maybego = glink.getTitle();
							Set<GOterm> maybegos = page_gos.get(maybego);
							if(maybegos!=null){
								for(GOterm matchgo : maybegos){										
									//record candidate annotation
									//data about page source
									CandiAnnoFromLink canno = new CandiAnnoFromLink();
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
									canno.setTarget_wiki_page_title(glink.getTitle());
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
												if(go.isInferred()){
													canno.setMatches_parent_of_existing_annotation(true);
												}else{
													canno.setMatches_existing_annotation_directly(true);
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
												if(go.isInferred()){
													canno.setMatches_parent_of_panther_go(true);
												}else{
													canno.setMatches_panther_go_directly(true);
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
*/

	}
	
	public static void summarizeUMLS(){
		int limit = 1000000;
		String datadir = "C:\\Users\\bgood\\data\\genewiki\\umls_tab\\";
		Set<GeneWikiPage> links = GeneWikiUtils.getNRlinks(limit, Config.gwikidir);
		float total_pages = links.size();
		System.out.println("total to process: "+total_pages);

		//check what we've got already
		File folder = new File(datadir);
		Map<String, List<UmlsRelationship>> page_umls = new HashMap<String, List<UmlsRelationship>>();
		for(String name : folder.list()){
			List<UmlsRelationship> rels = page_umls.get(name);
			if(rels==null){
				rels = new ArrayList<UmlsRelationship>();
			}
			try {
				BufferedReader f = new BufferedReader(new FileReader(datadir+name));
				String line = f.readLine();
				while(line!=null){
					UmlsRelationship r = null;
					String[] data = line.split("\t");
					String source = data[0];
					if(data[1].equals("no exact match")){
						r = new UmlsRelationship("none", null, null, null, null);
					}else{
						String vocab = data[1].split(":")[0];
						String reltype = data[1].split(":")[1];
						String target = data[2];
						String target_text = data[3];
						r = new UmlsRelationship(source, reltype, vocab, target, target_text);
					}
					rels.add(r);
					page_umls.put(name, rels);
					line = f.readLine();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		System.out.println("processed "+page_umls.size());

		float total_processed = page_umls.size();
		float total_pages_with_match = 0;
		float total_processed_no_match = 0;
		
		Map<String, Float> group_counts = new HashMap<String, Float>();	
		Map<String, Float> type_counts = new HashMap<String, Float>();
		Map<String, Float> vocab_counts = new HashMap<String, Float>();
		Map<String, Float> relation_counts = new HashMap<String, Float>();
		
		Set<String> distinct_go_match = new HashSet<String>();
		Set<String> go_pairs = new HashSet<String>();
		
		for(GeneWikiPage data : links ){
			if(page_umls.containsKey(data.getTitle())){
				List<UmlsRelationship> rels = page_umls.get(data.getTitle());
				if(rels.size()==1&&rels.get(0).getSource_concept_id().equals("none")){
					total_processed_no_match++;
				}else{
					total_pages_with_match++;
					for(UmlsRelationship rel : rels){
						if(rel.getRelationship().equals("semantic_group")){
							Float gc = group_counts.get(rel.getTarget_concept_name());
							if(gc==null){
								gc = new Float(1);
								group_counts.put(rel.getTarget_concept_name(), gc);
							}else{
								gc++;
								group_counts.put(rel.getTarget_concept_name(), gc);
							}
						}else if(rel.getRelationship().equals("semantic_type")){
							Float tc = type_counts.get(rel.getTarget_concept_name());
							if(tc==null){
								tc = new Float(1);
								type_counts.put(rel.getTarget_concept_name(), tc);
							}else{
								tc++; 
								type_counts.put(rel.getTarget_concept_name(), tc);
							}
						}
						if(rel.getVocab().equals("GO")){
							distinct_go_match.add(data.getTitle());
							go_pairs.add(data.getTitle()+rel.getTarget_concept_id());
							//System.out.println(" GO match "+data.getTitle()+" "+rel.getTarget_concept_name());
						}
						
						Float vc = vocab_counts.get(rel.getVocab());
						if(vc==null){
							vc = new Float(1);
							vocab_counts.put(rel.getVocab(), vc);
						}else{
							vc++; 
							vocab_counts.put(rel.getVocab(), vc);
						}
						Float rc = relation_counts.get(rel.getRelationship());
						if(rc==null){
							rc = new Float(1);
							relation_counts.put(rel.getRelationship(), rc);
						}else{
							rc++;
							relation_counts.put(rel.getRelationship(), rc);
						}
					}
				}
			}
		}

		System.out.println("total processed: "+total_processed+" total pages with a match: "+total_pages_with_match+" ("+total_pages_with_match/total_processed+")"+
				" total processed pages with no match: "+total_processed_no_match+" ("+total_processed_no_match/total_processed+")");
		
		System.out.println("distinct pages that matched a GO term: "+distinct_go_match.size()+" sample: ");
		
		System.out.println("\nGroup\tCount");
		for(Entry<String, Float> entry: group_counts.entrySet()){
			System.out.println(entry.getKey()+"\t"+entry.getValue());
		}
		System.out.println("\nType\tCount");
		for(Entry<String, Float> entry: type_counts.entrySet()){
			System.out.println(entry.getKey()+"\t"+entry.getValue());
		}
		System.out.println("\nVocab\tCount");
		for(Entry<String, Float> entry: vocab_counts.entrySet()){
			System.out.println(entry.getKey()+"\t"+entry.getValue());
		}
		System.out.println("\nRelation\tCount");
		for(Entry<String, Float> entry: relation_counts.entrySet()){
			System.out.println(entry.getKey()+"\t"+entry.getValue());
		}
	}
	
	public static List<UmlsRelationship> getRelations(String filename){
		List<UmlsRelationship> rels = new ArrayList<UmlsRelationship>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(filename));
			String line = f.readLine();
			while(line!=null){
				UmlsRelationship r = null;
				String[] data = line.split("\t");
				String source = data[0];
				if(data[1].equals("no exact match")){
					//r = new UmlsRelationship("none", null, null, null, null);
				}else{
					String vocab = data[1].split(":")[0];
					String reltype = data[1].split(":")[1];
					String target = data[2];
					String target_text = data[3];
					r = new UmlsRelationship(source, reltype, vocab, target, target_text);
				}
				rels.add(r);
				line = f.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rels;
	}
}
