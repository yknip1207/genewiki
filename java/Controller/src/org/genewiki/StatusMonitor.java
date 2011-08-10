package org.genewiki;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.login.FailedLoginException;

import org.genewiki.api.Wiki;
import org.gnf.genewiki.GWRevision;
import org.gnf.genewiki.stream.WatchlistManager;

/**
 * Provides a singleton instance to get information about the
 * Genewiki from Wikipedia. Time-sensitive methods require
 * authentication with a user who has the entire Genewiki page 
 * set in their watchlist.
 * @author eclarke
 *
 */
public enum StatusMonitor {

	instance;
	
	private final Wiki wikipedia;
	private final String wpUser = "genewikiplus";
	private final String wpPass = "myPassword";
	
	private static final String template = "Template:GNF_Protein_box";
	
	private StatusMonitor() {
		System.out.println("Initializing status monitor...");
		wikipedia = new Wiki();
		wikipedia.setMaxLag(10);
		try {
			wikipedia.login(wpUser, wpPass.toCharArray());
		} catch (FailedLoginException e) {
			throw new RuntimeException("Bad credentials.");
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
	
	/**
	 * Searches for everything that transcludes the GNF_Protein_box
	 * template in namespace 0 (articles)
	 * @return list of titles for the corresponding articles
	 */
	public List<String> getAllGeneWikiPages() {
	
		try {	
			String[] GWPages = wikipedia.whatTranscludesHere(template, 0);
			List<String> GWPageTitles = Arrays.asList(GWPages);
			return GWPageTitles;
		} catch (IOException e) {
			e.printStackTrace();
			return Collections.emptyList();
		} 

	}
	
	/**
	 * Searches for all the links on the given list of pages.
	 * @param primaryPages
	 * @return complete set (not self-exclusive) of outbound links
	 * @throws IOException if there's a problem reading from wikipedia
	 */
	public List<String> getOutboundLinks(List<String> primaryPages) throws IOException {
		
		List<String> outboundLinks = new ArrayList<String>();
		for (String title : primaryPages) {
			String[] outbound = wikipedia.getLinksOnPage(title);
			outboundLinks.addAll(Arrays.asList(outbound));
		}
		return Collections.synchronizedList(outboundLinks);
	
	}
	
	/**
	 * Queries the user's watchlist whose account was used to log into 
	 * Wikipedia and returns the titles of pages edited within the specified
	 * span of time.
	 * @param minutesAgo number of minutes since current time
	 * @return list of titles from watchlist most recently edited (no duplicates)
	 */
	public List<String> getRecentChanges(int minutesAgo) {
		
		System.err.println("Getting changes since "+minutesAgo+" minutes ago.");
		WatchlistManager watcher = new WatchlistManager(wpUser, wpPass, null);
		Calendar past = Calendar.getInstance();
		past.add(Calendar.MINUTE, -minutesAgo);
		List<GWRevision> live = watcher.getRecentChangesFromWatchlist(past, 50, 50*minutesAgo);
		Set<String> changed = new HashSet<String>(live.size());
		for (GWRevision rev : live) {
			changed.add(rev.getTitle());
		}
		return new ArrayList<String>(changed);	
	}
	
}