package org.gnf.genewiki.network;

import java.io.BufferedWriter;
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
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
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
		//String file = "/Users/bgood/data/wikiportal/fb_denver/all_gw_network.txt";
		//buildTabLinkNetwork(file, 500000);

		//		String file = "/Users/bgood/data/wikiportal/fb_denver/all_gw_mined_network.txt";
		//		buildTabSemanticNetwork(file);

		//		String file = "/Users/bgood/data/wikiportal/fb_denver/all_gene_wiki_text_for_fb.txt";
		//		getTagCloudText(file, 100);

		Model m = getBaseModel();
		String credfile = "/Users/bgood/workspace/Config/gw_creds.txt";
		Map<String, String> creds = GeneWikiUtils.read2columnMap(credfile);
		RevisionCounter rc = new RevisionCounter(creds.get("wpid"), creds.get("wppw"));
		//loop
		String title1 = "ABCA4"; String title2 = "AXIN2";
		List<String> titles = new ArrayList<String>();
		titles.add(title1); titles.add(title2);
		for(String title : titles){
//			m = extractRelatedEditors(m,title, rc);
			m = extractLinksAsRDF(title, m);
		}
//		outputEditorModelAsText(m);
		outputLinkModelAsText(m);
		
	}

	public static void outputLinkModelAsText(Model m){
		String queryString = 
			"PREFIX ex: <http://example.org/> " +
			"PREFIX DC: <http://purl.org/dc/elements/1.1/> " +
			"PREFIX FOAF: <http://xmlns.com/foaf/0.1/> " +
			"PREFIX RDF: <"+RDF.getURI()+"> "+
			"SELECT ?gene ?link "+ 
			"WHERE "+
			" { ?gene RDF:type ex:Gene . " +
			"{ ?gene ex:hyperlink ?link } " +
			"UNION " +
			"{?link ex:hyperlink ?gene }" +
			"} ";

		Query query = QueryFactory.create(queryString);
		//		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, m);

		try{
			ResultSet rs = qe.execSelect();
			while(rs.hasNext()){
				QuerySolution rb = rs.nextSolution() ;
				Resource gene = rb.getResource("gene");
				Resource link = rb.getResource("link");
				System.out.println("WikiLink\t"+gene.getLocalName()+"\t"+link.getLocalName()+"\t");
			}
		}finally{
			qe.close();
		}
	}

	public static Model extractLinksAsRDF(String title, Model m){
		Property hlink = m.getProperty("http://example.org/hyperlink");
		Resource genetype = m.getResource("http://example.org/Gene");

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
			//add labels
			for(String r : page.getWikisynset()){
				genepage.addLiteral(RDFS.label, r);
			}				
			for(GeneWikiLink link : page.getGlinks()){
				String objecttitle = link.getTarget_page();
				try{
					objecttitle = URLEncoder.encode(objecttitle, "utf-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Resource object = m.createResource("http://dbpedia.org/resource/"+objecttitle);
				genepage.addProperty(hlink, object);
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
				linksource.addProperty(hlink, genepage);
			}

		}else{
			//System.out.println("Nothing found for: "+page.getData().getTitle());
		}
		return m;
	}

	public static void outputEditorModelAsText(Model m){
		String queryString = 
			"PREFIX ex: <http://example.org/> " +
			"PREFIX DC: <http://purl.org/dc/elements/1.1/> " +
			"PREFIX FOAF: <http://xmlns.com/foaf/0.1/>" +
			"SELECT ?gene ?user ?edits "+ 
			"WHERE "+
			" { ?event DC:subject ?gene . " +
			" ?event DC:contributor ?user . " +
			" ?event ex:weight ?edits " +
			"} ";

		Query query = QueryFactory.create(queryString);
		//		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, m);

		try{
			ResultSet rs = qe.execSelect();
			while(rs.hasNext()){
				QuerySolution rb = rs.nextSolution() ;
				Resource gene = rb.getResource("gene");
				Resource user = rb.getResource("user");
				int edits = rb.getLiteral("edits").getInt();
				System.out.println(gene.getLocalName()+"\t"+user.getLocalName()+"\t"+edits);
			}
		}finally{
			qe.close();
		}
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
		Resource edited_page = m.createResource("http://example.org/page_edit_r");
		edited_page.addProperty(RDF.type, RDFS.Class);
		Property weight = m.createProperty("http://example.org/weight");
		Property hyperlink = m.createProperty("http://example.org/hyperlink");
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
