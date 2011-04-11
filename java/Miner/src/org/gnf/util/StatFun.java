package org.gnf.util;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

public class StatFun {

	public static String statsToTabString(DescriptiveStatistics stats){
		String row = stats.getN()+"\t"+stats.getSum()+"\t"+stats.getMin()+"\t"+stats.getMax()+"\t"+
		stats.getMean()+"\t"+stats.getStandardDeviation()+"\t"+stats.getPercentile(50)+"\t"+stats.getSkewness()+"\t"+
		stats.getKurtosis()+"\t";		
		return row;
	}
	public static String getTabHeader(){
		String header = "N\tsum\tmin\tmax\tmean\tstd dev\tmedian\tskewness\tkurtosis\t";
		return header;
	}
	
	public static void buildROC(String[] data){
		//data[0] = the rank of the row according to the chosen parameter (for example, the yahoo score for a triple)
		//data[1] = 
	}
	
}
