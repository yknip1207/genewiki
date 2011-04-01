package tests;

/**
 * Runs a query against Wikipedia for pages with a specific category.
 * Tests for the presence and functionality of gwtwiki/bliki api libraries (as a sanity check).
 * Also a way to practice basic XML parsing.
 * @author eclarke@gnf.org
 */



import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;


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
		String cmcontinue = findNodeInXml(rawXmlResponse, "categorymembers", "cmcontinue");
		while(cmcontinue != null) {
			String[] valuePairs2 = {"list", "categorymembers", "cmtitle", "Category:Physics", "cmcontinue", cmcontinue};
			rawXmlResponse = connector.queryXML(user, valuePairs2);
			if (rawXmlResponse == null) {
				System.out.println("No XML result for query.");
			}
			System.out.println(rawXmlResponse);
			
			cmcontinue = findNodeInXml(rawXmlResponse, "categorymembers", "cmcontinue");
		}
	}
	
	public static String findNodeInXml(String rawXmlResponse, String queryNode, String queryAttribute) {

		// Parses the raw XML response from wp and looks for the queried attribute
		// of a queried node so that we can get the next round of results.
		// For this to work in our context, queryNode should be categorymembers and 
		// queryAttribute should be cmcontinue.
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource input = new InputSource();
			input.setCharacterStream(new StringReader(rawXmlResponse));
			String cmcontinue = null;
			Document doc = db.parse(input);
			NodeList nodes = doc.getElementsByTagName(queryNode);
			for (int i = 0; i < nodes.getLength(); i++) {
				Element element = (Element) nodes.item(i);
				// I was trying to make this more general for no particular reason,
				// but I wound up just hard-coding some stuff into it for this application.
				if(element.hasAttribute(queryAttribute)) {
					cmcontinue = element.getAttribute(queryAttribute);
				}
			}
			System.out.println("Query attribute value: " + cmcontinue);
			
			return cmcontinue;
		} catch (Exception e) {
			System.out.println("Error :(");
			e.printStackTrace();
		}
		
		return null;
	}
	

	public static String getCharacterDataFromElement(Element e) {
		Node child = e.getFirstChild();
		if (child instanceof CharacterData) {
			CharacterData cd = (CharacterData) child;
			return cd.getData();
		}
		return null;
	}

	public static void main(String[] args) {
		testQueryCategoryMembers();
	}
}
