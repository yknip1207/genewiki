/**
 * 
 */
package org.genewiki;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import org.genewiki.pbb.Configs;
import org.genewiki.pbb.controller.BotController;
import org.genewiki.pbb.exceptions.ExceptionHandler;
import org.genewiki.pbb.util.PageList;
import org.genewiki.pbb.wikipedia.ProteinBox;
import org.genewiki.pbb.wikipedia.WikipediaInterface;
import org.gnf.wikiapi.Connector;
import org.gnf.wikiapi.User;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * The main entry point for code generation and page-existence utilities.
 * @author eclarke@scripps.edu
 *
 */
public class GeneWikiCodeGenerator {
	static ExceptionHandler exHandler = ExceptionHandler.INSTANCE; // Bridge between the bot state and this controller
	private static boolean upload = false;
	private static String uploadTitle = null;
	private static boolean debug = false; 
	/**
	 * @param args
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		OptionParser parser = new OptionParser();
		
		OptionSpec<String> templates = parser.accepts("templates", "generate template wikicode")
				.withOptionalArg().ofType(String.class)
				.describedAs("entrez ids");
		OptionSpec<String> stubs = parser.accepts("stubs", "generate stub wikicode")
				.withOptionalArg().ofType(String.class)
				.describedAs("entrez ids");
		OptionSpec<String> check = parser.accepts("check", "check title existence")
				.withRequiredArg().ofType(String.class).withValuesSeparatedBy('|')
				.describedAs("titles");
		parser.accepts("i", "input file").withRequiredArg().describedAs("filename");
		parser.accepts("o", "output file").withRequiredArg().describedAs("filename");
		parser.accepts("upload", "upload result of template or stub option.").withOptionalArg().describedAs("title (req'd for stub)");
		parser.accepts("debug", "debug mode diverts uploads to sandbox");
 		OptionSet options = null;
		try {
			options = parser.parse(args);
		} catch (OptionException e) {
			printHelp(parser);
		}
	
		Configs.INSTANCE.setFromFile("bot.properties");
		
		if (options.has("debug")) {
			debug = true;
		} else {
//			System.out.println("debug mode enforced.");
//			printHelp(parser);
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
				if (debug) {
					uploadTitle = "User:Pleiotrope/sandbox/"+uploadTitle;	
					Configs.INSTANCE.set("templatePrefix", "User:Pleiotrope/sandbox/");
				}
			} else if (options.has(stubs)) {
				System.out.println("Stub upload requires the specification of a title.");
				printHelp(parser);
			}
		}
		
		if (options.has(check)) 
			checkExistence((List<String>) options.valuesOf("check")); 
		
		if (options.has(templates)) {
			if (infile != null || outfile != null) {
				templates(infile, outfile);
			} else {
				if (options.hasArgument(templates)) {
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
			System.exit(1);
			return Collections.emptyList();
		}
		
		
		Generator gen = new Generator();
		String stub = "";
		String talk = "{{WikiProjectBannerShell| " +
				"{{Wikiproject MCB|class=stub|importance=Low}} " +
				"{{WikiProject Gene Wiki|class=stub|importance=Low}} }}";
		for (String id : ids) {
			stub = gen.generateStub(id);
			String summary = "Created page as part of the expansion of the Gene Wiki project.";
			if (upload) {
				WikipediaInterface wpi = new WikipediaInterface();
				String title = strip(uploadTitle, "#{}[]<>|");
				wpi.putArticle(stub, title, summary);
				String talkTitle = (title.startsWith("User:"))? title.replace("User:", "User_talk:") : "Talk:"+title;
				wpi.putArticle(talk, talkTitle, summary);
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
	public static List<Boolean> checkExistence(List<String> titles) {
		Map<String, String> map = null;
		List<Boolean> results = new ArrayList<Boolean>(titles.size());
		List<String> titleResults = new ArrayList<String>(titles.size());
		try {
			map = PageList.in();
		} catch (FileNotFoundException e) {
			PageList.out();
			System.out.println("Created new entrez-title map.");
		//	checkExistence(title); // recursion! kind of
			return results;
		}
		if (!Configs.INSTANCE.initialized())
			Configs.INSTANCE.setFromFile("bot.properties");
		User user = new User(Configs.INSTANCE.str("username"), Configs.INSTANCE.str("password"),
				"http://en.wikipedia.org/w/api.php");
		for (String title : titles) {
			if (map.containsKey(title)) {
				titleResults.add(map.get(title));
				results.add(true);
			} else if (map.containsValue(title)) {
				titleResults.add(title);
				results.add(true);
			} else {
				
				Connector connector = new Connector();
				String[] valuePairs = {"titles", title, "redirects",""};
				String response = connector.queryXML(user, valuePairs);
				if (response.contains("missing=\"\"")) {
					titleResults.add("#missing");
					results.add(false);
				} else if (response.contains("<redirects>")){
					String line = "<r from=\""+title+"\" to=\"";
					int a = response.indexOf(line)+line.length();
					int b = response.indexOf("\" />", a);
					String redirectedTitle = response.substring(a, b);
					
					titleResults.add(redirectedTitle);
					results.add(false);
				} else {
					titleResults.add(title);
					results.add(false);
				}
			}
		}
		Joiner joiner = Joiner.on("|");
		System.out.println(joiner.join(titleResults));
		return results;
	}
	
	/* ---- Utility methods ---- */
	private static String strip(String str, String chars) {
		CharMatcher matcher = CharMatcher.anyOf(chars);
		return matcher.removeFrom(str);
	}

}
