package org.scripps.util.json.google;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.scripps.util.DateFun;

import java.util.Collections;

public class MotionChartRow {

	// {"c": [{"v":"Apples"},{"v":new Date(568015200000)},{"v":1000},{"v":1000},{"v":"good"},{"v":1000}]}

	String entity;
	Calendar date;
	List vals;
	
	public MotionChartRow(String entityn, Calendar daten, List values) {
		entity = entityn; date = daten;
		vals = new ArrayList(values);
	}

	public static void main(String args[]){
		List values = new ArrayList();
		values.add(100); values.add(103); values.add("great"); values.add(105);
		Collections.reverse(values);
		MotionChartRow r = new MotionChartRow("apples", Calendar.getInstance(), values);
		System.out.println(r.getAsJSON());
	}
	
	public String toString(){
		String s = entity+"\t"+DateFun.year_month_day().format(date.getTime())+"\t";
		for(Object o : vals){
			s+=o+"\t";
		}
		return s;
	}
	
	public String getAsJSON(){
		String d = date.getTimeInMillis()+"";
		String r = "{\"c\": [{\"v\":\""+entity+"\"},{\"v\":"+d+"},";
		for(Object o : vals){
			r+= "{\"v\":\""+o+"\"},";
		}
			r = r.substring(0, r.length()-1);
			r+=	"]}";
		return r;
	}
	

	
}
