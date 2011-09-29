package org.scripps.ontologies.dont;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

import org.scripps.ontologies.go.GOterm;
import org.scripps.util.BioInfoUtil;
import org.scripps.util.Gene;
import org.scripps.util.MyGeneInfo;

public class DO_OMIM {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	//	getAutoImmuneOmims();

	}
//
//	public static Set<String> getAutoImmuneOmims(){
//		Map<String, Set<String>> do_omims = loadDO_OMIM_map();
//		Set<String> autos = new HashSet<String>();
//		DOowl doid = new DOowl();
//		doid.initFromFileRDFS();
//	//	doid.initFromFile();
//		Set<String> terms = doid.getSubs("417"); // 417
//		for(String t : terms){
//			// DOID:419	scleroderma
//			String do_id = t.split("\t")[0];
//			Set<String> omims = do_omims.get(do_id);
//	//		System.out.println(t);
//			if(omims!=null){
//				System.out.println(t+"\t"+omims);
//				for(String om : omims){
//					if(!om.startsWith("MTH")){
//						autos.add(om);
//					}
//				}
//			}
//		}
//		return autos;
//	}
	
	public static void dumpAsSif(){
		Map<String, Set<String>> omim_genes = loadMorbidMap();
		try {
			FileWriter f = new FileWriter("/Users/bgood/data/ontologies/DO/omim_gene.sif");
			FileWriter f2 = new FileWriter("/Users/bgood/data/ontologies/DO/omim_genes.txt");
			Set<String> dgenes = new HashSet<String>();
			for(Entry<String, Set<String>> entry : omim_genes.entrySet()){
				String omim = entry.getKey();
				for(String gene : entry.getValue()){
					f.write(omim+"\tint\t"+gene.trim()+"\n");
					if(dgenes.add(gene.trim())){
						f2.write(gene.trim()+"\n");
					}
				}
			}
			f.close();
			f2.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public static Map<String, Set<String>> readDoOmimGene(){
		Map<String, Set<String>> gene_do = new HashMap<String, Set<String>>();
		try {
			BufferedReader f = new BufferedReader(new FileReader("/Users/bgood/data/ontologies/DO/jan-6-2011/DO_OMIM_GENE.txt"));
			String line = f.readLine();//skip header
			line = f.readLine().trim();
			while(line!=null){
				String[] item = line.split("\t");
				if(item!=null&&item.length>1){
					String doid = item[0]; 
					String geneid = item[1]; 
					Set<String> doids = gene_do.get(geneid);
					if(doids == null){
						doids = new HashSet<String>();
					}
					doids.add(doid);
					gene_do.put(geneid, doids);
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
		return gene_do;
	}
	
	public static void buildDO2OMIM2GENE(){
		Map<String, Set<String>> do_omims = loadDO_OMIM_map(); 
		Map<String, Set<String>> omim_genes = loadMorbidMap();
		omim_genes = convertMorbidMapToGene(omim_genes);
		int n = 0; int n_omim = 0; int doids = 0; int n_mapped2genes = 0;
		for(Entry<String, Set<String>> do_omim : do_omims.entrySet()){
			String doid = do_omim.getKey();
			doids++;
			for(String omim : do_omim.getValue()){
				n_omim++;
				Set<String> genes = omim_genes.get(omim);
				if(genes!=null){
					n++;
					for(String gene : genes){
						n_mapped2genes++;
						System.out.println(n_mapped2genes+"\t"+doid+"\t"+gene+"\t");
					}
				}
			}
		}
		System.out.println("doids "+doids);
	}
	
	/**
	 * Loads data from a mapping file created by Gilbert Feng at NWU
	 * Could also get these mappings directly from the ontology 
	 * @return
	 */
	public static Map<String, Set<String>> loadDO_OMIM_map(){
		String file = "/Users/bgood/data/ontologies/DO/jan-6-2011/DOMAPOMIM1969.txt";
		Map<String, Set<String>> do_omim = new HashMap<String, Set<String>>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(file));
			String line = f.readLine();//skip header
			line = f.readLine().trim();
			while(line!=null){
				String[] item = line.split("\t");
				if(item!=null&&item.length>1){
					String term = item[0]; 
					String doid = item[1]; 
					String omim_id = item[2];
					Set<String> omims = do_omim.get(doid);
					if(omims == null){
						omims = new HashSet<String>();
					}
					omims.add(omim_id);
					do_omim.put(doid, omims);
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
		return do_omim;
	}


	/**
	 * Loads data from morbidmap file retrieved from OMIM database
	 * @return
	 */
	public static Map<String, Set<String>> loadMorbidMap(){
		String file = "/Users/bgood/data/ontologies/DO/OMIM-jan-6-2011/morbidmap";
		Map<String, Set<String>> omim_genes = new HashMap<String, Set<String>>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(file));
			String line = f.readLine();//skip header
			line = f.readLine().trim();
			while(line!=null){
				String[] item = line.split("\\|");
				if(item!=null&&item.length>1){
					String term = item[0]; 
					//dig other identifier (multi-record id)
					String omim_id1 = getOmimId(term);
					String gene_block = item[1]; 
					String omim_id = item[2];

					Set<String> genes = omim_genes.get(omim_id);
					if(genes == null){
						genes = new HashSet<String>();
					}
					String[] gs = gene_block.split(",");
					for(String g : gs){
						genes.add(g);
					}
					omim_genes.put(omim_id, genes);
					omim_genes.put(omim_id1, genes);
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
		System.out.println("loaded from morbidmap "+omim_genes.size());
		return omim_genes;
	}

	public static Map<String, Set<String>> convertMorbidMapToGene(Map<String, Set<String>> mmap){
		Map<String, Set<String>> symbol_gene = new HashMap<String, Set<String>>();
		for(Set<String> symbols : mmap.values()){
			for(String symbol : symbols){
				symbol_gene.put(symbol, new HashSet<String>());
			}
		}
		//build map to ncbi genes from gene symbols
		for(String symbol : symbol_gene.keySet()){
			try {
				Set<String> gids = MyGeneInfo.mapGeneSymbol2NCBIGene(symbol);
				if(gids==null||gids.size()==0){
					gids = MyGeneInfo.mapGeneAlias2NCBIGene(symbol);
				}
				if(gids!=null){
					symbol_gene.put(symbol, gids);
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//apply to omim map
		for(Entry<String, Set<String>> omim_gene : mmap.entrySet()){
			Set<String> symbols = omim_gene.getValue();
			Set<String> geneids = new HashSet<String>();
			for(String symbol : symbols){
				Set<String> mapped_geneids = symbol_gene.get(symbol);
				if(mapped_geneids!=null){
					geneids.addAll(mapped_geneids);
				}
			}
			mmap.put(omim_gene.getKey(), geneids);
		}
		return mmap;
	}

	public static String getOmimId(String in){
		String reg = "[\\d]{6}";
		Pattern pattern = 
			Pattern.compile(reg);
		Matcher matcher = 
			pattern.matcher(in);
		String id = "";
		if (matcher.find()) {
			id = matcher.group().trim();
			return id;
		}
		return null;
	}
}
