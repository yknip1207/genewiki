package org.gnf.dont;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gnf.genewiki.Config;
import org.gnf.go.GOterm;

public class DOmapping {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	}
	
	
	public static Map<String, Set<DOterm>> merge(){
		// TODO Auto-generated method stub
		Map<String, Set<DOterm>> rif = loadGeneRifs2DO();
		Map<String, Set<DOterm>> omim = loadOmim2DO();
		int match = 0; int total = 0;
		for(Entry<String, Set<DOterm>> e : rif.entrySet()){
			String genid = e.getKey();
			Set<DOterm> rif_dos = e.getValue();
			Set<DOterm> omim_dos = omim.get(genid);
			if(omim_dos!=null){
				rif_dos.addAll(omim_dos);
				rif.put(genid, rif_dos);
			}
		}
		return rif;
	}
	
	public static void compare(){		
		Map<String, Set<DOterm>> rif = loadGeneRifs2DO();
		Map<String, Set<DOterm>> omim = loadOmim2DO();
		int match = 0; int total = 0;
		for(Entry<String, Set<DOterm>> e : rif.entrySet()){
			String genid = e.getKey();
			Set<DOterm> rif_dos = e.getValue();
			Set<DOterm> omim_dos = omim.get(genid);
			for(DOterm dot : rif_dos){
				total++;
				if(omim_dos!=null){
					for(DOterm omim_dot : omim_dos){
						if(omim_dot.getAccession().equals(dot.getAccession())){
							match++;
							break;
						}
					}
				}
			}
		}
		System.out.println("total generifs "+total+" - n matching omims "+match);
	}

	public static Map<String, Set<DOterm>> loadGeneRifs2DO() {
		Map<String, Set<DOterm>> id_term = new HashMap<String, Set<DOterm>>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(Config.generif2do));
			String line = f.readLine().trim();
			while(line!=null){
				String[] item = line.split("\t");
				if(item!=null&&item.length>1){
					String geneid = item[0];
					String rif = item[1];
					String pmid = item[2];
					String umlsid = item[3];
					String dioid = item[4];
					String snippet = item[5];
					String index = item[6];

					DOterm dot = new DOterm();
					dot.setAccession(dioid);
					dot.setEvidence("DOA");

					Set<DOterm> dts = id_term.get(geneid);
					if(dts==null){
						dts = new HashSet<DOterm>();
					}
					dts.add(dot);
					id_term.put(geneid, dts);
				}
				line = f.readLine();
			}
			f.close();
			System.out.println("loaded doa mappings");
		}catch(IOException e){
			e.printStackTrace();
		}
		return id_term;
	}


	public static Map<String, Set<DOterm>> loadOmim2DO() {
		Map<String, Set<String>> gene_dos = DO_OMIM.readDoOmimGene();
		Map<String, Set<DOterm>> id_term = new HashMap<String, Set<DOterm>>();

		for(Entry<String, Set<String>> gene_do : gene_dos.entrySet()){
			String geneid = gene_do.getKey();
			for(String doid : gene_do.getValue()){
				DOterm dot = new DOterm();
				dot.setAccession(doid);
				dot.setEvidence("OMIM");
				Set<DOterm> dts = id_term.get(geneid);
				if(dts==null){
					dts = new HashSet<DOterm>();
				}
				dts.add(dot);
				id_term.put(geneid, dts);
			}
		}
		return id_term;
	}
}
