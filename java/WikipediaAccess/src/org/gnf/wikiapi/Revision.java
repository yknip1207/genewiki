package org.gnf.wikiapi;

/**
 * Manages revision data from the <a href="http://meta.wikimedia.org/w/api.php">Wikimedia API</a>
 */
public class Revision {
	String user;

	String timestamp;

	String anon;

	String content;

	String revid;
	
	String parentid;
	
	int size;
	
	public Revision() {
		this.user = "";
		this.timestamp = "";
		this.anon = "";
		this.content = "";
		this.revid = "";
		this.parentid = "";
		size = 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Revision) {
			return content.equals(((Revision) obj).content);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return content.hashCode();
	}
	
	public String getAnon() {
		return anon;
	}

	public void setAnon(String anon) {
		this.anon = anon;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getRevid() {
		return revid;
	}

	public void setRevid(String revid) {
		this.revid = revid;
	}

	public String getParentid() {
		return parentid;
	}

	public void setParentid(String parentid) {
		this.parentid = parentid;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
}
