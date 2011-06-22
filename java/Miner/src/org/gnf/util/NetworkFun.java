package org.gnf.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NetworkFun {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String input = "/Users/bgood/data/wikiportal/fb_denver/networks/fb_gw_network_all_filtered.txt";
		String output = "/Users/bgood/data/wikiportal/fb_denver/networks/fb_gw_network_all_filtered_table.txt";
		int limit = 10000;
		try {
			int n = 0;
			BufferedReader f = new BufferedReader(new FileReader(input));
			f.readLine();
			String line = "";
			line = f.readLine().trim();
			Set<String> rownamess = new HashSet<String>();
			Set<String> colnamess = new HashSet<String>();
			Set<String> kindss = new HashSet<String>();
			List<String> rownames = new ArrayList<String>();
			List<String> colnames = new ArrayList<String>();
			List<String> kinds = new ArrayList<String>();
			Map<String, String> vals = new HashMap<String, String>();
			Map<String, String> kindmap = new HashMap<String, String>();
			Map<String, String> colkindmap = new HashMap<String, String>();
			Map<String, String> colcolors = new HashMap<String, String>();
			colcolors.put("editor", "0,255,255");
			colcolors.put("GO", "0,0,255");
			colcolors.put("DO", "0,255,0");
			colcolors.put("link", "124,124,124");
			colcolors.put("gene", "0,64,124");
			while(line!=null&&n<limit){
				n++;
				String[] r = line.split("\t");
				if(r.length==4){
					rownamess.add(r[0]);
					String col = nameCol(r[3], r[2]);
				//	colnames.add(col);
					kindss.add(r[3]);
					vals.put(r[0]+col, r[1].replaceAll(" ", "_"));
					kindmap.put(r[0]+col, r[3]);
					colkindmap.put(col, r[3]);
				}
				line = f.readLine();
			}
			f.close();
			//convert to lists to keep order
			rownames.addAll(rownamess);
			kinds.addAll(kindss);			
			//sort the columns by kind
			List sorted_keys = MapFun.sortMapByValue(colkindmap);
			colnames.addAll(sorted_keys);
			
			FileWriter w = new FileWriter(output);
			w.write("data\tdata\tdata\t");
			int c = 40;
			Map<String, Integer> col_index = new HashMap<String, Integer>();
			for(String col : colnames){
				w.write(c+"\t");
				col_index.put(col, c);
				c++;

			}
			w.write("\ndata\tdata\tdata\t");
			for(String col : colnames){
				w.write(colcolors.get(colkindmap.get(col))+"\t");
			}
			w.write("\ndata\tdata\tdata\t");
			for(String col : colnames){
				w.write(col+"\t");
			}
			w.write("\n");
			c = 1;
			for(String row : rownames){
				int row_id = c;
//				//if row name appears in column list, make sure you keep the same index, otherwise just iterate
//				if(col_index.get(row)!=null){
//					row_id = col_index.get(row);
//				}else{
//					c++;
//					row_id = c;
//				}
				w.write(row_id+"\t"+(int)Math.rint(Math.random()*254)+","+(int)Math.rint(Math.random()*254)+","+(int)Math.rint(Math.random()*254)+"\t"+row+"\t");
				for(String col : colnames){
						if(vals.get(row+col)!=null){
							w.write(vals.get(row+col)+"\t");
						}else{
							w.write("-\t");
						}
				}
				w.write("\n");
				c++;
			}
			w.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static String nameCol(String pre, String col){
		if(pre.equals("GO")){
			col = "GO__"+col.toLowerCase();
		}else if(pre.equals("DO")){
			col = "DO__"+col.toLowerCase();
		}else if(pre.equals("editor")){
			col = "editor__"+col.toLowerCase();
		}else if(pre.equals("link")){
			col = "link__"+col.toLowerCase().replaceAll("#", "_").replaceAll(":", "_");
		}else if(pre.equals("gene")){
			col = "gene__"+col;
		}else{
			col = col.toLowerCase();
		}
		return col;
	}

}
