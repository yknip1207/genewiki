/**
 * 
 */
package org.gnf.genewiki.metrics;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.gnf.util.DateFun;
import org.gnf.util.FileFun;
import org.gnf.util.HttpUtil;
import org.gnf.util.MapFun;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;

/**
 * @author bgood
 *
 */
public class PageViewCounter {

	/**

       {
       "title": "insulin",
       "month": "201009",
       "total_views": 124211,
       "daily_views": [
                0,3779,3850,3193,2372,2680,3427,3774,4562,4171,3481,2407,3238,4740,4848,5360,4810,4297,2898,3747,4891,5281,5120,5174,4160,2921,3777,5459,5593,5256,4945,0
                ]
       }
	 */

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//		DescriptiveStatistics days = getPageViewSummaryForDateRange("Cleft_lip_and_palate", 2010, 2, 2, 2011, 2, 2);
		//		System.out.println(days.toString());

		String article_name_file = "/users/bgood/data/wikiportal/facebase_genes.txt";
		Set<String> titles = FileFun.readOneColFile(article_name_file);
		Calendar latest = Calendar.getInstance();
		Calendar earliest = Calendar.getInstance();
		earliest.add(Calendar.MONTH, -12);
		JSONObject r1 = generatePageSetViewReportJSON(getPageViewSummaryForDateRange(titles, earliest, latest), earliest, latest);
//		latest.add(Calendar.MONTH, -1);
//		earliest.add(Calendar.MONTH, -2);
//		JSONObject r2 = generatePageSetViewReportJSON("FaceBase PageViews", titles, earliest, latest);
		JSONArray ja = new JSONArray();
		ja.put(r1); 
//		ja.put(r2);
		System.out.println(ja.toString());
	}

	public static JSONObject generatePageSetViewReportJSON(Map<String, DescriptiveStatistics> page_views, Calendar earliest, Calendar latest){
		//now start counting
		//average page views per day for all articles
		DescriptiveStatistics all_pvs = new DescriptiveStatistics();
		for(DescriptiveStatistics stats : page_views.values()){
			for(double v : stats.getValues()){
				all_pvs.addValue(v);
			}
		}
		PageViewsReport vr = new PageViewsReport();
		vr.setMax_day((int)all_pvs.getMax());
		vr.setMean_day((float)all_pvs.getMean());
		vr.setMedian_day((int)all_pvs.getPercentile(50));
		vr.setTotal_views((int)all_pvs.getSum());
		vr.setTimespan(DateFun.year_month_day.format(earliest.getTime()) +" : "+ DateFun.year_month_day.format(latest.getTime()));
		vr.setRank(0);

		return vr.getJSON();
	}
	

	public static String summarizeSetInRange(Set<String> titles, Calendar t1, Calendar t2, String outfile){
		String report = "";
		System.out.println("Requesting "+titles.size()+" pages for range: "+DateFun.wp_format.format(t1.getTime())+" "+DateFun.wp_format.format(t2.getTime()));
		Map<String, DescriptiveStatistics> page_views = getPageViewSummaryForDateRange(titles, t1, t2);
		//now start counting
		//average page views per month for all articles
		DescriptiveStatistics all_pvs = new DescriptiveStatistics();
		for(DescriptiveStatistics stats : page_views.values()){
			for(double v : stats.getValues()){
				all_pvs.addValue(v);
			}
		}
		report+="\ntotal page views: "+all_pvs.getSum()+"\tavg per day: "+all_pvs.getMean()+"\tmax per day: "+all_pvs.getMax()+"\n\n";
		//articles ranked by total number of page views
		Map<String, Double> ranked_by_total_pv = new HashMap<String, Double>();
		for(Entry<String, DescriptiveStatistics> entry : page_views.entrySet()){
			ranked_by_total_pv.put(entry.getKey(), entry.getValue().getSum());
		}
		List sorted_keys = MapFun.sortMapByValue(ranked_by_total_pv);
		Collections.reverse(sorted_keys);
		int max = 20; int n = 1;
		report+="Top "+max+" articles by number of views\n";
		try {
			FileWriter writer = new FileWriter(outfile);
			writer.write("n\ttitle\td.getSum()\td.getMean()\td.getMax()\td.getPercentile(50)\n");
			for(Object k : sorted_keys){
				DescriptiveStatistics d = page_views.get(k);
				if(n<max){
					report+=n+"\t"+k+"\t"+d.getSum()+"\t"+d.getMean()+"\t"+d.getMax()+"\n";
				}
				writer.write(n+"\t"+k+"\t"+d.getSum()+"\t"+d.getMean()+"\t"+d.getMax()+"\t"+d.getPercentile(50)+"\n");
				n++;
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return report;
	}

	public static String generatePageSetViewReportText(Set<String> titles, String outdir){
		String report = "";
		//Attention measures for set - page views
		//total page views for all articles for past 12 months
		Calendar now = Calendar.getInstance();
		Calendar then = Calendar.getInstance();
		then.add(Calendar.YEAR, -1);
		//go get the data
		report+="Last 12 months (from "+DateFormat.getInstance().format(now.getTime())+")";
		report+=summarizeSetInRange(titles, then, now, outdir+"_last12.txt");
		report+="\n";
		report+="Last month (from "+DateFormat.getInstance().format(now.getTime())+")";
		then.add(Calendar.YEAR, 1);
		then.add(Calendar.MONTH, -1);
		report+=summarizeSetInRange(titles, then, now, outdir+"_last1.txt");

		return report;
	}

	public static Map<String, DescriptiveStatistics> getPageViewSummaryForDateRange(Collection<String> titles, Calendar t1, Calendar t2){
		Map<String, DescriptiveStatistics> page_views = new HashMap<String, DescriptiveStatistics>();
		int n = 1;
		for(String title : titles){
			System.out.println("retrieving page view info for "+n+" of "+titles.size()+" "+title);
			DescriptiveStatistics views = getPageViewSummaryForDateRange(title, t1, t2);
			if(views!=null){
				page_views.put(title, views);
			}else{
				page_views.put(title, new DescriptiveStatistics());
			}
			n++;
		}
		return page_views;
	}

	/**
	 * Uses the stats.grok.se service to get cumulative page view statistics for the requested date range.
	 * Note that the service returns 0 values for a few days for pages typically viewed 100,000+times/day hence it seems there are some errors..
	 * Might improve accuracy to eliminate zero-valued days for titles with average views over 1,000 a day..
	 * @param title
	 * @param year1
	 * @param month1
	 * @param day1
	 * @param year2
	 * @param month2
	 * @param day2
	 * @return
	 */
	public static DescriptiveStatistics getPageViewSummaryForDateRange(String title, Calendar t1, Calendar t2){
		int year1 = t1.get(Calendar.YEAR);
		int month1 = t1.get(Calendar.MONTH);
		int day1 = t1.get(Calendar.DAY_OF_MONTH);
		int year2 = t2.get(Calendar.YEAR);
		int month2 = t2.get(Calendar.MONTH);
		int day2 = t2.get(Calendar.DAY_OF_MONTH);
		DescriptiveStatistics days = new DescriptiveStatistics();
		//get the data (comes in blocks of months)
		if(year1>year2||month1>12||month2>12||day1>31||day2>31){
			System.out.println("please enter correct date ranges");
			return null;
		}
		int m_start = month1;

		for(int year = year1; year<=year2; year++){
			if(year>year1){
				m_start = 1;
			}
			for(int month = m_start; month<=12; month++){
				if(year==year2){
					if(month>month2){
						break;
					}
				}
				String year_s = year+""; String month_s = month+"";  
				if(month<10){
					month_s = "0"+month_s;
				}
				try {
					MonthlyPageViews mpvs = parsePageViews(getPageViewJson(title, year_s, month_s));
					int m_max = 31;
					int m_min = 0;
					if(year==year2 && month==month2){
						m_max = day2;
					}else{
						switch (month){
						case 2: m_max = 28;
						case 4: m_max = 30;
						case 6: m_max = 30;
						case 9: m_max = 30;
						case 11: m_max = 30;
						default: m_max = 31;
						}
						if(year%4==0&&month==2){
							m_max = 29;
						}
					}
					if(year == year1 && month == month1){
						m_min = day1;
					}
					//these are here because the service seems to arbitrarily switch between starting on 0 and starting on 1 and ending on the actual last day or the day before..
					if(mpvs.getDaily_views()[m_min]==0){
						m_min++;
					}
					if(mpvs.getDaily_views()[m_max]==0){
						m_max--;
					}
					for(int i=m_min ; i<=m_max; i++){
						days.addValue(mpvs.getDaily_views()[i]);
						//						if(mpvs.getDaily_views()[i]==0){
						//							System.out.println(i+" m min "+m_min+" month "+month+" year "+year);
						//						}
					}
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return days;
	}


	public static MonthlyPageViews parsePageViews(String pv_json){
		MonthlyPageViews pv = new MonthlyPageViews();
		try {
			JSONObject r = new JSONObject(pv_json);
			if(r==null){
				return null;
			}
			String title = r.getString("title");
			pv.setTitle(title);
			String month = r.getString("month");
			double month_views = r.getDouble("total_views");
			pv.setMonthly_total(month_views);
			JSONArray days = (JSONArray) r.get("daily_views");
			if(days == null){
				return null;
			}
			for(int i=0; i<days.length(); i++){
				double views = days.getDouble(i);
				pv.getDaily_views()[i]=views;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pv;
	}


	public static String getPageViewJson(String title, String year, String month) throws UnsupportedEncodingException{
		String url = "http://stats.grok.se/json/en/";
		String out = "";
		//200712/insulin
		title = title.replace(" ", "_");
		String encoded = URLEncoder.encode(title, "UTF8");
		GetMethod get = new GetMethod(url+year+month+"/"+encoded);
		// Get HTTP client
		HttpClient httpclient = new HttpClient();
		// Execute request
		try {
			int result = httpclient.executeMethod(get);
			// Display status code
			//	System.out.println("Response status code: " + result);
			// Display response
			//	System.out.println("Response body: ");
			//	out = get.getResponseBodyAsString();
			//	System.out.println(out);
			InputStream s = get.getResponseBodyAsStream(); 
			out = HttpUtil.convertStreamToString(s);

		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// Release current connection to the connection pool once you are done
			get.releaseConnection();
		}
		return out;
	}

}
