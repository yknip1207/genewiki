package org.genewiki.asgraph;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.genewiki.Config;
import org.genewiki.mapping.annotations.CandidateAnnotation;
import org.genewiki.mapping.annotations.CandidateAnnotations;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TestExporter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		OntModel model = getBaseGwikiModel();
		model = loadDiseaseAssociations(model);
		model = loadGOAssociations(model);
		String out1 = "/users/bgood/data/genewiki/tmp/dogo_relations.rdf";
		writeModel(model, out1);
		String out2 = "/users/bgood/data/genewiki/tmp/dogo_network.txt";
		writeRelationsForCytoscape(model, "http://www.w3.org/2004/02/skos/core#related", out2);
		String out3 = "/users/bgood/data/genewiki/tmp/dogo_types.txt";
		writeRelationsForCytoscape(model, RDF.type.getURI(), out3);
	}

	/**
	 * Load Gene Ontology relations into model
	 * @param model
	 * @return
	 */
	public static OntModel loadGOAssociations(OntModel model){
		CandidateAnnotations cannolist = new CandidateAnnotations();
		cannolist.loadAndFilterCandidateGOAnnotations(Config.merged_mined_annos, false);
		List<CandidateAnnotation> cannos = cannolist.getCannos();		
		System.out.println("Loaded all candidate GO annotations "+cannos.size());
		for(CandidateAnnotation canno : cannos){
			OntResource subject = model.createOntResource("http://dbpedia.org/resource/"+canno.getSource_wiki_page_title().replace(' ','_'));
			Literal label = model.createLiteral(canno.getSource_wiki_page_title().replace('_', ' '));
			if(!subject.hasLabel(label)){
				subject.addLabel(label);
			}
			subject.setRDFType(model.createClass("http://gnf.org#Gene"));
			OntResource object = model.createOntResource("http://purl.org/obo/owl/GO#"+canno.getTarget_accession().replace(":","_"));

			Literal slabel = model.createLiteral(canno.getTarget_preferred_term().replace('_', ' '));
			if(!object.hasLabel(slabel)){
				object.addLabel(slabel);
			}			
			object.setRDFType(model.createClass("http://gnf.org#"+canno.getVocabulary_branch()));

			subject.addProperty(model.getProperty("http://www.w3.org/2004/02/skos/core#related"), object);
		}
		return model;
	}

	/**
	 * Load the associations between diseases and genes into the model
	 * @param model
	 * @return
	 */
	public static OntModel loadDiseaseAssociations(OntModel model){
		CandidateAnnotations cannolist = new CandidateAnnotations();
		cannolist.loadAndFilterCandidateDOAnnotations(Config.merged_mined_annos);
		List<CandidateAnnotation> cannos = cannolist.getCannos();		
		System.out.println("Loaded all candidate DO annotations "+cannos.size());
		for(CandidateAnnotation canno : cannos){
			OntResource subject = model.createOntResource("http://dbpedia.org/resource/"+canno.getSource_wiki_page_title().replace(' ','_'));
			Literal label = model.createLiteral(canno.getSource_wiki_page_title().replace('_', ' '));
			if(!subject.hasLabel(label)){
				subject.addLabel(label);
			}
			subject.setRDFType(model.createClass("http://gnf.org#Gene"));

			OntResource object = model.createOntResource("http://purl.org/obo/owl/DOID#"+canno.getTarget_accession().replace(":","_"));
			Literal slabel = model.createLiteral(canno.getTarget_preferred_term().replace('_', ' '));
			if(!object.hasLabel(slabel)){
				object.addLabel(slabel);
			}
			object.setRDFType(model.createClass("http://gnf.org#Disease"));

			subject.addProperty(model.getProperty("http://www.w3.org/2004/02/skos/core#related"), object);
		}
		return model;
	}

	/**
	 * Build foundation of gene wiki RDF.  Eventually replace this with an OWL file load if things progress.
	 * @return
	 */
	public static OntModel getBaseGwikiModel(){
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		OntClass gene = model.createClass("http://gnf.org#Gene");
		gene.addLiteral(RDFS.label, "Gene");
		OntClass bp = model.createClass("http://gnf.org#biological_process");
		bp.addLiteral(RDFS.label, "Biological Process");
		OntClass mf = model.createClass("http://gnf.org#molecular_function");
		mf.addLiteral(RDFS.label, "Molecular Function");
		OntClass cc = model.createClass("http://gnf.org#cellular_component");
		cc.addLiteral(RDFS.label, "Cellular Component");
		OntClass disease = model.createClass("http://gnf.org#Disease");
		disease.addLiteral(RDFS.label, "Disease");
		OntClass anatomy = model.createClass("http://gnf.org#Anatomy");
		anatomy.addLiteral(RDFS.label, "Anatomy");
		String skos_pre = "http://www.w3.org/2004/02/skos/core#";
		String skos_rel = skos_pre+"related";
		Property related = model.createProperty(skos_rel);
		return model;
	}

	/**
	 * Write out a jena model
	 * @param model
	 * @param file
	 */
	public static void writeModel(Model model, String file){
		try{
			FileOutputStream fout=new FileOutputStream(file);
			model.write(fout);
		}catch(IOException e){
			System.out.println("Exception caught"+e.getMessage());
		}
	}

	/**
	 * Write out model for cytoscape
	 * @param model
	 * @param file
	 */
	public static void writeRelationsForCytoscape(OntModel model, String relation_uri, String file){
		StmtIterator stmts = model.listStatements((Resource)null, model.getProperty(relation_uri), (RDFNode)null);
		try {
			FileWriter w = new FileWriter(file);
			while(stmts.hasNext()){
				Statement stmt =  stmts.nextStatement();
				OntResource o = stmt.getObject().as(OntResource.class);
				OntResource s = stmt.getSubject().as(OntResource.class);
				String row = s.getLabel(null)+"\t"+o.getLabel(null);
				w.write(row+"\n");
			}
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
