package org.genewiki.pbb;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.genewiki.pbb.mygeneinfo.MyGeneInfoParser;
import org.genewiki.pbb.util.FileHandler;
import org.genewiki.pbb.wikipedia.ProteinBox;
import org.joda.time.DateTime;
import org.gnf.util.BioInfoUtil;

public class Generator {

	private final MyGeneInfoParser parser;
	private ProteinBox box;
	private String symbol;
	public Generator() {
		parser = new MyGeneInfoParser();
	}
	
	public static void main(String[] args ) {
		Generator gen = new Generator();
		System.out.println(gen.generateStub("1017"));
	}
	
	/**
	 * Returns the wiki-formatted text for a ProteinBox template
	 * based on the supplied id.
	 * @param id
	 * @return 
	 */
	public String generateTemplate(String id) {
		try { Integer.parseInt(id);}
		catch (NumberFormatException e) {
			throw new RuntimeException("Invalid Entrez id.");
		}
		box = parser.parse(id);
		box = box.updateWith(box);
		return box.toString();
	}

	public ProteinBox getBox() {
		return box;
	}
	
	public String getSymbol() {
		return symbol;
	}

	/**
	 * Creates a stub for a specified Entrez gene id, using information from
	 * mygene.info (name, symbol, summary, chromosome position) and up to 10 reference citations
	 * from NCBI, with pmids that reference > 100 genes filtered out preemptively.
	 * @param id
	 * @return
	 */
	public String generateStub(String id) {
		JsonNode root = parser.getJsonForId(id);
		StringBuffer out = new StringBuffer();
		String sym = root.path("symbol").getTextValue();
		this.symbol = sym;
		String name = root.path("name").getTextValue();
		name = String.format("%s%s",Character.toUpperCase(name.charAt(0)),name.substring(1));
		String summary = root.path("summary").getTextValue();
		String chr = root.path("genomic_pos").path("chr").getTextValue();
		String entrezCite = "<ref name=\"entrez\">{{cite web | title = Entrez Gene: "+name+"| " +
				"url = http://www.ncbi.nlm.nih.gov/sites/entrez?db=gene&cmd=retrieve&list_uids="+id+"| accessdate = " +
						DateTime.now().toString()+"}}</ref>\n\n";
		
		out.append("{{PBB|geneid="+id+"}} \n");
		out.append("'''"+name+"''' is a [[protein]] that in humans is encoded by the "+sym+" [[gene]].");
		out.append(entrezCite);
		out.append(summary).append(entrezCite);
		out.append("==References== \n {{reflist}} \n");
		out.append("==Further reading == \n {{refbegin | 2}}\n");
		
		Map<String, List<String>> g2p = getGene2PubList();
		List<String> badpmids = filterPMIDsAboveLevel(g2p, 100);
		List<String> pmids = g2p.get(id);
		pmids.removeAll(badpmids);
		for (int i = 0; i < 10; i++ ) {
			try {
				out.append("{{Cite pmid|"+pmids.get(i)+"}}\n");
			} catch (IndexOutOfBoundsException e) {}
		}
		out.append("{{refend}}\n");
		String ins = (chr != null) ? chr+"-" : "";
		out.append("{{gene-"+ins+"stub}}"); // if chr. position isn't available, make it just {{gene-stub}}
		return out.toString();
	}
	
	/**
	 * Translates the gene2pubmed file into a map specifying gene:pubmed list
	 * @return
	 */
	public Map<String, List<String>> getGene2PubList() {
		FileHandler fh = new FileHandler(false);
		try {
			Map<String, List<String>> gene2pub = BioInfoUtil.getHumanGene2pub("/Users/eclarke/Desktop/gene2pubmed");
			return gene2pub;
		} catch (Exception e) {
			e.printStackTrace();
			return Collections.emptyMap();
		}
	}
	
	/**
	 * Returns a list of pmids that reference < or = to the specified number of genes.
	 * @param map
	 * @param level
	 * @return
	 */
	public List<String> filterPMIDsAboveLevel(Map<String, List<String>> map, int level) {
		Map<String, Integer> count = new HashMap<String, Integer>();
		for (String gene : map.keySet()) {
			for (String pmid : map.get(gene)) {
				Integer i = count.get(pmid);
				count.put(pmid, i = (i == null)? 1 : i+1);
			}
		}
		List<String> filtered = new ArrayList<String>();
		for (String pmid : count.keySet()) {
			Integer c = count.get(pmid);
			if (c <= level) 
				filtered.add(pmid);		
		}
		return filtered;
	}
	
}
