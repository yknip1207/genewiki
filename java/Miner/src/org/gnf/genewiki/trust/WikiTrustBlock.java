/**
 * 
 */
package org.gnf.genewiki.trust;

/**
 * @author bgood
 *
 */
public class WikiTrustBlock {

	/**
	 * @param args
	 */
	
	String editor;
	double trust;
	int revid;
	String text;
	int start;
	int stop;
	
	
	public static void main(String[] args) {
		

	}

	public String getEditor() {
		return editor;
	}

	public void setEditor(String editor) {
		this.editor = editor;
	}

	public double getTrust() {
		return trust;
	}

	public void setTrust(double trust) {
		this.trust = trust;
	}

	public int getRevid() {
		return revid;
	}

	public void setRevid(int revid) {
		this.revid = revid;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getStop() {
		return stop;
	}

	public void setStop(int stop) {
		this.stop = stop;
	}

	
	
}
