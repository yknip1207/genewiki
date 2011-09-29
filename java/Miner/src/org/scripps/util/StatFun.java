package org.scripps.util;

import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.inference.ChiSquareTest;
import org.apache.commons.math.stat.inference.ChiSquareTestImpl;
import org.apache.commons.math.util.MathUtils;
import org.apache.commons.math.distribution.HypergeometricDistribution;
import org.apache.commons.math.distribution.HypergeometricDistributionImpl;

public class StatFun {
	
	public static void main(String[] args){
//		//example chance of drawing 2 red cards in 5 card hand = 0.325
//		int populationSize = 52; int numberOfSuccessesInPopulation = 26; int sampleSize = 5; 
//		int successesInSample = 2;		
		//example chance of drawing 2 red cards in 5 card hand = 0.325
//		int populationSize = 5000; int numberOfSuccessesInPopulation = 100; int sampleSize = 4; 
//		int successesInSample = 1;		
//		double p = hypergeoTest(populationSize, numberOfSuccessesInPopulation, sampleSize, successesInSample);
//		System.out.println(p);
		
		int plusplus = 15; int minusplus = 1; int plusminus = 1; int minusminus = 12500;
		System.out.println("Chi square "+chiSquareValue(plusplus, minusplus, plusminus, minusminus));

		System.out.println("fisher 2 tail "+fishersExact2tailed(plusplus,minusplus,plusminus,minusminus));
	}

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
	
	public static double hypergeoTest(int populationSize, int numberOfSuccessesInPopulation, int sampleSize, int successesInSample){
		HypergeometricDistribution h = new HypergeometricDistributionImpl(populationSize, numberOfSuccessesInPopulation, sampleSize); 
		return h.probability(successesInSample);
		
	}
	
	public static double chiSquareTest(int plusplus, int minusplus, int plusminus, int minusminus){
		long[][] counts = new long[2][2];
		//++
		counts[0][0] = plusplus;
		//-+
		counts[1][0] = minusplus;
		//+- 
		counts[0][1] = plusminus;
		//--
		counts[1][1] = minusminus;
		ChiSquareTest c = new ChiSquareTestImpl();
		double chi = 0;
		try {
			chi = c.chiSquareTest(counts);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return chi;
		
	}
	
	public static double oddsRatio(int a, int b, int c, int d){
		double o = (a/(a+c))/(b/(b+d));
		return o;		
		}
	
	public static double fishersExact2tailed(int a, int b, int c, int d){
	//	double p = (MathUtils.factorial(a+b)*MathUtils.factorial(c+d)*MathUtils.factorial(a+c)*MathUtils.factorial(b+d))/(MathUtils.factorial(a)*MathUtils.factorial(b)*MathUtils.factorial(c)*MathUtils.factorial(d)*MathUtils.factorial(a+b+c+d));
		double p = 0;
		FisherExact fact = new FisherExact(a+b+c+d);
		p = fact.getTwoTailedP(a, b, c, d);
		return p;
		
	}
	
	public static double chiSquareValue(int plusplus, int minusplus, int plusminus, int minusminus){
		long[][] counts = new long[2][2];
		//++
		counts[0][0] = plusplus;
		//-+
		counts[1][0] = minusplus;
		//+- 
		counts[0][1] = plusminus;
		//--
		counts[1][1] = minusminus;
		ChiSquareTest c = new ChiSquareTestImpl();
		double chi = 0;
		chi = c.chiSquare(counts);

		return chi;
		
	}
	
}
