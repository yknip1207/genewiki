package edu.scripps.testing;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.genewiki.Generator;
import org.genewiki.pbb.Configs;
import org.genewiki.util.FileHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GeneratorTest {

	private Generator 	gen;
	private String 		path;
	private String		sep;
	private String		template;
	private FileHandler	fh;
	
	@Before
	public void setUp() throws Exception {
		gen = new Generator();
		sep = System.getProperty("file.separator");
		// looking for files in src/edu/scripps/testing
		path = String.format("src%sedu%sscripps%stesting%s", sep,sep,sep,sep);
		template = "Template1401.txt";
		fh = new FileHandler(path);
		Configs.INSTANCE.setFromFile("bot.properties");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGenerateTemplate() throws FileNotFoundException, IOException {
		assertEquals("Generated template does not match stored template in "+path+"Template1401.txt. " +
				"It is possible the stored template is out-of-date.", fh.read(template), gen.generateTemplate("1401"));
	}

	@Test
	public void testGenerateStub() {
		String actual = gen.generateStub("1401");
		assertTrue("Does not contain PBB template.", actual.contains("{{PBB|geneid=1401}}"));
		assertTrue("Did not correcly include name in beginning.", actual.contains("'''C-reactive protein, pentraxin-related'''"));
		assertTrue("Did not correctly add the stub template with chromosome location", actual.contains("{{gene-1-stub}}"));
	}

}
