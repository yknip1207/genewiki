/**
 * 
 */
package org.gnf.genewiki.metrics;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gnf.genewiki.GWRevision;
import org.gnf.util.DateFun;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author bgood
 *
 */
public class RevisionsReport {
	int total_edits;
	int distinct_articles_edited;
	int distinct_editors;
	int total_bytes_added;
	String timespan;
	Map<String, Integer> user_edits;
	Map<String, Calendar> user_lastedit;
	Map<String, Integer> article_edits;
	Map<String, Set<String>> article_editors;
	Map<String, Calendar> article_lastedit;
	Map<String, Integer> article_bytes;
	Map<String, List<GWRevision>> title_revisions;
	
	public RevisionsReport (List<GWRevision> revisions){
		if(revisions.size()==0){
			return;
		}
		total_edits = revisions.size();
		total_bytes_added = 0;
		user_edits = new HashMap<String, Integer>();
		user_lastedit = new HashMap<String, Calendar>();
		article_edits = new HashMap<String, Integer>();
		article_lastedit = new HashMap<String, Calendar>();
		article_bytes = new HashMap<String, Integer>();
		article_editors = new HashMap<String, Set<String>>();
		title_revisions = new HashMap<String, List<GWRevision>>();
		
		for(GWRevision r : revisions){
			List<GWRevision> revs = title_revisions.get(r.getTitle());
			if(revs==null){
				revs = new ArrayList<GWRevision>();
			}
			revs.add(r);
			title_revisions.put(r.getTitle(), revs);
			
			//users by total edits
			Integer edits = user_edits.get(r.getUser());
			if(edits==null){
				edits = new Integer(0);
			}
			edits++;
			user_edits.put(r.getUser(), edits);
			
			//users by most recent edit
			Date thisdate = null;
			try {
				thisdate = DateFun.wp_format().parse(r.getTimestamp());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Calendar thisedit = Calendar.getInstance();
			thisedit.setTime(thisdate);
		
			Calendar edit_date = user_lastedit.get(r.getUser());
			if(edit_date==null){
				edit_date = Calendar.getInstance();
				edit_date.setTime(thisdate);
				user_lastedit.put(r.getUser(), edit_date);
			}else{
				if(thisedit.getTimeInMillis()>edit_date.getTimeInMillis()){
					user_lastedit.put(r.getUser(), thisedit);
				}
			}
			//articles by total edits
			Integer art_edits = article_edits.get(r.getTitle());
			if(art_edits==null){
				art_edits = new Integer(0);
			}
			art_edits++;
			article_edits.put(r.getTitle(), art_edits);
			
			//articles by most recent edit
			Calendar art_date = article_lastedit.get(r.getTitle());
			if(art_date==null){
				art_date = Calendar.getInstance();
				art_date.setTime(thisdate);
				article_lastedit.put(r.getTitle(), art_date);
			}else{
				if(thisedit.getTimeInMillis()>edit_date.getTimeInMillis()){
					article_lastedit.put(r.getTitle(), thisedit);
				}
			}
		}
		for(Entry<String, List<GWRevision>> e : title_revisions.entrySet()){
			String title = e.getKey();
			List<GWRevision> revs = e.getValue();
	//		Collections.sort(revs);
			int bytes_added = revs.get(0).getSize()-revs.get(revs.size()-1).getSize();
			article_bytes.put(e.getKey(), bytes_added);
			total_bytes_added+=bytes_added;
			
			for(GWRevision r : revs){
				Set<String> editors = article_editors.get(title);
				if(editors==null){
					editors = new HashSet<String>();
				}
				editors.add(r.getUser());			
				article_editors.put(title, editors);
			}
		}
		distinct_articles_edited = article_edits.keySet().size();
		distinct_editors = user_edits.keySet().size();
	}
	
	public static String getSummaryHeader(){
		String header = "total_edits\tdistinct_articles_edited\tdistinct_editors\ttotal_bytes_added\t";
		return header;
	}
	public String getSummaryString(){	
		String report = total_edits+"\t"+distinct_articles_edited+"\t"+distinct_editors+"\t"+total_bytes_added+"\t";
		return report;
	}
	
	public JSONObject getSummaryJSON(){
		JSONObject json = new JSONObject();
		try {
			json.put("timespan", getTimespan());
			json.put("total_edits", getTotal_edits());
			json.put("distinct_articles", getDistinct_articles_edited());
			json.put("distinct_editors", getDistinct_editors());
			json.put("total_bytes_added", getTotal_bytes_added());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}
	
	public static String getArticleHeader(){
		String header = "title\tedits\teditors\tbytes\tlast_edit\t";
		return header;
	}
	public String getArticleString(String title){
		String row = 
		title+"\t"+
		article_edits.get(title)+"\t"+
		article_editors.get(title).size()+"\t"+
		article_bytes.get(title)+"\t"+
		DateFun.wp_format().format(article_lastedit.get(title).getTime())+"\t";
		return row;
	}
	
	public int getTotal_edits() {
		return total_edits;
	}
	public void setTotal_edits(int total_edits) {
		this.total_edits = total_edits;
	}
	public int getDistinct_articles_edited() {
		return distinct_articles_edited;
	}
	public void setDistinct_articles_edited(int distinct_articles_edited) {
		this.distinct_articles_edited = distinct_articles_edited;
	}
	public int getDistinct_editors() {
		return distinct_editors;
	}
	public void setDistinct_editors(int distinct_editors) {
		this.distinct_editors = distinct_editors;
	}
	public int getTotal_bytes_added() {
		return total_bytes_added;
	}
	public void setTotal_bytes_added(int total_bytes_added) {
		this.total_bytes_added = total_bytes_added;
	}
	public Map<String, Integer> getUser_edits() {
		return user_edits;
	}
	public void setUser_edits(Map<String, Integer> user_edits) {
		this.user_edits = user_edits;
	}
	public Map<String, Calendar> getUser_lastedit() {
		return user_lastedit;
	}
	public void setUser_lastedit(Map<String, Calendar> user_lastedit) {
		this.user_lastedit = user_lastedit;
	}
	public Map<String, Integer> getArticle_edits() {
		return article_edits;
	}
	public void setArticle_edits(Map<String, Integer> article_edits) {
		this.article_edits = article_edits;
	}
	public Map<String, Calendar> getArticle_lastedit() {
		return article_lastedit;
	}
	public void setArticle_lastedit(Map<String, Calendar> article_lastedit) {
		this.article_lastedit = article_lastedit;
	}

	public Map<String, List<GWRevision>> getTitle_revisions() {
		return title_revisions;
	}

	public void setTitle_revisions(Map<String, List<GWRevision>> title_revisions) {
		this.title_revisions = title_revisions;
	}

	public Map<String, Integer> getArticle_bytes() {
		return article_bytes;
	}

	public void setArticle_bytes(Map<String, Integer> article_bytes) {
		this.article_bytes = article_bytes;
	}

	public Map<String, Set<String>> getArticle_editors() {
		return article_editors;
	}

	public void setArticle_editors(Map<String, Set<String>> article_editors) {
		this.article_editors = article_editors;
	}

	public String getTimespan() {
		return timespan;
	}

	public void setTimespan(String timespan) {
		this.timespan = timespan;
	}
	
	
}
