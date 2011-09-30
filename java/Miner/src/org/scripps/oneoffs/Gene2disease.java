/**
 * 
 */
package org.scripps.oneoffs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.genewiki.annotationmining.annotations.CandidateAnnotation;
import org.scripps.nlp.umls.UmlsDb;
import org.scripps.oneoffs.journal.GeneSetSelector;
import org.scripps.ontologies.mesh.MeshRDF;

import com.hp.hpl.jena.ontology.OntClass;

/**
 * @author bgood
 *
 */
public class Gene2disease {
	public static String gw_mined_cache = "/Users/bgood/data/bioinfo/gwmeshcache.txt";
	public static String gw_article_cache = "/Users/bgood/data/bioinfo/gene_wiki_as_java/";
	UmlsDb db;


	public Gene2disease() {
		db = new UmlsDb();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Gene2disease g2d = new Gene2disease();
		//load candidate annotations
		String anno_dir = "/Users/bgood/data/NARupdate2011/ncbo_annotations/";
		List<CandidateAnnotation> annos = g2d.loadCandidateMeshDisorderAnnotations(anno_dir);
		
	}

	public List<CandidateAnnotation> loadCandidateMeshDisorderAnnotations(String dir){
		List<CandidateAnnotation> cannos = new ArrayList<CandidateAnnotation>();
		File folder = new File(dir);		
		int g = 0; int n = 0;
		for(String file : folder.list()){
			g++;
			if(!file.endsWith(".txt")){
				continue;
			}
			BufferedReader f;
			try {
				f = new BufferedReader(new FileReader(dir+file));
				String line = f.readLine();
				//	line = f.readLine();//skip header
				while(line!=null){
					String[] item = line.split("\t");
					//only keep disease annotations 
					String stypess = item[10];
					String[] stypes = stypess.split(" ; ");
					boolean disorder = false;
					for(String stype : stypes){
						String group = db.getGroupForStypeName(stype.trim());
						if(group!=null&&group.equals("Disorders")){ 
							disorder = true;
							break;
						}
					}
					if(disorder&&item[11].equals("Medical Subject Headings")){
						CandidateAnnotation canno = new CandidateAnnotation();
						canno.setEntrez_gene_id(item[0]);
						canno.setSource_wiki_page_title(item[1]);
						canno.setSection_heading(item[2]);
						canno.setLink_author(item[3]);
						canno.setWikitrust_page(Float.parseFloat(item[4]));
						canno.setWikitrust_sentence(Float.parseFloat(item[5]));
						canno.setAnnotationScore(Double.parseDouble(item[6]));
						canno.setTarget_accession(item[7]);
						canno.setTarget_uri(item[8]);
						canno.setTarget_preferred_term(item[9]);
						canno.setTarget_semantic_types(item[10]);
						canno.setTarget_vocabulary(item[11]);
						canno.setTarget_vocabulary_id(item[12]);
						canno.setMatched_text(item[13]);
						canno.setParagraph_around_link(item[14]);
						if(item.length>15){
							canno.setCsvrefs(item[15]);
						}
						cannos.add(canno);		
						n++;
						System.out.println(g+"\t"+n+"\n"+canno);
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
		}
		return cannos;
	}

}
