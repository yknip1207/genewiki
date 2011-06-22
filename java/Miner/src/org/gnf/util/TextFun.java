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
}
