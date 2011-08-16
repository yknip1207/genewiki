/**
 * 
 */
package org.gnf.genewiki.metrics;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.gnf.genewiki.journal.Prioritizer;
import org.gnf.go.GOterm;
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
		getGenePubGoCounts();
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
