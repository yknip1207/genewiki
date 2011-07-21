package org.gnf.pbb.wikipedia;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.net.URL;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sourceforge.jwbf.core.actions.util.ActionException;
import net.sourceforge.jwbf.core.actions.util.ProcessException;
import net.sourceforge.jwbf.core.contentRep.Article;
import net.sourceforge.jwbf.core.contentRep.SimpleArticle;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;

import org.gnf.pbb.Configs;
import org.gnf.pbb.exceptions.ExceptionHandler;
import org.gnf.pbb.util.DiffUtils;
import org.gnf.pbb.util.DiffUtils.Diff;
/**
 * Manages retrieving articles from wikipedia and maintaining a logged-in state.
 * Also handles the cache files and writing to/from them when the useCache flag is 
 * set to true.
 * @author eclarke
 *
 */
public class WikipediaController {
	
	// private final boolean USE_SANDBOX = false;
	// private final String SANDBOX_URL = "User:Pleiotrope/sandbox/test_gene_sandbox";
	
	private final static Logger logger = Logger.getLogger(WikipediaController.class.getName());
	ExceptionHandler botState;
	private MediaWikiBot wpBot;
	private String username;
	private String password;
	private String cacheDirectory;
	private String hostLocation;
	private String templatePrefix;
	private boolean usecache;
	private boolean verbose;
	private boolean dryrun;

	/**
	 * Creates a new interface with Wikipedia, initializing a MediaWikiBot
	 * and parsing a credentials file.
	 */
	public WikipediaController(ExceptionHandler exHandler, Configs configs) {
		botState = exHandler;
		try {
			this.hostLocation = configs.str("hostLocation");
			wpBot = new MediaWikiBot(new URL(hostLocation));
			cacheDirectory = configs.str("cacheLocation");
			usecache = configs.flag("usecache");
			verbose = configs.flag("verbose");
			dryrun = configs.flag("dryrun");
			username = configs.str("username");
			password = configs.str("password");
			templatePrefix = configs.str("templatePrefix");
		} catch (Exception e) {
			botState.fatal(e);
		}
		if(verbose){
			logger.setLevel(Level.FINE);
		}
	}
	
	/**
	 * Creates a cache at the current working directory of the form pbb.MM.YYYY
	 * @return cache directory name on successful directory creation
	 * @throws Exception 
	 */
	public String createCacheDirectory() throws Exception {
		boolean success = false;
		Calendar cal = Calendar.getInstance();
		String ps = System.getProperty("file.separator");
		String cwd = System.getProperty("user.dir")+ps+"pbb."+cal.get(Calendar.MONTH)+"."+cal.get(Calendar.YEAR)+ps;
		
		// create directory if it doesn't already exist
		if (!(new File(cwd)).exists()) 
			success = (new File(cwd)).mkdir();
		if (!success) {
			logger.fine("Cache directory " +cwd+ "exists.");
		}
		return cwd;
	}

	/**
	 * Pulls raw article text from wikipedia and stores it in the cache.
	 * @param title of article
	 * @return raw text of article as string
	 */
	public String getContent(String title) {
		String content = ""; // This is eventually where the article content will end up
		String cachedFilename = "";
		try {
			cachedFilename = Integer.toString(title.hashCode());
			logger.fine("File path: "+cachedFilename);
		} catch (Exception e) {
			e.getMessage();
			e.printStackTrace();
		}
		if (usecache) {
			if (cachedFileExists(cachedFilename)) {
				logger.fine("Cached version exists & useCache flag specified; using cached version");
				content = retrieveFileFromCache(cachedFilename);
			} else {
				logger.fine("No cached version exists according to pathname specified.");
				logger.fine("Querying Wikipedia directly...");
				content = retrieveArticleFromWikipedia(title, cachedFilename);
			}
			
		} else {
			logger.fine("useCache flag set to false; querying Wikipedia for article text.");
			content = retrieveArticleFromWikipedia(title, cachedFilename);			
		}
		if (content.length() == 0) {
			logger.severe("Content length is zero; something went wrong retrieving the article.");
		}
		return content;
	}
	
	public String getContentForId(String id) {
		String realTitle = templatePrefix+id;
		return getContent(realTitle);
	}

	public String retrieveFileFromCache(String filename) {
		String content;
		StringBuffer stringBuffer = new StringBuffer();
		String nl = System.getProperty("line.separator");
		Scanner scanner = null;
		try {
			scanner = new Scanner(new FileInputStream(cacheDirectory+filename));
		} catch (FileNotFoundException e) {
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

	/**
	 * Pull information from Wikipedia to send to the parser.
	 * @param title full location of the article (everything past the mediawiki url)
	 * @param cachedFilename name to use when storing retrieved content in the cache
	 * @return
	 */
	private String retrieveArticleFromWikipedia(String title, String cachedFilename) {
		String content = "";
		if (!isAuthenticated()) {
			logger.info("Bot is logging in...");
			try {
				authenticate();
			} catch (Exception e) {
				logger.severe(e.getMessage());
				e.printStackTrace();
			}
		}
		
		try {
			Article article = wpBot.readContent(title);
			String articleContent = article.getText();
			// We write the content to the cache and pull it back out to ensure that line endings and encoding are 
			// correct; this is probably not the best way to do this.
			writeContentToCache(articleContent, cachedFilename);
			content = retrieveFileFromCache(cachedFilename);
		} catch (ActionException e) {
			logger.severe(e.getMessage());
			e.printStackTrace();
		} catch (ProcessException e) {
			logger.severe(e.getMessage());
			e.printStackTrace();
		}
		return content;
	}

	/**
	 * Writes content to a directory specified by the cacheDirectory variable in
	 * the configuration file.
	 * @param content
	 * @param filename
	 */
	public void writeContentToCache(String content, String filename) {
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter(cacheDirectory+filename));
			writer.write(content);
			writer.close();
		} catch (Exception e) {
			logger.severe(e.getMessage());
			e.printStackTrace();
		}
		
	}

	public boolean cachedFileExists(String filename) {
		return (new File(cacheDirectory+filename)).exists();
	}

	public boolean isAuthenticated() {
		return wpBot.isLoggedIn();
	}

	
	/**
	 * Logs the bot in with the username and password from
	 * the configuration file.
	 * @throws Exception
	 */
	public void authenticate() throws Exception {
		wpBot.login(username, password);
	}

	/**
	 * Puts content either to Wikipedia or to the cache directory, depending on bot state
	 * and settings. 
	 * @param content which should be previously verified
	 * @param title will be used to find the appropriate URL to write to
	 * @param changes a summary of changes made, such as fields updated
	 * @throws Exception
	 */
	public void putContent(String content, String title, String changes) throws Exception {
		String status = "";
		String live_title = "";
		
		// If the dryrun flag is true or the bot is unable for some reason
		// to validate its update content, it will write it to the cache 
		// instead of posting to wikipedia.
		if (!botState.isFine() || dryrun) {
			writeContentToCache(content, "DRYRUN_"+title);
			logger.info(changes);
			logger.info("Wrote file "+cacheDirectory+"DRYRUN_"+title);
			return;
		} else {
			live_title = templatePrefix + title;
		}
		
		if(!isAuthenticated())
			authenticate();
		try {
			Article page = wpBot.readContent(live_title);
			String prevContent = page.getText();
			page.setText(content);
			page.setEditSummary(changes);
			page.setEditor("Protein Box Bot");
			SimpleArticle article = page.getSimpleArticle();
			
			// If the update is not set as a minor edit, it runs the risk
			// of being aborted by an mediawiki extension (additionally,
			// our updates do not qualify as minor in the first place).
			article.setMinorEdit(true);
			
			// NOTE: This is where the bot core actually uploads the information 
			// to wikipedia.
			wpBot.writeContent(article);
			
			// Create a diff file to cache
			DiffUtils diff = new DiffUtils();
			LinkedList<Diff> diffs = diff.diff_main(prevContent, content);
			diff.diff_cleanupSemantic(diffs);
			String htmlDiff = diff.diff_prettyHtml(diffs);
			writeContentToCache(htmlDiff, "DIFFS_"+title+".html");
			status = "Success writing new content to page " + live_title;
			logger.info(status);
			
		} catch (ActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Writes a report to the cache directory based on successful updates and fields updated.
	 * @param report
	 */
	public void writeReport(String report) {
		Calendar cal = Calendar.getInstance();
		this.writeContentToCache(report, "BotExecutionReport_"+cal.get(Calendar.DAY_OF_MONTH)+"."+cal.get(Calendar.MONTH)+"."+cal.get(Calendar.YEAR));
	}

}
