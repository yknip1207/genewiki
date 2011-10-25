/**
 * 
 */
package org.scripps.ontologies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.genewiki.annotationmining.Config;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.OWL;

/**
 * Prepare ontology data for import into Semantic Media Wiki
 * Focus on bringing class hierarchy in as categories
 * Works with OWL
 * @author bgood
 *
 */
public class Onto2wiki {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Onto2wiki o = new Onto2wiki();
		String input_ont = "file:/Users/bgood/data/gw_mashup/doid.owl";
		Map<String, wikiCategory> cat_map = o.getCategories(input_ont);
		System.out.println(cat_map.size());
		for(Entry<String, wikiCategory> cat : cat_map.entrySet()){
			System.out.println(cat.getKey()+"\t"+cat.getValue().toString());
		}
	}

	class wikiCategory{
		String uri;
		String title;
		Set<String> directParents;
		String description;
		Set<String> synonyms;


		public wikiCategory(String uri, String title,
				Set<String> directParents, String description,
				Set<String> synonyms) {
			this.uri = uri;
			this.title = title;
			this.directParents = directParents;
			this.description = description;
			this.synonyms = synonyms;
		}


		public String toString(){
			return title+"\t"+synonyms+"\t"+uri+"\t"+directParents;
		}
	}

	public Map<String, wikiCategory> getCategories(String input_ont_file){
		Map<String, wikiCategory> cat_map = new HashMap<String, wikiCategory>();
		OntModel ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM); 
		ont.read(input_ont_file);
		System.out.println("read OWL file into Jena Model with no reasoning");
		ExtendedIterator<OntClass> classes = ont.listClasses();
		int c = 0;
		OntProperty dep = ont.getOntProperty(OWL.getURI()+"deprecated");

		while(classes.hasNext()){
			c++;
			OntClass oclass = classes.next();
			String title = oclass.getLabel(null);
			RDFNode d = oclass.getPropertyValue(dep);
			boolean isdep = false;
			if(d!=null){
				Literal l = d.as(Literal.class);
				isdep = l.getBoolean();
			}
			if(!isdep){

				String uri = oclass.getURI();
				String description = oclass.getComment(null);
				Set<String> directParents = new HashSet<String>();
				ExtendedIterator<OntClass> parents = oclass.listSuperClasses();
				while(parents.hasNext()){
					OntClass p = parents.next();
					directParents.add(p.getLabel(null));
				}
				Set<String> synonyms = new HashSet<String>();
				ExtendedIterator<RDFNode> syns = oclass.listLabels(null);
				while(syns.hasNext()){
					Literal t = syns.next().as(Literal.class);
					String syn = t.getString();
					if(!syn.equals(title)){
						synonyms.add(syn);
					}
				}
				wikiCategory cat = new wikiCategory(uri, title, directParents, description, synonyms);
				cat_map.put(title, cat);
				System.out.println(c+"\t"+cat);
			}else{
				System.out.println(c+"\t"+title+"\tDEPRECATED");
			}
		}

		return cat_map;

	}

}
