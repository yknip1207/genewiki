/**
 * 
 */
package org.gnf.pbb;

import java.io.File;
import java.io.IOException;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

// GNF extension and reduction of the Bliki (gwtwiki) codebase
// http://code.google.com/p/gwtwiki
import org.gnf.wikiapi.User;

/**
 * 
 * A class that uses credentials.json to authenticate to wikipedia
 * example credentials.json:
 * 
 * |{
 * | "user":"[your wikipedia username]".
 * | "password:"[your wikipedia password]"
 * |}
 * 
 * @author eclarke
 *
 */
public class Authenticate {

	public static String[] login(String credFile) throws Exception {
		/**
		 * Uses a credentials.json file passed to it to log in to wikipedia.
		 */
		User user = null;
		String[] credentials = {null, null};
		// credentials[0] = user; credentials[1] = password

		// using jackson json libraries; safer than JSON.org libs
		JsonFactory jf = new JsonFactory();
		JsonParser jp = jf.createJsonParser(new File(credFile));
		
		if (jp.nextToken() != JsonToken.START_OBJECT){
			throw new IOException("Invalid JSON format in " + credFile);
		}
		while (jp.nextToken() != JsonToken.END_OBJECT) {
			String fieldName = jp.getCurrentName();
			jp.nextToken();
			if (fieldName.equals("user")) {
				credentials[0] = jp.getText();
			} else if (fieldName.equals("password")) {
				credentials[1] = jp.getText();
			}
		}
		
		return credentials;
		
//		if (completeLogin) {
//			try{
//				user = new User(credentials[0],credentials[1],"http://en.wikipedia.org/w/api.php");
//				Boolean rawLoginResponse = user.login();
//				if (rawLoginResponse == null) { 
//					System.out.println("Error logging in; server returned response:");
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		return user;
	}
	
	public static void main(String[] args) {
		try {
			String[] credentials = login("credentials.json");
			System.out.println(credentials[0]+ " " +credentials[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
