/**
 * 
 */
package org.gnf.genewiki.stream;


import org.gnf.wikiapi.Page;

import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.gnf.genewiki.GeneWikiPage;
import org.gnf.genewiki.GeneWikiUtils;
import org.gnf.genewiki.WikiCategoryReader;
import org.gnf.genewiki.metrics.RevisionCounter;
import org.gnf.util.DateFun;
import org.gnf.util.FileFun;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Provide command line access to tools for measuring Wikipedia content.
 * @author bgood
 *
 */
public class StreamAccess {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int optind=0;
		String title_file = null;
		String output_dir = null;	
		String index_file = null;
		int daysback = 1;
		String credential_file = null;
		boolean test = false;
		int interval = 0;
		int max_times = -1;
		int seconds2goback = 120;
		
		while(optind<args.length)
		{
			if(args[optind].equals("-h")||args.length==0)
			{
				printHelp();
				return;
			}
			else if (args[optind].equals("-o"))
			{
				output_dir = args[++optind];
				if(!output_dir.endsWith("/")){
					output_dir+="/";
				}
			}
			else if (args[optind].equals("-index"))
			{
				index_file = args[++optind];				
			}
			else if (args[optind].equals("-i"))
			{
				interval = Integer.parseInt(args[++optind]);				
			}
			else if (args[optind].equalsIgnoreCase("-test"))
			{
				test = true;
			}
			else if (args[optind].equalsIgnoreCase("-d"))
			{
				daysback = Integer.parseInt(args[++optind]);
			}
			else if (args[optind].equalsIgnoreCase("-c"))
			{
				credential_file = args[++optind];
			}
			else
			{
				break;
			}
			++optind;
		}
		if(credential_file==null||interval==0){
			System.err.println("Need to specify a credential file and a gene index file and an update interval.");
			System.exit(-1);
		}
		Map<String, String> creds = GeneWikiUtils.read2columnMap(credential_file);
		//start watchlist managed watching task
		RevisionStream.startWatchlistTweetertask(interval, max_times, seconds2goback, creds);
		
		//to start the old watching and tweeting process	
//		Map<String, String> gene_wiki = GeneWikiUtils.read2columnMap(index_file);
//		List<String> titles = new ArrayList<String>();
//		titles.addAll(gene_wiki.values());
//		Collections.sort(titles);
//		RevisionStream.startWikiWatcher(interval, max_times, seconds2goback, titles, index_file, creds);
		
		//for a daily type rss
		//RevisionStream.dailyDose(index_file, output_dir+"gwrss.xml", output_dir+"gwtweetsrss.xml", daysback, test, new RevisionCounter(wp_user, wp_pw));

		
		System.out.println("All done, bye bye!");
	}

	public static void printHelp(){
		System.err.println("Provided by Benjamin Good. bgood@scripps.edu");

	}
	
}
