package org.gnf.util;

import org.gnf.wikiapi.Connector;
import org.gnf.wikiapi.Page;
import org.gnf.wikiapi.ParseData;
import org.gnf.wikiapi.User;
import org.gnf.wikiapi.query.Parse;
import org.gnf.wikiapi.query.RequestBuilder;

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
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.gnf.genewiki.Config;
import org.gnf.genewiki.GeneWikiLink;
import org.gnf.genewiki.GeneWikiPage;
import org.gnf.genewiki.GeneWikiUtils;
import org.gnf.genewiki.associations.AnnoMapCompare;
import org.gnf.genewiki.associations.CandidateAnnotation;
import org.gnf.genewiki.parse.ParseUtils;
import org.gnf.go.GOmapper;
import org.gnf.go.GOowl;
import org.gnf.go.GOterm;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class BioInfoUtil {

	public static String GENE_INFO_HUMAN = "/Users/bgood/data/genewiki/input/publicgeneinfo/gene_info.Hs";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//make a humangene2go
		/*	String gene2go =  "C:/Users/bgood/data/genewiki/gene2go";
		String out =  "C:/Users/bgood/data/genewiki/human_gene2go";
		String taxon = "9606";
		getSpeciesSpecificGene2go(gene2go, taxon, out);
		 */
		/*	generateDataForGOAChart(); */

		/*		String gene2e =  "C:/Users/bgood/data/genewiki/gene2ensembl";
		String human_gene2e =  "C:/Users/bgood/data/genewiki/human_gene2ensembl";
		String e2e =  "C:/Users/bgood/data/genewiki/human_gene2ensembl";
		String humanpanther =  "C:/Users/bgood/data/genewiki/panther_human.0_HUMAN";
		//getSpeciesSpecificGene2go(gene2go, taxon, out);
		String out = "C:/Users/bgood/data/genewiki/gene2panther2go";
		buildGenePanther2go(human_gene2e, humanpanther, out);
		 */
		//		compareMaps();
		//	String human_gene2e =  "C:/Users/bgood/data/genewiki/human_gene2ensembl";
		/*	String human_gene2e =  "C:/Users/bgood/data/gene_ensembl__xref_entrezgene__dm.txt";
		String human_func = "C:/Users/bgood/data/genewiki/human_funcbase/predictions/human_predictions.tab";
		String out = "C:/Users/bgood/data/genewiki/human_funcbase/predictions/human_predictions_gene.tab/";
		buildGene2Funcbase2go(human_gene2e, human_func,out);
		 */

		/*		Map<String, Gene> human = loadHumanGeneInfo();
		System.out.println(human.size());
		 */
		//read in gene to go ~term map
		/*	String file = Config.gene_go_file;
		Map<String, Set<GOterm>> geneid_go = GeneWikiUtils.readGene2GO(file, "\t", "#");
		System.out.println("read geneid_go map");

		file = Config.gene_panther_go_file;
		//file = "C:/Users/bgood/data/genewiki/panther/gene2panther2go_from_web";
		Map<String, Set<GOterm>> panther_go = GeneWikiUtils.readGene2GO(file,  "\t", "#");
		AnnoMapCompare c = compareGOMaps(geneid_go, panther_go, true);
		System.out.println(c.toString());
		 */

		Map<String, Set<GOterm>> genego = GeneWikiUtils.readGene2GO(Config.gene_go_file, "\t", "#");
		genego = expandGoMap(genego, null, true);


	}

	/**
	 * 
	 * @param m
	 * @param old_ont
	 * @return
	 */
	public static Map<String, Set<GOterm>> expandGoMap(Map<String, Set<GOterm>> m, GOowl gol, GOowl gol_no_infer, boolean addChildren){

		//get parents for all linked terms
		Map<GOterm, Set<GOterm>> ps = new HashMap<GOterm, Set<GOterm>>(m.size());
		//children - one level
		Map<GOterm, Set<GOterm>> cs = new HashMap<GOterm, Set<GOterm>>(m.size());
		for(Entry<String, Set<GOterm>> entry : m.entrySet()){
			for(GOterm goterm : entry.getValue()){
				if(!ps.containsKey(goterm)){
					Set<GOterm> parents = gol.getSupers(goterm);
					ps.put(goterm, parents);
				}
				if(addChildren){
					if(!cs.containsKey(goterm)){
						Set<GOterm> children = gol_no_infer.getDirectChildren(goterm);
						cs.put(goterm, children);
					}
				}
			}
		}
		System.out.println("expansions cached");



		Map<String, Set<GOterm>> tmp_geneid_go = new HashMap<String, Set<GOterm>>();
		//add the parents and the children
		for(Entry<String, Set<GOterm>> entry : m.entrySet()){
			String geneid = entry.getKey();
			int size = 0;

			for(GOterm goterm : entry.getValue()){				
				if(ps.get(goterm)!=null){
					int psize = ps.get(goterm).size();
					//					if(psize>50){
					//						Set<GOterm> bigmama = ps.get(goterm);
					//						System.err.println("Wow.. many parents for "+geneid+" "+goterm.getTerm()+" "+psize);
					//					}
					size+=psize;
				}
				if(addChildren&&cs.get(goterm)!=null){					
					int csize = cs.get(goterm).size();
					//					if(csize>50){
					//						System.err.println("Wow.. big family for "+geneid+" "+goterm.getTerm()+" "+csize);
					//					}
					size+=csize;
				}
			}

			Set<GOterm> p = new HashSet<GOterm>(size);
			for(GOterm goterm : entry.getValue()){
				p.add(goterm);
				Set<GOterm> parents = ps.get(goterm);
				if(parents!=null){
					for(GOterm parent : parents){
						if(parent != goterm){
							p.add(parent);
						}
					}
				}

				//add the children - if you can take it!
				if(addChildren){
					Set<GOterm> children = cs.get(goterm);
					if(children!=null){
						for(GOterm child : children){
							if(child != goterm){
								p.add(child);
							}
						}
					}
				}

			}
			tmp_geneid_go.put(geneid, p);
		}

		return tmp_geneid_go;
	}

	/**
	 * 
	 * @param m
	 * @param old_ont
	 * @return
	 */
	public static Map<String, Set<GOterm>> expandGoMap(Map<String, Set<GOterm>> m, String old_ont, boolean addChildren){
		GOowl gol = new GOowl();
		GOowl gol_no_infer = null;
		if(old_ont==null){
			gol.initFromFileRDFS(true);
		}else{
			gol.go = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
			gol.go.read(old_ont);
			System.out.println("read go OWL file into Jena Model with RDFS inference on");
		}
		if(addChildren){
			gol_no_infer = new GOowl();
			if(old_ont==null){
				gol_no_infer.initFromFile(true);
			}else{
				gol_no_infer.go = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
				gol_no_infer.go.read(old_ont);
				System.out.println("read go OWL file into Jena Model with no reasoning");
			}
		}

		//get parents for all linked terms
		Map<GOterm, Set<GOterm>> ps = new HashMap<GOterm, Set<GOterm>>();
		//children - one level
		Map<GOterm, Set<GOterm>> cs = new HashMap<GOterm, Set<GOterm>>();
		for(Entry<String, Set<GOterm>> entry : m.entrySet()){
			for(GOterm goterm : entry.getValue()){
				if(!ps.containsKey(goterm)){
					Set<GOterm> parents = gol.getSupers(goterm);
					ps.put(goterm, parents);
				}
				if(addChildren){
					if(!cs.containsKey(goterm)){
						Set<GOterm> children = gol_no_infer.getDirectChildren(goterm);
						cs.put(goterm, children);
					}
				}
			}
		}
		System.out.println("expansions cached");



		Map<String, Set<GOterm>> tmp_geneid_go = new HashMap<String, Set<GOterm>>();
		//add the parents
		for(Entry<String, Set<GOterm>> entry : m.entrySet()){
			String geneid = entry.getKey();
			Set<GOterm> p = new HashSet<GOterm>();
			for(GOterm goterm : entry.getValue()){
				p.add(goterm);
				Set<GOterm> parents = ps.get(goterm);
				if(parents!=null){
					for(GOterm parent : parents){
						if(parent != goterm){
							p.add(parent);
						}
					}
				}

				//add the children - if you can take it!
				if(addChildren){
					Set<GOterm> children = cs.get(goterm);
					if(children!=null){
						for(GOterm child : children){
							if(child != goterm){
								p.add(child);
							}
						}
					}
				}

			}
			tmp_geneid_go.put(geneid, p);
		}

		gol.close();
		if(gol_no_infer!=null){
			gol_no_infer.close();
		}
		return tmp_geneid_go;
	}

	/**
	 * Given any two maps of thing to sets of goterms, find the relations between the sets (keys and annotations)	
	 * @param geneid_go
	 * @param panther_go
	 * @return
	 */
	public static AnnoMapCompare compareGOMaps(Map<String, Set<GOterm>> s1, Map<String, Set<GOterm>> s2, boolean reason){
		double gene_match = 0; double go_match = 0; double pgo_count = 0; double pgenes = 0;
		double annos_in_gene_intersect_from_s1 = 0;
		double annos_in_gene_intersect_from_s2 = 0;

		//expand set 2..
		if(reason){
			s2 = expandGoMap(s2, null, false);
		}

		for(Entry<String, Set<GOterm>> panther : s2.entrySet()){
			pgo_count+= panther.getValue().size();
			pgenes++;
			if(s1.get(panther.getKey())!=null){
				gene_match++;
				for(GOterm pgo : panther.getValue()){
					annos_in_gene_intersect_from_s2++;
					for(GOterm hgo : s1.get(panther.getKey())){
						if(pgo.getAccession().equals(hgo.getAccession())){
							go_match++;
						}
					}
				}
			}
		}

		double rgo_count = 0; 
		double rgenes = 0;
		for(Entry<String, Set<GOterm>> reverse : s1.entrySet()){
			rgo_count+= reverse.getValue().size();
			rgenes++;
			if(s2.get(reverse.getKey())!=null){
				annos_in_gene_intersect_from_s1+= reverse.getValue().size();
			}
		}
		SetComparison keysetcompare = new SetComparison(rgenes, pgenes, gene_match);
		//only compare annotations from the intersection.
		SetComparison annosetcompare = new SetComparison(annos_in_gene_intersect_from_s1, annos_in_gene_intersect_from_s2, go_match);
		AnnoMapCompare result = new AnnoMapCompare(keysetcompare, annosetcompare);

		return result;
	}


	public static void compareMaps(){
		//read in gene to go ~term map
		String file = Config.gene_go_file;
		Map<String, Set<GOterm>> geneid_go = GeneWikiUtils.readGene2GO(file, "\t", "#");
		System.out.println("read geneid_go map");

		file = Config.gene_panther_go_file;
		//file = "C:/Users/bgood/data/genewiki/panther/gene2panther2go_from_web";
		Map<String, Set<GOterm>> panther_go = GeneWikiUtils.readGene2GO(file,  "\t", "#");

		System.out.println("read panther geneid_go map");
		float gene_match = 0; float go_match = 0; float pgo_count = 0; float pgenes = 0;
		for(Entry<String, Set<GOterm>> panther : panther_go.entrySet()){
			if(!panther.getKey().startsWith("E")){
				pgo_count+= panther.getValue().size();
				pgenes++;
				if(geneid_go.get(panther.getKey())!=null){
					gene_match++;
					for(GOterm pgo : panther.getValue()){
						for(GOterm hgo : geneid_go.get(panther.getKey())){
							if(pgo.getAccession().equals(hgo.getAccession())){
								go_match++;
							}
						}
					}
				}
			}
		}
		System.out.println("genes\t"+pgenes+"\tgene match:\t"+gene_match+"\t"+gene_match/(float)pgenes +"\t"+
				"\tgo_match\t"+go_match+"\tpgo_count\t"+pgo_count+"\tfraction matched\t"+go_match/pgo_count);


		System.out.println("\n\n");
		gene_match = 0; go_match = 0; pgo_count = 0; pgenes = 0;
		for(Entry<String, Set<GOterm>> panther : geneid_go.entrySet()){
			if(!panther.getKey().startsWith("E")){
				pgo_count+= panther.getValue().size();
				pgenes++;
				if(panther_go.get(panther.getKey())!=null){
					gene_match++;
					for(GOterm pgo : panther.getValue()){
						for(GOterm hgo : panther_go.get(panther.getKey())){
							if(pgo.getAccession().equals(hgo.getAccession())){
								go_match++;
							}
						}
					}
				}
			}
		}
		System.out.println("genes\t"+pgenes+"\tgene match:\t"+gene_match+"\t"+gene_match/(float)pgenes +"\t"+
				"\tgo_match\t"+go_match+"\tpgo_count\t"+pgo_count+"\tfraction matched\t"+go_match/pgo_count);


	}

	public static Map<String, Gene> loadHumanGeneInfo(String key){
		Map<String, Gene> human = new HashMap<String, Gene>();
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(GENE_INFO_HUMAN));
			String line = f.readLine();
			while(line!=null){
				String[] item = line.split("\t");
				if(item!=null&&item.length>3){
					Gene g = new Gene();
					g.setGeneID(item[0]);
					g.setGeneSymbol(item[1]);
					g.setGeneDescription(item[2]);
					if(!item[3].equals("-")){
						String[] names = item[3].split("\\|");
						List<String> gnames = new ArrayList<String>(names.length);
						for(String n : names){
							gnames.add(n);
						}
						g.setAltids(gnames);
					}
					if(key.equals("geneid")){				
						human.put(g.getGeneID(), g);
					}else if(key.equals("symbol")){
						human.put(g.getGeneSymbol(), g);
					}else{
						System.out.println("please choose gene key {geneid or symbol}");
						return null;
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
		return human;
	}

	public static String stripNonAsci(String line){
		return(line.replaceAll("[^\\p{ASCII}]", ""));
	}

	public static void stripNonAsciForFile(String inputfile, String outfile){
		//output = input.replaceAll([^\\p{ASCII}], "");

		BufferedReader f;
		FileWriter w;
		try {
			w = new FileWriter(outfile);
			f = new BufferedReader(new FileReader(inputfile));
			String line = f.readLine();
			while(line!=null){
				w.write(line.replaceAll("[^\\p{ASCII}]", "")+"\n");
				line = f.readLine();
			}
			f.close();
			w.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String callWikipediaParser(String wikitext, String title){
		User user = new User("", "", "http://en.wikipedia.org/w/api.php");
		user.login();
		Connector connector = new Connector();
		RequestBuilder request = Parse.create().text(wikitext).title(title);  
		ParseData parseData = connector.parse(user, request);
		// String html = StringEscapeUtils.unescapeHtml(parseData.getText());
		//System.out.println("Retrieved html text:\n" + parseData.getText());	
		return parseData.getText();
	}
	/*
	public static void makeWikiTextLegible(){
		String gwikidir = "C:/Users/bgood/data/genewiki/javaobj2/";
		List<GeneWikiPage> pages = GeneWikiUtils.loadSerializedDir(gwikidir, 10);
		System.out.println("read in wikigene files");
		int linkcount = 0;
		//		 String input = "is a [[protein]] which in ";
		for(GeneWikiPage page : pages){
			for(GeneWikiLink glink : page.getData().getGlinks()){
				String p = glink.getParagraph();

				p = cleanupGeneWikitext(p);

				System.out.println(p+"\n");
			}
		}
	}
	 */

	public static String cleanupGeneWikitext(String p){

		if(p.trim().startsWith("[[Image") && p.trim().endsWith("]]")){
			p = callWikipediaParser(p, "");
		}else{
			p = p.replaceAll("\\{\\{FixBunching.*?\\}\\}", "");
			p = p.replaceAll("\\{\\{PBB.*?\\}\\}", "");
			//remove links inside of images
			p = p.replaceAll("Image.*?\\[\\[.*?\\]\\]", "");
			p = p.replaceAll("\\[\\[\\.\\]\\]", "");
			//remove images
			p = p.replaceAll("\\[\\[Image.*?\\]\\]", "");
			//get rid of references to entrez gene
			p = p.replaceAll("<ref name=\"entrez\">.*?</ref>", "");
			//convert wikiformatting to html (e.g. put in links for [[]]				
			//	p = ParseUtils.wikiToHtml(p);				
			//remove templates
			p = p.replaceAll("\\{\\{.*?\\}\\}", "");
			p = p.replaceAll("#_note-pmid","http://www.ncbi.nlm.nih.gov/pubmed?term=");
			p = p.replaceAll(">\\[.*?\\]<", ">[Ref]<");

		}
		p = p.replaceAll("\\n", "");
		p = p.replaceAll("<pre>", "");
		p = p.replaceAll("</pre>", "");
		p = p.replaceAll(",", "-");
		p = stripNonAsci(p);
		return p;
	}



	public static void generateDataForGOAChart(){
		//read in gene to go ~term map
		String gene_go_file = "C:/Users/bgood/data/genewiki/human_gene2go";
		String outfile = "C:/Users/bgood/data/genewiki/human_annotation";
		Map<String, Set<GOterm>> geneid_go = GeneWikiUtils.readGene2GO(gene_go_file, "\t", "#");
		System.out.println("read geneid_go map");
		//http://www.ncbi.nlm.nih.gov/portal/utils/pageresolver.fcgi?recordid=1281028831410665
		//aug 5, 2010 - genes, and genomes, no pseudogenes, human[ORGN]
		int nhuman = 27050;
		DescriptiveStatistics stats = new DescriptiveStatistics();
		try {
			FileWriter out = new FileWriter(outfile);
			for(Entry<String, Set<GOterm>> gene_go : geneid_go.entrySet()){
				out.write(gene_go.getKey()+"\t"+gene_go.getValue().size()+"\n");
				stats.addValue(gene_go.getValue().size());
			}
			System.out.println(stats.toString()+"\n");
			for(int i=0; i< nhuman-geneid_go.size(); i++){
				out.write("gene\t0\n");
				stats.addValue(0);
			}
			System.out.println(stats.toString()+"\n");
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void getSpeciesSpecificGene2go(String gene2gofile, String taxon, String outfile){
		BufferedReader f;
		FileWriter w;
		try {
			w = new FileWriter(outfile);
			f = new BufferedReader(new FileReader(gene2gofile));
			String line = f.readLine();
			line = f.readLine();//header
			w.write(line+"\n");
			while(line!=null){
				String[] item = line.split("\t");
				if(item[0].equals(taxon)){
					w.write(line+"\n");
				}
				line = f.readLine();
			}
			f.close();
			w.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void getSpeciesSpecificGene2Ensembl(String gene2efile, String taxon, String outfile){
		BufferedReader f;
		FileWriter w;
		try {
			w = new FileWriter(outfile);
			f = new BufferedReader(new FileReader(gene2efile));
			String line = f.readLine();
			line = f.readLine();//header
			//w.write(line+"\n");
			while(line!=null){
				String[] item = line.split("\t");
				if(item[0].equals(taxon)){
					w.write(line+"\n");
				}
				line = f.readLine();
			}
			f.close();
			w.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Map<String, List<String>> getHumanGene2pub(String gene2pubmed_file){
		Map<String, List<String>> p2g = new HashMap<String, List<String>>();
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(gene2pubmed_file));
			String line = f.readLine();
			while(line!=null){
				if(!line.startsWith("9606")){
					line = f.readLine();
					continue;
				}
				String[] g2p = line.split("\t");
				List<String> pmids = p2g.get(g2p[1]);
				if(pmids==null){
					pmids = new ArrayList<String>();
				}
				pmids.add(g2p[2]);
				p2g.put(g2p[1], pmids);
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
		return p2g;	
	}

	public static HashMap<String, Set<GOterm>> readGene2GO(String file, boolean skipIEA, boolean only_human){
		HashMap<String,Set<GOterm>> map = new HashMap<String, Set<GOterm>>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(file));
			String line = f.readLine().trim();
			while(line!=null){
				if(!line.startsWith("#")){
					String[] item = line.split("\t");
					if(item!=null&&item.length>1){
						String taxa = item[0];
						if(!taxa.equals("9606")&&only_human){
							line = f.readLine();
							continue;
						}
						String code = item[3];
						//always skip no data...
						if(code.equals("ND")){
							line = f.readLine();
							continue;
						}
						if(skipIEA&&code.equals("IEA")){
							line = f.readLine();
							continue;
						}
						String geneid = item[1];					
						Set<GOterm> GOs = map.get(geneid);
						if(GOs == null){
							GOs = new HashSet<GOterm>();
							map.put(geneid, GOs);
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
		return map;
	}

	public static void writeStatsForR(DescriptiveStatistics stats, String outfile, String colheader){
		try {
			FileWriter out = new FileWriter(outfile);
			out.write(colheader+"\n");
			for(double v : stats.getValues()){
				out.write(v+"\n");
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
