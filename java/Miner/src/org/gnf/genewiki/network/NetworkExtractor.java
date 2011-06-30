package org.gnf.genewiki.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
import org.gnf.ncbo.Ontologies;
import org.gnf.ncbo.web.AnnotatorClient;
import org.gnf.ncbo.web.NcboAnnotation;
import org.gnf.umls.SemanticType;
import org.gnf.umls.TypedTerm;
import org.gnf.umls.UmlsDb;
import org.gnf.umls.metamap.MMannotation;
import org.gnf.umls.metamap.MetaMap;
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
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


/***
 * Class to handle extraction of structured representations (RDF) of the gene wiki
 * @author bgood
 *
 */
public class NetworkExtractor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String datadir = "/Users/bgood/data/network/test/";
		//getAllGeneWikiEditorsLinksAsRDF(datadir);
		//outputRDFdirAsText(datadir, "/Users/bgood/data/network/all_gw_loops_editors.txt");
		//typeLinksWithMetamap(datadir, "/Users/bgood/data/network/mapped_links.txt");
		summarizeNetwork(datadir, "/Users/bgood/data/network/node_degrees.txt", "/Users/bgood/data/network/mapped_links.txt");

	}
//TODO rewrite this in a non stupid way..
	public static void summarizeNetwork(String rdfdir, String outfile, String link_type_file){
		File f = new File(rdfdir);
		int limit = 100000; int c = 0;
		Map<String, Integer> in_deg = new HashMap<String, Integer>();
		Map<String, Integer> out_deg = new HashMap<String, Integer>();
		Map<String, List<TypedTerm>> typed_links = getTypedLinks(link_type_file, 860);
		try {
			FileWriter w = new FileWriter(outfile);
			w.write("Type\turi\tin-degree\tout-degree\n");
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(File t: f.listFiles()){
			if(t.getName().startsWith(".")){
				continue;
			}
			c++;
			Model m = getBaseModel();
			m.read("file:"+t.getAbsolutePath());
			//	Resource g = m.getResource("http://dbpedia.org/resource/"+t.getName());
			Resource G = m.getResource("http://example.org/Gene");
			ResIterator gs = m.listResourcesWithProperty(RDF.type, G);
			while(gs.hasNext()){
				Resource g = gs.next();
				StmtIterator s = g.listProperties(m.getProperty("http://example.org/wikilink_in"));
				int in = 0;
				if(s!=null){
					List<Statement> ss = s.toList();
					in = ss.size();
					if(in>0){
						for(Statement st : ss){
							Resource l = (Resource)st.getObject();
							Integer link_d = out_deg.get("link\t"+l.getURI());
							if(link_d == null){
								link_d = 0;
							}
							link_d++;
							try {
								String luri = URLDecoder.decode(l.getURI(), "UTF-8");
								out_deg.put("link\t"+luri, link_d);
							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}
				}
				in_deg.put("gene\t"+g.getURI(), in);
				s = g.listProperties(m.getProperty("http://example.org/wikilink_out"));
				int out = 0;
				if(s!=null){
					List<Statement> ss = s.toList();
					out = ss.size();
					if(in>0){
						for(Statement st : ss){
							Resource l = (Resource)st.getObject();
							Integer link_d = in_deg.get("link\t"+l.getURI());
							if(link_d == null){
								link_d = 0;
							}
							link_d++;
							try {
								String luri = URLDecoder.decode(l.getURI(), "UTF-8");
								in_deg.put("link\t"+luri, link_d);
							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
				out_deg.put("gene\t"+g.getURI(), out);
				//editors
				String uri = g.getURI();
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
		}
		try {
			FileWriter w = new FileWriter(outfile);
			w.write("Type\turi\tin-degree\tout-degree\tsemantic_type\n");
			Set<String> keys = new HashSet<String>(out_deg.keySet());
			keys.addAll(in_deg.keySet());
			for(String key : keys){
				key = key.replaceAll("_", " ");
				//skip links that are genes
				String uri = key.split("\t")[1];
				String thing = "";
				if(uri.startsWith("http://db")){
					thing = uri.substring("http://dbpedia.org/resource/".length());
				}else if(key.startsWith("user")){
					thing = uri.substring(uri.lastIndexOf("/"));
				}
				if(key.startsWith("link")){
					String test = "gene\t"+uri;
					if(keys.contains(test)){
						continue;
					}
				}
				Integer in = in_deg.get(key);
				if(in==null){
					in = 0;
				}
				Integer out = out_deg.get(key);
				if(out==null){
					out = 0;
				}
				List<TypedTerm> ttlist = typed_links.get(thing);
				String typeinfo = "none";
				if(ttlist!=null){
					for(TypedTerm tt : ttlist){
						if(tt!=null&&tt.getTypes()!=null){
							typeinfo = "";
							for(SemanticType st : tt.getTypes()){
								typeinfo+=st.getStype_name()+" | "+st.getSgroup_name()+" ; ";
							}
						}
					}
				}
				w.write(key.split("\t")[0]+"\t"+thing+"\t"+in+"\t"+out+"\t"+typeinfo+"\t\n");

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
			if(t.getName().startsWith(".")){
				continue;
			}
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

	public static void typeLinksWithMetamap(String dir, String outfile){
		File f = new File(dir);
		int limit = 10000; int c = 0;

		Set<String> linktext = new HashSet<String>();
		for(File t: f.listFiles()){
			if(t.getName().startsWith(".")){
				continue;
			}
			c++;
			Model m = getBaseModel();
			Resource wikiconcept = m.getResource("http://example.org/wikiconcept");
			m.read("file:"+t.getAbsolutePath());
			StmtIterator stmt = m.listStatements((Resource)null, RDF.type, wikiconcept);
			while(stmt.hasNext()){
				Statement s = stmt.nextStatement();
				String link = s.getSubject().getLocalName().replace("_", " ");
				try {
					link = URLDecoder.decode(link, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(link!=""&&link.length()>2){
					linktext.add(link);
				}
			}
			m.close();
			if(c%100==0){
				System.out.println("Finished reading: "+c);
			}
			if(c==limit){
				break;
			}
		}
		//now go get the concepts associated with these stupid words
		System.out.println("N links = "+linktext.size());
		MetaMap mm = new MetaMap();
		UmlsDb d = new UmlsDb();
		c = 0;
		try {
			Map<String,List<TypedTerm>> getTypedLinks = getTypedLinks(outfile, 860);
			FileWriter out = new FileWriter(outfile);
			for(String link : linktext){
				if(!getTypedLinks.containsKey(link)){
					List<MMannotation> cs = mm.getCUIsFromText(link, null, true, link); //"GO,FMA,SNOMEDCT"		
					if(cs!=null&&cs.size()>0){
						for(MMannotation anno : cs){
							out.write(link+"\t"+anno.getScore()+"\t"+anno.getCui()+"\t"+anno.getTermName()+"\t");
							for(String abbr : anno.getSemanticTypes()){
								String[] type_i = d.getSemanticTypeInfoFromAbbreviation(abbr).split("\t");
								out.write(abbr+"|"+type_i[0]+"|"+d.getGroupForStype(type_i[1])+";");
							}
							out.write("\n");
						}
					}else{
						out.write(link+"	0	none	none	none	none	none\n");
					}
				}
				c++;
				if(c%100==0){
					System.out.println(c+" "+link);
				}
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static Map<String,List<TypedTerm>> getTypedLinks(String linkfile, int minscore){
		Map<String,List<TypedTerm>> typed_terms = new HashMap<String,List<TypedTerm>>();
		File fc = new File(linkfile);
		if(!fc.canRead()){
			return typed_terms;
		}
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(linkfile));
			String line = f.readLine();
			while(line!=null){
				String[] pieces = line.split("\t");
				TypedTerm t = new TypedTerm(pieces[0], pieces[1], pieces[2], pieces[3], pieces[4]);
				if(-1*t.getScore()>minscore){
					List<TypedTerm> tlist = typed_terms.get(pieces[0]);
					if(tlist==null){
						tlist = new ArrayList<TypedTerm>();
					}
					tlist.add(t);
					typed_terms.put(pieces[0], tlist);
				}
				line = f.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return typed_terms;
	}

	public static Model extractLinksAsRDF(String title, Model m){
		Property hlink_out = m.getProperty("http://example.org/wikilink_out");
		Property hlink_in = m.getProperty("http://example.org/wikilink_in");
		Resource genetype = m.getResource("http://example.org/Gene");
		Resource wikiconcept = m.getResource("http://example.org/wikiconcept");

		GeneWikiPage page = new GeneWikiPage(title);
		page.defaultPopulate();
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
		Resource wikiconcept = m.createResource("http://example.org/wikiconcept");
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
