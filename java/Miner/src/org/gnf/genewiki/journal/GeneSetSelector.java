/**
 * 
 */
package org.gnf.genewiki.journal;

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

import org.apache.axis2.AxisFault;
import org.gnf.genewiki.Config;
import org.gnf.genewiki.GeneWikiPage;
import org.gnf.genewiki.GeneWikiUtils;
import org.gnf.genewiki.Heading;
import org.gnf.genewiki.Sentence;
import org.gnf.genewiki.associations.CandidateAnnotation;
import org.gnf.genewiki.associations.CandidateAnnotations;
import org.gnf.genewiki.associations.Filter;
import org.gnf.genewiki.mapping.GenericTextToAnnotation;
import org.gnf.genewiki.ncbi.PubMed;
import org.gnf.ncbo.Ontologies;
import org.gnf.ncbo.web.AnnotatorClient;
import org.gnf.ncbo.web.NcboAnnotation;

/**
 * Manage the process of selecting interesting groups of genes.  The groups should 'go together' such that review articles about them
 * would make sense showing up together in a special edition of a journal.  We will use the NCBI and NCBO to do this.
 * @author bgood
 *
 */
public class GeneSetSelector {

	public static String gene2pubmedfile = "/Users/bgood/data/bioinfo/gene2pubmed";
	public static String pubcache = "/Users/bgood/data/bioinfo/pubcache.txt";
	public static String annotator_result_cache = "/Users/bgood/data/bioinfo/annotatorcache.txt";
	public static String pmid_mesh_cache = "/Users/bgood/data/bioinfo/pmidmeshcache.txt";
	public static String gw_mesh_cache = "/Users/bgood/data/bioinfo/gwmeshcache.txt";
	Map<String, List<String>> gene2pubs;
	Map<String, Set<String>> pub2meshes;
	Map<String, Set<String>> gw2meshes;
	String credfile = "/Users/bgood/workspace/Config/gw_creds.txt";
	public static String gene2gwtitlefile = "/Users/bgood/data/bioinfo/gene_wiki_index.txt";
	Map<String, String> gene2gwtitle;


	public GeneSetSelector() {
		gene2pubs = Prioritizer.getHumanGene2pub(gene2pubmedfile);	
		Map<String, String> creds = GeneWikiUtils.read2columnMap(credfile);
		boolean recalculate_gene_wiki_index = false;
		gene2gwtitle = GeneWikiUtils.getGeneWikiGeneIndex(gene2gwtitlefile, recalculate_gene_wiki_index, creds);
		pub2meshes = PubMed.getCachedPmidMeshMap(pmid_mesh_cache);
		gw2meshes = getCachedGWMeshMap(gw_mesh_cache);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	}

	public static void test(){
		GeneSetSelector g = new GeneSetSelector();
		Set<String> reln_pm_sig = g.getPubmedMeshSignatureForGene("5649");
		Set<String> pin1_pm_sig = g.getPubmedMeshSignatureForGene("5300");
		Set<String> reln_gw_sig = g.getGeneWikiMeshSignatureForGene("5649", 100);	
		Set<String> pin1_gw_sig = g.getGeneWikiMeshSignatureForGene("5300", 100);

		System.out.println("rel "+reln_pm_sig.size()+"\t"+reln_gw_sig.size());
		reln_pm_sig.retainAll(reln_gw_sig);
		System.out.println("rel intersect "+reln_pm_sig.size());
		System.out.println(reln_pm_sig);
		System.out.println("pin "+pin1_pm_sig.size()+"\t"+pin1_gw_sig.size());
		pin1_pm_sig.retainAll(pin1_gw_sig);
		System.out.println("pin intersect "+pin1_pm_sig.size());
		System.out.println(pin1_pm_sig);
	}
	
	public static Map<String, Set<String>> getCachedGWMeshMap(String gwmeshcache){
		Map<String, Set<String>> gw_meshs = new HashMap<String, Set<String>>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(gwmeshcache));
			String line = f.readLine();
			while(line!=null){
				String[] row = line.split("\t");
				String pmid = row[0];
				String[] meshes = row[1].split("::");
				Set<String> mshset = new HashSet<String>();
				for(String m : meshes){
					mshset.add(m);
				}
				gw_meshs.put(pmid, mshset);
				line = f.readLine();
			}
			f.close();
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return gw_meshs;
	}

	/**
	 * For a givn NCBI gene id, get the corresponding gene wiki entry, if any, retrieve the text, and return Mesh terms found in the text
	 * @param gene_id
	 * @return
	 */
	public Set<String> getGeneWikiMeshSignatureForGene(String gene_id, int sentence_limit){	
		//first check cache
		if(gw2meshes.get(gene_id)!=null){
			return gw2meshes.get(gene_id);
		}
		//otherwise go get it (and store it)

		long t0 = System.currentTimeMillis();
		Set<String> terms = new HashSet<String>();
		GeneWikiPage page = new GeneWikiPage();
		page.setNcbi_gene_id(gene_id);
		String title = gene2gwtitle.get(gene_id);
		page.setTitle(title);
		if(title==null){
			return null;
		}
		page.controlledPopulate(true, false, true, false, false, true, true, false);
		int ignoreafterindex = page.getPageContent().lastIndexOf("References");
		List<Sentence> sentences = page.getSentences();
		if(sentences==null||sentences.size()==0){
			return null;
		}
		int c = 0;
		for(Sentence s : sentences){
			c++;
			Heading heading = page.getHeadingByTextIndex(s.getStartIndex());
			String text = s.getPrettyText();
			if(text==null||text.length()<5){
				continue;
			}
			boolean allowSynonyms = true;
			List<CandidateAnnotation> annos = GenericTextToAnnotation.annotateTextWithNCBO_OneOnt("gene wiki", gene_id, s.getPrettyText(), allowSynonyms, Ontologies.MESH_ONT );
			if(annos==null||annos.size()==0){
				continue;
			}
			for(CandidateAnnotation canno : annos){
				terms.add(canno.getTarget_preferred_term());
			}
			if(c>sentence_limit){
				break;
			}
		}
		System.out.println("10 sentences in "+((System.currentTimeMillis()-t0)/1000)+" secs.");
		//add this to the cache
		if(terms!=null&&terms.size()>0){
			FileWriter f;
			try {
				f = new FileWriter(gw_mesh_cache, true);

				String meshterms =  "";
				for(String mesh : terms){
					meshterms+= mesh+"::";						
				}
				if(meshterms.length()>1){
					meshterms = meshterms.substring(0, meshterms.length()-2);
					f.write(gene_id+"\t"+meshterms+"\n");
				}
				f.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return terms;
	}

	/**
	 * Find the set of MeSH keywords associated with the articles associated with this gene.
	 * Uses Entrez tools (and local cache) to get the title and abstract of articles linked in the gene2pmid file from NCBI.
	 * Runs the Annotator on the text to find MeSH terms.
	 * Returns set of preferred labels for the MeSH terms.
	 * @param gene_id
	 * @return
	 */
	public Set<String> getPubmedMeshSignatureForGeneWithAnnotator(String gene_id){
		Set<String> terms = new HashSet<String>();
		//get pmids and text from abstracts and tiles linked to this gene by NCBI (relies on local copy of gene2pubmed)
		Set<String> pmids = new HashSet<String>(gene2pubs.get(gene_id));
		boolean usecache = true;
		Map<String, String> pmid_texts = null;
		try {
			pmid_texts = PubMed.getPubmedTitlesAndAbstracts(pmids, pubcache, usecache);
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean allowSynonyms = true;
		for(Entry<String, String> pmid_text : pmid_texts.entrySet()){
			List<CandidateAnnotation> annos = GenericTextToAnnotation.annotateTextWithNCBO_OneOnt(pmid_text.getKey(), gene_id, pmid_text.getValue(), allowSynonyms, Ontologies.MESH_ONT);
			terms.addAll(annos2signature(annos));
		}
		return terms;
	}

	/**
	 * Use MeSH indexing by NLM to determine a semantic signature for the gene
	 * @param gene_id
	 * @return
	 */
	public Set<String> getPubmedMeshSignatureForGene(String gene_id){
		Set<String> terms = new HashSet<String>();
		Set<String> pmids = new HashSet<String>(gene2pubs.get(gene_id));
		System.out.println("gene\t"+gene_id+"\tpmids\t"+pmids.size());
		try {
			Map<String, Set<String>> pmid_meshes = new HashMap<String, Set<String>>();
			int c = 0;
			for(String pmid : pmids){
				c++;
				if(pub2meshes.get(pmid)!=null){
					pmid_meshes.put(pmid, pub2meshes.get(pmid));
				}else{
					Map<String, Set<String>> fresh = PubMed.getMeshTerms(pmid);
					pub2meshes.put(pmid, fresh.get(pmid));
					//add to cache for next time
					if(fresh!=null&&fresh.size()>0){
						pmid_meshes.put(pmid, fresh.get(pmid));
						FileWriter f = new FileWriter(pmid_mesh_cache, true);
						String meshterms =  "";
						for(String mesh : fresh.get(pmid)){
							meshterms+= mesh+"::";						
						}
						if(meshterms.length()>1){
							meshterms = meshterms.substring(0, meshterms.length()-2);
							f.write(pmid+"\t"+meshterms+"\n");
						}
						f.close();
					}
				}
				if(c%10==0){
					System.out.println("through "+c);
				}
			}
			for(Entry<String, Set<String>> pmid_mesh : pmid_meshes.entrySet()){
				terms.addAll(pmid_mesh.getValue());
			}
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return terms;
	}



	public Set<String> annos2signature(List<CandidateAnnotation> annos){
		Set<String> terms = new HashSet<String>();
		for(CandidateAnnotation anno : annos){
			terms.add(anno.getTarget_preferred_term());
		}
		return terms;
	}

}
