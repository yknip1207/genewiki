package org.gnf.pbb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.gnf.pbb.controller.PBBController;
import org.gnf.pbb.exceptions.PbbExceptionHandler;
import org.gnf.pbb.exceptions.Severity;
import org.gnf.pbb.logs.DatabaseManager;

public class ProteinBoxBot {
	static PbbExceptionHandler exHandler; // Bridge between the bot state and this controller
	
	public static void main(String[] args) {
		OptionParser parser = new OptionParser();
		parser.accepts("resume");
		parser.accepts("ids");
		parser.accepts("init");
		parser.accepts("help");
		
		OptionSet options = parser.parse(args);
		
		if (options.has("resume")) {
			resume();
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
		print("# A tool to maintain the Gene Wiki, developed by  #");
		print("# the Su Lab @ The Scripps Research Institute.    #");
		print("# Maintained by Erik Clarke (eclarke@scripps.edu) #");
		print("###################################################");
		print("#                                                 #");
		print("#  Main Menu:                                     #");
		print("#  ---------------------------------------------  #");
		print("#  1. Continue using general db of ids            #");
		print("#  2. Update specific ids                         #");
		print("#  3. Initialize database                         #");
		print("#  4. Get help                                    #");
		print("#  5. Quit                                        #");
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
			case 2: specifyIds();			break;
			case 3: initialize();			break;
			case 4: help();					break;
			case 5: quit();					break;
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
	
	public static void resume() {
		exHandler = PbbExceptionHandler.INSTANCE;

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
		print("Delete the PbbGeneral.db sqlite database manually to reinitialize the bot.");
	}
	
	public static void help() {
		print("Refer to documentation on http://code.google.com/p/genewiki");
	}
	
	public static void quit() {
		
	}
	
	/**
	 * Yeah, it's exactly what it looks like.
	 * @param str
	 */
	public static void print(String str) {
		System.out.println(str);
	}
	
	private static void launchBot(List<String> inputs) {
		Configs.GET.setFromFile("bot.properties");
		// We're running the bot controller in a thread to allow us to monitor the bot's state 
		Thread controller = new Thread(new PBBController(inputs));
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
