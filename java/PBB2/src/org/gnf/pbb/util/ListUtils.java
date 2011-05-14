package org.gnf.pbb.util;

import java.util.List;

public class ListUtils {

	public static String toString(List<String> list) {
		StringBuilder sb = new StringBuilder();
		if (list == null) 
			return "";
		for (String str : list) {
			sb.append(str);
			if (list.size() > 1)
				sb.append(", ");
		}
		if (list.size() > 1) 
			sb.deleteCharAt(sb.length()-1);
		
		return sb.toString();
	}
	
}
