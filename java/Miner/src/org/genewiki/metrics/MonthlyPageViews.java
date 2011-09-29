/**
 * 
 */
package org.genewiki.metrics;

/**
 * @author bgood
 *
 */
public class MonthlyPageViews {

	String title;
	double monthly_total;
	double[] daily_views;
	
	public MonthlyPageViews() {
		title = "";
		monthly_total = 0;
		daily_views = new double[32];
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public double getMonthly_total() {
		return monthly_total;
	}
	public void setMonthly_total(double monthly_total) {
		this.monthly_total = monthly_total;
	}
	public double[] getDaily_views() {
		return daily_views;
	}
	public void setDaily_views(double[] daily_views) {
		this.daily_views = daily_views;
	}
	
	
}
