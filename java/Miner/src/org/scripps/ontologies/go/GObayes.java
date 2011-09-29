package org.scripps.ontologies.go;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.genewiki.GeneWikiUtils;
import org.genewiki.annotationmining.Config;
import org.scripps.util.BioInfoUtil;

public class GObayes {

	HashMap<String, Set<GOterm>> gene_gos;
	double total_genes;
	HashMap<GOterm, Set<String>> go_genes;
	double total_gos;

	public GObayes(boolean addParents){
		//get goa
		gene_gos = GeneWikiUtils.readGene2GO(Config.gene_go_file, addParents);
		total_genes = gene_gos.size();
		go_genes = flipGeneGoMap(gene_gos);
		total_gos = go_genes.size();
		System.out.println("total genes: "+total_genes+" total_gos: "+total_gos);
	}
	
	public GObayes(boolean addParents, GOowl gol){
		//get goa
		gene_gos = Annotation.readGene2GOtrackEvidence(Config.gene_go_file);
		gene_gos = (HashMap<String, Set<GOterm>>) BioInfoUtil.expandGoMap(gene_gos, gol, null, false);
		total_genes = gene_gos.size();
		go_genes = flipGeneGoMap(gene_gos);
		total_gos = go_genes.size();
		System.out.println("total genes: "+total_genes+" total_gos: "+total_gos);
	}

	public void cleanup(){
		gene_gos.clear();
	    gene_gos = null;
	    go_genes.clear();
	    go_genes = null;
	    try {
			finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void finalize () throws Throwable {
		super.finalize();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GObayes gob = new GObayes(false);
		//String geneid = "983";
		GOterm term = gob.getGObyAccN("0005576", ""); //"extracellular");
//		GOterm term = gob.getGObyAccN("0003824", ""); //enzyme
		System.out.println(gob.priorForGO(term));
		
	/*	double l = gob.likelihood(geneid, term);
		System.out.println("done 1 "+l+"\n");

		term = gob.getGObyAccN("0005634", "nucleus");
		l = gob.likelihood(geneid, term);
		System.out.println("done 2 "+l);
		*/
	}


	public double priorForGO(GOterm term){
		Set<String> term_genes = go_genes.get(term);	
		if(term_genes==null){
			return 0;
		}
		double prior_for_term_true = (double)term_genes.size()/total_genes;
		return prior_for_term_true;
	}

	/**
	 * Return the likelihood of this gene getting this GO annotation based on all of the GO annotations	
	 * @param geneid
	 * @param term
	 * @return
	 */
	public double likelihood(String geneid, GOterm term){

		double L = 0;

		Set<String> term_genes = go_genes.get(term);
		Set<String> not_term_genes = new HashSet<String>(gene_gos.keySet());
		if(term_genes==null){
			term_genes = new HashSet<String>();
		}
		not_term_genes.removeAll(term_genes);

		Set<GOterm> gene_terms = gene_gos.get(geneid);
		Set<GOterm> not_gene_terms = new HashSet<GOterm>(go_genes.keySet());
		if(gene_terms ==null){
			gene_terms = new HashSet<GOterm>();
		}
		not_gene_terms.removeAll(gene_terms);

		double n_genes_with_term = (double)term_genes.size();
		double prior_for_term_true = n_genes_with_term/total_genes;
		double n_genes_without_term = (double)not_term_genes.size();
		double prior_for_term_false = n_genes_without_term/total_genes;


		//calculate a dependent probability for this go term given each other go term's presence or absence
		double p_term_given_dterm = 1;
		//calculate a dependent probability for this go term given each other go term's presence or absence
		double not_p_term_given_dterm = 1;

		for(GOterm dterm : go_genes.keySet()){
			if(dterm==term){
				continue;
			}
			if(gene_terms.contains(dterm)){
				//for the gene in question, this dterm is used as an annotation, 
				//so calculate the frequency that the test go term is used for the 
				//same genes as this one
				Set<String> genes_dterm_true = new HashSet<String>(go_genes.get(dterm));
				genes_dterm_true.retainAll(term_genes);
				p_term_given_dterm = p_term_given_dterm*(1 + (double)genes_dterm_true.size())/(1 + n_genes_with_term);

				//reverse
				//get all genes
				Set<String> not_genes_dterm_false = new HashSet<String>(gene_gos.keySet());
				//get just the genes that are not annotated with dterm
				not_genes_dterm_false.removeAll(go_genes.get(dterm));
				//get the genes that are not annotated with this dterm but are annotated with the term in question
				not_genes_dterm_false.retainAll(not_term_genes);
				not_p_term_given_dterm = not_p_term_given_dterm*(1 + (double)not_genes_dterm_false.size())/(1 + n_genes_without_term);

				if(not_p_term_given_dterm==0){
					System.out.println("not p = 0");
				}
			}
			/*This is commented becase a) its slow and b) the number of go terms not used for any particular gene is very high - the product of the resulting frequencies becomes zero..
			 * 
			 * 		else{
				//for the gene in question, this dterm is not used as an annotation,
				//so calculate the frequency that the test go term is NOT used for the //same genes as this one

				//get all genes
				Set<String> genes_dterm_false = new HashSet<String>(gene_gos.keySet());
				//get just the genes that are not annotated with dterm
				genes_dterm_false.removeAll(go_genes.get(dterm));
				//get the genes that are not annotated with this dterm but are annotated with the term in question
				genes_dterm_false.retainAll(term_genes);
				p_term_given_dterm = p_term_given_dterm*(1 + (double)genes_dterm_false.size())/(1 + n_genes_with_term);

				//reverse
				Set<String> not_genes_dterm_true = new HashSet<String>(go_genes.get(dterm));
				not_genes_dterm_true.retainAll(not_term_genes);
				double dd = (1 + (double)not_genes_dterm_true.size())/(1 + n_genes_without_term);						
				not_p_term_given_dterm = not_p_term_given_dterm*dd;			
			}
			 */

		}

		double l_yes = p_term_given_dterm*prior_for_term_true;
		double l_no = not_p_term_given_dterm*prior_for_term_false;
		L = l_yes/(l_yes+l_no);
		/*		System.out.println("prior true: "+prior_for_term_true);
		System.out.println("prior false: "+prior_for_term_false);
		System.out.println("L true: "+l_yes);
		System.out.println("L false: "+l_no);
		System.out.println("L : "+L);
		 */		
		return L;
	}


	public static double freqOfSet1inSet2(Set set1, Set set2){
		Set intersect = new HashSet(set2);
		intersect.retainAll(set1);
		return (double)intersect.size()/(double)set2.size();
	}



	public static double naiveB(Set<Set> groups, Set group2, double total){
		double b = 1;
		double notb = 1;
		double prior2 = (double)group2.size()/total;
		double notprior2 = (total - (double)group2.size())/total;		
		for(Set group1 : groups){
			Set intersect = new HashSet(group2);
			intersect.retainAll(group1);
			double i = (double)intersect.size();
			double likelihood_g2_given_g1 = i/(double)group1.size();			
			double likelihood_not_g2_given_g1 = ((double)group1.size()-i)/(double)group1.size();
			b = b*likelihood_g2_given_g1;
			notb = notb*likelihood_not_g2_given_g1;
		}
		b = b*prior2;
		notb = notb*notprior2;

		b = b/(b + notb);

		return b;
	}

	public static double naive(Set group1, Set group2, double total){
		double b = 0;
		//		double prior1 = (double)group1.size()/total;
		double prior2 = (double)group2.size()/total;
		double notprior2 = (total - (double)group2.size())/total;
		Set intersect = new HashSet(group2);
		intersect.retainAll(group1);
		double i = (double)intersect.size();
		double likelihood_g2_given_g1 = i/(double)group1.size()*prior2;
		double likelihood_not_g2_given_g1 = ((double)group1.size()-i)/(double)group1.size()*notprior2;
		b = likelihood_g2_given_g1/(likelihood_g2_given_g1 + likelihood_not_g2_given_g1);
		return b;
	}

	public static double influence(Set group1, Set group2, double total){
		double prior2 = (double)group2.size()/total;
		double b = naive(group1, group2, total);
		return b - prior2;
	}

	public static HashMap<GOterm, Set<String>> flipGeneGoMap(HashMap<String, Set<GOterm>> gene_gos){
		HashMap<GOterm, Set<String>> go_genes = new HashMap<GOterm, Set<String>>();
		for(Entry<String, Set<GOterm>> gene_go : gene_gos.entrySet()){
			String gene = gene_go.getKey();
			for(GOterm go : gene_go.getValue()){
				Set<String> genes = go_genes.get(go);
				if(genes==null){
					genes = new HashSet<String>();
				}
				genes.add(gene);
				go_genes.put(go, genes);
			}
		}
		return go_genes;
	}

	public GOterm getGObyAccN(String acc, String term){
		GOterm d = new GOterm(null, "GO:"+acc, "Function", term, true);
		d.setEvidence("ncbi");
		return d;
	}

	public void writeOutGOAMatrix(){
		try {
			FileWriter writer = new FileWriter("C:\\Users\\bgood\\data\\genewiki\\gogrid.csv");
			int cols = 0;
			writer.write("gene");
			cols++;
			for(GOterm goterm : go_genes.keySet()){
				cols++;
				writer.write(","+goterm.getAccession());			
			}
			System.out.println("cols "+cols);
			writer.write("\n");
			int rcols = cols;

			for(Entry<String, Set<GOterm>> gene_go: gene_gos.entrySet()){			
				cols = 0;
				writer.write(gene_go.getKey());
				cols++;
				for(GOterm goterm : go_genes.keySet()){		
					if(gene_go.getValue().contains(goterm)){
						cols++;
						writer.write(",1");
					}else{
						cols++;
						writer.write(",0");
					}
				}
				if(cols!=rcols){
					System.out.println("cols = "+cols);
					return;
				}
				writer.write("\n");
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public HashMap<String, Set<GOterm>> getGene_gos() {
		return gene_gos;
	}

	public void setGene_gos(HashMap<String, Set<GOterm>> geneGos) {
		gene_gos = geneGos;
	}

	public double getTotal_genes() {
		return total_genes;
	}

	public void setTotal_genes(double totalGenes) {
		total_genes = totalGenes;
	}

	public HashMap<GOterm, Set<String>> getGo_genes() {
		return go_genes;
	}

	public void setGo_genes(HashMap<GOterm, Set<String>> goGenes) {
		go_genes = goGenes;
	}

	public double getTotal_gos() {
		return total_gos;
	}

	public void setTotal_gos(double totalGos) {
		total_gos = totalGos;
	}

	/**

		Set<String> intersect = new HashSet<String>(dnabindinggenes);
		intersect.retainAll(cytoplasmgenes);

		System.out.println("i = "+intersect.size()+" dnab = "+dnabindinggenes.size()+" cytob = "+cytoplasmgenes.size());
		System.out.println("fraction cytoplasm annotations when dnabinding annotation "+(double)intersect.size()/(double)cytoplasmgenes.size());
		System.out.println("fraction dnab annotations when cyto annotation "+(double)intersect.size()/(double)dnabindinggenes.size());
		double priorcyto = (double)cytoplasmgenes.size()/total_genes;
		double priordnab = (double)dnabindinggenes.size()/total_genes;
		System.out.println(priorcyto+" "+priordnab);

		double l_dnab_given_c = (double)intersect.size()/(double)cytoplasmgenes.size()*priordnab;
		System.out.println("likelihood of DNAbinding anno given cytoplasm anno = "+l_dnab_given_c);

		double nl_dnab_given_c = ((double)cytoplasmgenes.size() - (double)intersect.size())/(double)cytoplasmgenes.size()*priordnab;

		System.out.println("likelihood of NOT DNAbinding anno given cytoplasm anno = "+nl_dnab_given_c);
		double pDNA = l_dnab_given_c/(l_dnab_given_c + nl_dnab_given_c);
		System.out.println("pretty P of dna given cyto: "+pDNA);
		System.out.println("likelihood of cyto anno given DNAbinding anno = "+(double)intersect.size()/(double)dnabindinggenes.size()*priorcyto+" prior cyto "+priorcyto);

	 */
}
