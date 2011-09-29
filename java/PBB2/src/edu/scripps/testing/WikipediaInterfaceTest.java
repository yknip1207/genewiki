package edu.scripps.testing;

import static org.junit.Assert.*;

import org.genewiki.pbb.Configs;
import org.genewiki.pbb.exceptions.ExceptionHandler;
import org.genewiki.pbb.wikipedia.WikipediaInterface;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WikipediaInterfaceTest {

	WikipediaInterface wpi;
	
	@Before
	public void setUp() throws Exception {
		ExceptionHandler exh = ExceptionHandler.INSTANCE;
		Configs cfg = Configs.INSTANCE;
		cfg.setFromFile("bot.properties");
		cfg.set("templatePrefix", "User:Pleiotrope/sandbox/Template:PBB/");
		exh.reset();
		wpi = new WikipediaInterface(exh, cfg);
	}

	@After
	public void tearDown() throws Exception {
		
	}
	
	@Test
	public void testGetContent() {
		String expected = "This is a wikipedia page!\n";
		String result = wpi.getContent("User:Pleiotrope/sandbox/testPage");
		assertEquals(result, expected);
	}

	@Test
	public void testGetContentForId() {
		String expected = "{{GNF_Protein_box}}\n";
		String result = wpi.getContentForId("1401");
		assertEquals(result, expected);
	}
	
	@Test
	public void testPutArticle() {
		String article = "This is an article!\n";
		String title = "User:Pleiotrope/sandbox/EditMe";
		String changes = "ProteinBoxBot Unit Test";
		wpi.putArticle(article, title, changes);
		String result = wpi.getContent(title);
		assertTrue(result.equals(article));
		String blank = "Ready for next test...\n";
		wpi.putArticle(blank, title, changes);
		result = wpi.getContent(title);
		assertTrue(result.equals(blank));
	}
	
	@Test
	public void testPutTemplate() {
		String template = "This is a template!\n";
		String id = "1017";
		String changes = "ProteinBoxBot Unit Test";
		wpi.putTemplate(template, id, changes);
		assertTrue(wpi.getContentForId("1017").equals(template));
		String blank = "Ready for next test...\n";
		wpi.putTemplate(blank, id, changes);
		assertTrue(wpi.getContentForId("1017").equals(blank));
	}
	
}
