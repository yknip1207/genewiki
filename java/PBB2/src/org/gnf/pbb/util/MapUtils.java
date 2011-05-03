package org.gnf.pbb.util;

import java.util.LinkedHashMap;
import java.util.Set;

public class MapUtils {

	/**
	 * Outputs a map<string, string> in human-readable format.
	 * @param map
	 * @return string of key : value lines
	 */
	public static String toString(LinkedHashMap<String, String> map) {
		Set<String> keys = map.keySet();
		StringBuilder sb = new StringBuilder();
		for (Object key : keys) {
			sb.append(key+" : "+map.get(key)+"\n");
		}
		return sb.toString();
	}
	
	
}
