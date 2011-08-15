/**
 * 
 */
package org.gnf.genewiki.metrics;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.gnf.go.GOterm;
import org.gnf.util.BioInfoUtil;
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
		double total_in_gene = 42159; //See NCBI Gene with query (alive[prop] AND txid9606 )
		//count genes linked go (from ncbi gene2go) 
		boolean skipIEA = true; boolean only_human = true;
		Map<String, Set<GOterm>> gene2go = BioInfoUtil.readGene2GO("/Users/bgood/data/bioinfo/gene2go", skipIEA, only_human ); //updated Aug. 9, 2011
		double genes_with_5_plus_go = 0;
		double genes_with_any_go = gene2go.keySet().size();
		
		for(Entry<String, Set<GOterm>> g2p : gene2go.entrySet()){
			if(g2p.getValue().size()>5){
				genes_with_5_plus_go++;
			}
		}
		double lowlinked_go = (total_in_gene - genes_with_5_plus_go);
		double nolinked_go = (total_in_gene - genes_with_any_go);
		System.out.println("GO: 5 or fewer:\t"+lowlinked_go+"\t%total:\t"+lowlinked_go/total_in_gene);
		System.out.println("GO: No linked refs:\t"+nolinked_go+"\t%total:\t"+nolinked_go/total_in_gene);		
		
		//count genes linked to pubmed articles		
		String gene2pubmedcountfile = "/Users/bgood/data/NARupdate2011/gene2pubmedGO_counts_no_IEA.txt";
		Map<String, List<String>> gene2pub = BioInfoUtil.getHumanGene2pub("/Users/bgood/data/bioinfo/gene2pubmed"); //updated Aug. 9, 2011
		double genes_with_5_plus = 0;
		double genes_with_any = gene2pub.keySet().size();
		try {
			FileWriter f = new FileWriter(gene2pubmedcountfile);
			f.write("gene\tpmid_count\tgo_count\n");
			Set<String> genes = new HashSet<String>(gene2pub.keySet());
			genes.addAll(gene2go.keySet());
			for(String gene : genes){
				String r = gene+"\t";
				if(gene2pub.get(gene)!=null){
					if(gene2pub.get(gene).size()>5){		
						genes_with_5_plus++;
					}
					r+=gene2pub.get(gene).size()+"\t";
				}else{
					r+="0\t";
				}
				if(gene2go.get(gene)!=null){
					r+=gene2go.get(gene).size()+"\t";
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
		double lowlinked = (total_in_gene - genes_with_5_plus);
		double nolinked = (total_in_gene - genes_with_any);
		System.out.println("PubMed, 5 or fewer:\t"+lowlinked+"\t%total:\t"+lowlinked/total_in_gene);
		System.out.println("PubMed, No linked refs:\t"+nolinked+"\t%total:\t"+nolinked/total_in_gene);

		

	}

}
