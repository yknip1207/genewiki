/**
 * 
 */
package org.gnf.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.text.html.HTMLDocument.Iterator;

import org.gnf.go.GOterm;

/**
 * @author bgood
 *
 */
public class MapFun {

	public static void main(String[] args) {
		Map m = new HashMap();
		m.put("a", "some");
		m.put("b", "random");
		m.put("c", "words");
		m.put("d", "to");
		m.put("e", "be");
		m.put("f", "sorted");
		m.put("g", "by");
		m.put("h", "value");
		for (Object key : sortMapByValue(m)) {
			System.out.printf("key: %s, value: %s\n", key, m.get(key));
		}
	}

	/**
	 * return the keys as a list sorted according to the associated value
	 * @param m
	 * @return
	 */
	public static List sortMapByValue(final Map m) {
		List keys = new ArrayList();
		keys.addAll(m.keySet());
		Collections.sort(keys, new Comparator() {
			public int compare(Object o1, Object o2) {
				Object v1 = m.get(o1);
				Object v2 = m.get(o2);
				if (v1 == null) {
					return (v2 == null) ? 0 : 1;
				}
				else if (v1 instanceof Comparable) {
					return ((Comparable) v1).compareTo(v2);
				}
				else {
					return 0;
				}
			}
		});
		return keys;
	}

	public static Map<String, Set<String>> flipMapStringGOs(Map<String, Set<GOterm>> inmap){
		Map<String, Set<String>> outmap = new HashMap<String, Set<String>>();
		for(Entry<String, Set<GOterm>> in : inmap.entrySet()){
			if(in.getKey().equals("55669")){
				System.out.println("flip 55669 size "+in.getValue().size());
			}
			for(GOterm f : in.getValue()){
				Set<String> fvals = outmap.get(f.getAccession());
				if(fvals==null){
					fvals = new HashSet<String>();
				}
				fvals.add(in.getKey());
				outmap.put(f.getAccession(), fvals);
			}
		}
		return outmap;
	}

	public static Map<String, Set<String>> mergeStringSetMaps(Map<String, Set<String>> map1, Map<String, Set<String>> map2){
		for(Entry<String, Set<String>> in : map1.entrySet()){
			if(map2.keySet().contains(in.getKey())){
				map2.get(in).addAll(map1.get(in.getKey()));
			}else{
				map2.put(in.getKey(), in.getValue());
			}
		}
		return map2;
	}

	public static Map<String, Set<String>> flipMapStringSetStrings(Map<String, Set<String>> inmap){
		Map<String, Set<String>> outmap = new HashMap<String, Set<String>>();
		for(Entry<String, Set<String>> in : inmap.entrySet()){
			if(in!=null&&in.getValue()!=null){
				for(String f : in.getValue()){
					Set<String> fvals = outmap.get(f);
					if(fvals==null){
						fvals = new HashSet<String>();
					}
					fvals.add(in.getKey());
					outmap.put(f, fvals);
				}
			}
		}
		return outmap;
	}

	public static Map<String, Set<String>> flipMapStringListStrings(Map<String, List<String>> inmap){
		Map<String, Set<String>> outmap = new HashMap<String, Set<String>>();
		for(Entry<String, List<String>> in : inmap.entrySet()){
			for(String f : in.getValue()){
				Set<String> fvals = outmap.get(f);
				if(fvals==null){
					fvals = new HashSet<String>();
				}
				fvals.add(in.getKey());
				outmap.put(f, fvals);
			}
		}
		return outmap;
	}

	public static Map<String, String> flipMapStringString(Map<String, String> inmap){
		Map<String, String> outmap = new HashMap<String, String>();
		for(Entry<String, String> in : inmap.entrySet()){
			outmap.put(in.getValue(), in.getKey());
		}
		return outmap;
	}
}
