package tests;

import org.gnf.pbb.Authenticate;

import org.gnf.genewiki.parse.*;

import org.gnf.wikiapi.query.Edit;

public class EditTest {
	// As of 1 April 2011 this gene is a stub; using it for testing
	public static final String testGene = "http://en.wikipedia.org/wiki/Arylsulfatase_A";

	public static void main(String[] args) {
		
		// Logging in to wikipedia...
		try {
			Authenticate.login("credentials.json");
		} catch (Exception e) {
			System.out.println("There was an error authenticating.");
			e.printStackTrace();
		}
		
		// Find the specified page and make a minor edit
		
	}
	
}
