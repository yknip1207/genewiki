package org.gnf.pbb.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ListUtils {

	public static String toString(List<String> list, String delimiter) {
		StringBuilder sb = new StringBuilder();
		if (list == null) 
			return "";
		for (String str : list) {
			sb.append(str);
			if (list.size() > 1)
				sb.append(delimiter);
		}
		if (list.size() > 1) 
			sb.deleteCharAt(sb.length()-1);
		
		return sb.toString();
	}
	
	public static String toString(List<String> list) {
		return toString(list, ", ");
	}

	public static List<String> parseString(String strList) {
		List<String> out = new ArrayList<String>();
		String[] contents = strList.split(",");
		for (String str : contents) {
			out.add(str.trim());
		}
		return out;
	}
	
}
