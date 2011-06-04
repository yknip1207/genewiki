package org.gnf.pbb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.gnf.pbb.controller.PBBController;
import org.gnf.pbb.exceptions.PbbExceptionHandler;
import org.gnf.pbb.exceptions.Severity;
import org.gnf.pbb.exceptions.ValidationException;
import org.gnf.pbb.logs.DatabaseManager;
import org.gnf.pbb.util.Find;
//import org.gnf.pbb.util.Find;


/**
 * Launcher is a command line tool to manipulate and run Protein Box Bot.
 * @author eclarke
 *
 */
public class Launcher {

	public static void main(String[] args) throws InterruptedException, IOException, SQLException {
		Logger logger = Logger.getLogger(Launcher.class.getName());
		Configs.GET.setConfigsFromFile("BotConfigs.json");
		PbbExceptionHandler exHandler = PbbExceptionHandler.INSTANCE;
		DatabaseManager dbManager = new DatabaseManager();
		try {
			dbManager.init();
		} catch (ValidationException e1) {
			System.out.println("Database exists, not overwriting...");
		}
		List<String> inputs = new ArrayList<String>(0);
		
		if (args.length < 1) {
			System.out.println("Retrieving some gene ids to play with instead.");
			inputs  = Find.updateCandidates(100, Configs.GET);
			Collections.shuffle(inputs);
			System.out.println(inputs);
			System.out.println("Press any key to continue.");
			System.in.read();
		}
		
		for (String geneId : args) {
			try {
				Integer.parseInt(geneId);
				inputs.add(geneId);
			} catch (NumberFormatException e) {
				logger.warning(String.format("Supplied argument '%s' is not an integer; omitting.", geneId));
			}
		}
		
		// We're running the bot controller in a thread to allow us to monitor the bot's state 
		Thread controller = new Thread(new PBBController(inputs));
		System.out.println("Starting bot controller now with supplied arguments. \n" +
				"Press q+enter to end bot operation and see completion report.");
		controller.start();

		while (controller.isAlive()) {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			if (br.readLine().equalsIgnoreCase("q")) {
				controller.interrupt();
				controller.join();
				logger.info("Keyboard interrupt detected.");
			}
			if (exHandler.checkState().compareTo(Severity.FATAL) > 0) {
				logger.severe("Bot failure.");
				logger.severe(exHandler.printExceptionStackTraces(exHandler.getExceptionsOfSeverity(Severity.FATAL)));
				controller.interrupt();
				Thread.sleep(10000);
				controller.join(10000);
				
			}
			Thread.sleep(500);
		}
	}
}
