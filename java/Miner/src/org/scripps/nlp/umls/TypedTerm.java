/**
 * 
 */
package org.scripps.nlp.umls;

import java.util.ArrayList;
import java.util.List;

/**
 * @author bgood
 *
 */
public class TypedTerm {
	String term;
	float score;
	String cui;
	String preferred_term;
	List<SemanticType> types;


	public TypedTerm(String term, String score, String cui,
			String preferred_term, String types_) {
		super();
		this.term = term;
		this.score = Float.parseFloat(score);
		this.cui = cui;
		this.preferred_term = preferred_term;
		String[] t = types_.split(";");
		List<SemanticType> types = new ArrayList<SemanticType>();
		for(String tc : t){
			String[] sts = tc.split("\\|");
			if(sts.length==3){
				SemanticType stype = new SemanticType(sts[0], sts[1], sts[2]);
				types.add(stype);
			}
		}
		this.setTypes(types);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public String getCui() {
		return cui;
	}

	public void setCui(String cui) {
		this.cui = cui;
	}

	public String getPreferred_term() {
		return preferred_term;
	}

	public void setPreferred_term(String preferred_term) {
		this.preferred_term = preferred_term;
	}

	public List<SemanticType> getTypes() {
		return types;
	}

	public void setTypes(List<SemanticType> types) {
		this.types = types;
	}

}
