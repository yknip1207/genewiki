package org.gnf.genewiki.metrics;

import org.json.JSONException;
import org.json.JSONObject;

public class PageViewsReport {

	String report_title;
	String timespan;
	int rank;
	int total_views;
	float mean_day;
	int max_day;
	float median_day;

	public JSONObject getJSON(){
		JSONObject vr = new JSONObject();
		try {
			vr.put("title", getReport_title());
			vr.put("timespan", getTimespan());
			vr.put("rank", getRank());
			vr.put("total_views", getTotal_views());
			vr.put("mean_day", getMean_day());
			vr.put("max_day", getMax_day());
			vr.put("median_day", getMedian_day());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return vr;
	}
	
// Reflection not working for some reason	
//	public String getJSON(){
//		String json = "";
//		JSONObject vr = new JSONObject(this);
//		json = vr.toString();
//		return json;
//	}
	
	public String getReport_title() {
		return report_title;
	}
	public void setReport_title(String report_title) {
		this.report_title = report_title;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public int getTotal_views() {
		return total_views;
	}
	public void setTotal_views(int total_views) {
		this.total_views = total_views;
	}
	public float getMean_day() {
		return mean_day;
	}
	public void setMean_day(float mean_day) {
		this.mean_day = mean_day;
	}
	public int getMax_day() {
		return max_day;
	}
	public void setMax_day(int max_day) {
		this.max_day = max_day;
	}
	public float getMedian_day() {
		return median_day;
	}
	public void setMedian_day(float median_day) {
		this.median_day = median_day;
	}

	public String getTimespan() {
		return timespan;
	}

	public void setTimespan(String timespan) {
		this.timespan = timespan;
	}
	
	
}
