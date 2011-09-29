package org.scripps.datasources;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.genewiki.GeneWikiUtils;
import org.genewiki.annotationmining.Config;
import org.scripps.ontologies.go.GOowl;
import org.scripps.ontologies.go.GOterm;

public class GoGeneCrawler {


	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	public static void getParseAndStoreGoGeneData(){
		HashMap<String, String> gene_wiki = GeneWikiUtils.read2columnMap(Config.article_gene_map_file);
		Set<String> geneids = new HashSet<String>(gene_wiki.values());
		//gets the data from the web server and stores it locally
		getGogeneDataForGeneSet(geneids);
		//parses the retrieved data and stores it for access
		saveGoGeneData();	
	}
	
	
	/**
	 * Load up gogene associations
	 * @return
	 */
	public static Map<String, Set<GOterm>> getGoGeneData(){
		Map<String, Set<GOterm>> map = new HashMap<String, Set<GOterm>>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(Config.gogene_parsed_data));
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
					String term = item[3]; String root = item[2];
					GOterm go = new GOterm(id, acc, root, term, true);
					go.setEvidence("gogene");
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
	 * 
	 * @return
	 */
	public static Map<String, GOterm> getMissingGo(){
		Map<String, GOterm> missing = new HashMap<String, GOterm>();
		Set<String> old = readMissingGOterms(Config.gogene_missinggo);
		GOowl gol = new GOowl();
		gol.initFromFile(false);
		for(String o : old){
			GOterm go = gol.getGOfromLabel(o);
			if(go==null){
				System.out.println("no go for "+o);
			}else{
				missing.put(o, go);
			}
		}
		return missing;
	}

	public static void saveGoGeneData(){
		Map<String, Set<GOterm>> g2g = parseGoGeneData();
		System.out.println(g2g.size());
		try {
			FileWriter f = new FileWriter(Config.gogene_parsed_data);
			for(Entry<String, Set<GOterm>> g : g2g.entrySet()){
				for(GOterm gt : g.getValue()){
					f.write(g.getKey()+"\t"+gt.getAccession()+"\t"+gt.getRoot()+"\t"+gt.getTerm()+"\n");
				}
			}
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Map<String, Set<GOterm>> parseGoGeneData(){
		//63036	go-annotation	GO:extracellular region
		/*
		63036	co-occurrence	MSH:Amebiasis
		63036	text-mined	GO:DNA binding
		 */
		//		Map<String, GOterm> missing = getMissingGo();

		GOowl gol = new GOowl();
		gol.initFromFile(false);
		Map<String, Set<GOterm>> map = new HashMap<String, Set<GOterm>>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(Config.gogene_downloaded_data));
			String line = f.readLine().trim();
			while(line!=null){
				String[] item = line.split("\t");
				if(item!=null&&item.length>1){
					String geneid = item[0];
					String relation = item[1];
					String target = item[2];
					if(target.startsWith("GO")&&(relation.equals("text-mined")||relation.equals("co-occurrence"))){
						target = target.substring(3).trim();


						GOterm gterm = gol.getGOfromLabel(target);
						//					GOterm gterm = missing.get(target);
						if(gterm==null){
							//							System.out.println("No go for "+target);
						}else{
							gterm.setEvidence("GoGene");
							Set<GOterm> GOs = map.get(geneid);
							if(GOs == null){
								GOs = new HashSet<GOterm>();
								map.put(geneid, GOs);
							}
							GOs.add(gterm);					
						}
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

	/**
	 * Use NCBI gene identifiers to get GoGenes data
	 * @param genes
	 */
	public static void getGogeneDataForGeneSet(Set<String> genes){
		String glist = "";
		int count = 0;
		//		int startat = 6400;
		try {
			FileWriter f = new FileWriter(Config.gogene_downloaded_data, true);
			for(String gene : genes){
				count++;
				glist += gene+" ";
				if(count%200==0){
					String data = postGeneIds(glist);
					System.out.println("Done "+count);
					glist = "";
					f.write(data);
				}	
			}
			String data = postGeneIds(glist);
			System.out.println("Done "+count);
			glist = "";
			f.write(data);

			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static String postGeneIds(String genes){
		String out = "";
		// Get file to be posted
		//        String strXMLFilename = args[1];
		//	        File input = new File(strXMLFilename);
		// Prepare HTTP post
		PostMethod post = new PostMethod(Config.gogene_urlroot);
		NameValuePair[] data = {
				new NameValuePair("q", genes),
				new NameValuePair("type", "SIFExportAll")
		};
		// Request content will be retrieved directly
		// from the input stream
		// RequestEntity entity = new FileRequestEntity(input, "text/xml; charset=ISO-8859-1");
		//	        post.setRequestEntity(entity);
		post.setRequestBody(data);
		// Get HTTP client
		HttpClient httpclient = new HttpClient();
		// Execute request
		try {
			int result = httpclient.executeMethod(post);
			// Display status code
			System.out.println("Response status code: " + result);
			// Display response
			System.out.println("Response body: ");
			out = post.getResponseBodyAsString();
			System.out.println(out);
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// Release current connection to the connection pool once you are done
			post.releaseConnection();
		}
		return out;
	}


	public static Set<String> readMissingGOterms(String file){
		Set<String> genes = new HashSet<String>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(file));
			String line = f.readLine();
			while(line!=null){
				genes.add(line.trim());
				line = f.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return genes;
	}

}
