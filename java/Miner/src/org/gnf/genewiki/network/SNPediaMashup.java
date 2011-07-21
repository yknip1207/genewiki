/**
 * 
 */
package org.gnf.genewiki.network;

import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.gnf.dont.DOmapping;
import org.gnf.dont.DOowl;
import org.gnf.dont.DOterm;
import org.gnf.genewiki.GeneWikiLink;
import org.gnf.genewiki.GeneWikiPage;
import org.gnf.genewiki.GeneWikiUtils;
import org.gnf.genewiki.Sentence;
import org.gnf.genewiki.WikiCategoryReader;
import org.gnf.genewiki.associations.CandidateAnnotation;
import org.gnf.genewiki.mapping.GeneWikiPageMapper;
import org.gnf.genewiki.metrics.RevisionCounter;
import org.gnf.ncbo.web.AnnotatorClient;
import org.gnf.ncbo.web.NcboAnnotation;
import org.gnf.util.FileFun;
import org.gnf.util.MapFun;
import org.gnf.util.TextFun;
import org.gnf.wikiapi.Category;
import org.gnf.wikiapi.Page;
import org.gnf.wikiapi.User;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author bgood
 *
 */
public class SNPediaMashup {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//get data for ismb mashup paper
		//getSNPDiseaseLinksFromSNPedia();
		//loadSNPedia2disease();
		//String rdf = "file:///Users/bgood/data/SMW/data2share/gw_snp_mashup3.rdf";
		//String outputdir = "/Users/bgood/data/SMW/dump3/";
		//summarizeGeneDiseaseIntersection(rdf, outputdir);
		//summarizeGeneDiseaseJustSnpedia(rdf, outputdir);
		//summarizeGeneDiseaseJustGwiki(rdf, outputdir);
		//summarizeGeneDiseaseUnion(rdf, outputdir);
		//measureOnSamples(rdf, outputdir);

		//get data in a simple way..
		//String outfile = "/Users/bgood/data/SMW/snpedia_disease_snp_take2.txt";
		//getSNPDiseaseLinksFromSNPediaViaSNPText(outfile);
//		String rdf = "file:///Users/bgood/data/SMW/data2share/gw_snp_mashup3.rdf";
//		String outputdir = "/Users/bgood/data/SMW/take2/";
//		summarizeGeneDiseaseNewSNPtagging(rdf, outputdir);
		//		
		//		String snpdisease = "/Users/bgood/data/SMW/snpedia_disease_snp_take2.txt";
		//		Map<String, Set<String>> snp_doids = FileFun.loadMapFromTabD(snpdisease, 1, 5, true);
		//		System.out.println(snp_doids.keySet().size()+" snps mapped 1");
		//		Map<String, Set<String>> snp_diseases = FileFun.loadMapFromTabD(snpdisease, 1, 5, true);
		//		System.out.println(snp_diseases.keySet().size()+" snps mapped 2");
		//		int none = 0;
		//		int some = 0;
		//		for(Entry<String, Set<String>> snp_disease : snp_diseases.entrySet()){
		//			if(snp_disease.getValue().size()==1&&snp_disease.getValue().contains("none")){
		//				none++;
		//			}else{
		//				some++;
		//			}
		//		}
		//		System.out.println(none+" "+some);
		
		//makeniceuris();
	}


//	public static void makeniceuris(){
//		String rdf = "file:///Users/bgood/data/SMW/data2share/gw_snp_mashup3.rdf";
//		String outputfile = "/Users/bgood/data/SMW/data2share/gw_snp_mashup3_niceuris.rdf";
//		OntModel model = ModelFactory.createOntologyModel();
//		OntModel model2 = ModelFactory.createOntologyModel();
//		//184.72.42.242 -> www.snpedia.com
//		//dbpedia.org/resource/
////		model.getResource("http://www.snpedia.com/mediawiki/index.php/Special:URIResolver/Property-3AIn_gene");
//		model.read(rdf);
//		Property hasSnp = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3AHasSNP");
//		Property associated_with = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3AIs-2Dassociated-2Dwith");
//		Property sameas = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3ASame-2Das");
//		ExtendedIterator i = model.listAllOntProperties();
//		while(i.hasNext()){
//			OntProperty p = (OntProperty)i.next();
//			System.out.println(p.getURI());
//			StmtIterator stmt = model.listStatements(null, p, (RDFNode)null);
//			while(stmt.hasNext()){
//				Statement st = stmt.next();
//				Resource sub1 = st.getSubject();
//				RDFNode ob1 = st.getObject();
//				if(p==associated_with||p==hasSnp){
//					//
//					model2.add(sub, RDFS.seeAlso, ob);
//				}
//			}
//		}
//	}
	
	public static void summarizeGeneDiseaseNewSNPtagging(String rdffile, String outputdir){
		//get snp-disease mappings from simple method
		String snpdisease = "/Users/bgood/data/SMW/snpedia_disease_snp_take2.txt";
		Map<String, Set<String>> snp_doids = FileFun.loadMapFromTabD(snpdisease, 1, 5, true);

		//get gene-disease from gw for compare
		Map<String, Set<String>> gw_diseases = FileFun.loadMapFromTabD("/Users/bgood/data/SMW/dump3/gene2disease_onlygw.txt", 0, 3, true);
		Map<String, Set<String>> disease_genes_gw = MapFun.flipMapStringSetStrings(gw_diseases);
		
		//get gene-disease from gw for compare
		Map<String, Set<String>> snplink_diseases = FileFun.loadMapFromTabD("/Users/bgood/data/SMW/dump3/gene2diseasesnponly.txt", 0, 3, true);
		Map<String, Set<String>> disease_genes_snplink = MapFun.flipMapStringSetStrings(snplink_diseases);
		
		//get snps linked to diseases for comparison
//4
		Set<String> snpsfromsnplink = FileFun.readOneColFromFile("/Users/bgood/data/SMW/dump3/gene2diseasesnponly.txt", 4, 10000000);
		
		String credfile = "/Users/bgood/workspace/Config/gw_creds.txt";
		Map<String, String> creds = GeneWikiUtils.read2columnMap(credfile);
		//	RevisionCounter rc = new RevisionCounter(creds.get("wpid"), creds.get("wppw"), snpedia);
		GeneWikiPage gwiki = new GeneWikiPage(creds.get("wpid"), creds.get("wppw"));

		String datafile = rdffile;
		//load the disease ontology mappings for comparison
		Map<String, Set<DOterm>> gene_dos = DOmapping.loadGeneRifs2DO();
		DOowl dowl = new DOowl();
		gene_dos = dowl.expandDoMap(gene_dos, false);

		//load the gene wiki index to get the mappings to ncbi gene ids
		String gindex = "./gw_data/gene_wiki_index_MANUAL.txt";
		//	Map<String, String> gene_page = new HashMap<String, String>();
		Map<String, String> page_gene = new HashMap<String, String>();
		File in = new File(gindex);
		if(in.canRead()){
			try {
				BufferedReader f = new BufferedReader(new FileReader(gindex));
				String line = f.readLine().trim();
				while(line!=null){
					if(!line.startsWith("#")){
						String[] item = line.split("\t");
						if(item!=null&&item.length>1){
							//		if(gene_page.get(item[0])!=null){
							//			System.out.println(item[0]+" duplicated "+item[1]+" -- "+gene_page.get(item[1]));
							//		}
							//		gene_page.put(item[0], item[1].replaceAll(" ", "_"));
							page_gene.put(item[1].replaceAll(" ", "_"), item[0]);
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
		}

		Model model = ModelFactory.createDefaultModel();
		model.read(datafile);
		//		Property snp = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3AHasSNP");
		Property associated_with = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3AIs-2Dassociated-2Dwith");
		//		Property sameas = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3ASame-2Das");


		////////////////////		
		////  Get all gene-SNPs
		///////////////////
		String queryString = 
			"PREFIX wiki: <http://184.72.42.242/mediawiki/index.php/Special:URIResolver/>" +
			"SELECT ?gene ?disease ?do_term ?snp ?snpdisease "+ 
			"WHERE { "+
			" ?gene wiki:Property-3AHasSNP ?snp .  " +
			"} ";

		//////////////////////
		////  Get all gene-disease, gene-snp relations
		/////////////////////

		Query query = QueryFactory.create(queryString);
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model);

		try{
			ResultSet rs = qe.execSelect();
			try {
				FileWriter tab = new FileWriter(outputdir+"gene2snp2disease.txt");
				tab.write("Gene\tTitle\tDisease\tDiseaseOntologyTerm\tSNP\tMatch2DOA\tmethods\n");
				FileWriter tab2 = new FileWriter(outputdir+"gene2disease.txt");
				tab2.write("Gene\tTitle\tDisease\tDiseaseOntologyTerm\tMatch2DOA\tmethods\n");
				int parents = 0; int directs = 0; int total = 0;
				Set<String> gene_disease = new HashSet<String>();
				Set<String> gene_disease_snp = new HashSet<String>();
				Set<String> genes = new HashSet<String>();
				Set<String> diseases = new HashSet<String>();
				Set<String> snps = new HashSet<String>();
				Set<String> gene_disease_gw = new HashSet<String>();
				int matches_snplink = 0;
				int matches_gw = 0;
				int matches_none = 0;
				int matches_gd_all = 0;
				int matches_snplink_gd = 0;
				int matches_gw_gd = 0;
				int matches_none_gd = 0;
				int matches_snplink_gene = 0;
				int matches_gw_gene = 0;
				int matches_none_gene = 0;
				int matches_snplink_disease = 0;
				int matches_gw_disease = 0;
				int matches_none_disease = 0;
				int matches_snplink_snp = 0;
				int matches_none_snp = 0;
				
				while(rs.hasNext()){
					QuerySolution rb = rs.nextSolution() ;
					Resource gene = rb.getResource("gene");
					Resource snp = rb.getResource("snp");				
					String snpid = snp.getLocalName();

					Set<String> snpdiseases = snp_doids.get(snpid);

					for(String disease : snpdiseases){
						String method = "snpText2DO";
						if(disease.equals("none")){
							continue;
						}
						disease = disease.substring(disease.lastIndexOf("/")+1);
						String dlabel = "";
						List<String> dlabels = dowl.getDoTermLabel("http://purl.org/obo/owl/DOID#"+disease.replace(":", "_"));
						if(dlabels!=null&&dlabels.size()>0){
							dlabel = dlabels.get(0);
						}
						
						
						String g = smwuriToText(gene);
						String gene_id = page_gene.get(g);
						
						StmtIterator it = gene.listProperties(associated_with);
						while(it.hasNext()){
							Resource dd = it.nextStatement().getObject().as(Resource.class);
							if(dd.getLocalName().equals(disease)){
								gene_disease_gw.add(gene_id+disease);
								break;
							}
						}


						String doa_match = "none";
						Set<DOterm> dos = null;

						if(gene_id==null){
							gwiki.setTitle(g);
							gwiki.setTitleToRedirect();
							gwiki.retrieveWikiTextContent(false);
							gwiki.parseAndSetNcbiGeneId();
							gene_id = gwiki.getNcbi_gene_id();
							if(gene_id!=null&&gene_id.trim()!=""){
								page_gene.put(g, gene_id);
								FileWriter f = new FileWriter(gindex, true);
								f.write(gene_id+"\t"+g+"\n");
								f.close();
							}else{
								gene_id = null;
							}
						}

						if(gene_id!=null){
							if(genes.add(gene_id)){
								boolean nt = true;
								if(gw_diseases.get(gene_id)!=null){
									matches_gw_gene++;
									nt = false;
								}
								if(snplink_diseases.get(gene_id)!=null){
									matches_snplink_gene++;
									nt= false;
								}
								if(nt){
									matches_none_gene++;
								}
							}
							if(diseases.add(disease)){
								boolean nt = true;
								if(disease_genes_gw.get(disease)!=null){
									matches_gw_disease++;
									nt = false;
								}
								if(disease_genes_snplink.get(disease)!=null){
									matches_snplink_disease++;
									nt= false;
								}
								if(nt){
									matches_none_disease++;
								}
							}
							if(snps.add(snp.getLocalName())){
								boolean nt = true;
								if(snpsfromsnplink.contains(snp.getLocalName())){
									matches_snplink_snp++;
									nt = false;
								}
								if(nt){
									matches_none_snp++;
								}
							}

							if(gene_disease_snp.add(gene_id+" "+disease+" "+snp)){
								if(gw_diseases.get(gene_id)!=null&&gw_diseases.get(gene_id).contains(disease)){
									method+=" : gwtext";
									matches_gw++;
								}
								if(snplink_diseases.get(gene_id)!=null&&snplink_diseases.get(gene_id).contains(disease)){
									method+=" : snpLinks2DO";
									matches_snplink++;
								}
								if(method.equals("snpText2DO")){
									matches_none++;
								}else if(method.equals("snpText2DO : gwtext : snpLinks2DO")){
									matches_gd_all++;
								}
								
								tab.write(gene_id+"\t"+g+"\t"+dlabel+"\t"+disease+"\t"+smwuriToText(snp)+"\t"+doa_match+"\t"+method+"\n");
							}
							if(gene_disease.add(gene_id+"_"+disease)){
								total++;
								dos = gene_dos.get(gene_id);
								if(dos!=null){
									for(DOterm dot : dos){
										if(dot.getAccession().equals(disease)){
											if(dot.isInferred_parent()){
												doa_match = "parent";
												parents++;
											}else{
												doa_match = "direct";
												directs++;
											}
											break;
										}
									}
								}
								boolean nt = true;
								if(gw_diseases.get(gene_id)!=null&&gw_diseases.get(gene_id).contains(disease)){
									matches_gw_gd++;
									nt = false;
								}
								if(snplink_diseases.get(gene_id)!=null&&snplink_diseases.get(gene_id).contains(disease)){
									matches_snplink_gd++;
									nt= false;
								}
								if(nt){
									matches_none_gd++;
								}
								tab2.write(gene_id+"\t"+g+"\t"+dlabel+"\t"+disease+"\t"+doa_match+"\t"+method+"\n");
							}else{
								System.out.println(gene_id+"\t"+disease);
							}
						}else{
							System.out.println("No gene mapped to "+g);
						}
					}
				}
				System.out.println("matches_snp_link_gene\tmatches_gw_gene\tnone\t");
				System.out.println(matches_snplink_gene+"\t"+matches_gw_gene+"\t"+matches_none_gene+"\t");
				
				System.out.println("matches_snp_link_disease\tmatches_gw_disease\tnone\t");
				System.out.println(matches_snplink_disease+"\t"+matches_gw_disease+"\t"+matches_none_disease+"\t");
				
				System.out.println("matches_snp_link_snp\tnone\t");
				System.out.println(matches_snplink_snp+"\t"+matches_none_snp+"\t");
				
				System.out.println("matches_snp_link_gd\tmatches_gw_gd\tnone\t");
				System.out.println(matches_snplink_gd+"\t"+matches_gw_gd+"\t"+matches_none_gd+"\t");
				System.out.println("matches_snp_link\tmatches_gw\tnone\tall\t");
				System.out.println(matches_snplink+"\t"+matches_gw+"\t"+matches_none+"\t"+matches_gd_all);
				System.out.println("parents\tdirects\tnone\ttotal");
				System.out.println(parents+"\t"+directs+"\t"+(total-parents-directs)+"\t"+total);
				System.out.println("Genes\tDiseases\tSNPS\tGene_Disease_Pairs\tGene_Disease_Snps\tGene_Disease_also_inGW");
				System.out.println(genes.size()+"\t"+diseases.size()+"\t"+snps.size()+"\t"+gene_disease.size()+"\t"+gene_disease_snp.size()+"\t"+gene_disease_gw.size());
				tab.close();
				tab2.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}finally{
			// Important � free up resources used running the query
			qe.close();
		}



	}

	public static void getSNPDiseaseLinksFromSNPediaViaSNPText(String outfile){
		String snpedia = "http://www.snpedia.com/api.php";
		WikiCategoryReader cats = new WikiCategoryReader();
		cats.init("i9606", "2manypasswords", snpedia);
		List<Page> snps = cats.listPagesByCategory(100000, 500, "Is_a_snp");
		HashMap<String, HashSet<String>> disease_snps = new HashMap<String, HashSet<String>>();
		System.out.println("Got "+snps.size()+" snps");
		Set<String> donesnps = new HashSet<String>();
		try {
			FileWriter f = null;
			File test = new File(outfile);
			if(!test.exists()){
				f = new FileWriter(outfile);		
				f.write("#Entrez_gene\tArticle_title\tSection_header\tMost_recent_editor\tScore_from_Annotator\tNCBO_ont_id/Term_id\tTerm_name\tSurrounding_text\tInline-references");
				f.close();
			}else{
				donesnps = FileFun.readOneColFromFile(outfile, 1, 1000000);
				System.out.println("done with "+donesnps.size());
			}
			int i = 0;
			for(Page page : snps){
				//		Page page = new Page();
				//		page.setTitle("Rs10045431");
				i++;
				System.out.println("Parsing: "+i+"\t"+page.getTitle());
				String d_title = page.getTitle();
				if(d_title!=null){
					if(donesnps.contains(d_title)){
						System.out.println("Have: "+i+"\t"+page.getTitle());
					}else{
						System.out.println("Don't have "+i+"\t"+page.getTitle());
						GeneWikiPage d_page = new GeneWikiPage(cats.getUser());
						d_page.setTitle(d_title);
						boolean worked = d_page.retrieveWikiTextContent(false);
						List<Sentence> sentences = new ArrayList<Sentence>();
						String ptext = TextFun.removeNonalphanumeric(d_page.getPageContent());
						String[] words = ptext.split(" ");
						String block = "";
						for(int w=0; w< words.length; w++){
							if(w%250==0&&w>0){
								Sentence s = new Sentence();
								s.setText(block);
								sentences.add(s);
								block = "";
							}else{
								block += words[w]+" ";
							}
						}
						Sentence s = new Sentence();
						s.setText(block);
						sentences.add(s);

						d_page.setHeadings();
						d_page.setSentences(sentences);
						System.out.println("#Sentences:\t"+d_page.getSentences().size());
						System.out.println("#Bytes:\t"+d_page.getSize());
						boolean allowSynonyms = true; boolean useGO = false;  boolean useDO = true; boolean useFMA = false; boolean usePRO = false;
						f = new FileWriter(outfile, true);		
						if(worked){
							System.out.println("#Processing text with NCBO Annotator...\n");
							List<CandidateAnnotation> annos = GeneWikiPageMapper.annotateArticleNCBO(d_page, allowSynonyms, useGO, useDO, useFMA, usePRO);
							if(annos!=null&&annos.size()>0){
								System.out.println("#Entrez_gene\tArticle_title\tSection_header\tMost_recent_editor\tScore_from_Annotator\tNCBO_ont_id/Term_id\tTerm_name\tSurrounding_text\tInline-references");
								for(CandidateAnnotation anno : annos){
									System.out.println(anno.getEntrez_gene_id()+"\t"+anno.getSource_wiki_page_title()+"\t"+anno.getSection_heading()+"\t"+anno.getLink_author()+"\t"+anno.getAnnotationScore()+"\t"+anno.getTarget_accession()+"\t"+anno.getTarget_preferred_term()+"\t"+anno.getParagraph_around_link()+"\t"+anno.getPubmed_references());
									f.write(anno.getEntrez_gene_id()+"\t"+anno.getSource_wiki_page_title()+"\t"+anno.getSection_heading()+"\t"+anno.getLink_author()+"\t"+anno.getAnnotationScore()+"\t"+anno.getTarget_accession()+"\t"+anno.getTarget_preferred_term()+"\t"+anno.getParagraph_around_link()+"\t"+anno.getPubmed_references()+"\n");
								}
							}else{
								System.out.println("#No GO or DO annotations found for "+d_title);
								f.write("none\t"+d_title+"\tnone\tnone\tnone\tnone\tnone\tnone\tnone\n");
							}
						}else{
							System.out.println("#An error occurred when gathering data from SNPedia for "+d_title);
						}
						f.close();
					}
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void loadSNPedia2disease(){
		HashMap<String, HashSet<String>> do_snps = new HashMap<String, HashSet<String>>();
		String infile = "/Users/bgood/data/SMW/snpedia_disease_snp.txt";
		try {
			BufferedReader f = new BufferedReader(new FileReader(infile));
			String line = f.readLine(); line = f.readLine();
			while(line!=null){
				String[] r = line.split("\t");
				String[] onts = r[1].split("\\|");
				for(String ont : onts){
					//disease ontology
					if(ont.startsWith("44764/")){
						String term = ont.substring(6);
						String[] snps = r[2].split("\\|");
						HashSet<String> snplist = new HashSet<String>();
						for(String snp : snps){
							snplist.add(snp);
						}
						do_snps.put(term+"\t"+r[0], snplist);
						break;
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
		System.out.println(do_snps);
	}

	public static void getSNPDiseaseLinksFromSNPedia(){
		String snpedia = "http://www.snpedia.com/api.php";
		WikiCategoryReader cats = new WikiCategoryReader(snpedia);
		List<Page> snps = cats.listPagesByCategory(1000, 500, "Is_a_medical_condition");
		HashMap<String, HashSet<String>> disease_snps = new HashMap<String, HashSet<String>>();
		String outfile = "/Users/bgood/data/SMW/snpedia_disease_snp__.txt";
		System.out.println("Got "+snps.size()+" medical conditions");
		int i = 0;
		for(Page page : snps){
			i++;
			System.out.println("Parsing: "+i+"\t"+page.getTitle());
			String d_title = page.getTitle();
			if(d_title!=null){
				GeneWikiPage d_page = new GeneWikiPage(cats.getUser());
				d_page.setTitle(d_title);
				d_page.retrieveWikiTextContent(false);
				d_page.controlledPopulate(true, true, false, false, true, true, true, false);
				//links out
				for(GeneWikiLink glink : d_page.getGlinks()){
					GeneWikiPage linked_page = new GeneWikiPage(cats.getUser());
					linked_page.setTitle(glink.getTarget_page());
					linked_page.setCategories();
					if(linked_page.getCategories()!=null){
						for(Category lcat : linked_page.getCategories()){
							if(lcat.getTitle().equals("Category:Is a snp")){
								HashSet<String> snpslist = disease_snps.get(d_title);
								if(snpslist==null){
									snpslist = new HashSet<String>();
								}
								snpslist.add(linked_page.getTitle());
								disease_snps.put(d_title, snpslist);
								break;
							}
						}
					}
				}
				//links in
				d_page.retrieveAllInBoundWikiLinks(true, false);
				for(GeneWikiLink glink : d_page.getInglinks()){
					GeneWikiPage linked_page = new GeneWikiPage(cats.getUser());
					linked_page.setTitle(glink.getTarget_page());
					linked_page.setCategories();
					if(linked_page.getCategories()!=null){
						for(Category lcat : linked_page.getCategories()){
							if(lcat.getTitle().equals("Category:Is a snp")){
								HashSet<String> snpslist = disease_snps.get(d_title);
								if(snpslist==null){
									snpslist = new HashSet<String>();
								}
								snpslist.add(linked_page.getTitle());
								disease_snps.put(d_title, snpslist);								
								break;
							}
						}
					}
				}
			}
		}
		try {
			FileWriter f = new FileWriter(outfile);
			f.write("SNPedia_medical_condition\tdisease_ontology_or_OMIM_mappings\tSNPS");
			for(Entry<String, HashSet<String>> disease_snp : disease_snps.entrySet()){
				//get DO class if possible
				List<NcboAnnotation> doas = AnnotatorClient.ncboAnnotateText(disease_snp.getKey(), true, false, true, false, false, true, false);
				String d = disease_snp.getKey();
				if(doas!=null){
					d+="\t";
					for(NcboAnnotation doa : doas){
						d+="|"+doa.getConcept().getLocalConceptId();
					}
					d+="\t";
				}
				String snpstext = "";
				for(String snp : disease_snp.getValue()){
					snpstext+=snp+"|";
				}
				if(snpstext.endsWith("|")){
					snpstext.substring(0,snpstext.length()-1);
				}
				System.out.println(d+snpstext);
				f.write(d+snpstext+"\n");
			}
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void getEditCountsForSNPedia(){
		String snpedia = "http://www.snpedia.com/api.php";
		WikiCategoryReader cats = new WikiCategoryReader(snpedia);
		List<Page> snps = cats.listPagesByCategory(100000, 500, "Is_a_snp");
		Set<String> titles = new HashSet<String>();
		for(Page page : snps){
			titles.add(page.getTitle());
		}
		String credfile = "/Users/bgood/workspace/Config/gw_creds.txt";
		Map<String, String> creds = GeneWikiUtils.read2columnMap(credfile);
		RevisionCounter rc = new RevisionCounter(creds.get("wpid"), creds.get("wppw"), snpedia);
		Calendar latest = Calendar.getInstance();
		Calendar earliest = Calendar.getInstance();
		earliest.add(Calendar.YEAR, -1);
		String outfile = "/Users/bgood/data/SMW/snpedia_edit_report";
		rc.generateBatchReport(titles, latest, earliest, outfile);
	}

	public static void dumpTabTriples(){
		String datafile = "file:///Users/bgood/data/SMW/gw_snp_mashup.rdf";
		String gindex = "./gw_data/gene_wiki_index.txt";
		Map<String, String> gene_page = new HashMap<String, String>();
		File in = new File(gindex);
		if(in.canRead()){
			try {
				BufferedReader f = new BufferedReader(new FileReader(gindex));
				String line = f.readLine().trim();
				while(line!=null){
					if(!line.startsWith("#")){
						String[] item = line.split("\t");
						if(item!=null&&item.length>1){
							if(gene_page.get(item[0])!=null){
								System.out.println(item[0]+" duplicated "+item[1]+" -- "+gene_page.get(item[1]));
							}
							gene_page.put(item[0], item[1].replaceAll(" ", "_"));
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
		}
		//		Map<String, String> gene_page = GeneWikiUtils.getGeneWikiGeneIndex(gindex, false);
		Map<String, String> page_gene = MapFun.flipMapStringString(gene_page);
		Model model = ModelFactory.createDefaultModel();
		model.read(datafile);
		Property snp = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3AHasSNP");
		Property associated_with = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3AIs-2Dassociated-2Dwith");
		Property sameas = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3ASame-2Das");
		StmtIterator it = model.listStatements();
		String buri = "http://184.72.42.242/mediawiki/index.php/Special:URIResolver/";
		try {
			FileWriter tab = new FileWriter("/Users/bgood/data/SMW/gene_snp_disease.txt");
			while(it.hasNext()){
				Statement s = it.nextStatement();
				if(s.getPredicate().equals(snp)||s.getPredicate().equals(associated_with)){
					Resource o = s.getObject().as(Resource.class);
					String target = o.getLocalName();
					if(target.equals("")){
						target = o.getURI();
						if(target.trim().length()>buri.length()){
							target = target.replace(buri, "");
						}
					}
					String subject = s.getSubject().getLocalName();
					if(subject.equals("")){
						String uri = s.getSubject().getURI();
						if(uri.trim().length()>buri.length()){
							subject = uri.replace(buri, "").replaceAll("-2D", "-");
						}else{
							subject = s.getSubject().getURI();
						}
					}
					String prop = s.getPredicate().getLocalName().replaceAll("Property-3A", "").replaceAll("2D", "");
					tab.write(page_gene.get(subject)+"\t"+subject+"\t"+prop+"\t"+target+"\n");
				}
			}
			tab.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/**
	 * Process the RDF export to produce a summary of the mashup.
	 * Main method for producing the results in ISMB bio-ontology SIG
	 * @param rdffile
	 * @param output
	 */
	public static void summarizeGeneDiseaseJustGwiki(String rdffile, String outputdir){
		String credfile = "/Users/bgood/workspace/Config/gw_creds.txt";
		Map<String, String> creds = GeneWikiUtils.read2columnMap(credfile);
		//	RevisionCounter rc = new RevisionCounter(creds.get("wpid"), creds.get("wppw"), snpedia);
		GeneWikiPage gwiki = new GeneWikiPage(creds.get("wpid"), creds.get("wppw"));

		String datafile = rdffile;
		//load the disease ontology mappings for comparison
		Map<String, Set<DOterm>> gene_dos = DOmapping.loadGeneRifs2DO();
		DOowl dowl = new DOowl();
		gene_dos = dowl.expandDoMap(gene_dos, false);

		//load the gene wiki index to get the mappings to ncbi gene ids
		String gindex = "./gw_data/gene_wiki_index_MANUAL.txt";
		//	Map<String, String> gene_page = new HashMap<String, String>();
		Map<String, String> page_gene = new HashMap<String, String>();
		File in = new File(gindex);
		if(in.canRead()){
			try {
				BufferedReader f = new BufferedReader(new FileReader(gindex));
				String line = f.readLine().trim();
				while(line!=null){
					if(!line.startsWith("#")){
						String[] item = line.split("\t");
						if(item!=null&&item.length>1){
							//		if(gene_page.get(item[0])!=null){
							//			System.out.println(item[0]+" duplicated "+item[1]+" -- "+gene_page.get(item[1]));
							//		}
							//		gene_page.put(item[0], item[1].replaceAll(" ", "_"));
							page_gene.put(item[1].replaceAll(" ", "_"), item[0]);
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
		}

		Model model = ModelFactory.createDefaultModel();
		model.read(datafile);
		//		Property snp = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3AHasSNP");
		Property associated_with = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3AIs-2Dassociated-2Dwith");
		//		Property sameas = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3ASame-2Das");


		////////////////////		
		////  Get gene-disease and gene-snp-disease examples
		///////////////////
		String queryString = 
			"PREFIX wiki: <http://184.72.42.242/mediawiki/index.php/Special:URIResolver/>" +
			"SELECT ?gene ?disease ?do_term ?snp "+ 
			"WHERE { "+
			" ?gene wiki:Property-3AIs-2Dassociated-2Dwith ?disease . " +
			" ?disease wiki:Property-3ASame-2Das ?do_term . " +
			"	FILTER regex(?do_term, \"^DOID\", \"i\") . "+
			" OPTIONAL{" +
			" ?gene wiki:Property-3AHasSNP ?snp .  " +
			" ?snp wiki:Property-3AIs-2Dassociated-2Dwith ?disease . "+
			" } "+
			"} ";

		//////////////////////
		////  Get all gene-disease, gene-snp relations
		/////////////////////

		Query query = QueryFactory.create(queryString);
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model);

		try{
			ResultSet rs = qe.execSelect();
			try {
				FileWriter tab = new FileWriter(outputdir+"gene2disease.txt");
				FileWriter tabgw = new FileWriter(outputdir+"gene2disease_onlygw.txt");
				tab.write("Gene\tTitle\tDisease\tDiseaseOntologyTerm\tSNP\tMatch2DOA\n");
				tabgw.write("Gene\tTitle\tDisease\tDiseaseOntologyTerm\tMatch2DOA\n");
				int parents = 0; int directs = 0; int total = 0;
				Set<String> gene_disease = new HashSet<String>();
				Set<String> gene_disease_snp = new HashSet<String>();
				Set<String> genes = new HashSet<String>();
				Set<String> diseases = new HashSet<String>();
				Set<String> snps = new HashSet<String>();
				Set<String> gene_disease_gw_and_snp = new HashSet<String>();
				while(rs.hasNext()){
					QuerySolution rb = rs.nextSolution() ;
					Resource gene = rb.getResource("gene");
					Resource disease = rb.getResource("disease");
					String do_term = rb.getLiteral("do_term").getString();
					Resource snp = rb.getResource("snp");
					String g = smwuriToText(gene);
					String gene_id = page_gene.get(g);

					if(snp!=null){
						StmtIterator it = snp.listProperties(associated_with);
						while(it.hasNext()){
							Resource dd = it.nextStatement().getObject().as(Resource.class);
							if(dd.equals(disease)){
								gene_disease_gw_and_snp.add(gene_id+do_term);
								break;
							}
						}
					}

					String doa_match = "none";
					Set<DOterm> dos = null;

					if(gene_id==null){
						gwiki.setTitle(g);
						gwiki.setTitleToRedirect();
						gwiki.retrieveWikiTextContent(false);
						gwiki.parseAndSetNcbiGeneId();
						gene_id = gwiki.getNcbi_gene_id();
						if(gene_id!=null&&gene_id.trim()!=""){
							page_gene.put(g, gene_id);
							FileWriter f = new FileWriter(gindex, true);
							f.write(gene_id+"\t"+g+"\n");
							f.close();
						}else{
							gene_id = null;
						}
					}

					if(gene_id!=null){
						genes.add(gene_id);
						diseases.add(do_term);
						if(snp!=null){
							snps.add(snp.getLocalName());
						}
						if(gene_disease.add(gene_id+"_"+do_term)){
							total++;
							dos = gene_dos.get(gene_id);
							if(dos!=null){
								for(DOterm dot : dos){
									if(dot.getAccession().equals(do_term)){
										if(dot.isInferred_parent()){
											doa_match = "parent";
											parents++;
										}else{
											doa_match = "direct";
											directs++;
										}
										break;
									}
								}
							}
							tabgw.write(gene_id+"\t"+g+"\t"+smwuriToText(disease)+"\t"+do_term+"\t"+doa_match+"\n");
						}
						//						else{
						//							System.out.println(gene_id+"\t"+do_term);
						//						}
						if(gene_disease_snp.add(gene_id+" "+do_term+" "+snp)){
							tab.write(gene_id+"\t"+g+"\t"+smwuriToText(disease)+"\t"+do_term+"\t"+smwuriToText(snp)+"\t"+doa_match+"\n");
						}
					}else{
						System.out.println("No gene mapped to "+g);
					}
				}
				System.out.println("parents\tdirects\tnone\ttotal");
				System.out.println(parents+"\t"+directs+"\t"+(total-parents-directs)+"\t"+total);
				System.out.println("Genes\tDiseases\tSNPS\tGene_Disease_Pairs\tGene_Disease_Snps\tGene_Disease_also_inSNP");
				System.out.println(genes.size()+"\t"+diseases.size()+"\t"+snps.size()+"\t"+gene_disease.size()+"\t"+gene_disease_snp.size()+"\t"+gene_disease_gw_and_snp.size());
				tab.close();
				tabgw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}finally{
			// Important � free up resources used running the query
			qe.close();
		}



	}

	/**
	 * Process the RDF export to produce a summary of the mashup.
	 * Main method for producing the results in ISMB bio-ontology SIG
	 * @param rdffile
	 * @param output
	 */
	public static void summarizeGeneDiseaseJustSnpedia(String rdffile, String outputdir){
		String credfile = "/Users/bgood/workspace/Config/gw_creds.txt";
		Map<String, String> creds = GeneWikiUtils.read2columnMap(credfile);
		//	RevisionCounter rc = new RevisionCounter(creds.get("wpid"), creds.get("wppw"), snpedia);
		GeneWikiPage gwiki = new GeneWikiPage(creds.get("wpid"), creds.get("wppw"));

		String datafile = rdffile;
		//load the disease ontology mappings for comparison
		Map<String, Set<DOterm>> gene_dos = DOmapping.loadGeneRifs2DO();
		DOowl dowl = new DOowl();
		gene_dos = dowl.expandDoMap(gene_dos, false);

		//load the gene wiki index to get the mappings to ncbi gene ids
		String gindex = "./gw_data/gene_wiki_index_MANUAL.txt";
		//	Map<String, String> gene_page = new HashMap<String, String>();
		Map<String, String> page_gene = new HashMap<String, String>();
		File in = new File(gindex);
		if(in.canRead()){
			try {
				BufferedReader f = new BufferedReader(new FileReader(gindex));
				String line = f.readLine().trim();
				while(line!=null){
					if(!line.startsWith("#")){
						String[] item = line.split("\t");
						if(item!=null&&item.length>1){
							//		if(gene_page.get(item[0])!=null){
							//			System.out.println(item[0]+" duplicated "+item[1]+" -- "+gene_page.get(item[1]));
							//		}
							//		gene_page.put(item[0], item[1].replaceAll(" ", "_"));
							page_gene.put(item[1].replaceAll(" ", "_"), item[0]);
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
		}

		Model model = ModelFactory.createDefaultModel();
		model.read(datafile);
		//		Property snp = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3AHasSNP");
		Property associated_with = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3AIs-2Dassociated-2Dwith");
		//		Property sameas = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3ASame-2Das");


		////////////////////		
		////  Get gene-disease and gene-snp-disease examples
		///////////////////
		String queryString = 
			"PREFIX wiki: <http://184.72.42.242/mediawiki/index.php/Special:URIResolver/>" +
			"SELECT ?gene ?disease ?do_term ?snp "+ 
			"WHERE { "+
			" ?gene wiki:Property-3AHasSNP ?snp .  " +
			" ?snp wiki:Property-3AIs-2Dassociated-2Dwith ?disease . " +
			" ?disease wiki:Property-3ASame-2Das ?do_term . " +
			"	FILTER regex(?do_term, \"^DOID\", \"i\") . "+
			"} ";

		//////////////////////
		////  Get all gene-disease, gene-snp relations
		/////////////////////

		Query query = QueryFactory.create(queryString);
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model);

		try{
			ResultSet rs = qe.execSelect();
			try {
				FileWriter tab = new FileWriter(outputdir+"gene2snp2disease.txt");
				tab.write("Gene\tTitle\tDisease\tDiseaseOntologyTerm\tSNP\tMatch2DOA\n");
				int parents = 0; int directs = 0; int total = 0;
				Set<String> gene_disease = new HashSet<String>();
				Set<String> gene_disease_snp = new HashSet<String>();
				Set<String> genes = new HashSet<String>();
				Set<String> diseases = new HashSet<String>();
				Set<String> snps = new HashSet<String>();
				Set<String> gene_disease_gw = new HashSet<String>();
				while(rs.hasNext()){
					QuerySolution rb = rs.nextSolution() ;
					Resource gene = rb.getResource("gene");
					Resource disease = rb.getResource("disease");
					String do_term = rb.getLiteral("do_term").getString();
					Resource snp = rb.getResource("snp");
					String g = smwuriToText(gene);
					String gene_id = page_gene.get(g);

					StmtIterator it = gene.listProperties(associated_with);
					while(it.hasNext()){
						Resource dd = it.nextStatement().getObject().as(Resource.class);
						if(dd.equals(disease)){
							gene_disease_gw.add(gene_id+do_term);
							break;
						}
					}


					String doa_match = "none";
					Set<DOterm> dos = null;

					if(gene_id==null){
						gwiki.setTitle(g);
						gwiki.setTitleToRedirect();
						gwiki.retrieveWikiTextContent(false);
						gwiki.parseAndSetNcbiGeneId();
						gene_id = gwiki.getNcbi_gene_id();
						if(gene_id!=null&&gene_id.trim()!=""){
							page_gene.put(g, gene_id);
							FileWriter f = new FileWriter(gindex, true);
							f.write(gene_id+"\t"+g+"\n");
							f.close();
						}else{
							gene_id = null;
						}
					}

					if(gene_id!=null){
						genes.add(gene_id);
						diseases.add(do_term);
						snps.add(snp.getLocalName());

						if(gene_disease.add(gene_id+"_"+do_term)){
							total++;
							dos = gene_dos.get(gene_id);
							if(dos!=null){
								for(DOterm dot : dos){
									if(dot.getAccession().equals(do_term)){
										if(dot.isInferred_parent()){
											doa_match = "parent";
											parents++;
										}else{
											doa_match = "direct";
											directs++;
										}
										break;
									}
								}
							}
						}else{
							System.out.println(gene_id+"\t"+do_term);
						}
						if(gene_disease_snp.add(gene_id+" "+do_term+" "+snp)){
							tab.write(gene_id+"\t"+g+"\t"+smwuriToText(disease)+"\t"+do_term+"\t"+smwuriToText(snp)+"\t"+doa_match+"\n");
						}
					}else{
						System.out.println("No gene mapped to "+g);
					}
				}
				System.out.println("parents\tdirects\tnone\ttotal");
				System.out.println(parents+"\t"+directs+"\t"+(total-parents-directs)+"\t"+total);
				System.out.println("Genes\tDiseases\tSNPS\tGene_Disease_Pairs\tGene_Disease_Snps\tGene_Disease_also_inGW");
				System.out.println(genes.size()+"\t"+diseases.size()+"\t"+snps.size()+"\t"+gene_disease.size()+"\t"+gene_disease_snp.size()+"\t"+gene_disease_gw.size());
				tab.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}finally{
			// Important � free up resources used running the query
			qe.close();
		}



	}

	/**
	 * Process the RDF export to produce a summary of the mashup.
	 * Main method for producing the results in ISMB bio-ontology SIG
	 * @param rdffile
	 * @param output
	 */
	public static void summarizeGeneDiseaseUnion(String rdffile, String outputdir){
		String datafile = rdffile;
		String credfile = "/Users/bgood/workspace/Config/gw_creds.txt";
		Map<String, String> creds = GeneWikiUtils.read2columnMap(credfile);
		//	RevisionCounter rc = new RevisionCounter(creds.get("wpid"), creds.get("wppw"), snpedia);
		GeneWikiPage gwiki = new GeneWikiPage(creds.get("wpid"), creds.get("wppw"));
		//load the disease ontology mappings for comparison
		Map<String, Set<DOterm>> gene_dos = DOmapping.loadGeneRifs2DO();
		DOowl dowl = new DOowl();
		gene_dos = dowl.expandDoMap(gene_dos, false);

		//load the gene wiki index to get the mappings to ncbi gene ids
		String gindex = "./gw_data/gene_wiki_index_MANUAL.txt";
		//	Map<String, String> gene_page = new HashMap<String, String>();
		Map<String, String> page_gene = new HashMap<String, String>();
		File in = new File(gindex);
		if(in.canRead()){
			try {
				BufferedReader f = new BufferedReader(new FileReader(gindex));
				String line = f.readLine().trim();
				while(line!=null){
					if(!line.startsWith("#")){
						String[] item = line.split("\t");
						if(item!=null&&item.length>1){
							//		if(gene_page.get(item[0])!=null){
							//			System.out.println(item[0]+" duplicated "+item[1]+" -- "+gene_page.get(item[1]));
							//		}
							//		gene_page.put(item[0], item[1].replaceAll(" ", "_"));
							page_gene.put(item[1].replaceAll(" ", "_"), item[0]);
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
		}

		Model model = ModelFactory.createDefaultModel();
		model.read(datafile);
		//		Property snp = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3AHasSNP");
		//		Property associated_with = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3AIs-2Dassociated-2Dwith");
		//		Property sameas = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3ASame-2Das");


		////////////////////		
		////  Get gene-disease and gene-snp-disease examples
		///////////////////
		String queryString = 
			"PREFIX wiki: <http://184.72.42.242/mediawiki/index.php/Special:URIResolver/>" +
			"SELECT ?gene ?disease ?do_term ?snp "+ 
			"WHERE { "+
			" { ?gene wiki:Property-3AIs-2Dassociated-2Dwith ?disease . " +
			" ?disease wiki:Property-3ASame-2Das ?do_term . " +
			"	FILTER regex(?do_term, \"^DOID\", \"i\") . " +
			" } " +
			" UNION { "+
			" ?gene wiki:Property-3AHasSNP ?snp .  " +
			" ?snp wiki:Property-3AIs-2Dassociated-2Dwith ?disease . " +
			" ?disease wiki:Property-3ASame-2Das ?do_term . " +
			"	FILTER regex(?do_term, \"^DOID\", \"i\") . " +
			" }" +
			"} ";

		//////////////////////
		////  Get all gene-disease, gene-snp relations
		/////////////////////

		Query query = QueryFactory.create(queryString);
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model);

		try{
			ResultSet rs = qe.execSelect();
			try {
				FileWriter tab = new FileWriter(outputdir+"gene2diseaseORsnp2disease.txt");
				tab.write("Gene\tTitle\tDisease\tDiseaseOntologyTerm\tsnp\tMatch2DOA\n");
				int parents = 0; int directs = 0; int total = 0;
				Set<String> gene_disease_snp = new HashSet<String>();
				Set<String> gene_disease = new HashSet<String>();
				Set<String> genes = new HashSet<String>();
				Set<String> diseases = new HashSet<String>();
				Set<String> snps = new HashSet<String>();
				while(rs.hasNext()){
					QuerySolution rb = rs.nextSolution() ;
					Resource gene = rb.getResource("gene");
					Resource disease = rb.getResource("disease");
					String do_term = rb.getLiteral("do_term").getString();
					Resource snp = rb.getResource("snp");

					String g = smwuriToText(gene);
					String gene_id = page_gene.get(g);
					String doa_match = "none";
					Set<DOterm> dos = null;


					if(gene_id==null){
						gwiki.setTitle(g);
						gwiki.setTitleToRedirect();
						gwiki.retrieveWikiTextContent(false);
						gwiki.parseAndSetNcbiGeneId();
						gene_id = gwiki.getNcbi_gene_id();
						if(gene_id!=null&&gene_id.trim()!=""){
							page_gene.put(g, gene_id);
							FileWriter f = new FileWriter(gindex, true);
							f.write(gene_id+"\t"+g+"\n");
							f.close();
						}else{
							gene_id = null;
						}
					}

					if(gene_id!=null){
						genes.add(gene_id);
						diseases.add(do_term);
						if(snp!=null)snps.add(snp.getLocalName());

						if(gene_disease.add(gene_id+"_"+do_term)){
							total++;
							dos = gene_dos.get(gene_id);
							if(dos!=null){
								for(DOterm dot : dos){
									if(dot.getAccession().equals(do_term)){
										if(dot.isInferred_parent()){
											doa_match = "parent";
											parents++;
										}else{
											doa_match = "direct";
											directs++;
										}
										break;
									}
								}
							}
							//one allowed snp per gene-disease pair just to mark where it came from
							tab.write(gene_id+"\t"+g+"\t"+smwuriToText(disease)+"\t"+do_term+"\t"+smwuriToText(snp)+"\t"+doa_match+"\n");
						}else{
							System.out.println(gene_id+"\t"+do_term);
						}
					}else{
						System.out.println("No gene mapped to "+g);
					}
				}
				System.out.println("parents\tdirects\tnone\ttotal");
				System.out.println(parents+"\t"+directs+"\t"+(total-parents-directs)+"\t"+total);
				System.out.println("Genes\tDiseases\tSNPS\tGene_Disease_Pairs\tGene_Disease_Snps");
				System.out.println(genes.size()+"\t"+diseases.size()+"\t"+snps.size()+"\t"+gene_disease.size()+"\t"+gene_disease_snp.size());
				tab.close();



			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}finally{
			// Important � free up resources used running the query
			qe.close();
		}
	}

	public static class LinkedDisease{
		String disease;
		String linktype; //gw, snp, both
		public LinkedDisease(String do_term, String string) {
			this.disease = do_term;
			this.linktype = string;
		}

	}

	public static void measureOnSamples(String rdffile, String outputdir){
		String datafile = rdffile;
		//load the gene wiki index to get the mappings to ncbi gene ids
		String gindex = "./gw_data/gene_wiki_index_MANUAL.txt";
		//	Map<String, String> gene_page = new HashMap<String, String>();
		Map<String, String> page_gene = new HashMap<String, String>();
		File in = new File(gindex);
		if(in.canRead()){
			try {
				BufferedReader f = new BufferedReader(new FileReader(gindex));
				String line = f.readLine().trim();
				while(line!=null){
					if(!line.startsWith("#")){
						String[] item = line.split("\t");
						if(item!=null&&item.length>1){
							page_gene.put(item[1].replaceAll(" ", "_"), item[0]);
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
		}

		Model model = ModelFactory.createDefaultModel();
		model.read(datafile);
		//		Property snp = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3AHasSNP");
		//		Property associated_with = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3AIs-2Dassociated-2Dwith");
		//		Property sameas = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3ASame-2Das");

		Set<String> gene_disease_snp = new HashSet<String>();
		Set<String> gene_disease = new HashSet<String>();
		Map<String, ArrayList<LinkedDisease>> gene_overlap = new HashMap<String, ArrayList<LinkedDisease>>();
		Set<String> genes = new HashSet<String>();
		Set<String> diseases = new HashSet<String>();
		Set<String> snps = new HashSet<String>();
		////////////////////		
		////  Get gene-disease and gene-snp-disease examples
		///////////////////
		String queryString = 
			"PREFIX wiki: <http://184.72.42.242/mediawiki/index.php/Special:URIResolver/>" +
			"SELECT ?gene ?disease ?do_term "+ 
			"WHERE "+
			" { ?gene wiki:Property-3AIs-2Dassociated-2Dwith ?disease . " +
			" ?disease wiki:Property-3ASame-2Das ?do_term . " +
			"	FILTER regex(?do_term, \"^DOID\", \"i\") . " +
			"} ";

		//////////////////////
		////  Get all gene-disease, gene-snp relations
		/////////////////////

		Query query = QueryFactory.create(queryString);
		//		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model);

		try{
			ResultSet rs = qe.execSelect();
			while(rs.hasNext()){
				QuerySolution rb = rs.nextSolution() ;
				Resource gene = rb.getResource("gene");
				String do_term = rb.getLiteral("do_term").getString();
				Resource snp = rb.getResource("snp");

				String g = smwuriToText(gene);
				String gene_id = page_gene.get(g);

				if(gene_id!=null){
					genes.add(gene_id);
					diseases.add(do_term);
					if(snp!=null)snps.add(snp.getLocalName());

					if(gene_disease.add(gene_id+"_"+do_term)){
						ArrayList<LinkedDisease> linked = gene_overlap.get(gene_id);
						if(linked==null){
							linked = new ArrayList<LinkedDisease>();
						}
						linked.add(new SNPediaMashup.LinkedDisease(do_term,"gw"));
						gene_overlap.put(gene_id, linked);
					}else{
						//System.out.println(gene_id+"\t"+do_term);
					}
				}else{
					System.out.println("No gene mapped to "+g);
				}
			}
			//			System.out.println("Genes\tDiseases\tSNPS\tGene_Disease_Pairs\tGene_Disease_Snps");
			//			System.out.println(genes.size()+"\t"+diseases.size()+"\t"+snps.size()+"\t"+gene_disease.size()+"\t"+gene_disease_snp.size());
		}finally{
			// Important � free up resources used running the query
			qe.close();
		}

		queryString = 
			"PREFIX wiki: <http://184.72.42.242/mediawiki/index.php/Special:URIResolver/>" +
			"SELECT ?gene ?disease ?do_term ?snp "+ 
			"WHERE "+
			" {  "+
			" ?gene wiki:Property-3AHasSNP ?snp .  " +
			" ?snp wiki:Property-3AIs-2Dassociated-2Dwith ?disease . " +
			" ?disease wiki:Property-3ASame-2Das ?do_term . " +
			"	FILTER regex(?do_term, \"^DOID\", \"i\") . "+
			"} ";

		//////////////////////
		////  Get all gene-disease, gene-snp relations
		/////////////////////

		query = QueryFactory.create(queryString);
		// Execute the query and obtain results
		qe = QueryExecutionFactory.create(query, model);
		Set<String> bothpairs = new HashSet<String>();
		try{
			ResultSet rs = qe.execSelect();
			while(rs.hasNext()){
				QuerySolution rb = rs.nextSolution() ;
				Resource gene = rb.getResource("gene");
				String do_term = rb.getLiteral("do_term").getString();
				Resource snp = rb.getResource("snp");

				String g = smwuriToText(gene);
				String gene_id = page_gene.get(g);

				if(gene_id!=null){
					genes.add(gene_id);
					diseases.add(do_term);
					if(snp!=null)snps.add(snp.getLocalName());

					if(gene_disease.add(gene_id+"_"+do_term)){
						ArrayList<LinkedDisease> linked = gene_overlap.get(gene_id);
						if(linked==null){
							linked = new ArrayList<LinkedDisease>();
						}
						linked.add(new SNPediaMashup.LinkedDisease(do_term,"snp"));
						gene_overlap.put(gene_id, linked);
						bothpairs.add(gene_id+"_"+do_term);
					}
					else{
						if(bothpairs.add(gene_id+"_"+do_term)){
							//System.out.println(gene_id+" "+do_term+" "+snp);
							ArrayList<LinkedDisease> linked = gene_overlap.get(gene_id);
							if(linked==null){
								linked = new ArrayList<LinkedDisease>();
							}
							linked.add(new SNPediaMashup.LinkedDisease(do_term,"both"));
							gene_overlap.put(gene_id, linked);
						}
					}
				}else{
					System.out.println("No gene mapped to "+g);
				}
			}

			System.out.println("Genes\tDiseases\tSNPS\tGene_Disease_Pairs\tGene_Disease_Snps");
			System.out.println(genes.size()+"\t"+diseases.size()+"\t"+snps.size()+"\t"+gene_disease.size()+"\t"+gene_disease_snp.size());
			//
			System.out.println(gene_overlap.size());

			List<String> gs = new ArrayList<String>(gene_overlap.keySet());
			Collections.shuffle(gs);

			int group = 100;
			DescriptiveStatistics stats = new DescriptiveStatistics();
			for(int l=0; l<2000; l+=100){
				double g = 0; double b = 0; double s = 0;
				for(int i = l; i<l+100; i++){
					for(LinkedDisease ld : gene_overlap.get(gs.get(i))){
						if(ld.linktype.equals("gw")){
							g++;
						}else if(ld.linktype.equals("both")){
							b++;
						}else if(ld.linktype.equals("snp")){
							s++;
						}
					}
				}
				System.out.println(g+"\t"+b+"\t"+s+"\t"+b/(g+b+s));
				stats.addValue((double)b/(g+b+s));
			}
			System.out.println(stats);
		}finally{
			// Important � free up resources used running the query
			qe.close();
		}

	}

	/**
	 * Process the RDF export to produce a summary of the mashup.
	 * Main method for producing the results in ISMB bio-ontology SIG
	 * @param rdffile
	 * @param output
	 */
	public static void summarizeGeneDiseaseIntersection(String rdffile, String outputdir){
		String datafile = rdffile;
		String credfile = "/Users/bgood/workspace/Config/gw_creds.txt";
		Map<String, String> creds = GeneWikiUtils.read2columnMap(credfile);
		//	RevisionCounter rc = new RevisionCounter(creds.get("wpid"), creds.get("wppw"), snpedia);
		GeneWikiPage gwiki = new GeneWikiPage(creds.get("wpid"), creds.get("wppw"));
		//load the disease ontology mappings for comparison
		Map<String, Set<DOterm>> gene_dos = DOmapping.loadGeneRifs2DO();
		DOowl dowl = new DOowl();
		gene_dos = dowl.expandDoMap(gene_dos, false);

		//load the gene wiki index to get the mappings to ncbi gene ids
		String gindex = "./gw_data/gene_wiki_index_MANUAL.txt";
		//	Map<String, String> gene_page = new HashMap<String, String>();
		Map<String, String> page_gene = new HashMap<String, String>();
		File in = new File(gindex);
		if(in.canRead()){
			try {
				BufferedReader f = new BufferedReader(new FileReader(gindex));
				String line = f.readLine().trim();
				while(line!=null){
					if(!line.startsWith("#")){
						String[] item = line.split("\t");
						if(item!=null&&item.length>1){
							//		if(gene_page.get(item[0])!=null){
							//			System.out.println(item[0]+" duplicated "+item[1]+" -- "+gene_page.get(item[1]));
							//		}
							//		gene_page.put(item[0], item[1].replaceAll(" ", "_"));
							page_gene.put(item[1].replaceAll(" ", "_"), item[0]);
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
		}

		Model model = ModelFactory.createDefaultModel();
		model.read(datafile);
		//		Property snp = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3AHasSNP");
		//		Property associated_with = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3AIs-2Dassociated-2Dwith");
		//		Property sameas = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3ASame-2Das");


		////////////////////		
		////  Get gene-disease and gene-snp-disease examples
		///////////////////
		String queryString = 
			"PREFIX wiki: <http://184.72.42.242/mediawiki/index.php/Special:URIResolver/>" +
			"SELECT ?gene ?disease ?do_term ?snp "+ 
			"WHERE { "+
			" ?gene wiki:Property-3AIs-2Dassociated-2Dwith ?disease . " +
			" ?disease wiki:Property-3ASame-2Das ?do_term . " +
			"	FILTER regex(?do_term, \"^DOID\", \"i\") . "+
			" ?gene wiki:Property-3AHasSNP ?snp .  " +
			" ?snp wiki:Property-3AIs-2Dassociated-2Dwith ?disease . " +
			"} ";

		//////////////////////
		////  Get all gene-disease, gene-snp relations
		/////////////////////

		Query query = QueryFactory.create(queryString);
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model);

		try{
			ResultSet rs = qe.execSelect();
			try {
				FileWriter tab = new FileWriter(outputdir+"gene2diseaseANDsnp2disease.txt");
				tab.write("Gene\tTitle\tDisease\tDiseaseOntologyTerm\tSNP\tMatch2DOA\n");
				int parents = 0; int directs = 0; int total = 0;
				Set<String> gene_disease_snp = new HashSet<String>();
				Set<String> gene_disease = new HashSet<String>();
				Set<String> genes = new HashSet<String>();
				Set<String> diseases = new HashSet<String>();
				Set<String> snps = new HashSet<String>();
				while(rs.hasNext()){
					QuerySolution rb = rs.nextSolution() ;
					Resource gene = rb.getResource("gene");
					Resource disease = rb.getResource("disease");
					String do_term = rb.getLiteral("do_term").getString();
					Resource snp = rb.getResource("snp");

					String g = smwuriToText(gene);
					String gene_id = page_gene.get(g);
					String doa_match = "none";
					Set<DOterm> dos = null;


					if(gene_id==null){
						gwiki.setTitle(g);
						gwiki.setTitleToRedirect();
						gwiki.retrieveWikiTextContent(false);
						gwiki.parseAndSetNcbiGeneId();
						gene_id = gwiki.getNcbi_gene_id();
						if(gene_id!=null&&gene_id.trim()!=""){
							page_gene.put(g, gene_id);
							FileWriter f = new FileWriter(gindex, true);
							f.write(gene_id+"\t"+g+"\n");
							f.close();
						}else{
							gene_id = null;
						}
					}

					if(gene_id!=null){
						genes.add(gene_id);
						diseases.add(do_term);
						snps.add(snp.getLocalName());

						if(gene_disease.add(gene_id+"_"+do_term)){
							total++;
							dos = gene_dos.get(gene_id);
							if(dos!=null){
								for(DOterm dot : dos){
									if(dot.getAccession().equals(do_term)){
										if(dot.isInferred_parent()){
											doa_match = "parent";
											parents++;
										}else{
											doa_match = "direct";
											directs++;
										}
										break;
									}
								}
							}
						}else{
							System.out.println(gene_id+"\t"+do_term);
						}
						if(gene_disease_snp.add(gene_id+" "+do_term+" "+snp)){
							tab.write(gene_id+"\t"+g+"\t"+smwuriToText(disease)+"\t"+do_term+"\t"+smwuriToText(snp)+"\t"+doa_match+"\n");
						}
					}else{
						System.out.println("No gene mapped to "+g);
					}
				}
				System.out.println("parents\tdirects\tnone\ttotal");
				System.out.println(parents+"\t"+directs+"\t"+(total-parents-directs)+"\t"+total);
				System.out.println("Genes\tDiseases\tSNPS\tGene_Disease_Pairs\tGene_Disease_Snps");
				System.out.println(genes.size()+"\t"+diseases.size()+"\t"+snps.size()+"\t"+gene_disease.size()+"\t"+gene_disease_snp.size());
				tab.close();



			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}finally{
			// Important � free up resources used running the query
			qe.close();
		}




		///////////////////
		//// Get all snp-disease
		//////////////////
		/*		queryString = 
			"PREFIX wiki: <http://184.72.42.242/mediawiki/index.php/Special:URIResolver/>" +
			"SELECT ?gene ?disease ?do_term ?snp "+ 
			"WHERE { "+
			" ?gene wiki:Property-3AIs-2Dassociated-2Dwith ?disease . " +
			" ?disease wiki:Property-3ASame-2Das ?do_term . " +
			"	FILTER regex(?do_term, \"^DOID\", \"i\") . "+
			" 	OPTIONAL {?gene wiki:Property-3AHasSNP ?snp . } " +
			"} ";


		query = QueryFactory.create(queryString);
		// Execute the query and obtain results
		qe = QueryExecutionFactory.create(query, model);

		try{
			ResultSet rs = qe.execSelect();

			try {
				FileWriter tab = new FileWriter("/Users/bgood/data/SMW/gene_snp_disease_doid_DOA.txt");
				int parents = 0; int directs = 0; int total = 0;
				while(rs.hasNext()){
					QuerySolution rb = rs.nextSolution() ;
					Resource gene = rb.getResource("gene");
					Resource disease = rb.getResource("disease");
					String do_term = rb.getLiteral("do_term").getString();
					Resource snp = rb.getResource("snp");

					String g = smwuriToText(gene);
					String gene_id = page_gene.get(g);
					String doa_match = "none";
					Set<DOterm> dos = null;
					if(gene_id!=null){
						total++;
						dos = gene_dos.get(gene_id);
						if(dos!=null){
							for(DOterm dot : dos){
								if(dot.getAccession().equals(do_term)){
									if(dot.isInferred_parent()){
										doa_match = "parent";
										parents++;
									}else{
										doa_match = "direct";
										directs++;
									}
									break;
								}
							}
						}			
						tab.write(gene_id+"\t"+g+"\t"+smwuriToText(disease)+"\t"+do_term+"\t"+smwuriToText(snp)+"\t"+doa_match+"\n");
					}else{
						System.out.println("No gene mapped to "+g);
					}
				}
				System.out.println("parents\tdirects\tnone\ttotal");
				System.out.println(parents+"\t"+directs+"\t"+(total-parents-directs)+"\t"+total);
				tab.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}finally{
			// Important � free up resources used running the query
			qe.close();
		}
		 */
	}

	public static String smwuriToText(Resource r){
		if(r==null){
			return "";
		}
		String output = r.getURI();
		String buri = "http://184.72.42.242/mediawiki/index.php/Special:URIResolver/";

		if(output.trim().length()>buri.length()){
			output = output.replace(buri, "").replaceAll("-2D", "-");
		}

		return output;
	}

}
