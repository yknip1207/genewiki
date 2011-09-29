package org.scripps.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OMIM {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		String of = "/users/bgood/data/bioinfo/mim2gene";
//		Map<String, Set<String>> m = loadOMIM2Gene(of, true);
//		System.out.println(m.size());

//		String gene2uniprot = "/users/bgood/data/bioinfo/mimgenes2uniprot.txt";
		String names = "/users/bgood/data/bioinfo/omim_id_name.txt";
		Map<String, String> m = loadOMIM22DiseaseName(names);
		for(String i : m.keySet()){
			System.out.println(i+"-"+m.get(i));
		}
	}

	/**
	 * reads in a file produced from the omim text file via
	 * egrep '^#|^%' omim.txt > omim_id_name.txt
	 * @param file
	 * @return
	 */
	public static Map<String, String> loadOMIM22DiseaseName(String file){
		Map<String, String> id_name = new HashMap<String, String>();
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(file));
			String line = f.readLine();
			line = f.readLine();
			while(line!=null){
				String id = line.substring(1,7);
				String name = line.substring(8);
				id_name.put(id, name);
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
		return id_name;
	}
	
	public static Map<String, Set<String>> loadOMIM2Uniprot(String gene2uniprot, String omim2gene, boolean onlydiseases, boolean onlyreviewed){
		String uniaccfile = "/Users/bgood/data/bioinfo/reviewed_human_accs.txt.list";
		Set<String> reviewed_human = Uniprot.loadReviewedhuman(uniaccfile);

		Map<String, Set<String>> gene_uniprot = new HashMap<String, Set<String>>();
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(gene2uniprot));
			String line = f.readLine();
			line = f.readLine();
			while(line!=null){
				String[] item = line.split("\t");
				if(item!=null&&item.length>0){
					String gene = item[0];
					String uniprot = item[1];
					if((onlyreviewed&&reviewed_human.contains(uniprot))||!onlyreviewed){
						Set<String> unis = gene_uniprot.get(gene);
						if(unis==null){
							unis = new HashSet<String>();
						}
						unis.add(uniprot);
						gene_uniprot.put(gene, unis);
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
		Map<String, Set<String>> m = loadOMIM2Gene(omim2gene, onlydiseases);
		Map<String, Set<String>> omim_uniprot = new HashMap<String, Set<String>>();
		for(String omim : m.keySet()){
			Set<String> genes = m.get(omim);
			if(genes!=null){
				Set<String> uniaccs = new HashSet<String>(); 
				for(String gene : genes){
					Set<String> uniprots = gene_uniprot.get(gene);
					if(uniprots!=null){
						uniaccs.addAll(uniprots);
					}
				}
				if(uniaccs.size()>0){
					omim_uniprot.put(omim, uniaccs);
				}
			}
		}
		return omim_uniprot;
	}

	public static Map<String, Set<String>> loadGene2OMIM(String file, boolean onlydiseases){
		Map<String, Set<String>> human = new HashMap<String, Set<String>>();
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(file));
			String line = f.readLine();
			line = f.readLine();
			while(line!=null){
				String[] item = line.split("\t");
				if(item!=null&&item.length>2){
					String omim = item[0];
					String gene = item[1];
					String type = item[2];
					if(onlydiseases&&type.equals("phenotype")){
						Set<String> omims = human.get(gene);
						if(omims==null){
							omims = new HashSet<String>();
						}
						omims.add(omim);
						human.put(gene, omims);
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

	public static Map<String, Set<String>> loadOMIM2Gene(String file, boolean onlydiseases){
		Map<String, Set<String>> human = new HashMap<String, Set<String>>();
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(file));
			String line = f.readLine();
			line = f.readLine();
			while(line!=null){
				String[] item = line.split("\t");
				if(item!=null&&item.length>2){
					String omim = item[0];
					String gene = item[1];
					String type = item[2];
					if(onlydiseases&&type.equals("phenotype")){
						Set<String> genes = human.get(omim);
						if(genes==null){
							genes = new HashSet<String>();
						}
						genes.add(gene);
						human.put(omim, genes);
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


}
