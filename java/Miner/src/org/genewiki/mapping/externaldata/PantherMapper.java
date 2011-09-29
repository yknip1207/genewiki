package org.genewiki.mapping.externaldata;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.genewiki.Config;
import org.scripps.ontologies.go.GOowl;
import org.scripps.ontologies.go.GOterm;

public class PantherMapper {

	public static String panther_human = Config.panther+"PTHR7.0_HUMAN";
	public static String panther_mouse = Config.panther+"PTHR7.0_MOUSE";
	public static String panther_zebrafish = Config.panther+"PTHR7.0_ZEBRAFISH";
	public static String panther_rat = Config.panther+"PTHR7.0_RAT";
	public static String panther_fugu = Config.panther+"PTHR7.0_FUGU";
	public static String panther_worm = Config.panther+"PTHR7.0_WORM";
	public static String panther_fly = Config.panther+"PTHR7.0_FLY";
	public static String panther_yeast = Config.panther+"PTHR7.0_YEAST";

	public static String goa_mouse = Config.panther_goa+"/gene_association.mgi"; 
	public static String goa_rat = Config.panther_goa+"/gene_association.rgd"; 
	public static String goa_fly = Config.panther_goa+"/gene_association.fb"; 
	public static String goa_yeast = Config.panther_goa+"/gene_association.sgd"; 
	public static String goa_worm = Config.panther_goa+"/gene_association.wb";
	public static String goa_zebrafish = Config.panther_goa+"/gene_association.zfin"; 

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//assemblePantherFamilyBasedGene2GOmap();
	}

	
	public static Map<String, Set<GOterm>> getPantherData(){
		Map<String, Set<GOterm>> map = new HashMap<String, Set<GOterm>>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(Config.gene_panther_go_file));
			String line = f.readLine().trim();
			while(line!=null){
				String[] item = line.split("\t");
				if(item!=null&&item.length>1){
					String geneid = item[0];					
					Set<GOterm> GOs = map.get(geneid);
					if(GOs == null){
						GOs = new HashSet<GOterm>();
						map.put(geneid, GOs);
					}
					String id = null; String acc = item[1];
					String evidence = item[2];
					GOterm go = new GOterm(id, acc, "", "", true);
					go.setEvidence(evidence);
					GOs.add(go);//the text term for the go id						
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
	
	
	
	
	/**
	 * Read in all the data from Panther and from the GO and assemble a human-specific file of annotations predicted by orthology
	 */
	public static void assemblePantherFamilyBasedGene2GOmap(){
		//get human genes that we care about (from gwiki) and then get panther families
		Map<String, String> humangene2pan = getHumanGene2PantherFamily();
		String[] orgs = {"fly","worm","yeast","zebrafish","mouse"};//rat doesn't work because of different ids
		Map<String, Set<String>> pan2gene = new HashMap<String, Set<String>>();
		Map<String, Set<GOterm>> gene2go = new HashMap<String, Set<GOterm>>();
		for(String org : orgs){
			//get panther families for ortho genes
			Map<String, Set<String>> pan2geneorgs = getPantherFamily(org);
			for(Entry<String, Set<String>> pan2geneorg : pan2geneorgs.entrySet()){
				if(pan2gene.containsKey(pan2geneorg.getKey())){
					Set<String> genes = pan2gene.get(pan2geneorg.getKey());
					genes.addAll(pan2geneorg.getValue());
					pan2gene.put(pan2geneorg.getKey(), genes);
				}else{
					pan2gene.put(pan2geneorg.getKey(), pan2geneorg.getValue());
				}
			}
			//get GO annotation for each organism
			Map<String, Set<GOterm>> orggene2go = getGOaMap(org);
			System.out.println(org+" "+orggene2go.size());
			for(Entry<String, Set<GOterm>> orggene : orggene2go.entrySet()){
				//TODO something is amiss here.. things are getting overwrriten by mice!
				if(gene2go.containsKey(orggene.getKey())){
					System.out.println("Duplicate gene-GO key! "+orggene.getKey());
				}
				gene2go.put(orggene.getKey(), orggene.getValue());
			}
		}
		//build human gene keyed map
		Map<String, Set<GOterm>> humangene2go =  new HashMap<String, Set<GOterm>>();

		//count orthologue GOs per human gene
		float orthos = 0; float genes = 0; 

		for(Entry<String, String> human : humangene2pan.entrySet()){
			String human_g = human.getKey();
			String panfam = human.getValue();
			Set<String> orth_gs = pan2gene.get(panfam);
			genes++;
			if(orth_gs==null){
				continue;
			}
			orthos++;
			for(String orth_g : orth_gs){
				Set<GOterm> mgos = gene2go.get(orth_g);
				if(mgos!=null){							
					Set<GOterm> hgos = humangene2go.get(human_g);
					if(hgos==null){
						hgos = new HashSet<GOterm>();
					}
					hgos.addAll(mgos);
					//record the specific panther family as evidence
					//this takes a lot longer and a lot more memory - since we can reverse engineer it if its ever really needed I'm leaving it out for now.
//					for(GOterm h : hgos){
//						h.setEvidence(h.getEvidence()+"_"+panfam);
//					}
					humangene2go.put(human_g, hgos);				
				}

			}
		}
	//	System.out.println("ORTHOS: wm "+wm+" z "+zfin+" m "+mgi+" fb "+fb+" sgd "+sgd);
		System.out.println("Human genes with a panther family:"+genes+"\tGenes with a detected orthologs:"+orthos+"\torthos/gene: "+orthos/genes);
		//write out ortho file
		writeOrthoToGo(humangene2go,Config.gene_panther_go_file);
	}

	/**
	 * Reads the file downloaded from Panther (web export), containing rows like:
	 * HUMAN|ENSEMBL=ENSG00000117020|UniProtKB=Q9Y243	10000	RAC-gamma serine/threonine-protein kinase;AKT3	Q9Y243	SUBFAMILY NOT NAMED (PTHR24352:SF69)	kinase activity	gamete generation;negative regulation of apoptosis;mitosis;cell surface receptor linked signal transduction;intracellular signaling cascade;nitric oxide biosynthetic process;protein metabolic process;mitosis;signal transduction		protein kinase	Ras Pathway->AKT;;Hypoxia response via HIF activation->v-akt murine thymoma viral oncogene homolog 1;;FAS signaling pathway->Apoptosis signal regulating kinase 1;;Endothelin signaling pathway->Thymoma viral proto-oncogene;;Huntington disease->Thymoma Viral Proto-Oncogene 1;;Interleukin signaling pathway->Protein kinase B;;T cell activation->Akt;;Insulin/IGF pathway-protein kinase B signaling cascade->Protein kinase B;;p53 pathway->V-akt murine thymoma viral oncogene homolog;;Apoptosis signaling pathway->Protein kinase B;;EGF receptor signaling pathway->Thymoma viral proto-oncogene;;FGF signaling pathway->Thymoma viral proto-oncogene;;Angiogenesis->Protein Kinase B;;p53 pathway by glucose deprivation->Akt;;p53 pathway feedback loops 2->AKT;;VEGF signaling pathway->Thymoma Viral Proto-Oncogene;;Inflammation mediated by chemokine and cytokine signaling pathway->protein kinase B;;PI3 kinase pathway->PKB;;PDGF signaling pathway->PROTEIN KINASE B-BETA;;;;;;;;;;;;;;;;;;;;	Homo sapiens
	 * @return a map from gene id to panther family id
	 */
	public static Map<String, String> getHumanGene2PantherFamily(){
		Map<String, String> gene2pan = new HashMap<String, String>();
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(Config.panther_gwiki_web_export));
			String line = f.readLine();
			while(line!=null){
				String[] item = line.split("\t");
				String geneid = item[1];
				String panfam = item[4]; //e.g. SUBFAMILY NOT NAMED (PTHR24352:SF69)
				if(panfam.indexOf("PTHR")==-1){
					line = f.readLine();
					continue;
				}
				panfam = panfam.substring(panfam.indexOf("(PTHR")+1);
				panfam = panfam.substring(0,panfam.indexOf(':'));

				if(geneid.contains(",")){
					for(String g : geneid.split(",")){ //e.g. 200316,60489
						gene2pan.put(g, panfam);
					}
				}else{
					gene2pan.put(geneid, panfam);
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

		return gene2pan;
	}

	public static void writeOrthoToGo(Map<String, Set<GOterm>> ortho2go, String outfile){			
		FileWriter w;
		try {
			w = new FileWriter(outfile);
			for(Entry<String, Set<GOterm>> entry : ortho2go.entrySet()){
				for(GOterm got : GOterm.compressGOSet(entry.getValue())){
					w.write(entry.getKey()+"\t"+got.getAccession()+"\t"+got.getEvidence()+"\n");
				}
			}
			w.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads a GO annotation file retrieved from http://www.geneontology.org/GO.downloads.annotations.shtml
	 * @param org
	 * @return a map linking gene ids to a set of GO terms
	 */
	public static Map<String, Set<GOterm>> getGOaMap(String org){
		Map<String, Set<GOterm>> gene2go = new HashMap<String, Set<GOterm>>();
		BufferedReader f = null;
		try {
			if(org.equals("mouse")){
				f = new BufferedReader(new FileReader(goa_mouse));
			}else if(org.equals("rat")){
				f = new BufferedReader(new FileReader(goa_rat));	
			}else if(org.equals("fly")){
				f = new BufferedReader(new FileReader(goa_fly));	
			}else if(org.equals("yeast")){
				f = new BufferedReader(new FileReader(goa_yeast));	
			}else if(org.equals("worm")){
				f = new BufferedReader(new FileReader(goa_worm));	
			}else if(org.equals("zebrafish")){
				f = new BufferedReader(new FileReader(goa_zebrafish));	
			}else{
				System.out.println("Don't know about org: "+org);
				return null;
			}
			String line = f.readLine();
			while(line!=null){
				if(line.startsWith("!")){
					line = f.readLine();
					continue;
				}
				String[] item = line.split("\t");
				//get the id from the supplying database
				String geneid = item[1];
				String acc = item[4];
				if(acc.startsWith("GO:")){				
					Set<GOterm> gos = gene2go.get(geneid);
					if(gos==null){
						gos = new HashSet<GOterm>();
					}
					GOterm ngo = new GOterm("",acc,"","", true);
					ngo.setEvidence("Panther:"+org);
					gos.add(ngo);
					gene2go.put(geneid, gos);
					//		System.out.println(geneid+"\t"+acc);
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

		return gene2go;
	}

	/**
	 * Reads a Panther sequence classification file retrieved from 
	 * ftp://ftp.pantherdb.org/sequence_classifications/current_release
	 * @param org
	 * @return map of panther family id to a set of gene ids
	 */
	public static Map<String, Set<String>> getPantherFamily(String org){
		Map<String, Set<String>> pan2gene = new HashMap<String, Set<String>>();
		BufferedReader f = null;
		try {
			if(org.equals("mouse")){
				f = new BufferedReader(new FileReader(panther_mouse));
			}else if(org.equals("rat")){
				f = new BufferedReader(new FileReader(panther_rat));	
			}else if(org.equals("fly")){
				f = new BufferedReader(new FileReader(panther_fly));	
			}else if(org.equals("yeast")){
				f = new BufferedReader(new FileReader(panther_yeast));	
			}else if(org.equals("worm")){
				f = new BufferedReader(new FileReader(panther_worm));	
			}else if(org.equals("zebrafish")){
				f = new BufferedReader(new FileReader(panther_zebrafish));	
			}else{
				System.out.println("Don't know about org: "+org);
				return null;
			}

			String line = f.readLine();
			while(line!=null){
				String[] item = line.split("\t");
				//get the main db id
				String geneid = item[0].split("\\|")[1];
				//deal with conflicts between ids in panther and go downloads
				if(org.equals("worm")||org.equals("fly")){
					geneid = geneid.substring(3);
				}else if(org.equals("mouse")||org.equals("yeast")){
					geneid = geneid.substring(4);
				}else if(org.equals("zebrafish")){
					if(geneid.startsWith("ENSEMBL")){
						line = f.readLine();
						continue;
					}
					//TODO there are also ensembl ids mixed in here, this wil only work on zfin ids
					//working on zfin ids because these are the ones the GO supplies
					geneid = geneid.substring(5);
				}

				String panfam = item[3];
				panfam = panfam.substring(panfam.indexOf("(PTHR")+1);
				panfam = panfam.substring(0,panfam.indexOf(':'));

				Set<String> geneids = pan2gene.get(panfam);
				if(geneids==null){
					geneids = new HashSet<String>();
				}
				geneids.add(geneid);
				pan2gene.put(panfam, geneids);
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

		return pan2gene;
	}




	public static void buildGenePanther2go(String gene2efile, String pantherfile, String outfile){
		Map<String, String> ensembl2gene = new HashMap<String, String>();
		//	Map<String, List<GOterm>> ensembl2go = new HashMap<String, List<GOterm>>();
		Map<String, List<GOterm>> gene2panther2go = new HashMap<String, List<GOterm>>();
		BufferedReader f;
		FileWriter w;
		try {
			//load the gene2ensembl map
			f = new BufferedReader(new FileReader(gene2efile));
			String line = f.readLine();
			while(line!=null){
				String[] item = line.split("\t");
				String geneid = item[1];
				String ensid = item[2];
				ensembl2gene.put(ensid, geneid);
				line = f.readLine();
			}
			f.close();
			//load the ensembl to GO map
			f = new BufferedReader(new FileReader(pantherfile));
			line = f.readLine();
			while(line!=null){
				List<GOterm> goterms = new ArrayList<GOterm>();
				String[] item = line.split("\t");
				String ensid = item[0].split("\\|")[1].substring(8);
				//ion transport#GO:0006811;transport#GO:0006810;cation transport#GO:0006812
				if(item.length>6){
					String mf = item[6];
					if(mf!=null&&!mf.equals("")){
						goterms.addAll(getGOsFromPantherFile(mf, "Function"));
					}
					if(item.length>7){

						String bp = item[7];
						if(bp!=null&&!bp.equals("")){
							goterms.addAll(getGOsFromPantherFile(bp, "Process"));
						}
						if(item.length>8){
							String cc = item[8];
							if(cc!=null&&!cc.equals("")){
								goterms.addAll(getGOsFromPantherFile(cc, "Component"));
							}
						}
					}
				}
				if(goterms!=null&&goterms.size()>0){
					if(ensembl2gene.get(ensid)==null){
						gene2panther2go.put(ensid, goterms);
						System.out.println(ensid);
					}else{
						gene2panther2go.put(ensembl2gene.get(ensid), goterms);
					}
				}
				line = f.readLine();
			}
			f.close();
			//now write it out			
			w = new FileWriter(outfile);
			for(Entry<String, List<GOterm>> entry : gene2panther2go.entrySet()){
				for(GOterm got : entry.getValue()){
					w.write("9606\t"+entry.getKey()+"\t"+got.getAccession()+"\tPanther\t-\t"+got.getTerm()+"\t-\t"+got.getRoot()+"\n");
				}
			}
			w.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void buildGenePanther2goFromWebExport(String pantherfile, String outfile){
		//	Map<String, List<GOterm>> ensembl2go = new HashMap<String, List<GOterm>>();
		Map<String, List<GOterm>> gene2panther2go = new HashMap<String, List<GOterm>>();
		BufferedReader f;
		FileWriter w;
		GOowl gol = new GOowl();
		gol.initFromFile(false);
		try {
			f = new BufferedReader(new FileReader(pantherfile));
			String line = f.readLine();
			while(line!=null){
				List<GOterm> goterms = new ArrayList<GOterm>();
				String[] item = line.split("\t");
				String geneid = item[1];
				//ion transport#GO:0006811;transport#GO:0006810;cation transport#GO:0006812
				if(item[4]!=null){
					goterms.addAll(getGOsFromPantherWebExport(item[4], gol, "Function"));
				}
				if(item[5]!=null){
					goterms.addAll(getGOsFromPantherWebExport(item[5], gol, "Process"));
				}
				if(item[6]!=null){
					goterms.addAll(getGOsFromPantherWebExport(item[6], gol, "Component"));
				}

				if(goterms!=null&&goterms.size()>0){
					gene2panther2go.put(geneid, goterms);
				}
				line = f.readLine();
			}
			f.close();
			//now write it out			
			w = new FileWriter(outfile);
			for(Entry<String, List<GOterm>> entry : gene2panther2go.entrySet()){
				for(GOterm got : entry.getValue()){
					w.write("9606\t"+entry.getKey()+"\t"+got.getAccession()+"\tPanther\t-\t"+got.getTerm()+"\t-\t"+got.getRoot()+"\n");
				}
			}
			w.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static List<GOterm> getGOsFromPantherWebExport(String gofield, GOowl gol, String root){
		List<GOterm> gos = new ArrayList<GOterm>();
		String[] goarray = gofield.split(";");
		for(String go : goarray){
			if((!go.equals(""))&&go.length()>3){
				GOterm got = gol.getGOfromLabel(go);
				if(got!=null){
					got.setEvidence("panther");
					got.setRoot(root);
					gos.add(got);
				}
			}
		}
		return gos;
	}

	public static List<GOterm> getGOsFromPantherFile(String gofield, String root){
		List<GOterm> gos = new ArrayList<GOterm>();
		String[] goarray = gofield.split(";");
		for(String go : goarray){
			String[] gnames = go.split("#");
			GOterm got = new GOterm("", gnames[1], root, gnames[0], true);
			gos.add(got);
		}
		return gos;
	}

}
