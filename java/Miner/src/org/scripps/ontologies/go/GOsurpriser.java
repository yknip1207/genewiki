package org.scripps.ontologies.go;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.genewiki.Config;
import org.genewiki.GeneWikiUtils;
import org.genewiki.mapping.annotations.CandidateAnnotation;

public class GOsurpriser {
	
	HashMap<String, Set<GOterm>> gene_gos;
	Map<String, Double> go_go;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	public GOsurpriser(boolean addParents){
		gene_gos = GeneWikiUtils.readGene2GO(Config.gene_go_file, addParents);
		//get the anti-associated list
		go_go = GOsurpriseEval.loadAntiAssoc();		
	}
	
	public double getSurprise(CandidateAnnotation canno){
		double s = 0;
		double c = 0;
		DescriptiveStatistics stats = new DescriptiveStatistics();
		Set<GOterm> goas = gene_gos.get(canno.getEntrez_gene_id());
		//get the anti-correlated set for all of these terms (if any)
		if(goas!=null){
			for(GOterm go1 : goas){
				c++;
				String acc1 = go1.getAccession();
				String acc2 = canno.getTarget_accession();
				if(acc1.compareTo(acc2) > 0){
						String tmp = acc1;
						acc1 = acc2;
						acc2 = tmp;
				}
				Double d = go_go.get(acc1+acc2);
				if(d!=null){
					stats.addValue(d.doubleValue());
				}else{
					stats.addValue(1);
				}
			}
		}else{
			//no counter-indications found
			return 1;
		}
			//rank = worst counter example, if P = 0 rank = 0..
		return stats.getMin();
	}
}
