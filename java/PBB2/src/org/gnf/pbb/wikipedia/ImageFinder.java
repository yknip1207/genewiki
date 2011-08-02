package org.gnf.pbb.wikipedia;

import org.gnf.pbb.Configs;
import org.gnf.pbb.exceptions.ImageNotFoundException;
import org.gnf.pbb.exceptions.ExceptionHandler;

import info.bliki.api.User;
import info.bliki.api.Connector;
public class ImageFinder {
	
	public static boolean imageExists(String title) {
		return SimpleWpCtrl.pageExists(title);
	}

	/**
	 * @param pdb id
	 * @return valid URI for the image from commons.wikimedia.org
	 * @throws ImageNotFoundException if image does not exist
	 */
	public static String imageFromPDB(String pdb) throws ImageNotFoundException {
		String title = "PDB_"+pdb+"_EBI.jpg";
		if (ImageFinder.imageExists("File:"+title)) {
			return title;
		} else {
			throw new ImageNotFoundException();
		}
	}
	
	public static String imageFromSymbol(String sym, String pdb) throws ImageNotFoundException {
		String title = "Protein_"+sym+"_PDB_"+pdb+".png";
		if (ImageFinder.imageExists("File:"+title)) {
			return title;
		} else {
			throw new ImageNotFoundException();
		}
	}
	
	/**
	 * Tries both known sources for images and returns the first one it finds that's valid.
	 * @param sym
	 * @param pdb
	 * @return
	 * @throws ImageNotFoundException
	 */
	public static String getImage(String sym, String pdb) throws ImageNotFoundException {
		try {
			return ImageFinder.imageFromSymbol(sym, pdb);
		} catch (ImageNotFoundException e) {
			return ImageFinder.imageFromPDB(pdb);
		}
	}
	
	/**
	 * For testing...
	 * @param args
	 */
	public static void main(String[] args) {
		Configs.GET.setFromFile("bot.properties");
		try {
			System.out.println(ImageFinder.getImage("CRP","1b09"));
		} catch (ImageNotFoundException e) {
			System.out.println("No image found.");
		}
	}
	
}

class SimpleWpCtrl {
	
	/**
	 * Provides a simple login and query interface using org.gnf.wikiapi with wikimedia
	 * commons.
	 * @param title
	 * @return true if page exists
	 */
	public static boolean pageExists(String title) {
		User user = new User(Configs.GET.str("username"), Configs.GET.str("password"),
				"http://commons.wikimedia.org/w/api.php");
		String[] valuePairs =  {"titles", title};
		Connector connector = new Connector();
		String response = connector.queryXML(user, valuePairs);
		boolean result = response.contains("missing=\"\"") ? false : true;
		return result;
	}
	
}