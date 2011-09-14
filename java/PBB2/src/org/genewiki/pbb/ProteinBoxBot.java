package org.genewiki.pbb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.genewiki.pbb.controller.BotController;
import org.genewiki.pbb.db.DatabaseManager;
import org.genewiki.pbb.exceptions.ExceptionHandler;
import org.genewiki.pbb.exceptions.Severity;
import org.genewiki.pbb.util.PageList;
import org.genewiki.pbb.wikipedia.ProteinBox;
import org.genewiki.pbb.wikipedia.WikipediaInterface;
import org.gnf.wikiapi.Connector;
import org.gnf.wikiapi.User;

public class ProteinBoxBot {
	static ExceptionHandler exHandler; // Bridge between the bot state and this controller
	
	public static void main(String[] args) {
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger logger = org.apache.log4j.Logger.getRootLogger();
		logger.setLevel(org.apache.log4j.Level.OFF);
		
		OptionParser parser = new OptionParser("i:o:");
		parser.accepts("resume");
		parser.accepts("restart");
		parser.accepts("ids");
		parser.accepts("init");
		parser.accepts("templates");
		parser.accepts("stubs");
		parser.accepts("both");
		parser.accepts("check").withRequiredArg();
		parser.accepts("stubs");
		parser.accepts("help");
		
		OptionSet options = parser.parse(args);
		File infile  = null;
		File outfile = null;
		if (options.has("resume")) {
			resume();
		} else if (options.has("restart")) {
			restart();
		} else if (options.has("ids")) {
			specifyIds();
		} else if (options.has("init")) {
			initialize();
		} else if (options.has("templates")) {
			if (options.hasArgument("i")) 
				infile = new File((String) options.valueOf("i"));
			if (options.hasArgument("o"))
				outfile = new File((String) options.valueOf("o"));
			if (infile != null || outfile != null) {
				try { templates(infile, outfile); } 
				catch (FileNotFoundException e) { e.printStackTrace();}
			} else {
				templates(queryList());
			}
		} else if (options.has("stubs")) {
			
		} else if (options.has("help")) {
			try {
				parser.printHelpOn(System.out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (options.hasArgument("check")){
			checkExistence((String) options.valueOf("check"));
		} else {
			showMenu();
		}
	}

	public static void showMenu() {
		print("############## Protein Box Bot v.2 ################");
		print("#                                                 #");
		print("#  Utilities to maintain the GeneWiki project on  #");
		print("#  Wikipedia. Developed by the Su Lab at the      #");
		print("#  Scripps Research Institute, La Jolla, CA.      #");
		print("#                                                 #");
		print("#=================================================#");
		print("#                                                 #");
		print("#  Update Menu:                                   #");
		print("#  ---------------------------------------------  #");
		print("#  1. Continue using general db of ids            #");
		print("#  2. Rescan and update from the beginning        #");
		print("#  3. Update specific ids                         #");
		print("#  4. Initialize database                         #");
		print("#                                                 #");
		print("#  Code Generation Menu:                          #");
		print("#  ---------------------------------------------  #");
		print("#  5. Generate templates for a list of ids        #");
		print("#  6. Generate stubs for a list of ids            #");
		print("#  7. Generate both from one list                 #");
		print("#                                                 #");
		print("#  Other:                                         #");
		print("#  ---------------------------------------------  #");
		print("#  8. Check if title exists on Wikipedia          #");
		print("#  9. Get help                                    #");
		print("#  10. Quit                                        #");
		print("#                                                 #");
		print("###################################################");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		print("");
		System.out.print("Enter a selection: ");
		try {
			String in = br.readLine();
			int selection = Integer.parseInt(in);
			switch (selection) {
			case 1: resume();				break;
			case 2: restart();				break;
			case 3: specifyIds();			break;
			case 4: initialize();			break;
			case 5: templates(queryList()); break;
			case 6: stubs(queryList());		break;
			case 7: genBoth(queryList());	break;
			case 8: checkExistence();		break;
			case 9: help();					break;
			case 10: quit();					break;
			default: 						break;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			print("Invalid entry.");
			showMenu();
		}
	}
	
	/* ---- UPDATE ROUTINES ---- */
	private static void restart() {
		DatabaseManager.init(false);
		DatabaseManager.clearUpdated();
		resume();
		
	}

	public static void resume() {
		exHandler = ExceptionHandler.INSTANCE;
		DatabaseManager.init(false);
		System.out.println("Automatically querying general database for non-updated gene ids...");
		List<String> inputs  = DatabaseManager.findPBBTargets(1000);
		
		System.out.println(inputs);
		System.out.println("Press any key to continue.");
		
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		launchBot(inputs);
	}
	
	public static void specifyIds() {
		print("");
		System.out.print("Enter a list of ids, separated by spaces: ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			String in = br.readLine();
			String[] splitIn = in.split("\\ ");
			List<String> inputBuffer = Arrays.asList(splitIn);
			List<String> inputs = new ArrayList<String>();
			for (String geneId : inputBuffer) {
				try {
					Integer.parseInt(geneId);
					inputs.add(geneId);
				} catch (NumberFormatException e) {
					print(String.format("Supplied id %s is not a valid Entrez ID, omitting...", geneId));
				}
			}
			
			launchBot(inputs);
		} catch (IOException e) {
			e.printStackTrace();
			print("Input error. Returning to main menu....");
			showMenu();
		}
	}
	
	public static void initialize() {
		print("Warning! This option will overwrite any pre-existing databases with the same name as the new database.");
		print("Are you sure you want to continue? [y/N] ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			String in = br.readLine();
			if (in.startsWith("y")) {
				DatabaseManager.init(true);
				print("Initialization successful. Database may need to be populated (but populate function not yet implemented...)");
			} else {
				print("Did not initialize database.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/* ---- CODE GENERATION ROUTINES ---- */
	private static void templates(File infile, File outfile) throws FileNotFoundException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(infile)));
		String line = "";
		List<String> ids = new ArrayList<String>();
		try {
			while ((line = br.readLine()) != null) {
				try {
					Integer.parseInt(line);
					ids.add(line);
				} catch (NumberFormatException e) {
					print(String.format("Supplied id '%s' is not a valid Entrez ID, omitting...", line));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		templates(ids);
		
	}
	private static void templates(List<String> ids) {
		Configs.GET.setFromFile("bot.properties");
		Generator gen = new Generator();
		BotController bot = new BotController(ids);
		for (String id : ids) {
			gen.generateTemplate(id);
			ProteinBox box = gen.getBox();
			box.setEditSummary("Created new GNF_Protein_box template.");
			print("Ready to push to Wikipedia...");
			waitForReturn();
			try { bot.update(box); }
			catch (Exception e) { e.printStackTrace(); }
		}
	}
	
	public static void stubs(List<String> ids) {
		Configs.GET.setFromFile("bot.properties");
		exHandler = exHandler.INSTANCE;	
		Generator gen = new Generator();
		BotController bot = new BotController(ids);
		WikipediaInterface wpi = new WikipediaInterface(exHandler, Configs.GET);
		User user = new User(Configs.GET.str("username"), Configs.GET.str("password"),
				"http://commons.wikimedia.org/w/api.php");
		
		Connector connector = new Connector();
		for (String id : ids) {
			String stub = gen.generateStub(id);
			String summary = "Created new page for further expansion as part of the Gene Wiki project.";
			print(stub);
			print("Ready to push to stub for "+id+" to Wikipedia. Please verify this title: ");
			String title = gen.getSymbol();
			print(title);
			if (waitForReturn().equalsIgnoreCase("n")) {
				print("Enter a new title: ");
				title = waitForReturn();
			}
			String[] valuePairs = {"titles", title};
			String response = connector.queryXML(user, valuePairs);
			if (response.contains("missing=\"\"")) {
				wpi.putArticle(stub, title, summary);
				print("Created new stub with title: "+title);
			} else {
				print("That title already exists. Please select another: ");
				title = waitForReturn();
				print("I'm not checking again, so please verify this is the title you want... ");
				waitForReturn();
				wpi.putArticle(stub, title, summary);
			}
		}
	}
	
	public static void genBoth(List<String> ids){
		//TODO expand me 
	}
	
	/** 
	 * User-facing wrapper to check a title's existence
	 * @return true if exists
	 */
	public static boolean checkExistence() {
		System.out.print("Enter the title to check: ");
		String title = waitForReturn();
		return checkExistence(title);
	}
	
	/**
	 * Checks the existence of the specified title on Wikipedia
	 * @param title
	 * @return true if exists
	 */
	public static boolean checkExistence(String title) {
		Map<String, String> map = null;
		try {
			map = PageList.in();
		} catch (FileNotFoundException e) {
			PageList.out();
			System.out.println("Created new entrez-title map.");
		//	checkExistence(title); // recursion! kind of
			return false;
		}
		
		if (map.containsKey(title)) {
			System.out.print(map.get(title));
			return true;
		} else if (map.containsValue(title)) {
			System.out.print(title);
			return true;
		} else {
			if (!Configs.GET.initialized())
				Configs.GET.setFromFile("bot.properties");
			User user = new User(Configs.GET.str("username"), Configs.GET.str("password"),
					"http://en.wikipedia.org/w/api.php");
			Connector connector = new Connector();
			String[] valuePairs = {"titles", title, "redirects",""};
			String response = connector.queryXML(user, valuePairs);
			if (response.contains("missing=\"\"")) {
				System.out.print("_false");
				return false;
			} else if (response.contains("<redirects>")){
				String line = "<r from="+title+" to=";
				int a = response.indexOf(line, response.indexOf("<redirects>")+11)+line.length();
				int b = response.indexOf(" />", a);
				String redirectedTitle = response.substring(a, b);
				
				System.out.print(redirectedTitle);
				return true;
			} else {
				System.out.print(title);
				return true;
			}
		}
		
		
	}
	
	public static void help() {
		print("Refer to documentation on http://code.google.com/p/genewiki");
	}
	
	public static void quit() {
		
	}
	
	private static List<String> queryList() {
		print("");
		System.out.print("Enter a list of ids, separated by spaces: ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			String in = br.readLine();
			String[] splitIn = in.split("\\ ");
			List<String> inputBuffer = Arrays.asList(splitIn);
			List<String> inputs = new ArrayList<String>();
			for (String geneId : inputBuffer) {
				try {
					Integer.parseInt(geneId);
					inputs.add(geneId);
				} catch (NumberFormatException e) {
					print(String.format("Supplied id %s is not a valid Entrez ID, omitting...", geneId));
				}
			}
			
			return inputs;
		} catch (IOException e) {
			e.printStackTrace();
			print("Exiting...");
			System.exit(1);
			return null; // this should be dead code
		}
	}
	
	/**
	 * Yeah, it's exactly what it looks like.
	 * @param str
	 */
	private static void print(String str) {
		System.out.println(str);
	}
	
	private static String waitForReturn() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try { return br.readLine(); }
		catch (Exception e) { return null; }
	}
	
	private static void launchBot(List<String> inputs) {
		Configs.GET.setFromFile("bot.properties");
		// We're running the bot controller in a thread to allow us to monitor the bot's state 
		Thread controller = new Thread(new BotController(inputs));
		System.out.println("Starting bot controller now with supplied arguments. \n" +
				"Press q+enter to end bot operation and see completion report.");
		controller.start();
		try { 
			while (controller.isAlive()) {
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				if (br.readLine().equalsIgnoreCase("q")) {
					controller.interrupt();
					controller.join();
					print("Keyboard interrupt detected. \n");
				}
				if (!exHandler.canRecover()) {
					print("Bot failure.");
					print(exHandler.printExceptionStackTraces(exHandler.getExceptionsOfSeverity(Severity.FATAL)));
					controller.interrupt();
					controller.join();
					
				}
				Thread.sleep(500); // Don't want this loop to hog all the resources. 
								   // Adding a short sleep timer in here allows the JVM to do other stuff
								   // - at least in theory.
			}
			print("");
		} catch (InterruptedException ie) {
			print("Bot interrupted. Returning to main menu....");
			showMenu();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
