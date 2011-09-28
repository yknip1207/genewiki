/**
 * 
 */
package org.gnf.genewiki.metrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.gnf.genewiki.GeneWikiUtils;
import org.gnf.genewiki.journal.Prioritizer;
import org.gnf.genewiki.ncbi.PubMed;
import org.gnf.genewiki.ncbi.PubMed.PubmedSummary;
import org.gnf.go.GOterm;
import org.gnf.search.GoogleSearch;
import org.gnf.util.BigFile;
import org.gnf.util.BioInfoUtil;
import org.gnf.util.Gene;
import org.gnf.util.MapFun;
import org.gnf.util.MyGeneInfo;
/**
 * Keep track of how stats used for Gene Wiki NAR update article were assembled
 * @author bgood
 *
 */
public class NAR2ReportBuilder {

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		//getGenePubGoWordCounts();
		//runGoogleRankAnalysis();
		//buildPublicationGrowthTable();
		//buildRevisionGrowthTrustTable();
		//buildRevisionGrowthTrustTableForNonGeneWikiSample();
		buildSizeViewsEdits();
	}

	/**
	 * build monthly size, views, edits table
	 * @throws ClassNotFoundException 
	 */

	public static void buildSizeViewsEdits(){
		SimpleDateFormat format_exl = new SimpleDateFormat("MM/dd/yyyy");

		MetricsDB db = new MetricsDB();
		try {
			//get page_views by month
			//indexes each cumulative count with the first day of the following month (e.g. sept 2010 would be indexed as oct 1, 2010)
			Map<String, Integer> month_views = db.getSumPageViewsByMonth();
			//get size by month
			Map<String, Integer> month_words = db.getSumWordsByMonth();
			//refs by month
			Map<String, Integer> month_refs = db.getSumReferencesByMonth();
			
			//get edits by month

			//output
			System.out.println("Month\tt0\tt1\tTotal views\tTotal words\tTotal edits\tTotal refs");
			for(Entry<String, Integer> month_view : month_views.entrySet()){
				Date m = format_exl.parse(month_view.getKey());
				Calendar t1 = Calendar.getInstance();
				t1.clear();
				t1.setTime(m);
				t1.set(Calendar.HOUR_OF_DAY, 24);
				t1.set(Calendar.MINUTE, 59);
				t1.set(Calendar.SECOND, 59);
				Calendar t0 = Calendar.getInstance();
				t0.clear();
				t0.setTime(m);
				t0.set(Calendar.DAY_OF_MONTH, 1);
				int revs =  db.getRevCountInDateRange(t0, t1);
				System.out.print(month_view.getKey()+"\t"+format_exl.format(t0.getTime())+"\t"+format_exl.format(t1.getTime())+"\t"+month_view.getValue()+"\t");
				System.out.print(month_words.get(month_view.getKey())+"\t"+revs+"\t"+month_refs.get(month_view.getKey())+"\n");
			}

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/***
	 * This uses data from wikitrust, supplied by luca di alfaro to build a tabel describing the accumulation of 'good' and 'bad' edits to the gene wiki.	
	 */
	public static void buildRevisionGrowthTrustTableForNonGeneWikiSample(){
		String luca_revs = "/Users/bgood/data/NARupdate2011/luca/rev_details_c79f3a9.csv";
		String file = "/Users/bgood/data/NARupdate2011/luca/good_bad_revs_gw_onlybots.txt";
		MetricsDB db = new MetricsDB();
		float total_good = 0; float total_bad = 0; 
		try {
			TreeSet<String> page_ids = db.getPageids();
			BigFile revs = new BigFile(luca_revs);
			int c = 0;
			int page_id_index = 50; int page_title_index = 51;

			Map<Calendar,String> sorted = new TreeMap<Calendar, String>();
			Map<Calendar,Float> qs = new TreeMap<Calendar, Float>();
			SimpleDateFormat format = new SimpleDateFormat("\"yyyyMMddHHmmss\"");
			SimpleDateFormat format_exl = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
			Calendar start = Calendar.getInstance();
			start.clear();
			start.set(2009, Calendar.SEPTEMBER,1);
			Calendar stop = Calendar.getInstance();
			stop.clear();
			stop.set(2011, Calendar.SEPTEMBER,1);
			for (String line : revs){
				c++;
				if(c==1){
					continue;
				}
				int quality_index = 67; int rev_id_index = 69; int time_index = 71;int user_index = 74;
				String[] row = line.split(",");
				String title = row[51];
				if(row.length>75){
					int n = row.length - 75;					
					for(int i = 52;i<52+n;i++){
						title += ","+row[i];
						quality_index++; rev_id_index++; time_index++;user_index++;
					}
				}
				String page_id = row[page_id_index];
				String page_title = title;
				String rev_id = row[rev_id_index];
				String time = row[time_index];

				if(user_index<row.length&&time.length()==16){
					//2010 04 16 09 22 30

					Calendar stamp = Calendar.getInstance();
					stamp.clear();
					stamp.setTime(format.parse(time));
					String user = row[user_index];
					if(stamp.after(start)&&stamp.before(stop)){
						if(user.toLowerCase().contains("bot")){
							float quality = Float.parseFloat(row[quality_index]);					
							if(page_ids.contains(page_id)){		
								qs.put(stamp, quality);
								String r = quality+"\t"+page_id+"\t"+page_title+"\t"+rev_id+"\t"+user+"\t";
								sorted.put(stamp, r);
							}
						}
					}
				}
			}
			FileWriter f = new FileWriter(file);
			f.write("date\ttotal_good\ttotal_bad\tquality\tpage_id\tpage_title\trev_id\teditor\n");
			for(Entry<Calendar,String> q : sorted.entrySet()){
				float quality = qs.get(q.getKey());
				if(quality>=0.4){
					total_good++;
				}else{
					total_bad++;
				}
				f.write(format_exl.format(q.getKey().getTime())+"\t"+total_good+"\t"+total_bad+"\t"+q.getValue()+"\n");
			}
			f.close();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("vandalism rate "+total_bad/(total_good+total_bad));
	}

	/***
	 * This uses data from wikitrust, supplied by luca di alfaro to build a tabel describing the accumulation of 'good' and 'bad' edits to the gene wiki.	
	 */
	public static void buildRevisionGrowthTrustTableForGeneWiki(){
		String luca_revs = "/Users/bgood/data/NARupdate2011/luca/rev_details_c79f3a9.csv";
		String file = "/Users/bgood/data/NARupdate2011/luca/good_bad_revs.txt";
		MetricsDB db = new MetricsDB();
		try {
			TreeSet<String> page_ids = db.getPageids();
			BigFile revs = new BigFile(luca_revs);
			int c = 0;
			int page_id_index = 50; int page_title_index = 51;
			int total_good = 0; int total_bad = 0; 
			Map<Calendar,String> sorted = new TreeMap<Calendar, String>();
			Map<Calendar,Float> qs = new TreeMap<Calendar, Float>();
			SimpleDateFormat format = new SimpleDateFormat("\"yyyyMMddHHmmss\"");
			SimpleDateFormat format_exl = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
			Calendar start = Calendar.getInstance();
			start.clear();
			start.set(2009, Calendar.SEPTEMBER,1);
			Calendar stop = Calendar.getInstance();
			stop.clear();
			stop.set(2011, Calendar.SEPTEMBER,1);
			for (String line : revs){
				c++;
				if(c==1){
					continue;
				}
				int quality_index = 67; int rev_id_index = 69; int time_index = 71;int user_index = 74;
				String[] row = line.split(",");
				String title = row[51];
				if(row.length>75){
					int n = row.length - 75;					
					for(int i = 52;i<52+n;i++){
						title += ","+row[i];
						quality_index++; rev_id_index++; time_index++;user_index++;
					}
				}
				String page_id = row[page_id_index];
				String page_title = title;
				String rev_id = row[rev_id_index];
				String time = row[time_index];

				if(user_index<row.length&&time.length()==16){
					//2010 04 16 09 22 30

					Calendar stamp = Calendar.getInstance();
					stamp.clear();
					stamp.setTime(format.parse(time));
					String user = row[user_index];
					if(stamp.after(start)&&stamp.before(stop)){
						//if(!user.toLowerCase().contains("bot")&&!user.equals("\"Yeast2Hybrid\"")){
						float quality = Float.parseFloat(row[quality_index]);					
						if(page_ids.contains(page_id)){		
							qs.put(stamp, quality);
							String r = quality+"\t"+page_id+"\t"+page_title+"\t"+rev_id+"\t"+user+"\t";
							sorted.put(stamp, r);
						}
						//}
					}
				}
			}
			FileWriter f = new FileWriter(file);
			f.write("date\ttotal_good\ttotal_bad\tquality\tpage_id\tpage_title\trev_id\teditor\n");
			for(Entry<Calendar,String> q : sorted.entrySet()){
				float quality = qs.get(q.getKey());
				if(quality>=0.4){
					total_good++;
				}else{
					total_bad++;
				}
				f.write(format_exl.format(q.getKey().getTime())+"\t"+total_good+"\t"+total_bad+"\t"+q.getValue()+"\n");
			}
			f.close();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	//TODO - finish up and link to gene wiki growth
	public static void buildPublicationGrowthTable(){
		String pubmed_sum_cache = "/Users/bgood/data/bioinfo/pubmed_summary_cache.txt";
		PubMed pm = new PubMed();
		SimpleDateFormat p_time = new SimpleDateFormat("yyyy MMM");
		Map<String, PubMed.PubmedSummary> pmid_sum = pm.getCachedPubmedsums(pubmed_sum_cache);
		//build a map of pubs by month
		Map<Calendar, List<PubMed.PubmedSummary>> month_psums = new TreeMap<Calendar, List<PubMed.PubmedSummary>>();
		for(PubMed.PubmedSummary psum : pmid_sum.values()){
			String d = psum.getPubDate();
			if(d!=null&&d.length()>=8&&!(d.contains("-"))){
				d = pm.convertSeasonToMonth(d); // there are a few cases where NLM puts a season like 'summer' in place of the month..
				Calendar pub_date = Calendar.getInstance();
				pub_date.clear();
				try {
					pub_date.setTime(p_time.parse(d));				
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
				List<PubMed.PubmedSummary> have = month_psums.get(pub_date);
				if(have==null){
					have = new ArrayList<PubMed.PubmedSummary>();
				}
				have.add(psum);
				month_psums.put(pub_date, have);
			}
		}
		//print out the counts
		int total = 0;
		for(Entry<Calendar, List<PubMed.PubmedSummary>> month_psumlist : month_psums.entrySet()){
			int monthly = month_psumlist.getValue().size();
			total+=monthly;
			System.out.println(p_time.format(month_psumlist.getKey().getTime())+"\t"+month_psumlist.getValue().size()+"\t"+total);
		}
	}


	/**
	 * Collects and renders data regarding the rank in the google results (first page only) for gene wiki pages when searching by official gene symbol
	 */
	public static void runGoogleRankAnalysis(){

		//work from local cache of index
		String index = "/users/bgood/data/bioinfo/gene_wiki_index.txt";
		Map<String,String> gene_wiki = GeneWikiUtils.readGeneWikiGeneIndex(index);
		//convert to gene symbols
		Map<String, Gene> gene_info = null;
		boolean usepublicmygeneinfo = false;
		try {
			gene_info = MyGeneInfo.getBatchGeneInfo(gene_wiki.keySet(), usepublicmygeneinfo);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//execute google query for gene symbol and cache results 
		String google_result_cache = "/users/bgood/data/NARupdate2011/google/google_cache.txt";
		Map<String, List<String>> gene_urls = getUrlsForGeneSymbols(google_result_cache);
		String google_rank = "/users/bgood/data/NARupdate2011/google/google_rank.txt";
		int g = 0;
		float n_with_gresults = 0; float n_first_page = 0; float n_gene_cards_not_gw = 0; float n_gene_cards = 0;
		Map<String, Float> gene_rank = new HashMap<String, Float>();
		boolean newone = true;
		for(Gene gene : gene_info.values()){
			if(gene_urls.containsKey(gene.getGeneID())){
				newone = false;
			}else{
				newone = true;
			}
			g++;
			String wikiurl = "http://en.wikipedia.org/wiki/";
			String title = gene_wiki.get(gene.getGeneID());
			//some id remapping happened at mygene.info, skip it
			if(title==null){
				continue;
			}
			title = title.replaceAll(" ", "_");
			try {
				title = URLEncoder.encode(title, "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//capture exceptions where gogole urls are not fully encoded..
			title = title.replaceAll("%28", "(");
			title = title.replaceAll("%29", ")");
			title = title.replaceAll("%2C", ",");
			wikiurl = wikiurl+title;
			System.out.println("on "+g+"\t"+gene.getGeneSymbol()+" fraction on first page = "+n_first_page/n_with_gresults);
			try {
				List<String> urls = null;
				if(!newone){
					urls = gene_urls.get(gene.getGeneID());
				}else{
					urls = GoogleSearch.getTopHits(gene.getGeneSymbol());
					try {
						int sleeptime = 1500+(int)(1000*Math.random());
						Thread.sleep(sleeptime);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(urls!=null&&urls.size()>0){
					n_with_gresults++;
					FileWriter f = new FileWriter(google_result_cache, true);
					int rank = 1;
					int wrank = 0;
					boolean genecards = false;
					for(String url : urls){
						if(newone){
							f.write(gene.getGeneID()+"\t"+gene.getGeneSymbol()+"\t"+rank+"\t"+url+"\n");
						}
						if(url.equalsIgnoreCase(wikiurl)){
							wrank = rank;
							gene_rank.put(gene.getGeneSymbol(), new Float(wrank));
							n_first_page++;
						}
						rank++;
						if(url.contains("genecards.org")){
							genecards = true;
						}
					}
					if(genecards){
						n_gene_cards++;
					}
					if(wrank==0){
						System.out.println("not on first page: "+gene.getGeneSymbol()+" "+wikiurl);
						FileWriter fr = new FileWriter("/Users/bgood/data/NARupdate2011/google/not_on_first.txt", true);
						fr.write(gene.getGeneID()+"\t"+gene.getGeneSymbol()+"\t"+wikiurl+"\n");
						fr.close();
						if(genecards){
							n_gene_cards_not_gw++;
						}
					}
					f.close();
					if(newone){
						FileWriter fr = new FileWriter(google_rank, true);
						fr.write(gene.getGeneID()+"\t"+gene.getGeneSymbol()+"\t"+wrank+"\n");
						fr.close();
					}
				}else{
					System.out.println("Error on request for "+gene.getGeneSymbol()+" stopping");
					break;
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//make histogram
		Map<Float, Integer> vals = new HashMap<Float, Integer>();
		for(Entry<String, Float> gr : gene_rank.entrySet()){
			Integer vcount = vals.get(gr.getValue());
			if(vcount==null){
				vcount = new Integer(0);
			}
			vcount++;
			vals.put(gr.getValue(), vcount);
		}
		System.out.println("Rank\tCount");
		for(Entry<Float, Integer> val : vals.entrySet()){
			System.out.println(val.getKey()+"\t"+val.getValue());
		}
		System.out.println("\nN gene cards "+n_gene_cards+" n gc not gene wiki "+n_gene_cards_not_gw);

	}
	public static Map<String, List<String>> getUrlsForGeneSymbols(String google_cache_file){
		Map<String, List<String>> gene_urls = new HashMap<String, List<String>>();
		if(google_cache_file!=null){
			File in = new File(google_cache_file);
			if(in.canRead()){
				try {
					BufferedReader f = new BufferedReader(new FileReader(google_cache_file));
					String line = f.readLine().trim();
					while(line!=null){
						String[] item = line.split("\t");
						if(item!=null&&item.length>1){
							List<String> urls = gene_urls.get(item[0]);
							if(urls == null){
								urls = new ArrayList<String>();
							}
							urls.add(item[3]);
							gene_urls.put(item[0], urls);
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
			}
		}
		return gene_urls;
	}

	/**
	 * builds a table allowing containing gene, n related pubmed articles (filtered  remove massive gene pubs), n related go terms, and n words form gene wiki article
	 */
	public static void getGenePubGoWordCounts(){
		double total_in_gene = 20473; // 42159 for all.. //See NCBI Gene with query (alive[prop] AND txid9606 )
		//count genes linked go (from ncbi gene2go) 
		boolean skipIEA = true; boolean only_human = true;
		Map<String, Set<GOterm>> gene2go = BioInfoUtil.readGene2GO("/Users/bgood/data/bioinfo/gene2go", skipIEA, only_human ); //updated Aug. 9, 2011

		//count genes linked to pubmed articles		
		String gene2pubmedcountfile = "/Users/bgood/data/NARupdate2011/gene2pmedGO_noIEA_pcoding_no_mega_pub_gw_words.txt";
		Map<String, List<String>> gene2pub = BioInfoUtil.getHumanGene2pub("/Users/bgood/data/bioinfo/gene2pubmed"); //updated Aug. 9, 2011
		boolean filter_mega_gene_pubs = true;
		//System.out.println("pmids before filter "+MapFun.flipMapStringListStrings(gene2pub).keySet().size());
		if(filter_mega_gene_pubs){
			gene2pub = Prioritizer.filterMegaGenePubs(gene2pub, 100);
		}
		//System.out.println("pmids AFTER filter "+MapFun.flipMapStringListStrings(gene2pub).keySet().size());
		Set<String> genes = new HashSet<String>(gene2pub.keySet());
		genes.addAll(gene2go.keySet());

		//remove non protein coding genes
		Map<String, Gene> gene_info = null;
		boolean usepublicmygeneinfo = false;
		try {
			gene_info = MyGeneInfo.getBatchGeneInfo(genes, usepublicmygeneinfo);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		System.out.println("GENE IDS WITH A GO OR A PMID "+genes.size());
		for(Entry<String, Gene> gene_entry : gene_info.entrySet()){
			Gene g = gene_entry.getValue();
			if(g!=null&&g.getGenetype()!=null&&!g.getGenetype().equals("protein-coding")){
				genes.remove(g.getGeneID());
			}
		}
		System.out.println("GENE IDS WITH A GO OR A PMID that are coding "+genes.size());

		double genes_with_5_plus = 0;
		double genes_with_any = 0;
		double genes_with_5_plus_go = 0;
		double genes_with_any_go = 0;		

		MetricsDB db = new MetricsDB();
		try {
			FileWriter f = new FileWriter(gene2pubmedcountfile);
			f.write("gene\ttitle\tGW_word_count\tpmid_count\tgo_count\n");

			for(String gene : genes){
				String title = "";
				String words = "";
				try {
					title = db.getTitleByGeneID(gene);
					words = db.getWordsByGeneID(gene);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				String r = gene+"\t"+title+"\t"+words+"\t";
				if(gene2pub.get(gene)!=null){
					genes_with_any++;
					if(gene2pub.get(gene).size()>5){		
						genes_with_5_plus++;
					}
					r+=gene2pub.get(gene).size()+"\t";
				}else{
					r+="0\t";
				}
				if(gene2go.get(gene)!=null){
					genes_with_any_go++;
					r+=gene2go.get(gene).size()+"\t";
					if(gene2go.get(gene).size()>5){
						genes_with_5_plus_go++;
					}
				}else{
					r+="0\t";
				}
				r+="\n";
				f.write(r);
			}
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double lowlinked_go = (total_in_gene - genes_with_5_plus_go);
		double nolinked_go = (total_in_gene - genes_with_any_go);
		System.out.println("GO: 5 or fewer:\t"+lowlinked_go+"\t%total:\t"+lowlinked_go/total_in_gene);
		System.out.println("GO: No linked refs:\t"+nolinked_go+"\t%total:\t"+nolinked_go/total_in_gene);

		double lowlinked = (total_in_gene - genes_with_5_plus);
		double nolinked = (total_in_gene - genes_with_any);
		System.out.println("PubMed, 5 or fewer:\t"+lowlinked+"\t%total:\t"+lowlinked/total_in_gene);
		System.out.println("PubMed, No linked refs:\t"+nolinked+"\t%total:\t"+nolinked/total_in_gene);
	}

}
