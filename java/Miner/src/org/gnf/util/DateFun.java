package org.gnf.util;

import java.text.SimpleDateFormat;

public class DateFun {

	private static SimpleDateFormat wp_format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	private static SimpleDateFormat year_month_day = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat yearMonthDay = new SimpleDateFormat("yyyyMMdd");
	private static SimpleDateFormat  monthDayYear = new SimpleDateFormat("MMddyyyy");
	private static SimpleDateFormat  year = new SimpleDateFormat("yyyy");
	private static SimpleDateFormat  weekinyear = new SimpleDateFormat("yyyy'W'ww");
	//20060501000000
	private static SimpleDateFormat timestamp = new SimpleDateFormat("yyyyMMddHHmmss");
	
	/**
	 * Creates a new SimpleDateFormat using "yyyy-MM-dd'T'HH:mm:ss'Z'"
	 * @return
	 */
	public static SimpleDateFormat wp_format() {
		return (SimpleDateFormat) wp_format.clone();
	}
	
	/**
	 * Creates a new SimpleDateFormat using "yyyy-MM-dd"
	 * @return
	 */
	public static SimpleDateFormat year_month_day() {
		return (SimpleDateFormat) year_month_day.clone();
	}
	
	/**
	 * Creates a new SimpleDateFormat using "yyyyMMdd"
	 * @return
	 */
	public static SimpleDateFormat yearMonthDay() {
		return (SimpleDateFormat) yearMonthDay.clone();
	}
	
	/**
	 * Creates a new SimpleDateFormat using "MMddyyyy"
	 * @return
	 */
	public static SimpleDateFormat monthDayYear() {
		return (SimpleDateFormat) monthDayYear.clone();
	}
	
	/**
	 * Creates a new SimpleDateFormat using "yyyy"
	 * @return
	 */
	public static SimpleDateFormat year() {
		return (SimpleDateFormat) year.clone();
	}
	
	/**
	 * Creates a new SimpleDateFormat using "yyyy'W'ww"
	 * @return
	 */
	public static SimpleDateFormat weekinyear() {
		return (SimpleDateFormat) weekinyear.clone();
	}
	
	/**
	 * Creates a new SimpleDateFormat using "yyyyMMddHHmmss"
	 * @return
	 */
	public static SimpleDateFormat timestamp() {
		return (SimpleDateFormat) timestamp.clone();
	}
	
}
