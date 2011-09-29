package org.scripps.ncbi;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.genewiki.GeneWikiUtils;
import org.scripps.ontologies.go.GOowl;
import org.scripps.ontologies.go.GOterm;
import org.scripps.util.BioInfoUtil;
import org.scripps.util.Gene;

public class GwapBuilder {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		getWellAnnotatedHumanGenesUnderAGOroot();
	}

	
	public static void getWellAnnotatedHumanGenesUnderAGOroot(){
		String rootacc = "0002376";//"0002757";//
		GOterm t = new GOterm("", "GO_"+rootacc, "", "immune system process", true);
		//get the list of allowed terms
		GOowl gowl = new GOowl();
		gowl.initFromFileRDFS(false);
		Set<GOterm> terms = gowl.getSubs(t);
		Set<String> accs = new HashSet<String>();
		for(GOterm gt : terms){
			accs.add(gt.getAccession());
		}
		System.out.println("read in subclass list : total "+terms.size());

		gowl.close();
		
		//read in gene to go ~term map
		String gene_go_file = "C:/Users/bgood/data/genewiki/human_gene2go";
		String outfile = "C:/Users/bgood/data/genewiki/human_annotation-GO2-"+rootacc;
		Map<String, Set<GOterm>> geneid_go1 = GeneWikiUtils.readGene2GO(gene_go_file, "\t", "#");
		System.out.println("read geneid_go map");
		//http://www.ncbi.nlm.nih.gov/portal/utils/pageresolver.fcgi?recordid=1281028831410665
		//aug 5, 2010 - genes, and genomes, no pseudogenes, human[ORGN]
		int nhuman = 27050;
		DescriptiveStatistics stats = new DescriptiveStatistics();
		
		//filter
		Map<String, Set<GOterm>> geneid_go_filtered = new HashMap<String, Set<GOterm>>();
		Map<String, Set<GOterm>> geneid_go_all = new HashMap<String, Set<GOterm>>();
		for(Entry<String, Set<GOterm>> gene_go : geneid_go1.entrySet()){
			Set<GOterm> ng = new HashSet<GOterm>();
			for(GOterm gt : gene_go.getValue()){
				if(accs.contains(gt.getAccession())){
					ng.add(gt);
				}
			}
			if(ng.size()>0){
				geneid_go_filtered.put(gene_go.getKey(), ng);
				geneid_go_all.put(gene_go.getKey(), gene_go.getValue());
			}
		}
		//load details for genes;
		Map<String, Gene> geneinfo = BioInfoUtil.loadHumanGeneInfo("geneid");
		
		try {
			FileWriter out = new FileWriter(outfile);
			for(String geneid : geneid_go_all.keySet()){
				float all = (float)geneid_go_all.get(geneid).size();
				float f = (float)geneid_go_filtered.get(geneid).size();
				float r = all/140 + f/8;
				out.write(geneinfo.get(geneid).toString()+"\t"+all+"\t"+f+"\t"+r+"\n");				
				stats.addValue(geneid_go_all.get(geneid).size());
			}
			System.out.println("all \n"+stats.toString()+"\n");
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
