package org.gnf.genewiki.stream;
import java.util.Map;

import org.gnf.genewiki.GeneWikiUtils;

import com.rosaloves.bitlyj.*;
import static com.rosaloves.bitlyj.Bitly.*;

public class Shortener {

	static String user;
	static String key;
	
	public Shortener(Map<String, String> creds){
		initShortener(creds);
	}
	
	public static void initShortener(Map<String, String> creds){
		user = creds.get("bitlyUser");
		key = creds.get("bitlyKey");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String credfile = "/Users/bgood/workspace/Config/gw_creds.txt";
		Map<String, String> creds = GeneWikiUtils.read2columnMap(credfile);
		Shortener s = new Shortener(creds);
		Url url = as(user, key).call(shorten("http://rosaloves.com/stories/view/12"));
		System.out.println(url.getShortUrl());
	}

}
