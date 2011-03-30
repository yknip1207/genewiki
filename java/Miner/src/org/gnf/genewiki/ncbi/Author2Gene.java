package org.gnf.genewiki.ncbi;
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

import org.gnf.genewiki.GeneWikiUtils;
import org.gnf.go.GObayes;
import org.gnf.go.GOmapper;
import org.gnf.go.GOowl;
import org.gnf.go.GOterm;

import com.hp.hpl.jena.ontology.OntClass;

import edu.emory.mathcs.backport.java.util.Collections;

import gov.nih.nlm.ncbi.www.soap.eutils.EFetchSequenceServiceStub;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub.LinkSetDbType;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub.LinkSetType;


public class Author2Gene {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) {
		//getGuineas();
		getgameforGuineas();
//		String o = getGeneInfo("1182,356,");
//		System.out.println(o);
	}

	public static void getgameforGuineas(){
		Map<String, Integer> genego_count = new HashMap<String, Integer>();
		GObayes gob = new GObayes(false);
		Map<String, List<String>> genego_guinea = new HashMap<String, List<String>>();
		Map<String, Set<String>> go_gene = new HashMap<String, Set<String>>();
		Map<String, Set<String>> go_guinea = new HashMap<String, Set<String>>();
		Set<String> genegoguinea = new HashSet<String>();
		String g = "C:/Users/bgood/data/guineas.txt";
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(g));

			String line = f.readLine().trim();
			while(line!=null){
				String[] item = line.split("\t");
				if(item!=null&&item.length>5){
					String guinea = item[0];
					String geneid = item[1];
					String genesymbol = item[2];
					String genedescription = item[3];
					String goterms = item[5];
					String[] gos = goterms.split(",");
					for(String go : gos){
						//go to genes
						Set<String> ngenes = go_gene.get(go);
						if(ngenes==null){
							ngenes = new HashSet<String>();
						}
						ngenes.add(genesymbol);
						go_gene.put(go, ngenes);

						//go to guineas
						Set<String> goguineas = go_guinea.get(go);
						if(goguineas==null){
							goguineas = new HashSet<String>();
						}
						goguineas.add(guinea);
						go_guinea.put(go, goguineas);

						//distinct genegoguinea
						if(genegoguinea.add(geneid+go+guinea)){
							Integer c = genego_count.get(genesymbol+" : "+go);
							if(c==null){
								c = 0;
							}
							c++;
							genego_count.put(genesymbol+" : "+go, c);
							List<String> guineas = genego_guinea.get(genesymbol+" : "+go);
							if(guineas==null){
								guineas = new ArrayList<String>();
							}
							guineas.add(guinea);
							genego_guinea.put(genesymbol+" : "+go, guineas);
						}
					}
					//String goaccs = item[5];
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
		//	for(Entry<String, Integer> gg : genego_count.entrySet()){
		//		System.out.println(gg.getKey()+"\t"+gg.getValue()+"\t"+genego_guinea.get(gg.getKey()));
		//	}
		//	System.out.println();

		GOowl gowl = new GOowl();
		gowl.initFromFile(false);
		
		
		for(String go : go_guinea.keySet()){
			if(go!=null&&go.length()>5){
				String accnum = go.substring(go.indexOf("0"));
				GOterm got = gob.getGObyAccN(accnum, "");
				int ngenes = 0;
				String genes = "";
				if(gob.getGo_genes().get(got)!=null){
					ngenes = gob.getGo_genes().get(got).size();
					for(String gene : gob.getGo_genes().get(got)){
						genes+=gene+",";
					}
				}
				String uri = gowl.makeGoUri(accnum);
				OntClass gterm = gowl.go.getOntClass(uri);
				if(ngenes>7){
					genes = "more than 7";
				}else{
					genes = getGeneInfo(genes);
				}
				System.out.println(ngenes+"\t"+got.getAccession()+"\t"+gterm.getLabel("en")+
						"\t"+go_guinea.get(go).size()+"\t"+go_guinea.get(go)+"\t"+go_gene.get(go)+"\t"+genes);
			}
		}
	}

	public static void getGuineas(){
		String[] queries = {"Orth AP", "Andrew T Miller", "Ann E Herman"
				, "Ben Wen", "Bishnu Nayak", "Christian Schmedt", "Daniel Beisner", 
				"Deborah G. Nguyen", "John M Joslin", "Jonathan Deane", "Michael P Cooke", 
				"Boitano AE", "Teresa Ramirez-Montagut"};
		Map<String, List<String>> pub2gene = getPub2gene();
		String gene2gofile = "C:/Users/bgood/data/gene2go";
		HashMap<String, Set<GOterm>> gene2go = GeneWikiUtils.readGene2GO(gene2gofile, "\t", "#");
		try{
			FileWriter w = new FileWriter("C:/Users/bgood/data/guineas.txt");
			for(String query : queries){
				System.out.println(query);
				List<String> pmids = getPmidsByQuery(query, "50");
				Set<String> genes = new HashSet<String>();
				for(String pmid : pmids){
					if(pub2gene.get(pmid)!=null){
						for(String g : pub2gene.get(pmid)){
							if(genes.add(g)){
								String sum = g+"\t"+getGeneInfo(g)+"\t";
								if(gene2go.get(g)!=null){
									for(GOterm go : gene2go.get(g)){
										sum+=go.getTerm()+"| ";
									}
									sum+="\t";
									for(GOterm go : gene2go.get(g)){
										sum+=go.getAccession()+", ";
									}
								}
								w.write(query+"\t"+sum+"\n");
							}
						}
					}
				}
			}
			w.close();
		}catch(IOException e){
			System.out.println(e);
			System.out.println(e.getStackTrace());
			e.printStackTrace();
		}
	}

	public static Map<String, List<String>> getPub2gene(){
		Map<String, List<String>> p2g = new HashMap<String, List<String>>();
		BufferedReader f;
		String inputfile = "C:/Users/bgood/data/gene2pubmed";
		try {
			f = new BufferedReader(new FileReader(inputfile));
			String line = f.readLine();
			while(line!=null){
				if(line.startsWith("#")){
					line = f.readLine();
					continue;
				}
				String[] g2p = line.split("\t");
				List<String> genes = p2g.get(g2p[2]);
				if(genes==null){
					genes = new ArrayList<String>();
				}
				genes.add(g2p[1]);
				p2g.put(g2p[2], genes);
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

	
	

	public static String getGeneInfo(String id){
		String geneinfo = ""; 
		try
		{
			EUtilsServiceStub service = new EUtilsServiceStub();
			// call NCBI ESummary utility
			EUtilsServiceStub.ESummaryRequest req = new EUtilsServiceStub.ESummaryRequest();
			req.setDb("gene");
			req.setId(id);
			EUtilsServiceStub.ESummaryResult res = service.run_eSummary(req);
			// results output
			for(int i=0; i<res.getDocSum().length; i++)
			{
				//       System.out.println("ID: "+res.getDocSum()[i].getId());
				for (int k = 0; k < res.getDocSum()[i].getItem().length; k++)
				{
					/*             System.out.println("    " + res.getDocSum()[i].getItem()[k].getName() +

	                                       ": " + res.getDocSum()[i].getItem()[k].getItemContent());
					 */
					if(res.getDocSum()[i].getItem()[k].getName().equals("Name")){
						geneinfo += res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"
					}
					if(res.getDocSum()[i].getItem()[k].getName().equals("Description")){
						geneinfo += res.getDocSum()[i].getItem()[k].getItemContent();
					}

				}
				geneinfo+=";  ";
			}
			//	            System.out.println("-----------------------\n");
		}

		catch(Exception e) { System.out.println(e.toString()); }
		return geneinfo;
	}


	public static List<String> getPmidsByQuery(String query, String max){
		String[] ids = { "" };
		List<String> pmids = new ArrayList<String>();
		String fetchIds = "";
		// STEP #1: search in PubMed for "Cooke MP"
		String searchfor = query;
		//
		try  {
			EUtilsServiceStub service = new EUtilsServiceStub();
			// call NCBI ESearch utility
			EUtilsServiceStub.ESearchRequest req = new EUtilsServiceStub.ESearchRequest();
			req.setDb("pubmed");
			req.setTerm(searchfor);
			//	            req.setSort("PublicationDate");
			req.setRetMax(max);
			EUtilsServiceStub.ESearchResult res = service.run_eSearch(req);
			// results output
			int N = res.getIdList().getId().length;
			ids[0] = "";

			for (int i = 0; i < N; i++)
			{
				if (i > 0) ids[0] += ",";
				ids[0] += res.getIdList().getId()[i];
				pmids.add(res.getIdList().getId()[i]);
			}
			System.out.println("Search in PubMed for \""+searchfor+"\" returned " + res.getCount() + " hits");
			//			System.out.println("Search links in  for the first "+N+" UIDs: "+ids[0]);
			//			System.out.println();

		}

		catch (Exception e) { System.out.println(e.toString()); }
		return pmids;

		/*
	        // STEP #2: get links in the gene database
	        //
	        try {
	            EUtilsServiceStub service = new EUtilsServiceStub();
	            // call NCBI ELink utility
	            EUtilsServiceStub.ELinkRequest req = new EUtilsServiceStub.ELinkRequest();
	         //   req.setDb("gene");
	            req.setDbfrom("pubmed");
	            req.setId(ids);
	            EUtilsServiceStub.ELinkResult res = service.run_eLink(req);
	            LinkSetType[] lst = res.getLinkSet();
	            LinkSetDbType[] lstdbt = lst[0].getLinkSetDb();
	            for (int i = 0; i < lstdbt[0].getLink().length; i++){
	                if (i > 0) fetchIds += ",";
	                fetchIds += res.getLinkSet()[0].getLinkSetDb()[0].getLink()[i].getId().getString();
	            }
	            System.out.println("ELink returned the following UIDs from gene: " + fetchIds);
	            System.out.println();
	        }
	        catch (Exception e) { 
	        	e.printStackTrace();
	        	System.out.println(e.toString()); 
	        	}

	        // STEP #3: fetch records from nuccore

	        //


		 */

	}

}
