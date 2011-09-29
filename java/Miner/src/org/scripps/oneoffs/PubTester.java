package org.scripps.oneoffs;

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

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.genewiki.annotationmining.Config;
import org.genewiki.annotationmining.annotations.CandidateAnnotation;
import org.genewiki.annotationmining.annotations.CandidateAnnotations;
import org.scripps.datasources.ncbi.PubMed;
import org.scripps.util.BioInfoUtil;


public class PubTester {

	public static void main(String[] args) {
		//		CandidateAnnotations cannolist = new CandidateAnnotations();
		//		cannolist.loadCandidateGOAnnotations(Config.merged_mined_annos);
		//		List<CandidateAnnotation> testcannos = cannolist.getCannos();		
		//		System.out.println("Loaded candidate annotations "+testcannos.size());
		//getCandigoPubYears(testcannos);

		//dataForPlot();
		//getPubYearsForHumanGOA();
		//vennThem();
		
		getPmidsForHumanGOA();
	}

	public static void vennThem(){
		String in = "/users/bgood/data/genewiki/intermediate/goa_pmid_year.txt";
		Map<String, String> goayears = loadPmidYearMap(in);
		String in2 = "/users/bgood/data/genewiki/intermediate/gw_pmid_year.txt";
		Map<String, String> gwyears = loadPmidYearMap(in2);
		int goa = goayears.size(); int gw = gwyears.size(); int inter = 0;
		for(String pmid : goayears.keySet()){
			if(gwyears.containsKey(pmid)){
				inter++;
			}
		}
		System.out.println("GO: "+goa+"\tGW:"+gw+"\tIntersect:"+inter);
	}

	public static Set<String> getPmidsForHumanGOA(){
		String in = Config.panther_goa+"gene_association.goa_human";
		List<String> pmids = new ArrayList<String>();
//		Set<String> pmids = new HashSet<String>();
		BufferedReader f;
		int c = 0;
		try {
			f = new BufferedReader(new FileReader(in));
			String line = f.readLine();
			while(line!=null){
				if(line.startsWith("!")){
					line = f.readLine();
					continue;
				}
				String[] item = line.split("\t");
				String pmid = item[5];
				if(pmid.startsWith("PMID")){
					pmid = pmid.substring(5);
					pmids.add(pmid);
					c++;
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
		System.out.println("human pmids "+pmids.size());
		return null;
	}


		public static void getPubYearsForHumanGOA(){
			String in = Config.panther_goa+"gene_association.goa_human";
			Map<String, String> years = new HashMap<String, String>();
			BufferedReader f;
			int c = 0; int n = 0;
			String pmids = "";
			try {
				f = new BufferedReader(new FileReader(in));
				String line = f.readLine();
				while(line!=null){
					if(line.startsWith("!")){
						line = f.readLine();
						continue;
					}
					String[] item = line.split("\t");
					String pmid = item[5].substring(5);
					if(!years.containsKey(pmid)&&item[5].startsWith("PMID")){
						pmids+=pmid+",";
						c++;
					}
					if(c%100==0&&pmids!=null&&pmids.length()>4){
						Map<String, String> m = PubMed.getPubmedDates(pmids);
						years.putAll(m);
						pmids = "";
						try {
							FileWriter out = new FileWriter("/users/bgood/data/genewiki/intermediate/goa_pmid_year.txt", true);
							for(Entry<String, String> year : m.entrySet()){
								out.write(year.getKey()+"\t"+year.getValue()+"\n");
							}
							out.close();						
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					n++;
					if(n%100==0){
						System.out.print(n+"\t");
						if(n%1000==0){
							System.out.println();
						}
					}

					line = f.readLine();
				}
				//get leftovers
				Map<String, String> m = PubMed.getPubmedDates(pmids);
				try {
					FileWriter out = new FileWriter("/users/bgood/data/genewiki/intermediate/goa_pmid_year.txt", true);
					for(Entry<String, String> year : m.entrySet()){
						out.write(year.getKey()+"\t"+year.getValue()+"\n");
					}
					out.close();						
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				years.putAll(m);
				f.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			System.out.println("Loaded "+years.size()+" pmid years");

		}

		public static void dataForPlot(){
			String in = "/users/bgood/data/genewiki/intermediate/pmid_year.txt";
			Map<String, String> allyears = loadPmidYearMap(in);
			DescriptiveStatistics ds = new DescriptiveStatistics();
			for(String i : allyears.values()){
				ds.addValue(Integer.parseInt(i));
			}
			System.out.println(ds.toString());
			BioInfoUtil.writeStatsForR(ds, "/users/bgood/data/genewiki/intermediate/gw-pubyears.txt", "PubDate");
		}

		public static Map<String, String> loadPmidYearMap(String in){
			Map<String, String> years = new HashMap<String, String>();
			BufferedReader f;
			try {
				f = new BufferedReader(new FileReader(in));
				String line = f.readLine();
				while(line!=null){
					String[] item = line.split("\t");
					String pmid = item[0];
					String year = item[1].substring(0,4);
					years.put(pmid, year);
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
			System.out.println("Loaded "+years.size()+" pmid years");
			return years;
		}

		/**
		 * Gets and stores off a pmid_year-published map based on a list of candidate annotations
		 * @param cannos
		 */
		public static void getCandigoPubYears(List<CandidateAnnotation> cannos){
			String in = "/users/bgood/data/genewiki/intermediate/pmid_year.txt";
			Map<String, String> allyears = loadPmidYearMap(in);
			int c = 0; 
			Set<String> pmids = new HashSet<String>();
			for(CandidateAnnotation canno : cannos){
				if(canno.getCsvrefs()!=null&&canno.getCsvrefs().length()>3){
					String[] refs = canno.getCsvrefs().split(",");
					for(String ref : refs){
						ref = ref.trim();
						if(!(allyears.containsKey(ref)||ref.contains("tp")||ref.contains("NCBI")||ref.length()>12||ref.length()<3)){	
							pmids.add(ref);
						}
					}
				}
			}
			System.out.println("got "+pmids.size()+" pubs");
			String pmi = "";
			c = 0;
			for(String p : pmids){
				pmi += p+",";
				if(c%100==0&&pmi!=null&&pmi.length()>3){
					Map<String, String> m = PubMed.getPubmedDates(pmi);
					try {
						FileWriter out = new FileWriter("/users/bgood/data/genewiki/intermediate/pmid_year.txt", true);
						for(Entry<String, String> year : m.entrySet()){
							out.write(year.getKey()+"\t"+year.getValue()+"\n");
						}
						out.close();						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					pmi = "";
				}
				c++;
			}
			Map<String, String> m = PubMed.getPubmedDates(pmi);
			try {
				FileWriter out = new FileWriter("/users/bgood/data/genewiki/intermediate/pmid_year.txt", true);
				for(Entry<String, String> year : m.entrySet()){
					out.write(year.getKey()+"\t"+year.getValue()+"\n");
				}
				out.close();						
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Total pmids = "+c);
		}

		//	/**
		//	 * Gets and stores off a pmid_year-published map based on a list of candidate annotations
		//	 * @param cannos
		//	 */
		//	public static void getPubYears(List<CandidateAnnotation> cannos){
		//		String in = "/users/bgood/data/genewiki/intermediate/pmid_year.txt";
		//		Map<String, String> allyears = loadPmidYearMap(in);
		//		int c = 0; int n = 0;
		//		String pmids = "";
		//		for(CandidateAnnotation canno : cannos){
		//			Map<String, String> years = pubYears(canno, allyears);
		//			if(years!=null){
		//				for(Entry<String, String> year : years.entrySet()){
		//					if(!allyears.containsKey(year.getKey())){
		//						allyears.put(year.getKey(), year.getValue());
		//						c++;
		//				 		try {
		//							FileWriter out = new FileWriter("/users/bgood/data/genewiki/intermediate/pmid_year.txt", true);
		//							out.write(year.getKey()+"\t"+year.getValue()+"\n");
		//							out.close();						
		//						} catch (IOException e) {
		//							// TODO Auto-generated catch block
		//							e.printStackTrace();
		//						}
		//					}
		//				}
		//			}
		//			n++;
		//			if(n%100==0){
		//				System.out.print(n+"\t");
		//				if(n%1000==0){
		//					System.out.println();
		//				}
		//			}
		//		}
		//		System.out.println("Total pmids = "+c);
		//	}

		/**
		 * Queries NCBI e-utils for data about publications linked to candidate annotations, returns a map from pmid to year it was published
		 * @param canno
		 * @param allyears
		 * @return
		 */
		public static Map<String, String> pubYears(CandidateAnnotation canno, Map<String, String> allyears){
			Map<String, String> years = new HashMap<String, String>();
			if(canno.getCsvrefs()==null||canno.getCsvrefs().length()<3){
				return null;
			}
			String[] refs = canno.getCsvrefs().split(",");
			for(String ref : refs){
				ref = ref.trim();
				if(allyears.containsKey(ref)||ref.contains("tp")||ref.contains("NCBI")||ref.length()>12||ref.length()<3){
					continue;
				}
				String y = PubMed.getPubmedDate(ref);
				if(y!=null&&y.length()>=4){
					String d = y.substring(0,4);
					if(d!=null){
						years.put(ref, d);
					}
				}
			}
			return years;
		}

	}
