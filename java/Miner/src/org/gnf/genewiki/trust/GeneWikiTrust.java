/**
 * 
 */
package org.gnf.genewiki.trust;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.gnf.genewiki.Config;
import org.gnf.genewiki.GeneWikiLink;
import org.gnf.genewiki.GeneWikiPage;
import org.gnf.genewiki.GeneWikiUtils;
import org.gnf.genewiki.Heading;
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
		wt.measureAuthorContributions(1000000, "/Users/bgood/data/bioinfo/gene_wikitrust_as_java/", "/Users/bgood/data/NARupdate2011/editor_info", true);
		//		collectAllWikiTrustRawData(1000000);
		//		GeneWikiPage p = GeneWikiUtils.deserializeGeneWikiPage(Config.gwroot+"intermediate/javaobj_wt/10000");
		//		System.out.println("size "+p.getSize());
		//		List<CandidateAnnotation> annos = GeneWikiPageMapper.annotateArticleNCBO(p, false, true, true, true, false);
		//		System.out.println(CandidateAnnotation.getHeader());
		//		for(CandidateAnnotation anno : annos){
		//			System.out.println(anno);
		//		}
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
		//trusts for all edits
		Map<String, List<Double>> user_trusts;
		//number revisions saved
		Map<String, Set<Integer>> user_revs;
		//amount of text touched
		Map<String, Integer> user_texts;


		public EditorInfo(Map<String, List<Double>> user_trusts,
				Map<String, Set<Integer>> user_revs,
				Map<String, Integer> user_texts) {
			super();
			this.user_trusts = user_trusts;
			this.user_revs = user_revs;
			this.user_texts = user_texts;
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

		public String getAllAsString(){
			String trusty ="User\tMean trust for page\n";
			for(Entry<String, List<Double>> ut : user_trusts.entrySet()){
				double m = 0;
				for(Double t : ut.getValue()){
					m+=t;
				}
				m = m/(double)ut.getValue().size();
				trusty+=ut.getKey()+"\t"+m+"\n";
			}
			trusty+="\nUser\tRevisions\n";
			for(Entry<String, Set<Integer>> rev : user_revs.entrySet()){
				trusty+=rev.getKey()+"\t"+rev.getValue().size()+"\n";
			}
			trusty+="\nUser\tLength of text touched\n";
			for(Entry<String, Integer> text : user_texts.entrySet()){
				trusty+=text.getKey()+"\t"+text.getValue()+"\n";
			}
			return trusty;
		}

	}
	public EditorInfo summarizeEditorTrust(List<WikiTrustBlock> blocks){
		//trusts for all edits
		Map<String, List<Double>> user_trusts = new HashMap<String, List<Double>>();
		//number revisions saved
		Map<String, Set<Integer>> user_revs = new HashMap<String, Set<Integer>>();
		//amount of text touched
		Map<String, Integer> user_texts = new HashMap<String, Integer>();
		for(WikiTrustBlock tb : blocks){
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
		}
		return new EditorInfo(user_trusts, user_revs, user_texts);
	}

	public void measureAuthorContributions(int limit, String serialized_wiktrust, String outputfile, boolean nobots){
		File folder = new File(serialized_wiktrust);

		if(folder.isDirectory()){
			int n = 0;
			List<EditorInfo> e_info = new ArrayList<EditorInfo>();
			for(String geneid : folder.list()){
				n++;
				if(n>limit){
					break;
				}
				GeneWikiPage page = GeneWikiUtils.deserializeGeneWikiPage(serialized_wiktrust+geneid);				
				List<WikiTrustBlock> blocks = WikiTrustClient.getTrustBlocks(page.getPageContent());
				if(nobots){
					List<WikiTrustBlock> botless = new ArrayList<WikiTrustBlock>(blocks.size());
					for(WikiTrustBlock block : blocks){
						if(!(block.editor.contains("bot")||block.editor.contains("Bot"))){
							botless.add(block);
						}
					}
					e_info.add(summarizeEditorTrust(botless));
				}else{
					e_info.add(summarizeEditorTrust(blocks));
				}
			}
			Map<Integer, Float> bin_total = new HashMap<Integer, Float>();
			Map<Integer, Integer> bin_count = new HashMap<Integer, Integer>();
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
						Float f = new Float(fraction);
						bin_total.put(i, f);
						bin_count.put(i, 1);
					}else{
						int bc = bin_count.get(i)+1;
						bin_count.put(i, bc);
						bin_total.put(i,(ctotal+fraction));
					}
				}

			}
			TreeMap<Integer, Float> t = new TreeMap<Integer, Float>(bin_total);
			for(Entry<Integer, Float> e : t.entrySet()){
				System.out.println(e.getKey()+"\t"+e.getValue()/n);
			}

			//			try {
			//				FileWriter w = new FileWriter(outputfile, true);
			//				
			//				w.close();
			//			} catch (IOException e) {
			//				// TODO Auto-generated catch block
			//				e.printStackTrace();
			//			}
		}

	}
}
