package org.gnf.ncbo.web;

public class NcboAnnotation {

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
	
}
