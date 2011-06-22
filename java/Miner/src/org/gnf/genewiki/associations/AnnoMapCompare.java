package org.gnf.genewiki.associations;

import org.gnf.util.SetComparison;

/**
 * Keep track of comparisons between maps
 * @author bgood
 *
 */
public class AnnoMapCompare {

	//keys of the maps
	SetComparison keysetcompare;
	//values of the intersection of the keys
	SetComparison valuesetcompare;
	
	public AnnoMapCompare(SetComparison keysetcompare,
			SetComparison valuesetcompare) {
		super();
		this.keysetcompare = keysetcompare;
		this.valuesetcompare = valuesetcompare;
	}
	public SetComparison getKeysetcompare() {
		return keysetcompare;
	}
	public void setKeysetcompare(SetComparison keysetcompare) {
		this.keysetcompare = keysetcompare;
	}
	public SetComparison getValuesetcompare() {
		return valuesetcompare;
	}
	public void setValuesetcompare(SetComparison valuesetcompare) {
		this.valuesetcompare = valuesetcompare;
	}
	
	public String toString(){
		String out = keySetOverlapToString()+"\n"+valSetOverlapToString();
		return out;
	}
	
	public String keySetOverlapToString(){
		String out = "Keyset Overlap:\n" +
				"Set1 size\t"+keysetcompare.getSet1_size()+"\tSet 2 size\t"+keysetcompare.getSet2_size()+"\tIntersection\t" +
				keysetcompare.getSet_intersection()+"\tPrecision (from set1 to set2) "+keysetcompare.getSet1_precision()+"\tPercent Overlap\t" +
				""+keysetcompare.getAccuracy()+"\tF overlap\t"+keysetcompare.getF();
		return out;
	}
	
	public String valSetOverlapToString(){
		String out = "Annoset Overlap:\n" +
		"Set1 size\t"+valuesetcompare.getSet1_size()+"\tSet 2 size\t"+valuesetcompare.getSet2_size()+"\tIntersection\t" +
		valuesetcompare.getSet_intersection()+"\tPrecision (from set1 to set2) "+valuesetcompare.getSet1_precision()+"\tPercent Overlap\t" +
		""+valuesetcompare.getAccuracy()+"\tF overlap\t"+valuesetcompare.getF();
		return out;
	}
	
}
