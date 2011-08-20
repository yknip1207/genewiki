package org.genewiki;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.genewiki.api.Wiki;
import org.genewiki.db.MetricsDatabase;
import org.genewiki.util.Serialize;
import org.gnf.genewiki.GWRevision;
import org.gnf.genewiki.GeneWikiPage;
import org.gnf.genewiki.metrics.MonthlyPageViews;
import org.gnf.genewiki.metrics.PageViewCounter;
import org.gnf.genewiki.metrics.RevisionCounter;
import org.gnf.genewiki.metrics.VolumeReport;

public class MetricsCollector {

	private final String username;
	private final String password;
	private final MetricsDatabase db;
	
	public MetricsCollector(String username, String password) {
		this.username = username;
		this.password = password;
		this.db = MetricsDatabase.instance;
	}
	
	public void collectMetrics(List<String> titles, Calendar start, Calendar end) throws SQLException {
		System.out.println(end.get(Calendar.YEAR));
		List<Calendar> months = splitIntoMonths(start,end);
		RevisionCounter revCounter = new RevisionCounter(username, password);
		for (String title : titles) {
			System.out.println("Parsing "+title);
			
			// Revisions
			List<GWRevision> revs = revCounter.getRevisions(end, start, title, false, null);
			Collections.reverse(revs);
			GWRevision prev = revs.get(0);	// to start
			int count = 0;
			for (GWRevision rev : revs) {
				long rev_id = Long.parseLong(rev.getRevid());
				Calendar date = timestampToCalendar(rev.getTimestamp());
				int page_id = Integer.parseInt(rev.getParentid());
				long bytes_changed = rev.getSize() - prev.getSize();
				db.insertRevisionRow(rev_id, date, page_id, bytes_changed);
				count++;
				System.out.println("Finished "+count+"/"+revs.size());
				prev = rev;
				// Editor
				String name = rev.getUser();
				boolean is_bot = (name.toLowerCase(Locale.ENGLISH).contains("bot"));
				db.insertEditorRow(name, is_bot, page_id, bytes_changed);
			}
			
			// Page info
			GeneWikiPage page = new GeneWikiPage(username, password);
			page.setTitle(title);
			page.defaultPopulate();
			page.retrieveAllInBoundWikiLinks(true, false);
			VolumeReport report = new VolumeReport();
			report.extractVolumeFromPopulatedPage(page);
			int page_id = Integer.parseInt(page.getPageid());
			String text = page.getPageContent();
			
			// Finding the entrez id from the linked PBB template, if available
			int entrez = -1;
			try {
				int a = text.indexOf("{{PBB|geneid=") + 13;
				int b = text.indexOf("}}",a);
				entrez = Integer.parseInt(text.substring(a, b));
			} catch (IndexOutOfBoundsException e) {
			} catch (NumberFormatException e) {
				// We failed the parsing, add to failed
			}
			
			// Handling the other info...
			int num_revs = revs.size();
			int bytes = page.getSize();
			int words = (int) report.getWords();
			int links_in = (int) report.getLinks_in();
			int links_out = (int) report.getLinks_out();
			int external = (int) report.getExternal_links();
			int pubmed_refs = (int) report.getExternal_links();
			int redirects = (int) report.getRedirects();
			int sentences = (int) report.getSentences();
			int media = (int) report.getMedia_files();
			db.insertPageRow(title, page_id, entrez, num_revs);
			db.insertPageInfoRow(page_id, bytes, words, links_out, links_in, 
					external, pubmed_refs, redirects, sentences, media);
			
			for (Calendar aMonth : months) {
				MonthlyPageViews mpvs = null;
				int month = aMonth.get(Calendar.MONTH)+1;
				String monthStr = (month < 10) ? "0"+month : ""+month;
				int year = aMonth.get(Calendar.YEAR);
				try {
					String pv_json = PageViewCounter.getPageViewJson(title, 
							Integer.toString(year), monthStr);
					mpvs = PageViewCounter.parsePageViews(pv_json);
				} catch (UnsupportedEncodingException e) { /*ignore*/ }
				
				int views = (int) mpvs.getMonthly_total();
				System.out.println("views: "+ views);
				db.insertPageViewRow(page_id, month, year, views);
			}
		}
	}
	
	public Calendar timestampToCalendar(String timestamp) {
		GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		int year = Integer.parseInt(timestamp.substring(0,4));
		int month = Integer.parseInt(timestamp.substring(5,7));
		int day = Integer.parseInt(timestamp.substring(8,10));
		int hour = Integer.parseInt(timestamp.substring(11, 13));
		int minute = Integer.parseInt(timestamp.substring(14,16));
		calendar.set(year, month, day, hour, minute);
		return calendar;
	}
	
	public List<Calendar> splitIntoMonths(Calendar start, Calendar end) {
		int startMonth = start.get(Calendar.MONTH);
		int startYear = start.get(Calendar.YEAR);
		int endMonth = end.get(Calendar.MONTH);
		int endYear = end.get(Calendar.YEAR);
		List<Calendar> list = new ArrayList<Calendar>();
		// example: 2011-2009+1 = 3*12 = 36-(12-8)-8 = 22+2 (due to starting at 0) = 24
		int months = (endYear-startYear+1)*12-(12-startMonth)-endMonth;
		
		for (int i=0; i < months; i++) {
			Calendar cal = copyCalendar(start);
			cal.add(Calendar.MONTH, i);
			list.add(cal);
		}
		return list;
	}
	
	public Calendar copyCalendar(Calendar origin) {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.setTime(origin.getTime());
		return cal;
	}
	
	/* --- ENTRY POINT --- */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws FileNotFoundException, SQLException {
		MetricsCollector collector = new MetricsCollector("genewikiplus", "Nv6w7QbL");
		System.out.println("Getting pages...");
		ArrayList<String> titles = (ArrayList<String>) Serialize.in("gwtitles.list.string");
		System.out.println("read serialized pages...");		
		Calendar start = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		start.set(2009, 8, 15, 0, 0, 0);
		start.getTime();
		GregorianCalendar end = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		end.set(2011, 8, 15, 0, 0, 0);
		end.getTime();
		collector.collectMetrics(titles, start, end);
	}
	
}


