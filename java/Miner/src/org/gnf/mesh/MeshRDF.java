/**
 * 
 */
package org.gnf.mesh;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.gnf.genewiki.Config;
import org.gnf.go.GOterm;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Treats the MeSH hierarchy as a subclass graph and works with jena rdfs.
 * @author bgood
 *
 */
public class MeshRDF {
	OntModel mesh;


	public MeshRDF(OntModel mesh) {
		this.mesh = mesh;
	}
	public MeshRDF() {
		super();
		mesh = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
		mesh.read("file:/Users/bgood/data/bioinfo/mesh_2011_simple2.owl");
	}
	public void reload(){
		mesh = null;
		mesh = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
		mesh.read("file:/Users/bgood/data/bioinfo/mesh_2011_simple2.owl");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//convertSkosRDF2simpleRDFS();

		MeshRDF m = new MeshRDF();
		String label = "Clinical Trials, Phase III as Topic";
		for(int i=0; i<2; i++){
			OntClass oc = m.getTermByLabel(label);
			System.out.println(i+" "+label);
			//		System.out.println(i+"\t"+m.getOntclassAsString(oc));
			//		System.out.println(m.getRoot(m.getFamily(oc, true)));
		}
		label = "West Nile Fever";
		for(int i=0; i<2; i++){
			OntClass oc = m.getTermByLabel(label);
			System.out.println(i+" "+label);
			//System.out.println(i+"\t"+m.getOntclassAsString(oc));
			//		System.out.println(m.getRoot(m.getFamily(oc, true)));
		}
		label = "Antigens, CD164";
		for(int i=0; i<2; i++){
			OntClass oc = m.getTermByLabel(label);
			System.out.println(i+" "+label);
			//		System.out.println(i+"\t"+m.getOntclassAsString(oc));
			//		System.out.println(m.getRoot(m.getFamily(oc, true)));
		}
	}

	public String getRoot(Set<String> parents){
		String root = "";
		for(String p : parents){
			String r = "Other";
			if(p.equalsIgnoreCase(r)){
				root = r;
				return root;
			}
			r = "Anatomy";
			if(p.equalsIgnoreCase(r)){
				root = r;
				return root;
			}
			r = "Chemicals and Drugs";
			if(p.equalsIgnoreCase(r)){
				root = r;
				return root;
			}
			r = "Diseases";
			if(p.equalsIgnoreCase(r)){
				root = r;
				return root;
			}
			r = "Phenomena and Processes";
			if(p.equalsIgnoreCase(r)){
				root = r;
				return root;
			}
		}
		return root;
	}

	public String getOntclassAsString(OntClass c){
		String s = c.getLabel(null);
		if(s==null||s.length()<2){
			s = c.getLocalName();
		}
		OntProperty alt = mesh.getOntProperty("http://www.w3.org/2004/02/skos/core#altLabel");
		StmtIterator stmt = c.listProperties(alt);
		while(stmt.hasNext()){
			s += " "+stmt.next().getObject().toString();
		}
		Set<String> parents = getFamily(c, true);
		s+="\n	Parents: "+parents.toString();
		Set<String> children = getFamily(c, false);
		s+="\n	Children: "+children.toString();
		return s;
	}

	public Set<String> getFamily(OntClass t, boolean parents){
		String uri = t.getURI();
		String qlabel = t.getLabel(null);
		Set<String> fam = new HashSet<String>();
		String queryString =  	
			"PREFIX RDFS: <http://www.w3.org/2000/01/rdf-schema#> "+
			"SELECT ?super ?label "+ 
			"WHERE { "+
			" <"+uri+"> RDFS:subClassOf ?super . "+
			" ?super RDFS:label ?label " +
			"} ";

		if(!parents){
			queryString =  "PREFIX RDFS: <http://www.w3.org/2000/01/rdf-schema#> "+
			"SELECT ?super ?label "+ 
			"WHERE { "+
			" ?super RDFS:subClassOf <"+uri+">  . "+
			" ?super RDFS:label ?label " +
			"} ";
		}

		Query query = QueryFactory.create(queryString);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, mesh);
		try{
			ResultSet rs = qe.execSelect();

			while(rs.hasNext()){
				QuerySolution rb = rs.nextSolution() ;
				Literal labelnode = rb.getLiteral("label");
				String ln = labelnode.getString();
				if(!ln.equals(qlabel)){
					fam.add(labelnode.getString());
				}
			}

		}finally{
			// Important � free up resources used running the query
			qe.close();
		}
		return fam;
	}

	/**
	 * Given a label for a mesh term, produce its class	
	 * @param label
	 * @return
	 */
	public OntClass getTermByLabel(String label){
		if(label==null||label.length()<4){
			return null;
		}
		System.out.println("looking for "+ label);
		long t0 = System.currentTimeMillis();
		//get a term by a label - skos:altLabel
		OntClass mterm = null;

		String queryString = "" +
		"PREFIX rdfs: <"+RDFS.getURI()+"> "+
		"SELECT ?term WHERE { "+
		" ?term rdfs:label ?label . "+
		" FILTER (str(?label) = \""+label+"\") }";	//regex(?name,'^da','i') 
		//System.out.println(getXref);
		Query query = QueryFactory.create(queryString);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, mesh);
		try{
			ResultSet rs = qe.execSelect();
			if(rs.hasNext()){
				QuerySolution rb = rs.nextSolution() ;
				RDFNode term = rb.get("term") ;
				mterm = term.as(OntClass.class);

			}
			else{
				//check alt labels before giving up
				queryString = "" +
				"PREFIX rdfs: <"+RDFS.getURI()+"> " +
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "+
				"SELECT ?term WHERE { "+
				" ?term skos:altLabel ?label . "+
				" FILTER (str(?label) = \""+label+"\") }";	//regex(?name,'^da','i') 
				//System.out.println(getXref);
				query = QueryFactory.create(queryString);
				// Execute the query and obtain results
				qe = QueryExecutionFactory.create(query, mesh);

				rs = qe.execSelect();
				if(rs.hasNext()){
					QuerySolution rb = rs.nextSolution() ;
					RDFNode term = rb.get("term") ;
					mterm = term.as(OntClass.class);
				}
			}
		}finally{
			// Important � free up resources used running the query
			qe.close();		
		}
		long time = System.currentTimeMillis() - t0;
		System.out.println("Done querying "+time/1000);
		return mterm;
	}


	/**
	 * Takes a SKOS version of MeSH from the hive project and produces a simpler version where
	 * the hierarchical links (broader than/ narrower than) are turned into sublcass links.
	 * 
	 * While this is not true to the definition of subclass, it means that its to perform the
	 * basic function 'get broader' and 'get narrower' using any RDFS capable reasoner...
	 * 
	 * Note that the mesh distribution does not include the upper most roots of the tree, like 'Diseases'
	 * If needed, it takes a few minutes to add them manually to the output of this in Protege..
	 */
	public static void convertSkosRDF2simpleRDFS(){
		//load into rdfs model.
		long t0 = System.currentTimeMillis();
		System.out.println("Loading ");
		String mesh_rdf_file = "file:/Users/bgood/data/bioinfo/mesh_2011.rdf"; //mesh_2011_RDFS
		String mesh_rdfs_out = "/Users/bgood/data/bioinfo/mesh_2011_simple.rdfs";
		OntModel mesh = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
		mesh.read(mesh_rdf_file);
		long time = System.currentTimeMillis() - t0;
		System.out.println("Done loading in "+time/1000);

		OntModel mesh2 = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);

		String queryString = "" +
		"PREFIX rdf: <"+RDF.getURI()+"> "+
		"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "+
		"SELECT ?term ?preflabel ?altlabel ?parent ?child WHERE { "+
		" ?term skos:prefLabel ?preflabel . " +
		" OPTIONAL {" +
		" ?term skos:altLabel ?altlabel ." +
		" } " +
		" OPTIONAL{" +
		" ?term skos:broader ?parent ." +
		" } "+
		" OPTIONAL{" +
		" ?term skos:narrower ?child ." +
		" } "+
		"}";
		Query query = QueryFactory.create(queryString);
		OntProperty alt = mesh2.createOntProperty("http://www.w3.org/2004/02/skos/core#altLabel");
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, mesh);
		try{
			ResultSet rs = qe.execSelect();
			while(rs.hasNext()){
				QuerySolution rb = rs.nextSolution() ;
				RDFNode term = rb.get("term") ;
				OntResource mterm1 = term.as(OntResource.class);
				OntResource mterm2 = mesh2.createOntResource(mterm1.getURI());
				mesh2.add(mterm2, RDF.type, RDFS.Class);
				Literal prefLabel = rb.getLiteral("preflabel");
				if(prefLabel!=null){
					mterm2.addLabel(prefLabel);
				}
				Literal altLabel = rb.getLiteral("altlabel");
				if(altLabel!=null){
					//not geting into new model..
					mesh2.add(mterm2, alt, altLabel);
				}	
				RDFNode parentnode = rb.get("parent") ;
				if(parentnode!=null){
					OntResource parent = parentnode.as(OntResource.class);
					mesh2.add(parent, RDF.type, RDFS.Class);
					mterm2.addProperty(RDFS.subClassOf, parent);
				}
				RDFNode childnode = rb.get("child") ;
				if(childnode!=null){
					OntResource child = childnode.as(OntResource.class);
					mesh2.add(child, RDF.type, RDFS.Class);
					mesh2.add(child, RDFS.subClassOf, mterm2);
				}
			}
		}finally{
			// Important � free up resources used running the query
			qe.close();
		}
		try{
			FileOutputStream fout=new FileOutputStream(mesh_rdfs_out);
			mesh2.write(fout);
		}catch(IOException e){
			System.out.println("Exception caught"+e.getMessage());
		}
	}
}
