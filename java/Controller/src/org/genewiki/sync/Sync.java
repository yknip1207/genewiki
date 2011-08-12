package org.genewiki.sync;

import org.genewiki.StatusMonitor;
import org.genewiki.api.*;
import org.genewiki.api.Wiki.Revision;
import org.genewiki.concurrent.RunnableFactory;
import org.genewiki.util.Serialize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;



/**
 * Syncs the Gene Wiki pages (and one layer of their links) with a specified external MediaWiki
 * @author eclarke
 *
 */
public class Sync implements RunnableFactory, Runnable {
	
	
	private Wiki wikipedia;
	private final String wpUser = "Genewikiplus";
	private final String wpPass = "..";
	
	private Wiki target;
	private final String tgUser = "GWPAdmin";
	private final String tgPass = "..";
	
//	private Set<String> outboundLinks;
	
	private List<String> updates;	// true = recent, false = all
	
	private String id = null;
	
	private Sync(List<String> targets, String id) {
		this.updates = targets;
		this.id = id;
		try {
			print(id, "Logging into Wikipedia... ");
			wikipedia = new Wiki();
			wikipedia.setMaxLag(10);
			wikipedia.login(wpUser, wpPass.toCharArray());
			println("done.");
			
			print(id, "Logging into target... ");
			target = new Wiki("genewikiplus.org:8080");
			target.setUsingCompressedRequests(false);
			target.setMaxLag(0);
			target.setThrottle(0);
			target.login(tgUser, tgPass.toCharArray());
			println("done.");
			
		} catch (FailedLoginException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns an instance to serve as a RunnableFactory.
	 */
	public Sync() {
		// Nothing needs to happen.
	}
	
	/**
	 * 
	 * @param wikipediaTitles
	 * @param wikiURL
	 * @return a list of strings that were successfully copied
	 */
	public List<String> writeToNewWiki(List<String> wikipediaTitles) {
		List<String> successful = new ArrayList<String>(wikipediaTitles.size());
		int pages = wikipediaTitles.size();
		int finished = 0;
		/* ---- TEMPORARY ---- */
		
		try {
			boolean[] exists = target.exists(wikipediaTitles.toArray(new String[10]));
			
			for (String title : wikipediaTitles) {
				if (!exists[wikipediaTitles.indexOf(title)]) {
					try {
						print(id, "Syncing "+title+" to target... ");
						String pageTxt = wikipedia.getPageText(title);
						target.edit(title, pageTxt, "Syncing content from Wikipedia", false);
						println(" success!");
					} catch (RuntimeException e) {
						println(" sync failed (maybe no valid page?). Moving on.");
						continue;
					}
				} else {
					// println(id+" :: Target MediaWiki already has updated page for "+title);
				}
				
				finished++;
				println(id+":: Finished "+finished+"/"+pages);
			}
		
			
			println("Syncing complete.");
			return successful;
		} catch (IOException e) {
			e.printStackTrace();
			return successful;
		} catch (LoginException e) {
			e.printStackTrace();
			return successful;
		}
			
	}
	
	public boolean newerThanTarget(String title) {
		try {
//			Revision wp = wikipedia.getTopRevision(title);
//			Revision tar = target.getTopRevision(title);
//			if (wp.compareTo(tar) > 0) {
//				return true;
//			} else {
//				return false;
//			}
			if (false) throw new IOException();
			return true;
		} catch (IOException e) {
			// Most likely due to the target not having the requested title
			e.printStackTrace();
			return true;
		} catch (StringIndexOutOfBoundsException e) {
			return true;
		}
	}
	
	public static void print(String id, String str) {
		System.out.print(id+" :: "+str);
	}
	
	public static void println(String str) {
		System.out.println(str);
	}

	
//	public static void main(String[] args) throws IOException {
//		StatusMonitor monitor = StatusMonitor.instance;
//		List<String> pages = monitor.getAllGeneWikiPages();
////		Sync sync = new Sync(pages, "1");
////		int size = pages.size();
////		int parsed = 0;
//		List<String> outbound = monitor.getInlineLinks(pages, true, "inlineLinks.list");
//		List<String> filtered = monitor.filterLinks(outbound);
//		Serialize.out("filteredLinks.list", new ArrayList<String>(filtered));
//		System.out.println("There were a total of "+filtered.size()+" outbound links found.");
//	}

	public void run() {
		try {
			writeToNewWiki(updates);
		} catch (Exception e){
			e.printStackTrace();
			print(id, "FAILURE ---------------------------------------------- |||||");
		}
	}

	@SuppressWarnings("unchecked")
	public Runnable newRunnable(List<?> tasks, int id) {
		return new Sync((List<String>) tasks, id+"");
	}
	
	
	
}
