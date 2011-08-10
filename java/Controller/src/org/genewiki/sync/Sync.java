package org.genewiki.sync;

import org.genewiki.api.*;
import org.genewiki.api.Wiki.Revision;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;



/**
 * Syncs the Gene Wiki pages (and one layer of their links) with a specified external MediaWiki
 * @author eclarke
 *
 */
public class Sync implements Runnable {
	
	
	private Wiki wikipedia;
	private final String wpUser = "Genewikiplus";
	private final String wpPass = "myPassword";
	
	private Wiki target;
	private final String tgUser = "GWPAdmin";
	private final String tgPass = "myPassword";
	
//	private Set<String> outboundLinks;
	
	private final List<String> updates;	// true = recent, false = all
	
	public Sync(List<String> targets) {
		this.updates = targets;
		try {
			print("Logging into Wikipedia... ");
			wikipedia = new Wiki();
			wikipedia.setMaxLag(10);
			wikipedia.login(wpUser, wpPass.toCharArray());
			println("done.");
			
			print("Logging into target... ");
			target = new Wiki("localhost");
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
	 * 
	 * @param wikipediaTitles
	 * @param wikiURL
	 * @return a list of strings that were successfully copied
	 */
	public List<String> writeToNewWiki(List<String> wikipediaTitles) {
		List<String> successful = new ArrayList<String>(wikipediaTitles.size());
		int pages = wikipediaTitles.size();
		int finished = 0;
		
		try {
			println("Syncing new content to target...");
			for (String title : wikipediaTitles) {
				if (newerThanTarget(title)) {
					println("Syncing page "+title+" to target Wiki... ");
					print("  Getting page content... ");
					String pageTxt = wikipedia.getPageText(title);
					print("  Writing page content...");
					target.edit(title, pageTxt, "Syncing content from Wikipedia", false);
					println("  Done with primary page.");
				} else {
					println("Target MediaWiki already has updated page for "+title);
				}
				
				finished++;
				println("done. Finished "+finished+"/"+pages);
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
			Revision wp = wikipedia.getTopRevision(title);
			Revision tar = target.getTopRevision(title);
			if (wp.compareTo(tar) > 0) {
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			// Most likely due to the target not having the requested title
			e.printStackTrace();
			return true;
		} catch (StringIndexOutOfBoundsException e) {
			return true;
		}
	}
	
	public static void print(String str) {
		System.out.print(str);
	}
	
	public static void println(String str) {
		System.out.println(str);
	}


	public void run() {
		writeToNewWiki(updates);
	}
	
	
	
}
