/**
 * 
 */
package org.genewiki.stream;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.genewiki.GeneWikiLink;
import org.genewiki.GeneWikiPage;
import org.genewiki.Heading;
import org.genewiki.Reference;
import org.genewiki.metrics.VolumeReport;
import org.scripps.datasources.MyGeneInfo;
import org.scripps.util.TextFun;

/**
 * @author bgood
 *
 */
public class Tweetable {
	int words;
	int sentences;
	int references;
	int out_links;
	int in_links;
	int headings;
	int media;
	int bytes;
	String user;
	String timestamp;
	String link;
	String difflink;
	String hashtag;
	String summary;
	String user_comment;


	public Tweetable(Tweetable source){
		words = source.words;
		sentences = source.sentences;
		references = source.references;
		out_links = source.references;
		in_links = source.references;
		headings = source.references;
		media = source.media;
		bytes = source.bytes;
		user = source.user;
		timestamp = source.timestamp;
		link = source.link;
		difflink = source.difflink;
		hashtag = source.hashtag;
		summary = source.summary;
		user_comment = source.user_comment;
	}
	
	/**
	 * Given two genewikipage's (revisions for the same article) emit a compressed summary of the meaningful changes that they contain
	 * @param p_t0
	 * @param p_t1
	 */
	public Tweetable(GeneWikiPage p_t0, GeneWikiPage p_t1){
		user_comment = p_t1.getLastComment();
		bytes = p_t1.getSize() - p_t0.getSize();
		try {
			p_t1.setGene_symbol(MyGeneInfo.getSymbolByGeneid(p_t1.getNcbi_gene_id(), true));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(p_t1.getGene_symbol()!=null&&p_t1.getGene_symbol().length()>1){
			hashtag = "#"+TextFun.makeHashTagSafe(p_t1.getGene_symbol());
		}else if(p_t1.getTitle().length()<7){
			hashtag = "#"+TextFun.makeHashTagSafe(p_t1.getTitle());
		}else {
			hashtag = "#"+TextFun.makeHashTagSafe(p_t1.getTitle());
		}
		timestamp = p_t1.getTimestamp();
		user = p_t1.getLasteditor();
		//lets not publish ip addresses..
		user = user.replaceAll("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)", "anon");
		user = TextFun.makeHashTagSafe(user);
		user = "#"+user;
		//?title=Reelin&action=historysubmit&diff=404945390&oldid=403408911
		difflink = "http://en.wikipedia.org/wiki/?action=historysubmit&diff="+p_t1.getRevid()+"&oldid="+p_t0.getRevid();
		link =     "http://en.wikipedia.org/wiki/"+p_t1.getTitle().replaceAll(" ","_");
		VolumeReport v_t0 = new VolumeReport();
		v_t0.extractVolumeFromPopulatedPage(p_t0);
		VolumeReport v_t1 = new VolumeReport();
		v_t1.extractVolumeFromPopulatedPage(p_t1);
		words = (int)(v_t1.getWords() - v_t0.getWords());
		sentences = (int)(v_t1.getSentences() - v_t0.getSentences());
		references = (int)(v_t1.getPubmed_refs() - v_t0.getPubmed_refs());
		out_links = (int)(v_t1.getLinks_out() - v_t0.getLinks_out());
		in_links = (int)(v_t1.getLinks_in() - v_t0.getLinks_in());
		headings = (int)(v_t1.getHeadings() - v_t0.getHeadings());
		media = (int)(v_t1.getMedia_files() - v_t0.getMedia_files());

	}

	public int getWords() {
		return words;
	}
	public void setWords(int words) {
		this.words = words;
	}
	public int getSentences() {
		return sentences;
	}
	public void setSentences(int sentences) {
		this.sentences = sentences;
	}
	public int getReferences() {
		return references;
	}
	public void setReferences(int references) {
		this.references = references;
	}
	public int getOut_links() {
		return out_links;
	}
	public void setOut_links(int out_links) {
		this.out_links = out_links;
	}
	public int getIn_links() {
		return in_links;
	}
	public void setIn_links(int in_links) {
		this.in_links = in_links;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getHashtag() {
		return hashtag;
	}
	public void setHashtag(String hashtag) {
		this.hashtag = hashtag;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}

	public int getHeadings() {
		return headings;
	}

	public void setHeadings(int headings) {
		this.headings = headings;
	}

	public int getMedia() {
		return media;
	}

	public void setMedia(int media) {
		this.media = media;
	}

	public int getBytes() {
		return bytes;
	}

	public void setBytes(int bytes) {
		this.bytes = bytes;
	}

	public String getDifflink() {
		return difflink;
	}

	public void setDifflink(String difflink) {
		this.difflink = difflink;
	}

	public String getUser_comment() {
		return user_comment;
	}

	public void setUser_comment(String user_comment) {
		this.user_comment = user_comment;
	}




}
