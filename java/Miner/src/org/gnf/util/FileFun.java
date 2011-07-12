/**
 * 
 */
package org.gnf.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author bgood
 *
 */
public class FileFun {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static Map<String, Set<String>> loadMapFromTabD(String filename, int key_index, int val_index, boolean hasheader){
		Map<String, Set<String>> m = new HashMap<String, Set<String>>();
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(filename));
			String line = f.readLine();
			if(hasheader){
				line = f.readLine();
			}
			while(line!=null){
				String[] item = line.split("\t");
				int k = key_index;
				int v = val_index;			
				if(item!=null&&item.length>1){
					String key = item[k];
					String val = item[v];
					Set<String> vals = m.get(key);
					if(vals==null){
						vals = new HashSet<String>();
					}
					vals.add(val);
					m.put(key, vals);
				}
				line = f.readLine();
			}
			f.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return m;
	}
	
	public static Set<String> readOneColFile(String file){
		return readOneColFile(file, 1000000);
	}
	public static Set<String> readOneColFile(String file, int limit){
		Set<String> item = new HashSet<String>();
		try {
			int n = 0;
			BufferedReader f = new BufferedReader(new FileReader(file));
			String line = f.readLine().trim();
			while(line!=null&&n<limit){
				n++;
				item.add(line.trim());
				line = f.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return item;
	}
	public static Set<String> readOneColFromFile(String file, int colindex, int limit){
		Set<String> item = new HashSet<String>();
		try {
			int n = 0;
			BufferedReader f = new BufferedReader(new FileReader(file));
			String line = f.readLine().trim();
			while(line!=null&&n<limit){
				n++;
				int c = colindex;
				if(line.startsWith("\t")){
					c--;
				}
				String thing = line.trim().split("\t")[c];
				item.add(thing);
				line = f.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return item;
	}
}
