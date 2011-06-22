package org.gnf.genewiki.stream;

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

import org.gnf.genewiki.GWRevision;
import org.gnf.genewiki.GeneWikiUtils;
import org.gnf.genewiki.metrics.RevisionCounter;
import org.gnf.util.DateFun;

import com.rosaloves.bitlyj.Url;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;

public class WikiWatcherTask extends TimerTask {
	int max;
	int n;
	Calendar earliest;
	Calendar latest;
	List<String> titles;
	RevisionCounter rc;
	Shortener shorty;
	String index_file;

	WikiWatcherTask(int max_times,int seconds2goback, List<String> titles_, String index_file_, Map<String, String> creds){
		super();
		max = max_times;
		n = 0;
		earliest = Calendar.getInstance();
		earliest.add(Calendar.SECOND, -1*seconds2goback);
		titles = titles_;
		index_file = index_file_;
		rc = new RevisionCounter(creds.get("wpid"), creds.get("wppw"));
		shorty = new Shortener(creds);
		Tweeter.initTweeter(creds);
		System.out.println("Initialized watcher");
		run();
	}

	@Override
	public void run() {
		//every so often, re-capture the gene wiki from the template and store the file
		if(n%15==0){
			Map<String, String> gene_wiki = GeneWikiUtils.getGeneWikiGeneIndex(index_file, false);
			//once a day rebuild from scratch - won't work on linux...
			//			if(n>0&&n%360==0){ //720 = 24 hr at 2min
			//				try{
			//					gene_wiki = GeneWikiUtils.getGeneWikiGeneIndex(index_file, true);
			//				}catch(Exception e){
			//					System.err.println("Problem trying to rebuild index from scratch: ");
			//					e.printStackTrace();
			//				}
			//				System.out.println("Completely rebuilt the gene wiki index, total size now "+gene_wiki.keySet().size()+" was "+titles.size());
			//			}
			Set<String> ntitles = new HashSet<String>(gene_wiki.values());
			System.out.println("Updated gene wiki index, total size now "+ntitles.size()+" was "+titles.size());
			if(ntitles.size()>10200){
				titles = new ArrayList<String>(ntitles);
			}
		}

		latest = Calendar.getInstance();
		SimpleDateFormat timestamp = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss");
		TimeZone tz = TimeZone.getTimeZone("GMT");
		timestamp.setTimeZone(tz);
		long start = System.currentTimeMillis();
		System.out.println(n+", Checking "+titles.size()+" articles for edits from: "+timestamp.format(earliest.getTime())+" to: "+timestamp.format(latest.getTime()));

		List<GWRevision> newones = rc.checkListForRevisionsInRange(latest, earliest, titles);
		if(newones!=null){
			System.out.println("Found "+newones.size());
			for(GWRevision gr : newones){
				String title = gr.getTitle();
				System.out.print(title+" ");
				Map<String,Tweetables> rev_tweetables = null;
				try{
					rev_tweetables = RevisionStream.getTweetables(title, earliest, latest, rc);
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
		earliest.setTime(latest.getTime());			
		n++;
		if(n==max){
			System.out.println("DONE! "+n);
			System.exit(0);
		}
	}

}
