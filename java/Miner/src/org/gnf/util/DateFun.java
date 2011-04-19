package org.gnf.util;

import java.text.SimpleDateFormat;


public class DateFun {

	public static SimpleDateFormat wp_format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	public static SimpleDateFormat year_month_day = new SimpleDateFormat("yyyy-MM-dd");
	public static SimpleDateFormat yearMonthDay = new SimpleDateFormat("yyyyMMdd");
	public static SimpleDateFormat  monthDayYear = new SimpleDateFormat("MMddyyyy");
	public static SimpleDateFormat  year = new SimpleDateFormat("yyyy");
	public static SimpleDateFormat  weekinyear = new SimpleDateFormat("yyyy'W'ww");
	//20060501000000
	public static SimpleDateFormat timestamp = new SimpleDateFormat("yyyyMMddHHmmss");
}
