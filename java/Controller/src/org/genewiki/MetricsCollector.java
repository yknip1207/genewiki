package org.genewiki;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.genewiki.concurrent.ThreadGenerator;
import org.genewiki.db.MetricsDatabase;
import org.genewiki.util.Serialize;
import org.gnf.genewiki.GWRevision;
import org.gnf.genewiki.GeneWikiPage;
import org.gnf.genewiki.associations.CandidateAnnotation;
import org.gnf.genewiki.mapping.GeneWikiPageMapper;
import org.gnf.genewiki.metrics.MonthlyPageViews;
import org.gnf.genewiki.metrics.PageViewCounter;
import org.gnf.genewiki.metrics.RevisionCounter;
import org.gnf.genewiki.metrics.VolumeReport;

public class MetricsCollector implements ThreadGenerator {

	private String username;
	private String password;
	private List<String> titles, completed, failed;
	private Calendar start, end;
	private int id;
	
	private MetricsDatabase db;
	
	public MetricsCollector() {
		start = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		start.set(2009, 8, 15, 0, 0, 0);
		start.getTime();
		end = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		end.set(2011, 8, 15, 0, 0, 0);
		end.getTime();
		username = "genewikiplus";
		password = "Nv6w7QbL";
	}
	
	private MetricsCollector(List<String> titles, 
			List<String> completed, List<String> failed, Calendar start, Calendar end,
			String user, String pass, int id) {
		this.titles = titles;
		this.completed = completed;
		this.failed = failed;
		this.db = MetricsDatabase.instance;
		this.start = start;
		this.end = end;
		this.username = user;
		this.password = pass;
	}
	
	public void updatePageInfo() {
		List<Calendar> monthList = splitIntoMonths(start,end);
		for (Calendar month : monthList) {
			int fMonth = month.get(Calendar.MONTH);
			int fYear = month.get(Calendar.YEAR);
			month.set(fYear, fMonth, 1);
		}
		for (String title : titles) {
			if (completed.contains(title) || failed.contains(title))
				break;
			
			try {
				System.out.print("Updating "+title+"... ");
				GeneWikiPage page = new GeneWikiPage(username, password);
				page.setTitle(title);
				page.defaultPopulate();
				VolumeReport report = new VolumeReport();
				report.extractVolumeFromPopulatedPage(page);
				int page_id = Integer.parseInt(page.getPageid());
				int pubmed_refs = (int) report.getPubmed_refs();
				int bytes = (int) page.getSize();
				db.updatePageInfo(page_id, pubmed_refs, bytes);
				
				
				for (Calendar month : monthList) {
				
					int fMonth = month.get(Calendar.MONTH) + 1;
					int fYear = month.get(Calendar.YEAR);
					Calendar next = copyCalendar(month);
					next.add(Calendar.MONTH, 1);
					int rev_count = db.getRevisionsBetweenDates(page_id, month, next);
					db.updatePageViews(page_id, rev_count, fMonth, fYear);
				}
				System.out.println("done.");
			} catch (SQLException e) {
				failed.add(title);
				e.printStackTrace();
			} catch (Exception e) {
				failed.add(title);
				e.printStackTrace();
			}
			
		}
	}
	
	public void collectMetrics() {
		System.out.println(end.get(Calendar.YEAR));
		List<Calendar> months = splitIntoMonths(start,end);
		RevisionCounter revCounter = new RevisionCounter(username, password);
		for (String title : titles) {
			if (completed.contains(title)) {
				break;
			} else if (failed.contains(title)) {
				break;
			}
			try {
				System.out.println("Parsing "+title);
				
				// Revisions
				System.out.println(id+": "+title+": handling revisions...");
				List<GWRevision> revs = revCounter.getRevisions(end, start, title, false, null);
				Collections.reverse(revs);
				GWRevision prev = revs.get(0);	// to start
				for (GWRevision rev : revs) {
					long rev_id = Long.parseLong(rev.getRevid());
					Calendar date = timestampToCalendar(rev.getTimestamp());
					int page_id = Integer.parseInt(rev.getParentid());
					long bytes_changed = rev.getSize() - prev.getSize();
					prev = rev;
					// Editor
					String name = rev.getUser();
					boolean is_bot = (name.toLowerCase(Locale.ENGLISH).contains("bot"));
					db.insertRevisionRow(rev_id, date, page_id, bytes_changed, name, is_bot);
				}
				
				System.out.println(id+": "+title+": handling page info...");
				// Page info
				GeneWikiPage page = new GeneWikiPage(username, password);
				page.setTitle(title);
				page.setUseTrust(true);
				page.defaultPopulateWikiTrust();
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
					failed.add(title);
					e.printStackTrace();
					Serialize.out("failed.list.string", new ArrayList<String>(failed));
				}
				
				// Handling the other info...
				int num_revs = revs.size();
				int bytes = page.getSize();
				int words = (int) report.getWords();
				int links_in = (int) report.getLinks_in();
				int links_out = (int) report.getLinks_out();
				int external = (int) report.getExternal_links();
				int pubmed_refs = (int) report.getPubmed_refs();
				int redirects = (int) report.getRedirects();
				int sentences = (int) report.getSentences();
				int media = (int) report.getMedia_files();
				db.insertPageInfoRow(page_id, title, entrez, num_revs, bytes, words, links_out, links_in, 
						external, pubmed_refs, redirects, sentences, media);
				
				
				System.out.println(id+": "+title+": handling pageviews...");
				// Handle pageviews
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
					Calendar next = copyCalendar(aMonth);
					next.add(Calendar.MONTH, 1);
					List<GWRevision> revs2 = revCounter.getRevisions(next, aMonth, title, false, null);
					int rev_count = revs2.size();
					
					db.insertPageViewRow(page_id, month, year, views, rev_count);
				}
				/*
				// Handle ontology terms
				List<CandidateAnnotation> annotations = 
						GeneWikiPageMapper.annotateArticleNCBO(page, true, false, false, false, false);
				for (CandidateAnnotation annotate : annotations) {
					
				}
				*/
				completed.add(title);
			} catch (SQLException e) {
				failed.add(title);
				e.printStackTrace();
				Serialize.out("failed.list.string", new ArrayList<String>(failed));
			} catch (RuntimeException e) {
				failed.add(title);
				e.printStackTrace();
				Serialize.out("failed.list.string", new ArrayList<String>(failed));
			} catch (Exception e) {
				failed.add(title);	
				e.printStackTrace();
				Serialize.out("failed.list.string", new ArrayList<String>(failed));
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
	
	public void run() {
		collectMetrics();
	}
	
	@SuppressWarnings("unchecked")
	public Runnable newRunnable(List<?> tasks, List<String> completed, List<String> failed, int id) {
		return new MetricsCollector((List<String>) tasks, completed, failed, start, end, username, password, id);
	}
	
}




