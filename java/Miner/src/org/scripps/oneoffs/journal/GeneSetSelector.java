/**
 * 
 */
package org.scripps.oneoffs.journal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.axis2.AxisFault;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.genewiki.GeneWikiPage;
import org.genewiki.GeneWikiUtils;
import org.genewiki.Heading;
import org.genewiki.ProteinPage;
import org.genewiki.Sentence;
import org.genewiki.annotationmining.Config;
import org.genewiki.annotationmining.annotations.CandidateAnnotation;
import org.genewiki.annotationmining.annotations.CandidateAnnotations;
import org.genewiki.annotationmining.annotations.Filter;
import org.genewiki.metrics.VolumeReport;
import org.scripps.datasources.MyGeneInfo;
import org.scripps.datasources.ncbi.PubMed;
import org.scripps.nlp.ncbo.GenericTextToAnnotation;
import org.scripps.nlp.ncbo.Ontologies;
import org.scripps.nlp.ncbo.web.AnnotatorClient;
import org.scripps.nlp.ncbo.web.NcboAnnotation;
import org.scripps.nlp.umls.TypedTerm;
import org.scripps.nlp.umls.UmlsDb;
import org.scripps.nlp.umls.metamap.MMannotation;
import org.scripps.nlp.umls.metamap.MetaMap;
import org.scripps.ontologies.mesh.MeshRDF;
import org.scripps.util.BioInfoUtil;
import org.scripps.util.MapFun;
import org.scripps.util.SetComparison;

import com.hp.hpl.jena.ontology.OntClass;

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
	public static String pmid_author_cache = "/Users/bgood/data/bioinfo/pmidauthorcache.txt";
	public static String gw_mesh_cache = "/Users/bgood/data/bioinfo/gwmeshcache.txt";
	public static String gene_wiki_article_cache = "/Users/bgood/data/bioinfo/gene_wiki_as_java/";
	Map<String, List<String>> gene2pubs;
	Map<String, Set<String>> pub2meshes;
	Map<String, Set<String>> gw2meshes;
	Map<String, Set<String>> pub2authors;
	String credfile = "/Users/bgood/workspace/Config/gw_creds.txt";
	public static String gene2gwtitlefile = "/Users/bgood/data/bioinfo/gene_wiki_index.txt";
	Map<String, String> gene2gwtitle;
	Map<String, Double> mesh2pubmedfreq;
	UmlsDb db;
	MeshRDF meshtree;
	AuthorSelector author_selector;
	Map<String, ProteinPage> gene_protein;


	public GeneSetSelector() {
		Map<String, String> creds = GeneWikiUtils.read2columnMap(credfile);
		gene2gwtitle = GeneWikiUtils.getGeneWikiGeneIndex(gene2gwtitlefile, false, creds);//GeneWikiUtils.readGeneWikiGeneIndex(gene2gwtitlefile);		
		gene2pubs = BioInfoUtil.getHumanGene2pub(gene2pubmedfile);	
		pub2meshes = PubMed.getCachedPmidMeshMap(pmid_mesh_cache);
		author_selector = new AuthorSelector();
		pub2authors = author_selector.pmid_authors;
		gw2meshes = getCachedGWMeshMap(gw_mesh_cache);
		mesh2pubmedfreq = new HashMap<String, Double>();
		db = new UmlsDb();
		meshtree = new MeshRDF();
		gene_protein = ProteinPage.loadSerializedDir("/Users/bgood/data/bioinfo/protein_template_pages/", 1000000000);
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
		System.out.println("g2p size "+g.gene2pubs.keySet().size()); //30768

		/////////////////
		//Build author cache
		/////////////////
		//		Map<String, Set<String>> p2g = MapFun.flipMapStringListStrings(g.gene2pubs);
		//		AuthorSelector.buildCacheLinkingPmids2Authors(p2g.keySet(), pmid_author_cache);

		/////////////////
		//Gather data
		////////////////
		String infile = "/Users/bgood/data/journal_collab/gene_data.txt";
		String outfile = "/Users/bgood/data/journal_collab/gene_data_lite.txt";
		String exloutfile = "/Users/bgood/data/journal_collab/gene_data_exl_lite.txt";
		int limit = 10000000;
		int minPubs = 10;
		boolean useannotator = false; //else use metamap
		boolean usecachedgenewiki = true;
		//		g.gatherGeneProfiles(limit, minPubs, useannotator, usecachedgenewiki, infile, outfile);
		//		g.convertToExcellableFile(infile, exloutfile);

		g.gatherLiteGeneProfiles(limit, minPubs, usecachedgenewiki, outfile);


		///////////////////
		//Identify interesting MeSH topics
		//////////////////
		//String meshoutfile = "/Users/bgood/data/journal_collab/mesh_data______.txt";
		//g.buildMeshProfiles(infile, meshoutfile);

		///////////////////
		//build some specific reports
		//////////////////	

		//		String[] terms = {
		//				"Aging", "Feeding Behavior","Learning","Sleep","Pain",
		//				"Motor Neuron Disease","Leukemia, T-Cell","Diabetic Neuropathies", "Influenza, Human", "Wounds and Injuries", "Parasitemia",
		//				"Complement Pathway, Classical",
		//				"Running","Survival","Bicycling","Swimming","Weight Lifting", 
		//				"Violence", "Gambling", "Alcohol Drinking",
		//				"War","Breast Implants"};
		//		Map<String, GeneProfile> gene_profiles = g.readGeneProfiles(infile);

		//		for(String mesh : terms){
		//			System.out.println("working on mesh term "+mesh);
		//			String filename = mesh.replace(",", ":");
		//			filename = filename.replace(" ", "_");
		//			String meshout = "/Users/bgood/data/journal_collab/MeSHgeneSets/"+filename+".txt";
		//			g.makeReportforMeSHterm(gene_profiles, protein_pages, mesh, meshout);
		//		}
		//g.makeReportsForSemanticGroup(infile, "Chemicals & Drugs", "/Users/bgood/data/journal_collab/MeSHgeneSets/Chemicals_Drugs/");

	}

	public void gatherLiteGeneProfiles(int limit, int minPubs,
			boolean usecachedgenewiki, String outfile) {
		Map<String, GeneProfile> pros = new HashMap<String, GeneProfile>();
		File test = new File(outfile);
		try {
			FileWriter f = new FileWriter(outfile);
			f.write(getGeneProfileHeaderLite()+"\n");
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int c = 0;
		//now start gathering more
		for(String gene_id : gene2pubs.keySet()){
			c++;
			if(c>= limit){
				break;
			}
			System.out.println(c+" working on "+gene_id);
			GeneProfile p = pros.get(gene_id);
			//			//check to see if we need to actually go get this one (if not we already had it..)
			if(gene2pubs.get(gene_id).size()>=minPubs&&p==null){ 
				p = profileGeneLite(gene_id, false, true);
				System.out.println(c+" profiling "+gene_id);
				//write the profile out to the new file
				if(p!=null){
					try {
						FileWriter f = new FileWriter(outfile, true);
						f.write(p.getAsStringLite()+"\n");
						System.out.println(p.getAsStringLite());
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

	}

	public void makeReportsForSemanticGroup(String gene_profile_file, String sgroup, String outfolder){
		Map<String, GeneProfile> gene_profiles = readGeneProfiles(gene_profile_file);
		Map<String, ProteinPage> protein_pages = ProteinPage.loadSerializedDir("/Users/bgood/data/bioinfo/protein_template_pages/", 1000000000);
		Set<String> terms = new HashSet<String>();
		//get the sgroup
		List<MeshProfile> mesh_profiles = getMeshTermsInGroup(gene_profile_file, sgroup);
		for(MeshProfile mpro : mesh_profiles){
			String mesh = mpro.term;
			System.out.println("working on mesh term "+mesh);
			String filename = mesh.replace(",", ":");
			filename = filename.replace(" ", "_");
			String meshout = outfolder+filename+".txt";
			File newfile = new File(meshout);
			if(!newfile.exists()){
				try{
					makeReportforMeSHterm(gene_profiles, protein_pages, mesh, meshout);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Using a file containing MeSH gene profile information, retrieve data for genes linked to the requested MeSH term and write the result out to the meshout file
	 * @param genefile
	 * @param term
	 * @param meshout
	 */
	public void makeReportforMeSHterm(Map<String, GeneProfile> gene_profiles, Map<String, ProteinPage> gene_protein, String term, String meshout){

		Map<String, List<GeneProfile>> mesh_profiles = new HashMap<String, List<GeneProfile>>();

		for(Entry<String, GeneProfile> gene_profile : gene_profiles.entrySet()){
			for(String pubmesh : gene_profile.getValue().pub_mesh_freq.keySet()){
				List<GeneProfile> mp = mesh_profiles.get(pubmesh);
				if(mp==null){
					mp = new ArrayList<GeneProfile>();
				}
				GeneProfile gp = gene_profile.getValue();
				mp.add(gp);
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
				if(pubs!=null){
					for(String pub : pubs){
						Set<String> meshes = pub2meshes.get(pub);
						if(meshes!=null){
							if(meshes.contains(term)){
								pmids+=pub+",";
							}
						}
					}
				}
				if(gp.page.getTitle()==null||gp.page.getTitle().equals("null")){
					if(gene_protein.containsKey(gp.page.getNcbi_gene_id())){
						ProteinPage np = gene_protein.get(gp.page.getNcbi_gene_id());
						np.setTitle(np.getTitle()+" (non_gw_article)");
						gp.page = np;
						VolumeReport vr = new VolumeReport();
						vr.extractVolumeFromPopulatedPage(gp.page);
						gp.gw_sentences = (int)vr.getSentences();
						gp.gw_words = (int)vr.getWords();
						gp.gw_headings = (int)vr.getHeadings();
						gp.gw_inline_refs = (int)vr.getPubmed_refs();
					}else{
						String symbol = MyGeneInfo.getSymbolByGeneid(gp.page.getNcbi_gene_id(), true);
						gp.page.setTitle(symbol);
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


	public List<MeshProfile> getMeshTermsInGroup(String infile, String sgroup){
		List<MeshProfile> mpros = new ArrayList<MeshProfile>();
		Map<String, GeneProfile> gene_profiles = readGeneProfiles(infile);
		Map<String, List<GeneProfile>> mesh_profiles = new HashMap<String, List<GeneProfile>>();
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
		int c = 0;
		for(Entry<String, List<GeneProfile>> mesh_profile : mesh_profiles.entrySet()){
			c++;
			Set<String> types = db.getSemanticTypesForMeshAtom(mesh_profile.getKey());
			Set<String> groups = new HashSet<String>();
			if(types!=null&&types.size()>0){
				for(String type : types){
					String group = db.getGroupForStypeName(type);
					if(group!=null){
						groups.add(group);
					}
				}
			}
			if(c%100==0){
				System.out.println("getting mesh profile number "+c+" "+mesh_profile.getKey()+" "+groups+" "+types);
			}
			if(groups.contains(sgroup)){
				MeshProfile mp = new MeshProfile(mesh_profile.getKey(), mesh_profile.getValue(), groups, types);
				mpros.add(mp);
			}
		}
		return mpros;
	}

	/**
	 * Using a file containing MeSH gene profile information, build another file containing data about each MeSH term (how many linked genes, etc.)
	 * 
	 * @param infile
	 * @param outfile
	 */
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
		int c = 0;
		int maxgenes2save = 100;
		for(Entry<String, List<GeneProfile>> mesh_profile : mesh_profiles.entrySet()){
			c++;

			Set<String> types = db.getSemanticTypesForMeshAtom(mesh_profile.getKey());
			Set<String> groups = new HashSet<String>();
			if(types!=null&&types.size()>0){
				for(String type : types){
					String group = db.getGroupForStypeName(type);
					if(group!=null){
						groups.add(group);
					}
				}
			}
			if(c%100==0){
				System.out.println("getting mesh profile number "+c+" "+mesh_profile.getKey()+" "+groups+" "+types);
			}
			MeshProfile mp = new MeshProfile(mesh_profile.getKey(), mesh_profile.getValue(), groups, types);
			try {
				FileWriter f = new FileWriter(outfile, true);
				f.write(mp.getAsString(maxgenes2save)+"\n");
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
		"Semantic Groups\t"+
		"Semantic Types\t"+
		"MeSH root\t"+
		"MeSH parents\t"+
		"Overall_pubmed_frequency\t"+
		"Gene_count\t"+
		"Median_overlap_in_genes\t" +
		"N genes linked to term more than once\t";
		//		"Mean_overlap_in_genes\t" +
		//		"Max_overlap_in_in_genes\t" +
		//		"Min_overlap_in_in_genes\t" +
		//"Genes\t";		
	}

	class MeshProfile{
		String term;
		int n_genes;
		int n_multi_linked_genes;
		Set<String> groups;
		Set<String> types;
		DescriptiveStatistics f_overlaps;
		DescriptiveStatistics intersects;
		Set<String> genes;
		Set<String> parents;
		String meshroot;

		MeshProfile(String t, List<GeneProfile> gene_profiles, Set<String> groups_, Set<String> types_){
			term = t;
			//having an issue with repeated queries eating up all the memory, going to virtual private memory, slowing down, and crashing..
			//adding timer to reload when that starts happening
			long time = System.currentTimeMillis();
			OntClass mc = meshtree.getTermByLabel(t);
			if(mc!=null){
				parents = meshtree.getFamily(mc, true);
				if(parents!=null){
					meshroot = meshtree.getRoot(parents);
				}
			}
			time = (System.currentTimeMillis() - time)/1000;
			if(time>3){
				System.out.println("Reloading mesh ontology");
				meshtree.reload();
			}

			n_genes = gene_profiles.size();
			f_overlaps = new DescriptiveStatistics();
			intersects = new DescriptiveStatistics();
			genes = new HashSet<String>();
			n_multi_linked_genes = 0;
			groups = groups_;
			types = types_;
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

		String getAsString(int maxgenes){
			String genetext = "";

			//			int c = 0;
			//			for(String gene : genes){
			//				c++;
			//				genetext+=gene+",";
			//				if(c>=maxgenes){
			//					break;
			//				}
			//			}

			String g = "";
			if(groups!=null&&groups.size()>0){
				for(String root : groups){
					g+=root+",";
				}
				g = g.substring(0,g.length()-1);
			}
			String t = "";
			if(types!=null&&types.size()>0){
				for(String root : types){
					t+=root+",";
				}
				t = t.substring(0,t.length()-1);
			}
			String meshparents = "";
			if(parents!=null){
				for(String p : parents){
					meshparents += p+" ; ";
				}
			}

			return term+"\t"+
			g+"\t"+
			t+"\t"+
			meshroot+"\t"+
			meshparents+"\t"+
			mesh2pubmedfreq.get(term)+"\t"+
			n_genes+"\t"+
			f_overlaps.getPercentile(50)+"\t"+
			n_multi_linked_genes+"\t";
			//	f_overlaps.getMean()+"\t"+
			//	f_overlaps.getMax()+"\t"+
			//	f_overlaps.getMin()+"\t"+
			//genetext+"\t";
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

	public void FixMissingWikiData(String existingfile, String outfile){
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
		//fix
		for(GeneProfile p : pros.values()){	
			String id = p.page.getNcbi_gene_id();
			if(id!=null&&id!=""&&id.trim().length()>0){
				GeneWikiPage full = GeneWikiUtils.deserializeGeneWikiPage(gene_wiki_article_cache+id);
				if(full!=null){
					VolumeReport vr = new VolumeReport();
					vr.extractVolumeFromPopulatedPage(full);
					p.gw_sentences = (int)vr.getSentences();
					p.gw_words = (int)vr.getWords();
					p.gw_headings = (int)vr.getHeadings();
					p.gw_inline_refs = (int)vr.getPubmed_refs();
				}
			}
		}

		for(GeneProfile p : pros.values()){	
			//write the profile out to the new file
			try {
				FileWriter f = new FileWriter(outfile, true);
				f.write(p.getAsString(true)+"\n");
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
	public void gatherGeneProfiles(int limit, int minPubs, boolean useannotator, boolean usecache, String existingfile, String outfile){
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
		//first transfer the ones we have to the new files
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
			if(c>= limit){
				break;
			}
			System.out.println(c+" working on "+gene_id);
			GeneProfile p = pros.get(gene_id);
			//			//check to see if we need to actually go get this one (if not we already had it..)
			if(gene2pubs.get(gene_id).size()>=minPubs&&p==null){ 
				p = profileGene(gene_id, useannotator, usecache);
				System.out.println(c+" profiling "+gene_id);
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
					String words = row[3];
					String sentences = row[4];
					String headings = row[5];
					String inline_refs = row[6];
					String pmids = row[7];
					String[] pm_meshes = row[12].split("::");
					String[] gw_meshes = row[13].split("::");
					Map<String, Double> author_counts = new HashMap<String, Double>();
					if(row.length>19){
						String[] authortext = row[19].split(",");					
						if(authortext!=null&&authortext.length>0){
							for(String author : authortext){
								String[] a = author.split("::");
								author_counts.put(a[0], Double.parseDouble(a[1]));
							}
						}
					}
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
					if(words!=null&&!words.equals("null")){
						pro.gw_words = Integer.parseInt(words);
					}
					if(headings!=null&&!headings.equals("null")){
						pro.gw_headings = Integer.parseInt(headings);
					}
					if(inline_refs!=null&&!inline_refs.equals("null")){
						pro.gw_inline_refs = Integer.parseInt(inline_refs);
					}
					pro.author_counts = author_counts;
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
		"GW_words\t"+
		"GW_sentences\t"+
		"GW_headings\t"+
		"GW_inline_refs\t"+
		"All_gene2pubmed_refs\t"+
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
		"weighted-pub_mesh\t" +
		"Top_authors\t";
	}

	private String getGeneProfileHeaderLite(){
		return 
		"Gene_id\t"+
		"GW_title\t"+
		"GW_bytes\t"+
		"GW_words\t"+
		"All_gene2pubmed_refs\t"+
		"Gene_wiki_priority\t"+
		"Most_recent_pub\t"+
		"Most_frequent_author\t"+
		"Top_authors_by_frequency\t"+
		"Top_authors_by_date\t";
	}

	class GeneProfile{
		SetComparison sc;
		GeneWikiPage page;
		int gw_words;
		int gw_sentences;
		int gw_headings;
		int gw_inline_refs;
		int pubmed_refs;
		Map<String, Double> pub_mesh_freq;
		Set<String> gw_mesh;
		Map<String, Double> author_counts;
		Map<String, Integer> author_lpdate;

		private GeneProfile(GeneWikiPage page_){
			page = page_;
			if(page==null){
				page = new GeneWikiPage();
			}else{
				VolumeReport vr = new VolumeReport();
				vr.extractVolumeFromPopulatedPage(page);
				gw_sentences = (int)vr.getSentences();
				gw_words = (int)vr.getWords();
				gw_headings = (int)vr.getHeadings();
				gw_inline_refs = (int)vr.getPubmed_refs();
			}
		}

		private GeneProfile(SetComparison sc_, GeneWikiPage page_, Map<String, Double> pub_mesh_, Set<String> gw_mesh_){
			sc = sc_;
			page = page_;
			if(page==null){
				page = new GeneWikiPage();
			}else{
				VolumeReport vr = new VolumeReport();
				vr.extractVolumeFromPopulatedPage(page);
				gw_sentences = (int)vr.getSentences();
				gw_words = (int)vr.getWords();
				gw_headings = (int)vr.getHeadings();
				gw_inline_refs = (int)vr.getPubmed_refs();
			}
			pub_mesh_freq = pub_mesh_;
			gw_mesh = gw_mesh_;
		}

		private void setAuthorList(){
			//authors = AuthorSelector.getRankedAuthorListForPmids(gene2pubs.get(page.getNcbi_gene_id()));
			if(page!=null&&page.getNcbi_gene_id()!=null){
				List<String> pubs = gene2pubs.get(page.getNcbi_gene_id());
				if(pubs!=null){
					author_counts = author_selector.getRankedAuthorListForPmids(pubs);
					author_lpdate = author_selector.getRecentAuthorListForPmids(pubs);
				}
			}
		}


		private String getAsStringLite(){
			if(page.getTitle()==null||page.getTitle().equals("null")){
				if(gene_protein.containsKey(page.getNcbi_gene_id())){
					ProteinPage np = gene_protein.get(page.getNcbi_gene_id());
					np.setTitle(np.getTitle()+" (non_gw_article)");
					page = np;
					VolumeReport vr = new VolumeReport();
					vr.extractVolumeFromPopulatedPage(page);
					gw_sentences = (int)vr.getSentences();
					gw_words = (int)vr.getWords();
					gw_headings = (int)vr.getHeadings();
					gw_inline_refs = (int)vr.getPubmed_refs();
				}else{
					String symbol;
					try {
						symbol = MyGeneInfo.getSymbolByGeneid(page.getNcbi_gene_id(), true);
						page.setTitle(symbol);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}		

			int max_authors = 10;
			setAuthorList();
			//	String top_authors = AuthorSelector.getTopForExcelCell(author_counts, max_authors);
			String recent_authors = AuthorSelector.getRecentForExcelCell(author_lpdate, author_counts,max_authors+10);
			String top_recent_authors = AuthorSelector.getTopRecentForExcelCell(author_counts, author_lpdate, max_authors);

			int pubs = 0;
			if(gene2pubs.get(page.getNcbi_gene_id())!=null){
				pubs = gene2pubs.get(page.getNcbi_gene_id()).size();
			}

			float priority = (float)pubs/(1+gw_words);

			int mr = 0; double mf = 0;
			if(author_lpdate!=null&&author_lpdate.size()>0){
				mr = author_lpdate.values().iterator().next();
			}
			if(author_counts!=null&&author_counts.size()>0){
				mf = author_counts.values().iterator().next();
			}

			return 
			page.getNcbi_gene_id()+"\t"+
			page.getTitle()+"\t"+
			page.getSize()+"\t"+
			gw_words+"\t"+
			pubs+"\t"+
			priority+"\t"+
			mr+"\t"+
			(int)mf+"\t"+
			top_recent_authors+"\t"+
			recent_authors+"\t";

		}

		private String getAsString(boolean keepalltext){
			if(page.getTitle()==null||page.getTitle().equals("null")){
				if(gene_protein.containsKey(page.getNcbi_gene_id())){
					ProteinPage np = gene_protein.get(page.getNcbi_gene_id());
					np.setTitle(np.getTitle()+" (non_gw_article)");
					page = np;
					VolumeReport vr = new VolumeReport();
					vr.extractVolumeFromPopulatedPage(page);
					gw_sentences = (int)vr.getSentences();
					gw_words = (int)vr.getWords();
					gw_headings = (int)vr.getHeadings();
					gw_inline_refs = (int)vr.getPubmed_refs();
				}else{
					String symbol;
					try {
						symbol = MyGeneInfo.getSymbolByGeneid(page.getNcbi_gene_id(), true);
						page.setTitle(symbol);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}		

			int max_authors = 10;
			setAuthorList();
			//	String top_authors = AuthorSelector.getTopForExcelCell(author_counts, max_authors);
			//	String recent_authors = AuthorSelector.getRecentForExcelCell(author_lpdate, max_authors);
			String top_recent_authors = AuthorSelector.getTopRecentForExcelCell(author_counts, author_lpdate, max_authors);
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

			int pubs = 0;
			if(gene2pubs.get(page.getNcbi_gene_id())!=null){
				pubs = gene2pubs.get(page.getNcbi_gene_id()).size();
			}


			return 
			page.getNcbi_gene_id()+"\t"+
			page.getTitle()+"\t"+
			page.getSize()+"\t"+
			gw_words+"\t"+
			gw_sentences+"\t"+
			gw_headings+"\t"+
			gw_inline_refs+"\t"+
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
			weighted_pub_mesh_terms+"\t"+
			top_recent_authors+"\t";
			//	top_authors+"\t"+
			//	recent_authors+"\t";

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

	public GeneProfile profileGeneLite(String gene_id, boolean useannotator, boolean usecache){
		Map<String, Double> pm_sig = getPubmedMeshSignatureForGene(gene_id);
		//retrieve text from wikipedia (or from local cache)
		Set<String> gw_sig = null;
		GeneWikiPage page = new GeneWikiPage();
		page.setNcbi_gene_id(gene_id);
		String title = gene2gwtitle.get(gene_id);
		if(title!=null){
			if(usecache){
				page = GeneWikiUtils.deserializeGeneWikiPage(gene_wiki_article_cache+gene_id);
				if(page==null){
					System.out.println("Gene Wiki page missing for "+title);
				}
			}else{
				page.setTitle(title);
				page.controlledPopulate(true, false, true, false, false, true, true, false);	
			}
		}

		return new GeneProfile(page);
	}

	public GeneProfile profileGene(String gene_id, boolean useannotator, boolean usecache){
		Map<String, Double> pm_sig = getPubmedMeshSignatureForGene(gene_id);
		//retrieve text from wikipedia (or from local cache)
		Set<String> gw_sig = null;
		GeneWikiPage page = new GeneWikiPage();
		page.setNcbi_gene_id(gene_id);
		String title = gene2gwtitle.get(gene_id);
		if(title!=null){
			if(usecache){
				page = GeneWikiUtils.deserializeGeneWikiPage(gene_wiki_article_cache+gene_id);
				if(page==null){
					System.out.println("Gene Wiki page missing for "+title);
				}
			}else{
				page.setTitle(title);
				page.controlledPopulate(true, false, true, false, false, true, true, false);	
			}
			if(page!=null){
				gw_sig = getGeneWikiMeshSignatureForGene(page, 10000, useannotator);
			}
		}
		SetComparison sc = new SetComparison(pm_sig.keySet(), gw_sig);

		return new GeneProfile(sc, page, pm_sig, gw_sig);
	}

	public static Map<String, Set<String>> getCachedGWMeshMap(String gwmeshcache){
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
