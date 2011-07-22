/**
 * 
 */
package org.gnf.dont;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.gnf.genewiki.Config;
import org.gnf.go.GOowl;
import org.gnf.go.GOterm;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author bgood
 *
 */
public class DOowl {
	public OntModel doid;
	static String do_root_uri = "http://purl.org/obo/owl/DOID#";
	static String infectious = do_root_uri+"DOID_0050117";
	static String anatomical = do_root_uri+"DOID_7";
	static String cellular_proliferation = do_root_uri+"DOID_14566";
	static String mental_health = do_root_uri+"DOID_150";
	static String metabolism = do_root_uri+"DOID_0014667";
	static String disorder = do_root_uri+"DOID_0060035";
	static String hereditary = do_root_uri+"DOID_630";
	static String syndrome = do_root_uri+"DOID_225";

	boolean rdf_inf;

	public DOowl() {
		super();
	}

	public void initFromFile(){
		OntModel doid = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM); 
		doid.read(Config.dordf);
		System.out.println("read DO OWL file into Jena Model with no reasoning");
		setDoid(doid);
		rdf_inf = false;
	}

	public void initFromFileRDFS(){
		OntModel doid = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
		doid.read(Config.dordf);
		System.out.println("read DO OWL file into Jena Model with RDFS reasoning");
		setDoid(doid);
		rdf_inf = true;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {		
	}

	/**
	 * 
	 * @param map from some thing (gene) to a set of DO accessions
	 * @return expanded map - all the way up and one level down
	 */
	public Map<String, Set<DOterm>> expandDoMap(Map<String, Set<DOterm>> m, boolean addchildren){
		if(!rdf_inf){
			initFromFileRDFS();
		}

		//get parents for all linked terms
		Map<DOterm, Set<DOterm>> ps = new HashMap<DOterm, Set<DOterm>>();
		//children - one level
		Map<DOterm, Set<DOterm>> cs = new HashMap<DOterm, Set<DOterm>>();
		for(Entry<String, Set<DOterm>> entry : m.entrySet()){
			for(DOterm DOterm : entry.getValue()){
				if(!ps.containsKey(DOterm)){
					Set<DOterm> parents = getSupers(DOterm);
					ps.put(DOterm, parents);
				}
				if(addchildren){
					if(!cs.containsKey(DOterm)){
						Set<DOterm> children = getDirectChildren(DOterm);
						cs.put(DOterm, children);
					}
				}

			}
		}
		System.out.println("expansions cached");

		Map<String, Set<DOterm>> tmp_geneid_go = new HashMap<String, Set<DOterm>>();
		//add the parents
		for(Entry<String, Set<DOterm>> entry : m.entrySet()){
			String geneid = entry.getKey();
			Set<DOterm> p = new HashSet<DOterm>();
			for(DOterm DOterm : entry.getValue()){
				p.add(DOterm);
				Set<DOterm> parents = ps.get(DOterm);
				if(parents!=null){
					for(DOterm parent : parents){
						if(parent != DOterm){
							p.add(parent);
						}
					}
				}
			}
			tmp_geneid_go.put(geneid, p);
		}
		return tmp_geneid_go;
	}

	public Set<DOterm> getDirectChildren(DOterm term){
		Set<DOterm> terms = new HashSet<DOterm>();
		String uri = makeDoUri(term.getAccession().substring(5));
		OntClass start = (OntClass)doid.getOntClass(uri);
		if(start==null){
			return null;
		}
		ExtendedIterator<OntClass> stmt = start.listSubClasses(true);
		while(stmt.hasNext()){
			OntClass child = stmt.next();
			String label = child.getLabel("EN");
			String local = child.getLocalName();
			String acc = local.replace("_",":");
			if(!acc.equals(term.getAccession())){
				DOterm newterm = new DOterm();
				newterm.setEvidence(term.getEvidence());
				newterm.setAccession(acc);
				newterm.setTerm(label);
				// not really 'inferred' here but heh..  
				newterm.setInferred_child(true);
				terms.add(newterm);
			}
		}
		if(terms.size()>0){
			return terms;
		}else{
			return null;
		}
	}

	public Set<DOterm> getSubs(DOterm term){
		Set<DOterm> terms = new HashSet<DOterm>();
		String uri = makeDoUri(term.getAccession().substring(5));
		String queryString = "PREFIX DOOWL: <http://www.geneontology.org/formats/oboInOwl#> "+ 	
		"PREFIX RDFS: <http://www.w3.org/2000/01/rdf-schema#> "+
		"SELECT ?sub ?label "+ 
		"WHERE { "+
		" ?sub RDFS:subClassOf <"+uri+"> . "+
		" ?sub RDFS:label ?label " +
		"} ";
		Query query = QueryFactory.create(queryString);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, doid);
		try{
			ResultSet rs = qe.execSelect();
			// Simple Output query results
			//ResultSetFormatter.out(System.out, rs, query);

			while(rs.hasNext()){
				//				QuerySolution rb = rs.nextSolution() ;
				//				Resource subclass = rb.get("sub").as(Resource.class) ;
				//				Literal labelnode = rb.getLiteral("label");
				//				String label = null;
				//				if(labelnode!=null){
				//					String local = subclass.getLocalName();
				//					String acc = local.replace("_",":");
				//					label = labelnode.getValue().toString();
				//					terms.add(acc+"\t"+label);
				QuerySolution rb = rs.nextSolution() ;
				Resource superclass = rb.get("sub").as(Resource.class) ;
				Literal labelnode = rb.getLiteral("label");
				String label = null;
				if(labelnode!=null){
					String local = superclass.getLocalName();
					String acc = local.replace("_",":");
					if(!acc.equals(term.getAccession())){
						label = labelnode.getValue().toString();
						DOterm newterm = new DOterm();
						newterm.setEvidence(term.getEvidence());
						newterm.setInferred_child(true);
						newterm.setAccession(acc);
						newterm.setTerm(label);
						terms.add(newterm);
					}
				}

			}

		}finally{
			// Important � free up resources used running the query
			qe.close();
		}
		if(terms.size()>0){
			return terms;
		}else{
			return null;
		}
	}


	public Set<DOterm> getSupers(DOterm term){
		Set<DOterm> terms = new HashSet<DOterm>();
		String uri = makeDoUri(term.getAccession().substring(5));
		String queryString = "PREFIX DOOWL: <http://www.geneontology.org/formats/oboInOwl#> "+ 	
		"PREFIX RDFS: <http://www.w3.org/2000/01/rdf-schema#> "+
		"SELECT ?super ?label "+ 
		"WHERE { "+
		" <"+uri+"> RDFS:subClassOf ?super . "+
		" ?super RDFS:label ?label " +
		"} ";
		Query query = QueryFactory.create(queryString);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, doid);
		try{
			ResultSet rs = qe.execSelect();
			// Simple Output query results
			//ResultSetFormatter.out(System.out, rs, query);

			while(rs.hasNext()){
				QuerySolution rb = rs.nextSolution() ;
				Resource superclass = rb.get("super").as(Resource.class) ;
				Literal labelnode = rb.getLiteral("label");
				String label = null;
				if(labelnode!=null){
					String local = superclass.getLocalName();
					String acc = local.replace("_",":");
					if(!acc.equals(term.getAccession())){
						label = labelnode.getValue().toString();
						DOterm newterm = new DOterm();
						newterm.setEvidence(term.getEvidence());
						newterm.setInferred_parent(true);
						newterm.setAccession(acc);
						newterm.setTerm(label);
						terms.add(newterm);
					}
				}

			}

		}finally{
			// Important � free up resources used running the query
			qe.close();
		}
		if(terms.size()>0){
			return terms;
		}else{
			return null;
		}
	}

	public static String makeDoUri(String do_id){
		return do_root_uri+"DOID_"+do_id;
	}

	/**
	 * Get the xrefs for the do accession from the UMLS
	 * @param do_acc_uri
	 * @return
	 */
	public List<String> getDoTermUmlsXref(String do_acc_uri){
		List<String> xrefs = new ArrayList<String>();
		String getXref = 
			"PREFIX DO: <http://purl.org/obo/owl/DOID#> "+
			"PREFIX GOOWL: <http://www.geneontology.org/formats/oboInOwl#> "+
			"PREFIX RDFS: <"+RDFS.getURI()+">"+
			"SELECT ?xref WHERE {"+
			"<"+do_acc_uri+"> GOOWL:hasDbXref ?bnode ."+ 
			"?bnode RDFS:label ?xref "+ 
			"FILTER regex(?xref, \"UMLS_CUI:\")"+  // UMLS_CUI:C0016977
			"}";

		//	System.out.println(getXref);
		Query query = QueryFactory.create(getXref);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, doid);
		try{
			ResultSet rs = qe.execSelect();
			while(rs.hasNext()){
				QuerySolution rb = rs.nextSolution() ;
				RDFNode xnode = rb.get("xref") ;
				String x = xnode.toString();
				xrefs.add(x.substring(10));
			}
			// Simple Output query results
			//ResultSetFormatter.out(System.out, results, query);

		}finally{
			qe.close();
		}
		return xrefs;
	}

/**
 * <owl:Class rdf:about="#DOID_12140">
    <oboInOwl:hasExactSynonym>
      <oboInOwl:Synonym>
        <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
        >chagas' disease with nervous system involvement</rdfs:label>
      </oboInOwl:Synonym>
    </oboInOwl:hasExactSynonym>
 * @return
 */
	public List<String> getDoTermLabel(String do_acc_uri){
		List<String> labels = new ArrayList<String>();
		String getXref = 
			"PREFIX DO: <http://purl.org/obo/owl/DOID#> "+
			"PREFIX GOOWL: <http://www.geneontology.org/formats/oboInOwl#> "+
			"PREFIX RDFS: <"+RDFS.getURI()+">"+
			"SELECT ?label WHERE {" +
//			"{"+
//			"<"+do_acc_uri+"> GOOWL:hasExactSynonym ?bnode ."+ 
//			"?bnode RDFS:label ?label }"+ 
//			"UNION " +
//			"{" +
			"<"+do_acc_uri+"> RDFS:label ?label" +
			"}" +
//			"}"+
			" ";

		//	System.out.println(getXref);
		Query query = QueryFactory.create(getXref);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, doid);
		try{
			ResultSet rs = qe.execSelect();
			while(rs.hasNext()){
				QuerySolution rb = rs.nextSolution() ;
				String x = rb.getLiteral("label").getString();
				labels.add(x);
			}
			// Simple Output query results
			//ResultSetFormatter.out(System.out, results, query);

		}finally{
			qe.close();
		}
		return labels;
	}

	public OntModel getDoid() {
		return doid;
	}

	public void setDoid(OntModel doid) {
		this.doid = doid;
	}

}
