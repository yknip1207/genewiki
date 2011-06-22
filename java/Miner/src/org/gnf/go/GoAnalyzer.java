package org.gnf.go;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.gnf.genewiki.Config;
import org.gnf.genewiki.GeneWikiUtils;
import org.gnf.genewiki.associations.CandidateAnnotation;
import org.gnf.genewiki.associations.CandidateAnnotations;
import org.gnf.util.BioInfoUtil;

public class GoAnalyzer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//countWikiTitles();
		//measureDepthOnCandidates();
		//	measurePublicHumanGOA();
		String file = "/Users/bgood/data/genewiki/NCBO-output/MinedGOANNOS.txt";	
		CandidateAnnotations cannolist = new CandidateAnnotations();
		cannolist.loadCandidateAnnotations(file);
		List<CandidateAnnotation> testcannos = cannolist.getCannos();
		measureDepthOnCandidates(testcannos);
	}

	public static void countWikiTitles(){
		String file = "C:\\Users\\bgood\\data\\genewiki\\gwiki2goannos_(all_with_panther_ortho)";
		CandidateAnnotations cannolist = new CandidateAnnotations();
		cannolist.loadCandidateAnnotations(file);
		List<CandidateAnnotation> testcannos = cannolist.getCannos();
		GOowl gol = new GOowl();
		gol.initFromFile(false);
		Set<String> titles = new HashSet<String>();
		Set<String> preferred = new HashSet<String>();
		Set<String> acc = new HashSet<String>();
		Set<String> row = new HashSet<String>();
		Map<String, Integer> m_type = new HashMap<String, Integer>();
		Map<String, Integer> term_count = new HashMap<String, Integer>();
		for(CandidateAnnotation canno : testcannos){
			//	if(canno.getTarget_accession().equals("GO:0003824")){
			//		continue;
			//	}
			titles.add(canno.getTarget_wiki_page_title());
			preferred.add(canno.getTarget_preferred_term());
			String m = canno.getString_matching_method();
			if(!m.startsWith("xref")){

				String uri = gol.makeGoUri(canno.getTarget_accession().substring(3));
				List<String> xs = gol.getGoTermWikiXref(uri);
				if(xs!=null&&xs.size()>0){
					m = "xref";
				}
			}else{
				m = "xref";
			}
			if(acc.add(canno.getTarget_accession())){
				//check for missing xref
				Integer c = m_type.get(m);
				if(c==null){
					c = 1;
				}else{
					c++;
				}
				m_type.put(m, c);
				row.add(m+"\t"+canno.getTarget_wiki_page_title()+"\t"+canno.getTarget_accession()+"\t"+canno.getTarget_preferred_term()+"\t"+canno.getVocabulary_branch());
			}

			String t = canno.getTarget_preferred_term();
			Integer tc = term_count.get(t);
			if(tc==null){
				tc = 1;
			}else{
				tc++;
			}
			term_count.put(t, tc);

		}

		//		for(Entry<String, Integer> mtype : m_type.entrySet()){
		//			System.out.println(mtype.getKey()+"\t"+mtype.getValue());
		//		}
		System.out.println();
		for(Entry<String, Integer> t : term_count.entrySet()){
			System.out.println(t.getKey()+"\t"+t.getValue());
		}
		System.out.println();

		System.out.println("titles "+titles.size());
		System.out.println("preferredterms "+preferred.size());
		System.out.println("accessions "+acc.size());

		//write out mapping for wiki2go
		/*		String outfile = "C:\\Users\\bgood\\data\\genewiki\\gwikilink2go-8-24-2010-xrefpref";
		try {
			FileWriter out = new FileWriter(outfile);
			out.write("Mapping strategy\tWikiPage\tAccession\tPreferredTerm\tRoot\n");
			for(String r : row){
				out.write(r+"\n");
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 */		 		
	}

	public static void measureDepthOnCandidates(){
		String file = "C:\\Users\\bgood\\data\\genewiki\\gwiki2goannos_(all_with_panther_ortho)";
		CandidateAnnotations cannolist = new CandidateAnnotations();
		cannolist.loadCandidateAnnotations(file);
		List<CandidateAnnotation> testcannos = cannolist.getCannos();
		measureDepthOnCandidates(testcannos);
	}

	public static void measureDepthOnCandidates(List<CandidateAnnotation> testcannos){
		Map<String, Integer> term_count = new HashMap<String, Integer>();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		GOowl gol = new GOowl();
		gol.initFromFile(false);
		Set<String> u = new HashSet<String>();
		for(CandidateAnnotation canno : testcannos){
			if(u.add(canno.getTarget_accession())){
				double d = (double)gol.getIsaDepth(canno.getTarget_accession());
				if(d==0){
					if(!gol.isGoTermObsolete(canno.getTarget_accession())){
						stats.addValue(d);
						System.out.println("d 0 "+canno.getTarget_accession()+" "+canno.getTarget_preferred_term());
					}else{
						//System.out.println("OBS "+canno.getTarget_accession()+" "+canno.getTarget_preferred_term());
					}
				}else{
					stats.addValue(d);
				}				
			}
			//sneak in the term count

			String t = canno.getTarget_preferred_term();
			Integer tc = term_count.get(t);
			if(tc==null){
				tc = 1;
			}else{
				tc++;
			}
			term_count.put(t, tc);
		}
		System.out.println("\n"+stats);

		//		BioInfoUtil.writeStatsForR(stats, "C:\\Users\\bgood\\data\\genewiki\\candi_onto_depth", "depth");
		for(Entry<String, Integer> s : term_count.entrySet()){
			System.out.println(s.getKey()+"\t"+s.getValue());
		}
	}

	public static void measurePublicHumanGOA(){
		DescriptiveStatistics stats = new DescriptiveStatistics();
		String file = Config.gene_go_file;
		GOowl gol = new GOowl();
		gol.initFromFile(false);
		Map<String, Set<GOterm>> geneid_go = GeneWikiUtils.readGene2GO(file, "\t", "#");
		System.out.println(geneid_go.size()+"\thuman genes");
		int n = 0;
		Set<String> u = new HashSet<String>();
		Map<String, Integer> term_use = new HashMap<String, Integer>();
		for(Entry<String, Set<GOterm>> gene2go : geneid_go.entrySet()){
			n+=gene2go.getValue().size();
			for(GOterm t : gene2go.getValue()){
				if(u.add(t.accession)){
					double d = (double)gol.getIsaDepth(t.accession);
					if(d==0){
						if(!gol.isGoTermObsolete(t.accession)){
							//				stats.addValue(d);
							System.out.println("d 0 "+t.accession+" "+t.getTerm());
						}else{
							System.out.println("OBS "+t.accession+" "+t.getTerm());
						}
					}else{
						stats.addValue(d);
					}
				}
				String a = t.getTerm();
				Integer tc = term_use.get(a);
				if(tc==null){
					tc = 1;
				}else{
					tc++;
				}
				term_use.put(a, tc);
			}
		}

		/*		try {
			FileWriter out = new FileWriter("C:\\Users\\bgood\\data\\genewiki\\real_onto_term_use");
			out.write("Preferred Term\tCount\n");
			for(Entry<String, Integer> t : term_use.entrySet()){
				out.write(t.getKey()+"\t"+t.getValue()+"\n");
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 */		

		System.out.println(n+"\tannotations\t"+(float)n/(float)geneid_go.size());
		System.out.println("\n"+stats);
		//		BioInfoUtil.writeStatsForR(stats, "C:\\Users\\bgood\\data\\genewiki\\real_onto_depth", "depth");
	}
}
