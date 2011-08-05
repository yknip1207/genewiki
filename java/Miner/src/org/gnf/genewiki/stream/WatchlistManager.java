/**
 * 
 */
package org.gnf.genewiki.stream;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.gnf.genewiki.GWRevision;
import org.gnf.genewiki.GeneWikiUtils;
import org.gnf.genewiki.WikiCategoryReader;
import org.gnf.genewiki.metrics.RevisionCounter;
import org.gnf.genewiki.parse.ParserAccess;
import org.gnf.wikiapi.Connector;
import org.gnf.wikiapi.Page;
import org.gnf.wikiapi.UnexpectedAnswerException;
import org.gnf.wikiapi.User;
import org.gnf.wikiapi.query.Edit;
import org.gnf.wikiapi.query.Watch;

/**
 * @author bgood
 *
 */
public class WatchlistManager {
	User user;
	String wikiapi;
	ParserAccess parser;
	Set<String> watchlist;

	public WatchlistManager(String wikipedia_user, String wikipedia_password, String wikiapi){
		init(wikipedia_user, wikipedia_password, wikiapi);
	}

	public void init(String wiki_user, String wiki_password, String apiurl){
		if(apiurl==null){
			apiurl="http://en.wikipedia.org/w/api.php";
		}
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
		WatchlistManager wm = new WatchlistManager(creds.get("wpid"), creds.get("wppw"), null);
		//wm.updateGeneWikiWatchList();
		Calendar t0 = Calendar.getInstance();
		t0.add(Calendar.DAY_OF_YEAR, -1);
		List<GWRevision> live = wm.getRecentChangesFromWatchlist(t0, 100, 100);
		int c = 0;
		for(GWRevision rev : live){
			c++;
			System.out.println(c+" "+rev.getTimestamp()+"  "+rev.getTitle()+" "+rev.getUser()+" ");
		}
		//		System.out.println(live.size()+" "+live.get(9).getTitle()+" "+live.get(9).getTimestamp());
	}

	public void updateGeneWikiWatchList(){
		//get gene wiki titles
		WikiCategoryReader catreader = new WikiCategoryReader();
		catreader.setUser(user);
		List<Page> gw = catreader.getPagesWithPBB(100000, 500);
		Set<String> genewiki_titles = pagelist2titleset(gw);
		System.out.println("Current n wiki titles = "+genewiki_titles.size());
		//get current watchlist titles
		List<Page> cu = getCurrentLiveWatchlist(500, 100000);
		Set<String> watchlist_titles = pagelist2titleset(cu);
		System.out.println("Current watchlist titles = "+watchlist_titles.size());
		//find missing gene wiki titles		
		genewiki_titles.removeAll(watchlist_titles);
		//push any additions
		//TODO - possibly look for things to delete but not now (might want to add other pages manually for gene wiki feed)
		if(genewiki_titles!=null){
			System.out.println("Adding "+genewiki_titles.size()+" to gene wiki watchlist");
			addTitles2watchlist(genewiki_titles);
		}

	}

	public void addTitles2watchlist(Set<String> titles){
		Connector connector = user.getConnector();
		int c = 0;
		for(String title : titles){
			c++;
			Watch watch = Watch.create();
			watch.title(title);
			String xmlresponse = connector.sendXML(user, watch);
			if(c%100==0){
				System.out.println(c+"\n"+xmlresponse);
			}
		}
	}

	public Set<String> pagelist2titleset(List<Page> pages){
		Set<String> titles = new HashSet<String>();
		for(Page page : pages){
			titles.add(page.getTitle());
		}
		return titles;
	}

	public List<Page> getCurrentLiveWatchlist(int batch, int total){
		
		List<Page> pages = new ArrayList<Page>();
		String nextTitle = "";
		Connector connector = user.getConnector();
		for(int i = 0; i< total; i+=batch){
			String[] query = { 
					"list", "watchlistraw", 
					"wrlimit", batch+"",
					"wrnamespace", "0",  //namespace = 10 equals the template namespace.
					"", ""
			};
			if(nextTitle!=null&&!nextTitle.equals("")){
				query[6] = "wrcontinue";
				query[7] = nextTitle;
			}
			String pagesXML = connector.queryXML(user, query);
			// <?xml version="1.0"?><api><watchlistraw><wr ns="0" title="Cyclin-dependent kinase 2" /></watchlistraw></api>\n
			List<Page> page_batch = parser.parseWatchlistXml(pagesXML);
			nextTitle = parser.getNextTitle();
			if(page_batch!=null&&page_batch.size()>0){
				pages.addAll(page_batch);
			}else{
				break;
			}
			if(nextTitle==null){
				break;
			}
		}
		return pages;
	}

	public List<GWRevision> getRecentChangesFromWatchlist(Calendar earliest, int batch, int total){
		SimpleDateFormat timestamp = new SimpleDateFormat("yyyyMMddHHmmss");
		TimeZone tz = TimeZone.getTimeZone("GMT");
		timestamp.setTimeZone(tz);
		String start = timestamp.format(earliest.getTime());
		
		List<GWRevision> pages = new ArrayList<GWRevision>();
		String nextTitle = "";
		Connector connector = user.getConnector();
		for(int i = 0; i< total; i+=batch){
			String[] categoryMembers = { 
					"generator", "watchlist", 
					"gwllimit", batch+"",
					"gwlnamespace", "0",  //namespace = 10 equals the template namespace.
					"gwlshow", "!bot",
					"", "",
					"gwlstart",start,
					"gwldir", "newer",
					"prop", "revisions",
					"rvprop", "user|timestamp|ids|size|flags|comment"
			};
			if(nextTitle!=null&&!nextTitle.equals("")){
				categoryMembers[8] = "gwlstart";
				categoryMembers[9] = nextTitle;
			}
			String pagesXML = connector.queryXML(user, categoryMembers);
			List<GWRevision> page_batch = parser.parseRecentWatchlistXml(pagesXML);
			nextTitle = parser.getNextTitle();
			if(page_batch!=null&&page_batch.size()>0){
				pages.addAll(page_batch);
			}else{
				break;
			}
			if(nextTitle==null){
				break;
			}
		}
		return pages;
	}

	public Set<String> getWatchlist() {
		return watchlist;
	}

	public void setWatchlist(Set<String> watchlist) {
		this.watchlist = watchlist;
	}


}
