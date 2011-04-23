/**
 * 
 */
package org.gnf.genewiki.rdf;

import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gnf.dont.DOmapping;
import org.gnf.dont.DOowl;
import org.gnf.dont.DOterm;
import org.gnf.genewiki.GeneWikiLink;
import org.gnf.genewiki.GeneWikiPage;
import org.gnf.genewiki.GeneWikiUtils;
import org.gnf.genewiki.WikiCategoryReader;
import org.gnf.genewiki.metrics.RevisionCounter;
import org.gnf.ncbo.web.AnnotatorClient;
import org.gnf.ncbo.web.NcboAnnotation;
import org.gnf.util.MapFun;
import org.gnf.wikiapi.Category;
import org.gnf.wikiapi.Page;
import org.gnf.wikiapi.User;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
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
		//dumpTabTriples();
		//runQueries();
		//getEditCountsForSNPedia()
		//getSNPDiseaseLinksFromSNPedia()
		//load snp-disease from snpedia
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
				d_page.retrieveWikiTextContent();
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
				List<NcboAnnotation> doas = AnnotatorClient.ncboAnnotateText(disease_snp.getKey(), true, false, true, false, false, true);
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

	public static void runQueries(){
		String datafile = "file:///Users/bgood/data/SMW/gw_snp_mashup.rdf";
		//load the disease ontology mappings for comparison
		Map<String, Set<DOterm>> gene_dos = DOmapping.loadGeneRifs2DO();
		DOowl dowl = new DOowl();
		gene_dos = dowl.expandDoMapUp(gene_dos);

		//load the gene wiki index to get the mappings to ncbi gene ids
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
		//		Property snp = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3AHasSNP");
		//		Property associated_with = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3AIs-2Dassociated-2Dwith");
		//		Property sameas = model.getProperty("http://184.72.42.242/mediawiki/index.php/Special:URIResolver/Property-3ASame-2Das");

		String queryString = 
			"PREFIX wiki: <http://184.72.42.242/mediawiki/index.php/Special:URIResolver/>" +
			"SELECT ?gene ?disease ?do_term ?snp "+ 
			"WHERE { "+
			" ?gene wiki:Property-3AIs-2Dassociated-2Dwith ?disease . " +
			" ?disease wiki:Property-3ASame-2Das ?do_term . " +
			"	FILTER regex(?do_term, \"^DOID\", \"i\") . "+
			" 	OPTIONAL {?gene wiki:Property-3AHasSNP ?snp . } " +
			"} ";
		Query query = QueryFactory.create(queryString);
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model);

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
				System.out.println("parents\tchildren\tdirects\tnone\ttotal");
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
