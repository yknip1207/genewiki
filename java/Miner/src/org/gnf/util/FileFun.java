/**
 * 
 */
package org.gnf.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
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
}
