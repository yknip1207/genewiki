package org.genewiki.pbb.wikipedia;

import org.genewiki.pbb.Configs;
import org.genewiki.pbb.exceptions.ImageNotFoundException;
import org.gnf.wikiapi.Connector;
import org.gnf.wikiapi.User;

import com.google.common.base.Preconditions;

public class ImageFinder {
	
	public static boolean imageExists(String title) {
		return SimpleWpCtrl.pageExists(title);
	}

	/**
	 * @param pdb id
	 * @return valid URI for the image from commons.wikimedia.org
	 * @throws ImageNotFoundException if image does not exist
	 */
	public static String imageFromPDB(String pdb) {
		String title = "PDB_"+pdb+"_EBI.jpg";
		if (ImageFinder.imageExists("File:"+title)) {
			return title;
		} else {
			return null;
		}
	}
	
	public static String imageFromSymbol(String sym, String pdb) {
		String title = "Protein_"+sym+"_PDB_"+pdb+".png";
		if (ImageFinder.imageExists("File:"+title)) {
			return title;
		} else {
			return null;
		}
	}
	
	/**
	 * Tries both known sources for images and returns the first one it finds that's valid.
	 * @param sym
	 * @param pdb
	 * @return
	 * @throws ImageNotFoundException
	 */
	public static String getImage(String sym, String pdb) {
		try {
			return Preconditions.checkNotNull(ImageFinder.imageFromSymbol(sym, pdb));
		} catch (NullPointerException e) {
			return ImageFinder.imageFromPDB(pdb);
		}
		
		
	}
	
	/**
	 * For testing...
	 * @param args
	 */
	public static void main(String[] args) {
		Configs.INSTANCE.setFromFile("bot.properties");
		try {
			System.out.println(Preconditions.checkNotNull(ImageFinder.getImage("CRP","1b09")));
		} catch (NullPointerException e) {
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
		User user = new User(Configs.INSTANCE.str("username"), Configs.INSTANCE.str("password"),
				"http://commons.wikimedia.org/w/api.php");
		String[] valuePairs =  {"titles", title};
		Connector connector = new Connector();
		String response = connector.queryXML(user, valuePairs);
		boolean result = response.contains("missing=\"\"") ? false : true;
		return result;
	}
	
}