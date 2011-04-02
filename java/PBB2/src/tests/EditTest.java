package tests;

import org.gnf.pbb.Authenticate;
import org.gnf.pbb.editor.XmlParser;
import org.gnf.wikiapi.Connector;
import org.gnf.wikiapi.User;
import org.gnf.wikiapi.query.RequestBuilder;


public class EditTest {
	// As of 1 April 2011 this gene is a stub; using it for testing
	public static final String testGeneUrl = "http://en.wikipedia.org/wiki/Arylsulfatase_A";
	public static final String testGeneTitle = "Arylsulfatase_A";
	public static String token = null;

	public static void main(String[] args) {
		
		// Logging in to wikipedia...
		User user = null;
		try {
			user = Authenticate.login("credentials.json");
			System.out.println(user.getToken());
		} catch (Exception e) {
			System.out.println("There was an error authenticating.");
			e.printStackTrace();
		}
		

		
		// Find the specified page and make a minor edit
		Connector connector = new Connector();
		setEditToken(user, connector);
		RequestBuilder rb = new RequestBuilder();
		rb.action("edit");
		rb.format("json");
		rb.put("title", "Talk:"+testGeneTitle);
		rb.put("section", "new");
		rb.put("summary", "Hello World!");
		rb.put("text", "Greetings from PBB2!");
		rb.put("token", "+\\");
		System.out.println(rb.toString());
		// Actually write something:
		//System.out.println(connector.sendXML(user, rb));
		
		
		
	}

	private static void setEditToken(User user, Connector connector) {
		String[] valuePairs = {"post", "info|revisions", "intoken", "edit", "titles", testGeneTitle};
		String rawXmlResponse = connector.queryXML(user, valuePairs);
		System.out.println(rawXmlResponse); //XXX
		if (rawXmlResponse == null) {
			System.out.println("No results returned for query.");
		} else {
			token = XmlParser.findAttributeInXml(rawXmlResponse, "page", "edittoken");
			if (token == null) {
				System.out.println("Edit token returned null; unable to continue.");
			}
		}
	}
	
}
