package org.gnf.pbb;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.gnf.pbb.controller.PBBController;


/**
 * Launcher is a command line tool to manipulate and run Protein Box Bot.
 * @author eclarke
 *
 */
public class Launcher {

	public static void main(String[] args) {
		Logger logger = Logger.getLogger(Launcher.class.getName());
		logger.setLevel(Level.FINE);
		final boolean DRY_RUN = false;
		final boolean USECACHE = false;
		final boolean STRICT_CHECKING = true;
		final boolean VERBOSE = true;
		final boolean DEBUG = true;
		
		// the controller needs to be initialized before anything interesting can be done
		PBBController controller = new PBBController(DRY_RUN, USECACHE, STRICT_CHECKING, VERBOSE, DEBUG);
		controller.global.setPrefix("User:Pleiotrope/sandbox/fakeTemplate:PBB/");
		
		for (String geneId : args) {
			controller.executeUpdateForId(geneId);
		}
		
	}
	
}
