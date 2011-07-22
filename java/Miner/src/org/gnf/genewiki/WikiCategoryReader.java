package org.gnf.genewiki;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gnf.genewiki.metrics.RevisionCounter;
import org.gnf.genewiki.parse.ParserAccess;
import org.gnf.wikiapi.Category;
import org.gnf.wikiapi.Connector;
import org.gnf.wikiapi.Page;
import org.gnf.wikiapi.User;



/**
 * This class contains some examples of using the Wikipedia API to identify lists of pages based on category membership
 * @author bgood
 *
 */
public class WikiCategoryReader {
	User user;
	public WikiCategoryReader(){
		
	}
	
	public WikiCategoryReader(Map<String, String> creds){
		init(creds.get("wpid"), creds.get("wppw"));
	}
	
	public WikiCategoryReader(String credfile){
//		String credfile = "/Users/bgood/workspace/Config/gw_creds.txt";
		Map<String, String> creds = GeneWikiUtils.read2columnMap(credfile);
		init(creds.get("wpid"), creds.get("wppw"));
	}

	public WikiCategoryReader(String wikiapi, String credfile){
	//	String credfile = "/Users/bgood/workspace/Config/gw_creds.txt";
		Map<String, String> creds = GeneWikiUtils.read2columnMap(credfile);
		init(creds.get("wpid"), creds.get("wppw"), wikiapi);
	}
	
	public void init(String wikipedia_user, String wikipedia_password){
		user = new User(wikipedia_user, wikipedia_password, "http://en.wikipedia.org/w/api.php");
		user.login();
	}
	
	public void init(String wiki_user, String wiki_password, String apiurl){
		user = new User(wiki_user, wiki_password, apiurl);
		user.login();
	}
	
	public static void main(String[] args){
		WikiCategoryReader r = new WikiCategoryReader("/Users/bgood/workspace/Config/gw_creds.txt");
		//Genes_by_human_chromosome
		List<Page> pages = r.listPagesByCategory(1000000, 500, "Human proteins"); //Genes_on_chromosome_1 Genes_by_human_chromosome
		System.out.println(pages.size()+" "+pages.get(3).getTitle());
	}

/**
 * Queries wikipedia for all the pages that use the PBB template, retrieves their text, parses the NCBI gene id out of the PBB template header 	
 * @param max
 * @param batchsize
 */
	public void buildGeneWikiIndex(int max, int batchsize){
		List<Page> pages = getPagesWithPBB(max, batchsize);
		for(Page page : pages){
			GeneWikiPage p = new GeneWikiPage(page.getTitle());
			p.retrieveWikiTextContent(false);
			p.parseAndSetNcbiGeneId();
			System.out.println(p.getTitle()+"\t"+p.getNcbi_gene_id());
		}
	}
	
	
	/**
	 * Retrieve (mostly unpopulated) Pages that use the PBB template.  A page is 'in' in the Gene Wiki if it uses this template.
	 */
	public  List<Page> getPagesWithTemplate(String template_name, int total, int batch){
			List<Page> pages = new ArrayList<Page>();
			//note only (main namespace)
			String nextTitle = "";
			Connector connector = new Connector();
			ParserAccess parser = new ParserAccess();
			for(int i = 0; i< total; i+=batch){
				String[] categoryMembers = { 
						"list", "embeddedin", 
						"eititle", template_name, 
						"eifilterredir", "nonredirects",
						"eilimit", batch+"",
						"einamespace", "0",  //namespace = 10 equals the template namespace.
						"", ""
						};
				if(nextTitle!=null&&!nextTitle.equals("")){
					categoryMembers[10] = "eicontinue";
					categoryMembers[11] = nextTitle;
				}
				String pagesXML = connector.queryXML(user, categoryMembers);
				List<Page> page_batch = parser.parseWikiEmbeddedApiXml(pagesXML);
				nextTitle = parser.getNextTitle();
				if(page_batch!=null&&page_batch.size()>0){
					pages.addAll(page_batch);
			//		System.out.println("Done getting PBB batch "+i+" next title: "+nextTitle);
				}else{
					break;
				}
				if(nextTitle==null){
					break;
				}
			}
			return pages;
		}
	public  List<Page> getPagesWithPBB(int total, int batch){
		return getPagesWithTemplate("Template:GNF_Protein_box", total, batch);
	}
	
	
	public void getHumanProteinsAndStore(){
		WikiCategoryReader gr = new WikiCategoryReader("/Users/bgood/workspace/Config/gw_creds.txt");
		try {
			FileWriter writer = new FileWriter("data\\hairball_10000.txt");
			FileWriter attwriter = new FileWriter("data\\hairball_10000_att.txt");
			int total = 10000;
			int batch = 500;
			List<Page> prots = gr.listHumanProteins(total, batch);
			for(Page prot : prots){
				GeneWikiPage gprot = new GeneWikiPage(prot.getTitle());
				gprot.defaultPopulate();
				writer.write(gprot.toTabDelimited());
				attwriter.write(gprot.getTitle()+"\tgene\n");
				for(String go : gprot.getGo_ids()){
					attwriter.write(gprot.getTitle()+"\t"+go+"\n");
				}
			}
			attwriter.close();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<Page> listPagesByCategory(int total, int batch, String catname){
		List<Page> pages = new ArrayList<Page>();
		//note only (main namespace)
		String nextTitle = " ";
		Connector connector = new Connector();
		ParserAccess parser = new ParserAccess();
		for(int i = 0; i< total; i+=batch){
			String[] categoryMembers = { 
					"list", "categorymembers", 
					"cmtitle", "Category:"+catname, 
					"cmlimit", batch+"",
					"cmnamespace", "0",
					"cmcontinue",nextTitle
					};
			String pagesXML = connector.queryXML(user, categoryMembers);
			pages.addAll(parser.parseWikiCategoriesApiXml(pagesXML));
			nextTitle = parser.getNextTitle();
			if(nextTitle==null){
				break;
			}
			System.out.println(i+"  "+nextTitle);
		}
		return pages;
	}
	
	public List<Page> listHumanProteins(int total, int batch){
		List<Page> pages = new ArrayList<Page>();
		//note only (main namespace)
		String nextTitle = "";
		Connector connector = new Connector();
		ParserAccess parser = new ParserAccess();
		for(int i = 0; i< total; i+=batch){
			String[] categoryMembers = { 
					"list", "categorymembers", 
					"cmtitle", "Category:Human proteins", 
					"cmlimit", batch+"",
					"cmnamespace", "0",
					"cmcontinue",nextTitle};
			String pagesXML = connector.queryXML(user, categoryMembers);
			pages.addAll(parser.parseWikiCategoriesApiXml(pagesXML));
			nextTitle = parser.getNextTitle();
			System.out.println(i+"  "+nextTitle);
		}
		return pages;
	}

// Category:Genes_by_human_chromosome


	public static void testQueryCategories001() {
		String[] listOfTitleStrings = { "Main Page", "API" };
		User user = new User("", "", "http://meta.wikimedia.org/w/api.php");
		user.login();
		List<Page> listOfPages = user.queryCategories(listOfTitleStrings);
		for (Page page : listOfPages) {
			// print page information
			System.out.println(page.toString());
			for (int j = 0; j < page.sizeOfCategoryList(); j++) {
				Category cat = page.getCategory(j);
				// print every category in this page
				System.out.println(cat.toString());
			}
		}
	}

	public static void testQueryCategoryMembers() {
		User user = new User("", "", "http://en.wikipedia.org/w/api.php");
		user.login();
		String[] valuePairs = { "list", "categorymembers", "cmtitle", "Category:Physics" }; 
		Connector connector = new Connector();
		String rawXmlResponse = connector.queryXML(user, valuePairs);
		if (rawXmlResponse == null) {
			System.out.println("Got no XML result for the query");
		}
		System.out.println(rawXmlResponse);

		// When more results are available, use "cmcontinue" from last query result
		// to continue
		String[] valuePairs2 = { "list", "categorymembers", "cmtitle", "Category:Physics", "cmcontinue", "Awards|" };
		rawXmlResponse = connector.queryXML(user, valuePairs2);
		if (rawXmlResponse == null) {
			System.out.println("Got no XML result for the query");
		}
		System.out.println(rawXmlResponse);

	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}