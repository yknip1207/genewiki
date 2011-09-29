/**
 * 
 */
package org.scripps.oneoffs.journal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.scripps.datasources.ncbi.PubMed;
import org.scripps.datasources.ncbi.PubMed.PubmedSummary;
import org.scripps.util.MapFun;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * @author bgood
 *
 */
public class AuthorSelector {

	Map<String, PubmedSummary> pmid_sum;
	Map<String, Set<String>> pmid_authors;

	public AuthorSelector() {
		String sum_cache = "/Users/bgood/data/bioinfo/pubmed_summary_cache.txt";
		PubMed pm = new PubMed();
		pmid_sum = pm.getCachedPubmedsums(sum_cache);
		String cachefile = "/Users/bgood/data/bioinfo/pmidauthorcache.txt";
		pmid_authors = pm.readCachedPmid2Authors(cachefile);
	}

	/**
	 * Manage the author search process for the Gene Wiki / Gene collaboration
	 * @param args
	 */
	public static void main(String[] args) {
		Set<String> pmids = new HashSet<String>();
		pmids.add("19781082");
		pmids.add("17237074");
		pmids.add("17173692");
		pmids.add("17094234");
		pmids.add("16899496");
		pmids.add("16735456");
		AuthorSelector a = new AuthorSelector();
		Map<String, Double> r = a.getRankedAuthorListForPmids(pmids);
		System.out.println(getTopForExcelCell(r, 3));
	}


	public Map<String, Double> getRankedAuthorListForPmidsFromNCBI(Set<String> pmids){
		Map<String, Set<String>> pmid_authors = PubMed.getPubmedAuthorsNCBI(pmids);
		Map<String, Set<String>> author_pmids = MapFun.flipMapStringSetStrings(pmid_authors);
		Map<String, Double> author_count = new HashMap<String, Double>(author_pmids.size());
		for(String author : author_pmids.keySet()){
			Double c = new Double(author_pmids.get(author).size());
			author_count.put(author, c);
		}
		List<String> sorted = MapFun.sortMapByValue(author_count);
		Collections.reverse(sorted);
		author_count = new LinkedHashMap<String, Double>(author_pmids.size());
		for(String author : sorted){
			Double c = new Double(author_pmids.get(author).size());
			author_count.put(author, c);
		}
		return author_count;
	}

	public Map<String, Double> getRankedAuthorListForPmids(Set<String> pmids){
		Map<String, Set<String>> localp2a = new HashMap<String, Set<String>>(pmids.size());
		for(String pmid : pmids){
			localp2a.put(pmid, pmid_authors.get(pmid));
		}	
		Map<String, Set<String>> author_pmids = MapFun.flipMapStringSetStrings(localp2a);
		Map<String, Double> author_count = new HashMap<String, Double>(author_pmids.size());
		for(String author : author_pmids.keySet()){
			Double c = new Double(author_pmids.get(author).size());
			author_count.put(author, c);
		}
		List<String> sorted = MapFun.sortMapByValue(author_count);
		Collections.reverse(sorted);
		author_count = new LinkedHashMap<String, Double>(author_pmids.size());
		for(String author : sorted){
			Double c = new Double(author_pmids.get(author).size());
			author_count.put(author, c);
		}
		return author_count;
	}

	public Map<String, Double> getRankedAuthorListForPmids(List<String> pmids){
		Set<String> pmidss = new HashSet<String>(pmids);
		return getRankedAuthorListForPmids(pmidss);
	}

	public static String getTopForExcelCell(Map<String, Double> authors, int n){
		String top = ""; int c = 0;
		if(authors!=null&&authors.size()>0){
			for(String k : authors.keySet()){
				top+=k+"::"+authors.get(k)+", ";
				c++;
				if(c>=n){
					break;
				}
			}
			top = top.substring(0,top.length()-1);
		}
		return top;
	}


	public Map<String, Integer> getRecentAuthorListForPmids(
			List<String> publist) {
		Set<String> pmidss = new HashSet<String>(publist);
		return getRecentAuthorListForPmids(pmidss);
	}
	public Map<String, Integer> getRecentAuthorListForPmids(
			Set<String> publist) {
		Map<String, Integer> author_year = new HashMap<String, Integer>();
		for(String pmid : publist){
			Set<String> authors = pmid_authors.get(pmid);
			PubMed.PubmedSummary summary = pmid_sum.get(pmid);
			if(summary!=null){
				String date = summary.getPubDate();
				if(authors!=null&&date!=null&date.length()>=4){
					int year = Integer.parseInt(date.substring(0,4));
					for(String author : authors){
						author_year.put(author, year);
					}
				}
			}
		}
		List<String> sorted = MapFun.sortMapByValue(author_year);
		Collections.reverse(sorted);
		Map<String, Integer> author_years = new LinkedHashMap<String, Integer>(author_year.size());
		for(String k : sorted){
			author_years.put(k, author_year.get(k));
		}
		return author_years;
	}

	public static String getRecentForExcelCell(
			Map<String, Integer> authors, Map<String, Double> author_counts, int max_authors) {
		String top = ""; int c = 0;
		if(authors!=null&&authors.size()>0){
			for(String k : authors.keySet()){
				double co = author_counts.get(k);
				top+=k+"("+authors.get(k)+")::"+(int)co+", ";
				c++;
				if(c>=max_authors){
					break;
				}
			}
			top = top.substring(0,top.length()-1);
		}
		return top;
	}

	public static String getTopRecentForExcelCell(
			Map<String, Double> author_counts,
			Map<String, Integer> author_lpdate, int max_authors) {
		String top = ""; int c = 0;
		if(author_counts!=null&&author_counts.size()>0&&author_lpdate!=null&&author_lpdate.size()>0){
			for(String k : author_counts.keySet()){
				double co = author_counts.get(k);
				top+=k+"("+author_lpdate.get(k)+")::"+(int)co+", ";
				c++;
				if(c>=max_authors){
					break;
				}
			}
			top = top.substring(0,top.length()-1);
		}
		return top;
	}
}
