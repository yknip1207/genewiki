/**
 * 
 */
package org.gnf.genewiki.metrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.gnf.genewiki.GeneWikiUtils;
import org.gnf.genewiki.journal.Prioritizer;
import org.gnf.go.GOterm;
import org.gnf.search.GoogleSearch;
import org.gnf.util.BioInfoUtil;
import org.gnf.util.Gene;
import org.gnf.util.MapFun;
import org.gnf.util.MyGeneInfo;
/**
 * Keep track of how stats used for Gene Wiki NAR update article were assembled
 * @author bgood
 *
 */
public class NAR2ReportBuilder {

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		//getGenePubGoCounts();
		runGoogleRankAnalysis();
	}

	public static Map<String, List<String>> getUrlsForGeneSymbols(String google_cache_file){
		Map<String, List<String>> gene_urls = new HashMap<String, List<String>>();
		if(google_cache_file!=null){
			File in = new File(google_cache_file);
			if(in.canRead()){
				try {
					BufferedReader f = new BufferedReader(new FileReader(google_cache_file));
					String line = f.readLine().trim();
					while(line!=null){
						String[] item = line.split("\t");
						if(item!=null&&item.length>1){
							List<String> urls = gene_urls.get(item[0]);
							if(urls == null){
								urls = new ArrayList<String>();
							}
							urls.add(item[3]);
							gene_urls.put(item[0], urls);
						}
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
		}
		return gene_urls;
	}

	public static void runGoogleRankAnalysis(){
		//get gene symbol list for gene wiki pages

		//work from local cache of index
		String index = "/users/bgood/data/bioinfo/gene_wiki_index.txt";
		Map<String,String> gene_wiki = GeneWikiUtils.readGeneWikiGeneIndex(index);
		//convert to gene symbols
		Map<String, Gene> gene_info = null;
		boolean usepublicmygeneinfo = false;
		try {
			gene_info = MyGeneInfo.getBatchGeneInfo(gene_wiki.keySet(), usepublicmygeneinfo);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//execute google query for gene symbol and cache results 
		String google_result_cache = "/users/bgood/data/NARupdate2011/google/google_cache.txt";
		Map<String, List<String>> gene_urls = getUrlsForGeneSymbols(google_result_cache);
		String google_rank = "/users/bgood/data/NARupdate2011/google/google_rank.txt";
		int g = 0;
		float n_with_gresults = 0; float n_first_page = 0;
		Map<String, Float> gene_rank = new HashMap<String, Float>();
		boolean newone = true;
		for(Gene gene : gene_info.values()){
			if(gene_urls.containsKey(gene.getGeneID())){
				newone = false;
			}else{
				newone = true;
			}
			g++;
			String wikiurl = "http://en.wikipedia.org/wiki/"+gene_wiki.get(gene.getGeneID());
			wikiurl.replaceAll(" ", "_");
			System.out.println("on "+g+"\t"+gene.getGeneSymbol()+" fraction on first page = "+n_first_page/n_with_gresults);
			try {
				List<String> urls = null;
				if(!newone){
					urls = gene_urls.get(gene.getGeneID());
				}else{
					urls = GoogleSearch.getTopHits(gene.getGeneSymbol());
					try {
						int sleeptime = 1500+(int)(1000*Math.random());
						Thread.sleep(sleeptime);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(urls!=null&&urls.size()>0){
					n_with_gresults++;
					FileWriter f = new FileWriter(google_result_cache, true);
					int rank = 1;
					int wrank = 0;
					for(String url : urls){
						if(newone){
							f.write(gene.getGeneID()+"\t"+gene.getGeneSymbol()+"\t"+rank+"\t"+url+"\n");
						}
						if(url.equalsIgnoreCase(wikiurl)){
							wrank = rank;
							gene_rank.put(gene.getGeneSymbol(), new Float(wrank));
							n_first_page++;
						}
						rank++;
					}
					f.close();
					if(newone){
						FileWriter fr = new FileWriter(google_rank, true);
						fr.write(gene.getGeneID()+"\t"+gene.getGeneSymbol()+"\t"+wrank+"\n");
						fr.close();
					}
				}else{
					System.out.println("Error on request for "+gene.getGeneSymbol()+" stopping");
					break;
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//make histogram
		Map<Float, Integer> vals = new HashMap<Float, Integer>();
		for(Entry<String, Float> gr : gene_rank.entrySet()){
			Integer vcount = vals.get(gr.getValue());
			if(vcount==null){
				vcount = new Integer(0);
			}
			vcount++;
			vals.put(gr.getValue(), vcount);
		}
		System.out.println("Rank\tCount");
		for(Entry<Float, Integer> val : vals.entrySet()){
			System.out.println(val.getKey()+"\t"+val.getValue());
		}

	}

	public static void getGenePubGoCounts(){
		double total_in_gene = 20473; // 42159 for all.. //See NCBI Gene with query (alive[prop] AND txid9606 )
		//count genes linked go (from ncbi gene2go) 
		boolean skipIEA = true; boolean only_human = true;
		Map<String, Set<GOterm>> gene2go = BioInfoUtil.readGene2GO("/Users/bgood/data/bioinfo/gene2go", skipIEA, only_human ); //updated Aug. 9, 2011

		//count genes linked to pubmed articles		
		String gene2pubmedcountfile = "/Users/bgood/data/NARupdate2011/gene2pmedGO_noIEA_pcoding_no_mega_pub.txt";
		Map<String, List<String>> gene2pub = BioInfoUtil.getHumanGene2pub("/Users/bgood/data/bioinfo/gene2pubmed"); //updated Aug. 9, 2011
		boolean filter_mega_gene_pubs = true;
		//System.out.println("pmids before filter "+MapFun.flipMapStringListStrings(gene2pub).keySet().size());
		if(filter_mega_gene_pubs){
			gene2pub = Prioritizer.filterMegaGenePubs(gene2pub, 100);
		}
		//System.out.println("pmids AFTER filter "+MapFun.flipMapStringListStrings(gene2pub).keySet().size());
		Set<String> genes = new HashSet<String>(gene2pub.keySet());
		genes.addAll(gene2go.keySet());

		//remove non protein coding genes
		Map<String, Gene> gene_info = null;
		boolean usepublicmygeneinfo = false;
		try {
			gene_info = MyGeneInfo.getBatchGeneInfo(genes, usepublicmygeneinfo);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		System.out.println("GENE IDS WITH A GO OR A PMID "+genes.size());
		for(Entry<String, Gene> gene_entry : gene_info.entrySet()){
			Gene g = gene_entry.getValue();
			if(g!=null&&g.getGenetype()!=null&&!g.getGenetype().equals("protein-coding")){
				genes.remove(g.getGeneID());
			}
		}
		System.out.println("GENE IDS WITH A GO OR A PMID that are coding "+genes.size());

		double genes_with_5_plus = 0;
		double genes_with_any = 0;
		double genes_with_5_plus_go = 0;
		double genes_with_any_go = 0;		

		try {
			FileWriter f = new FileWriter(gene2pubmedcountfile);
			f.write("gene\tpmid_count\tgo_count\n");

			for(String gene : genes){
				String r = gene+"\t";
				if(gene2pub.get(gene)!=null){
					genes_with_any++;
					if(gene2pub.get(gene).size()>5){		
						genes_with_5_plus++;
					}
					r+=gene2pub.get(gene).size()+"\t";
				}else{
					r+="0\t";
				}
				if(gene2go.get(gene)!=null){
					genes_with_any_go++;
					r+=gene2go.get(gene).size()+"\t";
					if(gene2go.get(gene).size()>5){
						genes_with_5_plus_go++;
					}
				}else{
					r+="0\t";
				}
				r+="\n";
				f.write(r);
			}
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double lowlinked_go = (total_in_gene - genes_with_5_plus_go);
		double nolinked_go = (total_in_gene - genes_with_any_go);
		System.out.println("GO: 5 or fewer:\t"+lowlinked_go+"\t%total:\t"+lowlinked_go/total_in_gene);
		System.out.println("GO: No linked refs:\t"+nolinked_go+"\t%total:\t"+nolinked_go/total_in_gene);

		double lowlinked = (total_in_gene - genes_with_5_plus);
		double nolinked = (total_in_gene - genes_with_any);
		System.out.println("PubMed, 5 or fewer:\t"+lowlinked+"\t%total:\t"+lowlinked/total_in_gene);
		System.out.println("PubMed, No linked refs:\t"+nolinked+"\t%total:\t"+nolinked/total_in_gene);
	}

}
