package org.gnf.util;

public class TextFun {

	
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
