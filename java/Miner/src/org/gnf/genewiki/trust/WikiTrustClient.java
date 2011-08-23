/**
 * 
 */
package org.gnf.genewiki.trust;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.gnf.genewiki.Reference;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author bgood
 *
 */
public class WikiTrustClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
//14118313&revid=381448016
		String o = getTrustForWikiPage("14118313", "381448016", "rawquality", 0); //"quality" "gettext" "rawquality"
		Map<String, Object> v = parseRawTrust(o);
		System.out.println("editor score "+v.get("Reputation"));
		//System.out.println(o.substring(0, o.indexOf(",")));
		System.out.println(o);
		//List<WikiTrustBlock> blocks = getTrustBlocks(o);
		//System.out.println(blocks.size());
		//System.out.println(summarizeEditorTrust(blocks));
		//{{#t:3,173948108,Boghog2}}bla bla bla bla  {{#t:3,169980927,ProteinBoxBot}}
		//means
		//{{trust is 3, revision id was 173948108, editor for this revision was BogHog2}} for the following text 'bla bla bla bla ' {{starting next one..
		
	}

//TODO parse raw trust information from UCSC (just one object with many name-value pairs)
	public static Map<String, Object> parseRawTrust(String trustjson){
		Map<String, Object> key_val = new TreeMap<String, Object>();
		try {
			JSONObject r = new JSONObject(trustjson);
			if(r==null){
				return null;
			}else{
				Iterator i = r.keys();
				while(i.hasNext()){
					String k = (String) i.next();
					System.out.println(k);
					key_val.put(k, r.get(k));
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return key_val;
	}
	
	public static String zapTrust(String annotatedtext){
		String text = annotatedtext.replaceAll("\\{\\{\\#.+?}}", "");
		return text;
	}
	
	public static String summarizeEditorTrust(List<WikiTrustBlock> blocks){
		String trusty = "";
		//trusts for all edits
		Map<String, List<Double>> user_trusts = new HashMap<String, List<Double>>();
		//number revisions saved
		Map<String, Set<Integer>> user_revs = new HashMap<String, Set<Integer>>();
		//amount of text touched
		Map<String, Integer> user_texts = new HashMap<String, Integer>();
		for(WikiTrustBlock tb : blocks){
			List<Double> trusts = user_trusts.get(tb.getEditor());
			if(trusts==null){
				trusts = new ArrayList<Double>();
			}
			trusts.add(tb.getTrust());
			user_trusts.put(tb.getEditor(), trusts);
			Set<Integer> edits = user_revs.get(tb.getEditor());
			if(edits == null){
				edits = new HashSet<Integer>();
			}
			edits.add(tb.getRevid());
			user_revs.put(tb.getEditor(), edits);
			Integer textamount = user_texts.get(tb.getEditor());
			if(textamount == null){
				textamount = 0;
			}
			textamount += tb.getText().length();
			user_texts.put(tb.getEditor(), textamount);
		}
		Map<String, Double> user_trust = new HashMap<String, Double>();
		trusty+="User\tMean trust for page\n";
		for(Entry<String, List<Double>> ut : user_trusts.entrySet()){
			double m = 0;
			for(Double t : ut.getValue()){
				m+=t;
			}
			m = m/(double)ut.getValue().size();
			user_trust.put(ut.getKey(), m);
			trusty+=ut.getKey()+"\t"+m+"\n";
		}
		trusty+="\nUser\tRevisions\n";
		for(Entry<String, Set<Integer>> rev : user_revs.entrySet()){
			trusty+=rev.getKey()+"\t"+rev.getValue().size()+"\n";
		}
		trusty+="\nUser\tLength of text touched\n";
		for(Entry<String, Integer> text : user_texts.entrySet()){
			trusty+=text.getKey()+"\t"+text.getValue()+"\n";
		}
		return trusty;
	}
	
	public static List<WikiTrustBlock> getTrustBlocks(String trusttext){
		List<WikiTrustBlock> blocks = new ArrayList<WikiTrustBlock>();
		Pattern pattern = 
			Pattern.compile("\\{\\{\\#.+?}}");
		Matcher matcher = 
			pattern.matcher(trusttext);

		int laststop = 0; WikiTrustBlock lastblock = null;
		while (matcher.find()) {
			WikiTrustBlock block = new WikiTrustBlock();
			String[] t = matcher.group().split(",");
			block.setTrust(Double.parseDouble(t[0].substring(5)));
			block.setRevid(Integer.parseInt(t[1]));
			block.setEditor(t[2].substring(0,t[2].length()-2));
			if(laststop != 0){
				lastblock.setText(trusttext.substring(laststop,matcher.start()));
				lastblock.setStart(laststop);
				lastblock.setStop(matcher.start());
				blocks.add(lastblock);
			}
			lastblock = block;
			laststop = matcher.end(); 
		}	
		return blocks;
	}
	
	public static double getMaxPossibleTrustForWiki(String trusttext){
		return Double.parseDouble(trusttext.substring(0, trusttext.indexOf(",")));
	}
	
	public static String getTrustForWikiPage(String pageid, String revid, String method, int req_num){
		int max_requests = 1;
		req_num++;
		String out = "";
		if(!(method.equals("gettext")||method.equals("rawquality")||method.equals("quality"))){
			System.out.println("Please enter one of: gettext, rawquality, quality");
			return null;
		}
		PostMethod post = new PostMethod("http://en.collaborativetrust.com/WikiTrust/RemoteAPI");
		//method=gettext&pageid=15580374&revid=365059815
	
		NameValuePair[] data = {
				new NameValuePair("method", method), 
				new NameValuePair("pageid", pageid),
				new NameValuePair("revid", revid)
		};
	//	System.out.println("http://en.collaborativetrust.com/WikiTrust/RemoteAPI?method=gettext&pageid="+pageid+"&revid="+revid);
		post.setRequestBody(data);
		// Get HTTP client
		HttpClient httpclient = new HttpClient();
		// Execute request
		try {
			int statusCode = httpclient.executeMethod(post);
			// Display status code
			//System.out.println("Response status code: " + statusCode);
			if( statusCode == 200 ) {
				InputStream s = post.getResponseBodyAsStream();
				out = convertStreamToString(s);
				post.releaseConnection();
			}
			if(statusCode<0||out.startsWith("TEXT_NOT_FOUND")){
				if(req_num<max_requests){
					System.out.println("bad response for pageid revid "+pageid+" "+revid+ " req_num "+req_num+" status code "+statusCode+" response "+out);
					//request failed, try again in 4 seconds up to 10 times.
					Thread.currentThread().sleep(3000);				
					return getTrustForWikiPage(pageid, revid, method, req_num);
				}else{
					System.out.println("Quitting: WikiTrust service would not answer request for pageid revid "+pageid+" "+revid);
					return null;
					//System.exit(-1);
				}
			}
			
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// Release current connection to the connection pool once you are done
			post.releaseConnection();
		}
		return out;
	}

	public static String convertStreamToString(InputStream is)    throws IOException {
		if (is != null) {
			Writer writer = new StringWriter();
			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(	new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {        
			return "";
		}
	}
}
