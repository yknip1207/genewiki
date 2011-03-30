/**
 * 
 */
package org.gnf.genewiki.stream;

import static com.rosaloves.bitlyj.Bitly.as;
import static com.rosaloves.bitlyj.Bitly.shorten;
import info.bliki.api.Page;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import org.gnf.genewiki.GWRevision;
import org.gnf.genewiki.GeneWikiPage;
import org.gnf.genewiki.GeneWikiUtils;
import org.gnf.genewiki.WikiCategoryReader;
import org.gnf.genewiki.metrics.RevisionCounter;
import org.gnf.util.DateFun;

import com.rosaloves.bitlyj.Url;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;

import java.util.Collections;

/**
 * 
 * @author bgood
 *
 */
public class RevisionStream {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

				Calendar latest = Calendar.getInstance();
				Calendar earliest = Calendar.getInstance();
				earliest.add(Calendar.YEAR, -2);
		List<String> titles = new ArrayList<String>();
//		//		Map<String, String> genes = GeneWikiUtils.read2columnMap("/Users/bgood/data/wikiportal/stream/test_genes.txt");
//		//		List<String> ncbis = new ArrayList<String>(genes.keySet());
//		//		titles = GeneWikiUtils.getTitlesfromNcbiGenes(ncbis);
//
//		Map<String, String> gene_wiki = GeneWikiUtils.read2columnMap("./gw_data/gene_wiki_index.txt");
//		titles.addAll(gene_wiki.values());
//		Collections.sort(titles);
//
//		//		titles.add("RPL38"); titles.add("ERG_(gene)"); titles.add("FLNA");titles.add("Apoliprotein_E");titles.add("FLI1");titles.add("CYFIP2");
//		//		titles.add("VIPR2"); 
//		//		titles.add("VIPR1");
//		//		titles.add("Human_chorionic_gonadotropin");
		titles.add("SHC1");
//		//	printTweetables(titles, earliest, latest, rc);
		String credfile = "/Users/bgood/workspace/Config/gw_creds.txt";
		Map<String, String> creds = GeneWikiUtils.read2columnMap(credfile);
				RevisionCounter rc = new RevisionCounter(creds.get("wpid"), creds.get("wppw"));
//		//		dailyDose("./gw_data/gene_wiki_index.txt", "/Users/bgood/workspace/genewiki/genewikitools/static/gwrss.xml",
//		//				"/Users/bgood/workspace/genewiki/genewikitools/static/gwtweetsrss.xml", 5000, true, rc);
//		int interval = 120;
//		int max_times = -1;
//		int seconds2goback = 5000;
//		startWikiWatcher(interval, max_times, seconds2goback, titles, creds);
		
		
		List<GWRevision> newones = rc.checkListForRevisionsInRange(latest, earliest, titles);
		if(newones!=null){
			System.out.println("Found "+newones.size());
			for(GWRevision gr : newones){
				String title = gr.getTitle();
				System.out.print(title+" ");
				Map<String,Tweetables> rev_tweetables = RevisionStream.getTweetables(title, earliest, latest, rc);
				for(Entry<String, Tweetables> rev_tweets : rev_tweetables.entrySet()){
					for(Tweetable tweet : rev_tweets.getValue().tweets){	
						String message = tweet.getSummary();
						System.out.println("tweeted\t"+tweet.getTimestamp()+"\t"+(message.length()+21)+"\n\t"+message+"\n"+tweet.getDifflink());
					}
				}
			}
			System.out.println();
		}
	}


	public static void startWikiWatcher(int interval, int max_times, int seconds2goback, List<String>titles, String index, Map<String, String> creds){
		Timer timer = new Timer();		
		timer.schedule(new WikiWatcherTask(max_times, seconds2goback, titles, index, creds), interval * 1000, interval * 1000);

	}


	/**
	 * Runs through all the titles of the gene wiki and checks for revisions that occurred in the previous DAYSBACK.
	 * Writes out an RSS file that summarizes these revisions
	 //TODO Write revisions out to twitter stream
	 *
	 */
	public static void dailyDose(String gw_index, String rssfile, String tweetrssfile, int secondsback, boolean test, RevisionCounter rc){
		TimeZone tz = TimeZone.getTimeZone("GMT");
		Calendar latest = Calendar.getInstance(tz);
		Calendar earliest = Calendar.getInstance(tz);
		//2011-02-24T01:47:14Z
		earliest.add(Calendar.SECOND, -1*secondsback);
		earliest.setTimeZone(tz);

		Set<String> titles = new HashSet<String>();
		if(!test){
			Map<String, String> gene_wiki = GeneWikiUtils.getGeneWikiGeneIndex(gw_index, false);
			titles = new HashSet<String>(gene_wiki.values());
		}else{		
			//			titles.add("RPL38"); 
			//			titles.add("ERG_(gene)"); 
			//			titles.add("FLNA");
			//			titles.add("Apoliprotein_E");
			//			titles.add("FLI1");
			//			titles.add("CYFIP2");
			titles.add("VIPR2");
			titles.add("VIPR1");
			titles.add("Human_chorionic_gonadotropin");
		}
		int n = 0;
		List<SyndEntry> rssentries = new ArrayList<SyndEntry>();
		List<SyndEntry> tweetentries = new ArrayList<SyndEntry>();
		for(String title : titles){
			title = title.replaceAll(" ", "_");
			n++;
			if(n%100==0){
				System.out.print(n+" ");
			}
			if(n%1000==0){
				System.out.println();
			}
			//get data
			Map<String,Tweetables> rev_tweetables = getTweetables(title, earliest, latest, rc);
			//write data - each important revision gets one RSS update, within each revision we may get multiple tweets

			for(Entry<String, Tweetables> rev_tweets : rev_tweetables.entrySet()){

				SyndEntry entry = new SyndEntryImpl();				
				SyndContent description = new SyndContentImpl();
				description.setType("html");
				Tweetable tweety = rev_tweets.getValue().tweets.get(0);
				String[] t = rev_tweets.getKey().split(":");
				entry.setTitle(t[0]+" was edited by "+tweety.getUser());
				entry.setLink(tweety.getDifflink());
				//				entry.setUpdatedDate(Calendar.getInstance().getTime());
				//				entry.setPublishedDate(Calendar.getInstance().getTime());
				try {
					entry.setPublishedDate(DateFun.wp_format.parse(tweety.getTimestamp()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
				description.setType("text/plain");
				String rss_summary = rev_tweets.getValue().getSummaryForRSS();
				int t_n = 0;
				for(Tweetable tweet : rev_tweets.getValue().tweets){	
					t_n++;
					SyndEntry t_entry = new SyndEntryImpl();
					SyndContent t_description = new SyndContentImpl();
					String[] t_title = rev_tweets.getKey().split(":");
					t_entry.setTitle(t_title[0]+" was edited by "+tweet.getUser());
					t_entry.setLink(tweet.getDifflink());
					//need to make this distinct for each rss entry for some readers to work - hence the increment
					t_entry.setUri(tweet.getDifflink()+"_"+t_n);
					try {
						t_entry.setPublishedDate(DateFun.wp_format.parse(tweet.getTimestamp()));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					t_description.setValue(tweet.getSummary());
					t_entry.setDescription(t_description);
					//"tweet\t"+tweet.getTimestamp()+"\t"+tweet.getSummary().length()+"\n\t"+
					System.out.println(tweet.getSummary()+" "+tweet.getDifflink());
					tweetentries.add(t_entry);
				}
				description.setValue(rss_summary);
				entry.setDescription(description);
				rssentries.add(entry);
			}
		}
		RSS.writeGeneWikiFeed(rssfile, rssentries);
		RSS.writeGeneWikiFeed(tweetrssfile, tweetentries);
	}

	public static void printTweetables(List<String> titles, Calendar earliest, Calendar latest, RevisionCounter rc){
		int n = 0;
		for(String title : titles){
			n++;
			if(n%100==0){
				System.out.print(n+" ");
			}
			if(n%1000==0){
				System.out.println();
			}
			Map<String, Tweetables> rev_tweetables = getTweetables(title, earliest, latest, rc);
			for(Tweetables tweetable : rev_tweetables.values()){
				for(Tweetable tweet : tweetable.tweets){	
					System.out.println("tweet\t"+tweet.getTimestamp()+"\t"+tweet.getSummary().length()+"\t"+tweet.getSummary()+"\t"+tweet.getDifflink());
				}
			}
		}
	}

	public static Map<String, Tweetables> getTweetables(String title, Calendar earliest, Calendar latest, RevisionCounter rc){

		Map<String, Tweetables> rev_tweets = new HashMap<String, Tweetables>();

		boolean getContent = false;
		List<GWRevision> revs = rc.getRevisions(latest, earliest, title, getContent, null);
		if(revs!=null){
			if(revs.size()==1&&revs.get(0).getParentid()!=null){
				GWRevision older_parent = rc.getRevision(revs.get(0).getParentid(), false);
				revs.add(older_parent);
			}
			Collections.reverse(revs);
			String last_revid = ""; int last_bytes = 0; int n = 0;
			for(GWRevision r : revs){			
				if(n > 0){
					GWRevision curr_rev = rc.getRevision(r.getRevid(), true);
					curr_rev.setTitle(title);
					GeneWikiPage curr = new GeneWikiPage(curr_rev, rc.getUser(), true);
					GWRevision last_rev = rc.getRevision(last_revid, true);
					last_rev.setTitle(title);
					GeneWikiPage last = new GeneWikiPage(last_rev, rc.getUser(), true);
					int diff = r.getSize()-last_bytes;
					Tweetables tweets = new Tweetables(last, curr, rc);
					rev_tweets.put(title+":"+last_revid+":"+curr_rev, tweets);
				}
				last_revid = r.getRevid();
				last_bytes = r.getSize();
				n++;
			}
		}
		return rev_tweets;
	}

}
