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
		Global global = Global.getInstance();
		global.set("firstCall", true);
		Logger logger = Logger.getLogger(Launcher.class.getName());
		logger.setLevel(Level.FINE);
		final boolean DRY_RUN = false;
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
		Thread detectKeyEntry = new Thread(new DetectKey());
		System.out.println("Starting bot controller now with supplied arguments. \n" +
				"Press q+enter to end bot operation and see completion report.");
		controller.start();
		detectKeyEntry.start();
		while (controller.isAlive()) {
			if (global.botHasFailed()) {
				controller.interrupt();
				controller.join();
				logger.severe("Bot failure.");
			}
			if (!detectKeyEntry.isAlive()) {
				controller.interrupt();
				controller.join();
				logger.warning("Interrupt detected.");
			}
			
		}
	}
}

class DetectKey implements Runnable {
	char[] chars = new char['0'];
	
	@Override
	public void run() {
		try {
			chars = Character.toChars(System.in.read());
			if (chars[0] == 'q')
				return;
		} catch (IOException e) {
			return;
		}
		
		
	}
	
}