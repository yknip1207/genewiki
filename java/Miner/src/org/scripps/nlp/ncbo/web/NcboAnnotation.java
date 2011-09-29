package org.scripps.nlp.ncbo.web;

import org.genewiki.GeneWikiPage;

public class NcboAnnotation implements Comparable{

	double score;
	Concept concept;
	Context context;
	
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public Concept getConcept() {
		return concept;
	}
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	public Context getContext() {
		return context;
	}
	public void setContext(Context context) {
		this.context = context;
	}
	
	public String toString(){
		if(context.isDirect){
		return "direct\t"+context.contextClass+"\t"+score+"\t"+concept.getLocalConceptId()+"\t"+concept.getPreferredName()+"\t"+context.getFrom()+"\t"+context.getTo();
		}else{
			return "mapped\t"+
			context.contextClass+"\t"+
			score+"\t"+context.concept.getLocalConceptId()+"\t"+
			context.concept.getPreferredName()+"\t"+
			context.getFrom()+"\t"+context.getTo();
		}
	}
	@Override
	public int compareTo(Object o) {
		if(! (o instanceof NcboAnnotation)){
			return -1;
		}
		NcboAnnotation target = (NcboAnnotation)o;
		return(this.getConcept().getFullId().compareTo(target.getConcept().getFullId()));
	}

	
	@Override
	public boolean equals(Object o) {
		if(! (o instanceof NcboAnnotation)){
			return false;
		}
		NcboAnnotation target = (NcboAnnotation)o;
		return(this.getConcept().getFullId().equals(target.getConcept().getFullId()));
	}

	@Override
	public int hashCode() {
		int code = this.getConcept().getFullId().hashCode();
		code = 31 * code;
		return code;
	}

}
