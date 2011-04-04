package tests;

import org.gnf.pbb.Authenticate;
import org.gnf.pbb.ProteinBox;

import net.sourceforge.jwbf.core.contentRep.SimpleArticle;
import net.sourceforge.jwbf.mediawiki.bots.*;


public class JwbfTest {
	public static final String TestPageTitle = "User:Pleiotrope/sandbox/test_gene_sandbox";

	public static void main(String[] args) throws Exception{
		MediaWikiBot bot = new MediaWikiBot("http://en.wikipedia.org/w/");
		String[] credentials = Authenticate.login("credentials.json");
		//XXX
//		if (bot.isLoggedIn() == false) {
//			System.out.print("Logging in... ");
//			bot.login(credentials[0], credentials[1]);
//			System.out.println("Success!");
//		}
//		
//		SimpleArticle sa = new SimpleArticle(bot.readContent(TestPageTitle));
//		String ArticleText = sa.getText();
//		
//		//SimpleArticle writeTest = new SimpleArticle();
//		sa.setMinorEdit(true);
//		if (!ArticleText.contains("Greetings from PBB2")) {
//			//sa.addText("//n Another minor modification to the talk page by ~~~~");
//			//bot.writeContent(sa);
//		} else {
//			System.out.println("Conditional modification failed; article was not modified.");
//		}
		
		ProteinBox proteinBox = new ProteinBox("410");
		proteinBox.setValuesFromSource(410);
		
		
	}
	
	
}
