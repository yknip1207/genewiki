package org.genewiki.stream;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.genewiki.GeneWikiUtils;

public class Tweeter {

	static String consKey;
	static String consSecret;

	static String accToken;
	static String accSecret;


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String credfile = "/Users/bgood/workspace/Config/gw_creds.txt";
		initTweeter(credfile);
		tweet("#VIPR2 #I9606 added 1 link.	http://en.wikipedia.org/wiki/?action=historysubmit&diff=415616263&oldid=414531076");
	}

	public static void initTweeter(String credfile){
		Map<String, String> creds = GeneWikiUtils.read2columnMap(credfile);
		initTweeter(creds);
	}
	
	public static void initTweeter(Map<String, String> creds){
		consKey = creds.get("consKey");
		consSecret = creds.get("consSecret");
		accToken = creds.get("accToken");
		accSecret = creds.get("accSecret");
	}
	
	public static boolean tweet(String message){
		boolean worked = false;
	    AccessToken accessToken = new AccessToken(accToken,accSecret);
	    Twitter twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(consKey, consSecret);
		twitter.setOAuthAccessToken(accessToken);
		
	    Status status;
		try {
			status = twitter.updateStatus(message);
			if(status!=null&&status.getId()!=0){
				System.out.println("Successfully updated the status to [" + status.getText() + "].");
				worked = true;
			}
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		return worked;
	}
	
/*****
 * Execute this method (with access to the interactive console) to gain the accessToken and accessTokenSecret needed to use a twitter account.
 * You will need to paste the URL from the console into a browser, login with the twitter account, and then get paste the provided key into the console.
 * Elegant? No.
 */
	public static void getNewAccessToken(){

		String tweet = "one tweet from the twitter4j api!";
		try {
			Twitter twitter = new TwitterFactory().getInstance();
			twitter.setOAuthConsumer(consKey, consSecret);
			try {
				// get request token.
				// this will throw IllegalStateException if access token is already available
				RequestToken requestToken = twitter.getOAuthRequestToken();
				System.out.println("Got request token.");
				System.out.println("Request token: " + requestToken.getToken());
				System.out.println("Request token secret: " + requestToken.getTokenSecret());
				AccessToken accessToken = null;

				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				while (null == accessToken) {
					System.out.println("Open the following URL and grant access to your account:");
					System.out.println(requestToken.getAuthorizationURL());
					System.out.print("Enter the PIN(if available) and hit enter after you granted access.[PIN]:");
					String pin = br.readLine();
					try {
						if (pin.length() > 0) {
							accessToken = twitter.getOAuthAccessToken(requestToken, pin);
						} else {
							accessToken = twitter.getOAuthAccessToken(requestToken);
						}
					} catch (TwitterException te) {
						if (401 == te.getStatusCode()) {
							System.out.println("Unable to get the access token.");
						} else {
							te.printStackTrace();
						}
					}
				}
				System.out.println("Got access token.");
				System.out.println("Access token: " + accessToken.getToken());
				System.out.println("Access token secret: " + accessToken.getTokenSecret());
			} catch (IllegalStateException ie) {
				// access token is already available, or consumer key/secret is not set.
				if (!twitter.getAuthorization().isEnabled()) {
					System.out.println("OAuth consumer key/secret is not set.");
					System.exit(-1);
				}
			}
			Status status = twitter.updateStatus(tweet);
			System.out.println("Successfully updated the status to [" + status.getText() + "].");
			System.exit(0);
		} catch (TwitterException te) {
			te.printStackTrace();
			System.out.println("Failed to get timeline: " + te.getMessage());
			System.exit(-1);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.out.println("Failed to read the system input.");
			System.exit(-1);
		}
	}
}
