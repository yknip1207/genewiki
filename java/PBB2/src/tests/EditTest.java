package tests;

import org.gnf.pbb.Authenticate;

public class EditTest {

	public static void main(String[] args) {
		try {
			Authenticate.login("credentials.json");
		} catch (Exception e) {
			System.out.println("There was an error authenticating.");
			e.printStackTrace();
		}
	}
	
}
