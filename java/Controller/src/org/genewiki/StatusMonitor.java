package org.genewiki;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.FailedLoginException;

import org.genewiki.api.Wiki;
import org.genewiki.stream.WatchlistManager;
import org.genewiki.stream.WatchlistManager;
import org.scripps.util.Serialize;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

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
	private final String wpUser = "Pleiotrope";
	private final String wpPass = "";
	
	private static final String template = "Template:GNF_Protein_box";
	
	private StatusMonitor() {
		System.out.println("Initializing status monitor...");
		wikipedia = new Wiki();
		wikipedia.setMaxLag(0);
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
	 * template in namespace 0 (articles). This is a fairly expensive
	 * query, so use sparingly and cache results if possible.
	 * @return list of titles for the corresponding articles
	 */
	public List<String> getAllGeneWikiPages() {
		System.out.println("Getting all gene wiki pages...");
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
	 * Searches for all the categories and templates used on a list of
	 * pages and returns their titles as strings.
	 * @param pages
	 * @return complete set (not self-exclusive) of outbound links
	 * @throws IOException if there's a problem reading from wikipedia
	 */
	public List<String> getLinkedResources(List<String> pages) throws IOException {
		
		HashSet<String> outboundLinks = new HashSet<String>();
		int parsed = 0;
		int size = 0;
		for (String title : pages) {
//			String[] outbound = wikipedia.getLinksOnPage(title);
			String[] categories = wikipedia.getCategories(title);
			String[] templates = wikipedia.getTemplates(title);
			outboundLinks.addAll(Arrays.asList(templates));
			outboundLinks.addAll(Arrays.asList(categories));
//			outboundLinks.addAll(Arrays.asList(outbound));
			parsed++;
			System.out.println(parsed+"/"+size+" pages parsed :: "+outboundLinks.size()+" links found.");
		}
		return Collections.synchronizedList(new ArrayList<String>(outboundLinks));
	}
	
	/**
	 * Generates a new list of inline links for a given list of pages. Does not use
	 * the Serialize methods to cache any content.
	 * @param pages
	 * @return list of link titles
	 * @throws IOException
	 */
	public List<String> getInlineLinks(List<String> pages) throws IOException {
		return getInlineLinks(pages, false, null);
	}
	
	/**
	 * Generates a new list of inline links for a given list of pages. Use serialize=true
	 * and a valid filename to cache the results, as this method takes a long
	 * time for large numbers of links (such as the entire GeneWiki page list). To obtain
	 * the results of a previous serialization, or to serialize results if previous results
	 * were unavailable, specify serialize=true and the filename.
	 * To forego serialization, specify serialize=false and filename=null.
	 * @param pages
	 * @param serialize 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public List<String> getInlineLinks(List<String> pages, boolean serialize, String filename) throws IOException {
		
		if (serialize) {
			if ((new File(filename)).exists()) {
				try {
					return new ArrayList<String>(
							(HashSet<String>)Serialize.in(filename));
				} catch (RuntimeException e) {
					e.printStackTrace();
					System.out.println("Could not read from file "+filename+" into List.");
				}
			} else {
				System.out.println("File does not exist, continuing.");
			}
		}
		
		/* 
		 * Finds links of type "some text and a [[wp page title|link somewhere]] etc"
		 * and returns a list of the "wp page title" portion of the link
		 */
		String regex = "\\[\\[.*?\\]\\]";
		Pattern pattern = Pattern.compile(regex);
		HashSet<String> linkTitles = new HashSet<String>();
		for (String title : pages) {
			String content = wikipedia.getPageText(title);
			Matcher matcher = pattern.matcher(content);
			while (matcher.find()) {
				String rawlink = matcher.group();
				String link = CharMatcher.anyOf("[]").removeFrom(rawlink);
				Iterable<String> iter = Splitter.on('|')
						.omitEmptyStrings()
						.trimResults()
						.split(link);
				String linkTitle = iter.iterator().next();
				linkTitles.add(linkTitle);
			}
			System.out.println(linkTitles.size()+" links found on "+pages.indexOf(title)+" pages.");
		}
		
		if (serialize) {
			Serialize.out(filename, linkTitles);
			System.out.println("Attempted serialization of result as file "+filename);
		}
		return new ArrayList<String>(linkTitles);
	}
	
	/**
	 * Filters a list of page titles to exclude links to other language wikipedias and 
	 * "redlinks"- pages that don't exist on Wikipedia. This is a fairly expensive
	 * method if using a large number of links.
	 * @param titles
	 * @return
	 * @throws IOException
	 */
	public List<String> filterLinks(List<String> titles) throws IOException {
		HashSet<String> filtered = new HashSet<String>();
		System.out.println("Checking page existence in Wikipedia...");
		boolean[] exists = wikipedia.exists(titles.toArray(new String[10]));
		for (int i=0; i < titles.size(); i++) {
			if (exists[i] && !titles.get(i).contains(":")) {
				filtered.add(titles.get(i));
			}
		}
		return new ArrayList<String>(filtered);
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
		List<GWRevision> live = watcher.getRecentChangesFromWatchlist(past, 50, 50*minutesAgo, true);
		Set<String> changed = new HashSet<String>(live.size());
		for (GWRevision rev : live) {
			changed.add(rev.getTitle());
		}
		return new ArrayList<String>(changed);	
	}
	
	/**
	 * Returns the number of revisions a certain page has been through.
	 * @param title
	 * @return
	 */
	public int getRevisionCount(String title) {
		try {
			if (title != null){
				return wikipedia.getPageHistory(title).length;
			} else {
				return 0;
			}
		} catch (IOException e) {
			return 0;
		}
	}
	
	/**
	 * Makes an attempt to find the Genewiki page that contains the PBB template
	 * associated with the entrez ID provided. Not guaranteed to find anything, or
	 * be accurate.
	 * @param entrez
	 * @return
	 * @throws IOException
	 */
	public String getGWPageFromEntrez(String entrez) throws IOException {
		String[] linked = wikipedia.whatEmbedsThis("Template:PBB/"+entrez, 0, false);
		
		if (linked.length != 0) {
			return linked[0];
		} else {
			return null;
		}
	}
	
	public Wiki getWiki() {
		return this.wikipedia;
	}
}