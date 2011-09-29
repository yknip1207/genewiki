package org.scripps.ontologies.go;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.genewiki.annotationmining.Config;

import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.OntTools;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class GOowl {
	public OntModel go;
	String go_root_uri = "http://purl.org/obo/owl/GO#";
	Property root_prop;

	public GOowl() {
		super();
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GOowl gol = new GOowl();
		gol.initFromFile(true);
		//String out = "/Users/bgood/data/genewiki/input/ontologies/10-21-2010/go_simplified.owl";
		//gol.buildSimpleGo(out);
		//http://purl.org/obo/owl/GO#GO_0035538
		System.out.println(gol.getGoBranch("0035538"));
		System.out.println(gol.getGoBranch("0004718"));
	}

	public void close(){
		go.close();
	}


	public void initFromFile(boolean simple){
		OntModel go = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM); 
		if(simple){
			go.read(Config.gordf);
		}else{
			go.read(Config.gordf_with_parts);
		}
		System.out.println("read go OWL file into Jena Model with no reasoning");
		
		setGo(go);
		root_prop = go.getProperty("http://www.geneontology.org/formats/oboInOwl#hasOBONamespace");
	}

	public void initFromFileRDFS(boolean simple){
		OntModel go = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
		if(simple){
			go.read(Config.gordf);
		}else{
			go.read(Config.gordf_with_parts);
		}
		System.out.println("read go OWL file into Jena Model with RDFS inference on");
		setGo(go);	
		root_prop = go.getProperty("http://www.geneontology.org/formats/oboInOwl#hasOBONamespace");
	}

	public void initFromFilePellet(boolean simple){
		OntModel go = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
		if(simple){
			go.read(Config.gordf);
		}else{
			go.read(Config.gordf_with_parts);
		}
		System.out.println("read go OWL file into Jena Model with Pellet reasoning on");
		setGo(go);
		root_prop = go.getProperty("http://www.geneontology.org/formats/oboInOwl#hasOBONamespace");
	}

	/**
	 * Build a version of the go where all parts and all regulated by are subclasses
	 * Use for simple 'parent' queries
	 */
	public void buildSimpleGo(String out){

		String queryString = 
			"PREFIX owl:  <http://www.w3.org/2002/07/owl#>  " +
			"PREFIX rdfs: <"+RDFS.getURI()+"> "+
			"SELECT ?class1 ?class2 "+ 
			"WHERE { "+
			" ?class1 rdfs:subClassOf ?anon . "+
			" ?anon owl:someValuesFrom ?class2 " +
			"} ";
		Query query = QueryFactory.create(queryString);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, go);
		OntModel newparts = ModelFactory.createOntologyModel();
		try{
			ResultSet rs = qe.execSelect();

			while(rs.hasNext()){
				QuerySolution rb = rs.nextSolution() ;
				Resource class1 = rb.getResource("class1");
				Resource class2 = rb.getResource("class2");
				newparts.add(class1, RDFS.subClassOf, class2);
			}
		}finally{
			// Important � free up resources used running the query
			qe.close();
		}
		go.add(newparts);
		//write it all out
		try{
			FileOutputStream fout=new FileOutputStream(out);
			go.write(fout);
		}catch(IOException e){
			System.out.println("Exception caught"+e.getMessage());
		}
	}


	public float getIsaDepth(String acc){
		String uri = makeGoUri(acc.substring(3));
		OntClass term = go.getOntClass(uri);
		OntClass mf = go.getOntClass(makeGoUri("0003674"));
		OntClass cc = go.getOntClass(makeGoUri("0005575"));
		OntClass bp = go.getOntClass(makeGoUri("0008150"));
		OntTools.Path path = OntTools.findShortestPath(go, term, bp, new OntTools.PredicatesFilter(RDFS.subClassOf));	
		if(path!=null){
			return path.size();
		}
		path = OntTools.findShortestPath(go, term, mf, new OntTools.PredicatesFilter(RDFS.subClassOf));				
		if(path!=null){
			return path.size();
		}
		path = OntTools.findShortestPath(go, term, cc, new OntTools.PredicatesFilter(RDFS.subClassOf));		
		if(path!=null){
			return path.size();
		}

		return 0;
	}

	public Set<GOterm> getSupers(GOterm term){
		Set<GOterm> terms = new HashSet<GOterm>();
		String uri = makeGoUri(term.getAccession().substring(3));
		String queryString = "PREFIX GOOWL: <http://www.geneontology.org/formats/oboInOwl#> "+ 	
		"PREFIX RDFS: <http://www.w3.org/2000/01/rdf-schema#> "+
		"SELECT ?super ?label "+ 
		"WHERE { "+
		" <"+uri+"> RDFS:subClassOf ?super . "+
		" ?super RDFS:label ?label " +
		"} ";
		Query query = QueryFactory.create(queryString);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, go);
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
						GOterm newterm = new GOterm(null, null, term.getRoot(), null, false);
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

	public Set<GOterm> getSubs(GOterm term){
		Set<GOterm> terms = new HashSet<GOterm>();
		String uri = makeGoUri(term.getAccession().substring(3));
		String queryString = "PREFIX GOOWL: <http://www.geneontology.org/formats/oboInOwl#> "+ 	
		"PREFIX RDFS: <http://www.w3.org/2000/01/rdf-schema#> "+
		"SELECT ?sub ?label "+ 
		"WHERE { "+
		" ?sub RDFS:subClassOf  <"+uri+"> . "+
		" ?sub RDFS:label ?label " +
		"} ";
		Query query = QueryFactory.create(queryString);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, go);
		try{
			ResultSet rs = qe.execSelect();
			// Simple Output query results
			//ResultSetFormatter.out(System.out, rs, query);

			while(rs.hasNext()){
				QuerySolution rb = rs.nextSolution() ;
				Resource superclass = rb.get("sub").as(Resource.class) ;
				Literal labelnode = rb.getLiteral("label");
				String label = null;
				if(labelnode!=null){
					String local = superclass.getLocalName();
					String acc = local.replace("_",":");
					if(!acc.equals(term.getAccession())){
						label = labelnode.getValue().toString();
						GOterm newterm = new GOterm(null, null, term.getRoot(), null, false);
						newterm.setEvidence(term.getEvidence());
						newterm.setInferred_child(true);
						newterm.setAccession(acc);
						newterm.setTerm(label);
						terms.add(newterm);
					}
				}

			}

		}finally{
			// Important to free up resources used running the query
			qe.close();
		}
		if(terms.size()>0){
			return terms;
		}else{
			return null;
		}
	}

	/**
	 * Assumes reasoning is on, only gets direct children..  This goes very very very slowly.. boo.
	 * @param term
	 * @param depth
	 * @return
	 */
	public Set<GOterm> getDirectChildren(GOterm term){
		Set<GOterm> terms = new HashSet<GOterm>();
		String uri = makeGoUri(term.getAccession().substring(3));
		OntClass start = (OntClass)go.getOntClass(uri);
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
				GOterm newterm = new GOterm(null, null, term.getRoot(), null, false);
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

	public String getGoBranch(String go_id){
		String guri = makeGoUri(go_id.trim());
		OntClass t = go.getOntClass(guri.trim());	
		if(t==null){
			System.out.println("no root for "+go_id+" "+guri);
			return "";
		}
		String root = t.getPropertyValue(root_prop).toString();
		
		return root;
	}
	
	
	public GOterm makeGOterm(String go_id){
		GOterm newterm = new GOterm(null, null, null, null, true);
		Property p = go.getProperty("http://www.geneontology.org/formats/oboInOwl#hasOBONamespace");
		String guri = makeGoUri(go_id.trim());
	
		String catac = "http://purl.org/obo/owl/GO#GO_0003824"; //GO:0004718
		OntClass c = go.getOntClass(catac);
		
		OntClass t = go.getOntClass(guri.trim());	
		String root = t.getPropertyValue(p).toString();
		newterm.setRoot(root);
		String local = t.getLocalName();
		newterm.setAccession(local.replace("_",":"));
		newterm.setTerm(t.getLabel("en"));
		newterm.setEvidence("unknown");
		return newterm;
	}
	
	public GOterm makeGOterm(GOterm term, OntClass t){
		GOterm newterm = new GOterm(null, null, term.getRoot(), null, true);
		if(term.getRoot()==null||term.getRoot().length()<3){
			Property p = go.getProperty("http://www.geneontology.org/formats/oboInOwl#hasOBONamespace");
			String root = t.getPropertyValue(p).toString();
			newterm.setRoot(root);
		}
		newterm.setEvidence(term.getEvidence());
		String local = t.getLocalName();
		newterm.setAccession(local.replace("_",":"));
		newterm.setTerm(t.getLabel("en"));
		return newterm;
	}
	
	public static String makeGoUri(String go_id){
		return "http://purl.org/obo/owl/GO#GO_"+go_id;
	}

	public static void printIterator(Iterator<?> i, String header) {
		System.out.println(header);
		for(int c = 0; c < header.length(); c++)
			System.out.print("=");
		System.out.println();

		if(i.hasNext()) {
			while (i.hasNext()) 
				System.out.println( i.next() );
		}       
		else
			System.out.println("<EMPTY>");

		System.out.println();
	}


	public GOterm getGOfromLabel(String label){
		GOterm goterm = new GOterm("", "", "", label, true);
		if(label.equals("cell surface receptor linked signal transduction")){
			goterm.setAccession("GO:0007166");
			return goterm;
		}
		if(label.equals("cell motion")){
			goterm.setAccession("GO:0006928");
			return goterm;
		}
		if(label.equals("GTPase activity")){
			goterm.setAccession("GO:0003924");
			return goterm;
		}
		if(label.equals("intracellular signaling cascade")){
			goterm.setAccession("GO:0007242");
			return goterm;
		}
		if(label.equals("small GTPase regulator activity")){
			goterm.setAccession("GO:0005083");
			return goterm;
		}
		if(label.equals("acyl-CoA metabolic process")){
			goterm.setAccession("GO:0006637");
			return goterm;
		}
		if(label.equals("DNA binding")){
			goterm.setAccession("GO:0003677");
			return goterm;
		}
		if(label.equals("G-protein coupled receptor activity")){
			goterm.setAccession("GO:0004930");
			return goterm;
		}
		if(label.equals("embryonic development")){
			goterm.setAccession("GO:0009790");
			return goterm;
		}
		if(label.equals("establishment or maintenance of chromatin architecture")){
			goterm.setAccession("GO:0006325");
			return goterm;
		}
		if(label.equals("gut mesoderm development")){
			goterm.setAccession("GO:0007502");
			return goterm;
		}
		if(label.equals("RNA splicing factor activity, transesterification mechanism")){
			goterm.setAccession("GO:0031202");
			return goterm;
		}
		if(label.equals("endothelial cell adhesion")){
			goterm.setAccession("GO:0071603");
			goterm.setRoot("biological_process");
			return goterm;
		}
		
		
		String queryString = "" +
		"PREFIX rdfs: <"+RDFS.getURI()+"> "+
		"PREFIX GOOWL: <http://www.geneontology.org/formats/oboInOwl#> "+
		"SELECT ?a ?ns WHERE { "+
		" ?a rdfs:label ?l . "+
		" ?a GOOWL:hasOBONamespace ?ns " +
		" FILTER (str(?l) = \""+label+"\") }";	//regex(?name,'^da','i') 
		//System.out.println(getXref);
		Query query = QueryFactory.create(queryString);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, go);
		try{
			ResultSet rs = qe.execSelect();
			// Simple Output query results
			//	ResultSetFormatter.out(System.out, rs, query);

			if(rs.hasNext()){
				QuerySolution rb = rs.nextSolution() ;
				RDFNode term = rb.get("a") ;
				OntClass gterm = term.as(OntClass.class);
				Literal branch = rb.getLiteral("ns");
				goterm.setAccession(gterm.getLocalName().replace("_", ":"));
				goterm.setRoot(branch.getString());
			}else{
				System.out.println("No term found for: "+label);
				return null;
			}


		}finally{
			// Important � free up resources used running the query
			qe.close();
		}


		return goterm;
	}

	public List<String> getGoTermSuperParts(String go_acc_uri){
		List<String> supers = new ArrayList<String>();
		String queryString = "PREFIX GO:   <http://purl.org/obo/owl/GO#> "+
		"PREFIX obo: <http://purl.org/obo/owl/OBO_REL#> "+
		"PREFIX owl:  <http://www.w3.org/2002/07/owl#>  "+
		"PREFIX rdfs: <"+RDFS.getURI()+">"+
		"PREFIX rdf: <"+RDF.getURI()+">"+
		"SELECT ?superpart WHERE { "+
		"<"+go_acc_uri+"> rdfs:subClassOf ?a . "+  //GO:GO_0007067
		"?a rdf:type owl:Restriction ."+
		"?a owl:onProperty obo:part_of . "+
		"?a owl:someValuesFrom ?superpart"+
		"}";
		//System.out.println(getXref);
		Query query = QueryFactory.create(queryString);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, go);
		try{
			ResultSet rs = qe.execSelect();
			// Simple Output query results
			ResultSetFormatter.out(System.out, rs, query);

			while(rs.hasNext()){
				QuerySolution rb = rs.nextSolution() ;
				RDFNode xnode = rb.get("superpart") ;
				String x = xnode.toString();
				supers.add(x);
			}


		}finally{
			// Important � free up resources used running the query
			qe.close();
		}
		return supers;
	}

	public List<String> getGoTermWikiXref(String go_acc_uri){
		List<String> wikixrefs = new ArrayList<String>();
		String getXref = 
			"PREFIX GO: <http://purl.org/obo/owl/GO#> "+
			"PREFIX GOOWL: <http://www.geneontology.org/formats/oboInOwl#> "+
			"PREFIX RDFS: <"+RDFS.getURI()+">"+
			"SELECT ?xref WHERE {"+
			"<"+go_acc_uri+"> GOOWL:hasDbXref ?bnode ."+ 
			"?bnode RDFS:label ?xref "+ 
			"FILTER regex(?xref, \"Wikipedia:\")"+
			"}";

	//	System.out.println(getXref);
		Query query = QueryFactory.create(getXref);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, go);
		try{
			ResultSet rs = qe.execSelect();
			while(rs.hasNext()){
				QuerySolution rb = rs.nextSolution() ;
				RDFNode xnode = rb.get("xref") ;
				String x = xnode.toString();
				//wikixrefs.addAll(splitCamel(x.substring(10)));
				wikixrefs.add(x.substring(10));
			}
			// Simple Output query results
			//ResultSetFormatter.out(System.out, results, query);

		}finally{
			// Important � free up resources used running the query
			qe.close();
		}
		return wikixrefs;
	}

	public boolean isGoTermObsolete(String go_acc){
		String go_acc_uri = makeGoUri(go_acc.substring(3));
		boolean obs = false;
		String get = 
			"PREFIX GOOWL: <http://www.geneontology.org/formats/oboInOwl#> "+
			"PREFIX RDFS: <"+RDFS.getURI()+"> "+
			"SELECT ?label WHERE {"+
			"<"+go_acc_uri+"> RDFS:subClassOf GOOWL:ObsoleteClass . "+ 
			"<"+go_acc_uri+"> RDFS:label ?label "+ 
			"}";

		//		System.out.println(get);

		Query query = QueryFactory.create(get);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, go);
		try{
			ResultSet rs = qe.execSelect();
			if(rs.hasNext()){
				obs = true;
			}

		}finally{
			// Important � free up resources used running the query
			qe.close();
		}
		return obs;
	}

	public Set<GOterm> getGoTermObsolete(GOterm term){
		String go_acc_uri = makeGoUri(term.getAccession().substring(3));
		Set<GOterm> terms = new HashSet<GOterm>();
		String get = 
			"PREFIX GOOWL: <http://www.geneontology.org/formats/oboInOwl#> "+
			"PREFIX RDFS: <"+RDFS.getURI()+"> "+
			"SELECT ?replacement ?label WHERE {"+
			"<"+go_acc_uri+"> GOOWL:replacedBy ?replacement . "+ 
			"?replacement RDFS:label ?label "+ 
			"}";

		Query query = QueryFactory.create(get);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, go);
		try{
			ResultSet rs = qe.execSelect();
			while(rs.hasNext()){
				QuerySolution rb = rs.nextSolution() ;
				RDFNode rep = rb.get("replacement") ;
				Literal label = rb.get("label").as(Literal.class);

				String accession = rep.as(Resource.class).getLocalName().replace("_",":");
				GOterm newterm = new GOterm(null, accession, term.getRoot(), label.getString(), true);
				newterm.setEvidence(term.getEvidence());
				terms.add(newterm);
			}

			// Simple Output query results
			//		ResultSetFormatter.out(System.out, rs, query);

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


	public static List<String> splitCamel(String camel){
		String[] strArray = StringUtils.splitByCharacterTypeCamelCase(camel);
		if(strArray == null || strArray.length==0){
			return null;
		}else{
			List<String> list = new ArrayList<String>(strArray.length);
			for(String item : strArray){
				list.add(item);
			}
			return list;
		}

	}

	public Model getGo() {
		return go;
	}



	public void setGo(OntModel go) {
		this.go = go;
	}


	/**
	 * An example to show how to use PelletReasoner as a Jena reasoner. The reasoner can
	 * be directly attached to a plain RDF <code>Model</code> or attached to an <code>OntModel</code>.
	 * This program shows how to do both of these operations and achieve the exact same results. 
	 * 
	 * @author Evren Sirin
	 */

	public static void usageWithDefaultModel() {
		System.out.println("Results with plain RDF Model");
		System.out.println("----------------------------");
		System.out.println();

		// ontology that will be used
		String ont = "http://protege.cim3.net/file/pub/ontologies/koala/koala.owl#";
		String ns = "http://protege.stanford.edu/plugins/owl/owl-library/koala.owl#";

		// create Pellet reasoner
		Reasoner reasoner = PelletReasonerFactory.theInstance().create();

		// create an empty model
		Model emptyModel = ModelFactory.createDefaultModel( );

		// create an inferencing model using Pellet reasoner
		InfModel model = ModelFactory.createInfModel( reasoner, emptyModel );

		// read the file
		model.read( ont );

		// print validation report
		ValidityReport report = model.validate();
		printIterator( report.getReports(), "Validation Results" );

		// print superclasses
		Resource c = model.getResource( ns + "MaleStudentWith3Daughters" );         
		printIterator(model.listObjectsOfProperty(c, RDFS.subClassOf), "All super classes of " + c.getLocalName());

		System.out.println();
	}

	public static void usageWithOntModel() {    
		System.out.println("Results with OntModel");
		System.out.println("---------------------");
		System.out.println();

		// ontology that will be used
		String ont = "http://protege.cim3.net/file/pub/ontologies/koala/koala.owl#";
		String ns = "http://protege.stanford.edu/plugins/owl/owl-library/koala.owl#";

		// create an empty ontology model using Pellet spec
		OntModel model = ModelFactory.createOntologyModel( PelletReasonerFactory.THE_SPEC );

		// read the file
		model.read( ont );

		// print validation report
		ValidityReport report = model.validate();
		printIterator( report.getReports(), "Validation Results" );

		// print superclasses using the utility function
		OntClass c = model.getOntClass( ns + "MaleStudentWith3Daughters" );         
		printIterator(c.listSuperClasses(), "All super classes of " + c.getLocalName());
		// OntClass provides function to print *only* the direct subclasses 
		printIterator(c.listSuperClasses(true), "Direct superclasses of " + c.getLocalName());

		System.out.println();
	}

}
