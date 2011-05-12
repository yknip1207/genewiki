package org.gnf.pbb;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.gnf.pbb.controller.PBBController;
import org.gnf.pbb.exceptions.NoBotsException;
import org.gnf.pbb.exceptions.ValidationException;
import org.gnf.pbb.wikipedia.WikipediaController;
import org.junit.BeforeClass;
import org.junit.Test;

public class ControllerTest {
	static PBBController controller;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		List<String> ids = new ArrayList<String>(0);
		controller = new PBBController(true, true, true, true, true, ids);
	}

	@Test
	public void testCreateUpdate() {
		controller.global.canExecute(false);
		assertFalse(controller.createUpdate());
		controller.reset();
	}

	@Test
	public void testReset() {
		controller.global.canExecute(false);
		controller.reset();
		assertTrue(controller.global.canExecute());
		controller.reset();
	}

	@Test
	public void testImportWikipediaData() {
		WikipediaController wpcontrol = new WikipediaController();
		String test = "{GNF_Protein_box \n {{nobots}} }";
		wpcontrol.writeContentToCache(test, "testTemplate");
		controller.global.set("usecache", true); // just to make sure we're looking in the cache here
		try {
			controller.importWikipediaData("testTemplate");
			fail(); // If no exceptions are raised.
		} catch (ValidationException e){
			// Don't care.
		} catch (NoBotsException nbe) {
			
		}
	}

	@Test
	public void testExecuteUpdateForId() {
		// This should set the execution state to stopped as it's not a valid ID
		controller.resetAndExecuteUpdateForId("abcd");
		assertFalse(controller.global.canExecute());
		
		// This should do the opposite as it's a valid ID
		controller.resetAndExecuteUpdateForId("410");
		assertTrue(controller.global.canExecute());
	}

	@Test
	public void testUpdate() {
		controller.prepareUpdateForId("410");
		controller.global.canUpdate(false);
		controller.global.set("dryrun", false);
		assertFalse(controller.update());
		controller.reset();
		
	}

}
