package org.gnf.genewiki;

import org.gnf.wikiapi.Category;
import org.gnf.wikiapi.Connector;
import org.gnf.wikiapi.Link;
import org.gnf.wikiapi.Page;
import org.gnf.wikiapi.Revision;
import org.gnf.wikiapi.User;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gnf.genewiki.parse.JdomBackLinks;
import org.gnf.genewiki.parse.JdomExtLinks;
import org.gnf.genewiki.parse.JdomForwardLinks;
import org.gnf.genewiki.parse.ParseUtils;
import org.gnf.genewiki.parse.ParserAccess;
import org.gnf.genewiki.lingpipe.SentenceSplitter;
import org.gnf.genewiki.trust.WikiTrustBlock;
import org.gnf.genewiki.trust.WikiTrustClient;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDFS;



/****
 * GeneWikiPage contains methods to:
 * 		retrieve articles from Wikipedia and convert them into Java objects
 *		Serialize/Deserialize these objects as Java bytecode
 *		parse links, sentences, references, headings, gene identifiers from Wikipedia text
 * 		parse authors, trust information from WikiTrust markup
 * @author bgood
 *
 */
public class GeneWikiPage implements Serializable, Comparable{

	//transient means we are not going to save these fields when we serialize objects of this class
	transient User user;
	transient ParserAccess parser;
	transient Page wikidetails; 

	private static final long serialVersionUID = 7752881885149904262L;

	//page info
	String title;
	String pageid;

	//revision info
	String revid;
	String parentid;
	String lasteditor;
	int size;
	String timestamp;
	String pageContent;
	String lastComment;

	//use wikitrust text
	boolean useTrust;
	//trust score for whole page
	double pageTrust;


	//extracted information from page, redirects etc.
	List<GeneWikiLink> glinks;
	List<GeneWikiLink> inglinks;
	List<String> extlinks;
	Set<String> wikisynset;
	String redirects_to;
	String ncbi_gene_id;
	String gene_symbol;
	List<String> go_ids;
	String uniprot_id;
	boolean redirect_checked;

	List<Reference> refs;
	List<Sentence> sentences;
	List<Heading> headings;
	List<Category> categories;

	public GeneWikiPage(){
		init("","");
	}

	public GeneWikiPage(GWRevision rev, User u, boolean extract_info){
		user = u;
		revid = rev.getRevid();
		parentid = rev.getParentid();
		lasteditor = rev.getUser();
		size = rev.getSize();
		timestamp = rev.getTimestamp();
		pageContent = rev.getContent();
		title = rev.getTitle();
		lastComment = rev.getComment();

		if(extract_info){
			parser = new ParserAccess();
			glinks = new ArrayList<GeneWikiLink>();
			extlinks = new ArrayList<String>();
			wikisynset = new HashSet<String>();
			refs = new ArrayList<Reference>();
			sentences = new ArrayList<Sentence>();
			headings = new ArrayList<Heading>();

			parseAndSetNcbiGeneId();
			//	parseAndSetGeneSymbol();
			parseAndSetSentences();
			setReferences();
			setHeadings();
			setGeneWikiLinksByHeadings();
			retrieveAllOutBoundHyperLinks();
		}

	}

	public GeneWikiPage(User u){
		user = u;
		user.login();
		parser = new ParserAccess();
		glinks = new ArrayList<GeneWikiLink>();
		inglinks = new ArrayList<GeneWikiLink>();
		extlinks = new ArrayList<String>();
		go_ids  = new ArrayList<String>();
		wikisynset = new HashSet<String>();
		ncbi_gene_id = "";
		uniprot_id = "";
		redirects_to = null;
		redirect_checked = false;
		wikidetails = new Page();
		refs = new ArrayList<Reference>();
	}

	public GeneWikiPage(String title) {
		init("","");
		this.title = title;
	}

	public GeneWikiPage(String username, String password) {
		init(username, password);
	}

	public void init(String username, String password){
		user = new User(username, password, "http://en.wikipedia.org/w/api.php");
		user.login();
		parser = new ParserAccess();
		glinks = new ArrayList<GeneWikiLink>();
		inglinks = new ArrayList<GeneWikiLink>();
		extlinks = new ArrayList<String>();
		go_ids  = new ArrayList<String>();
		wikisynset = new HashSet<String>();
		ncbi_gene_id = "";
		uniprot_id = "";
		redirects_to = null;
		redirect_checked = false;
		wikidetails = new Page();
		refs = new ArrayList<Reference>();
	}


	/**
	 * Display examples of the use of this class
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		//		String title = "CDKN1B"; 
		//		GeneWikiPage prot = new GeneWikiPage(title);
		//		boolean setredirects = true; boolean getAllWikiLinks = false; boolean getOnlyWikiLinksInText = false; boolean getHyperLinks = false;
		//		prot.populate(setredirects, getAllWikiLinks, getOnlyWikiLinksInText, getHyperLinks);
		//		System.out.println("Page id: "+prot.getWikidetails().getPageid()+" revision id: "+prot.getWikidetails().getCurrentRevision().getRevid()+" "+prot.getWikidetails().getCurrentRevision().getUser());
		//		String trust = WikiTrustClient.getTrustForWikiPage(prot.getPageid(), prot.getRevid());
		////		List<WikiTrustBlock> blocks = WikiTrustClient.getTrustBlocks(trust);
		////		System.out.println(blocks.size());
		////		System.out.println(WikiTrustClient.summarizeEditorTrust(blocks));
		//		System.out.println("page content: "+prot.getPageContent().length());
		//		System.out.println("wikitrust size :"+trust.length());
		//		String s = WikiTrustClient.zapTrust(trust);
		//		System.out.println("wikitrust zapped size :"+s.length());
		//		
		//		System.out.println(trust);
		//		

		//		prot.retrieveAllInBoundWikiLinks(true, false);

		testParseGeneWikiPage("Novartis");

	}

	public static void testParseGeneWikiPage(String title){
		GeneWikiPage prot = new GeneWikiPage();//GeneWikiUtils.deserializeGeneWikiPage("/Users/bgood/data/genewiki/intermediate/javaobj/1812"); //5354
		prot.setTitle(title);
		prot.defaultPopulateWikiTrust();

		System.out.println("Headings "+prot.getHeadings().size());
		for(Heading h : prot.getHeadings()){
			System.out.println(h.getPrettyText());
		}
		int refindex = prot.getPageContent().lastIndexOf("References");
		
		System.out.println("Sentences "+prot.getSentences().size());
		System.out.println("References "+prot.getRefs().size());
		System.out.println("ext links "+prot.getExtlinks().size());
		
		for(String link : prot.getExtlinks()){
			System.out.println(link);
		}
		
		for (Sentence sentence : prot.getSentences()) {
			List<Reference> refs = prot.getRefsForSentence(sentence, refindex);
			System.out.println(sentence.getStartIndex()+"-"+sentence.getStopIndex()+" "+sentence.getPrettyText());
			for(Reference ref : refs){
				System.out.println("\t"+ref);
			}
		}
		

	}
	
	/**
	 * populates this page object with everything except for wikilinks that appear in templates 
	 * (only wikilinks that appear in the text are kept by default because templates are often used to import
	 * large numbers of links that aren't directly relevant to the pages they show up on)
	 * 
	 * Replaces the text content of the article with text marked up by WikiTrust
	 * @return
	 */
	public boolean defaultPopulateWikiTrust(){
		boolean setredirects = true; 
		boolean getAllWikiLinks = false; 
		boolean getWikiLinksInText = true; 
		boolean getHyperLinks = true;
		boolean setReferences = true;
		boolean setHeaders = true;
		boolean setSentences = true;
		boolean useWikiTrust = true;
		return controlledPopulate(setredirects, getAllWikiLinks, getWikiLinksInText, getHyperLinks, setReferences, setHeaders, setSentences, useWikiTrust);
	}

	/**
	 * populates this page object with everything except for wikilinks that appear in templates 
	 * (only wikilinks that appear in the text are kept by default because templates are often used to import
	 * large numbers of links that aren't directly relevant to the pages they show up on)
	 * @return
	 */
	public boolean defaultPopulate(){
		boolean setredirects = true; 
		boolean getAllWikiLinks = false; 
		boolean getWikiLinksInText = true; 
		boolean getHyperLinks = true;
		boolean setReferences = true;
		boolean setHeaders = true;
		boolean setSentences = true;
		boolean useWikiTrust = false;
		return controlledPopulate(setredirects, getAllWikiLinks, getWikiLinksInText, getHyperLinks, setReferences, setHeaders, setSentences, useWikiTrust);
	}

	/**
	 * populates this page object with everything except for wikilinks that appear in templates 
	 * (only wikilinks that appear in the text are kept by default because templates are often used to import
	 * large numbers of links that aren't directly relevant to the pages they show up on)
	 * @return
	 */
	public boolean defaultPopulateNoLinks(){
		boolean setredirects = true; 
		boolean getAllWikiLinks = false; 
		boolean getWikiLinksInText = false; 
		boolean getHyperLinks = false;
		boolean setReferences = true;
		boolean setHeaders = true;
		boolean setSentences = true;
		boolean useWikiTrust = false;
		return controlledPopulate(setredirects, getAllWikiLinks, getWikiLinksInText, getHyperLinks, setReferences, setHeaders, setSentences, useWikiTrust);
	}



	/**
	 * Retrieves page data from wikipedia and, depending on options selected, parses out or collects additional features 
	 * @param setredirects
	 * @param getAllWikiLinks
	 * @param getWikiLinksInText
	 * @param getHyperLinks
	 * @param setReferences
	 * @param setHeaders
	 * @param setSentences
	 * @return
	 */
	public boolean controlledPopulate(
			boolean setredirects, 
			boolean getAllWikiLinks, 
			boolean getWikiLinksInText, 
			boolean getHyperLinks,
			boolean setReferences,
			boolean setHeaders,
			boolean setSentences,
			boolean useWikiTrust){
		//always want  see if you are a redirect
		setRedirect();
		//assume we always want the text
		boolean gotext = false;
		if(useWikiTrust){
			gotext = setPageContentToWikiTrust();
			if(gotext==false){//in case we don't already have a revision id
				retrieveWikiTextContent(false);
				gotext = setPageContentToWikiTrust();
			}
		}else{
			gotext = retrieveWikiTextContent(false);
		}
		if(!gotext){
			return false;
		}
		if(setSentences){
			parseAndSetSentences();
		}
		if(setReferences){
			setReferences();
		}
		if(setHeaders){
			setHeadings();
		}		
		if(setredirects){
			setAllRedirects();
		}
		//this uses API which returns ~all links including those embedded as templates
		if(getAllWikiLinks){
			retrieveAllOutBoundWikiLinks(false);
		}
		//this specifically extracts wikilinks from the text
		else if(getWikiLinksInText){
			setGeneWikiLinksByHeadings();
		}
		//this extracts hyperlinks from text
		if(getHyperLinks){
			retrieveAllOutBoundHyperLinks();
		}
		return gotext;
	}

	/**
	 * This checks to see if the current page, as indicated by getTitle(), is itself a redirect page.
	 * If it is a redirect, then it sets the 'redirects to' and redirect_check fields appropriately
	 */
	public void setRedirect(){
		if(getTitle() == null){
			System.out.println("Title not set");
			return;
		}
		List<String> a = new ArrayList<String>();
		a.add(getTitle());
		Page p = null;
		String[] contentQuery = { 
				"prop", "revisions",
				//"redirects","true",
				"rvprop", "content"
		};

		Connector connector = user.getConnector();
		List<Page> pages = connector.query(user, a, contentQuery);
		if(pages!=null&&pages.size()==1){
			String content = pages.get(0).getCurrentContent();
			if(content != null){
				if(content.trim().startsWith("#REDIRECT")){
					String[] red = content.split("\\[\\[");
					redirects_to = red[1].replace(']',' ').trim();
				}
			}
		}else if(pages.size()>1){
			System.out.println("too many pages returned: "+pages.size());
		}else{
			System.out.println("No pages matched title: "+getTitle());
		}
		redirect_checked = true;
	}

	/***
	 * This gathers up all the different redirect pages that exist for the current page, as indicated by getTitle().
	 * The redirects are added as elements of wikisynset
	 * 
	 */
	public void setAllRedirects(){
		String prime = this.getTitle();
		if(redirect_checked && getRedirects_to()!=null){
			//then this page is a redirect page and its synset includes
			//itself, the page its redirected to (main) and all the pages that redirected to the main page
			wikisynset.add(getRedirects_to());
			prime = getRedirects_to();
		}
		//now get all incoming redirecting links for the prime...

		JdomBackLinks parser = new JdomBackLinks();
		int total = 10000;
		int batch = 500;
		List<String> a = new ArrayList<String>();
		String nextLinkTitle = "";
		Connector connector = user.getConnector();
		for(int i = batch; i< total; i+=batch){
			String[] linkQuery1 = { 
					"list", "backlinks",
					"bllimit", batch+"",
					"blnamespace", "0",
					"blfilterredir","redirects",
					"bltitle", prime
			};
			String[] linkQuery2 = { 
					"list", "backlinks",
					"bllimit", batch+"",
					"blnamespace", "0",
					"bltitle", prime,
					"blfilterredir","redirects",
					"blcontinue", nextLinkTitle
			};

			String pagesXML = "";
			if(nextLinkTitle==""){
				pagesXML = connector.queryXML(user, a, linkQuery1);
			}else{
				pagesXML = connector.queryXML(user, a, linkQuery2);
			}
			try{
				List<String> redirects = parser.parseLinks(pagesXML);

				if(redirects != null && redirects.size()>0){
					wikisynset.addAll(redirects);
				}
				//see if any more to get
				nextLinkTitle = parser.parseNextLinkChunk(pagesXML);
				if(nextLinkTitle==""){
					break;
				}
			}
			catch(Exception e){
				System.out.println("Error, no links found in pagesXML for "+getTitle()+"\n");
				e.printStackTrace();
			}
		}
	}

	/**
	 * If this wikipage is just a redirect page, then set the title of this page to the title of the page that it redirects to
	 */
	public void setTitleToRedirect(){
		if(getRedirects_to()!=null){
			this.setTitle(getRedirects_to());
		}
	}	

	public void setCategories(){
		List<String> titles = new ArrayList<String>();
		titles.add(this.getTitle());
		List<Page> listOfPages = user.queryCategories(titles);
		for (Page page : listOfPages) {
			for (int j = 0; j < page.sizeOfCategoryList(); j++) {
				Category cat = page.getCategory(j);
				if(this.categories==null){
					categories = new ArrayList<Category>();
				}
				this.categories.add(cat);
			}
		}
	}


	/**
	 * If extlinks has been set, try to dig external identifiers out of the links.  Currently only looks at ncbi gene
	 * Note that there are much better ways to do this if the goal is accuracy
	 */
	public void extractExternalIdsFromHyperlinks(){
		if(extlinks==null&&extlinks.size()<1){
			return;
		}
		for(String url : extlinks){

			if(url.startsWith("http://www.ncbi.nlm.nih.gov/sites/entrez?Db=gene&Cmd=ShowDetailView&TermToSearch=")){
				setNcbi_gene_id(url.substring(81));
			}else if(url.startsWith("http://amigo.geneontology.org/")&&url.length()>102){
				go_ids.add(url.substring(102));
			}

		}

	}

	/**
	 * WikiTrust adds user and trust data into the page.
	 * @return
	 */
	public boolean setPageContentToWikiTrust(){
		if(this.getRevid()!=null&&this.getPageid()!=null){
			String wt = WikiTrustClient.getTrustForWikiPage(this.getPageid(), this.getRevid(), "gettext", 0);
			if(wt!=null&&wt.length()>this.getPageContent().length()){
				this.setPageContent(wt);
				this.setUseTrust(true);
				double pagetrust = Double.parseDouble(WikiTrustClient.getTrustForWikiPage(this.getPageid(), this.getRevid(), "quality", 0));
				this.setPageTrust(pagetrust);
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}

	/**
	 * This goes and gets the WikiText that composes an article and sets the pageContent (and others)	
	 */
	public boolean retrieveWikiTextContent(boolean expandtemplates){
		if(getTitle() == null){
			System.out.println("Title not set");
			return false;
		}
		List<String> a = new ArrayList<String>();
		a.add(getTitle());
		Page p = null;
		String[] contentQuery = { 
				"prop", "revisions",
				"redirects","true",
				"rvprop", "content|user|timestamp|ids|size|flags", //timestamp|user|comment| 
				"",""
		};
		
		if(expandtemplates){
			contentQuery[6] = "rvexpandtemplates";
			contentQuery[7] = "";
		}

		Connector connector = user.getConnector();
		List<Page> pages = connector.query(user, a, contentQuery);
		if(pages!=null&&pages.size()==1){
			Page page = pages.get(0);
			setPageid(page.getPageid());
			if(page.getPageid()==null){
				return false;
			}
			setWikidetails(page);
			setRevid(page.getCurrentRevision().getRevid());
			setParentid(page.getCurrentRevision().getParentid());
			setLasteditor(page.getCurrentRevision().getUser());
			setSize(page.getCurrentRevision().getSize());
			setTimestamp(page.getCurrentRevision().getTimestamp());			
			this.setPageContent(page.getCurrentContent());
		}else if(pages.size()>1){
			System.out.println("too many pages returned: "+pages.size());
			return false;
		}else{
			System.out.println("No pages matched title: "+getTitle());
			return false;
		}
		return true;
	}


	/**
	 * This uses the api to access links heading out from this wikipage.  These links include -all links including those transcluded from templates. 
	 * Because transclusion can lead to a large number of links that can be of dubious relation to the source page this should be used with caution.
	 */
	public void retrieveAllOutBoundWikiLinks(boolean storeAllLinkedPageData){
		int total = 10000;
		int batch = 500;
		List<String> a = new ArrayList<String>();
		a.add(getTitle());
		String nextLinkTitle = "";
		Connector connector = user.getConnector();		
		JdomForwardLinks parser = new JdomForwardLinks();
		
		for(int i = batch; i< total; i+=batch){
			String[] linkQuery1 = { 
					"prop", "links",
					"pllimit", batch+"",
					"plnamespace", "0"
			};
			String[] linkQuery2 = { 
					"prop", "links",
					"pllimit", batch+"",
					"plnamespace", "0",
					"plcontinue", nextLinkTitle
			};

			String pagesXML = "";
			if(nextLinkTitle==""){
				pagesXML = connector.queryXML(user, a, linkQuery1);
			}else{
				pagesXML = connector.queryXML(user, a, linkQuery2);
			}
			try{
				List<String> links = parser.parseLinks(pagesXML);

				if(links != null && links.size()>0){
					for(String link : links){
						GeneWikiLink outlink = new GeneWikiLink();						
						//Note this will be a lot of data and will go slow...
//						if(storeAllLinkedPageData){
//							GeneWikiPage target = new GeneWikiPage(user);
//							target.setTitle(link.getTitle());
//							target.setRedirect();
//							target.setAllRedirects();
//							target.retrieveWikiTextContent();
//							target.retrieveAllOutBoundWikiLinks(false);
//							target.retrieveAllOutBoundHyperLinks();
//						}
						outlink.setTarget_page(link);
						glinks.add(outlink);
					}
				}

				//see if any more to get
				nextLinkTitle = parser.parseNextLinkChunk(pagesXML);
				if(nextLinkTitle==""){
					break;
				}
			}
			catch(Exception e){
				System.out.println("Error, no links found in pagesXML for "+getTitle());
			}
		}
	}

	/**
	 * This uses the API to collect all the links from other pages to the current page.  
	 * If maptoredirect is true, it checks to see whether this page is a redirect page and if it is, looks for links that point to the page that this page redirects to.
	 * The gathered links represent ~all the links, including those generated via transclusion
	 * If storeAllLinkedInPageData then go off get all the data for the pages that link to this page (text, links, redirects).  Note that could be a lot..
	 * @param maptoredirect
	 */
	public void retrieveAllInBoundWikiLinks(boolean maptoredirect, boolean storeAllLinkedInPageData){
		String pagetitle = getTitle();
		if(maptoredirect&&getRedirects_to()!=null){
			pagetitle = getRedirects_to();
		}
		JdomBackLinks parser = new JdomBackLinks();
		int total = 10000;
		int batch = 500;
		List<String> a = new ArrayList<String>();
		String nextLinkTitle = "";
		Connector connector = user.getConnector();	
		for(int i = batch; i< total; i+=batch){
			String[] linkQuery1 = { 
					"list", "backlinks",
					"bllimit", batch+"",
					"blnamespace", "0",
					"bltitle", pagetitle
			};
			String[] linkQuery2 = { 
					"list", "backlinks",
					"bllimit", batch+"",
					"blnamespace", "0",
					"bltitle", pagetitle,
					"blcontinue", nextLinkTitle
			};

			String pagesXML = "";
			if(nextLinkTitle==""){
				pagesXML = connector.queryXML(user, a, linkQuery1);
			}else{
				pagesXML = connector.queryXML(user, a, linkQuery2);
			}
			try{
				List<String> backlinks = parser.parseLinks(pagesXML);
				if(backlinks != null && backlinks.size()>0){
					for(String link : backlinks){
						GeneWikiLink backlink = new GeneWikiLink();	
						//Note this will be a lot of data and will go slow...
						if(storeAllLinkedInPageData){
							GeneWikiPage source = new GeneWikiPage(user);
							source.setTitle(link);
							source.setRedirect();
							source.setAllRedirects();
							source.retrieveWikiTextContent(false);
							source.retrieveAllOutBoundWikiLinks(false);
							source.retrieveAllOutBoundHyperLinks();
						}
						backlink.setTarget_page(link);
						inglinks.add(backlink);
					}
				}

				//see if any more to get
				nextLinkTitle = parser.parseNextLinkChunk(pagesXML);
				if(nextLinkTitle==""){
					break;
				}
			}
			catch(Exception e){
				System.out.println("Error, no links found in pagesXML for "+getTitle());
			}
		}
	}

	/**
	 * Get and set all the non wikilink hyperlinks on this page
	 */
	public void retrieveAllOutBoundHyperLinks(){
		JdomExtLinks parser = new JdomExtLinks();
		int total = 3000;
		int batch = 500;
		List<String> a = new ArrayList<String>();
		a.add(getTitle());
		String nextLinkTitle = "";
		Connector connector = user.getConnector();		
		for(int i = batch; i< total; i+=batch){
			String[] linkQuery1 = { 
					"prop", "extlinks",
					"ellimit", batch+""
			};
			String[] linkQuery2 = { 
					"prop", "extlinks",
					"ellimit", batch+"",
					"eloffset", nextLinkTitle
			};

			String pagesXML = "";
			if(nextLinkTitle==""){
				pagesXML = connector.queryXML(user, a, linkQuery1);
			}else{
				pagesXML = connector.queryXML(user, a, linkQuery2);
			}

			//get out the links
			List<String> elinks = parser.parseLinks(pagesXML);
			//add to the gene
			if(elinks!=null){
				getExtlinks().addAll(elinks);
			}
			//see if any more to get
			nextLinkTitle = parser.parseNextLinkChunk(pagesXML);
			if(nextLinkTitle==""){
				break;
			}
		}
	}



	/**************************************************************************
	 * Wikitext parsing methods below
	 * 
	 */

	public void parseAndSetNcbiGeneId(){
		if(this.getPageContent()==null){
			return;
		}
		String reg = "\\{\\{PBB\\|geneid= *(\\d+)\\}\\}";
		Pattern pattern = 
			Pattern.compile(reg);
		Matcher matcher = 
			pattern.matcher(this.getPageContent());
		String geneid = "";
		if (matcher.find()) {
			geneid = matcher.group().trim();
			geneid = geneid.substring(13, geneid.length()-2);
			this.setNcbi_gene_id(geneid);
		}
	}


	public void parseAndSetGeneSymbol(){
		String reg = "\\| Symbol = .{1,10} \\|";
		Pattern pattern = 
			Pattern.compile(reg);
		Matcher matcher = 
			pattern.matcher(this.getPageContent());
		String sym = "";
		if (matcher.find()) {
			sym = matcher.group().trim();
			sym = sym.substring(11, sym.length()-2);
			sym = sym.trim();
			this.setGene_symbol(sym);
		}
	}

	/***
	 * This is where the outgoing in-text wikilinks are parsed out of the wikitext.  The 'byheadings' just means that the we associate each link with the heading of the
	 * text section where it was found.
	 */
	@SuppressWarnings("unchecked")
	public void setHeadings(){
		setHeadings(new ArrayList<Heading>());
		Heading h = new Heading();
		h.setStartIndex(0);
		h.setStopIndex(0);
		h.setText("Summary");
		this.headings.add(h);

		String reg = "={2,5}.{3,100}={2,5}";
		Pattern pattern = 
			Pattern.compile(reg);
		Matcher matcher = 
			pattern.matcher(this.getPageContent());
		while (matcher.find()) {
			h = new Heading();
			h.setStartIndex(matcher.start());
			h.setStopIndex(matcher.end());
			h.setText(matcher.group().replaceAll("=", "").trim());
			this.headings.add(h);
		}
		Collections.sort(this.headings);
	}


	/***
	 * This is where the outgoing in-text wikilinks are parsed out of the wikitext.  The 'byheadings' just means that the we associate each link with the heading of the
	 * text section where it was found.
	 */
	public void setGeneWikiLinksByHeadings(){
		String[] parts = this.getPageContent().split("==");
		boolean heading = false;
		String predicate = "summary";
		int chunkstartindex = 0;
		//		for(String part : parts){
		//			if(heading){
		//				//collect links under this heading
		//				predicate = part;
		//				heading = false;
		//			}else{
		//				List<GeneWikiLink> gs = extractGeneWikiLinks(part, predicate,chunkstartindex);
		//				if(gs!=null){
		//					glinks.addAll(gs);
		//				}
		//				heading = true;
		//				chunkstartindex+=4;
		//			}
		//			chunkstartindex+=part.length(); //4 accounts for the '==' lost in the split
		//		}
		for(int i=0; i<parts.length; i++){
			if(heading&&parts[i].length()<200){
				predicate = parts[i];
				heading = false;
			}else{
				List<GeneWikiLink> gs = extractGeneWikiLinks(parts[i], predicate,chunkstartindex);
				if(gs!=null){
					glinks.addAll(gs);
				}
				heading = true;
				chunkstartindex+=4;
			}
			chunkstartindex+=parts[i].length(); //4 accounts for the '==' lost in the split
		}
	}

	/***
	 * This uses a simple regular expression to parse out wikilinks [[likethis]] in the input text.  
	 * The header is just used to track where in the article the links were found
	 * @param input
	 * @param header
	 * @param chunkstartindex allows for tracking the place the links are found within the context of the entire page
	 * @return
	 */
	public List<GeneWikiLink> extractGeneWikiLinks(String input, String header, int chunkstartindex){
		List<GeneWikiLink> links = new ArrayList<GeneWikiLink>();
		int contextRange = 50;
		//	 String input = "is a [[protein]] which in ";
		String reg = "\\[\\[.*?\\]\\]";
		Pattern pattern = 
			Pattern.compile(reg);
		Matcher matcher = 
			pattern.matcher(input);

		while (matcher.find()) {
			String paragraph = ParseUtils.paragraph(input, matcher.start());
			String before = ParseUtils.before(input, matcher.start(), contextRange);
			String after = ParseUtils.after(input, matcher.end(), contextRange);
			String link = ParseUtils.trimWikiLink(matcher.group());
			//pull out link title if different from link
			if(link.contains("|")){
				link = link.substring(0,link.indexOf("|"));
			}
			GeneWikiLink glink = new GeneWikiLink();
			glink.setStartIndex(matcher.start()+chunkstartindex);
			glink.setParagraph(paragraph);
			glink.setPosttext(after);
			glink.setSectionHeader(header.trim().replace('=', ' ').trim());
			glink.setPretext(before);
			glink.setSnippet("..."+before + "!-"+link +"-! "+ after+"...");
			link = link.replace(' ', '_');
			//			GeneWikiPage target = new GeneWikiPage(user);
			//			target.setTitle(link);
			//			target.setRedirect();
			//			target.setAllRedirects();
			glink.setTarget_page(link);

			links.add(glink);
		}
		return links;
	}

	/**
	 * Parse out all of the references from this page and stores them
	 */
	public void setReferences(){
		Map<String, Reference> tmp = new HashMap<String, Reference>();
		String input = new String(this.getPageContent());
		//	System.out.println(input);
		//		String reg1 = "<ref {1,5}name {0,5}=.{0,60}/>|<ref.+?</ref>";//"<ref name=.{0,25}/>|<ref.+?</ref>";//"<ref.+?</ref>"; //"<ref[^<]*</ref>";
		Pattern pattern = 
			Pattern.compile(Reference.regexP);
		Matcher matcher = 
			pattern.matcher(input);

		while (matcher.find()) {
			Reference reference = new Reference();
			reference.setStartIndex(matcher.start());
			reference.setStopIndex(matcher.end());
			reference.setWikiText(matcher.group());
			String ref = matcher.group();
			String pmid = getPmidFromRef(ref);
			String url = null;
			if(pmid!=null){
				reference.setPmid(pmid);
			}else {
				url = getUrlFromRef(ref);
				if(url!=null){
					reference.setUrl(url);
				}
			}
			//if we have a name and some identifying info, keep track of it in tmp map, prefer pmids > urls > raw ref info

			String name = getNameOfRef(ref);
			reference.setRefName(name);

			if(name!=null&&pmid!=null){
				tmp.put(name, reference);
			}else if(name!=null&&url!=null){
				tmp.put(name, reference);
			}
			else if(name!=null&&ref!=null&&ref.length()>25&&!tmp.containsKey(name)){
				tmp.put(name, reference);
			}
			this.getRefs().add(reference);
		}
		//check for unidentified references with names and see if we can get to the info. in tmp map
		for(Reference ref : this.getRefs()){
			String name = ref.getRefName();
			if(name==null){
				continue;
			}
			//if this reference is just a name, check for the info from another reference with the same name
			if(ref.getPmid()==null&&ref.getUrl()==null){
				if(tmp.containsKey(name)){
					ref.setPmid(tmp.get(name).getPmid());
					ref.setUrl(tmp.get(name).getUrl());
					ref.setWikiText(tmp.get(name).getWikiText());
				}
			}
		}


	}

	public String getNameOfRef(String wikiref){
		String name = null;
		String reg = "name {0,5}=.+?>";
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(wikiref);
		if (matcher.find()) {
			name = matcher.group();
			name = name.substring(0,name.length()-1);
			name = name.replaceAll("\"","");
			name = name.replaceAll("/","");
			name = name.replaceAll("=","");
			name = name.replaceAll(">","");
			name = name.replaceAll("name","");
			name = name.trim();
		}

		return name;
	}

	public String getPmidFromRef(String wikiref){
		String pmid = null;
		String reg = "pmid[ =]*(\\d+)";
		Pattern pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(wikiref);
		if (matcher.find()) {
			pmid = matcher.group().substring(4);
		}else{
			reg = "PMID\\s*(\\d+)";
			pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(wikiref);
			if (matcher.find()) {
				pmid = matcher.group().substring(4);
			}
		}
		if(pmid!=null){
			pmid = pmid.replaceAll("=","");
			pmid = pmid.replaceAll(" ","");
			pmid = pmid.replaceAll("pmid","");
			pmid = pmid.replaceAll("PMID","");
		}
		return pmid;
	}

	public String getUrlFromRef(String wikiref){
		String url = null;
		String reg = 
			"https?:\\/\\/([0-9a-zA-Z][-\\w]*[0-9a-zA-Z]\\.)+" +
			"([a-zA-Z]{2,9})(:\\d{1,4})?([-\\w\\/#~:.?+=&%@~]*)/.+?(\\||\\s)";
		//"http:[^<]*\\|";
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(wikiref);
		if (matcher.find()) {
			url = matcher.group();
			url = url.substring(0,url.length()-1);
		}
		return url;
	}	

	public void parseAndSetSentences(){
		SentenceSplitter s = new SentenceSplitter();
		List<Sentence> chunks = s.splitWikiSentences(this.getPageContent());
		this.setSentences(chunks);
	}

	public List<Reference> getRefsForSentence(Sentence sentence, int ignoreafterindex){
		if(sentence==null){ 
			return null;
		}
		List<Reference> refs = new ArrayList<Reference>();
		for(Reference r : this.getRefs()){
			if(r.getStartIndex()< ignoreafterindex &&
					r.getStartIndex()>sentence.getStartIndex() &&
					(r.getStartIndex()<sentence.getNextStartIndex()||sentence.getNextStartIndex()==-1)){
				refs.add(r);
			}
		}
		return refs;
	}

	/**
	 * Given an index within the page, return the reference that contains it or null
	 * @param textIndex
	 * @return
	 */
	public Reference getReferenceByTextIndex(int textIndex){
		for(Reference ref : this.getRefs()){
			if(ref.getStartIndex()<=textIndex&&
					ref.getStopIndex()>=textIndex){
				return ref;
			}
		}
		return null;
	}

	/**
	 * Given an index within the page, return the sentence that contains it
	 * @param textIndex
	 * @return
	 */
	public Sentence getSentenceByTextIndex(int textIndex){
		Sentence s = null;
		for(Sentence sentence : this.getSentences()){
			if(sentence.getStartIndex()<=textIndex&&sentence.getStopIndex()>=textIndex){
				return sentence;
			}
		}
		return s;
	}

	/**
	 * Given an index within the page, return the heading that it falls under
	 * @param textIndex
	 * @return
	 */
	public Heading getHeadingByTextIndex(int textIndex){
		if(this.getHeadings()==null||this.getHeadings().size()==0){
			return null;
		}
		Heading tmp = this.getHeadings().get(0);
		for(Heading h : this.getHeadings()){
			if(tmp.getStartIndex()<=textIndex&&h.getStartIndex()>=textIndex){
				return tmp;
			}else{
				tmp = h;
			}
		}
		return tmp;
	}


	/**************************************************************************
	 * Output methods below
	 * 
	 */

	//	public String toLinkTripleTable(){
	//		String out = "";
	//		for(String syn : wikisynset){
	//			out+=syn+"\t";
	//		}
	//		out+="\n";
	//		//wikiLinks out
	//		if(getGlinks()!=null){
	//			for(GeneWikiLink glink : getGlinks()){
	//				out+=getTitle()+"\tWikiLinkOut\t"+glink.getTarget_page()+"\t";
	////				for(String target : glink.getTarget_page().wikisynset){
	////					out+=target+"\t";
	////				}
	//				out+="\n";
	//			}
	//		}
	//		//wikilinks IN with synonyms
	//		if(getInglinks()!=null){
	//			for(GeneWikiLink inlink :getInglinks()){
	//				for(String syn : wikisynset){
	////					for(String target : inlink.getTarget_page().wikisynset){
	////						out+=syn+"\tWikiLinkIn\t"+target+"\n";
	////					}
	//					out+=syn+"\tWikiLinkIn\t"+inlink.getTarget_page().getTitle()+"\n";
	//				}
	//			}
	//		}
	//		return out;
	//	}

	//TODO convert gnf.org/ URIs into something LinkedData friendly (ie we need a server..) 	
	public Model toSimpleRDFModel(){
		Model model = ModelFactory.createDefaultModel();
		Resource subject = model.getResource("http://dbpedia.org/resource/"+getTitle().replace(' ','_'));
		subject.addLiteral(RDFS.label, getTitle().replace('_', ' '));
		Property link = model.createProperty("http://gnf.org#WikiLink");
		link.addLiteral(RDFS.label, "Wiki Link");
		if(getNcbi_gene_id()!=""){
			Property ncbi_gene = model.createProperty("http://gnf.org#ncbi_gene_id");
			//	ncbi_gene.addLiteral(RDFS.label, "GENE_ID");
			Resource generec = model.createResource("http://purl.org/commons/record/ncbi_gene/"+getNcbi_gene_id());
			Statement genelink = model.createStatement(subject, ncbi_gene,generec);
			model.add(genelink);
		}
		for(GeneWikiLink glink : getGlinks()){
			Resource object = model.getResource("http://dbpedia.org/resource/"+glink.getTarget_page().replace(' ','_'));
			//	object.addLiteral(RDFS.label, glink.getTitle());
			Statement addlink = model.createStatement(subject, link, object);
			model.add(addlink);
		}
		return model;
	}


	public String toTabDelimited(){
		String out = "";
		//WikiTitle \t WikiLink 
		//\t ncbi_gene_id \t header \t snippet
		if(getGlinks()!=null){
			for(GeneWikiLink link : getGlinks()){
				out+=getTitle()+"\t"+
				link.getTarget_page()+"\t"+
				getNcbi_gene_id()+"\t"+
				link.getSectionHeader().replaceAll("\n", " ")+"\t"+
				link.getSnippet().replaceAll("\n", " ")+"\n";
			}
			for(GeneWikiLink link : getInglinks()){
				out+=getTitle()+"\tlinks from\t"+
				link.getTarget_page()+"\n";
			}
		}else{
			out="\t\t\t\t\n";
		}
		return out;
	}


	/**
	 * Converts the GeneWikiPageData to HTML using the info.bliki.wiki library
	 * @return
	 */
	//	public String toHtml(){
	//		WikiModel wikiModel = new WikiModel(
	//				Configuration.DEFAULT_CONFIGURATION, Locale.ENGLISH, "${image}", "${title}");
	//		wikiModel.setUp();
	//		String result = "";
	//		try {
	//			result = wikiModel.render(new HTMLConverter(), this.getPageContent());
	//		} finally {
	//			wikiModel.tearDown();
	//		}
	//		return result;
	//	}

	@Override
	public boolean equals(Object o) {
		if(! (o instanceof GeneWikiPage)){
			return false;
		}
		GeneWikiPage target = (GeneWikiPage)o;
		return(this.getTitle().equals(target.getTitle()));
	}

	@Override
	public int compareTo(Object o) {
		if(! (o instanceof GeneWikiPage)){
			return -1;
		}
		GeneWikiPage target = (GeneWikiPage)o;
		return(this.getTitle().compareTo(target.getTitle()));
	}

	@Override
	public int hashCode() {
		int code = title.hashCode();
		code = 31 * code;
		return code;
	}

	public List<GeneWikiLink> getGlinks() {
		return glinks;
	}
	public void setGlinks(List<GeneWikiLink> glinks) {
		this.glinks = glinks;
	}
	public List<GeneWikiLink> getInglinks() {
		return inglinks;
	}
	public void setInglinks(List<GeneWikiLink> inglinks) {
		this.inglinks = inglinks;
	}
	public List<String> getExtlinks() {
		return extlinks;
	}
	public void setExtlinks(List<String> extlinks) {
		this.extlinks = extlinks;
	}
	public Set<String> getWikisynset() {
		return wikisynset;
	}
	public void setWikisynset(Set<String> wikisynset) {
		this.wikisynset = wikisynset;
	}
	public String getRedirects_to() {
		return redirects_to;
	}
	public void setRedirects_to(String redirectsTo) {
		redirects_to = redirectsTo;
	}
	public String getNcbi_gene_id() {
		return ncbi_gene_id;
	}
	public void setNcbi_gene_id(String ncbiGeneId) {
		ncbi_gene_id = ncbiGeneId;
	}
	public List<String> getGo_ids() {
		return go_ids;
	}
	public void setGo_ids(List<String> goIds) {
		go_ids = goIds;
	}
	public String getUniprot_id() {
		return uniprot_id;
	}
	public void setUniprot_id(String uniprotId) {
		uniprot_id = uniprotId;
	}
	public boolean isRedirect_checked() {
		return redirect_checked;
	}
	public void setRedirect_checked(boolean redirectChecked) {
		redirect_checked = redirectChecked;
	}
	public String getPageContent() {
		return pageContent;
	}
	public void setPageContent(String pageContent) {
		this.pageContent = pageContent;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public Page getWikidetails() {
		return wikidetails;
	}


	public void setWikidetails(Page wikidetails) {
		this.wikidetails = wikidetails;
	}

	public List<Reference> getRefs() {
		return refs;
	}

	public void setRefs(List<Reference> refs) {
		this.refs = refs;
	}

	public List<Sentence> getSentences() {
		return sentences;
	}

	public void setSentences(List<Sentence> sentences) {
		this.sentences = sentences;
	}

	public List<Heading> getHeadings() {
		return headings;
	}

	public void setHeadings(List<Heading> headings) {
		this.headings = headings;
	}

	public String getRevid() {
		return revid;
	}

	public void setRevid(String revid) {
		this.revid = revid;
	}

	public String getParentid() {
		return parentid;
	}

	public void setParentid(String parentid) {
		this.parentid = parentid;
	}

	public String getLasteditor() {
		return lasteditor;
	}

	public void setLasteditor(String lasteditor) {
		this.lasteditor = lasteditor;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getPageid() {
		return pageid;
	}

	public void setPageid(String pageid) {
		this.pageid = pageid;
	}

	public boolean isUseTrust() {
		return useTrust;
	}

	public void setUseTrust(boolean useTrust) {
		this.useTrust = useTrust;
	}

	public double getPageTrust() {
		return pageTrust;
	}

	public void setPageTrust(double pageTrust) {
		this.pageTrust = pageTrust;
	}

	public String getGene_symbol() {
		return gene_symbol;
	}

	public void setGene_symbol(String gene_symbol) {
		this.gene_symbol = gene_symbol;
	}

	public String getLastComment() {
		return lastComment;
	}

	public void setLastComment(String lastComment) {
		this.lastComment = lastComment;
	}

	public List<Category> getCategories() {
		return categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}

}
