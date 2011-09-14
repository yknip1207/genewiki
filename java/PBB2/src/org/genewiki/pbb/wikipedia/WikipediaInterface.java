package org.genewiki.pbb.wikipedia;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;



import org.genewiki.api.Wiki;
import org.genewiki.pbb.Configs;
import org.genewiki.pbb.exceptions.ExceptionHandler;

public class WikipediaInterface {

	private final static Logger logger = Logger.getLogger(WikipediaInterface.class.getName());
	private final Wiki wp;
	private ExceptionHandler exh;
	private Configs configs;
	private String username;
	private String password;
	private String template;
	
	public WikipediaInterface() {
		this(ExceptionHandler.INSTANCE, Configs.GET);
	}
	
	public WikipediaInterface(ExceptionHandler exHandler, Configs configs) {
		this.wp = new Wiki();
		this.exh = exHandler;
		this.configs = configs;
		username = configs.str("username");
		password = configs.str("password");
		template = configs.str("templatePrefix");
		wp.setMaxLag(10); 
		wp.setLogLevel(Level.OFF);
		try {
			authenticate();
		} catch (FailedLoginException e) {
			e.printStackTrace();
			exHandler.fatal(e);
		}
	}
	
	public void authenticate() throws FailedLoginException {
		try {
			authenticate(this.username, this.password);
		} catch (IOException e) {
			e.printStackTrace();
			exh.fatal(e);
		}
	}
	
	public void authenticate(String username, String password) throws FailedLoginException, IOException {
		wp.login(username, password.toCharArray());
	}
	
	public String getContent(String title) {
		try {
			return wp.getPageText(title);
		} catch (IOException e) {
			e.printStackTrace();
			exh.recoverable(e);
			return null;
		}
	}
	
	public String getContentForId(String id) {
		return getContent(template+id);
	}
	
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
