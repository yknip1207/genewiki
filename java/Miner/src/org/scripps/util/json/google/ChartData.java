/**
 * 
 */
package org.scripps.util.json.google;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.genewiki.GWRevision;
import org.genewiki.GeneWikiPage;
import org.genewiki.GeneWikiUtils;
import org.genewiki.metrics.PageViewCounter;
import org.genewiki.metrics.RevisionCounter;
import org.genewiki.stream.Tweetables;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;

/**
 * Hold, format, and write data for a Google Chart
 * @author bgood
 *
 */
public class ChartData {
	List<Col> cols;
	List<MotionChartRow> rows;
	
	/**
	 * Builds up a data structure suitable for sending to Google Charts
	 * Buyer beware, currently no checking is done to see if the types of the values in the cells in rows actually agree with the types for column headers here.  
	 * @param header_types
	 */
	public ChartData(LinkedHashMap<String, String> header_types) {
		//needs to have at least one data value
		if(header_types==null||header_types.size()<1){
			System.err.println("no header submitted to build google data");
			return;
		}
		//first needs to be a string and needs to be an entity identifier
		cols = new ArrayList<Col>(header_types.size());
		int i = 0;
		for(Entry<String, String> header_type : header_types.entrySet()){
			System.out.println(header_type.getKey());
			if(i==0&&(!header_type.getValue().equalsIgnoreCase("string"))){
				System.out.println("warning: first header must usually be a string indicating entity names");
			}
			if(i==1&&(!header_type.getValue().equalsIgnoreCase("date"))){
				System.out.println("warning: for motion charts, the second column must contain dates");
			}
			Col col = new Col();
			col.setId(header_type.getKey());
			col.setLabel(header_type.getKey());
			col.setType(header_type.getValue());
			cols.add(col);
			i++;
		}
		rows = new ArrayList<MotionChartRow>();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String json_out = "/Users/bgood/workspace/genewiki/genewikitools/static/mchart1000.json";
		demo(json_out, 1000, 10, Calendar.YEAR);
	}
	
	public static void demo(String json_out, int limit, int n_time_to_go_back, int time_units){
				
		LinkedHashMap<String, String> header_types = new LinkedHashMap<String, String>();
		header_types.put("Gene", "string");
		header_types.put("Date", "date");
		header_types.put("Cumulative_Edits", "number");
		header_types.put("Article_Size", "number");
		header_types.put("Page_Views", "number");
		header_types.put("Cumulative_Editors", "number");

		ChartData mc = new ChartData(header_types);
		boolean getContent = false;
		RevisionCounter rc = new RevisionCounter("i9606","2manypasswords");

		List<String> titles = new ArrayList<String>();
//		Map<String, String> genes = GeneWikiUtils.read2columnMap("/Users/bgood/data/wikiportal/stream/test_genes.txt");
//		List<String> ncbis = new ArrayList<String>(genes.keySet());
//		titles = GeneWikiUtils.getTitlesfromNcbiGenes(ncbis);

		Map<String, String> gene_wiki = GeneWikiUtils.read2columnMap("./gw_data/gene_wiki_index.txt");
		titles.addAll(gene_wiki.values());
		Collections.shuffle(titles);

		int ngenes = 0;
		for(String title : titles){
			ngenes++;
			if(ngenes>limit){
				break;
			}
			title = title.replaceAll(" ", "_");
			int cumulative_views =0; int cumulative_edits = 0; int cumulative_editors = 0; int size = 0;
			for(int i=0; i<n_time_to_go_back; i++){
				Calendar latest = Calendar.getInstance();
				latest.add(time_units, i-n_time_to_go_back+1);
				Calendar earliest = Calendar.getInstance();
				earliest.add(time_units, i-n_time_to_go_back);
				List<GWRevision> revs = rc.getRevisions(latest, earliest, title, getContent, null);

				int total_edits = revs.size();
				int total_bytes_added = 0;
				int total_editors = 0; 
				if(revs.size()>0 && revs.get(0).getSize()>0){
					size = revs.get(0).getSize();
					total_bytes_added = revs.get(0).getSize()- revs.get(revs.size()-1).getSize();
					Set<String> editors = new HashSet<String>();
					for(GWRevision r : revs){
						editors.add(r.getUser());
					}
					total_editors = editors.size();
					cumulative_edits+=total_edits;
					cumulative_editors+=total_editors;
				}
				//page views
				DescriptiveStatistics views = PageViewCounter.getPageViewSummaryForDateRange(title, earliest, latest);
				cumulative_views += (int)views.getSum();
				//build the vis object
				List vals = new ArrayList();
				vals.add(cumulative_edits); vals.add(size); vals.add(cumulative_views); vals.add(cumulative_editors);
				MotionChartRow mr = new MotionChartRow(title, latest, vals);
				System.out.println(mr.toString());
				mc.rows.add(mr);

			}
		}
		System.out.println(mc.toJSONString());
		try {
			FileWriter f = new FileWriter(json_out);
			f.write(mc.toJSONString());
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public String toJSONString(){
		String j = "{\"cols\":["+"\n";
		for(Col c : cols){
			if(c!=null){
				j+=c.getAsJSON()+",";
			}
		}
		j = j.substring(0,j.length()-1);
		j+="], \"rows\": ["+"\n";
		for(MotionChartRow r : rows){
			j+=r.getAsJSON()+",";
		}
		j = j.substring(0,j.length()-1);
		j+="]}";
		return j;
	}

}
