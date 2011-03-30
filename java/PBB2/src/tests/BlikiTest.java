package tests;

/**
 * Runs a query against Wikipedia for pages with a specific category.
 * Tests for the presence and functionality of gwtwiki/bliki api libraries (as a sanity check).
 * @author eclarke@gnf.org
 */

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.xml.sax.SAXException;
import info.bliki.api.*;


public class BlikiTest {

	public static void testQueryCategoryMembers() {
		User user = new User("", "", "http://en.wikipedia.org/w/api.php");
		user.login();
		
		String[] valuePairs = {"list", "categorymembers", "cmtitle", "Category:Physics"};
		Connector connector = new Connector();
		String rawXmlResponse = connector.queryXML(user, valuePairs);
		if (rawXmlResponse == null) {
			System.out.println("No XML result for query.");
		}
		
		// if more results are available, use "cmcontinue" from the last query result
		String[] valuePairs2 = {"list", "categorymembers", "cmtitle", "Category:Physics", "cmcontinue", "Awards|"};
		rawXmlResponse = connector.queryXML(user, valuePairs2);
		if (rawXmlResponse == null) {
			System.out.println("No XML result for query.");
		}
		System.out.println(rawXmlResponse);
	}
	
	public static void main(String[] args) {
		testQueryCategoryMembers();
	}
}
