/**
 * 
 */
package org.gnf.search;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.gnf.util.HttpUtil;

/**
 * Manage interactions with google's search engine
 * 
 * @author bgood
 *
 */
public class GoogleSearch {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			List<String> urls = getTopHits("Cdk2");
			int rank = 1;
			for(String url : urls){
				System.out.println(rank+"\t"+url);
				rank++;
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	
	/**
	 * Executes a Google search using a GET request on their main web server.  Returns a list of URLs from the first page of results.
	 * List is sorted so that the first element of the list is the top hit.  List removes links to Google cached pages and other Google searches
	 * Note that this does not use any official API..  Not sure how they feel about this, but Google Custom Search isn't really what we need here.
	 * @param searchtext
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public static List<String> getTopHits(String query) throws UnsupportedEncodingException{
		String results = getRawSearchResult(query);
		String url = null;
		List<String> urls = new ArrayList<String>();
		String reg = 
			"https?:\\/\\/([0-9a-zA-Z][-\\w]*[0-9a-zA-Z]\\.)+" +
			"([a-zA-Z]{2,9})(:\\d{1,4})?([-\\w\\/#~:.?+=&%@~]*)/.+?(\\||\")"; // (\\||\\s)
		//"http:[^<]*\\|";
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(results);
		while (matcher.find()) {
			url = matcher.group();
			url = url.substring(0,url.length()-1);
			if(!url.contains("google.com")&&!url.contains("googleusercontent.com")){
				urls.add(url);		
			}
		}
		return urls;
	}
	
	
	/**
	 * Executes a Google search using a GET request on their main web server. 
	 * @param query
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String getRawSearchResult(String query) throws UnsupportedEncodingException{
		String url = "http://www.google.com/search?q=";
		String out = "";
		String encoded = URLEncoder.encode(query, "UTF8");
		GetMethod get = new GetMethod(url+encoded);
		// Get HTTP client
		HttpClient httpclient = new HttpClient();
		httpclient.getParams().setParameter(
			    HttpMethodParams.USER_AGENT,
			    "Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en-US; rv:1.8.1) Gecko/20061010 Firefox/2.0"
			);
		// Execute request
		try {
			int result = httpclient.executeMethod(get);
			// Display status code
			if(result>399){
				System.out.println("Response status code: " + result);
			}
			InputStream s = get.getResponseBodyAsStream(); 
			out = HttpUtil.convertStreamToString(s);
			//System.out.println(out);

		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// Release current connection to the connection pool once you are done
			get.releaseConnection();
		}
		return out;
	}
}
