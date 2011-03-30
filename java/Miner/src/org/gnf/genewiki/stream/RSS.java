package org.gnf.genewiki.stream;

import com.ibm.icu.util.Calendar;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedOutput;

import java.io.FileWriter;
import java.io.Writer;

import java.util.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class RSS {
	private static final DateFormat DATE_PARSER = new SimpleDateFormat("yyyy-MM-dd");
	
	public static void writeGeneWikiFeed(String fileName, List<SyndEntry> entries) {
		try {
			String feedType = "rss_2.0";
//			String fileName = "/Users/bgood/workspace/genewiki/genewikitools/static/wikifeed2.xml";

			SyndFeed feed = new SyndFeedImpl();
			feed.setFeedType(feedType);

			feed.setTitle("Gene Wiki Action!");
			feed.setLink("http://en.wikipedia.org/wiki/Portal:Gene_Wiki");
			feed.setDescription("Hot sexy human gene article updates");
			feed.setPublishedDate(Calendar.getInstance().getTime());			
			
			feed.setEntries(entries);

			Writer writer = new FileWriter(fileName);
			SyndFeedOutput output = new SyndFeedOutput();
			output.output(feed,writer);
			writer.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
