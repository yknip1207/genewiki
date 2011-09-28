/**
 * 
 */
package org.gnf.genewiki.metrics;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.gnf.genewiki.Config;
import org.gnf.genewiki.GeneWikiPage;
import org.gnf.genewiki.GeneWikiUtils;
import org.gnf.go.GOterm;
import org.gnf.util.DateFun;
import org.gnf.util.FileFun;
import org.gnf.util.StatFun;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * @author bgood
 *
 */
public class VolumeCounter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GeneWikiPage p = 
			GeneWikiUtils.deserializeGeneWikiPage("/Users/bgood/data/bioinfo/gene_wikitrust_as_java/6422");
		VolumeReport v = getArticleVolume(p);
		System.out.println(VolumeReport.getHeader());
		System.out.println(v.toString());
		//	processGeneWiki(1000000, Config.gwikidir, "/Users/bgood/data/wikiportal/jan11");
		//	System.out.println(Volumetrics.getHeader());
		//	System.out.println(summary);
		//	GeneWikiPage page = GeneWikiUtils.deserializeGeneWikiPage(Config.gwikidir+"/5649");
		//	System.out.println(getArticleVolume(page));		
		
		String outdir = "/Users/bgood/data/wikiportal/facebase_feb11";
		String article_name_file = "/users/bgood/data/wikiportal/facebase_genes.txt";
		Set<String> titles = FileFun.readOneColFile(article_name_file);
		List<GeneWikiPage> pages = new ArrayList<GeneWikiPage>();
		for(String title : titles){
			GeneWikiPage page = new GeneWikiPage();
			page.setTitle(title);
			System.out.println("processing "+title);
			boolean gotext = page.defaultPopulate();
			if(gotext){
				page.retrieveAllInBoundWikiLinks(true, false);
			}
			pages.add(page);
		}
		VolumeReport r = summarizeArticleVolumes(getArticleVolumes(pages), outdir+"_volume");
		r.setTimestamp(DateFun.year_month_day().format(Calendar.getInstance().getTime()));
		JSONArray a = new JSONArray();
		a.put(r.toJSON());
		System.out.println(a.toString());
	}
	
	
	public static void processGeneWiki (int limit, String obj_dir, String outdir){
	
	//	List<GeneWikiPage> pages = GeneWikiUtils.loadSerializedDir("/Users/bgood/data/genewiki_dec_2010/intermediate/javaobj/", limit);
	//	summarizeArticleVolumes(getArticleVolumes(pages), Config.gwroot+"/tmp/WikiVolumeSnapshot-dec-2010");
		
		Map<String, GeneWikiPage> pages = GeneWikiUtils.loadSerializedDir(obj_dir, limit);
		summarizeArticleVolumes(getArticleVolumes(pages.values()), outdir+"_volume");
	
//		Set<String> titles = GeneWikiUtils.getGeneNames(pages);
//		String page_view_report = PageViewCounter.generatePageSetViewReportText(titles, outdir+"_page_view_full");
//		System.out.println(page_view_report);
	}
	
	
	
	public static VolumeReport summarizeArticleVolumes(List<VolumeReport> volumes, String file){
		VolumeReport sum_report = new VolumeReport();
		String data = "";
		Map<String, DescriptiveStatistics> summaries = new HashMap<String, DescriptiveStatistics>();
		summaries.put("bytes", new DescriptiveStatistics());
		summaries.put("words", new DescriptiveStatistics());
		summaries.put("links_out", new DescriptiveStatistics());
		summaries.put("links_in", new DescriptiveStatistics());
		summaries.put("external_links", new DescriptiveStatistics());
		summaries.put("pubmed references", new DescriptiveStatistics());
		summaries.put("redirects", new DescriptiveStatistics());
		summaries.put("sentences", new DescriptiveStatistics());
		summaries.put("media", new DescriptiveStatistics());
		
		for(VolumeReport v : volumes){
			summaries.get("bytes").addValue(v.getBytes());
			summaries.get("words").addValue(v.getWords());
			summaries.get("links_out").addValue(v.getLinks_out());
			summaries.get("links_in").addValue(v.getLinks_in());
			summaries.get("external_links").addValue(v.getExternal_links());
			summaries.get("pubmed references").addValue(v.getPubmed_refs());
			summaries.get("redirects").addValue(v.getRedirects());
			summaries.get("sentences").addValue(v.getSentences());
			summaries.get("media").addValue(v.getMedia_files());
			data += v.toString()+"\n";
		}
		
		sum_report.setBytes(summaries.get("bytes").getSum());
		sum_report.setWords(summaries.get("words").getSum());
		sum_report.setSentences(summaries.get("sentences").getSum());
		sum_report.setLinks_out(summaries.get("links_out").getSum());
		sum_report.setLinks_in(summaries.get("links_in").getSum());
		sum_report.setExternal_links(summaries.get("external_links").getSum());
		sum_report.setPubmed_refs(summaries.get("pubmed references").getSum());
		sum_report.setRedirects(summaries.get("redirects").getSum());
		sum_report.setMedia_files(summaries.get("media").getSum());
		sum_report.setN_articles(volumes.size());
		try {
			FileWriter f = new FileWriter(file+".txt");
			f.write(VolumeReport.getHeader()+"\n");
			f.write(data);
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			FileWriter f = new FileWriter(file+"_sum.txt");
			sum_report.toString();
			String sum_header = "field\t"+StatFun.getTabHeader();
			System.out.println(sum_header);
			f.write(sum_header+"\n");
			for(Entry<String, DescriptiveStatistics> e : summaries.entrySet()){
				String row = e.getKey()+"\t"+StatFun.statsToTabString(e.getValue());
				System.out.println(row);
				f.write(row+"\n");
			}
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sum_report;
	}
	
	public static List<VolumeReport> getArticleVolumes(Collection<GeneWikiPage> pages){
		List<VolumeReport> volumes = new ArrayList<VolumeReport>();
		for(GeneWikiPage page : pages){
			volumes.add(getArticleVolume(page));
		}
		return volumes;
	}
	
	public static VolumeReport getArticleVolume(GeneWikiPage page){
		VolumeReport volume = new VolumeReport();
		volume.extractVolumeFromPopulatedPage(page);
		return volume;
	}
	
}
