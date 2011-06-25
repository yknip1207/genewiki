package org.gnf.umls;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gnf.genewiki.Config;
import org.gnf.genewiki.GeneWikiLink;
import org.gnf.genewiki.GeneWikiPage;
import org.gnf.genewiki.GeneWikiUtils;
import org.gnf.genewiki.network.AllegroConnector;
import org.gnf.genewiki.network.Grapher;
import org.gnf.go.GOmapper;
import org.gnf.umls.Client;
import org.gnf.umls.Utils;

//import com.franz.ag.AllegroGraphException;
//import com.franz.agjena.query.AllegroGraphQueryExecutionFactory;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


public class SemanticLinker {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Client c = new Client(args[0], args[1]);//username password
		SemanticLinker s = new SemanticLinker();
//		String umls_triples = c.getTabTriplesForSearchString("hippocampus");
//		System.out.println(umls_triples);
//		s.getUmlsForLinkedPages(c);
		//s.getUmlsForGenes(c);
	
		try {
			FileReader test = new FileReader("C:\\Users\\bgood\\data\\genewiki\\umls_tab\\Choriocarcinoma");
			char[] r = new char[1000];
			test.read(r);
			System.out.println(r);
			test.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public void getUmlsForGenes(Client c){
		int limit = 1000000;
		String outdir = "C:\\Users\\bgood\\data\\genewiki\\umls_tab_genes\\";

		//read in article_gene map;
		String file = "data/genewiki/GeneWikiIndex.txt";
		String delimiter = "\t";
		String skipprefix = "#";
		boolean reverse = true;
		//could be more than one!
		Map<String, Set<String>> article_geneid = GeneWikiUtils.readMapFile(file, delimiter, skipprefix, reverse);

		int total = article_geneid.size();
		System.out.println("total to process: "+total);
		int n = 0;

		//check what we've got already
		File folder = new File(outdir);
		List<String> exist = new ArrayList<String>();
		for(String name : folder.list()){
			exist.add(name);
		}
		System.out.println("done with already "+exist.size());
		n = exist.size();
		int skipped = 0;
		for(String gene : article_geneid.keySet()){
			if(exist.contains(gene)){			
				continue;
			}else if((gene.contains(":"))||gene.contains("*")||gene.contains("\\")||gene.contains("/")){
				skipped++;
				System.out.println("Skipping "+gene+" "+skipped);				
				continue;
			}
			boolean gotit = false;
			n++;
			String umls_triples = "";
			try{
				umls_triples = c.getTabTriplesForSearchString(gene);
				gotit = true;
			}catch(Exception e){
				e.printStackTrace(System.out);
			}

			if(umls_triples==""||umls_triples==null){
				umls_triples = gene+"\tno exact match\tnone\n";
			}
			//write out each one
			if(gotit){
				try {
					FileWriter writer = new FileWriter(outdir+gene);
					writer.write(umls_triples);
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(n+"\t"+gene);
			}
		}
	}


	public void getUmlsForLinkedPages(Client c){
		int limit = 1000000;
		String outdir = "C:/Users/bgood/data/genewiki/umls_tab/";
		Set<GeneWikiPage> links = GeneWikiUtils.getNRlinks(limit, Config.gwikidir);
		int total = links.size();
		System.out.println("total to process: "+total);
		int n = 0;

		//check what we've got already
		File folder = new File(outdir);
		List<String> exist = new ArrayList<String>();
		for(String name : folder.list()){
			exist.add(name.toLowerCase());
		}
		int done = 0;
		for(GeneWikiPage page : links){
			String l = page.getTitle().toLowerCase();
			if(exist.contains(l)||(page.getTitle().contains(":"))||page.getTitle().contains("*")||page.getTitle().contains("\\")||page.getTitle().contains("/")){
				done++;
			}
		}
		System.out.println("already have "+done+" left to do "+(links.size()-done));
		
		for(GeneWikiPage page : links){
			n++;
			if(exist.contains(page.getTitle())||(page.getTitle().contains(":"))||page.getTitle().contains("*")||page.getTitle().contains("\\")||page.getTitle().contains("/")){
				//System.out.println("Skipping "+page.getTitle());
				continue;
			}
			
			boolean gotit = false;
			
			String umls_triples = "";
			String wikititle = page.getTitle();
			for(String searchString : page.getWikisynset()){

				try{
					umls_triples = c.getTabTriplesForSearchString(searchString);
					gotit = true;
				}catch(Exception e){
					e.printStackTrace(System.out);
				}
				if(umls_triples!=""){
					break;
				}
			}
			if(umls_triples==""||umls_triples==null){
				umls_triples = wikititle+"\tno exact match\tnone\n";
			}
			//write out each one
			if(gotit){
				try {
					System.out.println(n+"\t"+wikititle);
					FileWriter writer = new FileWriter(outdir+wikititle);
					writer.write(umls_triples);
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}


	public static void cleanupwikilinks(AllegroConnector conn){
	/*	System.out.println("indexing");
		try {
			Grapher.indexAllegro(conn);
		} catch (AllegroGraphException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("done indexing, running query");
	/*	
		
		/*	
		Model model = AllegroConnector.getAllegroGraphModel(allegroproject);
		Property wikilink = model.getProperty("http://gnf.org/WikiLink");
		//find the wikilinks - whatever the hell they are named
	   String select = " " +
		"PREFIX rdf: <" +RDF.getURI()+"> "+
	   	"SELECT ?gene ?rel ?wikipage WHERE { "+
		"?gene rdf:type <http://gnf.org/GeneWikiGene> . "+
		"?wikipage rdf:type <http://gnf.org/GeneWikiLink>. "+
		"?gene ?rel ?wikipage . "+
		"}";

	   QueryExecution qexec = AllegroGraphQueryExecutionFactory.create(select, model) ;
       try {
           ResultSet rs = qexec.execSelect() ;
           int n = 0;
           while(rs.hasNext()) {
               n++;
        	   QuerySolution rb = rs.nextSolution() ;
               RDFNode rel = rb.get("rel") ;
               RDFNode gene = rb.get("gene");
               RDFNode wikipage = rb.get("wikipage");

               Resource gene_r = (Resource)gene.as(Resource.class);
               Resource wikipage_r = (Resource)wikipage.as(Resource.class);

          //     gene_r.addProperty(wikilink, wikipage_r);
               System.out.println(n+"\tLInked "+gene_r+" to "+wikipage_r);
           }
       } finally  {
           qexec.close() ;
       }
		 */
	}

	public static Model addWikiLinksToModel(Client c, String inputFile, String output_dir, Model model, int limit, int batchsize){
		System.out.println("reading"); 
		Set<String> rterms = Utils.readTermFile(inputFile);
		List<String> terms = new ArrayList<String>(rterms);
		System.out.println("sorting");
		Collections.sort(terms);
		System.out.println("getting model ready");

		String typeuri = "http://gnf.org/GeneWikiLink";

		//specify type
		Resource type = model.getResource(typeuri);

		//property for a wikilink
		Property prop = model.getProperty("http://gnf.org/WikiLink");
		prop.addLiteral(RDFS.label, "WikiLink");

		int p = 0;
		System.out.println("gathering wiki pages");
		Set<String> wiki_targets = new HashSet<String>();
		boolean skip = true;

		
		boolean setredirects = true; boolean getAllWikiLinks = false; boolean getOnlyWikiLinksInText = true; boolean getHyperLinks = false;
		
		for(String title : terms){

			//			if(title.equals("HOXB1")){
			//				skip = false;
			//			}
			//			if(skip){
			//				continue;
			//			}
			System.out.println(p+" working on page: "+title);
			System.out.println("start size is "+model.size());
			if(!title.contains(":")){
				p++;
				if(p==limit){
					break;
				}
				String uri = "http://dbpedia.org/resource/"+title.replace(' ', '_');
				Resource subject = model.getResource(uri);

				//GO GET THE PAGE with the links for this gene
				GeneWikiPage wikipage = new GeneWikiPage(title);
				wikipage.defaultPopulate();
				//now for all the links we find for the page, add the associated triple			
				for(GeneWikiLink glink : wikipage.getGlinks()){
					System.out.println(p+" working on LINK: "+glink.getTarget_page());
					Resource linked = model.getResource("http://dbpedia.org/resource/"+glink.getTarget_page().replace(' ', '_'));
					subject.addProperty(prop, linked);					
					linked.addProperty(RDF.type, type);
					wiki_targets.add(glink.getTarget_page());
				}
			}
		}
		//now fill in data for the linked pages from the umls
		System.out.println("now about to add data for linked pages");
		//for storing backups
		Model batch = ModelFactory.createDefaultModel();
		p = 0;
		for(String target : wiki_targets){
			p++;
			//now add UMLS information for the linked thing
			Resource linked = batch.getResource("http://dbpedia.org/resource/"+target.replace(' ', '_'));
			boolean getrelations = false;
			c.getRDFTriplesForSearchString(target, linked, batch, getrelations);

			if(p%batchsize==0){
				System.out.println("Storing backup "+p);
				writeModelNtriples(batch, output_dir+"_"+p);
				model.add(batch);
				batch.close();
				batch = ModelFactory.createDefaultModel();
			}

			System.out.println("added UMLS data for a glink - size is "+model.size());
		}

		if(!batch.isClosed()){
			System.out.println("Storing LAST backup "+p);
			writeModelNtriples(batch, output_dir+"_"+p);
			model.add(batch);
			batch.close();
		}

		return model;
	}

	public static Model loadWikiConceptsFromUmls(Client c, String input_file, String output_dir, String typeuri, Model model, int limit, int backup_batch_size){
		boolean include_relations = true;
		System.out.println("reading");
		Set<String> rterms = Utils.readTermFile(input_file);
		List<String> terms = new ArrayList<String>(rterms);
		System.out.println("sorting");
		Collections.sort(terms);
		System.out.println("getting model ready");

		//specify type
		Resource type = model.getResource(typeuri);

		int p = 0;
		System.out.println("gathering");
		//for storing backups
		Model batch = ModelFactory.createDefaultModel();
		for(String searchString : terms){

			System.out.println("working on: "+searchString);
			if(!searchString.contains(":")){
				p++;
				if(p==limit){
					break;
				}

				String uri = "http://dbpedia.org/resource/"+searchString.replace(' ', '_');
				Resource subject = batch.createResource(uri);
				//specify what kind of thing we have here from a the wikipedia perspective
				//GeneWikiGene
				subject.addProperty(RDF.type, type);
				//now we go get the UMLS concept it matches 
				//if there is one we also add the concepts that concept is related to
				//if not we add a 'no match' attribute for bookkeeping
				c.getRDFTriplesForSearchString(searchString, subject, batch, include_relations);
				System.out.println(searchString+"\t"+p);

				//store backup on file and add data to triple store
				if(p%backup_batch_size==0){
					System.out.println("Storing backup "+p);
					writeModelNtriples(batch, output_dir+"_"+p);
					model.add(batch);
					batch.close();
					batch = ModelFactory.createDefaultModel();
				}

			}else{
				System.out.println(searchString+"-ignored\t"+p);
			}
		}

		if(!batch.isClosed()){
			System.out.println("Storing LAST backup "+p);
			writeModelNtriples(batch, output_dir+"_"+p);
			model.add(batch);
			batch.close();
		}


		return model;
	}

	public static void writeModelNtriples(Model m, String file){
		//write it all out
		try{
			FileOutputStream fout=new FileOutputStream(file+".nt");
			m.write(fout, "N-TRIPLE");
		}catch(IOException e){
			System.out.println("Exception caught writing rdf file"+e.getMessage());
		}
	}

	/**
 SELECT ?gene ?rel ?val WHERE {
?gene <http://gnf.org/UmlsConcept> ?concept .
?gene rdf:type <http://gnf.org/GeneWikiGene> .
?concept ?rel ?target .
?target rdfs:label ?val 
}

get me genes linked to concepts that appear inthe GO
 SELECT ?gene ?rel ?val WHERE {
?gene <http://gnf.org/UmlsConcept> ?concept .
?gene rdf:type <http://gnf.org/GeneWikiGene> .
?concept ?rel ?target .
?target rdfs:label ?val .
?target <http://gnf.org/UmlsVocab> <http://gnf.org/source_vocab_GO>
}

show connections made by gene wiki that are also made by UMLS
 SELECT ?gene ?rel ?val ?target WHERE {
?gene rdf:type <http://gnf.org/GeneWikiGene> .
?gene <http://gnf.org/UmlsConcept> ?concept .
?concept ?rel ?target .
?target rdfs:label ?val .
?gene <http://gnf.org/WikiLink> ?wikipage . 
?wikipage <http://gnf.org/UmlsConcept> ?target  
}

Show a specific set of semantic connections from the UMLS
SELECT ?genename ?edge ?targetname WHERE {

?gene rdf:type <http://gnf.org/GeneWikiGene> .
?gene <http://gnf.org/UmlsConcept> ?concept .
?concept rdfs:label ?genename .
?concept ?edge ?target .
?target rdfs:label ?targetname .

FILTER (
?edge = <http://gnf.org/NCI_is_associated_anatomy_of_gene_product> ||
?edge = <http://gnf.org/NCI_process_involves_gene> ||
?edge = <http://gnf.org/NCI_disease_has_associated_gene>||
?edge = <http://gnf.org/NCI_chemical_or_drug_affects_gene_product>
)

SELECT ?genename ?targetname WHERE {

?gene rdf:type <http://gnf.org/GeneWikiGene> .
?gene <http://gnf.org/UmlsConcept> ?concept .
?concept rdfs:label ?genename .
?concept <http://gnf.org/NCI_process_involves_gene> ?target .
?target rdfs:label ?targetname 
}
limit 1000

}

	 */

	/*
	 String inputFile = "data\\bloggedgenelist.txt";
		String typeuri = "http://gnf.org/GeneWikiGene";
		String allegroproject = "C:/Users/bgood/WikiGeneRDF/GeneWiki-UMLS-10/";///!!!!!!!
		String outputdir = "data\\gene-wiki-links-tmp\\";
		int limit = 100;
		int batchsize = 10;

		AllegroConnector conn = new AllegroConnector();
		conn.setAllegroGraphModel(allegroproject);
		Model model = conn.getModel();	

		try{
			model = loadWikiConceptsFromUmls(c, inputFile, outputdir, typeuri, model, limit, batchsize);
			//write it all out
			writeModelNtriples(model, "data\\genewiki-umls-100");
			System.out.println("Done with UMLS for genes - size is "+model.size());
		} finally  {
			try {
				conn.closeEverything();
			} catch (com.franz.agbase.AllegroGraphException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try{
			model = addWikiLinksToModel(c, inputFile, outputdir, model, limit, batchsize);
			System.out.println("Done adding wikilinks - size is "+model.size());

			//write it all out
			writeModelNtriples(model, "data\\genewiki-umls-100-with-links");

		}
		finally{
			try {
				conn.closeEverything();
			} catch (com.franz.agbase.AllegroGraphException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	 */

}
