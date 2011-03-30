package tests;

/**
 * Runs a query against Wikipedia for pages with a specific category.
 * Tests for the presence and functionality of gwtwiki/bliki api libraries (as a sanity check).
 * @author eclarke@gnf.org
 */

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;

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
		String cmcontinue = findNodeInXml(rawXmlResponse, "categorymembers");
		
		String[] valuePairs2 = {"list", "categorymembers", "cmtitle", "Category:Physics", "cmcontinue", cmcontinue};
		rawXmlResponse = connector.queryXML(user, valuePairs2);
		if (rawXmlResponse == null) {
			System.out.println("No XML result for query.");
		}
		System.out.println(rawXmlResponse);
	}
	
	public static String findNodeInXml(String rawXmlResponse, String queryNode) {
		
//		File xmlfile = new File("C:\\tmp\\queryXMLoutput.xml");
//		BufferedWriter writer = null;
//		try {
//			writer = new BufferedWriter(new FileWriter(xmlfile));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		try {
//			writer.write(rawXmlResponse);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		try {
//			writer.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
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
				// but I wound up just hardcoding some stuff into it for this application.
				if(element.hasAttribute("cmcontinue")) {
					cmcontinue = element.getAttribute("cmcontinue");
				}
			}
			System.out.println("cmcontinue value: " + cmcontinue);
			
			return cmcontinue;
		} catch (Exception e) {
			System.out.println("Exception!");
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
