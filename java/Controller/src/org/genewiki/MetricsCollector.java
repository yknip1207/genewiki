package org.genewiki;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.genewiki.concurrent.ThreadGenerator;
import org.genewiki.db.MetricsDatabase;
import org.genewiki.metrics.MonthlyPageViews;
import org.genewiki.metrics.PageViewCounter;
import org.genewiki.metrics.RevisionCounter;
import org.genewiki.metrics.VolumeReport;
import org.genewiki.util.FileHandler;
import org.genewiki.util.Serialize;
import org.genewiki.util.Stopwatch;
import org.gnf.wikiapi.User;

public class MetricsCollector implements ThreadGenerator {

	private String username;
	private String password;
	private List<String> titles, completed, failed;
	private Calendar start, end;
	private int id;
	private String mode;
	private MetricsDatabase db;
	
	public MetricsCollector() {
		this(null);
	}
	
	public MetricsCollector(String mode) {
		start = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		start.clear();
		start.set(2009, Calendar.SEPTEMBER, 1, 0, 0, 0);
		start.getTime();
		end = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		end.clear();
//		end.set(2011, Calendar.SEPTEMBER, 1, 0, 0, 0);
		end.set(2011, Calendar.SEPTEMBER, 1, 0, 0, 0);	// FIXME this is only set to fill in early revisions
		end.getTime();
		username = "genewikiplus";
		password = "Nv6w7QbL";
		this.mode = mode;
	}
	
	private MetricsCollector(List<String> titles, 
			List<String> completed, List<String> failed, Calendar start, Calendar end,
			String user, String pass, int id, String mode) {
		this.titles = titles;
		this.completed = completed;
		this.failed = failed;
		this.db = MetricsDatabase.instance;
		this.start = start;
		this.end = end;
		this.username = user;
		this.password = pass;
		this.mode = mode;
	}
	
	public void printMonths() {
		List<Calendar> months = splitIntoMonths(start, end);
		for (Calendar month : months){ 
			System.out.println(month.getTime().toString()+" : "+month.getTimeInMillis());

		}
		System.out.println(months.size());
	}
	
	public void fixMonths() {
		List<Calendar> months = splitIntoMonths(start, end);
		for (int i = 0; i < months.size(); i++ ) {
			long a = 0;
			long b = 0;
			try {
				a = months.get(i).getTimeInMillis();
				b = months.get(i+1).getTimeInMillis();
			} catch (IndexOutOfBoundsException e) {
				a = months.get(i).getTimeInMillis();
				b = a*2; // just something sufficiently larger than "a" to give us the rest of the dates
			}
			double a2 = a/1000;
			a = Math.round(a2)*1000;
			double b2 = b/1000;
			b = Math.round(b2)*1000;
			
			try {
				System.out.println("Fixing dates between "+a+" and "+b);
				db.fixMonths(a, b);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
	}
	
//	public void fixMonths() {
//		try {
//			db.fixMonths();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	public void fixPageIds() {
		RevisionCounter revCounter = new RevisionCounter(username, password);
		User user = revCounter.getUser();
		int count = 0;
		for (String title : titles) {
			System.out.println("fixing "+title+": "+count);
			if (completed.contains(title)) {
				System.out.println("Skipping "+title+": already done.");
				break;
			} else if (failed.contains(title)) {
				break;
			}
			try {
				GeneWikiPage mPage = new GeneWikiPage(username, password);
				mPage.setTitle(title);
				mPage.defaultPopulate();
				mPage.retrieveWikiTextContent(false);
				mPage.parseAndSetNcbiGeneId();
				int page_id = Integer.parseInt(mPage.getPageid());
				int entrez = -1;
				try {
					entrez = Integer.parseInt(mPage.getNcbi_gene_id());
				} catch (Exception e) {
					String text = mPage.getPageContent();
					int a = text.indexOf("{{PBB|geneid=") + 13;
					int b = text.indexOf("}}",a);
					entrez = Integer.parseInt(text.substring(a, b));
				}
				db.fixPageIds(title, page_id, entrez);
				completed.add(title);	
				count++;
			} catch (Exception e) {
				failed.add(title);
				e.printStackTrace();
				Serialize.out("failed.list.string", new ArrayList<String>(failed));
			}
		}
	}
	
	public void addSept2011() {
		Calendar sept = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		sept.set(2011, Calendar.SEPTEMBER, 0, 0, 0, 0);	// Month is zero-based, Aug = 7
		sept.getTime();
		RevisionCounter revCounter = new RevisionCounter(username, password);
		User user = revCounter.getUser();
		for (String title : titles) {
			if (completed.contains(title)) {
				System.out.println("Skipping "+title+": already done.");
				break;
			} else if (failed.contains(title)) {
				break;
			}
			try {
				
//				// General info
//				GeneWikiPage page = new GeneWikiPage(username, password);
//				page.setTitle(title);
////				page.setUseTrust(false);
//				page.defaultPopulate();
//				int page_id = Integer.parseInt(page.getPageid()); 
//				// Finding the entrez id from the linked PBB template, if available
//				String text = page.getPageContent();
//				int entrez = -1;
//				try {
//					int a = text.indexOf("{{PBB|geneid=") + 13;
//					int b = text.indexOf("}}",a);
//					entrez = Integer.parseInt(text.substring(a, b));
//				} catch (IndexOutOfBoundsException e) {
//				} catch (NumberFormatException e) {
//					failed.add(title);
//					e.printStackTrace();
//					Serialize.out("failed.list.string", new ArrayList<String>(failed));
//				}
				
				// Monthly info
				System.out.println(sept.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US)+": "+title);
				GWRevision rev = revCounter.getRevisionOnDay(sept, title, true);
				List<GWRevision> mRevs = revCounter.getRevisions(sept, start, title, false, null);
				GeneWikiPage mPage = new GeneWikiPage(rev, user, true);
				mPage.retrieveWikiTextContent(false);
				mPage.retrieveAllInBoundWikiLinks(true, false);
				VolumeReport report = new VolumeReport();
				report.extractVolumeFromPopulatedPage(mPage);
				int page_id = Integer.parseInt(mPage.getPageid());
				int entrez = -1;
				try { 
					entrez = Integer.parseInt(mPage.getNcbi_gene_id().trim()); 
				} catch (Exception e) {
					try {
						String text = mPage.getPageContent();
						int a = text.indexOf("{{PBB|geneid=") + 13;
						int b = text.indexOf("}}",a);
						entrez = Integer.parseInt(text.substring(a, b));
					} catch (Exception e2){
						failed.add(title);
						e2.printStackTrace();
						Serialize.out("failed.list.string", new ArrayList<String>(failed));
					}
				}
				int num_revs = mRevs.size();
				int bytes = mPage.getSize();
				int words = (int) report.getWords();
				int links_in = (int) report.getLinks_in();
				int links_out = (int) report.getLinks_out();
				int external = (int) report.getExternal_links();
				int pubmed_refs = (int) report.getPubmed_refs();
				int redirects = (int) report.getRedirects();
				int sentences = (int) report.getSentences();
				int media = (int) report.getMedia_files();
				org.genewiki.api.Wiki.Revision revision = StatusMonitor.instance.getWiki().getFirstRevision(title);
				Calendar created = revision.getTimestamp();
				String creator = revision.getUser();
				db.insertMonthlyRow(page_id, title, entrez, num_revs, bytes, words, links_out, links_in, 
						external, pubmed_refs, redirects, sentences, media, created, creator, sept);
				completed.add(title);				
				Serialize.out("completed.list.string", new ArrayList<String>(completed));
			} catch (SQLException e) {
				failed.add(title);
				e.printStackTrace();
				Serialize.out("failed.list.string", new ArrayList<String>(failed));
			} catch (IOException e) {
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
	
	public void updateMetrics() {
		FileHandler fh = new FileHandler();
		List<Calendar> months = splitIntoMonths(start,end);
		Calendar august = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		august.set(2011, 7, 0, 0, 0, 0);	// Month is zero-based, Aug = 7
		august.getTime();
		Calendar september = copyCalendar(august);
		september.add(Calendar.MONTH, 1);
		RevisionCounter revCounter = new RevisionCounter(username, password);
		User user = revCounter.getUser();
		for (String title : titles) {
			if (completed.contains(title)) {
				System.out.println("Skipping "+title+": already done.");
				break;
			} else if (failed.contains(title)) {
				break;
			}
			try {
				
//				// Handling revisions
//				System.out.println(id+": "+title+": updating page info...");
//				List<GWRevision> revs = revCounter.getRevisions(end, start, title, false, null);
				GeneWikiPage page = new GeneWikiPage(username, password);
				page.setTitle(title);
//				page.setUseTrust(false);
				page.defaultPopulate();
				int page_id = Integer.parseInt(page.getPageid()); 
				// Finding the entrez id from the linked PBB template, if available
				String text = page.getPageContent();
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
//				// Revisions
//				System.out.println(id+": "+title+": handling revisions...");
//				Collections.reverse(revs);
//				GWRevision prev = new GWRevision();
//				prev.setSize(0);
//				for (GWRevision rev : revs) {
//					long rev_id = Long.parseLong(rev.getRevid());
//					Calendar date = timestampToCalendar(rev.getTimestamp());
//					long bytes_changed = rev.getSize() - prev.getSize();
//					prev = rev;
//					// Editor
//					String name = rev.getUser();
//					boolean is_bot = (name.toLowerCase(Locale.ENGLISH).contains("bot"));
//					db.insertRevisionRow(rev_id, date, page_id, bytes_changed, name, is_bot);
//				}
				
				// Handling monthly stats
				for (Calendar month : months) {
					try {
						System.out.println(month.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US)+": "+title);
						GWRevision rev = revCounter.getRevisionOnDay(month, title, true);
						List<GWRevision> mRevs = revCounter.getRevisions(month, start, title, false, null);
						GeneWikiPage mPage = new GeneWikiPage(rev, user, true);


						mPage.retrieveAllInBoundWikiLinks(true, false);
						VolumeReport report = new VolumeReport();
						report.extractVolumeFromPopulatedPage(mPage);
						
						int num_revs = mRevs.size();
						int bytes = mPage.getSize();
						int words = (int) report.getWords();
						int links_in = (int) report.getLinks_in();
						int links_out = (int) report.getLinks_out();
						int external = (int) report.getExternal_links();
						int pubmed_refs = (int) report.getPubmed_refs();
						int redirects = (int) report.getRedirects();
						int sentences = (int) report.getSentences();
						int media = (int) report.getMedia_files();
						org.genewiki.api.Wiki.Revision revision = StatusMonitor.instance.getWiki().getFirstRevision(title);
						Calendar created = revision.getTimestamp();
						String creator = revision.getUser();
						db.insertMonthlyRow(page_id, title, entrez, num_revs, bytes, words, links_out, links_in, 
								external, pubmed_refs, redirects, sentences, media, created, creator, month);
					} catch (NullPointerException e) {
						//fh.write(title+":"+month.getTime().toString()+"\n", "monthFailed", 'a');
						//System.out.println("Failed: "+title+":"+month.getTime().toString());
						continue;
					}
					
				}
				
				// Handling updating pageviews for August 2011
//				int aug = august.get(Calendar.MONTH)+1;
//				String monthStr = (aug < 10) ? "0"+aug : ""+aug;
//				int year = august.get(Calendar.YEAR);
//				MonthlyPageViews mpvs = null;
//				try {
//					String pv_json = PageViewCounter.getPageViewJson(title, 
//							Integer.toString(year), monthStr);
//					mpvs = PageViewCounter.parsePageViews(pv_json);
//				} catch (UnsupportedEncodingException e) { /*ignore*/ }
//				int views = (int) mpvs.getMonthly_total();
//				int rev_count = db.getRevisionsBetweenDates(page_id, august, september);
//				db.updatePageViewRow(page_id, aug, year, views, rev_count);
				
				// Add to list of completed so we don't get redundancies
				completed.add(title);				
				Serialize.out("completed.list.string", new ArrayList<String>(completed));
			} catch (SQLException e) {
				failed.add(title);
				e.printStackTrace();
				Serialize.out("failed.list.string", new ArrayList<String>(failed));
			} catch (IOException e) {
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
	
	public void remakeRevisionsTable() {
		RevisionCounter revCounter = new RevisionCounter(username, password);
		SimpleDateFormat wp_time = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		TimeZone tz = TimeZone.getTimeZone("GMT");
		wp_time.setTimeZone(tz);
		for (String title : titles) {
			
			if (completed.contains(title)) {
				break;
			} else if (failed.contains(title)) {
				break;
			}
			try {
				// Revisions
				System.out.println(completed.size()+": "+title+": handling revisions...");
				List<GWRevision> revs = revCounter.getRevisions(end, start, title, false, null);
				Collections.reverse(revs);
				GWRevision prev = revs.get(0);	// to start
				for (GWRevision rev : revs) {
					long rev_id = Long.parseLong(rev.getRevid());
					Calendar date = Calendar.getInstance();
					date.setTime(wp_time.parse(rev.getTimestamp()));
					
					long bytes_changed = rev.getSize() - prev.getSize();
					long rev_size = rev.getSize();
					prev = rev;
					// Editor
					String name = rev.getUser();
					boolean is_bot = (name.toLowerCase(Locale.ENGLISH).contains("bot"));
					db.insertRevisionRow2(rev_id, date, title, bytes_changed, rev_size, name, is_bot);
				}
				completed.add(title);
				Serialize.out("completed.list.string", new ArrayList<String>(completed));
			} catch (IndexOutOfBoundsException e) {
				System.out.println(title+" did not exist in this timespan.");
				failed.add(title);	
				Serialize.out("failed.list.string", new ArrayList<String>(failed));
			} catch (Exception e) {
				failed.add(title);	
				e.printStackTrace();
				Serialize.out("failed.list.string", new ArrayList<String>(failed));
			}
		}
	}
	
	
	public void completeAugustPageViews() {
		Calendar august = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		august.set(2011, 7, 0, 0, 0, 0);	// Month is zero-based, Aug = 7
		august.getTime();
		int aug = 8;
		MonthlyPageViews mpvs = null;
		for (String title : titles) {
			if (completed.contains(title)) {
				break;
			} else if (failed.contains(title)) {
				break;
			}
			try {
				GeneWikiPage mPage = new GeneWikiPage();
				mPage.setTitle(title);
				mPage.defaultPopulate();
				int page_id = Integer.parseInt(mPage.getPageid());
				String pv_json = PageViewCounter.getPageViewJson(title, "2011", "08");
				mpvs = PageViewCounter.parsePageViews(pv_json);
				int views = (int) mpvs.getMonthly_total();
				Calendar september = copyCalendar(august);
				september.add(Calendar.MONTH, 1);
				int rev_count = db.getRevisionsBetweenDates(page_id, august, september);
				db.updatePageViewRow(page_id, aug, 2011, views, rev_count);
				System.out.println("Updating "+title+": "+completed.size());
				completed.add(title);
				Serialize.out("completed.list.string", new ArrayList<String>(failed));
			} catch (Exception e) {
				failed.add(title);	
				e.printStackTrace();
				Serialize.out("failed.list.string", new ArrayList<String>(failed));
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
				org.genewiki.api.Wiki.Revision revision = StatusMonitor.instance.getWiki().getFirstRevision(title);
				Calendar created = revision.getTimestamp();
				String creator = revision.getUser();
				db.insertPageInfoRow(page_id, title, entrez, num_revs, bytes, words, links_out, links_in, 
						external, pubmed_refs, redirects, sentences, media, created, creator);
				
				
				System.out.println(id+": "+title+": handling pageviews...");
				// Handle pageviews
				Stopwatch timer = new Stopwatch();
				
				for (Calendar aMonth : months) {
					MonthlyPageViews mpvs = null;
					int month = aMonth.get(Calendar.MONTH)+1;
					String monthStr = (month < 10) ? "0"+month : ""+month;
					int year = aMonth.get(Calendar.YEAR);
					timer.start();
					try {
						String pv_json = PageViewCounter.getPageViewJson(title, 
								Integer.toString(year), monthStr);
						mpvs = PageViewCounter.parsePageViews(pv_json);
					} catch (UnsupportedEncodingException e) { /*ignore*/ }
					timer.stop();
					System.out.println(id+": "+title+": took "+timer.time()+"ms to fetch pageviews.");
					int views = (int) mpvs.getMonthly_total();
					Calendar next = copyCalendar(aMonth);
					next.add(Calendar.MONTH, 1);
//					List<GWRevision> revs2 = revCounter.getRevisions(next, aMonth, title, false, null);
					int rev_count = db.getRevisionsBetweenDates(page_id, aMonth, next);
					
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
		int month = Integer.parseInt(timestamp.substring(5,7))-1;
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
		if (mode.equals("continue")){
			collectMetrics();
		} else if (mode.equals("update")) {
			System.out.println("Updating...");
			remakeRevisionsTable();
		}
	}
	
	@SuppressWarnings("unchecked")
	public Runnable newRunnable(List<?> tasks, List<String> completed, List<String> failed, int id) {
		return new MetricsCollector((List<String>) tasks, completed, failed, start, end, username, password, id, new String(mode));
	}
	
}




