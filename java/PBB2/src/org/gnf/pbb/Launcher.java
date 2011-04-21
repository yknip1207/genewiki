package org.gnf.pbb;

import java.util.LinkedHashMap;
import java.util.List;

import org.gnf.pbb.controller.*;
import org.gnf.pbb.exceptions.NoBotsException;


/**
 * Launcher is a command line tool to manipulate and run Protein Box Bot.
 * @author eclarke
 *
 */
public class Launcher {

	public static void main(String[] args) {
		// the controller needs to be initialized before anything interesting can be done
		PBBController controller = new PBBController();
		
		final boolean OVERWRITE = true;
		final boolean DRY_RUN = true;
		final boolean USECACHE = false;
		
		for (String geneId : args) {
			// Representation of the data from mygene.info as hash map
			LinkedHashMap<String, List<String>> modelDataMap = controller.importObjectData(geneId).getGeneDataAsMap();
			// Representation of the data from a wikipedia info box as hash map
			LinkedHashMap<String, List<String>> viewDataMap = new LinkedHashMap<String, List<String>>();
			try {
				viewDataMap = controller.importDisplayData(geneId, USECACHE);
			} catch (NoBotsException e) {
				System.out.println(e.getFlagLocation());
				e.printStackTrace();
			}
			// Build an update object
			Update update = controller.updateValues(modelDataMap, viewDataMap, OVERWRITE);
			// And push it to the remote view (wikipedia)
			controller.updateRemoteView(update, DRY_RUN);
			if (!DRY_RUN) {
				System.out.println(update.getEditMessage());
				System.out.println("Wrote new page...");
			}
		}
		
	}
	
}
