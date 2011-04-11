package org.gnf.genewiki;

import info.bliki.api.Page;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.gnf.go.GOmapper;
import org.gnf.go.GOowl;
import org.gnf.go.GOterm;
import org.gnf.umls.UmlsRelationship;

/**
 * This class is a collection of small, static utility methods used within the GeneWikiMiner project
 * @author bgood
 *
 */
public class GeneWikiUtils {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	//	System.out.println(toPlainText(" [[Protein-protein interaction|interact]]"));
	}

//	public static String toPlainText(String wikitext){
//		WikiModel wikiModel = new WikiModel(
//				Configuration.DEFAULT_CONFIGURATION, Locale.ENGLISH, "${image}", "${title}");
//		wikiModel.setUp();
//		String result = "";
//		try {
//			result = wikiModel.render(new PlainTextConverter(), wikitext);
//		} finally {
//			wikiModel.tearDown();
//		}
//		return result;
//	}
	
	public static List<String> getTitlesfromNcbiGenes(List<String> ncbis){
		Map<String, String> gene_wiki = new HashMap<String, String>();
		File in = new File("./gw_data/gene_wiki_index.txt");
		if(in.canRead()){
			gene_wiki = GeneWikiUtils.read2columnMap("./gw_data/gene_wiki_index.txt");
		}
		List<String> titles = new ArrayList<String>();
		for(String n : ncbis){
			if(gene_wiki.get(n)!=null){
				titles.add(gene_wiki.get(n));
			}else{
				System.err.println("No wiki title found for ncbi gene "+n);
			}
		}
		return titles;
	}

	public static Map<String, String> getGeneWikiGeneIndex(String index_file, boolean recalculate_all){
		Map<String, String> old_gene_wiki = new HashMap<String, String>();
		File in = new File(index_file);
		if(in.canRead()){
			try {
				BufferedReader f = new BufferedReader(new FileReader(index_file));
				String line = f.readLine().trim();
				while(line!=null){
					if(!line.startsWith("#")){
						String[] item = line.split("\t");
						if(item!=null&&item.length>1){
							if(old_gene_wiki.get(item[0])!=null){
								System.out.println(item[0]+" duplicated "+item[1]+" -- "+old_gene_wiki.get(item[1]));
							}
							old_gene_wiki.put(item[0], item[1]);
						}
					}
					line = f.readLine();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		WikiCategoryReader r = new WikiCategoryReader();
		List<Page> pages = r.getPagesWithPBB(1000000, 500);
		System.out.println("N pages now = "+pages.size()+" n have "+old_gene_wiki.size());
		ArrayList<String> t = new ArrayList<String>(old_gene_wiki.values());
		int n_have = 0; int n_new = 0;
		Map<String, String> gene_wiki = new HashMap<String, String>(old_gene_wiki);
		GeneWikiPage tmppage = new GeneWikiPage();
		for(Page p : pages){
			//check for new or changed titles
			if(!t.contains(p.getTitle())||recalculate_all){
				tmppage.setTitle(p.getTitle());
				tmppage.retrieveWikiTextContent();
				tmppage.parseAndSetNcbiGeneId();
//				//check if its a replacement title
//				if(old_gene_wiki.get(page.getNcbi_gene_id())!=null){
//					System.out.println("Replacing "+old_gene_wiki.get(page.getNcbi_gene_id())+" with "+p.getTitle()+" for "+page.getNcbi_gene_id());
//				}else{//or if its new
//					System.out.println("Adding new gene wiki page for "+p.getTitle()+" for "+page.getNcbi_gene_id());
//				}
				//check if we have two pages for the same ncbi gene id
				if(gene_wiki.get(tmppage.getNcbi_gene_id())!=null&&!recalculate_all){
					System.out.println(" 2 pages for "+tmppage.getNcbi_gene_id()+" "+p.getTitle()+" & "+gene_wiki.get(tmppage.getNcbi_gene_id()));
				}
				gene_wiki.put(tmppage.getNcbi_gene_id(), p.getTitle());
				n_new++;
				if(n_new%100==0){
					System.out.println(n_new);
				}
			}else{
				n_have++;
			}
			//reset
			tmppage.setTitle(""); tmppage.setPageContent("");tmppage.setNcbi_gene_id("");
		}
		System.out.println();
		if(gene_wiki.size()>10200){
			try {
				FileWriter f = new FileWriter(index_file);
				for(Entry<String, String> entry : gene_wiki.entrySet()){
					f.write(entry.getKey()+"\t"+entry.getValue()+"\n");
				}
				f.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Got "+n_new+" new ones and "+n_have+" old ones saving "+gene_wiki.size());
			return gene_wiki;
		}else{
			System.err.println("Probably an error re-generating the gene wiki index - only found: "+gene_wiki.size());
			return old_gene_wiki;
		}
	}

	public static Set<String> getGeneNames(List<GeneWikiPage> pages){
		Set<String> names = new HashSet<String>();
		for(GeneWikiPage page : pages){
			names.add(page.getTitle());
		}
		return names;
	}

	/**
	 * Retrieves wikipedia pages that use the PBB template and saves them to a 
	 * directory as serialized GeneWikiPages named after the NCBI gene id found in the PBB template.
	 * If the directory already contains a file with that gene id, the revision number is checked and the file is only updated if a new revision has been found.
	 * @param GeneWikiIndexFile
	 * @param wikipagedatadir
	 * @param limit
	 */

	public static void retrieveAndStoreGeneWikiAsJava(int limit){
		//loads an index linking gene wiki page titles to NCBI geneids
		WikiCategoryReader r = new WikiCategoryReader();
		List<Page> pages = r.getPagesWithPBB(limit, 500);
		System.out.println("N pages = "+pages.size());
		//checks to see which are already done
		File folder = new File(Config.gwikidir);
		List<String> done = new ArrayList<String>();
		for(String f : folder.list()){
			done.add(f);
		}
		int todo = pages.size();
		int i = 0; int n_new_articles = pages.size()-done.size(); int n_altered_articles = 0;
		System.out.println("N new: "+n_new_articles);
		for(Page p : pages){
			i++;
			GeneWikiPage page = new GeneWikiPage(p.getTitle());
			page.retrieveWikiTextContent();
			page.parseAndSetNcbiGeneId();
			String geneid = page.getNcbi_gene_id();
			//if we have it, check for a revision
			String s_rev = "";
			String c_rev = "";
			if(done.contains(geneid)){
				GeneWikiPage stored = deserializeGeneWikiPage(Config.gwikidir+geneid);
				s_rev = stored.getRevid();
				c_rev = page.getRevid();
				//	System.out.println(s_rev+" <stored current> "+c_rev);
			}
			if(!(c_rev.equals(s_rev))||(!done.contains(geneid))){
				n_altered_articles++;
				if(i>limit){
					break;
				}
				//then go get it!
				boolean gotext = page.defaultPopulate();
				if(gotext){
					page.retrieveAllInBoundWikiLinks(true, false);
					GeneWikiUtils.saveToFile(page, Config.gwikidir+geneid);									
					System.out.println("done "+i+" of "+todo+"\t"+page.getTitle()+"\t"+geneid);
				}else{
					System.out.println("failed to get text for "+page.getTitle()+"\t"+geneid);
				}
			}
		}
		System.out.println("Finished Gene Wiki update. Articles added since last update: "+n_new_articles+".  Articles edited since last update: "+n_altered_articles);
	}

	/**
	 * Reads the gene wiki index file (ncbi gene id \t wikipedia page title) and 
	 * populates a directory with serialized GeneWikiPageData objects
	 * @param GeneWikiIndexFile
	 * @param wikipagedatadir
	 * @param limit
	 */

	public static void retrieveAndStoreGeneWikiAsJavaFromGWindex(int limit){
		//loads an index linking gene wiki page titles to NCBI geneids
		HashMap<String, String> gene_wiki = GeneWikiUtils.read2columnMap(Config.article_gene_map_file);
		//checks to see which are already done
		File folder = new File(Config.gwikidir);
		List<String> done = new ArrayList<String>();
		for(String f : folder.list()){
			done.add(f);
		}
		int todo = gene_wiki.size()-done.size();
		int i = 0;
		for(Entry<String, String> entry : gene_wiki.entrySet()){
			String geneid = entry.getKey();
			String title = entry.getValue();
			if(!done.contains(geneid)){
				if(i>limit){
					break;
				}
				//then go get it!
				GeneWikiPage page = new GeneWikiPage(title);
				boolean gotext = page.defaultPopulate();
				if(gotext){
					page.retrieveAllInBoundWikiLinks(true, false);
					page.setNcbi_gene_id(geneid);
					GeneWikiUtils.saveToFile(page, Config.gwikidir+geneid);									
					i++;
					System.out.println("done "+i+" of "+todo+"\t"+title+"\t"+geneid);
				}else{
					System.out.println("failed TO get text for "+title+"\t"+geneid);
				}
			}

		}
	}

	/**
	 * Given the path to a serialized Java GeneWikiPageData object, deserialize that object and set this GeneWikiPageData object to the read object.
	 * @param objfile
	 */
	public static GeneWikiPage deserializeGeneWikiPage(String objfile){
		GeneWikiPage data = (GeneWikiPage)readObjectFromFile(objfile);
		return data;
	}

	/**
	 * Store the GeneWikiData for this page as java bytecode
	 * @param objfile
	 */
	public static void serializeData(GeneWikiPage page, String objfile){
		GeneWikiUtils.saveToFile(page, objfile);
	}

	/**
	 * This can be used to make sure all the objects in a directory display current verisons of the parsing.
	 */
	public static void updatePageObjects(){
		//make sure most recent text indexing procedures run on cached pages
		int limit = 1000000;
		List<GeneWikiPage> pages = GeneWikiUtils.loadSerializedDir("/Users/bgood/data/genewiki/intermediate/javaobj-original/", limit);
		//		List<GeneWikiPage> pages = new ArrayList<GeneWikiPage>();
		//		GeneWikiPage test = GeneWikiUtils.deserializeGeneWikiPage(Config.gwikidir+"/10018");
		//		pages.add(test);
		String outdir = "/Users/bgood/data/genewiki/intermediate/javaobj/";
		for(GeneWikiPage page : pages){
			page.setReferences();
			page.parseAndSetSentences();
			int nlinks = page.getGlinks().size();
			System.out.println("page "+page.getTitle()+" nlinks "+nlinks);
			//add the redundant ones with the right indexes
			page.setGeneWikiLinksByHeadings();
			for(int i=0; i<nlinks; i++){
				GeneWikiLink updated = page.getGlinks().get(i+nlinks);
				page.getGlinks().get(i).setStartIndex(updated.startIndex);

			}
			for(int i=page.getGlinks().size(); i>nlinks; i--){
				page.getGlinks().remove(i-1);
			}	


			serializeData(page, outdir+page.getNcbi_gene_id());
		}
	}

	/**
	 * Leaves out bulk of information not directly related to generating candidation annotations
	 * @param dir
	 * @param out
	 * @param limit
	 */
	public static void slimSerializedGenePages(String dir, String out, int limit){
		File folder = new File(dir);
		if(folder.isDirectory()){
			int n = 0;
			for(String title : folder.list()){
				n++;
				GeneWikiPage page = deserializeGeneWikiPage(dir+title);
				page.setInglinks(null);
				page.setPageContent(null);
				List<GeneWikiLink> glinks = new ArrayList<GeneWikiLink>();
				for(GeneWikiLink glink : page.getGlinks()){
					glink.setParagraph(null);
					glink.setSentence(null);
					glink.setPretext(null);
					glink.setPosttext(null);
					glinks.add(glink);
				}
				page.setGlinks(glinks);
				saveToFile(page,out+title);
				if(n > limit){
					break;
				}
				if(n%100==0){
					System.out.print("loaded "+n+"\t");
				}
				if(n%1000==0){
					System.out.println("");
				}
			}
		}else{
			System.out.println(dir +" not a dir");
		}
	}


	public static GOterm presentInGOtermList(GeneWikiPage page, List<GOterm> terms){
		if(page==null||page.getWikisynset()==null){
			return null;
		}
		for(String test : page.getWikisynset()){
			GOterm match = presentInGOtermList(test, terms);
			if(match!=null){	
				return match;
			}
		}
		return null;
	}

	public static GOterm presentInGOtermList(String test, List<GOterm> terms){
		test = normalize(test);
		//go terms should already be normalized when they are loaded
		for(GOterm t : terms){
			if(t.getSynset_exact().contains(test)){
				t.setEvidence("synonym_exact");
				return t;
			}else if(t.getSynset_narrower().contains(test)){
				t.setEvidence("synonym_narrower");
				return t;
			}else if(t.getSynset_broader().contains(test)){
				t.setEvidence("synonym_broader");
				return t;
			}else if(t.getSynset_related().contains(test)){
				t.setEvidence("synonym_related");
				return t;
			}
		}
		return null;
	}

	public static List<GOterm> allPresentInGOtermList(GeneWikiPage page, List<GOterm> terms){
		List<GOterm> matches = new ArrayList<GOterm>();
		if(page==null||page.getWikisynset()==null){
			return null;
		}
		for(String test : page.getWikisynset()){
			List<GOterm> match = allPresentInGOtermList(test, terms);
			if(match!=null){
				String evidence = "title";
				if(!matches(page.getTitle(), test)){
					evidence = "redirect";
				}
				for(GOterm mt : match){
					mt.setEvidence(evidence+"_"+mt.getEvidence());
				}
				matches.addAll(match);
			}
		}
		if(matches.size()>0){
			return matches;
		}
		return null;
	}

	public static List<GOterm> allPresentInGOtermList(String test, List<GOterm> terms){
		List<GOterm> matches = new ArrayList<GOterm>();
		test = normalize(test);
		//go terms should already be normalized when they are loaded
		for(GOterm t : terms){
			if(t.getTerm().equals(test)){
				t.setEvidence("_term_match");
				matches.add(t);
			}else if(t.getSynset_exact().contains(test)){
				t.setEvidence("synonym_exact");
				matches.add(t);
			}else if(t.getSynset_narrower().contains(test)){
				t.setEvidence("synonym_narrower");
				matches.add(t);
			}else if(t.getSynset_broader().contains(test)){
				t.setEvidence("synonym_broader");
				matches.add(t);
			}else if(t.getSynset_related().contains(test)){
				t.setEvidence("synonym_related");
				matches.add(t);
			}
		}
		if(matches.size()>0){
			return matches;
		}
		return null;
	}

	public static List<GOterm> getGOTermsFromDbFiles(String dbdir){
		List<GOterm> gos = new ArrayList<GOterm>();
		//String dbdir = "C:\\Users\\bgood\\data\\ontologies\\go_daily-termdb-tables\\";
		String termfile = dbdir+"term.txt";
		String synfile = dbdir+"term_synonym.txt";
		Map<String, GOterm> id_term = new HashMap<String, GOterm>();
		//GO synonym types
		String EXACT = "19"; //was "18";
		String BROAD = "40"; //was "39";
		String NARROW = "44"; //was "43";
		String RELATED = "71"; //"70";
		try {
			BufferedReader f = new BufferedReader(new FileReader(termfile));
			String line = f.readLine().trim();
			while(line!=null){
				String[] item = line.split("\t");
				if(item!=null&&item.length>1){
					String id = item[0];					
					GOterm t = new GOterm(item[0], item[3], item[2], normalize(item[1]));
					id_term.put(id, t);					

				}
				line = f.readLine();
			}
			f.close();
			System.out.println("loaded go terms");
			f = new BufferedReader(new FileReader(synfile));
			line = f.readLine().trim();
			while(line!=null){
				String[] item = line.split("\t");
				if(item!=null&&item.length>1){
					String id = item[0];					
					GOterm t = id_term.get(id);
					if(t == null){
						System.out.println("Couldn't find id from syn "+id);
						continue;
					}else{
						//for different synonym types
						if(item[3].equals(EXACT)){
							t.getSynset_exact().add(normalize(item[1]));		
						}else if(item[3].equals(BROAD)){
							t.getSynset_broader().add(normalize(item[1]));		
						}else if(item[3].equals(NARROW)){
							t.getSynset_narrower().add(normalize(item[1]));		
						}else if(item[3].equals(RELATED)){
							t.getSynset_related().add(normalize(item[1]));		
						}
					}
				}
				line = f.readLine();
			}
			f.close();
			System.out.println("loaded go synonyms");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(Entry<String, GOterm> entry : id_term.entrySet()){
			GOterm t = entry.getValue();
			gos.add(t);
			//			System.out.println(t.getId()+" "+t.getAccession()+" "+t.getRoot()+" "+t.getTerm()+" : "+t.getSynset());
		}

		return gos;
	}

	public static Set<GeneWikiPage> getNRlinks(int limit, String gwikidir){
		Set<GeneWikiPage> links = new HashSet<GeneWikiPage>();
		List<GeneWikiPage> pages = GeneWikiUtils.loadSerializedDir(gwikidir, limit);
		System.out.println("read in wikigene files");
		int linkcount = 0;
		for(GeneWikiPage page : pages){
			if(page!=null&&page.getGlinks()!=null){
				for(GeneWikiLink glink : page.getGlinks()){
					links.add(new GeneWikiPage(glink.getTarget_page()));
					linkcount++;
				}
			}
		}
		System.out.println("Total links: "+linkcount);
		/*	for(GeneWikiPageData d : links){
			System.out.println(d.getTitle());
		}
		 */
		return links;
	}


	public static void saveToFile(Serializable obj, String filename){
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try    {
			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);
			out.writeObject(obj);
			out.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}

	public static Object readObjectFromFile(String filename){
		Object obj = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;

		try {
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			try{
				obj = in.readObject();
			}catch(RuntimeException re){
				System.out.println("failed to read "+filename);
			}
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		   
		return obj;
	}

	public static List<GeneWikiPage> loadSerializedDir(String dir, int limit){
		List<GeneWikiPage> pages = new ArrayList<GeneWikiPage>();
		File folder = new File(dir);
		if(folder.isDirectory()){
			int n = 0;
			for(String title : folder.list()){
				if(title.startsWith(".")){
					continue;
				}
				n++;
				GeneWikiPage page = deserializeGeneWikiPage(dir+title);
				pages.add(page);
				if(n > limit){
					break;
				}
				if(n%100==0){
					System.out.print("loaded "+n+"\t");
				}
				if(n%1000==0){
					System.out.println("");
				}

			}
		}else{
			System.out.println(dir +" not a dir");
			return null;
		}
		return pages;
	}

	public static List<UmlsRelationship> getCachedUmls(String pagetitle){
		if(pagetitle.contains(":")||pagetitle.contains("\\")||pagetitle.contains("*")||pagetitle.contains("#")){
			return null;
		}
		List<UmlsRelationship> rels = new ArrayList<UmlsRelationship>();
		String datadir = "C:\\Users\\bgood\\data\\genewiki\\umls_tab\\";
		try {
			BufferedReader f = new BufferedReader(new FileReader(datadir+pagetitle));
			String line = f.readLine();
			while(line!=null){
				UmlsRelationship r = null;
				String[] data = line.split("\t");
				String source = data[0];
				if(data[1].equals("no exact match")){
					return null;
				}else{
					String vocab = data[1].split(":")[0];
					String reltype = data[1].split(":")[1];
					String target = data[2];
					String target_text = data[3];
					r = new UmlsRelationship(source, reltype, vocab, target, target_text);
				}
				rels.add(r);
				line = f.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(rels.size()>0){
			return rels;
		}else{
			return null;
		}

	}


	public static HashMap<String, String> read2columnMap(String file){
		HashMap<String, String> map = new HashMap<String, String>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(file));
			String line = f.readLine().trim();
			while(line!=null){
				if(!line.startsWith("#")){
					String[] item = line.split("\t");
					if(item!=null&&item.length>1){
						map.put(item[0], item[1]);
					}
				}
				line = f.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}

	public static HashMap<String, Set<String>> readMapFromTable(String file, String delimiter, String skipprefix, int keycol, int valuecol){
		HashMap<String,Set<String>> map = new HashMap<String, Set<String>>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(file));
			String line = f.readLine().trim();
			while(line!=null){
				if(!line.startsWith(skipprefix)){
					String[] item = line.split(delimiter);
					if(item!=null&&item.length>1){
						Set<String> mapped = map.get(item[0]);
						if(mapped == null){
							mapped = new HashSet<String>();
							map.put(item[keycol], mapped);
						}
						mapped.add(item[valuecol]);
					}
				}
				line = f.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}

	public static HashMap<String, Set<String>> readMapFile(String file, String delimiter, String skipprefix, boolean reverse){
		HashMap<String,Set<String>> map = new HashMap<String, Set<String>>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(file));
			String line = f.readLine().trim();
			while(line!=null){
				if(!line.startsWith(skipprefix)){
					String[] item = line.split(delimiter);
					if(item!=null&&item.length>1){
						if(reverse){
							Set<String> mapped = map.get(item[1]);
							if(mapped == null){
								mapped = new HashSet<String>();
								map.put(item[1], mapped);
							}
							mapped.add(item[0]);
						}else{
							Set<String> mapped = map.get(item[0]);
							if(mapped == null){
								mapped = new HashSet<String>();
								map.put(item[0], mapped);
							}
							mapped.add(item[1]);
						}
					}
				}
				line = f.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}

	public static HashMap<String, Set<GOterm>> filterGoMapByEvidence(HashMap<String, Set<GOterm>> gene_gos, String evidence){
		HashMap<String, Set<GOterm>> e = new HashMap<String, Set<GOterm>>();
		for(Entry<String, Set<GOterm>> entry : gene_gos.entrySet()){
			Set<GOterm> k = new HashSet<GOterm>();
			Set<GOterm> u = new HashSet<GOterm>();
			for(GOterm goterm : entry.getValue()){
				if(goterm.getEvidence().equals(evidence)){
					k.add(goterm);
				}else{
					u.add(goterm);
				}
			}
			if(k.size()>0){
				e.put(entry.getKey(), k);
			}
		}
		return e;
	}
	public static HashMap<String, Set<GOterm>> readGenewiki2GOMapped(String file){

		String delimiter = "\t";
		HashMap<String,Set<GOterm>> map = new HashMap<String, Set<GOterm>>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(file));
			String line = f.readLine().trim();
			while(line!=null){
				String[] item = line.split(delimiter);
				if(item!=null&&item.length>1){
					String exists = item[0]; String gene_page = item[1]; 
					String go_acc = item[2]; String go_term = item[3]; String root = item[4];

					Set<GOterm> GOs = map.get(gene_page);
					if(GOs == null){
						GOs = new HashSet<GOterm>();
						map.put(gene_page, GOs);
					}

					GOterm go = new GOterm(null, go_acc, root, go_term);
					go.setEvidence(exists);
					if(!GOs.add(go)){
						System.out.println("duplicate go: "+gene_page+" "+go);						
					}
				}

				line = f.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}

	public static HashMap<String, GOterm> readGenewiki2GO(String file, String delimiter, String skipprefix){
		HashMap<String,GOterm> map = new HashMap<String, GOterm>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(file));
			String line = f.readLine();
			while(line!=null){
				if(!line.startsWith(skipprefix)){
					String[] item = line.split(delimiter);
					if(item!=null&&item.length>1){
						String geneid = item[0];					
						GOterm GO = map.get(geneid);
						if(GO == null){
							String id = item[1]; String acc = item[2]; String root = item[3]; String term = item[4];
							if(root.equals("cellular_component")){
								root = "Component";
							}else if(root.equals("biological_process")){
								root = "Process";
							}else if(root.equals("molecular_function")){
								root = "Function";
							}
							GOterm go = new GOterm(id, acc, root, term);
							map.put(geneid, go);
						}else{
							System.out.println("error reading genewiki to go file:"+file);
							return null;
						}
					}
				}
				line = f.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}


	public static Map<String, Set<GOterm>> convertWikiTitlesToGeneIds(Map<String, HashSet<GOterm>> page_gos){
		Map<String, Set<GOterm>> geneid_gos = new HashMap<String, Set<GOterm>>();
		String file = Config.article_gene_map_file;
		String delimiter = "\t";
		String skipprefix = "#";
		boolean reverse = true;
		Map<String, Set<String>> article_geneid = GeneWikiUtils.readMapFile(file, delimiter, skipprefix, reverse);
		System.out.println("read article gene map ");
		//map article names to gene ids

		for(Entry<String,HashSet<GOterm>> entry : page_gos.entrySet()){
			Set<String> genids = article_geneid.get(entry.getKey());
			if(genids!=null){
				for(String id :genids){
					if(id==null||id.length()<2){
						System.out.println("missing wiki2geneid map for "+entry.getKey());
						System.out.println("");
					}else{
						geneid_gos.put(id, entry.getValue());
					}
				}
			}
		}
		return geneid_gos;
	}

	public static HashMap<String, Set<GOterm>> readGene2GO(String file, String delimiter, String skipprefix){
		HashMap<String,Set<GOterm>> map = new HashMap<String, Set<GOterm>>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(file));
			String line = f.readLine().trim();
			while(line!=null){
				if(!line.startsWith(skipprefix)){
					String[] item = line.split(delimiter);
					if(item!=null&&item.length>1){
						String geneid = item[1];					
						Set<GOterm> GOs = map.get(geneid);
						if(GOs == null){
							GOs = new HashSet<GOterm>();
							map.put(geneid, GOs);
						}
						String id = null; String acc = item[2];
						String term = ""; String root = "";
						if(item.length>5){
							term = item[5];
							if(item.length>7){
								root = item[7];
							}
						}
						GOterm go = new GOterm(id, acc, root, term);
						go.setEvidence("ncbi");
						GOs.add(go);//the text term for the go id						
					}
				}
				line = f.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}

	public static HashMap<String, Set<GOterm>> readGene2GO(String gene2go, boolean addParents){
		//read in gene to go ~term map
		String delimiter = "\t";
		String skipprefix = "#";
		HashMap<String, Set<GOterm>> geneid_go = GeneWikiUtils.readGene2GO(gene2go, delimiter, skipprefix);
		System.out.println("read geneid_go map");

		//add ancestors to geneid_go map
		if(addParents){
			GOowl gol = new GOowl();
			gol.initFromFileRDFS(true);
			HashMap<String, Set<GOterm>> tmp_geneid_go = new HashMap<String, Set<GOterm>>();
			for(Entry<String, Set<GOterm>> entry : geneid_go.entrySet()){
				String geneid = entry.getKey();
				Set<GOterm> p = new HashSet<GOterm>();
				for(GOterm goterm : entry.getValue()){
					p.add(goterm);
					Set<GOterm> parents = gol.getSupers(goterm);
					if(parents!=null){
						p.addAll(parents);
					}
				}
				tmp_geneid_go.put(geneid, p);
			}
			geneid_go = tmp_geneid_go;
			gol.close();
		}
		return geneid_go;
	}

	public static HashMap<String, Set<String>> readCytoscape(String file, String delimiter, String skipprefix){
		HashMap<String,Set<String>> map = new HashMap<String, Set<String>>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(file));
			String line = f.readLine().trim();
			while(line!=null){
				if(!line.startsWith(skipprefix)){
					String[] item = line.split(delimiter);
					if(item!=null&&item.length>1){
						String geneid = item[0];					
						Set<String> GOs = map.get(geneid);
						if(GOs == null){
							GOs = new HashSet<String>();
							map.put(geneid, GOs);
						}
						GOs.add(item[1]);//the text term for the linked node						
					}
				}
				line = f.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}

	public static boolean matches(String s1, String s2){
		s1 = normalize(s1);
		s2 = normalize(s2);
		return s1.equals(s2);
	}

	//return true if any of the items in the synset match s2
	public static boolean synMatches(Set<String> synset, String s2){
		boolean match = false;
		s2 = normalize(s2);
		for(String syn :synset){
			if(s2.equals(normalize(syn))){
				return true;
			}
		}
		return match;
	}

	public static String normalize(String s){
		s = s.replace(' ', '_');
		s = s.replace(',', '_');
		s = s.replace('\'', '_');
		s = s.toLowerCase();
		return s;
	}

	/***
	 * read an annotation file and return the gene ids that it contains
	 */

	public static Set<String> getDoneGeneIds(String annooutfile){
		Set<String> done = new HashSet<String>();
		BufferedReader f;
		try {
			File t = new File(annooutfile);
			if(!t.canRead()){
				return done;
			}
			FileReader fr = new FileReader(annooutfile);

			f = new BufferedReader(fr);
			String line = f.readLine();
			line = f.readLine();//skip header
			while(line!=null){
				String[] item = line.split("\t");
				done.add(item[0]);
				line = f.readLine();
			}
			f.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return done;
	}
}
