/**
 * 
 */
package org.gnf.genewiki.metrics;

import java.util.HashSet;
import java.util.Set;

import org.gnf.genewiki.GeneWikiLink;
import org.gnf.genewiki.GeneWikiPage;
import org.gnf.genewiki.Reference;
import org.gnf.genewiki.Sentence;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author bgood
 *
 */
public class VolumeReport {

	int n_articles;
	String geneid;
	String title;
	String revid;
	String timestamp;
	String lasteditor;
	double bytes; //as reported by wikipedia
	double words; //words found in sentences found above the 'references' section with inline references removed (words counted by splitting text on spaces and counting)
	double links_out; //links to other wikipedia pages (does not include transcluded links but does include links to equivalent pages in other language wikipedias)
	double links_in; //links from other wikipedia pages (does include transcluded links)
	double external_links; //lots of auto-generated links to external bioinformatics resources
	double pubmed_refs; //number inline references	
	double redirects; //number redirects to this page (potential synonyms)
	double sentences; //sentences found above the 'references' section with inline references removed
	double media_files; //images or other files stored on wikipedia and displayed/linked to on this page
	double headings;

	public VolumeReport(){
		setTimestamp("");
		setLasteditor("");
		setGeneid("");
		setTitle("");
		setRevid("");
		setBytes(0);
		setWords(0);
		setLinks_out(0);
		setLinks_in(0);
		setExternal_links(0);
		setPubmed_refs(0);
		setRedirects(0);
		setSentences(0);
		setMedia_files(0);
		setN_articles(0);
		setHeadings(0);
	}

	public static String getHeader(){
		String out = "geneid\ttitle\trevid\ttimestamp\tlasteditor\tbytes\twords\tlinks_out\tlinks_in\texternal_links\tPubMed_references\tredirects\tsentences\tmedia_files\theadings\t";
		return out;
	}

	public String toString(){
		String out = geneid+"\t"+title+"\t"+revid+"\t"+timestamp+"\t"+lasteditor+"\t"+bytes+"\t"+words+"\t"+links_out+"\t"+links_in+"\t"+external_links+"\t"+pubmed_refs+"\t"+redirects+"\t"+sentences+"\t"+media_files+"\t"+headings+"\t";
		return out;
	}

	public JSONObject toJSON(){
		JSONObject r = new JSONObject();
		try {
			r.put("timestamp", getTimestamp());
			r.put("lasteditor", getLasteditor());
			r.put("geneid", getGeneid());
			r.put("title", getTitle());
			r.put("revid", getRevid());
			r.put("bytes", getBytes());
			r.put("links_out", getLinks_out());
			r.put("links_in", getLinks_in());
			r.put("timestamp", getTimestamp());
			r.put("ext_links", getExternal_links());
			r.put("pubmed_refs", getPubmed_refs());
			r.put("redirects", getRedirects());
			r.put("sentences", getSentences());
			r.put("words", getWords());
			r.put("media_files", getMedia_files());
			r.put("headings", getHeadings());
			r.put("n", getN_articles());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return r;
	}

	public void extractVolumeFromPopulatedPage(GeneWikiPage page) {
		if(page==null){
			return;
		}
		setLasteditor(page.getLasteditor());
		setTimestamp(page.getTimestamp());
		setGeneid(page.getNcbi_gene_id());
		setTitle(page.getTitle());
		setRevid(page.getRevid());
		setBytes(page.getSize());
		if(page.getGlinks()!=null){
			setLinks_out(page.getGlinks().size());
			double media = 0;
			for(GeneWikiLink glink : page.getGlinks()){
				if(glink.getTarget_page().contains("File")){
					media++;
				}
			}
			setMedia_files(media);
		}
		if(page.getInglinks()!=null){
			setLinks_in(page.getInglinks().size());
		}
		if(page.getExtlinks()!=null){
			setExternal_links(page.getExtlinks().size());
		}
		if(page.getHeadings()!=null){
			setHeadings(page.getHeadings().size());
		}
		Set<String> prefs = new HashSet<String>();
		int pmid_c = 0;
		if(page.getRefs()!=null){
			for(Reference ref : page.getRefs()){
				String pmid = ref.getPmid();
				if(pmid!=null&&pmid.length()>1&&prefs.add(pmid)){
					pmid_c++;
				}
			}
		}
		setPubmed_refs(pmid_c);
		setRedirects(page.getWikisynset().size());
		if(page.getSentences()!=null){
			setSentences(page.getSentences().size());
			double word = 0;
			for(Sentence sentence : page.getSentences()){
				String chars = sentence.getPrettyText();
				if(chars!=null){
					word += chars.split(" ").length;
				}
			}
			setWords(word);
		}else{
			setSentences(0);
			setWords(0);
		}
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getRevid() {
		return revid;
	}
	public void setRevid(String revid) {
		this.revid = revid;
	}
	public double getBytes() {
		return bytes;
	}
	public void setBytes(double bytes) {
		this.bytes = bytes;
	}
	public double getWords() {
		return words;
	}
	public void setWords(double words) {
		this.words = words;
	}
	public double getLinks_out() {
		return links_out;
	}
	public void setLinks_out(double links_out) {
		this.links_out = links_out;
	}
	public double getLinks_in() {
		return links_in;
	}
	public void setLinks_in(double links_in) {
		this.links_in = links_in;
	}


	public double getRedirects() {
		return redirects;
	}

	public void setRedirects(double redirects) {
		this.redirects = redirects;
	}

	public double getSentences() {
		return sentences;
	}

	public void setSentences(double sentences) {
		this.sentences = sentences;
	}

	public double getMedia_files() {
		return media_files;
	}

	public void setMedia_files(double media_files) {
		this.media_files = media_files;
	}

	public double getExternal_links() {
		return external_links;
	}

	public void setExternal_links(double external_links) {
		this.external_links = external_links;
	}

	public double getPubmed_refs() {
		return pubmed_refs;
	}

	public void setPubmed_refs(double pubmed_refs) {
		this.pubmed_refs = pubmed_refs;
	}

	public String getGeneid() {
		return geneid;
	}

	public void setGeneid(String geneid) {
		this.geneid = geneid;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getLasteditor() {
		return lasteditor;
	}

	public void setLasteditor(String lasteditor) {
		this.lasteditor = lasteditor;
	}

	public int getN_articles() {
		return n_articles;
	}

	public void setN_articles(int n_articles) {
		this.n_articles = n_articles;
	}

	public double getHeadings() {
		return headings;
	}

	public void setHeadings(double headings) {
		this.headings = headings;
	}



}
