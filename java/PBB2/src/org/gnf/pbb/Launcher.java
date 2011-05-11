package org.gnf.pbb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gnf.pbb.controller.PBBController;


/**
 * Launcher is a command line tool to manipulate and run Protein Box Bot.
 * @author eclarke
 *
 */
public class Launcher {

	public static void main(String[] args) throws InterruptedException {
		Logger logger = Logger.getLogger(Launcher.class.getName());
		logger.setLevel(Level.FINE);
		final boolean DRY_RUN = true;
		final boolean USECACHE = false;
		final boolean STRICT_CHECKING = true;
		final boolean VERBOSE = true;
		final boolean DEBUG = true;
		List<String> inputs = new ArrayList<String>(0);
		
		for (String geneId : args) {
			try {
				Integer.parseInt(geneId);
				inputs.add(geneId);
			} catch (NumberFormatException e) {
				logger.warning(String.format("Supplied argument '%s' is not an integer; omitting.", geneId));
			}
		}
		
		Thread controller = new Thread(new PBBController(DRY_RUN, USECACHE, STRICT_CHECKING, VERBOSE, DEBUG, inputs));
		System.out.println("Starting bot controller now with supplied arguments. \n" +
				"Press any key + enter to end bot operation and see completed report.");
		controller.start();
		while (controller.isAlive()) {
			try {
				int ch = System.in.read();
				Character.toString((Character.toChars(ch))[0]);
				controller.interrupt();
				logger.warning("\n * Bot execution interrupted, waiting for termination... *\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ArrayIndexOutOfBoundsException e) {
				controller.interrupt();
				logger.warning("\n *Bot execution interrupted, waiting for termination... *\n");
			}
			controller.join();
		}
		
	}
}
