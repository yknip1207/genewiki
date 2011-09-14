/**
 * 
 */
package org.gnf.genewiki.metrics;


import org.gnf.wikiapi.Connector;
import org.gnf.wikiapi.Page;
import org.gnf.wikiapi.Revision;
import org.gnf.wikiapi.User;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import org.gnf.genewiki.Config;
import org.gnf.genewiki.GWRevision;
import org.gnf.genewiki.GeneWikiPage;
import org.gnf.genewiki.GeneWikiUtils;
import org.gnf.genewiki.parse.ParserAccess;
import org.gnf.ncbo.web.AnnotatorClient;
import org.gnf.ncbo.web.NcboAnnotation;
import org.gnf.util.DateFun;
import org.gnf.util.FileFun;
import org.gnf.util.MapFun;
import org.json.JSONArray;

import java.util.Collections;

/**
 * @author bgood
 *
 */
public class RevisionCounter {

	User user;
	String wikiapi;
	ParserAccess parser;
	Page wikidetails; 
	SimpleDateFormat wp_format = DateFun.wp_format();

	public RevisionCounter(String wikipedia_user, String wikipedia_password){
		init(wikipedia_user, wikipedia_password);
	}

	public RevisionCounter(String wikipedia_user, String wikipedia_password, String wikiapi){
		init(wikipedia_user, wikipedia_password, wikiapi);
	}
	
	public void init(String wikipedia_user, String wikipedia_password){
		user = new User(wikipedia_user, wikipedia_password, "http://en.wikipedia.org/w/api.php");
		user.login();
		parser = new ParserAccess();
	}
	
	public void init(String wiki_user, String wiki_password, String apiurl){
		user = new User(wiki_user, wiki_password, apiurl);
		user.login();
		parser = new ParserAccess();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String credfile = "/Users/bgood/workspace/Config/gw_creds.txt";
		Map<String, String> creds = GeneWikiUtils.read2columnMap(credfile);
		RevisionCounter rc = new RevisionCounter(creds.get("wpid"), creds.get("wppw"));
	//	Calendar latest = Calendar.getInstance();
	//	latest.add(Calendar.MONTH, -2);
//		GWRevision rev = rc.getRevisionOnDay(latest, "CD90", true);
//		GeneWikiPage p = new GeneWikiPage(rev, rc.user, true);
//		System.out.println(rev.getRevid()+" "+rev.getTimestamp()+"\n"+p.getPageContent());
//		List<String> titles = new ArrayList<String>();
//		titles.add("VLDL_receptor");
//	//	Map<String, String> gene_wiki = GeneWikiUtils.read2columnMap("./gw_data/gene_wiki_index.txt");
//	//	titles.addAll(gene_wiki.values());
//	//	Collections.sort(titles);
//		String article_names = "/users/bgood/data/wikiportal/facebase_genes.txt";
//		titles.addAll(FileFun.readOneColFile(article_names));
		Calendar latest = Calendar.getInstance();
		Calendar earliest = Calendar.getInstance();
		earliest.add(Calendar.YEAR, -3);
//		String outfile = "/Users/bgood/data/wikiportal/fb_denver/networks/fb_gene_editor";
//		rc.generateBatchReport(new HashSet<String>(titles), latest, earliest, outfile);	
		List<GWRevision> revs = rc.getRevisions(latest, earliest, "VLDL_receptor", false, null);
		System.out.println(revs.size()+" hello ");
		for(GWRevision rev : revs){
			System.out.println(rev.getRevid()+"\t"+rev.getTimestamp());
		}
		
	}
	
	public RevisionsReport generateBatchReport(Set<String> titles, Calendar latest, Calendar earliest, String outfile){
		RevisionsReport report = null;
		String timespan = DateFun.yearMonthDay().format(earliest.getTime())+"-"+DateFun.yearMonthDay().format(latest.getTime());
		int n = 0;
		try {
			FileWriter f = new FileWriter(outfile+"_all.txt");
			FileWriter editor_network = new FileWriter(outfile+"_gene_editor_net.txt");
			editor_network.write("gene\tedits\teditor\n");
			f.write(timespan+"__"+RevisionsReport.getArticleHeader()+"\n");
			List<GWRevision> allrevs = new ArrayList<GWRevision>();
			for(String title : titles){
				List<GWRevision> revs = getRevisions(latest, earliest, title, false, null);
				if(revs!=null&&revs.size()>0){
					allrevs.addAll(revs);
					report = new RevisionsReport(revs);	
					System.out.println(n+"\t"+report.getArticleString(title));
					f.write(report.getArticleString(title)+"\n");
					for(Entry<String, Integer> user_edit : report.getUser_edits().entrySet()){
						editor_network.write(title+"\t"+user_edit.getValue()+"\t"+user_edit.getKey()+"\n");
					}
				}else{
					System.out.println(title+"\tnone\t");
					f.write(title+"\t0\n");
				}
				n++;
			}
			editor_network.close();
			f.close();
			f = new FileWriter(outfile+"_summary.txt");
			report = new RevisionsReport(allrevs);
			report.setTimespan(DateFun.year_month_day().format(earliest.getTime())+" - "+DateFun.year_month_day().format(latest.getTime()));
			f.write(RevisionsReport.getSummaryHeader()+"\n");
			f.write(report.getSummaryString()+"\n");
			f.close();

			f = new FileWriter(outfile+"_editors.txt");
			f.write("Editor\tEdit_count\tMost_recent\n");
			for(String editor : report.getUser_edits().keySet()){
				f.write(editor+"\t"+report.getUser_edits().get(editor)+"\t"+wp_format.format(report.getUser_lastedit().get(editor).getTime())+"\n");
			}
			f.close();
			System.out.println(RevisionsReport.getSummaryHeader());
			System.out.println(report.getSummaryString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return report;
	}

	public List<GWRevision> checkListForRevisionsInRange(Calendar latest, Calendar earliest, List<String> titles){
		if(latest.before(earliest)){
			System.out.println("latest can't be before earliest");
			return null;
		}		
		List<GWRevision> revs = new ArrayList<GWRevision>();

		for(int i=0; i<titles.size(); i+=50){
			List<String> currtitles = new ArrayList<String>();
			for(int c=i; (c<i+50)&&(c<titles.size()); c++){
				currtitles.add(titles.get(c));
			}
			String props2get = "timestamp|ids|size";
			SimpleDateFormat wp_time = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			TimeZone tz = TimeZone.getTimeZone("GMT");
			wp_time.setTimeZone(tz);

			String[] revQuery = { 
					"prop", "revisions",
					"redirects","true",  
					"rvprop", props2get
			};

			Connector connector = user.getConnector();
			List<Page> pages = connector.query(user, currtitles, revQuery);
			if(pages!=null){
				for(Page page : pages){
					if(page.getCurrentRevision()!=null){
						String t = page.getCurrentRevision().getTimestamp();
						Calendar time = Calendar.getInstance(tz);
						try {
							time.setTime(wp_time.parse(t));
							if(time.after(earliest)&&time.before(latest)){
								GWRevision g = new GWRevision(page.getCurrentRevision(), page.getTitle());
								revs.add(g);
								System.out.println(i+" "+page.getTitle()+" lasted edited "+wp_time.format(time.getTime()));
								System.out.println(wp_time.format(earliest.getTime())+"   "+wp_time.format(latest.getTime()));
							}else{
								//System.out.println("not between!");
							}
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}

		return revs;
	}

	/**
	 * Get a the most recent revision up to the specified day, return null if none
	 * @param theday
	 * @param title
	 * @param getContent
	 * @return
	 */
	public GWRevision getRevisionOnDay(Calendar theday, String title, boolean getContent){
		GWRevision grev = null;
		List<String> a = new ArrayList<String>();
		a.add(title);
		ParserAccess parser = new ParserAccess();
		String props2get = "user|timestamp|ids|size|flags|comment";
		if(getContent){
			props2get+="|content";
		}

		SimpleDateFormat timestamp = new SimpleDateFormat("yyyyMMddHHmmss");
		TimeZone tz = TimeZone.getTimeZone("GMT");
		timestamp.setTimeZone(tz);
		String t0 = timestamp.format(theday.getTime());

		String[] revQuery = { 
				"prop", "revisions",
				"redirects","true",
				"rvlimit", "1",
				"rvdir", "older",
				"rvstart", t0,  // 20110204000000  
				"rvprop", props2get
		};

		Connector connector = user.getConnector();
		String pagesXML = connector.queryXML(user, a,revQuery);
		//		connector.getManager().closeIdleConnections(0);
		//		connector.getManager().deleteClosedConnections();
		List<GWRevision> newrevs = parser.parseRevisionsXml(pagesXML);
		if(newrevs!=null&&newrevs.size()==1){
			grev = new GWRevision(newrevs.get(0), title);
		}else{
			return null;
		}
		return grev;
	}
	
	public List<GWRevision> getRevisions(Calendar latest, Calendar earliest, String title, boolean getContent, List<GWRevision> alreadyHave){
		if(latest.before(earliest)){
			System.out.println("latest can't be before earliest");
			return null;
		}

		List<GWRevision> revs = new ArrayList<GWRevision>();
		if(alreadyHave!=null){
			revs.addAll(alreadyHave);
		}
		List<String> a = new ArrayList<String>();
		a.add(title);
		ParserAccess parser = new ParserAccess();
		String props2get = "user|timestamp|ids|size|flags|comment";
		if(getContent){
			props2get+="|content";
		}

		SimpleDateFormat timestamp = new SimpleDateFormat("yyyyMMddHHmmss");
		TimeZone tz = TimeZone.getTimeZone("GMT");
		timestamp.setTimeZone(tz);
		String start = timestamp.format(latest.getTime());
		String end = timestamp.format(earliest.getTime());

		String[] revQuery = { 
				"prop", "revisions",
				"redirects","true",
				"rvlimit", "500",
				"rvdir", "older",
				"rvstart", start,  // 20110204000000
				"rvend",   end,    
				"rvprop", props2get
		};

		Connector connector = user.getConnector();
		String pagesXML = connector.queryXML(user, a,revQuery);
		
		// Homebrew revisions parser!
		
		
		//		connector.getManager().closeIdleConnections(0);
		//		connector.getManager().deleteClosedConnections();
		List<GWRevision> newrevs = parser.parseRevisionsXml(pagesXML);
		if(newrevs!=null){
			for(Revision nrev : newrevs){
				GWRevision grev = new GWRevision(nrev, title);
				revs.add(grev);
			}
			if(revs.size()<1||newrevs.size()==500){
				return revs;
			}
		}else{
			System.err.println("Failed gettting revions: \n"+pagesXML);
			return revs;
		}
		GWRevision lastone = revs.get(revs.size()-1);
		Date lastdate = null;
		
		try {
			lastdate = wp_format.parse(lastone.getTimestamp());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		//latest.setTime(lastdate);
		Calendar nextbatchlatest = Calendar.getInstance();
		nextbatchlatest.setTime(lastdate);

//TODO - fix recursion...
//		if(newrevs!=null&&newrevs.size()>0){
//			//System.out.println("contTime "+wp_format.format(latest.getTime()));
//			revs = getRevisions(nextbatchlatest, earliest, title, getContent, revs);
//		}
		return revs;
	}

	public Page getLatest(String title){
		List<String> a = new ArrayList<String>(); 
		a.add(title);
		String[] contentQuery = { 
				"prop", "revisions",
				"redirects","true",
				"rvprop", "timestamp"
		};

		Connector connector = user.getConnector();
		List<Page> pages = connector.query(user, a, contentQuery);
		if(pages!=null&&pages.size()==1){
			Page page = pages.get(0);
			return page;
		}
		return null;
	}

	public GWRevision getRevision(String revid, boolean getContent){

		List<String> a = new ArrayList<String>();
		ParserAccess parser = new ParserAccess();
		String props2get = "user|timestamp|ids|size|flags|comment";
		if(getContent){
			props2get+="|content";
		}
		String[] revQuery = { 
				"prop", "revisions",
				"rvprop", props2get,
				"revids", revid
		};

		Connector connector = user.getConnector();
		String pagesXML = connector.queryXML(user, a,revQuery);
		if(pagesXML==null){
			System.err.println("No response received for revision "+revid);
			return null;
		}
		List<GWRevision> newrevs = parser.parseRevisionsXml(pagesXML);
		if(newrevs!=null&&newrevs.size()==1){
			return newrevs.get(0);
		}
		return null;
	}

	public List<String> getDiffBlocks(String revid, String revidtodiffto){
		List<String> diffs = new ArrayList<String>();
		ParserAccess parser = new ParserAccess();
		List<String> a = new ArrayList<String>();
		String[] revQuery = { 
				"prop", "revisions",
				"rvdiffto", revidtodiffto,
				"revids", revid
		};

		Connector connector = user.getConnector();
		String XML = connector.queryXML(user, a,revQuery);
		diffs = parser.parseDifsXml(XML);		
		return diffs;
	}
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
