/**
 * 
 */
package org.gnf.genewiki;

import org.gnf.wikiapi.Revision;

/**
 * @author bgood
 *
 */
public class GWRevision extends Revision implements Comparable{
	String title;
	String comment;
	String minor;
	
	public GWRevision(Revision rev, String title){
		this.setAnon(rev.getAnon());
		this.setContent(rev.getContent());
		this.setParentid(rev.getParentid());
		this.setRevid(rev.getRevid());
		this.setSize(rev.getSize());
		this.setTimestamp(rev.getTimestamp());
		this.setTitle(title);
		this.setUser(rev.getUser());
	}
	
	public GWRevision() {
		// TODO Auto-generated constructor stub
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public int compareTo(Object arg0) {
		GWRevision tocompare = (GWRevision) arg0;
		return this.getTimestamp().compareTo(tocompare.getTimestamp());
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getMinor() {
		return minor;
	}

	public void setMinor(String minor) {
		this.minor = minor;
	}
}
