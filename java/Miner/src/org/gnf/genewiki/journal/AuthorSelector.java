/**
 * 
 */
package org.gnf.genewiki.journal;

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

	public static Map<String, Set<String>> readCachedPmid2Authors(String cachefile){
		Map<String, Set<String>> pmid_authors = new HashMap<String, Set<String>>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(cachefile));
			String line = f.readLine();
			while(line!=null){
				String[] row = line.split("\t");
				if(row!=null&&row.length>1){
					String pmid = row[0];
					String[] authors = row[1].split("::");
					Set<String> authorset = new HashSet<String>();
					for(String a : authors){
						authorset.add(a);
					}
					pmid_authors.put(pmid, authorset);
				}
				line = f.readLine();
			}
			f.close();
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pmid_authors;
	}

	public static void buildCacheLinkingPmids2Authors(Set<String> pmids, String cachefile){
		Map<String, Set<String>> old_pmid_authors = new HashMap<String, Set<String>>();
		File cache = new File(cachefile);
		try {
			if(!cache.createNewFile()){
				old_pmid_authors = readCachedPmid2Authors(cachefile);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Total pmids done = "+old_pmid_authors.keySet().size());
		pmids.removeAll(old_pmid_authors.keySet());
		System.out.println("Total pmids to get = "+pmids.size());
		int max_batch = 500;
		int c = 0; int n = 0;
		String plist = "";
		if(pmids!=null&&pmids.size()>0){
			for(String pmid : pmids){
				plist+=pmid+",";
				c++;
				n++;
				if(c==max_batch||c==pmids.size()){
					System.out.println("Getting author names - at "+n);
					plist = plist.substring(0, plist.length()-1);
					Map<String, Set<String>> tmp_authors = PubMed.getPubmedAuthors(plist);
					try {
						FileWriter f = new FileWriter(cachefile, true);
						for(String pmid_ : tmp_authors.keySet()){
							f.write(pmid_+"\t");
							Set<String> authors = tmp_authors.get(pmid_);
							if(authors!=null&&authors.size()>0){
								String as = "";
								for(String a : authors){
									as+=a+"::";
								}
								as = as.substring(0,as.length()-2);
								f.write(as+"\n");
							}
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					plist = "";
					c=0;
				}
			}
		}				
	}

	public static Map<String, Double> getRankedAuthorListForPmidsFromCache(List<String> pmids, Map<String, Set<String>> all_pmid_authors){
		Map<String, Set<String>> pmid_authors = new HashMap<String, Set<String>>();
		if(pmids==null){
			return null;
		}
		for(String pmid : pmids){
			pmid_authors.put(pmid, all_pmid_authors.get(pmid));
		}
		if(pmid_authors==null||pmid_authors.size()==0){
			return null;
		}
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
