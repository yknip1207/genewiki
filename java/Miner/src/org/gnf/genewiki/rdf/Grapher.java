package org.gnf.genewiki.rdf;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;


//import com.franz.ag.AllegroGraphException;
//import com.franz.agbase.AllegroGraph;
//import com.franz.agbase.AllegroGraphConnection;
//import com.franz.agjena.query.AllegroGraphQueryExecutionFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


public class Grapher {

	/**
	 * @param args
	 * @throws AllegroGraphException 
	 */
//	public static void main(String[] args) throws AllegroGraphException {
//		// TODO Auto-generated method stub
//		String allegroproject = "C:/Users/bgood/WikiGeneRDF/GeneWiki-UMLS-blogged/";
//		AllegroConnector conn = new AllegroConnector();
//		conn.setAllegroGraphModel(allegroproject);
//		Model model = conn.getModel();	
//		String out = "data\\GeneWikiUmlsTestBigGraphProcessOnly";
//		int limit = 100;
//		//	indexAllegro(allegroproject);
//		//	buildUmlsGeneGraph(allegroproject, model, out, limit);
//		buildUmlsGeneProcessGraphInSparql(conn, model, out);
//	}
//
//	public static void buildUmlsGeneProcessGraphInSparql(AllegroConnector conn, Model model, String file) throws AllegroGraphException{
//		indexAllegro(conn);
//
//		String queryString = "" +
//		"PREFIX rdf: <" +RDF.getURI()+"> "+
//		"PREFIX rdfs: <" +RDFS.getURI()+"> "+
//		"SELECT ?genename ?targetname WHERE { "+
//		"?gene rdf:type <http://gnf.org/GeneWikiGene> ."+
//		"?gene <http://gnf.org/UmlsConcept> ?concept ."+
//		"?concept rdfs:label ?genename ."+
//		"?concept <http://gnf.org/NCI_process_involves_gene> ?target ."+
//		"?target rdfs:label ?targetname }";
//
//		QueryExecution qexec = AllegroGraphQueryExecutionFactory.create(queryString, model) ;
//		try {
//			FileWriter writer = new FileWriter(file+".sif");
//			FileWriter attwriter = new FileWriter(file+".noa");
//			Set<String> nodeatts = new HashSet<String>();
//			attwriter.write("TYPE\n");
//			try {
//				ResultSet rs = qexec.execSelect() ;
//				while(rs.hasNext()) {
//					QuerySolution rb = rs.nextSolution() ;
//					RDFNode x = rb.get("genename") ;
//					RDFNode y = rb.get("targetname");
//					if(x.toString().length()<30 && y.toString().length()<30){
//						nodeatts.add(x+" = Gene\n");
//						nodeatts.add(y+" = Process\n");
//						writer.write(x+"\tNCI_process_involves_gene\t"+y+"\n");
//					}
//				}
//			} finally  {
//				qexec.close() ;
//				writer.close();
//				for(String node : nodeatts){
//					attwriter.write(node);
//				}
//				attwriter.close();
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	public static void indexAllegro(AllegroConnector conn) throws AllegroGraphException{
//		conn.getAgStore().indexAllTriples();
//	}
//
//	public static void indexNewAllegro(String project) throws AllegroGraphException{
//		AllegroGraphConnection ags = new AllegroGraphConnection();
//		try {
//			ags.enable();
//		} catch (Exception e) {
//			throw new AllegroGraphException("Server connection problem", e);
//		}
//		AllegroGraph ts = ags.access(project, null);
//		ts.indexNewTriples();
//	}
//
//	public static void buildUmlsGeneGraph(String allegroproject, Model model, String out, int limit) throws AllegroGraphException{
//		//get GeneWikiGenes
//		Resource gwgene = model.getResource("http://gnf.org/GeneWikiGene");
//		Property umlsconcept = model.getProperty("http://gnf.org/UmlsConcept");
//		Property umlstype = model.getProperty("http://gnf.org/UmlsSemanticType");
//		Property nciprocess = model.getProperty("http://gnf.org/NCI_process_involves_gene");
//		ResIterator genes = model.listSubjectsWithProperty(RDF.type, gwgene);
//		try {
//			FileWriter writer = new FileWriter(out+".sif");
//			FileWriter attwriter = new FileWriter(out+".noa");
//			HashMap<String, HashSet<String>> target_type = new HashMap<String, HashSet<String>>();
//			attwriter.write("TYPE\n");
//			int c = 0;
//			while(genes.hasNext()&&c<limit){
//				indexNewAllegro(allegroproject);
//
//				System.out.println(c);
//				Resource gene = genes.next();
//				c++;
//
//				//if its going too slow, try indexing...
//				StmtIterator conceptstmt = gene.listProperties(umlsconcept);
//				String genename = gene.getLocalName();
//				attwriter.write(genename+" = Gene\n");
//				while(conceptstmt.hasNext()){
//					Resource concept = (Resource) conceptstmt.next().getObject();
//					StmtIterator relations = concept.listProperties(nciprocess);
//					//concept.listProperties();
//					while(relations.hasNext()){
//						Statement triple = relations.next();
//						String edge = triple.getPredicate().getLocalName();
//						//	if(! (edge.equals("UmlsSemanticType")||edge.equals("UmlsSemanticGroup")||edge.equals("UmlsVocab")
//						//			||edge.equals("label"))){
//						if(edge.equals("NCI_process_involves_gene")||
//								edge.equals("NCI_disease_has_associated_gene")||
//								edge.equals("NCI_is_associated_anatomy_of_gene_product")||
//								edge.equals("NCI_chemical_or_drug_affects_gene_product")){
//							Resource targetnode = (Resource)triple.getObject();
//							String target = targetnode.getProperty(RDFS.label).getString();
//							writer.write(genename+"\t"+edge+"\t"+target+"\n");
//							//get the semantic type of the target and store as a node attribute
//							StmtIterator stypes = targetnode.listProperties(umlstype);
//							while(stypes.hasNext()){
//								Resource stype = (Resource)stypes.next().getObject();
//								String stypelabel = stype.getProperty(RDFS.label).getString();
//								HashSet<String> types = target_type.get(target);
//								if(types==null){
//									types = new HashSet<String>();
//								}
//								types.add(stypelabel);
//								target_type.put(target, types);
//							}
//						}
//					}
//				}
//
//			}
//			System.out.println("done collecting edges now writing attributes");
//			for(Entry<String, HashSet<String>> entry : target_type.entrySet()){
//				for(String att : entry.getValue()){
//					attwriter.write(entry.getKey()+" = "+att+"\n");
//				}
//			}
//
//			attwriter.close();
//			writer.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

}
