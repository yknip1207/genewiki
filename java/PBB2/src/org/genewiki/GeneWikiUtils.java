/**
 * 
 */
package org.genewiki;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.genewiki.pbb.Configs;
import org.genewiki.pbb.Generator;
import org.genewiki.pbb.controller.BotController;
import org.genewiki.pbb.exceptions.ExceptionHandler;
import org.genewiki.pbb.util.PageList;
import org.genewiki.pbb.wikipedia.ProteinBox;
import org.genewiki.pbb.wikipedia.WikipediaInterface;
import org.gnf.wikiapi.Connector;
import org.gnf.wikiapi.User;

import com.google.common.base.CharMatcher;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * @author eclarke
 *
 */
public class GeneWikiUtils {
	static ExceptionHandler exHandler = ExceptionHandler.INSTANCE; // Bridge between the bot state and this controller
	static {
		Configs.GET.setFromFile("bot.properties");
	}
	private static boolean upload = false;
	private static String uploadTitle = null;
	private static boolean debug = false; 
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) {
		OptionParser parser = new OptionParser();
		
		OptionSpec<String> templates = parser.accepts("templates", "generate template wikicode")
				.withOptionalArg().ofType(String.class)
				.describedAs("entrez ids");
		OptionSpec<String> stubs = parser.accepts("stubs", "generate stub wikicode")
				.withOptionalArg().ofType(String.class)
				.describedAs("entrez ids");
		parser.accepts("i", "input file").withRequiredArg().describedAs("filename");
		parser.accepts("o", "output file").withRequiredArg().describedAs("filename");
		parser.accepts("check", "check existence of title").withRequiredArg().describedAs("title");
		parser.accepts("upload", "upload result of template or stub option.").withOptionalArg().describedAs("title (req'd for stub)");
		parser.accepts("debug", "debug mode diverts uploads to sandbox");
 		OptionSet options = null;
		try {
			options = parser.parse(args);
		} catch (OptionException e) {
			printHelp(parser);
		}
		
		if (options.has("debug")) {
			debug = true;
		} else {
			System.out.println("debug mode enforced.");
			printHelp(parser);
		}
		
		File infile = null;
		File outfile = null;
		// Set the input and output, if specified (otherwise use stdin/out)
		if (options.has("i"))
			infile = new File((String) options.valueOf("i"));
		if (options.has("o"))
			outfile = new File((String) options.valueOf("o"));
		
		
		
		// If we're uploading results of code generation
		if (options.has("upload")) {
			upload = true;
			if (options.hasArgument("upload")) {
				uploadTitle = (String) options.valueOf("upload");
				if (debug) 
					uploadTitle = "User:Pleiotrope/sandbox/"+uploadTitle;
			} else if (options.has(templates)) {
				Configs.GET.set("templatePrefix", "User:Pleiotrope/sandbox/");
			} else if (options.has(stubs)) {
				System.out.println("Stub upload requires the specification of a title.");
				printHelp(parser);
			}
		}
		
		if (options.has("check")) 
			checkExistence((String) options.valueOf("check")); 
		
		if (options.has(templates)) {
			if (infile != null || outfile != null) {
				templates(infile, outfile);
			} else {
				if (options.hasArgument("template")) {
					List<String> ids = options.valuesOf(templates);
					templates(ids);
				}
			}
 		} 
		
		
		if (options.has(stubs)) {
 			if (infile != null || outfile != null) {
				stubs(infile, outfile);
			} else {
				if (options.hasArgument(stubs)) {
					List<String> ids = options.valuesOf(stubs);
					stubs(ids);
				}
			}
 		}
		
		if (args.length == 0) {
			printHelp(parser);
		}
	}
	
	private static void printHelp(OptionParser parser) {
		System.out.println("GeneWiki Code Generation and Management Utility");
		System.out.println("------------------------------------------------------------");
		try {
			parser.printHelpOn(System.out);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(1);
	}

	private static List<String> stubs(List<String> ids) {
		// we don't handle lists larger than size=1 yet
		if (ids.size() != 1) {
			System.out.println("Parsing more than one id at a time not yet implemented (sorry!)");
			System.exit(-1);
			return Collections.emptyList();
		}
		
		
		Generator gen = new Generator();
		String stub = "";
		for (String id : ids) {
			stub = gen.generateStub(id);
			String summary = "Created page as part of the expansion of the Gene Wiki project.";
			if (upload) {
				WikipediaInterface wpi = new WikipediaInterface();
				String title = strip(uploadTitle, "#{}[]<>|");
				wpi.putArticle(stub, title, summary);
				System.out.print("success!");
				System.exit(0);
				return Collections.emptyList();
			}
		}
		System.out.print(stub);	// here for gwgenerator.py
		return Collections.singletonList(stub);
	}
	
	private static void stubs(File infile, File outfile) {
		// TODO Auto-generated method stub
		
	}
	

	private static List<String> templates(List<String> ids) {
		// we don't handle lists larger than size=1 yet
		if (ids.size() != 1) {
			System.out.println("Parsing more than one id at a time not yet implemented (sorry!)");
			System.exit(1);
			return Collections.emptyList();
		}
		Configs.GET.setFromFile("bot.properties");
		Generator gen = new Generator();
		BotController bot = new BotController(ids);
		for (String id : ids) {
			gen.generateTemplate(id);
			ProteinBox box = gen.getBox();
			box.setEditSummary("Created new GNF_Protein_box template.");
			if (upload) {
				try { 
					bot.update(box); 
					System.out.print("success!");
					System.exit(0);
					return Collections.emptyList();
				}
				catch (Exception e) { e.printStackTrace(); }
			}
			System.out.print(box.toString()); // here for gwgenerator.py
			System.exit(0);
		}
		return Collections.emptyList(); // this should be fixed
	}
	
	private static void templates(File infile, File outfile) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Checks the existence of the specified title on Wikipedia
	 * @param title
	 * @return true if exists
	 */
	public static boolean checkExistence(String title) {
		Map<String, String> map = null;
		try {
			map = PageList.in();
		} catch (FileNotFoundException e) {
			PageList.out();
			System.out.println("Created new entrez-title map.");
		//	checkExistence(title); // recursion! kind of
			return false;
		}
		
		if (map.containsKey(title)) {
			System.out.print(map.get(title));
			return true;
		} else if (map.containsValue(title)) {
			System.out.print(title);
			return true;
		} else {
			if (!Configs.GET.initialized())
				Configs.GET.setFromFile("bot.properties");
			User user = new User(Configs.GET.str("username"), Configs.GET.str("password"),
					"http://en.wikipedia.org/w/api.php");
			Connector connector = new Connector();
			String[] valuePairs = {"titles", title, "redirects",""};
			String response = connector.queryXML(user, valuePairs);
			if (response.contains("missing=\"\"")) {
				System.out.print("_false");
				return false;
			} else if (response.contains("<redirects>")){
				String line = "<r from="+title+" to=";
				int a = response.indexOf(line, response.indexOf("<redirects>")+11)+line.length();
				int b = response.indexOf(" />", a);
				String redirectedTitle = response.substring(a, b);
				
				System.out.print(redirectedTitle);
				return true;
			} else {
				System.out.print(title);
				return true;
			}
		}	
	}
	
	/* ---- Utility methods ---- */
	private static String strip(String str, String chars) {
		CharMatcher matcher = CharMatcher.anyOf(chars);
		return matcher.removeFrom(str);
	}

}
