package tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import net.sourceforge.jwbf.core.contentRep.Article;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;

import org.gnf.pbb.Authenticate;
import org.gnf.pbb.controller.InfoBoxParser;


public class JwbfTest {
	public static final String TestPageTitle = "User:Pleiotrope/sandbox/test_gene_sandbox";

	public static void main(String[] args) throws Exception{
		final String tmpLocation = "C:\\tmp\\pageContents";
		File tmpFile = new File(tmpLocation);
		StringBuffer text = new StringBuffer();
		String content = "";
		boolean forceRefresh = false;
		
		MediaWikiBot bot = new MediaWikiBot("http://en.wikipedia.org/w/");
		String[] credentials = Authenticate.login("credentials.json");
		//XXX

		if (tmpFile.exists() && forceRefresh == false) { // don't need to pull this file endlessly
			String nl = System.getProperty("line.separator");
			Scanner scanner = new Scanner(new FileInputStream(tmpLocation));
			try {
				while (scanner.hasNextLine()) {
					text.append(scanner.nextLine() + nl);
				}
			} finally {
				scanner.close();
			}
			content = text.toString();
		} else {
			if (bot.isLoggedIn() == false) {
				System.out.print("Logging in... ");
				bot.login(credentials[0], credentials[1]);
				System.out.println("Success!");
			}
		
			Article artTemplate = bot.readContent(TestPageTitle);
			//System.out.println(artTemplate.getText());
		
			String pageContent = artTemplate.getText();
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\tmp\\pageContents"));
				writer.write(pageContent);
				writer.close();
				content = pageContent;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		InfoBoxParser.setFieldsFromText(content);
		//InfoBoxParser.fieldsIntegrityCheck();
		
		//LinkedHashMap<String,String> templateMap = new LinkedHashMap<String,String>();
		
		
				
		//SimpleArticle sa = new SimpleArticle(bot.readContent(TestPageTitle));
		//String ArticleText = sa.getText();
		
		//SimpleArticle writeTest = new SimpleArticle();
//		sa.setMinorEdit(true);
//		if (!ArticleText.contains("Greetings from PBB2")) {
//			//sa.addText("//n Another minor modification to the talk page by ~~~~");
//			//bot.writeContent(sa);
//		} else {
//			System.out.println("Conditional modification failed; article was not modified.");
//		}
		
//		GeneObject proteinBox = new GeneObject(410);
//		GeneOntology GoComp = proteinBox.getGoComponents();
//		System.out.println(Arrays.toString(GoComp.getCollated()));
//		System.out.println(proteinBox.getMmGenLocChr());
		
	}
	
	
}
