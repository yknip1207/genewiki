package org.gnf.go;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gnf.genewiki.Config;
import org.gnf.genewiki.GeneWikiUtils;

public class Annotation {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String goa_human = Config.panther_goa+"/gene_association.goa_human";	
		//getGoGrowth(goa_human);
		boolean skipIEA = false;
	//	Map<String, Set<GOterm>> goas = getGOaMap(goa_human, skipIEA);
	//	System.out.println("found "+goas.keySet().size()+" genes with an annotation ");
	
		Map<String, Set<GOterm>> genego = readGene2GO(Config.gene_go_file, true);
		System.out.println("found "+genego.keySet().size()+" genes with an annotation ");
	}

	public static void getGoGrowth(String goa_file){
		Map<String, Integer> ycounts = new HashMap<String, Integer>();
		Set<String> codes = new HashSet<String>();
		BufferedReader f = null;
		try {
			f = new BufferedReader(new FileReader(goa_file));			
			String line = f.readLine();
			//UniProtKB	Q8TEY7	USP33		GO:0005515	PMID:19118533	IPI	UniProtKB:Q13228	F	Ubiquitin carboxyl-terminal hydrolase 33	USP33|KIAA1097|VDU1|IPI00236901|IPI00377264|IPI00402757|UBP33_HUMAN|Q8TEY6|Q96AV6|Q9H9F0|Q9UPQ5	protein	taxon:9606	20101101	IntAct		UniProtKB:Q8TEY7-2	
			while(line!=null){
				if(line.startsWith("!")){
					line = f.readLine();
					continue;
				}
				String[] item = line.split("\t");
				//get the date
				String date = item[13].substring(0,4);
				String code = item[6];
				codes.add(code);
				Integer c = ycounts.get(date+"\t"+code);
				if(c==null){
					c = 1;
				}else{
					c++;
				}
				ycounts.put(date+"\t"+code, c);
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
		System.out.print("Year\t");
		for(String code : codes){
			System.out.print(code+"\t");
		}
		System.out.println();
		for(int i = 1998; i<2011; i++){
			System.out.print(i+"\t");
			for(String code : codes){
				Integer c = ycounts.get(i+"\t"+code);
				if(c == null){
					System.out.print("\t");
				}else{
					System.out.print(c+"\t");
				}
			}
			System.out.println();
		}

		//		for(Entry<String, Integer> ycount : ycounts.entrySet()){
		//			System.out.println(ycount.getKey()+"\t"+ycount.getValue());
		//		}

	}

	/**
	 * Reads a gene association file directly from GO.  Note that they don't use NCBI gene ids so this is not the same as the gene2go below
	 * @param goa_file
	 * @param skipIEA
	 * @return
	 */
	public static Map<String, Set<GOterm>> getGOAMap(String goa_file, boolean skipIEA){
		Map<String, Set<GOterm>> gene2go = new HashMap<String, Set<GOterm>>();
		BufferedReader f = null;
		try {
			f = new BufferedReader(new FileReader(goa_file));			
			String line = f.readLine();
			while(line!=null){
				if(line.startsWith("!")){
					line = f.readLine();
					continue;
				}
				String[] item = line.split("\t");
				//get the id from the supplying database
				String geneid = item[1];
				String acc = item[4];
				String code = item[6];
				if(acc.startsWith("GO:")){
					if(skipIEA&&code.equals("IEA")){
						line = f.readLine();
						continue;
					}else{
						Set<GOterm> gos = gene2go.get(geneid);
						if(gos==null){
							gos = new HashSet<GOterm>();
						}
						GOterm ngo = new GOterm("",acc,"","", true);
						//ngo.setEvidence(code);
						gos.add(ngo);
						gene2go.put(geneid, gos);
						//		System.out.println(geneid+"\t"+acc);
					}
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

		return gene2go;
	}
	
	public static HashMap<String, Set<GOterm>> readGene2GO(String file, boolean skipIEA){
		HashMap<String,Set<GOterm>> map = new HashMap<String, Set<GOterm>>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(file));
			String line = f.readLine().trim();
			while(line!=null){
				if(!line.startsWith("#")){
					String[] item = line.split("\t");
					if(item!=null&&item.length>1){
						String code = item[3];
						if(skipIEA&&code.equals("IEA")){
							line = f.readLine();
							continue;
						}
						String geneid = item[1];					
						Set<GOterm> GOs = map.get(geneid);
						if(GOs == null){
							GOs = new HashSet<GOterm>();
							map.put(geneid, GOs);
						}
						String id = null; String acc = item[2];
						String term = ""; String root = "";
						
						if(item.length>5){
							term = item[5];
							if(item.length>7){
								root = item[7];
							}
						}
						GOterm go = new GOterm(id, acc, root, term, true);
						go.setEvidence("ncbi_"+code);
						GOs.add(go);//the text term for the go id						
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
	
	public static HashMap<String, Set<GOterm>> readGene2GOtrackEvidence(String file){
		HashMap<String,Set<GOterm>> map = new HashMap<String, Set<GOterm>>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(file));
			String line = f.readLine().trim();
			while(line!=null){
				if(!line.startsWith("#")){
					String[] item = line.split("\t");
					if(item!=null&&item.length>1){
						String code = item[3];
//						if(skipIEA&&code.equals("IEA")){
//							line = f.readLine();
//							continue;
//						}
						String geneid = item[1];					
						Set<GOterm> GOs = map.get(geneid);
						if(GOs == null){
							GOs = new HashSet<GOterm>();
							map.put(geneid, GOs);
						}
						String id = null; String acc = item[2];
						String term = ""; String root = "";
						
						if(item.length>5){
							term = item[5];
							if(item.length>7){
								root = item[7];
							}
						}
						GOterm go = new GOterm(id, acc, root, term, true);
						go.setEvidence(code);
						GOs.add(go);//the text term for the go id						
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
}
