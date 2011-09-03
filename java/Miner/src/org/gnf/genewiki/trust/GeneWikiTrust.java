/**
 * 
 */
package org.gnf.genewiki.trust;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.gnf.genewiki.Config;
import org.gnf.genewiki.GeneWikiLink;
import org.gnf.genewiki.GeneWikiPage;
import org.gnf.genewiki.GeneWikiUtils;
import org.gnf.genewiki.Heading;
import org.gnf.genewiki.Reference;
import org.gnf.genewiki.Sentence;
import org.gnf.genewiki.associations.CandidateAnnotation;
import org.gnf.genewiki.mapping.GeneWikiPageMapper;
import org.gnf.genewiki.lingpipe.SentenceSplitter;
import org.gnf.ncbo.web.AnnotatorClient;
import org.gnf.ncbo.web.NcboAnnotation;
import org.gnf.util.MapFun;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * @author bgood
 *
 */
public class GeneWikiTrust {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		//		test();
		GeneWikiTrust wt = new GeneWikiTrust();
		boolean nobots = true;
		//wt.measureAuthorContributions(10000000, "/Users/bgood/data/bioinfo/gene_wikitrust_as_java/", "/Users/bgood/data/NARupdate2011/all_authors_no_bots_no_refs.txt", nobots);
		//	wt.measureTopAuthorContributions(10000000, "/Users/bgood/data/bioinfo/gene_wikitrust_as_java/", "/Users/bgood/data/NARupdate2011/top_authors_no_bots_no_refs.txt", nobots);
			EditorInfo info = wt.getEditorInfoForPage("7372", nobots, "/Users/bgood/data/bioinfo/gene_wikitrust_as_java/");
			System.out.println(info.getAllAsString());
		//wt.buildCumulativeAuthorContributionTable(10000000, "/Users/bgood/data/bioinfo/gene_wikitrust_as_java/", "/Users/bgood/data/NARupdate2011/author_trust_info.txt", false);
		
	}

	/**
	 * Creates gene wiki objects with the pagecontent replaced with content containing wikitrust markup.
	 * WikiTrust markup is collected from the wiki trust server and a new directory is populated with the updated java objects
	 * All the stored data (e.g. extracted hyperlinks) are left untouched except the sentences, references, and headings are reparsed
	 * @param limit
	 */
	public static void collectAllWikiTrustMarkup(int limit){
		String wt_gw_dir = Config.gwroot+"intermediate/javaobj_wt/";
		File done = new File(wt_gw_dir);
		List<String> have = new ArrayList<String>();
		for(String f : done.list()){
			have.add(f);
		}
		//timer
		long time = System.currentTimeMillis();
		//load the page titles (ncbi gene ids)
		File folder = new File(Config.gwikidir);
		if(folder.isDirectory()){
			int n = 0;
			for(String title : folder.list()){
				long starttime = System.currentTimeMillis();
				if(title.startsWith(".")||have.contains(title)){
					continue;
				}
				n++;
				GeneWikiPage page = GeneWikiUtils.deserializeGeneWikiPage(Config.gwikidir+title);
				boolean success = page.setPageContentToWikiTrust();
				if(success){
					//reparse to get the indexes right
					page.parseAndSetSentences();
					page.setReferences();
					page.setHeadings();	
					//write it out because this is slow
					GeneWikiUtils.saveToFile(page, wt_gw_dir+title);
				}else{
					System.out.println("gave up on title pageid revid "+title+" "+page.getPageid()+" "+page.getRevid());
				}
				if(n > limit){
					break;
				}
				if(n%100==0){
					System.out.print("loaded "+n+"\t");
				}
				if(n%1000==0){
					System.out.println("");
				}
				//wait one second between requests
				time = System.currentTimeMillis();
				if(time-starttime<1000){
					try {
						Thread.currentThread().sleep(1000);
						time = System.currentTimeMillis();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}
			}
		}
	}

	/**
	 * Collects the raw data that wikitrust uses to make a prediction about whether a particle revision is vandalism
	 * Does so for all the gene wiki articles and stores the results locally (up to the set limit)
	 * @param limit
	 */
	public static void collectAllWikiTrustRawData(int limit){
		String wt_gw_dir = Config.gwroot+"intermediate/wt_raw/";
		File done = new File(wt_gw_dir);
		List<String> have = new ArrayList<String>();
		for(String f : done.list()){
			have.add(f);
		}
		//timer
		long time = System.currentTimeMillis();
		//load the page titles (ncbi gene ids)
		File folder = new File(Config.gwikidir);
		if(folder.isDirectory()){
			int n = 0;
			for(String title : folder.list()){
				long starttime = System.currentTimeMillis();
				if(title.startsWith(".")||have.contains(title)){
					continue;
				}
				n++;
				GeneWikiPage page = GeneWikiUtils.deserializeGeneWikiPage(Config.gwikidir+title);
				String jsonresponse = WikiTrustClient.getTrustForWikiPage(page.getPageid(), page.getRevid(), "rawquality", 0);
				if(jsonresponse!=null){
					try {
						FileWriter w = new FileWriter(wt_gw_dir+page.getNcbi_gene_id());
						w.write(jsonresponse);
						w.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					System.out.println("gave up on title pageid revid "+title+" "+page.getPageid()+" "+page.getRevid());
				}
				if(n > limit){
					break;
				}
				if(n%100==0){
					System.out.print("loaded wt raw "+n+"\t");
				}
				if(n%1000==0){
					System.out.println("");
				}
				//wait one second between requests
				time = System.currentTimeMillis();
				if(time-starttime<1000){
					try {
						Thread.currentThread().sleep(1000);
						time = System.currentTimeMillis();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}
			}
		}
	}

	public static List<WikiTrustBlock> getTrustBlocksForSentence(List<WikiTrustBlock> blocks, Sentence sentence){
		List<WikiTrustBlock> bblocks = new ArrayList<WikiTrustBlock>();
		for(WikiTrustBlock block : blocks){
			if(block.getStart()<sentence.getStopIndex()&&
					block.getStop()>=sentence.getStartIndex()){
				bblocks.add(block);
			}
		}
		return bblocks;
	}

	public static void test(){
		String title = "DC-SIGN"; 
		GeneWikiPage prot = new GeneWikiPage(title);
		prot.defaultPopulateWikiTrust();		
		//		System.out.println("Page id: "+prot.getWikidetails().getPageid()+" revision id: "+prot.getWikidetails().getCurrentRevision().getRevid()+" "+prot.getWikidetails().getCurrentRevision().getUser());
		//		String trust = prot.getPageContent();
		//		SentenceSplitter.test(trust);

		//		System.out.println(WikiTrustClient.getTrustValForPage(trust));
		List<WikiTrustBlock> blocks = WikiTrustClient.getTrustBlocks(prot.getPageContent());

		int matched = 0; int notmatched = 0;
		for (Sentence sentence : prot.getSentences()) {
			boolean m = false;
			String text = sentence.getPrettyText();
			System.out.println(text);
			List<NcboAnnotation> annos = AnnotatorClient.ncboAnnotateText(text, false, true, false, false, false, false, false);
			if(annos==null||annos.size()==0){
				continue;
			}
			//get wikitrust blocks for this sentence
			List<WikiTrustBlock> wt_blocks = getTrustBlocksForSentence(blocks, sentence);
			//try to get the editor and trust for the anno producing text
			for(NcboAnnotation anno : annos){

				String matched_text = anno.getContext().getMatched_text();
				WikiTrustBlock matched_wt = null;
				for(WikiTrustBlock block : wt_blocks){
					if(block.getText().contains(matched_text)){
						m = true;
						matched_wt = block;
						break;
					}else{
						m = false;
					}
				}

				if(m){
					System.out.println("matched "+matched_text+"--"+matched_wt.getEditor()+"--"+matched_wt.getTrust()+"--"+matched_wt.getText()+"\n\t"+text);
					matched++;
				}else{
					System.out.println("not matched "+matched_text);
					notmatched++;
				}
			}
		}
		System.out.println("Annotations linked to editors and trust "+matched+" and not "+notmatched);
		matched = 0; notmatched = 0;
	}


	class EditorInfo{
		//wiki page title
		String title;
		//trusts for all edits
		Map<String, List<Double>> user_trusts;
		//number revisions saved
		Map<String, Set<Integer>> user_revs;
		//amount of text touched
		Map<String, Integer> user_texts;
		//pages touched
		Map<String, Set<String>> user_pages;


		public EditorInfo(String title, Map<String, List<Double>> user_trusts,
				Map<String, Set<Integer>> user_revs,
				Map<String, Integer> user_texts,
				Map<String, Set<String>> user_pages) {
			this.title = title;
			this.user_trusts = user_trusts;
			this.user_revs = user_revs;
			this.user_texts = user_texts;
			this.user_pages = user_pages;
		}
		public Map<String, List<Double>> getUser_trusts() {
			return user_trusts;
		}
		public void setUser_trusts(Map<String, List<Double>> user_trusts) {
			this.user_trusts = user_trusts;
		}
		public Map<String, Set<Integer>> getUser_revs() {
			return user_revs;
		}
		public void setUser_revs(Map<String, Set<Integer>> user_revs) {
			this.user_revs = user_revs;
		}
		public Map<String, Integer> getUser_texts() {
			return user_texts;
		}
		public void setUser_texts(Map<String, Integer> user_texts) {
			this.user_texts = user_texts;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public Map<String, Set<String>> getUser_pages() {
			return user_pages;
		}
		public void setUser_pages(Map<String, Set<String>> user_pages) {
			this.user_pages = user_pages;
		}	

		public String getAllAsString(){
			String trusty ="Title\tUser\tMean trust\trevisions_counted\ttext touched\tdistinct_pages_touched\n";
			
			Map<String, Double> user_avgtrust = new HashMap<String, Double>();
			for(Entry<String, List<Double>> ut : user_trusts.entrySet()){
				double m = 0;
				for(Double t : ut.getValue()){
					m+=t;
				}
				m = m/(double)ut.getValue().size();
				user_avgtrust.put(ut.getKey(), m);
			}
			
			List<String> sorted = MapFun.sortMapByValue(user_texts);			
			Collections.reverse(sorted);
			for(int i = 0; i<sorted.size(); i++){
				trusty+=
					title+"\t"+
					sorted.get(i)+"\t"+
					user_avgtrust.get(sorted.get(i))+"\t"+
					user_revs.get(sorted.get(i)).size()+"\t"+
					user_texts.get(sorted.get(i))+"\t"+
					user_pages.get(sorted.get(i)).size()+"\n";
			}
			return trusty;
		}


		public void getRankInfo(String outputfile){
			Map<Integer, Float> bin_total = new HashMap<Integer, Float>();
			Map<Integer, Integer> bin_count = new HashMap<Integer, Integer>();
			float max_editors = 0;
			Map<String, Integer> user_cont = getUser_texts();
			Map<String, Float> user_fraction = MapFun.convertCountsToPercentages(user_cont);
			List<String> sorted = MapFun.sortMapByValue(user_cont);			
			Collections.reverse(sorted);
			for(int i = 0; i<sorted.size(); i++){
				String key = sorted.get(i);

				Float fraction = user_fraction.get(key);
				Float ctotal = bin_total.get(i);
				if(ctotal==null){
					bin_total.put(i, fraction);
					bin_count.put(i, 1);
					max_editors = 1;
				}else{
					int bc = bin_count.get(i)+1;
					bin_count.put(i, bc);
					bin_total.put(i,(ctotal+fraction));
					if(bc>max_editors){
						max_editors = bc;
					}
				}
			}
			int total_editors = sorted.size();
			TreeMap<Integer, Float> t = new TreeMap<Integer, Float>(bin_total);

			try {
				FileWriter w = new FileWriter(outputfile, true);
				int i = 0;
				for(Entry<Integer, Float> e : t.entrySet()){
					String rank = e.getKey()+"";
					String contributions_at_rank = e.getValue()+"";
					w.write(getTitle()+"\t"+contributions_at_rank+"\t"+total_editors+"\t"+sorted.get(i)+"\n");
					System.out.println(getTitle()+"\t"+contributions_at_rank+"\t"+total_editors+"\t"+sorted.get(i));
					i++;
					if(i>0){
						break;
					}
				}
				w.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}



	}
	public EditorInfo summarizeEditorTrust(List<WikiTrustBlock> blocks, String title){
		//trusts for all edits
		Map<String, List<Double>> user_trusts = new HashMap<String, List<Double>>();
		//number revisions saved
		Map<String, Set<Integer>> user_revs = new HashMap<String, Set<Integer>>();
		//amount of text touched
		Map<String, Integer> user_texts = new HashMap<String, Integer>();
		//number of articles touched in this block set
		Map<String, Set<String>> user_pages = new HashMap<String, Set<String>>();
		
		for(WikiTrustBlock tb : blocks){
			//check if its a revision to a citation (almost always reformatting)
			//if it is, replace it with one letter (for some credit..)
			tb.text = tb.text.replaceAll(Reference.regexP, "r");
						
			List<Double> trusts = user_trusts.get(tb.getEditor());
			if(trusts==null){
				trusts = new ArrayList<Double>();
			}
			trusts.add(tb.getTrust());
			user_trusts.put(tb.getEditor(), trusts);
			Set<Integer> edits = user_revs.get(tb.getEditor());
			if(edits == null){
				edits = new HashSet<Integer>();
			}
			edits.add(tb.getRevid());			
			user_revs.put(tb.getEditor(), edits);
			
			Integer textamount = user_texts.get(tb.getEditor());
			if(textamount == null){
				textamount = 0;
			}
			textamount += tb.getText().length();
			user_texts.put(tb.getEditor(), textamount);
			
			Set<String> pages = user_pages.get(tb.getEditor());
			if(pages==null){
				pages = new HashSet<String>();
			}
			pages.add(tb.title);
			user_pages.put(tb.getEditor(), pages);
			
		}
		return new EditorInfo(title, user_trusts, user_revs, user_texts, user_pages);
	}

	public EditorInfo getEditorInfoForPage(String geneid, boolean nobots, String serialized_wiktrust){
		EditorInfo info = null;
		GeneWikiPage page = GeneWikiUtils.deserializeGeneWikiPage(serialized_wiktrust+geneid);			
		//	System.out.println(page.getTitle());
		List<WikiTrustBlock> blocks = WikiTrustClient.getTrustBlocks(page.getPageContent());
		if(nobots){
			List<WikiTrustBlock> botless = new ArrayList<WikiTrustBlock>(blocks.size());
			for(WikiTrustBlock block : blocks){
				if(!(block.editor.contains("bot")||block.editor.contains("Bot"))){
					botless.add(block);
				}
			}
			info = summarizeEditorTrust(botless, page.getTitle());
		}else{
			info = summarizeEditorTrust(blocks, page.getTitle());
		}
		return info;
	}



	public void buildAuthorContributionTable(int limit, String serialized_wiktrust, String outputfile, boolean nobots){
		File folder = new File(serialized_wiktrust);
		int n = 0;
		if(folder.isDirectory()){
			List<WikiTrustBlock> allblocks = new ArrayList<WikiTrustBlock>();
			for(String geneid : folder.list()){
				//geneid = "1017";//cdk2 "7157"; //p53//insulin "3630";// "5649"; //reelin
				n++;
				if(n>limit){
					break;
				}
				GeneWikiPage page = GeneWikiUtils.deserializeGeneWikiPage(serialized_wiktrust+geneid);			
				//	System.out.println(page.getTitle());
				List<WikiTrustBlock> blocks = WikiTrustClient.getTrustBlocks(page.getPageContent());
				for(WikiTrustBlock block : blocks){
					block.title = page.getTitle();
				}
				if(nobots){
					List<WikiTrustBlock> botless = new ArrayList<WikiTrustBlock>(blocks.size());
					for(WikiTrustBlock block : blocks){
						if(!(block.editor.contains("bot")||block.editor.contains("Bot"))){
							botless.add(block);
						}
					}
					allblocks.addAll(botless);
				}else{
					allblocks.addAll(blocks);
				}
			}
			EditorInfo einfo = summarizeEditorTrust(allblocks,"ALL");
			String cumulative = einfo.getAllAsString();
			try {
				FileWriter f = new FileWriter(outputfile);
				f.write(cumulative);
				f.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void measureAuthorContributions(int limit, String serialized_wiktrust, String outputfile, boolean nobots){
		File folder = new File(serialized_wiktrust);

		if(folder.isDirectory()){
			int n = 0;
			List<EditorInfo> e_info = new ArrayList<EditorInfo>();
			for(String geneid : folder.list()){
				//geneid = "1017";//cdk2 "7157"; //p53//insulin "3630";// "5649"; //reelin
				n++;
				if(n>limit){
					break;
				}
				GeneWikiPage page = GeneWikiUtils.deserializeGeneWikiPage(serialized_wiktrust+geneid);			
				//	System.out.println(page.getTitle());
				List<WikiTrustBlock> blocks = WikiTrustClient.getTrustBlocks(page.getPageContent());
				if(nobots){
					List<WikiTrustBlock> botless = new ArrayList<WikiTrustBlock>(blocks.size());
					for(WikiTrustBlock block : blocks){
						if(!(block.editor.contains("bot")||block.editor.contains("Bot"))){
							botless.add(block);
						}
					}
					e_info.add(summarizeEditorTrust(botless, page.getTitle()));
				}else{
					e_info.add(summarizeEditorTrust(blocks, page.getTitle()));
				}
			}
			Map<Integer, Float> bin_total = new HashMap<Integer, Float>();
			Map<Integer, Integer> bin_count = new HashMap<Integer, Integer>();
			float max_editors = 0;
			for(EditorInfo e : e_info){
				Map<String, Integer> user_cont = e.getUser_texts();
				Map<String, Float> user_fraction = MapFun.convertCountsToPercentages(user_cont);
				List<String> sorted = MapFun.sortMapByValue(user_cont);			
				Collections.reverse(sorted);
				for(int i = 0; i<sorted.size(); i++){
					String key = sorted.get(i);

					Float fraction = user_fraction.get(key);
					Float ctotal = bin_total.get(i);
					if(ctotal==null){
						bin_total.put(i, fraction);
						bin_count.put(i, 1);
					}else{
						int bc = bin_count.get(i)+1;
						bin_count.put(i, bc);
						bin_total.put(i,(ctotal+fraction));
						if(bc>max_editors){
							max_editors = bc;
						}
					}
				}

			}
			TreeMap<Integer, Float> t = new TreeMap<Integer, Float>(bin_total);

			try {
				FileWriter w = new FileWriter(outputfile);
				for(Entry<Integer, Float> e : t.entrySet()){
					w.write(e.getKey()+"\t"+e.getValue()+"\t"+e.getValue()/max_editors+"\t"+bin_count.get(e.getKey())+"\t"+e.getValue()/bin_count.get(e.getKey())+"\n");
					System.out.println(e.getKey()+"\t"+e.getValue()+"\t"+e.getValue()/max_editors+"\t"+bin_count.get(e.getKey())+"\t"+e.getValue()/bin_count.get(e.getKey()));
				}
				w.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}


	/**
	 * report the total fraction contributed to each gene wiki page by the most prolific author for that page
	 * @param limit
	 * @param serialized_wiktrust
	 * @param outputfile
	 * @param nobots
	 */
	public void measureTopAuthorContributions(int limit, String serialized_wiktrust, String outputfile, boolean nobots){
		File folder = new File(serialized_wiktrust);

		if(folder.isDirectory()){
			int n = 0;
			//	List<EditorInfo> e_info = new ArrayList<EditorInfo>();
			for(String geneid : folder.list()){
				//geneid = "1017";//cdk2 "7157"; //p53//insulin "3630";// "5649"; //reelin
				n++;
				if(n>limit){
					break;
				}
				GeneWikiPage page = GeneWikiUtils.deserializeGeneWikiPage(serialized_wiktrust+geneid);			
				//	System.out.println(page.getTitle());
				List<WikiTrustBlock> blocks = WikiTrustClient.getTrustBlocks(page.getPageContent());
				EditorInfo einfo = null;
				if(nobots){
					List<WikiTrustBlock> botless = new ArrayList<WikiTrustBlock>(blocks.size());
					for(WikiTrustBlock block : blocks){
						if(!(block.editor.contains("bot")||block.editor.contains("Bot"))){
							botless.add(block);
						}
					}
					einfo = summarizeEditorTrust(botless, page.getTitle());
				}else{
					einfo = summarizeEditorTrust(blocks, page.getTitle());
				}
				//	e_info.add(einfo);
				einfo.getRankInfo(outputfile);
			}


		}

	}

}