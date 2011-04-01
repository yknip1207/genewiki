/**
 * 
 */
package org.gnf.genewiki.metrics;

import info.bliki.api.Page;

import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.gnf.genewiki.GeneWikiPage;
import org.gnf.genewiki.WikiCategoryReader;
import org.gnf.util.DateFun;
import org.gnf.util.FileFun;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Provide command line access to tools for measuring Wikipedia content.
 * @author bgood
 *
 */
public class MetricsAccess {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		int optind=0;
		Set<String> titles = new HashSet<String>();
		String title_file = null;
		String output_dir = null;	
		String t0_in = null;
		String t1_in = null;
		int limit = 100000;
		String wp_user = "";
		String wp_pw = "";
		String template_name = null;
		boolean revisions = false; boolean pageviews = false; boolean volume = false;
		
		while(optind<args.length)
		{
			if(args[optind].equals("-h")||args.length==0)
			{
				printHelp();
				return;
			}
			else if (args[optind].equals("-t"))
			{
				template_name = args[++optind];
			}
			else if (args[optind].equals("-u"))
			{
				wp_user = args[++optind];
			}
			else if (args[optind].equals("-p"))
			{
				wp_pw = args[++optind];
			}
			else if (args[optind].equals("-f"))
			{
				title_file = args[++optind];
			}
			else if (args[optind].equalsIgnoreCase("-L"))
			{
				limit= Integer.parseInt(args[++optind]);
			}
			else if (args[optind].equals("-o"))
			{
				output_dir = args[++optind];
				if(!output_dir.endsWith("/")){
					output_dir+="/";
				}
			}	
			else if (args[optind].equals("-pv"))
			{
				pageviews = true;
			}
			else if (args[optind].equals("-vl"))
			{
				volume = true;
			}
			else if (args[optind].equals("-rv"))
			{
				revisions = true;
			}
			else if (args[optind].equalsIgnoreCase("-t0"))
			{
				t0_in = args[++optind];
			}
			else if (args[optind].equalsIgnoreCase("-t1"))
			{
				t1_in = args[++optind];
			}
			else
			{
				break;
			}
			++optind;
		}
		//must have titles to do anything
		if(title_file==null&&template_name==null){
			System.out.println("You must specify a title file as in -f \"./yourwikidir/your_wiki_page_titles.txt\" or the name of a template like -t Template:GNF_Protein_box");
			System.out.println("a specific titles file overrides a template");
			return;
		}else if(title_file==null&&template_name!=null){
			WikiCategoryReader r = new WikiCategoryReader();
			int batch = 500;
			if(limit<batch){
				batch = limit;
			}
			List<Page> pages = r.getPagesWithTemplate(template_name, limit, batch);
			for(Page page : pages){
				titles.add(page.getTitle());
			}
			System.out.println("\n--- Done getting "+titles.size()+" template matching pages --- "+(System.currentTimeMillis()-start)/1000+" seconds.");
		}else{
			titles = FileFun.readOneColFile(title_file, limit);
		}
		
		Calendar latest = Calendar.getInstance();
		Calendar earliest = Calendar.getInstance();
		if(t0_in!=null&&t1_in!=null){
			try {
				earliest.setTime(DateFun.yearMonthDay.parse(t0_in));
				latest.setTime(DateFun.yearMonthDay.parse(t1_in));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{ //default to past year
			earliest.add(Calendar.MONTH, -12);
		}

		//do the page views
		if(pageviews){
			System.out.println("Attempting to collect page views");
			System.out.println("Requesting page view info from http://stats.grok.se/json/en/ for "+titles.size()+" articles in date range "+DateFun.year_month_day.format(earliest.getTime()) +" : "+ DateFun.year_month_day.format(latest.getTime()));
			Map<String, DescriptiveStatistics> page_views = PageViewCounter.getPageViewSummaryForDateRange(titles, earliest, latest);
			System.out.println("Finished getting page views, now writing files to "+output_dir);
			JSONObject vreport = PageViewCounter.generatePageSetViewReportJSON(page_views, earliest, latest);			
			JSONArray ja = new JSONArray();
			ja.put(vreport); 
			FileWriter f;
			try {
				//write summary for admin website
				f = new FileWriter(output_dir+"views.json");
				f.write(ja.toString());
				f.close();
				//write table for spreadsheet
				f = new FileWriter(output_dir+"all_views.txt");
				String range = DateFun.yearMonthDay.format(earliest.getTime()) +" : "+ DateFun.yearMonthDay.format(latest.getTime());
				f.write(range+"\ttitle\ttotal page views\tmean /day\tmedian/day\tmax/day\n");
				for(Entry<String, DescriptiveStatistics> page_view : page_views.entrySet()){
					DescriptiveStatistics stats = page_view.getValue();
					f.write("\t"+page_view.getKey()+"\t"+stats.getSum()+"\t"+stats.getMean()+"\t"+stats.getPercentile(50)+"\t"+stats.getMax()+"\n");
				}
				f.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("\n--- Page Views done ---"+(System.currentTimeMillis()-start)/1000+" seconds.");
		}
		//do the revisions
		if(revisions){
			System.out.println("Getting revision data from wikipedia for "+titles.size()+" articles in date range "+DateFun.year_month_day.format(earliest.getTime()) +" : "+ DateFun.year_month_day.format(latest.getTime()));
			if(wp_user.equals("")||wp_pw.equals("")){
				System.out.println("Accessing Wikipedia anonymously - you might want to enter your user name and password (e.g. -u youruser -p yourpassword");
			}
			RevisionCounter rc = new RevisionCounter(wp_user, wp_pw);
			RevisionsReport report = rc.generateBatchReport(titles, latest, earliest, output_dir+"edit_report");
			JSONArray ja = new JSONArray();
			ja.put(report.getSummaryJSON()); 
			FileWriter f;
			try {
				f = new FileWriter(output_dir+"edits.json");
				f.write(ja.toString());
				f.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("\n--- Revisions done ---"+(System.currentTimeMillis()-start)/1000+" seconds.");
		}
		//do the volume
		if(volume){
			System.out.println("Getting volume data from wikipedia for "+titles.size()+" articles in date range "+DateFun.year_month_day.format(earliest.getTime()) +" : "+ DateFun.year_month_day.format(latest.getTime()));
			System.out.println("What you are going to see is the current snapshot");
			if(wp_user.equals("")||wp_pw.equals("")){
				System.out.println("Accessing Wikipedia anonymously - you might want to enter your user name and password (e.g. -u youruser -p yourpassword");
			}
			List<GeneWikiPage> pages = new ArrayList<GeneWikiPage>();
			int n = 0;
			for(String title : titles){
				n++;
				GeneWikiPage page = new GeneWikiPage(wp_user, wp_pw);
				page.setTitle(title);
				System.out.println("processing "+title+" - n = "+n+" of total "+titles.size());
				boolean gotext = page.defaultPopulate();
				if(gotext){
					page.parseAndSetNcbiGeneId();
					page.retrieveAllInBoundWikiLinks(true, false);
				}
				pages.add(page);
			}
			VolumeReport r = VolumeCounter.summarizeArticleVolumes(VolumeCounter.getArticleVolumes(pages), output_dir+"volume");
			r.setTimestamp(DateFun.year_month_day.format(Calendar.getInstance().getTime()));
			JSONArray a = new JSONArray();
			a.put(r.toJSON());		
			FileWriter f;
			try {
				f = new FileWriter(output_dir+"volume.json");
				f.write(a.toString());
				f.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("\n--- Volume done ---"+(System.currentTimeMillis()-start)/1000+" seconds.");
		}
		
		FileWriter f;
		try {
			f = new FileWriter(output_dir+"ReportConfig.txt");
			f.write("Report finished generating at: "+DateFun.wp_format.format(Calendar.getInstance().getTime())+"\n");
			for(String arg : args){
				f.write(arg+" ");
			}
			f.write("\n");
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("All done, bye bye!");
	}

	public static void printHelp(){
		System.err.println("Provided by Benjamin Good. bgood@gnf.org");
		System.err.println("-h this screen");
		System.err.println("-f <file> file containing list of Wikipedia article titles");
		System.err.println("-t <String> template embedded in page collection like Template:GNF_Protein_box - if title file specified, it overrides this");
		System.err.println("-L <integer> limit number of article titles processed");
		System.err.println("-t0 <timestamp> the earliest date to consider as yearmonthday like 20091201");
		System.err.println("-t1 <timestamp> the latest date to consider as yearmonthday like 20101231");
		System.err.println("-o <directory> place to put all the data - will overwrite files..");
		System.err.println("-u <String> wikipedia user name");
		System.err.println("-p <String> wikipedia password");
	}
	
}
