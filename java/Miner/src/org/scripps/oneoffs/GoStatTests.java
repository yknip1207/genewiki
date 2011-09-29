/**
 * 
 */
package org.scripps.oneoffs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Map.Entry;

import org.apache.axis2.AxisFault;
import org.genewiki.Config;
import org.genewiki.mapping.annotations.CandidateAnnotation;
import org.genewiki.mapping.annotations.CandidateAnnotations;
import org.scripps.ncbi.PubMed;
import org.scripps.nlp.ncbo.GenericTextToAnnotation;
import org.scripps.ontologies.go.Annotation;
import org.scripps.ontologies.go.GOterm;
import org.scripps.util.FileFun;
import org.scripps.util.MapFun;
import org.scripps.util.StatFun;


/**
 * @author bgood
 *
 */
public class GoStatTests {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//getDataForAbstract2Annotations();
		//measureGeneWikiValueforOverrepresentationAnalysis();
		measureGeneWikiValueforOverrepresentationAnalysis();
	}

	/**
	 * Do a simulation (1000 runs) where you select 87 genes randomly and perform the test exactly as you do 
	 * for the 87 genes corresponding to GO:0006936 ... see how many times out of 1000 do you get an 
	 * insignificant p-value using just pubmed derived annotations and a significant p-value after adding gene wiki text.
	 */
	public static void measureGeneWikiValueforOverrepresentationAnalysis(){
		String output = "/Users/bgood/data/genewiki_jan_2011/output/evaluations/overrepresentation/goa_recovery_sim1.txt";
		//annotations from abstracts
		String abannos = "/Users/bgood/data/genewiki_jan_2011/output/evaluations/overrepresentation/annos_from_abstracts.txt";

		//for each go term in gene2go
		//get the genes related to it 
		//get the go terms related to those genes via the annotator and associated abstracts
		//measure the gene2pubmed2annotator process' ability to reproduce original go2gene tuples
		//add on annotations from gene wiki and measure difference - does it help to include gene wiki content?

		boolean skipIEA = true;
		Map<String, Set<GOterm>> genego = Annotation.readGene2GO(Config.gene_go_file, skipIEA);
		Map<String, GOterm> acc2go = new HashMap<String, GOterm>();
		for(Set<GOterm> terms : genego.values()){
			for(GOterm t : terms){
				acc2go.put(t.getAccession(), t);
			}
		}
		int total_genes_from_goa = genego.keySet().size();
		Map<String, Set<String>> go_geness = MapFun.flipMapStringGOs(genego);
		//get gene_ab_go 		
		CandidateAnnotations abcannos = new CandidateAnnotations();
		abcannos.loadAndFilterCandidateGOAnnotations(abannos, false);
		Map<String, Set<String>> gene_ab_gos = abcannos.getGene2OntoMap();
		Map<String, Set<String>> go_ab_genes = MapFun.flipMapStringSetStrings(gene_ab_gos);

		System.out.println("got gene to go from abstracts ");
		//get gene2pubmed
		Map<String, Set<String>> gene2pmids = getGene2PMIDfromGene2GO();
		Map<String, Set<String>> go2pmids = new HashMap<String, Set<String>>();
		for(Entry<String, Set<String>> go_genes : go_geness.entrySet()){
			Set<String> pmids = new HashSet<String>();
			for(String gene : go_genes.getValue()){
				Set<String> pp = gene2pmids.get(gene);
				if(pp!=null&&pp.size()>0){
					pmids.addAll(pp);
				}else{
					//System.out.println("no pmids for gene "+gene+" from go "+go_genes.getKey());
				}
			}
			go2pmids.put(go_genes.getKey(), pmids);
		}

		System.out.println("got gene to go to pmid from goa ");

		CandidateAnnotations gwcannos = new CandidateAnnotations();
		boolean ignorepbb = true;
		gwcannos.loadAndFilterCandidateGOAnnotations(Config.text_mined_annos, ignorepbb);
		Map<String, Set<String>> gene_gw_gos = gwcannos.getGene2OntoMap();
		Map<String, Set<String>> go_gw_genes = MapFun.flipMapStringSetStrings(gene_gw_gos);
		System.out.println("got gene to go from gene wiki ");

		//make a random one with the same number of genes per go term as the gene wiki derived map
		List<String> allgenes = new ArrayList<String>(genego.keySet());
		Map<String, Set<String>> go_ran_genes = new HashMap<String, Set<String>>();
		for(Entry<String, Set<String>> go_gw_gene : go_gw_genes.entrySet()){
			Collections.shuffle(allgenes);
			Set<String> genes = new HashSet<String>();
			String go = go_gw_gene.getKey();
			for(int i=0; i<go_gw_gene.getValue().size(); i++){
				genes.add(allgenes.get(i));
			}
			go_ran_genes.put(go, genes);
		}
		System.out.println("got gene to go from random ");		

		try {
			FileWriter w = new FileWriter(output);
			w.write("acc	term	branch	pmid_count	ao_yy	ao_ny	ao_yn	ao_nn	f2t_abs_only	delta_log_f2t_gw	f2t_with_gw	gw_yy	gw_ny	gw_yn	gw_nn	delta_log_f2t_ran	f2t_with_ran	ran_yy	ran_ny	ran_yn	ran_nn	");
			for(int r=0; r<1000; r++){
				//		for(Entry<String, Set<String>> go_genes : go_geness.entrySet()){
				//			String goterm = go_genes.getKey();
				String goterm = "GO:0006936";
				GOterm full = acc2go.get(goterm);
				String preferred = full.getTerm();
				String branch = full.getRoot();
				Set<String> pmids = go2pmids.get(goterm);
				String pmid_count = "0";
				if(pmids!=null){
					pmid_count = pmids.size()+"";
				}
				//			Set<String> genes_f_goa = go_genes.getValue();
				Set<String> genes_f_goa_real = go_geness.get(goterm);
				//make it random
				Set<String> genes_f_goa = new HashSet<String>();
				Collections.shuffle(allgenes);
				for(int i=0; i<genes_f_goa_real.size(); i++){
					genes_f_goa.add(allgenes.get(i));
				}			

				///////////// ab only		
				Set<String> genes_f_ab = new HashSet<String>();
				if(go_ab_genes.get(goterm)!=null){
					genes_f_ab.addAll(go_ab_genes.get(goterm));
				}			
				//			if(genes_f_ab==null||genes_f_ab.size()==0){
				//				continue;
				//			}
				//	w.writeln(genes_f_goa);
				//	w.writeln(genes_f_ab);
				int total_goa_ = genes_f_goa.size();
				int total_goa_not_ab_ = 0;
				int total_ab_ = 0;
				int total_ab_goa_ = 0;
				int total_ab_not_goa_ = 0;
				int total_not_goa_not_ab = 0;
				double chi_p_ab_only = 0;
				if(genes_f_ab!=null&&genes_f_ab.size()>0){
					total_ab_ = genes_f_ab.size();
					//find genes reproduced..
					genes_f_ab.retainAll(genes_f_goa);
					//++
					total_ab_goa_ = genes_f_ab.size();
					//-+
					total_ab_not_goa_ = total_ab_ - total_ab_goa_;
					//+_
					total_goa_not_ab_ = total_goa_ - total_ab_goa_;
					//--
					total_not_goa_not_ab = total_genes_from_goa - total_ab_goa_ - total_ab_not_goa_ - total_goa_not_ab_;
					//test
					//				chi_p_ab_only = StatFun.chiSquareTest(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					//				chi_p_ab_only = StatFun.chiSquareValue(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					chi_p_ab_only = StatFun.fishersExact2tailed(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					w.write("\n"+goterm+"\t"+preferred+"\t"+branch+"\t"+pmid_count+"\t"+total_ab_goa_+"\t"+total_ab_not_goa_+"\t"+total_goa_not_ab_+"\t"+total_not_goa_not_ab+"\t"+chi_p_ab_only+"\t");
				}else{
					total_not_goa_not_ab = total_genes_from_goa - total_ab_goa_ - total_ab_not_goa_ - total_goa_not_ab_;
					chi_p_ab_only = StatFun.fishersExact2tailed(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					w.write("\n"+goterm+"\t"+preferred+"\t"+branch+"\t"+pmid_count+"\t"+total_ab_goa_+"\t"+total_ab_not_goa_+"\t"+total_goa_not_ab_+"\t"+total_not_goa_not_ab+"\t"+chi_p_ab_only+"\t");
				}
				//////// plus gene wiki
				genes_f_ab = new HashSet<String>();
				if(go_ab_genes.get(goterm)!=null){
					genes_f_ab.addAll(go_ab_genes.get(goterm));
				}	
				if(go_gw_genes.get(goterm)!=null){
					genes_f_ab.addAll(go_gw_genes.get(goterm));
				}
				if(genes_f_ab!=null&&genes_f_ab.size()>0){
					total_ab_ = genes_f_ab.size();
					//find genes reproduced..
					genes_f_ab.retainAll(genes_f_goa);
					//++
					total_ab_goa_ = genes_f_ab.size();
					//-+
					total_ab_not_goa_ = total_ab_ - total_ab_goa_;
					//+_
					total_goa_not_ab_ = total_goa_ - total_ab_goa_;
					//--
					total_not_goa_not_ab = total_genes_from_goa - total_ab_goa_ - total_ab_not_goa_ - total_goa_not_ab_;
					//test
					//				double chi_p_both = StatFun.chiSquareTest(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					//				double chi_p_both = StatFun.chiSquareValue(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					double chi_p_both = StatFun.fishersExact2tailed(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					w.write((Math.log(chi_p_both)-Math.log(chi_p_ab_only))+"\t"+chi_p_both+"\t"+total_ab_goa_+"\t"+total_ab_not_goa_+"\t"+total_goa_not_ab_+"\t"+total_not_goa_not_ab+"\t");
				}else{
					//				double chi_p_both = StatFun.chiSquareTest(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					//				double chi_p_both = StatFun.chiSquareValue(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					double chi_p_both = StatFun.fishersExact2tailed(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					w.write((Math.log(chi_p_both)-Math.log(chi_p_ab_only))+"\t"+chi_p_both+"\t"+total_ab_goa_+"\t"+total_ab_not_goa_+"\t"+total_goa_not_ab_+"\t"+total_not_goa_not_ab+"\t");
				}			

				////// plus random	
				genes_f_ab = new HashSet<String>();
				if(go_ab_genes.get(goterm)!=null){
					genes_f_ab.addAll(go_ab_genes.get(goterm));
				}	
				if(go_ran_genes.get(goterm)!=null){
					genes_f_ab.addAll(go_ran_genes.get(goterm));
				}
				if(genes_f_ab!=null&&genes_f_ab.size()>0){
					total_ab_ = genes_f_ab.size();
					//find genes reproduced..
					genes_f_ab.retainAll(genes_f_goa);
					//++
					total_ab_goa_ = genes_f_ab.size();
					//-+
					total_ab_not_goa_ = total_ab_ - total_ab_goa_;
					//+_
					total_goa_not_ab_ = total_goa_ - total_ab_goa_;
					//--
					total_not_goa_not_ab = total_genes_from_goa - total_ab_goa_ - total_ab_not_goa_ - total_goa_not_ab_;
					//test
					//		double chi_p_both = StatFun.chiSquareTest(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					//		double chi_p_both = StatFun.chiSquareValue(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					double chi_p_both = StatFun.fishersExact2tailed(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					w.write((Math.log(chi_p_both)-Math.log(chi_p_ab_only))+"\t"+chi_p_both+"\t"+total_ab_goa_+"\t"+total_ab_not_goa_+"\t"+total_goa_not_ab_+"\t"+total_not_goa_not_ab+"\t");
				}else{
					//		double chi_p_both = StatFun.chiSquareTest(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					//		double chi_p_both = StatFun.chiSquareValue(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					double chi_p_both = StatFun.fishersExact2tailed(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					w.write((Math.log(chi_p_both)-Math.log(chi_p_ab_only))+"\t"+chi_p_both+"\t"+total_ab_goa_+"\t"+total_ab_not_goa_+"\t"+total_goa_not_ab_+"\t"+total_not_goa_not_ab+"\t");
				}
				///// only gene wiki
				//////// plus gene wiki
				genes_f_ab = new HashSet<String>();
				//			if(go_ab_genes.get(goterm)!=null){
				//				genes_f_ab.addAll(go_ab_genes.get(goterm));
				//			}	
				if(go_gw_genes.get(goterm)!=null){
					genes_f_ab.addAll(go_gw_genes.get(goterm));
				}
				if(genes_f_ab!=null&&genes_f_ab.size()>0){
					total_ab_ = genes_f_ab.size();
					//find genes reproduced..
					genes_f_ab.retainAll(genes_f_goa);
					//++
					total_ab_goa_ = genes_f_ab.size();
					//-+
					total_ab_not_goa_ = total_ab_ - total_ab_goa_;
					//+_
					total_goa_not_ab_ = total_goa_ - total_ab_goa_;
					//--
					total_not_goa_not_ab = total_genes_from_goa - total_ab_goa_ - total_ab_not_goa_ - total_goa_not_ab_;
					//test
					//				double chi_p_both = StatFun.chiSquareTest(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					//				double chi_p_both = StatFun.chiSquareValue(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					double chi_p_both = StatFun.fishersExact2tailed(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					w.write((Math.log(chi_p_both)-Math.log(chi_p_ab_only))+"\t"+chi_p_both+"\t"+total_ab_goa_+"\t"+total_ab_not_goa_+"\t"+total_goa_not_ab_+"\t"+total_not_goa_not_ab+"\t");
				}else{
					//				double chi_p_both = StatFun.chiSquareTest(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					//				double chi_p_both = StatFun.chiSquareValue(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					double chi_p_both = StatFun.fishersExact2tailed(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					w.write((Math.log(chi_p_both)-Math.log(chi_p_ab_only))+"\t"+chi_p_both+"\t"+total_ab_goa_+"\t"+total_ab_not_goa_+"\t"+total_goa_not_ab_+"\t"+total_not_goa_not_ab+"\t");
				}

				//			}
			}
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Considers all gene_abstract_go predictions drawn from all abstracts linked to each gene (via a goa record)
	 * Means that some go_gene connections are drawn from abstracts linked to the genes but not necessarily to the GO term
	 */
	public static void measureGeneWikiValueExactlyAsItWasWhenPaperFirstSubmitted(){
		//for each go term in gene2go
		//get the genes related to it 
		//get the go terms related to those genes via the annotator and associated abstracts
		//measure the gene2pubmed2annotator process' ability to reproduce original go2gene tuples
		//add on annotations from gene wiki and measure difference - does it help to include gene wiki content?

		boolean skipIEA = true;
		Map<String, Set<GOterm>> genego = Annotation.readGene2GO(Config.gene_go_file, skipIEA);
		Map<String, GOterm> acc2go = new HashMap<String, GOterm>();
		for(Set<GOterm> terms : genego.values()){
			for(GOterm t : terms){
				acc2go.put(t.getAccession(), t);
			}
		}
		int total_genes_from_goa = genego.keySet().size();
		Map<String, Set<String>> go_geness = MapFun.flipMapStringGOs(genego);
		//get gene_ab_go 
		String abannos = "/Users/bgood/data/annos_from_abstracts.txt";
		CandidateAnnotations abcannos = new CandidateAnnotations();
		abcannos.loadAndFilterCandidateGOAnnotations(abannos, false);
		Map<String, Set<String>> gene_ab_gos = abcannos.getGene2OntoMap();
		Map<String, Set<String>> go_ab_genes = MapFun.flipMapStringSetStrings(gene_ab_gos);

		System.out.println("got gene to go from abstracts ");
		//get gene2pubmed
		Map<String, Set<String>> gene2pmids = getGene2PMIDfromGene2GO();
		Map<String, Set<String>> go2pmids = new HashMap<String, Set<String>>();
		for(Entry<String, Set<String>> go_genes : go_geness.entrySet()){
			Set<String> pmids = new HashSet<String>();
			for(String gene : go_genes.getValue()){
				Set<String> pp = gene2pmids.get(gene);
				if(pp!=null&&pp.size()>0){
					pmids.addAll(pp);
				}else{
					//System.out.println("no pmids for gene "+gene+" from go "+go_genes.getKey());
				}
			}
			go2pmids.put(go_genes.getKey(), pmids);
		}

		System.out.println("got gene to go to pmid from goa ");
		System.out.println("GO:0002115 "+go2pmids.get("GO:0002115"));

		CandidateAnnotations gwcannos = new CandidateAnnotations();
		boolean ignorepbb = true;
		gwcannos.loadAndFilterCandidateGOAnnotations(Config.text_mined_annos, ignorepbb);
		Map<String, Set<String>> gene_gw_gos = gwcannos.getGene2OntoMap();
		Map<String, Set<String>> go_gw_genes = MapFun.flipMapStringSetStrings(gene_gw_gos);
		System.out.println("got gene to go from gene wiki ");

		//make a random one with the same number of genes per go term as the gene wiki derived map
		List<String> allgenes = new ArrayList<String>(genego.keySet());
		Map<String, Set<String>> go_ran_genes = new HashMap<String, Set<String>>();
		for(Entry<String, Set<String>> go_gw_gene : go_gw_genes.entrySet()){
			Collections.shuffle(allgenes);
			Set<String> genes = new HashSet<String>();
			String go = go_gw_gene.getKey();
			for(int i=0; i<go_gw_gene.getValue().size(); i++){
				genes.add(allgenes.get(i));
			}
			go_ran_genes.put(go, genes);
		}
		System.out.println("got gene to go from random ");		

		try {
			FileWriter w = new FileWriter("/Users/bgood/data/term_gw_recovery_v4.txt");
			w.write("acc	term	branch	pmid_count	ao_yy	ao_ny	ao_yn	ao_nn	f2t_abs_only	delta_log_f2t_gw	f2t_with_gw	gw_yy	gw_ny	gw_yn	gw_nn	delta_log_f2t_ran	f2t_with_ran	ran_yy	ran_ny	ran_yn	ran_nn	");
			for(Entry<String, Set<String>> go_genes : go_geness.entrySet()){
				String goterm = go_genes.getKey();
				GOterm full = acc2go.get(goterm);
				String preferred = full.getTerm();
				String branch = full.getRoot();
				Set<String> pmids = go2pmids.get(goterm);
				String pmid_count = "0";
				if(pmids!=null){
					pmid_count = pmids.size()+"";
				}
				Set<String> genes_f_goa = go_genes.getValue();
				///////////// ab only		
				Set<String> genes_f_ab = new HashSet<String>();
				if(go_ab_genes.get(goterm)!=null){
					genes_f_ab.addAll(go_ab_genes.get(goterm));
				}			
				if(genes_f_ab==null||genes_f_ab.size()==0){
					continue;
				}
				//	w.writeln(genes_f_goa);
				//	w.writeln(genes_f_ab);
				int total_goa_ = genes_f_goa.size();
				int total_goa_not_ab_ = 0;
				int total_ab_ = 0;
				int total_ab_goa_ = 0;
				int total_ab_not_goa_ = 0;
				int total_not_goa_not_ab = 0;
				double chi_p_ab_only = 0;
				if(genes_f_ab!=null&&genes_f_ab.size()>0){
					total_ab_ = genes_f_ab.size();
					//find genes reproduced..
					genes_f_ab.retainAll(genes_f_goa);
					//++
					total_ab_goa_ = genes_f_ab.size();
					//-+
					total_ab_not_goa_ = total_ab_ - total_ab_goa_;
					//+_
					total_goa_not_ab_ = total_goa_ - total_ab_goa_;
					//--
					total_not_goa_not_ab = total_genes_from_goa - total_ab_goa_ - total_ab_not_goa_ - total_goa_not_ab_;
					//test
					//				chi_p_ab_only = StatFun.chiSquareTest(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					//				chi_p_ab_only = StatFun.chiSquareValue(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					chi_p_ab_only = StatFun.fishersExact2tailed(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					w.write("\n"+goterm+"\t"+preferred+"\t"+branch+"\t"+pmid_count+"\t"+total_ab_goa_+"\t"+total_ab_not_goa_+"\t"+total_goa_not_ab_+"\t"+total_not_goa_not_ab+"\t"+chi_p_ab_only+"\t");
				}else{
					total_not_goa_not_ab = total_genes_from_goa - total_ab_goa_ - total_ab_not_goa_ - total_goa_not_ab_;
					chi_p_ab_only = StatFun.fishersExact2tailed(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					w.write("\n"+goterm+"\t"+preferred+"\t"+branch+"\t"+pmid_count+"\t"+total_ab_goa_+"\t"+total_ab_not_goa_+"\t"+total_goa_not_ab_+"\t"+total_not_goa_not_ab+"\t"+chi_p_ab_only+"\t");
				}
				//////// plus gene wiki
				genes_f_ab = new HashSet<String>();
				if(go_ab_genes.get(goterm)!=null){
					genes_f_ab.addAll(go_ab_genes.get(goterm));
				}	
				if(go_gw_genes.get(goterm)!=null){
					genes_f_ab.addAll(go_gw_genes.get(goterm));
				}
				if(genes_f_ab!=null&&genes_f_ab.size()>0){
					total_ab_ = genes_f_ab.size();
					//find genes reproduced..
					genes_f_ab.retainAll(genes_f_goa);
					//++
					total_ab_goa_ = genes_f_ab.size();
					//-+
					total_ab_not_goa_ = total_ab_ - total_ab_goa_;
					//+_
					total_goa_not_ab_ = total_goa_ - total_ab_goa_;
					//--
					total_not_goa_not_ab = total_genes_from_goa - total_ab_goa_ - total_ab_not_goa_ - total_goa_not_ab_;
					//test
					//				double chi_p_both = StatFun.chiSquareTest(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					//				double chi_p_both = StatFun.chiSquareValue(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					double chi_p_both = StatFun.fishersExact2tailed(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					w.write((Math.log(chi_p_both)-Math.log(chi_p_ab_only))+"\t"+chi_p_both+"\t"+total_ab_goa_+"\t"+total_ab_not_goa_+"\t"+total_goa_not_ab_+"\t"+total_not_goa_not_ab+"\t");
				}else{
					//				double chi_p_both = StatFun.chiSquareTest(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					//				double chi_p_both = StatFun.chiSquareValue(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					double chi_p_both = StatFun.fishersExact2tailed(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					w.write((Math.log(chi_p_both)-Math.log(chi_p_ab_only))+"\t"+chi_p_both+"\t"+total_ab_goa_+"\t"+total_ab_not_goa_+"\t"+total_goa_not_ab_+"\t"+total_not_goa_not_ab+"\t");
				}			

				////// plus random	
				genes_f_ab = new HashSet<String>();
				if(go_ab_genes.get(goterm)!=null){
					genes_f_ab.addAll(go_ab_genes.get(goterm));
				}	
				if(go_ran_genes.get(goterm)!=null){
					genes_f_ab.addAll(go_ran_genes.get(goterm));
				}
				if(genes_f_ab!=null&&genes_f_ab.size()>0){
					total_ab_ = genes_f_ab.size();
					//find genes reproduced..
					genes_f_ab.retainAll(genes_f_goa);
					//++
					total_ab_goa_ = genes_f_ab.size();
					//-+
					total_ab_not_goa_ = total_ab_ - total_ab_goa_;
					//+_
					total_goa_not_ab_ = total_goa_ - total_ab_goa_;
					//--
					total_not_goa_not_ab = total_genes_from_goa - total_ab_goa_ - total_ab_not_goa_ - total_goa_not_ab_;
					//test
					//		double chi_p_both = StatFun.chiSquareTest(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					//		double chi_p_both = StatFun.chiSquareValue(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					double chi_p_both = StatFun.fishersExact2tailed(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					w.write((Math.log(chi_p_both)-Math.log(chi_p_ab_only))+"\t"+chi_p_both+"\t"+total_ab_goa_+"\t"+total_ab_not_goa_+"\t"+total_goa_not_ab_+"\t"+total_not_goa_not_ab+"\t");
				}else{
					//		double chi_p_both = StatFun.chiSquareTest(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					//		double chi_p_both = StatFun.chiSquareValue(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					double chi_p_both = StatFun.fishersExact2tailed(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					w.write((Math.log(chi_p_both)-Math.log(chi_p_ab_only))+"\t"+chi_p_both+"\t"+total_ab_goa_+"\t"+total_ab_not_goa_+"\t"+total_goa_not_ab_+"\t"+total_not_goa_not_ab+"\t");
				}
				///// only gene wiki
				//////// plus gene wiki
				genes_f_ab = new HashSet<String>();
				//			if(go_ab_genes.get(goterm)!=null){
				//				genes_f_ab.addAll(go_ab_genes.get(goterm));
				//			}	
				if(go_gw_genes.get(goterm)!=null){
					genes_f_ab.addAll(go_gw_genes.get(goterm));
				}
				if(genes_f_ab!=null&&genes_f_ab.size()>0){
					total_ab_ = genes_f_ab.size();
					//find genes reproduced..
					genes_f_ab.retainAll(genes_f_goa);
					//++
					total_ab_goa_ = genes_f_ab.size();
					//-+
					total_ab_not_goa_ = total_ab_ - total_ab_goa_;
					//+_
					total_goa_not_ab_ = total_goa_ - total_ab_goa_;
					//--
					total_not_goa_not_ab = total_genes_from_goa - total_ab_goa_ - total_ab_not_goa_ - total_goa_not_ab_;
					//test
					//				double chi_p_both = StatFun.chiSquareTest(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					//				double chi_p_both = StatFun.chiSquareValue(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					double chi_p_both = StatFun.fishersExact2tailed(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					w.write((Math.log(chi_p_both)-Math.log(chi_p_ab_only))+"\t"+chi_p_both+"\t"+total_ab_goa_+"\t"+total_ab_not_goa_+"\t"+total_goa_not_ab_+"\t"+total_not_goa_not_ab+"\t");
				}else{
					//				double chi_p_both = StatFun.chiSquareTest(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					//				double chi_p_both = StatFun.chiSquareValue(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					double chi_p_both = StatFun.fishersExact2tailed(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
					w.write((Math.log(chi_p_both)-Math.log(chi_p_ab_only))+"\t"+chi_p_both+"\t"+total_ab_goa_+"\t"+total_ab_not_goa_+"\t"+total_goa_not_ab_+"\t"+total_not_goa_not_ab+"\t");
				}

			}
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static Map<String, Set<String>> getGene2PMIDfromGene2GO(){
		HashMap<String,Set<String>> gene_pmids = new HashMap<String, Set<String>>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(Config.gene_go_file));
			String line = f.readLine().trim();
			while(line!=null){
				if(!line.startsWith("#")){
					String[] item = line.split("\t");
					if(item!=null&&item.length>1){
						//	String acc = item[2];
						//	String code = item[3];
						String geneid = item[1];
						String pmids = item[6];
						Set<String> gpmids = gene_pmids.get(geneid);
						if(gpmids==null){
							gpmids = new HashSet<String>();
						}
						if(pmids.trim()!="-"&&pmids.trim().length()>1){
							for(String pmid : pmids.split(",")){
								gpmids.add(pmid);
							}
						}
						gene_pmids.put(geneid,gpmids);				
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
		return gene_pmids;
	}

	public static void getDataForAbstract2Annotations(){
		//get genes associated with a go term in goa file
		//get pubmed ids linked to these genes
		//retrieve abstracts from pubmed linked to these pubmed ids
		//run annotator on these abstracts
		//record gene_go relationships discovered

		//get genes associated with a go term in goa file
		HashMap<String,Set<GOterm>> gene_gos = new HashMap<String, Set<GOterm>>();
		HashMap<String,Set<String>> gene_pmids = new HashMap<String, Set<String>>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(Config.gene_go_file));
			String line = f.readLine().trim();
			while(line!=null){
				if(!line.startsWith("#")){
					String[] item = line.split("\t");
					if(item!=null&&item.length>1){
						String code = item[3];
						String geneid = item[1];
						String pmids = item[6];
						Set<String> gpmids = gene_pmids.get(geneid);
						if(gpmids==null){
							gpmids = new HashSet<String>();
						}
						if(pmids.trim()!="-"&&pmids.trim().length()>1){
							for(String pmid : pmids.split(",")){
								gpmids.add(pmid);
							}
						}
						gene_pmids.put(geneid,gpmids);
						Set<GOterm> GOs = gene_gos.get(geneid);
						if(GOs == null){
							GOs = new HashSet<GOterm>();
							gene_gos.put(geneid, GOs);
						}
						String id = null; String acc = item[2];
						String term = ""; String root = "";

						if(item.length>5){
							term = item[5];
							if(item.length>7){
								root = item[7];
							}
						}
						GOterm go = new GOterm(id, acc, root, term, true);
						go.setEvidence("ncbi_"+code);
						GOs.add(go);//the text term for the go id						
					}
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
		//get pubmed ids linked to these genes
		//build a local cache of the text from pubmed
		String pubcache = "/Users/bgood/data/pubcache.txt";
		String abannos = "/Users/bgood/data/annos_from_abstracts.txt";
		//retrieve abstracts from pubmed linked to these pubmed ids
		Set<String> allpmids = new HashSet<String>();
		System.out.println("Setting id list");
		for(String gene : gene_gos.keySet()){
			Set<String> pmids = gene_pmids.get(gene);
			allpmids.addAll(pmids);
		}
		boolean cache = true;
		Map<String, String> pmid_texts = new HashMap<String, String>();
		try {
			System.out.println("Getting pubcache "+allpmids.size());
			pmid_texts.putAll(PubMed.getPubmedTitlesAndAbstracts(allpmids, pubcache, cache));
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//run annotator on these abstracts
		try {
			FileWriter w = new FileWriter(abannos);
			w.write("Entrez_gene_id	Source_wiki_page_title	Section_heading	Author	WT-Article	WT_annotation	WT_block	Annotator_score	Article_length	Target_accession	Target_preferred_term	Target_vocabulary	Vocabulary_branch	Evidence	Surrounding sentence	References"+"\n");
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Entrez_gene_id	Source_wiki_page_title	Section_heading	Author	WT-Article	WT_annotation	WT_block	Annotator_score	Article_length	Target_accession	Target_preferred_term	Target_vocabulary	Vocabulary_branch	Evidence	Surrounding sentence	References
		//record gene_go relationships discovered
		boolean allowSynonyms = false;
		boolean useGO = true; boolean useDO = true; boolean useFMA = false; boolean usePRO = false;
		int c = 0;
		List<CandidateAnnotation> cannos = new ArrayList<CandidateAnnotation>();
		System.out.println("Running annotator on abstracts for "+gene_pmids.keySet().size());
		String checked_genes = "/Users/bgood/data/checked_genes.txt";
		Set<String> annotated_genes = FileFun.readOneColFile(checked_genes);
		for(String gene : gene_pmids.keySet()){
			c++;
			if(annotated_genes.contains(gene)){
				continue;
			}
			for(String pmid : gene_pmids.get(gene)){
				String text = pmid_texts.get(pmid);
				if(text!=null&&text.length()>10){
					List<CandidateAnnotation> annos = GenericTextToAnnotation.annotateTextWithNCBO(pmid, gene, text, allowSynonyms, useGO, useDO, useFMA, usePRO);
					if(annos!=null){
						try {
							FileWriter w = new FileWriter(abannos, true);
							for(CandidateAnnotation anno : annos){
								w.write(anno.toString()+"\n");
								System.out.println("n:"+c+"\t"+anno.toString());
							}
							w.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						cannos.addAll(annos);
					}
				}
			}
			try{
				FileWriter w = new FileWriter(checked_genes, true);
				w.write(gene+"\n");
				w.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Performs a hypergeometric test to assess the probability that the gene wiki-mined go annotations were associated with go annotations by chance.
	 * Lower p values indicate more agreement between goa and gene wiki mining - with go and gw priors accounted for
	 */
	public static void measureSurpriseForRefindingGOannotationsInGeneWiki(){
		//for each GO process represented in the gene wiki mining results
		//look up all the genes that gene2GO links to it that are present in the gene wiki
		//find the counts of all the GO terms linked to these genes through gene wiki mining
		//for each GO term from the gene wiki, find the hypergeometric p value
		//populationSize = total number of genes in the gene wiki
		//numberSuccessesInPopulation = total number of genes annotated to this term in the whole gene wiki
		//sampleSize = number of genes in the gene wiki linked to the GO process being considered --- by the GO
		//successesInSample = number of genes in the sameple linked to the GO process being considered --- by the Gene Wiki

		//for each GO process represented in the gene wiki mining results
		String annos = Config.text_mined_annos;
		CandidateAnnotations cannolist = new CandidateAnnotations();
		cannolist.loadAndFilterCandidateGOAnnotations(annos, false);
		List<CandidateAnnotation> testcannos = cannolist.getCannos();
		//		System.out.println("Loaded candidate GO annotations "+testcannos.size());
		HashMap<String, Set<String>> go_gw_genes = new HashMap<String, Set<String>>();
		Map<String, GOterm> acc2go = new HashMap<String, GOterm>();
		for(CandidateAnnotation canno : testcannos){
			HashSet<String> genes = (HashSet)go_gw_genes.get(canno.getTarget_accession());
			if(genes==null){
				genes = new HashSet<String>();
			}
			genes.add(canno.getEntrez_gene_id());
			go_gw_genes.put(canno.getTarget_accession(), genes);
			GOterm gt = new GOterm("", canno.getTarget_accession(), canno.getTarget_preferred_term(), canno.getVocabulary_branch(), true);
			acc2go.put(canno.getTarget_accession(), gt);
		}		
		HashMap<String, Set<String>> gene_gw_go = (HashMap)MapFun.flipMapStringSetStrings(go_gw_genes);
		int populationSize = gene_gw_go.keySet().size();
		System.out.println("Loaded gene wiki go-gene map.  Has "+populationSize+" genes.");
		//set up the GOA data
		boolean skipIEA = false;
		Map<String, Set<GOterm>> genego = Annotation.readGene2GO(Config.gene_go_file, skipIEA);

		for(Set<GOterm> terms : genego.values()){
			for(GOterm t : terms){
				acc2go.put(t.getAccession(), t);
			}
		}
		//		String goterm = "GO:0008053";
		Map<String, Set<String>> go_genes = MapFun.flipMapStringGOs(genego);

		//		System.out.println("genes from gene wiki for "+goterm+" "+go_gw_genes.get(goterm));
		//		System.out.println("genes from goa for "+goterm+" "+go_genes.get(goterm));
		//		System.out.println("gos from goa for 55669 "+genego.get("55669"));	
		//		System.out.println("Loaded goa go-gene map.  Has "+genego.keySet().size()+" genes.");
		System.out.println("goterm\tgoname\tgobranch\tpopulationSize\tnHitsInPopulation\tsampleSize\thitsInSample\tp");
		for(String goterm : go_gw_genes.keySet()){

			//look up all the genes that gene2GO links to it that are present in the gene wiki
			Set<String> genesfromgo = go_genes.get(goterm);
			//find the counts of all the GO terms linked to these genes through gene wiki mining		
			//for each GO term from the gene wiki, find the hypergeometric p value
			//populationSize = total number of genes in the gene wiki			
			//numberSuccessesInPopulation = total number of genes annotated to this term in the whole gene wiki 
			int nHitsInPopulation = 0;
			if(go_gw_genes.get(goterm)!=null){
				Set<String> genesfromgw = go_gw_genes.get(goterm);
				//from both
				nHitsInPopulation = genesfromgw.size();
			}
			//sampleSize = number of genes in the gene wiki linked to the GO process being considered --- by the GO
			int sampleSize = 0;
			if(genesfromgo!=null){
				//only genes that are in the gene wiki (and have at least one go annotation)
				genesfromgo.retainAll(gene_gw_go.keySet());
				sampleSize = genesfromgo.size();
			}
			if(sampleSize==0){ //indicates that none of the genes linked to this go term by the goa are present in the gene wiki and have a go annotation
				System.out.println(goterm+"\t"+acc2go.get(goterm).getTerm()+"\t"+acc2go.get(goterm).getRoot()+"\t"+populationSize+"\t"+nHitsInPopulation+"\t"+sampleSize+"\t"+0+"\tNA");
				continue;
			}
			//successesInSample = number of genes in the sample linked to the GO process being considered --- by the Gene Wiki
			int hitsInSample = 0;
			for(String gene : genesfromgo){
				if(gene_gw_go.get(gene)!=null){
					for(String go : gene_gw_go.get(gene)){
						if(go!=null&&go.equals(goterm)){
							hitsInSample++;
						}
					}
				}
			}

			double p = StatFun.hypergeoTest(populationSize, nHitsInPopulation, sampleSize, hitsInSample);
			System.out.println(goterm+"\t"+acc2go.get(goterm).getTerm()+"\t"+acc2go.get(goterm).getRoot()+"\t"+populationSize+"\t"+nHitsInPopulation+"\t"+sampleSize+"\t"+hitsInSample+"\t"+p);
		}
	}
}
