package org.gnf.pbb.wikipedia;

import org.gnf.pbb.Configs;
import org.gnf.pbb.exceptions.PbbExceptionHandler;
import org.gnf.wikiapi.User;

public class ImageFinder {
	
	/* ---- Declarations ---- */
	private static WikipediaController wpControl;
	
	public ImageFinder(WikipediaController wpControl) {
		ImageFinder.wpControl = wpControl;
	}
	
	public static boolean imageExists(String pdb) {
		String title = "File:PDB_"+pdb+"_EBI.jpg";
		String content = wpControl.getContent(title);
		System.out.println(content);
		return false;
	}
	
	public static void main(String[] args) {
		Configs.GET.setFromFile("bot.properties");
		wpControl = new WikipediaController(PbbExceptionHandler.INSTANCE, Configs.GET);
		ImageFinder.imageExists("1b09");
	}


}

class simpleWpControl {
	
	public static boolean pageExists(String title) {
		User user = new User(Configs.GET.str("username"), Configs.GET.str("password"),
				"http://commons.mediawiki.com/w/api.php");
		return false;
	}
	
}