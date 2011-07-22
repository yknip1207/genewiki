package org.gnf.pbb.util;

import java.util.ArrayList;
import java.util.List;

import org.gnf.genewiki.*;
import org.gnf.pbb.Configs;
import org.gnf.pbb.exceptions.PbbExceptionHandler;
import org.gnf.pbb.wikipedia.WikipediaController;
import org.gnf.wikiapi.Page;

public class Find {
	
	/**
	 * This beast of a method finds candidates to update, either from a file named 'ids.txt' or
	 * from the mediawiki site specified in the configuration.
	 * @param count
	 * @param sourceURL
	 * @param cfg
	 * @return
	 */
	public static List<String> updateCandidates(int count, Configs cfg) {
		int batchsize = 100;
		List<String> candidates = new ArrayList<String>();
		WikipediaController wpControl = new WikipediaController(PbbExceptionHandler.INSTANCE, cfg);
		String indexName = "ids.txt";
		if (wpControl.cachedFileExists(indexName)) {
			candidates = ListUtils.parseString(wpControl.retrieveFileFromCache(indexName));
			return candidates;
		} else {
			try {
				String user = cfg.str("username");
				// String pass = cfg.str("password");
				WikiCategoryReader wcr = new WikiCategoryReader(user);
				List<Page> pages = wcr.getPagesWithPBB(count, batchsize);
				for (Page page : pages) {
					GeneWikiPage p = new GeneWikiPage(page.getTitle());
					p.retrieveWikiTextContent(true);
					p.parseAndSetNcbiGeneId();
					candidates.add(p.getNcbi_gene_id());
				}
				wpControl.writeContentToCache(ListUtils.toString(candidates), indexName);
				return candidates;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	

}
