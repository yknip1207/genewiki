package tests;

import org.gnf.pbb.Authenticate;
import org.gnf.wikiapi.User;

import net.sourceforge.jwbf.core.contentRep.SimpleArticle;
import net.sourceforge.jwbf.mediawiki.bots.*;


public class JwbfTest {

	public static void main(String[] args) throws Exception{
		MediaWikiBot bot = new MediaWikiBot("http://en.wikipedia.org/w/");
		//User user = Authenticate.login("credentials.json", false);
		if (bot.isLoggedIn() == false) {
			System.out.print("Logging in... ");
			bot.login("Pleiotrope", "Cynosura42");
			System.out.println("Success!");
		}
		
		SimpleArticle sa = new SimpleArticle(bot.readContent("Talk:Arylsulfatase_A"));
		String ArticleText = sa.getText();
		System.out.println(ArticleText);
		
		//SimpleArticle writeTest = new SimpleArticle();
		sa.setMinorEdit(true);
		if (!ArticleText.contains("Greetings from PBB2")) {
			//sa.addText("//n Another minor modification to the talk page by ~~~~");
			//bot.writeContent(sa);
		} else {
			System.out.println("Conditional modification failed; article was not modified.");
		}
		
	}
	
	
}
