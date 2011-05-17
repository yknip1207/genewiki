package org.gnf.genewiki.rdf;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gnf.genewiki.Config;
import org.gnf.genewiki.GeneWikiLink;
import org.gnf.genewiki.Sentence;
import org.gnf.genewiki.WikiCategoryReader;
import org.gnf.genewiki.Workflow;
import org.gnf.genewiki.GeneWikiPage;
import org.gnf.genewiki.GeneWikiUtils;
import org.gnf.genewiki.associations.CandidateAnnotation;
import org.gnf.genewiki.associations.CandidateAnnotations;
import org.gnf.go.GOmapper;
import org.gnf.util.FileFun;
import org.gnf.wikiapi.Page;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


/***
 * Class to handle conversion of extracted associations to simple rdf or tab formats
 * @author bgood
 *
 */
public class LinkNetworkExporter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//String file = "/Users/bgood/data/wikiportal/fb_denver/all_gw_network.txt";
		//buildTabLinkNetwork(file, 500000);

//		String file = "/Users/bgood/data/wikiportal/fb_denver/all_gw_mined_network.txt";
//		buildTabSemanticNetwork(file);
		
		String file = "/Users/bgood/data/wikiportal/fb_denver/all_gene_wiki_text_for_fb.txt";
		getTagCloudText(file, 100);
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
	
	public static void buildTabSemanticNetwork(String outfile){
		String article_name_file = "/users/bgood/data/wikiportal/facebase_genes.txt";
		Set<String> titles = FileFun.readOneColFile(article_name_file);
		CandidateAnnotations cannolist = new CandidateAnnotations();
		String annos = "/Users/bgood/data/genewiki_jan_2011/intermediate/text-mined-annos.txt";
		cannolist.loadAndFilterCandidateGOAnnotations(annos);
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

	public static void buildTabLinkNetwork(String outfile, int limit){
		//		String article_names = "/users/bgood/data/wikiportal/gene_wiki_titles.txt";
		//		Set<String> gwtitles = FileFun.readOneColFile(article_names);
		Set<String> titles = new HashSet<String>();
		WikiCategoryReader r = new WikiCategoryReader();
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

	public static void dumpAllLinksAsRDF(String outfile){
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
