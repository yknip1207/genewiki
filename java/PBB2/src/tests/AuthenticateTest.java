package tests;

import java.io.File;
import java.io.IOException;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import info.bliki.api.*;

public class AuthenticateTest {

	public static String[] credParser(String file) throws Exception {
		String[] credentials = {null, null};
		// credentials[0] = user; credentials[1] = password

		// using jackson json libraries; safer than JSON.org java libs
		//DEBUGGING
		File dir1 = new File(".");
		System.out.println("Current dir: " + dir1.getCanonicalPath());
		JsonFactory jf = new JsonFactory();
		JsonParser jp = jf.createJsonParser(new File(file));
		
		if (jp.nextToken() != JsonToken.START_OBJECT){
			throw new IOException("format error, expected data to start with an object");
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
	}
	
	public static void main(String[] args) {
		
		try{
			String[] credentials = credParser("credentials.json");
			User user = new User(credentials[0],credentials[1],"http://en.wikipedia.org/w/api.php");
			user.login();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}
