package tests;

import org.gnf.pbb.Authenticate;
import org.gnf.pbb.editor.XmlParser;
import org.gnf.wikiapi.Connector;
import org.gnf.wikiapi.User;
import org.gnf.wikiapi.query.Edit;
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
			//user = Authenticate.login("credentials.json");
			//System.out.println(user.getToken());
			throw new Exception ("This class is deprecated, do not run.");
		} catch (Exception e) {
			System.out.println("There was an error authenticating.");
			e.printStackTrace();
		}
		

		
		// Find the specified page and make a minor edit
		Connector connector = new Connector();
		try{
			setEditToken(user, connector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Edit edit = Edit.create();
		//rb.format("json");
		edit.title("Talk:"+testGeneTitle);
		edit.section("new"); //TODO: check if section is already created
		edit.summary("Hello World!");
		edit.text("Greetings from PBB2!");
		edit.token(token); //TODO: do we have a valid token?
		System.out.println(edit.toString());
		// Actually write something:
		//System.out.println(connector.sendXML(user, rb));
		
		
		
	}

	private static void setEditToken(User user, Connector connector) throws Exception {
		String[] valuePairs = {"prop", "info", "intoken", "edit", "titles", "Main_Page"};
		String rawXmlResponse = connector.queryXML(user, valuePairs);
		System.out.println(rawXmlResponse); //XXX
		if (rawXmlResponse == null) {
			System.out.println("No results returned for query.");
		} else {
			token = XmlParser.findAttributeInXml(rawXmlResponse, "page", "edittoken");
			if (token == null) {
				System.out.println("Edit token returned null; unable to continue.");
				throw new Exception("Failed to get non-null edit token.");
			} else if (token == "+\\") {
				throw new Exception("Edit token represents anonymous user... something went wrong.");
			}
		}
	}
	
}
