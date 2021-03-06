package org.genewiki.pbb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.genewiki.pbb.controller.BotController;
import org.genewiki.pbb.db.DatabaseManager;
import org.genewiki.pbb.exceptions.ExceptionHandler;
import org.genewiki.pbb.exceptions.Severity;

/**
 * The main entry point for running a BotController from the command line.
 * @author eclarke@scripps.edu
 *
 */
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
		parser.accepts("help");
		
		OptionSet options = parser.parse(args);
		if (options.has("resume")) {
			resume();
		} else if (options.has("restart")) {
			restart();
		} else if (options.has("ids")) {
			specifyIds();
		} else if (options.has("init")) {
			initialize();
		} else if (options.has("help")) {
			try {
				parser.printHelpOn(System.out);
			} catch (IOException e) {
				e.printStackTrace();
			}
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
		print("#  Maintained by: eclarke@scripps.edu             #");
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
		print("#  Other:                                         #");
		print("#  ---------------------------------------------  #");
		print("#  5. More info                                   #");
		print("#  6. Quit                                        #");
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
			case 5: help();					break;
			case 6: quit();					break;
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
	
	
	public static void help() {
		print("Refer to documentation at http://code.google.com/p/genewiki");
	}
	
	public static void quit() {
		System.exit(0);
	}
	
	private static void print(String str) {
		System.out.println(str);
	}
	
	
	private static void launchBot(List<String> inputs) {
		Configs.INSTANCE.setFromFile("bot.properties");
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
				Thread.sleep(500); // Loop doesn't need to execute very quickly
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
