package org.gnf.search;

public class SearchEngineIntersect {

	String term1;
	String term2;
	double term1_hits;
	double term2_hits;
	double intersect_hits;
	double simple_cooccur;
	double normalizedYahooRank;
	//size of google index now unknown
	//searching for 'a' = 20,120,000,000 hits		
	public static double LogM = 24.3315042;
	
	public static void main(String[] args){
		double horse = 12100; //42400; //46700000;
		double rider = 43500; //139000; //12200000;
		double both =  1010; //16600; //10200000; //2630000;
		//double m =     20120000000;   //8058044651;
		
		//size of Yahoo index now unknown
		//searching for 'a' = 36,900,990,446 hits		
		//ln(36,900,990,446) = 24.3315042
		double logm = 24.3315042;
		double loghorse = Math.log(horse);
		double logrider = Math.log(rider);
		double logboth = Math.log(both);
		
		double ngdhorserider = (Math.max(loghorse, logrider) - logboth)/(logm - Math.min(loghorse, logrider));
		System.out.println(ngdhorserider);
	}
	
	public SearchEngineIntersect(String term1, String term2, double term1Hits,
			double term2Hits, double intersectHits) {
		super();
		this.term1 = term1;
		this.term2 = term2;
		term1_hits = term1Hits;
		term2_hits = term2Hits;
		intersect_hits = intersectHits;
		simple_cooccur = 2*intersectHits/(term1_hits+term2_hits);
		if(intersectHits==0){
			normalizedYahooRank = 0;
		}else{
		normalizedYahooRank = 
			1 - ((Math.max(Math.log(term1Hits), Math.log(term2Hits)) - Math.log(intersectHits))/
			(LogM - Math.min(Math.log(term1Hits), Math.log(term2Hits))));
		}
	}
	public String getTerm1() {
		return term1;
	}
	public void setTerm1(String term1) {
		this.term1 = term1;
	}
	public String getTerm2() {
		return term2;
	}
	public void setTerm2(String term2) {
		this.term2 = term2;
	}
	public double getTerm1_hits() {
		return term1_hits;
	}
	public void setTerm1_hits(double term1Hits) {
		term1_hits = term1Hits;
	}
	public double getTerm2_hits() {
		return term2_hits;
	}
	public void setTerm2_hits(double term2Hits) {
		term2_hits = term2Hits;
	}
	public double getIntersect_hits() {
		return intersect_hits;
	}
	public void setIntersect_hits(double intersectHits) {
		intersect_hits = intersectHits;
	}
	public double getSimple_cooccur() {
		return simple_cooccur;
	}
	public void setSimple_cooccur(double simpleCooccur) {
		simple_cooccur = simpleCooccur;
	}

	public double getNormalizedYahooRank() {
		return normalizedYahooRank;
	}

	public void setNormalizedYahooRank(double normalizedYahooRank) {
		this.normalizedYahooRank = normalizedYahooRank;
	}
	
	
}
