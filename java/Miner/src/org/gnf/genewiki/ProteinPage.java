/**
 * 
 */
package org.gnf.genewiki;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gnf.wikiapi.Page;

/**
 * Articles in Wikipedia that specifically describe a protein but do not include the gene wiki template..
 * Definign feature is this template
 * {{protein
| Name = natriuretic peptide precursor A
| caption = 
| image = ANP-structure.jpg
| width = 
| HGNCid = 7939
| Symbol = NPPA
| AltSymbols = ANP, PND
| EntrezGene = 4878
| OMIM = 108780
| RefSeq = NM_006172
| UniProt = P01160
| PDB = 1ANP
| ECnumber = 
| Chromosome = 1
| Arm = p
| Band = 36.21
| LocusSupplementaryData = 
}}
 * @author bgood
 *
 */
public class ProteinPage extends GeneWikiPage{	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String credfile = "/Users/bgood/workspace/Config/gw_creds.txt";
		retrieveAndStoreAllAsJava(100000,credfile,"/Users/bgood/data/bioinfo/protein_template_pages/", false);
		Map<String, GeneWikiPage> gene_wiki = GeneWikiUtils.loadSerializedDir("/Users/bgood/data/bioinfo/gene_wiki_as_java", 10000000);
		Map<String, ProteinPage> done = loadSerializedDir("/Users/bgood/data/bioinfo/protein_template_pages/", 1000000000);
		int all_with_id = 0; int overlapping = 0;
		int total = 942;
		for(Entry<String, ProteinPage> id_page : done.entrySet()){
			all_with_id++;
			String id  = id_page.getKey();
			if(gene_wiki.containsKey(id)){
				overlapping++;
			}			
		}
		System.out.println("total protein template pages = "+total+"\n"+
				"with ncbi gene id = "+all_with_id+"\n"+
				" overlapping with gene wiki = "+overlapping);
	}

	@Override
	public void parseAndSetNcbiGeneId(){
		if(this.getPageContent()==null){
			return;
		}
		String reg = "EntrezGene.{0,5}=.{0,5}(\\d+)";
		Pattern pattern = 
			Pattern.compile(reg);
		Matcher matcher = 
			pattern.matcher(this.getPageContent());
		String id = "";
		if (matcher.find()) {
			id = matcher.group().trim();
			id = id.substring(id.indexOf("=")+1);
			id = id.trim();
			setNcbi_gene_id(id);
		}else{
			System.out.println("no gene id "+getTitle());
		}
	}

	public static void retrieveAndStoreAllAsJava(int limit, String credfile, String directory, boolean usewikitrust){
		//loads an index linking gene wiki page titles to NCBI geneids
		WikiCategoryReader r = new WikiCategoryReader(credfile);
		List<Page> pages = r.getPagesPlusRevidsWithTemplate("Template:protein",limit, 500);
		System.out.println("N pages = "+pages.size());
		//checks to see which are already done
		Map<String, ProteinPage> done = loadSerializedDir(directory, 1000000000);
		Map<String, String> title_id = new HashMap<String, String>();
		for(Entry<String, ProteinPage> gene_wiki : done.entrySet()){
			title_id.put(gene_wiki.getValue().getTitle(), gene_wiki.getKey());
		}
		int todo = pages.size();
		int i = 0; int n_new_articles = pages.size()-done.size(); int n_altered_articles = 0;
		System.out.println("N new: "+n_new_articles);
		for(Page p : pages){
			i++;
			String title = p.getTitle();
			//if we have it, check for a revision
			String s_rev = "";
			String c_rev = "";
			if(title_id.keySet().contains(title)){
				ProteinPage stored = done.get(title_id.get(title));
				s_rev = stored.getRevid();			
				c_rev = p.getCurrentRevision().getRevid();
				//	System.out.println(s_rev+" <stored current> "+c_rev);
			}
			if(!(c_rev.equals(s_rev))||(!title_id.keySet().contains(title))){
				n_altered_articles++;
				if(i>limit){
					break;
				}
				//then go get it!
				ProteinPage page = new ProteinPage();
				page.setTitle(title);
				boolean gotext = false;
				if(usewikitrust){
					gotext = page.defaultPopulateWikiTrust();
				}else{
					gotext = page.defaultPopulate();
				}
				page.parseAndSetNcbiGeneId();
				String geneid = page.getNcbi_gene_id();
				if(gotext&&geneid!=null&&!geneid.equals("")&&geneid.length()>0){
					GeneWikiUtils.saveToFile(page, directory+geneid);									
					System.out.println("done "+i+" of "+todo+"\t"+page.getTitle()+"\t"+geneid);
				}else{
					System.out.println("failed to get text for "+page.getTitle()+"\t or id "+geneid);
				}
			}
		}
		System.out.println("Finished {protein} update. Articles added since last update: "+n_new_articles+".  Articles edited since last update: "+n_altered_articles);
	}

	public static Map<String, ProteinPage> loadSerializedDir(String dir, int limit){
		Map<String, ProteinPage> pages = new HashMap<String,ProteinPage>();
		File folder = new File(dir);
		if(folder.isDirectory()){
			int n = 0;
			for(String title : folder.list()){
				if(title.startsWith(".")){
					continue;
				}
				n++;
				File f = new File(dir+title);
				ProteinPage page = null;
				if(f.exists()){
					page = (ProteinPage)GeneWikiUtils.readObjectFromFile(dir+title);
					pages.put(page.getNcbi_gene_id(), page);
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

			}
		}else{
			System.out.println(dir +" not a dir");
			return null;
		}
		return pages;
	}
}
