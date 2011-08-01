/**
 * 
 */
package org.gnf.genewiki.journal;

import java.io.BufferedReader;
import java.io.File;
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
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
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
import org.gnf.umls.TypedTerm;
import org.gnf.umls.UmlsDb;
import org.gnf.umls.metamap.MMannotation;
import org.gnf.umls.metamap.MetaMap;
import org.gnf.util.MapFun;
import org.gnf.util.SetComparison;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Manage the process of selecting interesting groups of genes.  The groups should 'go together' such that review articles about them
 * would make sense showing up together in a special edition of a journal.  We will use the NCBI and NCBO (or MetaMap) to do this.
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
	Map<String, Double> mesh2pubmedfreq;


	public GeneSetSelector() {
		gene2pubs = Prioritizer.getHumanGene2pub(gene2pubmedfile);	
		Map<String, String> creds = GeneWikiUtils.read2columnMap(credfile);
		boolean recalculate_gene_wiki_index = false;
		gene2gwtitle = GeneWikiUtils.getGeneWikiGeneIndex(gene2gwtitlefile, recalculate_gene_wiki_index, creds);
		pub2meshes = PubMed.getCachedPmidMeshMap(pmid_mesh_cache);
		gw2meshes = getCachedGWMeshMap(gw_mesh_cache);

		mesh2pubmedfreq = new HashMap<String, Double>();
		double total = 0;
		//make counts
		for(Entry<String, Set<String>> pub_meshes : pub2meshes.entrySet()){
			total++;
			for(String mesh : pub_meshes.getValue()){
				Double c = mesh2pubmedfreq.get(mesh);
				if(c==null){
					c = new Double(0);
				}
				c++;
				mesh2pubmedfreq.put(mesh, c);
			}
		}

		//make frequencies
		for(String mesh : mesh2pubmedfreq.keySet()){
			Double c = mesh2pubmedfreq.get(mesh);
			c = c/total;
			mesh2pubmedfreq.put(mesh, c);
		}

		//test gene getPubmedMeshSignatureForGene




		//		System.out.println(total);
		//		List sorted_mesh = MapFun.sortMapByValue(mesh2pubmedfreq);
		//		Collections.reverse(sorted_mesh);
		//		System.out.println("most frequent "+sorted_mesh.get(0)+" "+mesh2pubmedfreq.get(sorted_mesh.get(0)));
		//		System.out.println("most frequent "+sorted_mesh.get(1)+" "+mesh2pubmedfreq.get(sorted_mesh.get(1)));
		//		//				System.out.println(mesh2pubmedfreq.get("Humans"));
		//		//				Humans 233219.0 0.4712008986838968
		//		//				Angiocardiography 1.0 2.020422429921648E-6
		//		System.out.println("least frequent "+sorted_mesh.get(sorted_mesh.size()-2)+" "+mesh2pubmedfreq.get(sorted_mesh.get(sorted_mesh.size()-2)));
		//		System.out.println("least frequent "+sorted_mesh.get(sorted_mesh.size()-3)+" "+mesh2pubmedfreq.get(sorted_mesh.get(sorted_mesh.size()-3)));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GeneSetSelector g = new GeneSetSelector();
		//		System.out.println("g2p size "+g.gene2pubs.keySet().size()); //30768
		///////////////////
		//Gather data
		//////////////////
//		String infile = "/Users/bgood/data/journal_collab/1st_edition_gene_data_freq.txt";
//		String outfile = "/Users/bgood/data/journal_collab/1st_edition_gene_data_freq2.txt";
//		String exloutfile = "/Users/bgood/data/journal_collab/1st_edition_gene_data_freq2_exl.txt";
//		int limit = 100000;
//		int minPubs = 10;
//		boolean useannotator = false;
//		//g.gatherGeneProfiles(limit, minPubs, useannotator, infile, outfile);
//		g.convertToExcellableFile(outfile, exloutfile);
		///////////////////
		//Identify interesting MeSH topics
		//////////////////
//		String infile = "/Users/bgood/data/journal_collab/1st_edition_gene_data_freq2.txt";
//		String meshoutfile = "/Users/bgood/data/journal_collab/1st_edition_mesh_data_freq2.txt";
//		g.buildMeshProfiles(infile, meshoutfile);

		///////////////////
		//build some specific reports
		//////////////////		
		String mesh = "Asperger Syndrome";//"Endocarditis, Bacterial"; //"Isoantigens";//Wounds and Injuries
		String geneprofiles = "/Users/bgood/data/journal_collab/1st_edition_gene_data_freq2.txt";
		String meshout = "/Users/bgood/data/journal_collab/Asperger_Syndrome.txt";
		g.makeReportforMeSHterm(geneprofiles, mesh, meshout);
		mesh = "Wounds and Injuries";
		meshout = "/Users/bgood/data/journal_collab/Wounds_and_Injuries.txt";
		g.makeReportforMeSHterm(geneprofiles, mesh, meshout);
		mesh = "Social Perception";
		meshout = "/Users/bgood/data/journal_collab/Social_Perception.txt";
		g.makeReportforMeSHterm(geneprofiles, mesh, meshout);
		mesh = "Musculoskeletal Diseases";
		meshout = "/Users/bgood/data/journal_collab/Musculoskeletal_Diseases.txt";
		g.makeReportforMeSHterm(geneprofiles, mesh, meshout); 
		mesh = "Glucose Metabolism Disorders";
		meshout = "/Users/bgood/data/journal_collab/Glucose_Metabolism_Disorders.txt";
		g.makeReportforMeSHterm(geneprofiles, mesh, meshout);
	}


	public void makeReportforMeSHterm(String genefile, String term, String meshout){
		Map<String, GeneProfile> gene_profiles = readGeneProfiles(genefile);
		Map<String, List<GeneProfile>> mesh_profiles = new HashMap<String, List<GeneProfile>>();

		for(Entry<String, GeneProfile> gene_profile : gene_profiles.entrySet()){
			for(String pubmesh : gene_profile.getValue().pub_mesh_freq.keySet()){
				List<GeneProfile> mp = mesh_profiles.get(pubmesh);
				if(mp==null){
					mp = new ArrayList<GeneProfile>();
				}
				mp.add(gene_profile.getValue());
				mesh_profiles.put(pubmesh, mp);
			}
		}
		try {
			FileWriter f = new FileWriter(meshout);
			f.write(getGeneProfileHeader()+"termcount\tconnecting_pmids"+"\n");
			for(GeneProfile gp : mesh_profiles.get(term)){
				//measure relevance of this gene to this topic, also report pubmed ids..
				List<String> pubs = gene2pubs.get(gp.page.getNcbi_gene_id());
				String pmids = "";
				for(String pub : pubs){
					Set<String> meshes = pub2meshes.get(pub);
					if(meshes!=null){
						if(meshes.contains(term)){
							pmids+=pub+",";
						}
					}
				}
				f.write(gp.getAsString(false)+gp.pub_mesh_freq.get(term)+"\t"+pmids+"\n");
			}
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void buildMeshProfiles(String infile, String outfile){
		Map<String, GeneProfile> gene_profiles = readGeneProfiles(infile);
		Map<String, List<GeneProfile>> mesh_profiles = new HashMap<String, List<GeneProfile>>();
		try {
			FileWriter f = new FileWriter(outfile);
			f.write(getMeshProfileHeader()+"\n");
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(Entry<String, GeneProfile> gene_profile : gene_profiles.entrySet()){
			String gene_id = gene_profile.getKey();
			for(String pubmesh : gene_profile.getValue().pub_mesh_freq.keySet()){
				List<GeneProfile> mp = mesh_profiles.get(pubmesh);
				if(mp==null){
					mp = new ArrayList<GeneProfile>();
				}
				mp.add(gene_profile.getValue());
				mesh_profiles.put(pubmesh, mp);
			}
		}
		for(Entry<String, List<GeneProfile>> mesh_profile : mesh_profiles.entrySet()){
			MeshProfile mp = new MeshProfile(mesh_profile.getKey(), mesh_profile.getValue());
			try {
				FileWriter f = new FileWriter(outfile, true);
				f.write(mp.getAsString()+"\n");
				f.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private String getMeshProfileHeader(){
		return 
		"Mesh_Term\t"+
		"Overall_pubmed_frequency\t"+
		"Gene_count\t"+
		"Median_overlap_in_genes\t" +
		"N genes linked to term more than onces\t" +
//		"Mean_overlap_in_genes\t" +
//		"Max_overlap_in_in_genes\t" +
//		"Min_overlap_in_in_genes\t" +
		"Genes\t";		
	}

	class MeshProfile{
		String term;
		int n_genes;
		int n_multi_linked_genes;
		DescriptiveStatistics f_overlaps;
		DescriptiveStatistics intersects;
		Set<String> genes;

		MeshProfile(String t, List<GeneProfile> gene_profiles){
			term = t;
			n_genes = gene_profiles.size();
			f_overlaps = new DescriptiveStatistics();
			intersects = new DescriptiveStatistics();
			genes = new HashSet<String>();
			n_multi_linked_genes = 0;
			for(GeneProfile gp : gene_profiles){
				genes.add(gp.page.getNcbi_gene_id());
				intersects.addValue(gp.sc.getSet_intersection());
				if(gp.sc.getSet_intersection()>0){
					f_overlaps.addValue(gp.sc.getF());
				}else{
					f_overlaps.addValue(0);
				}
				//find multi linked genes
				Double n = gp.pub_mesh_freq.get(t);
				if(n>1){
					n_multi_linked_genes++;
				}
			}
		}

		String getAsString(){
			return term+"\t"+
			mesh2pubmedfreq.get(term)+"\t"+
			n_genes+"\t"+
			f_overlaps.getPercentile(50)+"\t"+
			n_multi_linked_genes+"\t"+
		//	f_overlaps.getMean()+"\t"+
		//	f_overlaps.getMax()+"\t"+
		//	f_overlaps.getMin()+"\t"+
			genes+"\t";
		}
	}

	public void convertToExcellableFile(String existingfile, String outfile){
		Map<String, GeneProfile> pros = new HashMap<String, GeneProfile>();
		File test = new File(outfile);
		if(!test.exists()){
			try {
				FileWriter f = new FileWriter(outfile);
				f.write(getGeneProfileHeader()+"\n");
				f.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		test = new File(existingfile);
		if(test.exists()){
			pros = readGeneProfiles(existingfile);
		}

		for(GeneProfile p : pros.values()){	
			//write the profile out to the new file
			try {
				FileWriter f = new FileWriter(outfile, true);
				f.write(p.getAsString(false)+"\n");
				//System.out.println(p.getAsString(false));
				f.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * This runs the workflow for running the semantic comparison on the genes
	 * @param limit
	 * @param minPubs
	 * @param useannotator
	 * @param outfile
	 */
	public void gatherGeneProfiles(int limit, int minPubs, boolean useannotator, String existingfile, String outfile){
		Map<String, GeneProfile> pros = new HashMap<String, GeneProfile>();
		File test = new File(outfile);
		if(!test.exists()){
			try {
				FileWriter f = new FileWriter(outfile);
				f.write(getGeneProfileHeader()+"\n");
				f.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		test = new File(existingfile);
		if(test.exists()){
			pros = readGeneProfiles(existingfile);
		}
		int c = 0;
		//first transfer the oes we have to the new files
		for(GeneProfile p : pros.values()){	
			c++;
			//write the profile out to the new file
			try {
				FileWriter f = new FileWriter(outfile, true);
				f.write(p.getAsString(true)+"\n");
				f.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("transferred "+c);
		//now start gathering more
		for(String gene_id : gene2pubs.keySet()){
			c++;
			System.out.println(c+" working on "+gene_id);
			GeneProfile p = pros.get(gene_id);
			//			//check to see if we need to actually go get this one (if not we already had it..)
			if(gene2pubs.get(gene_id).size()>=minPubs&&p==null){ 
				p = profileGene(gene_id, useannotator);
				System.out.println(c+" profiling "+gene_id);
			}
			if(c>= limit){
				break;
			}
			//write the profile out to the new file
			if(p!=null){
				try {
					FileWriter f = new FileWriter(outfile, true);
					f.write(p.getAsString(true)+"\n");
					System.out.println(p.getAsString(true));
					f.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				System.out.println("nothing found for "+gene_id);
			}
		}
	}

	public Map<String, GeneProfile> readGeneProfiles(String input){
		Map<String, GeneProfile> gps = new HashMap<String, GeneProfile>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(input));
			//skip header
			f.readLine();
			String line = f.readLine();
			while(line!=null){
				String[] row = line.split("\t");
				if(row!=null&&row.length>1){
					String gene_id = row[0];
					String title = row[1];
					String bytes = row[2];
					String sentences = row[3];
					String pmids = row[4];
					String[] pm_meshes = row[9].split("::");
					String[] gw_meshes = row[10].split("::");
					Set<String> gw_mesh_set = new HashSet<String>();
					for(String s : gw_meshes){
						gw_mesh_set.add(s);
					}
					Map<String, Double> mesh_freq = new HashMap<String, Double>();
					for(String s : pm_meshes){
						String[] tu = s.split(";");
						Double d = Double.parseDouble(tu[1]);
						mesh_freq.put(tu[0], d);
					}
					SetComparison s = new SetComparison(mesh_freq.keySet(), gw_mesh_set);
					GeneWikiPage page = new GeneWikiPage();
					page.setNcbi_gene_id(gene_id);
					page.setTitle(title);
					page.setSize(Integer.parseInt(bytes));
					GeneProfile pro = new GeneProfile(s, page, mesh_freq, gw_mesh_set);
					if(sentences!=null&&!sentences.equals("null")){
						pro.gw_sentences = Integer.parseInt(sentences);
					}
					if(pmids!=null&&!pmids.equals("null")){
						pro.pubmed_refs = Integer.parseInt(pmids);
					}
					gps.put(gene_id, pro);
				}
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
		return gps;
	}


	private String getGeneProfileHeader(){
		return 
		"Gene_id\t"+
		"GW_title\t"+
		"GW_bytes\t"+
		"GW_sentences\t"+
		"Pubmed_refs\t"+
		"F-mesh-overlap\t"+
		"Pubmed_Mesh_count\t"+
		"GW_Mesh_count\t"+
		"intersect_count\t"+
		"pub_mesh\t"+
		"gw_mesh\t"+
		"intersect\t" +
		"f-weighted-overlap\t" +
		"f-intersect_count\t" +
		"weighted-intersect\t"+
		"weighted-pub_mesh\t";
	}

	class GeneProfile{
		SetComparison sc;
		GeneWikiPage page;
		int gw_sentences;
		int pubmed_refs;
		Map<String, Double> pub_mesh_freq;
		Set<String> gw_mesh;
		private GeneProfile(SetComparison sc_, GeneWikiPage page_, Map<String, Double> pub_mesh_, Set<String> gw_mesh_){
			sc = sc_;
			page = page_;
			pub_mesh_freq = pub_mesh_;
			gw_mesh = gw_mesh_;
		}

		private String getAsString(boolean keepalltext){
			//limit comparison to more interesting mesh terms
			boolean penalize_prior = true;
			Map<String, Double> pubmeshfreq = count2freq(pub_mesh_freq, penalize_prior);
			List<String> sorted_meshes = MapFun.sortMapByValue(pubmeshfreq);
			Collections.reverse(sorted_meshes);
			int i = 0;
			int max_compare = 50;
			Map<String, Double> pubmeshfreqsolid = new HashMap<String, Double>();
			while(i<max_compare&&i<sorted_meshes.size()){
				pubmeshfreqsolid.put(sorted_meshes.get(i), pubmeshfreq.get(sorted_meshes.get(i)));
				i++;
			}
			SetComparison superset = new SetComparison(pubmeshfreqsolid.keySet(),gw_mesh);
			String weighted_pub_mesh_terms = "";			
			if(pubmeshfreqsolid!=null&&pubmeshfreqsolid.size()>0){
				for(String p : pubmeshfreqsolid.keySet()){
					weighted_pub_mesh_terms+=p+";"+pubmeshfreqsolid.get(p)+"::";
				}
				weighted_pub_mesh_terms = weighted_pub_mesh_terms.substring(0,weighted_pub_mesh_terms.length()-2);
			}

			//do it on all of them
			String pub_mesh_terms = "";			
			int c = 0;
			int maxkeep = 10;
			if(pub_mesh_freq!=null&&pub_mesh_freq.size()>0){
				for(String p : pub_mesh_freq.keySet()){
					pub_mesh_terms+=p+";"+pub_mesh_freq.get(p)+"::";
					c++;
					if(!keepalltext&&c>maxkeep){
						break;
					}
				}
				pub_mesh_terms = pub_mesh_terms.substring(0,pub_mesh_terms.length()-2);
			}
			String gw_mesh_terms = "";
			if(gw_mesh!=null&&gw_mesh.size()>0){
				c = 0;
				for(String p : gw_mesh){
					gw_mesh_terms+=p+"::";
					c++;
					if(!keepalltext&&c>maxkeep){
						break;
					}
				}
				gw_mesh_terms = gw_mesh_terms.substring(0,gw_mesh_terms.length()-2);
			}
			int sentences = 0;
			if(page.getSentences()!=null){
				sentences = page.getSentences().size();
			}
			int pubs = 0;
			if(gene2pubs.get(page.getNcbi_gene_id())!=null){
				pubs = gene2pubs.get(page.getNcbi_gene_id()).size();
			}
			return 
			page.getNcbi_gene_id()+"\t"+
			page.getTitle()+"\t"+
			page.getSize()+"\t"+
			sentences+"\t"+
			pubs+"\t"+
			sc.getF()+"\t"+
			sc.getSet1_size()+"\t"+
			sc.getSet2_size()+"\t"+
			sc.getSet_intersection()+"\t"+
			pub_mesh_terms+"\t"+
			gw_mesh_terms+"\t"+
			sc.getInterset()+"\t"+
			superset.getF()+"\t"+
			superset.getSet_intersection()+"\t"+
			superset.getInterset()+"\t"+
			weighted_pub_mesh_terms+"\t";

		}

	}

	public Map<String, Double> count2freq(Map<String, Double> termcounts, boolean penalize_prior){
		Map<String, Double> termfreq = new HashMap<String, Double>();
		double total = 0;
		for(Entry<String, Double> termcount : termcounts.entrySet()){
			total+=termcount.getValue();
		}
		for(Entry<String, Double> termcount : termcounts.entrySet()){
			double freq = termcount.getValue()/total;
			if(penalize_prior){
				Double basefreq = mesh2pubmedfreq.get(termcount.getKey());
				if(basefreq!=null){
					freq = freq - basefreq;
				}
			}
			termfreq.put(termcount.getKey(), freq);
		}
		return termfreq;
	}

	public GeneProfile profileGene(String gene_id, boolean useannotator){
		Map<String, Double> pm_sig = getPubmedMeshSignatureForGene(gene_id);
		//retrieve text from wikipedia
		GeneWikiPage page = new GeneWikiPage();
		page.setNcbi_gene_id(gene_id);
		String title = gene2gwtitle.get(gene_id);
		page.setTitle(title);
		Set<String> gw_sig = null;
		if(title!=null){
			page.controlledPopulate(true, false, true, false, false, true, true, false);
			gw_sig = getGeneWikiMeshSignatureForGene(page, 10000, useannotator);	
		}
		SetComparison sc = new SetComparison(pm_sig.keySet(), gw_sig);

		return new GeneProfile(sc, page, pm_sig, gw_sig);
	}

	public Map<String, Set<String>> getCachedGWMeshMap(String gwmeshcache){
		Map<String, Set<String>> gw_meshs = new HashMap<String, Set<String>>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(gwmeshcache));
			String line = f.readLine();
			while(line!=null){
				String[] row = line.split("\t");
				if(row!=null&&row.length>1){
					String pmid = row[0];
					String[] meshes = row[1].split("::");
					Set<String> mshset = new HashSet<String>();
					for(String m : meshes){
						mshset.add(m);
					}
					gw_meshs.put(pmid, mshset);
				}
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
	 * For a given gene wiki page, get the corresponding gene wiki entry, if any, retrieve the text, and return Mesh terms found in the text
	 * @param gene_id
	 * @return
	 */
	public Set<String> getGeneWikiMeshSignatureForGene(GeneWikiPage page, int sentence_limit, boolean annotator){	
		//first check cache
		String gene_id = page.getNcbi_gene_id();
		if(gw2meshes.get(gene_id)!=null){
			return gw2meshes.get(gene_id);
		}
		//otherwise go get it (and store it)
		Set<String> terms = new HashSet<String>();
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
			if(annotator){
				boolean allowSynonyms = true;
				List<CandidateAnnotation> annos = GenericTextToAnnotation.annotateTextWithNCBO_OneOnt("gene wiki", gene_id, s.getPrettyText(), allowSynonyms, Ontologies.MESH_ONT );
				if(annos==null||annos.size()==0){
					continue;
				}
				for(CandidateAnnotation canno : annos){
					terms.add(canno.getTarget_preferred_term());
				}
			}else{
				List<MMannotation> cs = MetaMap.getCUIsFromText(s.getPrettyText(), "MSH", false, ""); //"GO,FMA,SNOMEDCT"		
				if(cs!=null&&cs.size()>0){
					for(MMannotation anno : cs){
						terms.add(anno.getTermName());
					}
				}
			}
			if(c>sentence_limit){
				break;
			}
		}
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
	public Set<String> getPubmedMeshSignatureForGeneWithAnnotator(String gene_id, int limit){
		long t0 = System.currentTimeMillis();
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
		int c = 0;
		for(Entry<String, String> pmid_text : pmid_texts.entrySet()){
			List<CandidateAnnotation> annos = GenericTextToAnnotation.annotateTextWithNCBO_OneOnt(pmid_text.getKey(), gene_id, pmid_text.getValue(), allowSynonyms, Ontologies.MESH_ONT);
			terms.addAll(annos2signature(annos));
			c++;
			if(c>limit){
				break;
			}
		}
		System.out.println(c+" abstracts in "+((System.currentTimeMillis()-t0)/1000)+" secs.");
		return terms;
	}

	/**
	 * Use MeSH indexing by NLM to determine a semantic signature for the gene
	 * @param gene_id
	 * @return
	 */
	public Map<String, Double> getPubmedMeshSignatureForGene(String gene_id){
		Map<String, Double> term_freq = new HashMap<String, Double>();
		Set<String> pmids = new HashSet<String>(gene2pubs.get(gene_id));
		System.out.println("gene\t"+gene_id+"\tpmids\t"+pmids.size());
		try {
			Map<String, Set<String>> pmid_meshes = new HashMap<String, Set<String>>();
			int c = 0;
			String pmidlist = "";
			for(String pmid : pmids){
				if(pub2meshes.get(pmid)!=null){
					pmid_meshes.put(pmid, pub2meshes.get(pmid));
				}else{
					pmidlist+=pmid+",";
				}
			}
			if(pmidlist.length()>0){
				pmidlist = pmidlist.substring(0,pmidlist.length()-1);
				Map<String, Set<String>> fresh = PubMed.getMeshTerms(pmidlist);
				if(fresh!=null&&fresh.size()>0){
					for(Entry<String, Set<String>> pmid_mesh : fresh.entrySet()){
						String pmid = pmid_mesh.getKey();
						//add to cache for next time
						pub2meshes.put(pmid, fresh.get(pmid));				
						pmid_meshes.put(pmid, fresh.get(pmid));
						if(fresh.get(pmid)!=null){
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
				}
				if(c%10==0){
					System.out.println("through "+c);
				}
			}
			for(Entry<String, Set<String>> pmid_mesh : pmid_meshes.entrySet()){
				if( pmid_mesh.getValue()!=null){
					//terms.addAll(pmid_mesh.getValue());
					for(String mesh : pmid_mesh.getValue()){
						Double freq = term_freq.get(mesh);
						if(freq==null){
							freq = new Double(0);
						}
						freq++;
						term_freq.put(mesh, freq);
					}
				}
			}
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return term_freq;
	}



	public Set<String> annos2signature(List<CandidateAnnotation> annos){
		Set<String> terms = new HashSet<String>();
		for(CandidateAnnotation anno : annos){
			terms.add(anno.getTarget_preferred_term());
		}
		return terms;
	}

	public void printPubMedsignatureForGene(String testgeneid){
		Map<String, Double> m_sig = getPubmedMeshSignatureForGene(testgeneid);
		GeneWikiPage page = new GeneWikiPage();
		page.setNcbi_gene_id(testgeneid);
		boolean penalize_prior = true;
		Map<String, Double> pubmeshfreq = count2freq(m_sig, penalize_prior);
		List<String> sorted_meshes = MapFun.sortMapByValue(pubmeshfreq);
		Collections.reverse(sorted_meshes);
		int i = 0;
		int max_compare = 30;
		Map<String, Double> pubmeshfreqsolid = new HashMap<String, Double>();
		while(i<max_compare&&i<sorted_meshes.size()){
			pubmeshfreqsolid.put(sorted_meshes.get(i), pubmeshfreq.get(sorted_meshes.get(i)));
			i++;
		}
		//	Set<String> gw_mesh = getGeneWikiMeshSignatureForGene(page, 1000000, false);
		//	SetComparison superset = new SetComparison(pubmeshfreqsolid.keySet(),gw_mesh);
		//	String weighted_pub_mesh_terms = "";			
		if(pubmeshfreqsolid!=null&&pubmeshfreqsolid.size()>0){
			for(String p : pubmeshfreqsolid.keySet()){
				System.out.println(p+"\t"+m_sig.get(p)+"\t"+pubmeshfreqsolid.get(p)+"\t"+mesh2pubmedfreq.get(p));
			}
		}
	}
}
