/**
 * 
 */
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

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.genewiki.*;
/**
 * @author bgood
 *
 */
public class Tmp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String, List<String>> gene2pub = getGene2pub("/Users/bgood/data/gene2pubmed", "10090");
		Set<String> genes = getGenes("/Users/bgood/data/JWalker_genes.txt");
		Set<String> pmids = new HashSet<String>();
		DescriptiveStatistics d = new DescriptiveStatistics();
		for(String g : genes){
			if(gene2pub.get(g)!=null){
				pmids.addAll(gene2pub.get(g));
				d.addValue(gene2pub.get(g).size());
			}
		}
		System.out.println("Total pmids: "+pmids.size()+"\n"+d.toString());
	}

	public static Set<String> getGenes(String file){
		Set<String> genes = new HashSet<String>();
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(file));
			String line = f.readLine();
			line = f.readLine();
			while(line!=null){			
				String[] stats = line.split("\t");
				genes.add(stats[1]);
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
		return genes;	
	}

	public static Map<String, List<String>> getGene2pub(String gene2pubmed_file, String taxid){
		Map<String, List<String>> p2g = new HashMap<String, List<String>>();
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(gene2pubmed_file));
			String line = f.readLine();
			while(line!=null){
				if(!line.startsWith(taxid)){
					line = f.readLine();
					continue;
				}
				String[] g2p = line.split("\t");
				List<String> pmids = p2g.get(g2p[1]);
				if(pmids==null){
					pmids = new ArrayList<String>();
				}
				pmids.add(g2p[2]);
				p2g.put(g2p[1], pmids);
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

}
