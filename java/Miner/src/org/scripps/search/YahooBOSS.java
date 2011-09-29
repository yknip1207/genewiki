package org.scripps.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.genewiki.annotationmining.Config;
import org.genewiki.annotationmining.annotations.CandidateAnnotation;
import org.genewiki.annotationmining.annotations.CandidateAnnotations;
import org.scripps.ontologies.go.GOmapper;

import com.jellymold.boss.WebSearch;
import com.jellymold.boss.WebSearchResult;

public class YahooBOSS {

	//benjamgo@yahoo.com d09..
	static String appid = "lF3zNvPV34EiapOCzdJWoBjM1EdQfPO2jcA5FNpD7RXx6prNpcgIlv2lMyNWecw-";
	static String urlroot = "http://boss.yahooapis.com/ysearch/web/v1/";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		collectYahooCoOccurrenceData(Config.merged_mined_annos);
//OR10G8		
//		coOccurrence("\"oculocerebrorenal syndrome\"","OCRL", true);
//		coOccurrence("\"OR10G8\"","\"transduction\"", true);
//		coOccurrence("\"Morn repeat containing 1\"","\"asexual reproduction\"", true);
		
		coOccurrence("apple","banana", true);
	/*	coOccurrence("dog","cat", true);
		coOccurrence("horse","rider", true);
		coOccurrence("elbow","arm", true);
		coOccurrence("offense","defense", true);
		coOccurrence("nuclear","hamster", true);
		coOccurrence("apoptosis","barack obama", true);
		coOccurrence("cell","nucleus", true);
	*/	
	}

	/**
	 * Assuming all of the Yahoo search data has been collected, this adds the data to the list of candidate annotations
	 * @param cannos
	 * @return
	 */
	public static List<CandidateAnnotation> setIntersects(List<CandidateAnnotation> cannos){

		String gfile = Config.yahoo_gene_hits;
		Map<String, Double> gene_hits = loadYahooData(gfile);
		String ofile = Config.yahoo_target_hits;
		Map<String, Double> go_hits = loadYahooData(ofile);
		String tfile = Config.yahoo_intersect_hits;
		Map<String, Double> genego_hits = loadYahooData(tfile);

		for(CandidateAnnotation canno : cannos){
			String gene = "\""+canno.getSource_wiki_page_title()+"\"";
			String target = "\""+canno.getTarget_preferred_term().replace("_", " ")+"\"";
			String gene_target = gene+" "+target;
			if(gene_hits.get(gene)!=null
					&&genego_hits.get(gene_target)!=null
					&&go_hits.get(target)!=null){
				SearchEngineIntersect sect = new SearchEngineIntersect(
						gene, target, gene_hits.get(gene), go_hits.get(target), genego_hits.get(gene_target));
				canno.getEvidence().setSect(sect);
			}else{
				canno.getEvidence().setSect(null);
			}
		}
		return cannos;
	}

	/**
	 * Tries to measure the Yahoo-co-occurrence for all of the candidate annotations
	 */
	public static void collectYahooCoOccurrenceData(String anno_file){
		CandidateAnnotations cannolist = new CandidateAnnotations();
		//do them all!
		cannolist.loadCandidateAnnotations(anno_file);
		List<CandidateAnnotation> cannos = cannolist.getCannos();
		Set<String> genename = new HashSet<String>();
		Set<String> targetname = new HashSet<String>();
		Set<String> tuple = new HashSet<String>();

		for(CandidateAnnotation canno : cannos){
			String gene = "\""+canno.getSource_wiki_page_title()+"\"";
			genename.add(gene);
			String target = "\""+canno.getTarget_preferred_term().replace("_", " ")+"\"";
			targetname.add(target);
			tuple.add(gene+" "+target);
		}
		System.out.println("About to grab gene counts");
		String gfile = Config.yahoo_gene_hits;
		Set<String> gotgenes = loadYahoo(gfile);
		if(gotgenes!=null){
			genename.removeAll(gotgenes);
		}
		getAndSaveYahooList(genename, gfile);

		System.out.println("About to grab gotarget counts");
		String ofile = Config.yahoo_target_hits;
		Set<String> gotgoss = loadYahoo(ofile);
		if(gotgoss!=null){
			targetname.removeAll(gotgoss);
		}
		getAndSaveYahooList(targetname, ofile);

		System.out.println("About to grab tuple counts");
		String tfile = Config.yahoo_intersect_hits;
		Set<String> gottuples = loadYahoo(tfile);
		if(gottuples!=null){
			tuple.removeAll(gottuples);
		}
		getAndSaveYahooList(tuple, tfile);


	}

	public static void getAndSaveYahooList(Set<String> inputs, String file){
		WebSearch ws = new WebSearch();
		int c = 0;
		try {			
			for(String g : inputs){
				FileWriter writer = new FileWriter(file, true);
				try{
					ws.search(g);
					double n = ws.getTotalResults();
					writer.write(g+"\t"+n+"\n");
				}catch(Exception e){
					System.out.println("problem searching for "+g);
					e.printStackTrace();
				}
				c++;
				if(c%100==0){
					System.out.print(" "+c+" "+g);
				}if(c%1000==0){
					System.out.println();
				}
				writer.close();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Set<String> loadYahoo(String file){
		File test = new File(file);
		if(!test.exists()){
			return new HashSet<String>();
		}
		
		Set<String> gotit = new HashSet<String>();
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(file));
			String line = f.readLine();
			while(line!=null){
				String[] item = line.split("\t");
				gotit.add(item[0]);
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
		return gotit;
	}

	public static Map<String, Double> loadYahooData(String file){
		Map<String, Double>  gotit = new HashMap<String, Double>(); 
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(file));
			String line = f.readLine();
			while(line!=null){
				String[] item = line.split("\t");
				gotit.put(item[0], Double.parseDouble(item[1]));
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
		return gotit;
	}

	public static double coOccurrence(String one, String two, boolean logit){
		WebSearch ws = new WebSearch();
		ws.search(one);
		double n_one = ws.getTotalResults();
		ws.search(two);
		double n_two = ws.getTotalResults();
		ws.search(one+" "+two);
		double both = ws.getTotalResults();

		double normalizedYahooRank = 
			1 - ((Math.max(Math.log(n_one), Math.log(n_two)) - Math.log(both))/
			(24.3315042 - Math.min(Math.log(n_one), Math.log(n_two))));
		
		if(logit){
			System.out.println("\t"+one+":\t"+n_one+":\t"+two+":\t"+n_two+":\tboth\t"+both+"\t"+2*both/(n_one+n_two)+"\t"+normalizedYahooRank);
		}

		
		return 2*both/(n_one+n_two);
	}

}
