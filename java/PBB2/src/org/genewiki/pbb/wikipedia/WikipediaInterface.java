package org.genewiki.pbb.wikipedia;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;



import org.genewiki.api.Wiki;
import org.genewiki.pbb.Configs;
import org.genewiki.pbb.exceptions.ExceptionHandler;

/**
 * WikipediaInterface handles getting and putting articles to and from Wikipedia.
 * It uses an ExceptionHandler to manage errors and a Configs object to retrieve
 * the template name, username, and password.
 * @author eclarke
 *
 */
public class WikipediaInterface {

	/* ---- Declarations ---- */
	private final static 	Logger logger = Logger.getLogger(WikipediaInterface.class.getName());
	private final 			Wiki wp;
	private 				ExceptionHandler exh;
	private 				String username;
	private 				String password;
	private 				String template;
	
	
	/* ---- Constructors ---- */
	
	/**
	 * Constructs a new WikipediaInterface with static global ExceptionHandler
	 * and Configs objects and logs in using credentials from Configs object. 
	 * Triggers a fatal state in the ExceptionHandler if the authentication 
	 * fails.
	 */
	public WikipediaInterface() {
		this(ExceptionHandler.INSTANCE, Configs.INSTANCE);
	}
	
	/**
	 * Constructs a new WikipediaInterface with the specified ExceptionHandler
	 * and Configs objects and logs in using credentials from Configs object. 
	 * Triggers a fatal state in the ExceptionHandler if the authentication 
	 * fails.
	 * login fails
	 * @param exHandler
	 * @param configs
	 */
	public WikipediaInterface(ExceptionHandler exHandler, Configs configs) {
		this.wp = new Wiki();
		this.exh = exHandler;
		username = configs.str("username");
		password = configs.str("password");
		template = configs.str("templatePrefix");
		wp.setMaxLag(0); 
		wp.setLogLevel(Level.OFF);
		try {
			authenticate();
		} catch (FailedLoginException e) {
			e.printStackTrace();
			exHandler.fatal(e);
		}
	}
	
	
	/* ---- Public Methods ---- */
	
	/**
	 * Authenticates to Wikipedia using the instance variables for
	 * username and password.
	 * @throws FailedLoginException if credentials are incorrect
	 */
	public void authenticate() throws FailedLoginException {
		try {
			authenticate(this.username, this.password);
		} catch (IOException e) {
			e.printStackTrace();
			exh.fatal(e);
		}
	}
	
	/**
	 * Authenticates to Wikipedia using the specified username and password.
	 * @param username
	 * @param password
	 * @throws FailedLoginException
	 * @throws IOException
	 */
	public void authenticate(String username, String password) throws FailedLoginException, IOException {
		wp.login(username, password.toCharArray());
	}
	
	/**
	 * Returns the raw wikitext of the article with the specified title.
	 * Will cause a recoverable state to be flagged in the ExceptionHandler
	 * in the case of an IOException.
	 * @param title
	 * @return contents of article
	 */
	public String getContent(String title) {
		try {
			return wp.getPageText(title);
		} catch (IOException e) {
			e.printStackTrace();
			exh.recoverable(e);
			return null;
		}
	}
	
	/**
	 * Returns the raw wikitext of the template associated with the specified id.
	 * Will cause a recoverable state to be flagged in the ExceptionHandler
	 * in the case of an IOException.
	 * @param id
	 * @return contents of template
	 */
	public String getContentForId(String id) {
		return getContent(template+id);
	}
	
	/**
	 * Posts content to the  template page specified by the id. The title to
	 * post to is calculated by concatenating the Configs.templatePrefix field to the
	 * specified id. Causes a recoverable state to to be flagged in the ExceptionHandler
	 * in case the bot is not logged in or an IOException occurs.
	 * @param template
	 * @param id
	 * @param changes
	 */
	public void putTemplate(String template, String id, String changes) {
		String title = this.template+id;
		if (exh.canExecute()) {
			try {
				wp.edit(title, template, changes, true);
				logger.info("Successfully wrote new template to "+title);
			} catch (LoginException e) {
				e.printStackTrace();
				exh.recoverable(e);
			} catch (IOException e) {
				e.printStackTrace();
				exh.recoverable(e);
			}
		}
	}
	
	/**
	 * Posts content to the article specified by the title, overwriting any previous
	 * content that may have existed. Causes a recoverable state to to be flagged in the ExceptionHandler
	 * in case the bot is not logged in or an IOException occurs.
	 * @param article
	 * @param title
	 * @param changes
	 */
	public void putArticle(String article, String title, String changes) {
		if (exh.canExecute()) {
			try {
				wp.edit(title, article, changes, false);
				logger.info("Successfully wrote new article to "+title);
			} catch (LoginException e) {
				e.printStackTrace();
				exh.recoverable(e);
			} catch (IOException e) {
				e.printStackTrace();
				exh.recoverable(e);
			}
		}
	}
	
}
