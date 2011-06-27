package org.gnf.genewiki.network;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.gnf.genewiki.Config;
import org.gnf.genewiki.GWRevision;
import org.gnf.genewiki.GeneWikiLink;
import org.gnf.genewiki.Sentence;
import org.gnf.genewiki.WikiCategoryReader;
import org.gnf.genewiki.Workflow;
import org.gnf.genewiki.GeneWikiPage;
import org.gnf.genewiki.GeneWikiUtils;
import org.gnf.genewiki.associations.CandidateAnnotation;
import org.gnf.genewiki.associations.CandidateAnnotations;
import org.gnf.genewiki.metrics.RevisionCounter;
import org.gnf.genewiki.metrics.RevisionsReport;
import org.gnf.genewiki.network.SNPediaMashup.LinkedDisease;
import org.gnf.go.GOmapper;
import org.gnf.util.FileFun;
import org.gnf.util.TextFun;
import org.gnf.wikiapi.Page;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


/***
 * Class to handle conversion of extracted associations to simple rdf or tab formats
 * @author bgood
 *
 */
public class NetworkExtractor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String datadir = "/Users/bgood/data/network/rdf/";
		//getAllGeneWikiEditorsLinksAsRDF(datadir);
		//outputRDFdirAsText(datadir, "/Users/bgood/data/network/all_gw_loops_editors.txt");
		summarizeNetwork(datadir, "/Users/bgood/data/network/node_degrees.txt");
	}

	public static void summarizeNetwork(String rdfdir, String outfile){
		File f = new File(rdfdir);
		int limit = 10000000; int c = 0;
		Map<String, Integer> in_deg = new HashMap<String, Integer>();
		Map<String, Integer> out_deg = new HashMap<String, Integer>();
		try {
			FileWriter w = new FileWriter(outfile);
			w.write("Type\turi\tin-degree\tout-degree\n");
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(File t: f.listFiles()){
			c++;
			Model m = getBaseModel();
			m.read("file:"+t.getAbsolutePath());
			//Genes, links
			Resource g = m.getResource("http://dbpedia.org/resource/"+t.getName());
			StmtIterator s = g.listProperties(m.getProperty("http://example.org/wikilink_in"));
			int in = 0;
			if(s!=null){
				in = s.toList().size();
				if(in>0){
					while(s.hasNext()){
						Resource l = (Resource)s.nextStatement().getObject();
						Integer link_d = out_deg.get("link\t"+l.getURI());
						if(link_d == null){
							link_d = 0;
						}
						link_d++;
						out_deg.put("link\t"+l.getURI(), link_d);
					}
				}
			}
			in_deg.put("gene\t"+t.getName(), in);
			s = g.listProperties(m.getProperty("http://example.org/wikilink_out"));
			int out = 0;
			if(s!=null){
				out = s.toList().size();
				if(out>0){
					while(s.hasNext()){
						Resource l = (Resource)s.nextStatement().getObject();
						Integer link_d = in_deg.get("link\t"+l.getURI());
						if(link_d == null){
							link_d = 0;
						}
						link_d++;
						in_deg.put("link\t"+l.getURI(), link_d);
					}
				}
			}
			out_deg.put("gene\t"+t.getName(), out);
			//editors
			String uri = "";
			try {
				uri = "http://dbpedia.org/resource/"+URLEncoder.encode(t.getName(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String queryString = 
				"PREFIX ex: <http://example.org/> " +
				"PREFIX DC: <http://purl.org/dc/elements/1.1/> " +
				"PREFIX FOAF: <http://xmlns.com/foaf/0.1/> " +
				"PREFIX RDFS: <"+RDFS.getURI()+"> " +
				"SELECT ?user ?edits "+ 
				"WHERE "+
				" { ?event DC:subject <"+uri+"> . " +
				" ?event DC:contributor ?user . " +
				" ?event ex:weight ?edits ."+
				"} ";
			Query query = QueryFactory.create(queryString);
			//		// Execute the query and obtain results
			QueryExecution qe = QueryExecutionFactory.create(query, m);

			try{
				ResultSet rs = qe.execSelect();
				while(rs.hasNext()){
					QuerySolution rb = rs.nextSolution() ;
					Resource user = rb.getResource("user");
					int edits = rb.getLiteral("edits").getInt();
					Integer user_edits = out_deg.get("user\t"+user.getURI());
					if(user_edits==null){
						user_edits = 0;
					}
					user_edits++;
					out_deg.put("user\t"+user.getURI(), user_edits);
				}
			}finally{
				qe.close();
			}


			if(c%100==0){
				System.out.println("Finished processing: "+c);
			}
			if(c==limit){
				break;
			}
		}
		try {
			FileWriter w = new FileWriter(outfile);
			w.write("Type\turi\tin-degree\tout-degree\n");
			Set<String> keys = new HashSet<String>(out_deg.keySet());
			keys.addAll(in_deg.keySet());
			for(String key : keys){
				Integer in = in_deg.get(key);
				if(in==null){
					in = 0;
				}
				Integer out = out_deg.get(key);
				if(out==null){
					out = 0;
				}
				w.write(key+"\t"+in+"\t"+out+"\n");
			}
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void outputRDFdirAsText(String dir, String outfile){

		File f = new File(dir);
		int limit = 10000000; int c = 0;
		try {
			FileWriter w = new FileWriter(outfile, true);
			w.write("gene\tweight\ttarget\ttargetType\n");
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(File t: f.listFiles()){
			c++;
			Model m = getBaseModel();
			m.read("file:"+t.getAbsolutePath());
			boolean loops = true; boolean outs = false; boolean ins = false;
			String out = outputLinkModelAsText(m, loops, outs, ins);
			out+=outputEditorModelAsText(m);
			try {
				FileWriter w = new FileWriter(outfile, true);
				w.write(out);
				w.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			m.close();
			if(c%100==0){
				System.out.println("Finished writing: "+c);
			}
			if(c==limit){
				break;
			}
		}

		//		System.out.println("gene\tweight\ttarget\ttargetType");
		//		System.out.println(out);
	}

	public static void getAllGeneWikiEditorsLinksAsRDF(String outfolder){
		String credfile = "/Users/bgood/workspace/Config/gw_creds.txt";
		Map<String, String> creds = GeneWikiUtils.read2columnMap(credfile);
		RevisionCounter rc = new RevisionCounter(creds.get("wpid"), creds.get("wppw"));
		//get gene wiki
		WikiCategoryReader r = new WikiCategoryReader(creds);
		List<Page> pages = r.getPagesWithPBB(1000000, 500);
		int t = pages.size(); int c = 0;
		System.out.println("Processing "+t+" gene wiki articles");
		//store
		for(Page page : pages){
			c++;
			//check if exists
			String title = page.getTitle();
			File f = new File(outfolder+title);
			if(!f.exists()){
				Model m = ModelFactory.createDefaultModel();
				m = extractRelatedEditors(m,title, rc);
				m = extractLinksAsRDF(title, m);
				try{
					FileOutputStream fout=new FileOutputStream(outfolder+title.replace("/", "-"));
					m.write(fout);
					fout.close();
					m.close();
				}catch(IOException e){
					System.out.println("Exception caught"+e.getMessage());
				}
			}else{
				System.out.println(title+" exists");
			}
			if(c%100==0){
				System.out.println("Completed: "+c);
			}
		}
	}

	public static String outputLinkModelAsText(Model m, boolean loops, boolean outs, boolean ins){
		//gene	weight	target	targetType
		//first get linked forward and back
		Set<String> nodups = new HashSet<String>();
		String queryString = 
			"PREFIX ex: <http://example.org/> " +
			"PREFIX DC: <http://purl.org/dc/elements/1.1/> " +
			"PREFIX FOAF: <http://xmlns.com/foaf/0.1/> " +
			"PREFIX RDF: <"+RDF.getURI()+"> "+
			"PREFIX RDFS: <"+RDFS.getURI()+"> "+
			"SELECT distinct ?gene_l ?link_l "+ 
			"WHERE "+
			" { ?gene RDF:type ex:Gene . " +
			" ?gene ex:wikilink_out ?link . " +
			" ?gene ex:wikilink_in ?link . "+
			" ?link RDF:type ex:wikiconcept . " +
			" ?gene RDFS:label ?gene_l ." +
			" ?link RDFS:label ?link_l " +
			"}";

		Query query = QueryFactory.create(queryString);
		//		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, m);
		String out = "";
		if(loops){
			try{
				ResultSet rs = qe.execSelect();
				while(rs.hasNext()){
					QuerySolution rb = rs.nextSolution() ;
					String gene = rb.getLiteral("gene_l").getString();
					String link = rb.getLiteral("link_l").getString();
					out+=(gene+"\t2\t"+link+"\tlink_loop\t\n");
					nodups.add(gene+link);
				}
			}finally{
				qe.close();
			}
		}
		if(outs){
			queryString = "PREFIX ex: <http://example.org/> " +
			"PREFIX DC: <http://purl.org/dc/elements/1.1/> " +
			"PREFIX FOAF: <http://xmlns.com/foaf/0.1/> " +
			"PREFIX RDF: <"+RDF.getURI()+"> "+
			"PREFIX RDFS: <"+RDFS.getURI()+"> "+
			"SELECT distinct ?gene_l ?link_l "+ 
			"WHERE "+
			" { ?gene RDF:type ex:Gene . " +
			" ?gene ex:wikilink_out ?link . " +
			" ?link RDF:type ex:wikiconcept . " +
			" ?gene RDFS:label ?gene_l ." +
			" ?link RDFS:label ?link_l " +
			"}";

			query = QueryFactory.create(queryString);
			//		// Execute the query and obtain results
			qe = QueryExecutionFactory.create(query, m);
			try{
				ResultSet rs = qe.execSelect();
				while(rs.hasNext()){
					QuerySolution rb = rs.nextSolution() ;
					String gene = rb.getLiteral("gene_l").getString();
					String link = rb.getLiteral("link_l").getString();
					if(nodups.add(gene+link)){
						out+=(gene+"\t1\t"+link+"\tlink_out\t\n");
					}
				}
			}finally{
				qe.close();
			}
		}
		if(ins){
			queryString = "PREFIX ex: <http://example.org/> " +
			"PREFIX DC: <http://purl.org/dc/elements/1.1/> " +
			"PREFIX FOAF: <http://xmlns.com/foaf/0.1/> " +
			"PREFIX RDF: <"+RDF.getURI()+"> "+
			"PREFIX RDFS: <"+RDFS.getURI()+"> "+
			"SELECT distinct ?gene_l ?link_l "+ 
			"WHERE "+
			" { ?gene RDF:type ex:Gene . " +
			" ?gene ex:wikilink_in ?link . " +
			" ?link RDF:type ex:wikiconcept . " +
			" ?gene RDFS:label ?gene_l ." +
			" ?link RDFS:label ?link_l " +
			"}";

			query = QueryFactory.create(queryString);
			//		// Execute the query and obtain results
			qe = QueryExecutionFactory.create(query, m);
			try{
				ResultSet rs = qe.execSelect();
				while(rs.hasNext()){
					QuerySolution rb = rs.nextSolution() ;
					String gene = rb.getLiteral("gene_l").getString();
					String link = rb.getLiteral("link_l").getString();
					if(nodups.add(gene+link)){
						out+=(gene+"\t1\t"+link+"\tlink_in\t\n");
					}
				}
			}finally{
				qe.close();
			}
		}
		return out;
	}

	public static Model extractLinksAsRDF(String title, Model m){
		Property hlink_out = m.getProperty("http://example.org/wikilink_out");
		Property hlink_in = m.getProperty("http://example.org/wikilink_in");
		Resource genetype = m.getResource("http://example.org/Gene");
		Resource wikiconcept = m.getResource("http://example.org/wikiconcept");

		GeneWikiPage page = new GeneWikiPage(title);
		page.retrieveAllOutBoundWikiLinks(false);
		page.retrieveAllInBoundWikiLinks(true, false);

		if(page!=null&&page.getGlinks()!=null&&page.getGlinks().size()>0){
			String sourcetitle = page.getTitle();
			try {
				sourcetitle = URLEncoder.encode(sourcetitle, "utf-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Resource genepage = m.createResource("http://dbpedia.org/resource/"+sourcetitle);
			genepage.addProperty(RDF.type, genetype);	
			genepage.addLiteral(RDFS.label, title);

			for(GeneWikiLink link : page.getGlinks()){
				String objecttitle = link.getTarget_page();
				try{
					objecttitle = URLEncoder.encode(objecttitle, "utf-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Resource object = m.createResource("http://dbpedia.org/resource/"+objecttitle);
				genepage.addProperty(hlink_out, object);
				object.addProperty(RDF.type, wikiconcept);
				object.addLiteral(RDFS.label, link.getTarget_page());
			}
			for(GeneWikiLink link : page.getInglinks()){
				String subjecttitle = link.getTarget_page();
				try{
					subjecttitle = URLEncoder.encode(subjecttitle, "utf-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Resource linksource = m.createResource("http://dbpedia.org/resource/"+subjecttitle);
				genepage.addProperty(hlink_in, linksource);
				linksource.addProperty(RDF.type, wikiconcept);
				linksource.addLiteral(RDFS.label, link.getTarget_page());
			}

		}else{
			//System.out.println("Nothing found for: "+page.getData().getTitle());
		}
		return m;
	}

	public static String outputEditorModelAsText(Model m){
		//gene	weight	target	targetType
		String queryString = 
			"PREFIX ex: <http://example.org/> " +
			"PREFIX DC: <http://purl.org/dc/elements/1.1/> " +
			"PREFIX FOAF: <http://xmlns.com/foaf/0.1/> " +
			"PREFIX RDFS: <"+RDFS.getURI()+"> " +
			"SELECT ?gene_l ?user ?edits "+ 
			"WHERE "+
			" { ?event DC:subject ?gene . " +
			" ?event DC:contributor ?user . " +
			" ?event ex:weight ?edits ." +
			" ?gene RDFS:label ?gene_l" +
			"} ";
		String out = "";
		Query query = QueryFactory.create(queryString);
		//		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, m);

		try{
			ResultSet rs = qe.execSelect();
			while(rs.hasNext()){
				QuerySolution rb = rs.nextSolution() ;
				String gene = rb.getLiteral("gene_l").getString();
				Resource user = rb.getResource("user");
				int edits = rb.getLiteral("edits").getInt();
				out+=(gene+"\t"+edits+"\t"+user.getLocalName()+"\teditor\t\n");
			}
		}finally{
			qe.close();
		}
		return out;
	}

	public static Model extractRelatedEditors(Model m, String title, RevisionCounter rc){
		Calendar latest = Calendar.getInstance();
		Calendar earliest = Calendar.getInstance();
		earliest.add(Calendar.YEAR, -10);
		List<GWRevision> revs = rc.getRevisions(latest, earliest, title, false, null);
		if(revs!=null&&revs.size()>0){
			String safegene = title;
			try {
				safegene = URLEncoder.encode(title, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Resource gene = m.createResource("http://dbpedia.org/resource/"+safegene);
			Resource genetype = m.getResource("http://example.org/Gene");
			gene.addProperty(RDF.type, genetype);
			gene.addLiteral(RDFS.label, title);
			Resource geneuser = m.getResource("http://example.org/page_edit_r");
			Property weight = m.getProperty("http://example.org/weight");

			Map<String, Integer> user_edits = new HashMap<String, Integer>();
			for(GWRevision r : revs){
				String user = r.getUser().replace(" ", "_");
				user = TextFun.ip2anon(user);
				if(user.toLowerCase().contains("bot")){
					user = "AllBots";
				}
				//users by total edits
				Integer edits = user_edits.get(user);
				if(edits==null){
					edits = new Integer(0);
				}
				edits++;
				user_edits.put(user, edits);
			}

			for(Entry<String, Integer> user_edit : user_edits.entrySet()){
				//editor_network.write(title+"\t"+user_edit.getValue()+"\t"+user_edit.getKey()+"\n");
				String safeuser = user_edit.getKey().replace(" ", "_");
				Resource user = m.createResource("http://example.org/"+safeuser);
				user.addProperty(RDF.type, FOAF.Person);
				Resource event = m.createResource("http://example.org/"+safegene+"+"+safeuser);
				event.addProperty(RDF.type, geneuser);
				event.addProperty(DC.contributor, user);
				event.addProperty(DC.subject, gene);
				event.addLiteral(weight, user_edit.getValue());
			}
		}			

		return m;
	}

	/**
	 * Classes; genes, people, reified connection between genes and people
	 * Properties; contributor (genes_people), weight (anything to a number) 	
	 * @return Model an RDF representation of these entities
	 */
	public static Model getBaseModel(){
		Model m = ModelFactory.createDefaultModel();
		Resource genetype = m.createResource("http://example.org/Gene");
		genetype.addProperty(RDF.type, RDFS.Class);
		Resource wikiconcept = m.createResource("http://example.org/WikiConcept");
		wikiconcept.addProperty(RDF.type, RDFS.Class);
		Resource edited_page = m.createResource("http://example.org/page_edit_r");
		edited_page.addProperty(RDF.type, RDFS.Class);
		Property weight = m.createProperty("http://example.org/weight");
		Property hyperlink = m.createProperty("http://example.org/hyperlink_out");
		Property hyperlink_ = m.createProperty("http://example.org/hyperlink_in");
		return m;
	}	


	public static void getTagCloudText(String outfile, int limit){
		String article_names = "/users/bgood/data/wikiportal/facebase_genes.txt";
		Set<String> gwtitles = FileFun.readOneColFile(article_names);
		//	Set<String> titles = new HashSet<String>();
		//	WikiCategoryReader r = new WikiCategoryReader();
		//	int batch = 500;
		//	if(limit<batch){
		//		batch = limit;
		//	}
		//	List<Page> pages1 = r.getPagesWithTemplate("Template:GNF_Protein_box", limit, batch);
		//		for(Page page : pages1){
		//			titles.add(page.getTitle());
		//		}
		//	String article_name_file = "/users/bgood/data/wikiportal/facebase_genes.txt";
		//	Set<String> titles = FileFun.readOneColFile(article_name_file);
		int c = 0;
		//		List<GeneWikiPage> pages = new ArrayList<GeneWikiPage>();
		for(String title : gwtitles){
			GeneWikiPage page = new GeneWikiPage();
			page.setTitle(title);
			System.out.println("processing "+title);
			boolean gotext = page.defaultPopulate();
			try {
				FileWriter f = new FileWriter(outfile, true);				
				for(Sentence s : page.getSentences()){
					f.write(s.getPrettyText());			
				}
				f.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static void buildTabSemanticNetworkFromCachedMinedAnnotations(String outfile){
		String article_name_file = "/users/bgood/data/wikiportal/facebase_genes.txt";
		Set<String> titles = FileFun.readOneColFile(article_name_file);
		CandidateAnnotations cannolist = new CandidateAnnotations();
		String annos = "/Users/bgood/data/genewiki_jan_2011/intermediate/text-mined-annos.txt";
		cannolist.loadAndFilterCandidateGOAnnotations(annos, false);
		List<CandidateAnnotation> testcannos = cannolist.getCannos();
		try {
			FileWriter f = new FileWriter(outfile);
			for(CandidateAnnotation canno : testcannos){
				String title = canno.getSource_wiki_page_title();
				//	if(titles.contains(title)){
				f.write(title+"\tGO\t"+canno.getTarget_preferred_term()+"\n");
				//	}
			}
			cannolist.setCannos(null);
			cannolist.loadAndFilterCandidateDOAnnotations(annos);
			testcannos = cannolist.getCannos();
			for(CandidateAnnotation canno : testcannos){
				String title = canno.getSource_wiki_page_title();
				//	if(titles.contains(title)){
				f.write(title+"\tDO\t"+canno.getTarget_preferred_term()+"\n");
				//	}
			}
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void extractLinkNetwork(String outfile, int limit){
		//		String article_names = "/users/bgood/data/wikiportal/gene_wiki_titles.txt";
		//		Set<String> gwtitles = FileFun.readOneColFile(article_names);
		Set<String> titles = new HashSet<String>();
		WikiCategoryReader r = new WikiCategoryReader("/Users/bgood/workspace/Config/gw_creds.txt");
		int batch = 500;
		if(limit<batch){
			batch = limit;
		}
		List<Page> pages1 = r.getPagesWithTemplate("Template:GNF_Protein_box", limit, batch);
		for(Page page : pages1){
			titles.add(page.getTitle());
		}
		//	String article_name_file = "/users/bgood/data/wikiportal/facebase_genes.txt";
		//	Set<String> titles = FileFun.readOneColFile(article_name_file);
		int c = 0;
		//		List<GeneWikiPage> pages = new ArrayList<GeneWikiPage>();
		for(String title : titles){
			GeneWikiPage page = new GeneWikiPage();
			page.setTitle(title);
			System.out.println("processing "+title);
			boolean gotext = page.defaultPopulate();
			//			if(gotext){
			//				page.retrieveAllInBoundWikiLinks(true, false);
			//			}
			if(page!=null&&page.getGlinks()!=null&&page.getGlinks().size()>0){
				for(GeneWikiLink link : page.getGlinks()){
					c++;
					String target = link.getTarget_page();
					String edge = "link";
					if(titles.contains(target)){
						edge = "gene";
					}try {
						FileWriter f = new FileWriter(outfile, true);
						f.write(page.getTitle()+"\t"+edge+"\t"+target+"\n");
						f.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//					for(GeneWikiLink link : page.getInglinks()){
				//						c++;
				//						String target = link.getTarget_page();
				//						String edge = "link";
				//						if(gwtitles.contains(target)){
				//							edge = "gene";
				//						}
				//						f.write(target.replaceAll(" ","_")+"\t"+edge+"\t"+page.getTitle()+"\tIncoming Link\n");
				//					}
				System.out.println(page.getTitle()+" "+c);
			}else{
				System.out.println("Nothing found for: "+page.getTitle());
			}
		}

	}

	public static void extractAllLinksAsRDF(String outfile){
		Model m = ModelFactory.createDefaultModel();
		Property ass = m.createProperty("http://example.org/associated_with");
		String uriroot = "http://example.org/";
		Resource genetype = m.createResource(uriroot+"Gene");
		genetype.addProperty(RDF.type, RDFS.Class);

		List<GeneWikiPage> pages = GeneWikiUtils.loadSerializedDir(Config.gwikidir, 1000000);
		int c = 0;
		for(GeneWikiPage page : pages){
			if(page!=null&&page.getGlinks()!=null&&page.getGlinks().size()>0){
				String sourcetitle = page.getTitle();
				try {
					sourcetitle = URLEncoder.encode(sourcetitle, "utf-8");
					Resource subject = m.createResource(uriroot+sourcetitle);
					subject.addProperty(RDF.type, genetype);
					//add labels
					for(String r : page.getWikisynset()){
						subject.addLiteral(RDFS.label, r);
						if(r.equalsIgnoreCase("fibronectin")){
							System.out.println(subject);
						}
					}				
					//					for(GeneWikiLink link : page.getGlinks()){
					//						c++;
					////						String objecttitle = link.getTarget_page().getWikisynset().iterator().next();
					////						objecttitle = URLEncoder.encode(objecttitle, "utf-8");
					////						Resource object = m.createResource(uriroot+objecttitle);
					////						
					////						for(String r : link.getTarget_page().getWikisynset()){
					////							object.addLiteral(RDFS.label, r);
					////						}
					//						Statement asslink = m.createStatement(subject, ass, object);
					//						m.add(asslink);
					//						if(c%10000==0){
					//							System.out.println(c+" "+asslink);
					//						}
					//					}
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				//System.out.println("Nothing found for: "+page.getData().getTitle());
			}
		}
		System.out.println("Done with "+c+" writing");
		//write it all out
		try{
			FileOutputStream fout=new FileOutputStream(outfile);
			m.write(fout);
		}catch(IOException e){
			System.out.println("Exception caught"+e.getMessage());
		}
	}
}
