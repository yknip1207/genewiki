/**
 * 
 */
package org.scripps.mashup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gnf.genewiki.journal.GeneSetSelector;
import org.gnf.mesh.MeshRDF;

import com.hp.hpl.jena.ontology.OntClass;

/**
 * @author bgood
 *
 */
public class Gene2disease {
	public static String gw_mesh_cache = "/Users/bgood/data/bioinfo/gwmeshcache.txt";
	public static String gene_wiki_article_cache = "/Users/bgood/data/bioinfo/gene_wiki_as_java/";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//load candidate annoati
		
		
//		//load the cached gene to mesh mapping from the august collection
//		Map<String, Set<String>> gw2meshes = GeneSetSelector.getCachedGWMeshMap(gw_mesh_cache);
//		MeshRDF meshtree = new MeshRDF();
//		//filter to disease
//		Map<String, Set<String>> gw2mesh_diseases = new HashMap<String, Set<String>>();
//		for(Entry<String, Set<String>> gene_meshs : gw2meshes.entrySet()){
//			String gene = gene_meshs.getKey();
//			for(String mesh : gene_meshs.getValue()){
//				OntClass t = meshtree.getTermByLabel(mesh);
//				if(t!=null){
//				Set<String> parents = meshtree.getFamily(t, true);
//				String root = meshtree.getRoot(parents);
//				if(root.equals("Diseases")){
//					Set<String> diseases = gw2mesh_diseases.get(gene);
//					if(diseases==null){
//						diseases = new HashSet<String>();
//					}
//					diseases.add(mesh);
//					gw2mesh_diseases.put(gene, diseases);
//				}
//				}else{
//					System.out.println("couldn't find "+mesh)
//				}
//			}
//		}
//		//how many ?
//		int genes = gw2mesh_diseases.size();
//		int tuples = 0;
//		for(Entry<String, Set<String>> gene_diseases : gw2mesh_diseases.entrySet()){
//			tuples+=gene_diseases.getValue().size();
//		}
//		System.out.println("genes "+genes+" links "+tuples);
	}

}
