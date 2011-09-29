/**
 * 
 */
package org.scripps.datasources;

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
public class Uniprot {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static Set<String> loadReviewedhuman(String file){
		Set<String> accs = new HashSet<String>();
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(file));
			String line = f.readLine();
			line = f.readLine();
			while(line!=null){
				accs.add(line.trim());
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
		return accs;
	}

}
