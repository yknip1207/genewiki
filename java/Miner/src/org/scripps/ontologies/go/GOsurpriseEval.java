package org.scripps.ontologies.go;
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

import org.genewiki.GeneWikiUtils;
import org.genewiki.annotationmining.Config;
import org.scripps.util.BigFile;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.vocabulary.OWL;
public class GOsurpriseEval {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	//	double p = .05;
	//	getLowPpairs(p);
	//	makeAntiAssocDataLegible(p);
		
		testOnHumanGOA();
//		testOnGwikiLinks2GO();
//		testOnRandomizedHumanGOA();
//		testOnWikiLinksBasedonGOAdjs();
		
	}

	public static double getSurprise(String acc1, String acc2, Map<String, Double> go_go){
			if(acc1.compareTo(acc2) > 0){
				String tmp = acc1;
				acc1 = acc2;
				acc2 = tmp;
			}
			Double d = go_go.get(acc1+acc2);
			if(d!=null){
				return d.doubleValue();
			}else{
				return 1;
			}
	}
	
	public static void testOnWikiLinksBasedonGOAdjs(){
		double wlinks2go = 0;  double wlinks2go_bad = 0;  
		double pmax = .01;
		
		//get all the unknown links
		String file = "C:\\Users\\bgood\\data\\genewiki\\gwiki2golinks_smartestmaps_with_no_reasoningNR";
		HashMap<String, Set<GOterm>> wiki_gos = GeneWikiUtils.readGenewiki2GOMapped(file);
		//keep only Unknown
		wiki_gos = GeneWikiUtils.filterGoMapByEvidence(wiki_gos, "Known");
		//map page titles to gene ids
		//read in article_gene map;
		file = "data/genewiki/GeneWikiIndex.txt";
		String delimiter = "\t";
		String skipprefix = "#";
		boolean reverse = true;
		//could be more than one!
		Map<String, Set<String>> article_geneid = GeneWikiUtils.readMapFile(file, delimiter, skipprefix, reverse);
		//get goa
		boolean addParents = false;
		HashMap<String, Set<GOterm>> gene_gos = GeneWikiUtils.readGene2GO(Config.gene_go_file, addParents);
		//get the anti-associated list
		Map<String, Double> go_go = loadAntiAssoc();
		//for all the unknown links
		for(Entry<String, Set<GOterm>> entry : wiki_gos.entrySet()){
			String wgene = entry.getKey();
			Set<GOterm> wikigos = entry.getValue();
			wlinks2go+=wikigos.size();
			//get the goa terms for the source gene
			for(String geneid : article_geneid.get(wgene)){
				Set<GOterm> goas = gene_gos.get(geneid);
				//get the anti-correlated set for all of these terms (if any)
				//count and report occcurrences of a match from an anti-correlated go to the unknown link
				if(goas!=null){
				for(GOterm go2 : wikigos){					
					boolean bad = false;
					for(GOterm go1 : goas){
						String acc1 = go1.getAccession();
						String acc2 = go2.getAccession();
							if(acc1.compareTo(acc2) > 0){
								String tmp = acc1;
								acc1 = acc2;
								acc2 = tmp;
							}
							Double d = go_go.get(acc1+acc2);
							if(d!=null&&d<pmax){
								bad = true;
								//disjoint_pairs++;
								//avg_pdj+=d;
								System.out.println(wgene+"\t"+go2+"\t"+go1+"\t"+d.doubleValue());
							}	
					}
					if(bad){
						wlinks2go_bad++;
					}
				}
				}
			}
			}
		System.out.println("\ntotal tested links: "+wlinks2go+"\nbad: "+wlinks2go_bad+" "+wlinks2go_bad/wlinks2go);
				
	}

	public static void testOnRandomizedHumanGOA(){
		boolean addParents = false;
		HashMap<String, Set<GOterm>> gene_gos = GeneWikiUtils.readGene2GO(Config.gene_go_file, addParents);
		HashMap<String, Set<GOterm>> random = new HashMap<String, Set<GOterm>>();
		Set<GOterm> allgo = new HashSet<GOterm>();
		for(Entry<String, Set<GOterm>> entry : gene_gos.entrySet()){
			allgo.addAll(entry.getValue());
		}
		List<GOterm> allgolist = new ArrayList<GOterm>(allgo);
		int n = allgolist.size();
		for(Entry<String, Set<GOterm>> entry : gene_gos.entrySet()){
			Set<GOterm> rgos = new HashSet<GOterm>();
			for(int i = 0; i<entry.getValue().size(); i++){
				GOterm rgo = allgolist.get(ranInRange(n-1));
				rgos.add(rgo);
			}
			random.put(entry.getKey(), rgos);
			
		}
		
		testGeneGOMapForDJs(random);
	}
	
	public static int ranInRange(int max){
		double r = max*Math.random();
		return Math.round((float)r);
	}
	
	public static void testOnGwikiLinks2GO(){
		String file = "C:\\Users\\bgood\\data\\genewiki\\gwiki2golinks_loosestmaps_with_no_reasoningNR";
		HashMap<String, Set<GOterm>> gene_gos = GeneWikiUtils.readGenewiki2GOMapped(file);
		HashMap<String, Set<GOterm>> known = new HashMap<String, Set<GOterm>>();
		HashMap<String, Set<GOterm>> unknown = new HashMap<String, Set<GOterm>>();
		for(Entry<String, Set<GOterm>> entry : gene_gos.entrySet()){
			Set<GOterm> k = new HashSet<GOterm>();
			Set<GOterm> u = new HashSet<GOterm>();
			for(GOterm goterm : entry.getValue()){
				if(goterm.getEvidence().equals("Known")){
					k.add(goterm);
				}else{
					u.add(goterm);
				}
			}
			if(k.size()>0){
				known.put(entry.getKey(), k);
			}
			if(u.size()>0){
				unknown.put(entry.getKey(), u);
			}
		}
		
		System.out.println("All");
		testGeneGOMapForDJs(gene_gos);
		
		System.out.println("known");
		testGeneGOMapForDJs(known);
		
		System.out.println("unknown");
		testGeneGOMapForDJs(unknown);
	}
	
	public static void testOnHumanGOA(){
		boolean addParents = false;
		HashMap<String, Set<GOterm>> gene_gos = GeneWikiUtils.readGene2GO(Config.gene_go_file, addParents);
		testGeneGOMapForDJs(gene_gos);
	}

	public static void testGeneGOMapForDJs(HashMap<String, Set<GOterm>> gene_gos){
		double very_t = 0.01;
		Map<String, Double> go_go = loadAntiAssoc();
		int c = 0; float total_pairs = 0; float disjoint_pairs = 0; float very_dj_pairs = 0;
		int annotations = 0; double avg_pdj = 0;
		for(Entry<String, Set<GOterm>> gene_go : gene_gos.entrySet()){
			String gene = gene_go.getKey();
			Set<GOterm> clone = new HashSet<GOterm>(gene_go.getValue());
			for(GOterm go1 : gene_go.getValue()){
				annotations++;
				for(GOterm go2 : clone){
					if(!go2.equals(go1)){
						total_pairs++;
						String acc1 = go1.getAccession();
						String acc2 = go2.getAccession();
						if(acc1.compareTo(acc2) > 0){
							String tmp = acc1;
							acc1 = acc2;
							acc2 = tmp;
						}
						Double d = go_go.get(acc1+acc2);
						if(d!=null){
							disjoint_pairs++;
							avg_pdj+=d;
							if(d < very_t){
								very_dj_pairs++;
							}
							//System.out.println(gene+"\t"+acc1+"\t"+acc2+"\t"+d.doubleValue());
						}
					}
				}
			}
			c++;
		//	if(c%1000==0){
		//		System.out.print(c+" ");
		//	}
		}
		System.out.println("\nGenes: "+c+" annotations: "+annotations+" pairs: "+total_pairs+" dj_pairs: "+disjoint_pairs+" fraction "+disjoint_pairs/total_pairs);
		System.out.println("average p for dj pair "+avg_pdj/(double)disjoint_pairs);
		//System.out.println("Very dj_pairs (<"+very_t+"): "+very_dj_pairs+" fraction total "+very_dj_pairs/total_pairs);
		
	}
	
	public static Map<String, Double> loadAntiAssoc(){
		double p = .05;
		String file = "C:\\Users\\bgood\\data\\genewiki\\goa_human\\P_"+p;
		file = file.replace(".", "_")+"_go.txt";
		Map<String, Double> anti_pair = new HashMap<String, Double>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(file));
			String line = f.readLine();
			//skip header
			line = f.readLine();
			while(line!=null){
					String[] item = line.split("\t");
					if(item!=null&&item.length>1){
						String acc1 = item[0];
						String acc2 = item[3];
						if(acc1.compareTo(acc2) > 0){
							String tmp = acc1;
							acc1 = acc2;
							acc2 = tmp;
						}
						Double pval = Double.parseDouble(item[6]);
						Double p_p = anti_pair.get(acc1+acc2);
						if(p_p == null){
							p_p = new Double(pval);
							anti_pair.put(acc1+acc2, p_p);
						}else{
							System.out.println("More than one pair?? "+acc1+" "+acc2);
							return null;
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
		return anti_pair;
	}
	
	public static void makeAntiAssocDataLegible(double p){
		GOowl gol = new GOowl();
		gol.initFromFile(false);
		String termfile = "C:\\Users\\bgood\\data\\genewiki\\goa_human\\gene_association.goa_human.GO.terms"; //.terms
		
		String infile = "C:\\Users\\bgood\\data\\genewiki\\goa_human\\P_"+p;
		infile = infile.replace(".", "_");
		
		int pairs = 0;
		GOterm[] terms = new GOterm[30211];	
		
		BigFile file;
		try {
			file = new BigFile(termfile);
			int i = 0;
			for (String line : file){
				String go_id = line.substring(0,7);
				String go_uri = GOowl.makeGoUri(go_id);				
				OntClass gotit = gol.go.getOntClass(go_uri);
				if(gotit!=null&&gotit.hasRDFType(OWL.Class)){
					GOterm term = new GOterm(null, null, null, null, true);
					terms[i] = gol.makeGOterm(term, gotit);
					//System.out.println("found "+terms[i]);
				}else{
					terms[i] = new GOterm(null, go_id, null, go_uri, true);
					System.out.println("coudn't find "+terms[i]);
				}
				i++;
			}
			System.out.println("read go terms c = "+i);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		try {
		FileWriter out = new FileWriter(infile+"_go.txt");
		BufferedReader	f = new BufferedReader(new FileReader(infile));
			String line = f.readLine().trim();
			while(line!=null){
				String[] item = line.split("\t");
				if(item[2]!=null){
					double v = Double.parseDouble(item[2]);
					out.write(terms[(Integer.parseInt(item[0]))]+"\t"+terms[Integer.parseInt(item[1])]+"\t"+v+"\n");
				}else{
					System.out.println("null value for P");
				}
				line = f.readLine();
			}
			f.close();
		out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void getLowPpairs(double p){
		String pval_matrix = "C:\\Users\\bgood\\data\\genewiki\\goa_human\\gene_association.goa_human.GO.pvals";

		String outfile = "C:\\Users\\bgood\\data\\genewiki\\goa_human\\P_"+p;
		outfile = outfile.replace(".", "_");
		//		List<String> go_terms = new ArrayList<String>(50000);
		//		Map<String, Double> hits = new HashMap<String, Double>();
		BigFile file;
		try {
			file = new BigFile(pval_matrix);
			FileWriter writer = new FileWriter(outfile);

			int j = -1;//row number, file has header row with go term count
			for (String line : file){
				String[] row = line.split(" ");
				//				go_terms.add(row[0]);
				int y = -1; //col number (skip first col)
				for(String value : row){
					if(y>-1&&value!=null&&!value.equals("")&&!value.equals("1")){
						float val = Float.parseFloat(value);
						if(val < p){
							//	String k = row[0]+"\t"+go_terms.get(y);						
							//	writer.write(k+"\t"+val+"\n");
							writer.write(j+"\t"+y+"\t"+val+"\n");
						}
					}
					y++;
				}

				j++;
			}
			writer.close();
			//			System.out.println(go_terms.get(0)+" "+go_terms.get(1)+" "+go_terms.get(2)+" "+go_terms.get(3)+" "+go_terms.get(4));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
