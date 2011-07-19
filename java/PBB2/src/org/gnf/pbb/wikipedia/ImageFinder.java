package org.gnf.pbb.wikipedia;

import org.gnf.pbb.Configs;
import org.gnf.pbb.exceptions.ImageNotFoundException;
import org.gnf.pbb.exceptions.PbbExceptionHandler;
import org.gnf.wikiapi.User;
import org.gnf.wikiapi.Connector;

public class ImageFinder {
	
	public static boolean imageExists(String title) {
		return SimpleWpCtrl.pageExists(title);
	}

	/**
	 * @param pdb id
	 * @return valid URI for the image from commons.wikimedia.org
	 * @throws ImageNotFoundException if image does not exist
	 */
	public static String imageForPDB(String pdb) throws ImageNotFoundException {
		String title = "PDB_"+pdb+"_EBI.jpg";
		if (ImageFinder.imageExists("File:"+title)) {
			return title;
		} else {
			throw new ImageNotFoundException();
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