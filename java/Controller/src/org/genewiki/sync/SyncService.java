/**
 * 
 */
package org.genewiki.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import static java.lang.String.format;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.genewiki.GWRevision;
import org.genewiki.api.Wiki;
import org.genewiki.mail.Sendmail;
import org.genewiki.stream.WatchlistManager;
import org.joda.time.DateTime;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * SyncService uses an inner class, Sync, controlled by a repeating Timer
 * to check periodically for updates to Gene Wiki articles on Wikipedia and
 * to update them on GeneWikiPlus. The main() method will prompt for passwords
 * if not specified. A properties file with the usernames for wikipedia and genewiki+,
 * as well as the current location of genewiki+, is required. The location of this
 * file can be specified on the command line. If it is not, the program looks in the
 * local directory for a "gwsync.conf" file; if this doesn't exist, the program prompts
 * for the user to enter the path to the file.
 * <p> The program will run until external interruption.
 * @author eclarke
 *
 */
public class SyncService {
	
	private String wpPass;
	private final Wiki wikipedia;
	private String gwpPass;
	private final Wiki genewikiplus;
	private final Properties properties;
	
	private final String alertSubject = "GeneWiki Sync Service has fallen and can't get up.";

	/**
	 * Entry point for running the program standalone from a command-line.
	 * @param args
	 * @throws IOException 
	 * @throws FailedLoginException 
	 */
	public static void main(String[] args) throws IOException, FailedLoginException {
		OptionParser parser = new OptionParser();
		OptionSpec<String> wpPass = parser.accepts("wp", "wikipedia password")
				.withRequiredArg().ofType(String.class).describedAs("password");
		OptionSpec<String> gwPass = parser.accepts("gp", "genewiki+ password")
				.withRequiredArg().ofType(String.class).describedAs("password");
		OptionSpec<File> configs = parser.accepts("cf", "config options")
				.withRequiredArg().ofType(File.class).describedAs("config file");
		OptionSet options = null;
		try {
			options = parser.parse(args);
		} catch (OptionException e) {
			parser.printHelpOn(System.err);
			System.exit(1);
		}
		
		String wpPassword, gwpPassword;
		File configFile;
		
		// Parse Wikipedia password
		if (options.has(wpPass)) {
			wpPassword = options.valueOf(wpPass);
		} else {
			wpPassword = readln("Enter Wikipedia password:");
			if (wpPassword.equals("")) {
				System.err.println("Password cannot be blank.");
				System.exit(1);
			}
				
		}
		
		// Parse GeneWiki+ password
		if (options.has(gwPass)) {
			gwpPassword = options.valueOf(gwPass);
		} else {
			gwpPassword = readln("Enter GeneWiki+ password:");
			if (gwpPassword.equals("")) {
				System.err.println("Password cannot be blank.");
				System.exit(1);
			}
		}
		
		// Parse configs file
		if (options.has(configs)) {
			configFile = options.valueOf(configs);
		} else {
			if (!(configFile = new File("gwsync.conf")).exists()) {
				configFile = new File(readln("Enter config file location (absolute path):"));
				
			}
		}

		SyncService sync = new SyncService(wpPassword, gwpPassword, configFile.getCanonicalPath());
		sync.start();
	}
	
	/**
	 * Creates a new SyncService with the specified passwords and configuration options.
	 * @param wpPassword wikipedia password for account with GeneWiki watchlist
	 * @param gwpPassword GeneWiki+ password
	 * @param configLocation location of gwsync.conf
	 * @throws FileNotFoundException if configuration location is invalid
	 * @throws IOException if configuration cannot be read
	 * @throws FailedLoginException if provided credentials are invalid
	 */
	public SyncService(String wpPassword, String gwpPassword, String configLocation) 
			throws FileNotFoundException, IOException, FailedLoginException {
		this.wpPass  = wpPassword;
		this.gwpPass = gwpPassword;
		this.properties = new Properties();
		properties.load(new FileReader(configLocation));
		
		this.wikipedia = new Wiki();
		wikipedia.setMaxLag(0);
		wikipedia.login(
				checkNotNull(properties.getProperty("wikipedia.username")), 
				wpPass.toCharArray());
		
		this.genewikiplus = new Wiki(
				checkNotNull(properties.getProperty("genewiki+.location")), "");
		genewikiplus.setMaxLag(0);
		genewikiplus.setThrottle(0);
		genewikiplus.setUsingCompressedRequests(false);
		genewikiplus.login(
				checkNotNull(properties.getProperty("genewiki+.username")), 
				gwpPass.toCharArray());
	}
	
	/**
	 * Creates a new SyncService with the specified Wiki objects and configuration file
	 * location.
	 * @param wp Wikipedia Wiki
	 * @param gwp GeneWiki+ Wiki
	 * @param configLocation path to the configs file
	 * @throws FileNotFoundException if the configuration location is invalid
	 * @throws IOException if the configuration cannot be read
	 */
	public SyncService(Wiki wp, Wiki gwp, String configLocation) throws FileNotFoundException, IOException {
		this.wikipedia = wp;
		this.genewikiplus = gwp;
		this.properties = new Properties();
		properties.load(new FileReader(configLocation));
	}
	
	@Deprecated
	public void start(int period) {
		Sync sync = new Sync(wikipedia.getCurrentUser().getUsername(), wpPass, period);
		start(sync);
	}
	
	/**
	 * Starts the timer with the provided Sync object
	 * @param sync
	 */
	public void start(Sync sync) {
		Integer period = Integer.parseInt(properties.getProperty("sync.period", "5"));
		
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
		ScheduledFuture<?> future = executor.scheduleAtFixedRate(sync, 0, period, TimeUnit.MINUTES);
		long launchTime = Calendar.getInstance().getTimeInMillis();
		int fail = 0;
		int quickfail = 0; 
		log(String.format("Starting repeating sync with a period of %d minutes...", period));
		while (true) {
			if (future.isDone()) {
				try {
					future.get();
				} catch (ExecutionException e) {
					log(format("Exception encountered: %s", e.getCause().getMessage()));
					fail++;
					long failTime = Calendar.getInstance().getTimeInMillis();
					if (failTime - launchTime < 750) {
						quickfail++;
						if (quickfail > 10) {
							log("Failed immediately 10 consecutive times. Sending alert email and bailing out.");
							sendAlertEmail(e);
							return;
						} else {
							log(format("Failed immediately after launch. Sleeping for %d seconds...", 5*quickfail));
							pause(5000*quickfail);
						}
						
					}
					
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
			} else {
				pause(500);
			}
		}
	}
	
	/**
	 * Starts the repeating timer object to execute a Sync object at a period interval
	 * specified in the sync configuration file. If the Sync_Period property is not set,
	 * a default of 5 minutes is used. This method is not known to be threadsafe, nor does
	 * it make any sense for it to be used in a multithreaded context.
	 */
	public void start() {
		Timer timer = new Timer();
		Integer period = Integer.parseInt(properties.getProperty("sync.period", "5"));
		Sync sync = new Sync(wikipedia.getCurrentUser().getUsername(), wpPass, period);
		timer.scheduleAtFixedRate(sync, 5000, TimeUnit.MINUTES.toMillis(period));
		log(String.format("Starting sync with a period of %d minutes...", period));
	}
	
	
	/**
	 * Sync handles the minutiae of getting the recently-changed pages from the 
	 * WatchlistManager and updating them on GeneWiki+. It executes under the command
	 * of a Timer in the parent class.
	 * @author eclarke
	 *
	 */
	public class Sync extends TimerTask {
		
		private final WatchlistManager 	watcher;
		private final int 				period;
		
		public Sync(String wpUser, String wpPass, int per) {
			watcher = new WatchlistManager(wpUser, wpPass, null);
			period = per;
		}
		
		public Sync(WatchlistManager watchlistmanager, int per) {
			watcher = watchlistmanager;
			period = per;
		}
		
		@Override
		public void run() {
			try {
			log("Syncing...");
			List<String> changed = getRecentChanges(period);
			log(String.format("Found %d new changes...", changed.size()));
			writeChangedArticles(changed);
			} catch (Exception e) {
				/* Catching Exception(s) like this is bad form, but in this case
				 * it's more important that this thread not exit from an exception,
				 * as that will suppress future executions of the thread.
				 */
			}
		}
		
		private List<String> getRecentChanges(int minutesAgo) {
			Calendar past = Calendar.getInstance();
			past.add(Calendar.MINUTE, -minutesAgo);
			List<GWRevision> live = watcher.getRecentChangesFromWatchlist(past, 50, 50*minutesAgo, true);
			Set<String> changed = new HashSet<String>(live.size());
			for (GWRevision rev : live) {
				changed.add(rev.getTitle());
			}
			return new ArrayList<String>(changed);	
		}
		
		private List<String> writeChangedArticles(List<String> changed) {
			List<String> completed = new ArrayList<String>(changed.size());
			for (String title : changed) {
				try {
					String text = wikipedia.getPageText(title);
					String summary = wikipedia.getTopRevision(title).getSummary();
					genewikiplus.edit(title, text, summary, false);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (LoginException e) {
					e.printStackTrace();
				}
				
			}
			return completed;
		}
		
	}
	
	/* ----- private helper methods ----- */
	private boolean sendAlertEmail(Throwable ex) {
		String from = properties.getProperty("alert.email");
		String pass = properties.getProperty("alert.password");
		Set<Object> keys = properties.keySet();
		List<String> recipients = new ArrayList<String>(3);
		for (Object key : keys) {
			String sKey = (String) key;
			if (sKey.contains("alert.recipient"))
				recipients.add(properties.getProperty(sKey));
		}
		if (recipients.size() == 0)
			return false;
		Sendmail mail = new Sendmail(from, pass);
		String now = DateTime.now().toString();
		PrintWriter pWriter = new PrintWriter(new StringWriter());
		ex.printStackTrace(pWriter);
		
		String content = "You are receiving this message because you were specified as an alert recipient. \n" +
				"This is an alert to let you know that as of "+now+", GeneWiki Sync Service has attempted and failed to " +
						"restart itself ten times, and is bailing out. To maintain the accuracy of sync services to" +
						" GeneWiki+, please make sure everything is working correctly, check the logs, and restart the service. " +
						"The most recent stacktrace is included below: \n"+pWriter.toString(); 
		try {
			mail.send((String[]) recipients.toArray(), alertSubject, content);
			return true;
		} catch (AddressException e) {
			log(e.getMessage());
			return false;
		} catch (MessagingException e) {
			log(e.getMessage());
			return false;
		}
	}
	
	private static String readln(String prompt) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print(prompt+" ");
		try {
			return br.readLine();
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	private static void log(String message) {
		System.out.println(message);
	}
	
	private static void pause(long duration) {
		try { Thread.sleep(duration); }
		catch (InterruptedException e) {}
	}
	
}
