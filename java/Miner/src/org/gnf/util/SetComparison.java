package org.gnf.util;

public class SetComparison {
	double set1_size;
	double set2_size;
	double set_intersection;	
	double set1_precision;
	double set1_recall;
	double f;
	double accuracy;

	double set2_precision;
	double set2_recall;

	public SetComparison(double set1_size, double set2_size,
			double set_intersection) {
		super();
		this.set1_size = set1_size;
		this.set2_size = set2_size;
		this.set_intersection = set_intersection;
		
		set1_precision = set_intersection/set1_size;
		set1_recall = set_intersection/set2_size;
		set2_precision = set_intersection/set2_size;
		set2_recall = set_intersection/set1_size;

		accuracy = set_intersection/(set1_size+set2_size-set_intersection);
		f = 2*set1_precision*set1_recall/(set1_precision+set1_recall);
	}
	
	public double getSet1_size() {
		return set1_size;
	}
	public void setSet1_size(double set1_size) {
		this.set1_size = set1_size;
	}
	public double getSet2_size() {
		return set2_size;
	}
	public void setSet2_size(double set2_size) {
		this.set2_size = set2_size;
	}
	public double getSet_intersection() {
		return set_intersection;
	}
	public void setSet_intersection(double set_intersection) {
		this.set_intersection = set_intersection;
	}
	public double getSet1_precision() {
		return set1_precision;
	}
	public void setSet1_precision(double set1_precision) {
		this.set1_precision = set1_precision;
	}
	public double getSet1_recall() {
		return set1_recall;
	}
	public void setSet1_recall(double set1_recall) {
		this.set1_recall = set1_recall;
	}
	
	public double getSet2_precision() {
		return set2_precision;
	}
	public void setSet2_precision(double set2_precision) {
		this.set2_precision = set2_precision;
	}
	public double getSet2_recall() {
		return set2_recall;
	}
	public void setSet2_recall(double set2_recall) {
		this.set2_recall = set2_recall;
	}

	public double getF() {
		return f;
	}

	public void setF(double f) {
		this.f = f;
	}

	public double getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}
	
	
	
}
