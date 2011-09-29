/**
 * 
 */
package org.genewiki.mapping.externaldata;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.genewiki.Config;

/**
 * @author bgood
 *
 */
public class FuncBase {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//buildGene2Funcbase2go();
	}

	public static void buildGene2Funcbase2go(){
		Map<String, String> ensembl2gene = new HashMap<String, String>();
		BufferedReader f;
		FileWriter w;
		float nomatch = 0; float matched = 0;
		try {
			//load the gene2ensembl map
			f = new BufferedReader(new FileReader(Config.gene2ensembl));
			String line = f.readLine();
			while(line!=null){
				String[] item = line.split("\t");
				if(item[0].equals("9606")){
					String geneid = item[1];
					String ensid = item[2];
					ensembl2gene.put(ensid, geneid);
				}
				line = f.readLine();
			}
			f.close();
			//prepare output
			f = new BufferedReader(new FileReader(Config.funcbase_input));

			//load the ensembl to GO mapping and the score from funcbase
			w = new FileWriter(Config.funcbase);
			line = f.readLine();

			while(line!=null){
				String[] item = line.split("\t");
				boolean known_anno_exact = Boolean.parseBoolean(item[2]);
				boolean known_anno_child = Boolean.parseBoolean(item[4]);	
				//ignore annotations that are already known and recorded
				if(!(known_anno_exact||known_anno_child)){
					String goacc = item[0];			
					String ensid = item[1];
					String score = item[5];
					if(ensembl2gene.get(ensid)!=null){
						w.write(ensembl2gene.get(ensid)+"\t"+goacc+"\t"+score+"\n");
						matched++;
					}else{
						nomatch++;
						if(nomatch<10){
							System.out.println("no match in gene found for "+ensid);
						}
					}
				}
				line = f.readLine();
			}
			System.out.println("Successfully mapped "+matched+" predictions from ensembl to gene ids (%"+100*((matched-nomatch)/(matched+nomatch))+")");
			f.close();
			w.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("No match for "+nomatch);
	}
}
