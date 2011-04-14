/**
 * 
 */
package org.gnf.genewiki.trust;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

/**
 * @author bgood
 *
 */
public class GeneWikiTrust {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//collectAllWikiTrustMarkup(1000000);

		collectAllWikiTrustRawData(1000000);
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
			List<NcboAnnotation> annos = AnnotatorClient.ncboAnnotateText(text, false, true, false, false, false, false);
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

}
