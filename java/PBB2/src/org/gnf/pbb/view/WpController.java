package org.gnf.pbb.view;

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

import net.sourceforge.jwbf.core.actions.util.ActionException;
import net.sourceforge.jwbf.core.actions.util.ProcessException;
import net.sourceforge.jwbf.core.bots.util.CacheHandler;
import net.sourceforge.jwbf.core.bots.util.SimpleCache;
import net.sourceforge.jwbf.core.contentRep.Article;
import net.sourceforge.jwbf.core.contentRep.SimpleArticle;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
/**
 * Manages retrieving articles from wikipedia and maintaining a logged-in state.
 * Also handles the cache files and writing to/from them when the useCache flag is 
 * set to true.
 * @author eclarke
 *
 */
public class WpController implements ViewController {
	
	private final boolean USE_SANDBOX = false; // no live updates yet... stick to the sandbox.
	private final String SANDBOX_URL = "User:Pleiotrope/sandbox/test_gene_sandbox";
	
	private final static Logger logger = Logger.getLogger(WpController.class.getName());
	static MediaWikiBot wpBot;
	static String[] credentials;
	static String cacheDirectory;

	/**
	 * Creates a new interface with Wikipedia, initializing a MediaWikiBot
	 * and parsing a credentials file.
	 */
	public WpController() {
		URL wikipediaUrl = null;
		try {
			wikipediaUrl = new URL("http://en.wikipedia.org/w/");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		wpBot = new MediaWikiBot(wikipediaUrl);
//		try {
//			authenticate("credentials.json");
//		} catch (Exception e) {
//			e.getMessage();
//			e.printStackTrace();
//		} 
		try {
			cacheDirectory = createCacheDirectory();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	 * Pulls raw article text from wikipedia.
	 * @param title of article
	 * @param useCache: search the cache directory for title to reduce calls to wikipedia.
	 * @return raw text of article as string
	 */
	public String retrieveContent(String title, boolean useCache) {
		String content = ""; // This is eventually where the article content will end up
		String cacheFileRelPath = "";
		try {
			cacheFileRelPath = Integer.toString(title.hashCode());
			logger.fine("File path: "+cacheFileRelPath);
		} catch (Exception e) {
			e.getMessage();
			e.printStackTrace();
		}
		if (useCache) {
			if (cachedFileExists(cacheFileRelPath)) {
				logger.fine("Cached version exists & useCache flag specified; using cached version");
				content = retrieveFileFromCache(cacheFileRelPath);
			} else {
				logger.fine("No cached version exists according to pathname specified.");
				logger.fine("Querying Wikipedia directly...");
				content = retrieveArticleFromWikipedia(title, cacheFileRelPath);
			}
			
		} else {
			logger.fine("useCache flag set to false; querying Wikipedia for article text.");
			content = retrieveArticleFromWikipedia(title, cacheFileRelPath);			
		}
		if (content.length() == 0) {
			logger.severe("Content length is zero; something went wrong retrieving the article.");
		}
		return content;
	}

	private String retrieveFileFromCache(String filename) {
		String content;
		StringBuffer stringBuffer = new StringBuffer();
		String nl = System.getProperty("line.separator");
		Scanner scanner = null;
		try {
			scanner = new Scanner(new FileInputStream(cacheDirectory+filename));
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

	private String retrieveArticleFromWikipedia(String title, String filename) {
		String content = "";
		if (!isAuthenticated()) {
			//XXX This should not be class: severe in production
			logger.severe("DEBUG: Bot is logging in- is this expected?");
			try {
				authenticate("credentials.json");
			} catch (Exception e) {
				logger.severe(e.getMessage());
				e.printStackTrace();
			}
		}
		
		try {
			Article article = wpBot.readContent(title);
			String articleContent = article.getText();
			writeContentToCache(articleContent, filename);
			// I pull the information back out from the cachefile because I want to make sure the encoding and line
			// breaks remain constant. If this had to be optimized for speed, this is certainly something to change.
			content = retrieveFileFromCache(filename);
		} catch (ActionException e) {
			logger.severe(e.getMessage());
			e.printStackTrace();
		} catch (ProcessException e) {
			logger.severe(e.getMessage());
			e.printStackTrace();
		}
		return content;
	}

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

	public void authenticate(String credentials) throws Exception {
		JsonFactory jf = new JsonFactory();
		JsonParser jp = jf.createJsonParser(new File(credentials));
		String user = "";
		String password = "";
		if (jp.nextToken() != JsonToken.START_OBJECT){
			throw new IOException("Invalid JSON format in " + credentials);
		}
		while (jp.nextToken() != JsonToken.END_OBJECT) {
			String fieldName = jp.getCurrentName();
			jp.nextToken();
			if (fieldName.equals("user")) {
				user = jp.getText();
			} else if (fieldName.equals("password")) {
				password = jp.getText();
			}
		}
		wpBot.login(user, password);
	}

	@Override
	public synchronized String update(String content, String title, String changes, boolean dryRun) throws Exception {
		String status = "";
		if (dryRun) {
			writeContentToCache(content, "DRYRUN_"+title);
			System.out.println(changes);
			System.out.println("Wrote file "+cacheDirectory+"DRYRUN_"+title);
		} else if (USE_SANDBOX) {
			if(!isAuthenticated())
				authenticate("credentials.json");
			try {
				SimpleArticle page = wpBot.readData(SANDBOX_URL);
				page.setText(content);
				page.setEditSummary(changes);
				wpBot.writeContent(page);
			} catch (ActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ProcessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			String live_title = "Template:PBB/" + title;
			if(!isAuthenticated())
				authenticate("credentials.json");
			try {
				wpBot.setCacheHandler(new SimpleCache(new File(cacheDirectory), 5000));
				Boolean hasCache = wpBot.hasCacheHandler();
				Article page = wpBot.readContent(live_title);
				String prevContent = page.getText();
				page.setText(content);
				page.setEditSummary(changes);
				page.setEditor("Protein Box Bot");
				SimpleArticle article = page.getSimpleArticle();
				wpBot.writeContent(article);
				if (content.equals(prevContent)) {
					
					status = "No difference in outputted content was detected, and so \n" +
							"no new revision state may have been logged for the page " + live_title;
					logger.warning(status);
				} else {
					status = "Success writing new content to page " + live_title;
					logger.info(status);
				}
			} catch (ActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ProcessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return status;
	}

}
