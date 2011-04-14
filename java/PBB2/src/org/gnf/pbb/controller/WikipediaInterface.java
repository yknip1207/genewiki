package org.gnf.pbb.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Scanner;
import java.util.logging.Logger;

import org.gnf.pbb.Authenticate;

import net.sourceforge.jwbf.core.actions.util.ActionException;
import net.sourceforge.jwbf.core.actions.util.ProcessException;
import net.sourceforge.jwbf.core.contentRep.Article;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;
/**
 * Manages retrieving articles from wikipedia and maintaining a logged-in state.
 * Also handles the cache files and writing to/from them when the useCache flag is 
 * set to true.
 * @author eclarke
 *
 */
public class WikipediaInterface {
	private final static Logger logger = Logger.getLogger(WikipediaInterface.class.getName());
	static MediaWikiBot wpBot;
	static String[] credentials;

	/**
	 * Creates a new interface with Wikipedia, initializing a MediaWikiBot
	 * and parsing a credentials file.
	 */
	public WikipediaInterface() {
		URL wikipediaUrl = null;
		try {
			wikipediaUrl = new URL("http://en.wikipedia.org/w/");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		wpBot = new MediaWikiBot(wikipediaUrl);
		try {
			credentials = Authenticate.login("credentials.json");
		} catch (Exception e) {
			e.getMessage();
			e.printStackTrace();
		} 
		
	}
	
	/**
	 * Creates a cache at the current working directory of the form pbb.MM.YYYY
	 * @return cache directory name on successful directory creation
	 * @throws Exception 
	 */
	private static String getCacheDirectory() throws Exception {
		boolean success = false;
		Calendar cal = Calendar.getInstance();
		String cwd = System.getProperty("user.dir")+"\\pbb."+cal.get(Calendar.MONTH)+"."+cal.get(Calendar.YEAR);
		
		// create directory if it doesn't already exist
		if (!(new File(cwd)).exists()) 
			success = (new File(cwd)).mkdir();
		if (!success) {
			logger.fine("Cache directory " +cwd+ "exists.");
		}
		return cwd;
	}

	/**
	 * Pulls raw article text from wikipedia.
	 * @param title of article
	 * @param useCache: search the cache directory for title to reduce calls to wikipedia.
	 * @return raw text of article as string
	 */
	public String retrieveArticle(String title, boolean useCache) {
		String content = ""; // This is eventually where the article content will end up
		String cacheFilePath = "";
		try {
			cacheFilePath = getCacheDirectory()+"/"+title.hashCode();
			System.out.println(cacheFilePath);
		} catch (Exception e) {
			e.getMessage();
			e.printStackTrace();
		}
		if (useCache) {
			if ((new File(cacheFilePath)).exists()) {
				logger.fine("Cached version exists & useCache flag specified; using cached version");
				content = retrieveArticleFromCache(cacheFilePath);
			} else {
				logger.fine("No cached version exists according to pathname specified.");
				logger.fine("Querying Wikipedia directly...");
				content = retrieveArticleFromWikipedia(title, cacheFilePath);
			}
			
		} else {
			logger.fine("useCache flag set to false; querying Wikipedia for article text.");
			content = retrieveArticleFromWikipedia(title, cacheFilePath);			
		}
		if (content.length() == 0) {
			logger.severe("Content length is zero; something went wrong retrieving the article.");
		}
		return content;
	}

	private String retrieveArticleFromCache(String cacheFilePath) {
		String content;
		StringBuffer stringBuffer = new StringBuffer();
		String nl = System.getProperty("line.separator");
		Scanner scanner = null;
		try {
			scanner = new Scanner(new FileInputStream(cacheFilePath));
		} catch (FileNotFoundException e) {
			logger.severe("Even though we validated that the path exists, we got a FileNotFound error: " + e.getMessage());
			e.printStackTrace();
		}
		try {
			while (scanner.hasNextLine()) {
				stringBuffer.append(scanner.nextLine() + nl);
			}
		} finally {
			scanner.close();
		}
		content = stringBuffer.toString();
		return content;
	}

	private String retrieveArticleFromWikipedia(String title, String cacheFilePath) {
		String content = "";
		if (wpBot.isLoggedIn() == false) {
			System.out.println("DEBUG: Bot is logging in- is this expected?");
			try {
				wpBot.login(credentials[0], credentials[1]);
			} catch (ActionException e) {
				logger.severe(e.getMessage());
				e.printStackTrace();
			}
		}
		
		try {
			Article article = wpBot.readContent(title);
			String articleContent = article.getText();
			BufferedWriter writer = new BufferedWriter(new FileWriter(cacheFilePath));
			writer.write(articleContent);
			writer.close();
			// I pull the information back out from the cachefile because I want to make sure the encoding and line
			// breaks remain constant. If this had to be optimized for speed, this is certainly something to change.
			content = retrieveArticleFromCache(cacheFilePath);
		} catch (ActionException e) {
			logger.severe(e.getMessage());
			e.printStackTrace();
		} catch (ProcessException e) {
			logger.severe(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.severe(e.getMessage());
			e.printStackTrace();
		}
		return content;
	}

}
