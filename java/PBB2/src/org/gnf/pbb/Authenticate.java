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

	public static void login(String credFile) throws Exception {
		/**
		 * Uses a credentials.json file passed to it to log in to wikipedia.
		 */
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
		
		try{
			User user = new User(credentials[0],credentials[1],"http://en.wikipedia.org/w/api.php");
			user.login();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
