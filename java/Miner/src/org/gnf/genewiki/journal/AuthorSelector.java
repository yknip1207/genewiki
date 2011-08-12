/**
 * 
 */
package org.gnf.genewiki.journal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gnf.genewiki.ncbi.PubMed;
import org.gnf.util.MapFun;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * @author bgood
 *
 */
public class AuthorSelector {

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
		Map<String, Double> r = getRankedAuthorListForPmids(pmids);
		System.out.println(getTopForExcelCell(r, 3));
	}

	public static Map<String, Double> getRankedAuthorListForPmids(Set<String> pmids){
		Map<String, Set<String>> pmid_authors = PubMed.getPubmedAuthors(pmids);
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
	public static Map<String, Double> getRankedAuthorListForPmids(List<String> pmids){
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
}
