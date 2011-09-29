package org.genewiki.stream;

import static com.rosaloves.bitlyj.Bitly.as;
import static com.rosaloves.bitlyj.Bitly.shorten;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TimerTask;
import java.util.Map.Entry;

import org.genewiki.GWRevision;
import org.genewiki.GeneWikiUtils;
import org.genewiki.metrics.RevisionCounter;
import org.scripps.util.DateFun;

import com.rosaloves.bitlyj.Url;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;

/**
 * Monitor the genewikibot watchlist for recent changes.  
 * Post change summaries to twitter
 * Update the watcher as new articles are added to the gene wiki
 * @author bgood
 *
 */
public class WatchlistTweeterTask extends TimerTask {
	int max;
	int n;
	Calendar earliest;
	Calendar lastchecked;
	RevisionCounter rc;
	Shortener shorty;
	Map<String, String> creds_;
	WatchlistManager wl_man;
	boolean clean_non_gw_off_watchlist;

	WatchlistTweeterTask(int max_times,int seconds2goback, Map<String, String> creds){
		super();
		max = max_times;
		n = 0;
		earliest = Calendar.getInstance();
		earliest.add(Calendar.SECOND, -1*seconds2goback);
		lastchecked = earliest;
		rc = new RevisionCounter(creds.get("wpid"), creds.get("wppw"));
		wl_man = new WatchlistManager(creds.get("wpid"), creds.get("wppw"), null);
		clean_non_gw_off_watchlist = false;
		creds_ = creds;
		shorty = new Shortener(creds);
		Tweeter.initTweeter(creds);
		System.out.println("Initialized watcher");
		run();
	}

	@Override
	public void run() {
	//	if(wl_man.user!=null&&wl_man.user.login())
		//every so often, update the watchlist
		if(n%15==0){
			System.out.println("Updating watchlist ");			
			wl_man.user.login();
			wl_man.updateGeneWikiWatchList(clean_non_gw_off_watchlist);
		}
		SimpleDateFormat timestamp = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss");
		TimeZone tz = TimeZone.getTimeZone("GMT");
		timestamp.setTimeZone(tz);
		long start = System.currentTimeMillis();
		Calendar now = Calendar.getInstance();
		System.out.println(n+", Checking for edits from: "+timestamp.format(lastchecked.getTime())+" to now "+timestamp.format(now.getTime()));

		List<GWRevision> newones = wl_man.getRecentChangesFromWatchlist(lastchecked, 50, 50, false);
		lastchecked = Calendar.getInstance();
		//process revisions to make tweets
		if(newones!=null){
			System.out.println("Found "+newones.size());
			for(GWRevision gr : newones){
				String title = gr.getTitle();
				System.out.print(title+" ");
				Map<String,Tweetables> rev_tweetables = null;
				try{
					rev_tweetables = RevisionStream.getTweetables(title, earliest, Calendar.getInstance(), rc);
				}catch(Exception e){
					System.err.println("Something went wrong getting the tweetables ");
					e.printStackTrace();
				}
				if(rev_tweetables!=null){
					for(Entry<String, Tweetables> rev_tweets : rev_tweetables.entrySet()){
						for(Tweetable tweet : rev_tweets.getValue().tweets){	

							try{
								Url url = as(shorty.user, shorty.key).call(shorten(tweet.getDifflink()));
								String shorter = url.getShortUrl();
								String s = tweet.getSummary();
								if(s.length()+1+shorter.length()>140){
									s = s.substring(0,(139-shorter.length()));
								}
								String message = s+" "+shorter;
								Tweeter.tweet(message);
								System.out.println(message);
							}catch (Exception e){
								System.err.println("Something went wrong sending the tweet or shortening the URL. ");
								System.out.println(Tweeter.accSecret+"\n"+Tweeter.accToken+"\n"+Tweeter.consKey+"\n"+Tweeter.consSecret);
								e.printStackTrace();
							}
							try {
								Thread.currentThread().sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							//			System.out.println("tweeted\t"+tweet.getTimestamp()+"\t"+message.length()+"\n\t"+message);

						}
					}
				}
			}
			System.out.println();
		}
		long stop = System.currentTimeMillis();
		System.out.println("Finished checking in "+((stop-start)/(1000))+" seconds");
		n++;
		if(n==max){
			System.out.println("DONE! "+n);
			System.exit(0);
		}
		System.out.println("Cycle complete");
	}

}
