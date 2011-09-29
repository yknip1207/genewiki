package org.scripps.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class TextFun {

	public static void main(String[] args){
		System.out.println(removeNonalphanumeric("http://www.bla.com/Iam,t@*He-#1ben_"));
	}
	
	public static String removeNonalphanumeric(String s){
		s = s.replaceAll("[^A-Za-z0-9_]+"," ");
		return s;
	}
	
	public static String makeCleanLowercaseUri(String uri){
		String s = uri.substring(uri.lastIndexOf("/")+1);
		try {
			s = URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//common separators to undescores
		s = s.replace(" ", "_");
		s = s.replace("-", "_");
		s = s.replace(",", "_");
		s = s.replace(":", "_");
		
		//delete everything
		s = s.toLowerCase();
		s = s.replaceAll("([^a-z0-9_]+)", "");
		uri = uri.substring(0, uri.lastIndexOf("/")+1)+s;
		return uri;
	}
	
	public static String makeHashTagSafe(String s){
//		s = s.replace(" ", "");
//		s = s.replace(",", "");
//		s = s.replace("\'", "");
//		s = s.replace(".", "");
//		s = s.replace(":", "");
//		s = s.replace(";", "");
//		s = s.replace("-", "");
//		s = s.replace("\"", "");
		s = s.replaceAll("([^A-Za-z0-9_]+)", "");
		return s;
	}
	
	public static String ip2anon(String ip){
		return ip.replaceAll("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)", "anon");
	}
}
