/**
 * 
 */
package org.gnf.genewiki;

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
import java.util.Map.Entry;
import java.util.Set;

import org.gnf.genewiki.ncbi.PubMed;
import org.gnf.go.Annotation;
import org.gnf.go.GOterm;
import org.gnf.util.MapFun;

/**
 * @author bgood
 *
 */
public class Prioritizer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String gene = "7078";
		String pmids = "";
//filterMegaGenePubs(
		
		Map<String, List<String>> gene2pub = getGene2pub("/Users/bgood/data/gene2pubmed");
		List<String> g2p_list = gene2pub.get(gene);
		System.out.println(gene2pub.get(gene).size());
		for(String p : gene2pub.get(gene)){
			pmids+=p+",";
		}
//		Map<String, List<String>> gene2pub = new HashMap<String, List<String>>();
		List<String> ps = new ArrayList<String>();

		BufferedReader f;
		float in = 0; 
		try {
			f = new BufferedReader(new FileReader("/Users/bgood/data/TIMP3-2.txt"));
			String line = f.readLine();
			while(line!=null){
				String id = line.trim();
				pmids+=id+",";
				ps.add(id);
				line = f.readLine();
				if(g2p_list.contains(id)){
					in++;
				}
			}
			System.out.println("gene2pubmed "+g2p_list.size()+" intersect "+in+" live pubmed "+ps.size());
			f.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
// 		gene2pub.put(gene, ps);		



		Map<String, List<String>> pmid_authors = PubMed.getPubmedAuthors(pmids);
		if(pmid_authors!=null){
			Map<String, Integer> auth_count = new HashMap<String, Integer>();
			for(String pub : gene2pub.get(gene)){
				if(pmid_authors.get(pub)!=null){
					for(String author : pmid_authors.get(pub)){
						//									f.write(author+", ");
						if(auth_count.containsKey(author)){
							auth_count.put(author, auth_count.get(author)+1);
						}else{
							auth_count.put(author, 1);
						}
					}
				}
			}
			String top_auth = ""; int max_auth = 0;
			for(Entry<String, Integer> author_c : auth_count.entrySet()){
				System.out.println(author_c.getKey()+"\t"+author_c.getValue());
				if(author_c.getValue()> max_auth){
					top_auth = author_c.getKey()+"\t"+author_c.getValue();
					max_auth = author_c.getValue();
				}
			}
			System.out.println("\t\t"+top_auth+"\t");
		}		
	}


	public static void getHighPriority(){
		Map<String, List<String>> gene2pub = filterMegaGenePubs(getGene2pub("/Users/bgood/data/gene2pubmed"), 5);
		HashMap<String, Set<GOterm>> gene2go = Annotation.readGene2GO("/Users/bgood/data/human_gene2go.txt", false);
		Map<String, Float> gene2words = getGeneWiki2Words("/Users/bgood/data/genewiki_jan11_volume.txt");

		try {
			FileWriter f = new FileWriter("/Users/bgood/data/weka_wiki_counts_after_2005.txt");
			f.write("gene\tpmids\tgos\twikiwords\tauthors\top_author\tjournals\ttop_journal\n");
			int n = 0;
			for(String gene : gene2pub.keySet()){
				if(gene2pub.get(gene).size()>100){
					if(gene2words.get(gene)==null||gene2words.get(gene)<100){
						n++;
						//						if(n == 5){
						//							break;
						//						}
						f.write(gene+"\t"+gene2pub.get(gene).size()+"\t");						
						if(gene2go.get(gene)!=null){
							f.write(""+gene2go.get(gene).size());
						}else{
							f.write("0");
						}
						f.write("\t"+gene2words.get(gene)+"\t");
						String pmids = "";
						for(String p : gene2pub.get(gene)){
							pmids+=p+",";
						}
						Map<String, String> pmid_art_type = PubMed.getPubmedArticleTypes(pmids);
						if(pmid_art_type!=null){
							Map<String, Integer> j_count = new HashMap<String, Integer>();
							for(String pub : gene2pub.get(gene)){
								String art_type = pmid_art_type.get(pub);
								//now only reviews
								if(art_type.equals("Review")){
									//now limit to reviews in past 5 years
									String date = PubMed.getPubmedDate(pub);
									if(date.length()>4){
										int year = Integer.parseInt(date.substring(0,4));
										if(year>2005){
											if(j_count.containsKey(art_type)){
												j_count.put(art_type, j_count.get(art_type)+1);
											}else{
												j_count.put(art_type, 1);
											}
										}
									}
								}
							}
							String top_j = ""; int max_j = 0;
							for(Entry<String, Integer> art_type_c : j_count.entrySet()){
								f.write(art_type_c.getKey()+"|"+art_type_c.getValue()+",");
								if(art_type_c.getValue()> max_j){
									top_j = art_type_c.getKey()+"|"+art_type_c.getValue();
									max_j = art_type_c.getValue();
								}
							}							
							f.write("\t"+top_j+"\t");
						}				
						f.write("\n");
						System.out.println("Found "+n+" weak wikis ");
					}
				}
			}
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void getPriorityData(){
		Map<String, List<String>> gene2pub = getGene2pub("/Users/bgood/data/gene2pubmed");
		HashMap<String, Set<GOterm>> gene2go = Annotation.readGene2GO("/Users/bgood/data/human_gene2go.txt", false);
		Map<String, Float> gene2words = getGeneWiki2Words("/Users/bgood/data/genewiki_jan11_volume.txt");

		try {
			FileWriter f = new FileWriter("/Users/bgood/data/weka_wiki_genecounts.txt");
			f.write("gene\tpmids\tgos\twikiwords\tauthors\top_author\tjournals\ttop_journal\n");
			int n = 0;
			for(String gene : gene2pub.keySet()){
				if(gene2pub.get(gene).size()>100){
					if(gene2words.get(gene)==null||gene2words.get(gene)<100){
						n++;
						f.write(gene+"\t"+gene2pub.get(gene).size()+"\t");						
						if(gene2go.get(gene)!=null){
							f.write(""+gene2go.get(gene).size());
						}else{
							f.write("0");
						}
						f.write("\t"+gene2words.get(gene)+"\t");
						String pmids = "";
						for(String p : gene2pub.get(gene)){
							pmids+=p+",";
						}
						Map<String, List<String>> pmid_authors = PubMed.getPubmedAuthors(pmids);
						if(pmid_authors!=null){
							Map<String, Integer> auth_count = new HashMap<String, Integer>();
							for(String pub : gene2pub.get(gene)){
								if(pmid_authors.get(pub)!=null){
									for(String author : pmid_authors.get(pub)){
										//									f.write(author+", ");
										if(auth_count.containsKey(author)){
											auth_count.put(author, auth_count.get(author)+1);
										}else{
											auth_count.put(author, 1);
										}
									}
								}
							}
							String top_auth = ""; int max_auth = 0;
							for(Entry<String, Integer> author_c : auth_count.entrySet()){
								f.write(author_c.getKey()+"|"+author_c.getValue()+",");
								if(author_c.getValue()> max_auth){
									top_auth = author_c.getKey()+"|"+author_c.getValue();
									max_auth = author_c.getValue();
								}
							}
							f.write("\t"+top_auth+"\t");
						}						
						//////
						Map<String, String> pmid_journal = PubMed.getPubmedJournals(pmids);
						if(pmid_journal!=null){
							Map<String, Integer> j_count = new HashMap<String, Integer>();
							for(String pub : gene2pub.get(gene)){
								String journal = pmid_journal.get(pub);
								if(j_count.containsKey(journal)){
									j_count.put(journal, j_count.get(journal)+1);
								}else{
									j_count.put(journal, 1);
								}
							}
							String top_j = ""; int max_j = 0;
							for(Entry<String, Integer> journal_c : j_count.entrySet()){
								f.write(journal_c.getKey()+"|"+journal_c.getValue()+",");
								if(journal_c.getValue()> max_j){
									top_j = journal_c.getKey()+"|"+journal_c.getValue();
									max_j = journal_c.getValue();
								}
							}							
							f.write("\t"+top_j+"\t");
						}				
						f.write("\n");
						System.out.println("Found "+n+" weak wikis ");
					}
				}
			}
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static Map<String, String> getGeneWikiStats(String wikivolume_file){
		Map<String, String> g_w = new HashMap<String, String>();
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(wikivolume_file));
			String line = f.readLine();
			line = f.readLine();
			while(line!=null){			
				String[] stats = line.split("\t");
				g_w.put(stats[0], line.substring(line.indexOf("\t")));
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
		return g_w;	
	}

	public static Map<String, Float> getGeneWiki2refs(String wikivolume_file){
		Map<String, Float> g_w = new HashMap<String, Float>();
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(wikivolume_file));
			String line = f.readLine();
			line = f.readLine();
			while(line!=null){			
				String[] stats = line.split("\t");
				g_w.put(stats[0], Float.parseFloat(stats[10]));
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
		return g_w;	
	}

	public static Map<String, Float> getGeneWiki2bytes(String wikivolume_file){
		Map<String, Float> g_w = new HashMap<String, Float>();
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(wikivolume_file));
			String line = f.readLine();
			line = f.readLine();
			while(line!=null){			
				String[] stats = line.split("\t");
				g_w.put(stats[0], Float.parseFloat(stats[5]));
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
		return g_w;	
	}

	public static Map<String, Float> getGeneWiki2Words(String wikivolume_file){
		Map<String, Float> g_w = new HashMap<String, Float>();
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(wikivolume_file));
			String line = f.readLine();
			line = f.readLine();
			while(line!=null){			
				String[] stats = line.split("\t");
				g_w.put(stats[0], Float.parseFloat(stats[6]));
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
		return g_w;	
	}


	public static Map<String, List<String>> getGene2pub(String gene2pubmed_file){
		Map<String, List<String>> p2g = new HashMap<String, List<String>>();
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(gene2pubmed_file));
			String line = f.readLine();
			while(line!=null){
				if(!line.startsWith("9606")){
					line = f.readLine();
					continue;
				}
				String[] g2p = line.split("\t");
				List<String> pmids = p2g.get(g2p[1]);
				if(pmids==null){
					pmids = new ArrayList<String>();
				}
				pmids.add(g2p[2]);
				p2g.put(g2p[1], pmids);
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
		return p2g;	
	}

	public static Map<String, List<String>> filterMegaGenePubs(Map<String, List<String>> gene2pubs, int max_genes_per_pmid){
		Map<String, Set<String>> pmid2genes = MapFun.flipMapStringListStrings(gene2pubs);

		Set<String> too_many = new HashSet<String>();
		for(Entry<String, List<String>> gene2pub : gene2pubs.entrySet()){
			for(String pub : gene2pub.getValue()){
				if(pmid2genes.get(pub).size()> max_genes_per_pmid){
					too_many.add(pub);
				}
			}
		}

		for(Entry<String, List<String>> gene2pub : gene2pubs.entrySet()){
			List<String> pubs = gene2pub.getValue();
			for(String pmid : pubs){
				if(too_many.contains(pmid)){
					List<String> shorter = new ArrayList<String>(pubs);
					shorter.remove(pmid);
					//	System.out.println("removing "+pmid+" "+gene2pub.getKey());
					gene2pubs.put(gene2pub.getKey(), shorter);
				}
			}
		}
		return gene2pubs;
	}

}
