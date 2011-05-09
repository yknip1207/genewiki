package org.gnf.pbb;

import org.gnf.pbb.controller.PBBController;


/**
 * Launcher is a command line tool to manipulate and run Protein Box Bot.
 * @author eclarke
 *
 */
public class Launcher {

	public static void main(String[] args) {
		
		final boolean DRY_RUN = true;
		final boolean USECACHE = false;
		final boolean STRICT_CHECKING = true;
		final boolean VERBOSE = true;
		final boolean DEBUG = false;
		
		// the controller needs to be initialized before anything interesting can be done
		PBBController controller = new PBBController(DRY_RUN, USECACHE, STRICT_CHECKING, VERBOSE, DEBUG);
		for (String geneId : args) {
			System.out.println("Executing update for gene:" +geneId);
			controller.executeUpdateForId(geneId);
		}
		
	}
	
}
