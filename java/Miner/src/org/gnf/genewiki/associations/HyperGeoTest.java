/**
 * 
 */
package org.gnf.genewiki.associations;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Map.Entry;

import org.apache.axis2.AxisFault;
import org.gnf.genewiki.Config;
import org.gnf.genewiki.mapping.GenericTextToAnnotation;
import org.gnf.genewiki.ncbi.PubMed;
import org.gnf.go.Annotation;
import org.gnf.go.GOterm;
import org.gnf.util.FileFun;
import org.gnf.util.MapFun;
import org.gnf.util.StatFun;

/**
 * @author bgood
 *
 */
public class HyperGeoTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		//for each go term in gene2go
		//get the genes related to it 
		//get the go terms related to those genes via the annotator and associated abstracts
		//test 1, what fraction of the go_gene associations are rediscovered via the annotator-abstract process?
		
		boolean skipIEA = false;
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
		abcannos.loadAndFilterCandidateGOAnnotations(abannos);
		Map<String, Set<String>> gene_ab_gos = abcannos.getGene2OntoMap();
		Map<String, Set<String>> go_ab_genes = MapFun.flipMapStringSetStrings(gene_ab_gos);
		
		for(Entry<String, Set<String>> go_genes : go_geness.entrySet()){
			String goterm = go_genes.getKey();
			Set<String> genes_f_goa = go_genes.getValue();
			Set<String> genes_f_ab = go_ab_genes.get(goterm);
			int total_goa_ = genes_f_goa.size();
			int total_not_goa_ = total_genes_from_goa - total_goa_;
			int total_goa_not_ab_ = 0;
			int total_ab_ = 0;
			int total_ab_goa_ = 0;
			int total_ab_not_goa_ = 0;
			int total_not_goa_not_ab = 0;
			if(genes_f_ab!=null){
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
				double chi_p = StatFun.chiSquareTest(total_ab_goa_, total_ab_not_goa_, total_goa_not_ab_, total_not_goa_not_ab);
			}
			
		}
		
		
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
		Map<String, Set<String>> go_genes = MapFun.flipMapStringGOs(gene_gos);
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
		cannolist.loadAndFilterCandidateGOAnnotations(annos);
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
