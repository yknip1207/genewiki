package org.genewiki;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import org.codehaus.jackson.JsonNode;
import org.genewiki.pbb.mygeneinfo.MyGeneInfoParser;
import org.genewiki.pbb.wikipedia.ProteinBox;
import org.genewiki.pbb.util.FileHandler;
import org.genewiki.util.Serialize;
import org.gnf.util.BioInfoUtil;
import org.joda.time.DateTime;
import org.joda.time.Days;

public class Generator {

	private final MyGeneInfoParser parser;
	private ProteinBox box;
	private String symbol;
	public Generator() {
		parser = new MyGeneInfoParser();
	}
	
//	public static void main(String[] args ) {
//		Generator gen = new Generator();
//		GeneToPubmedDB db = new GeneToPubmedDB();
////		db.init();
////		db.populate(gen.getGene2PubList());
//		System.out.println(db.getPubmedsForGene("716").size());
//		System.out.println(db.getFilteredPubmedsForGene("716", 40).size());
//	}
	
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
		GeneToPubmedDB db = new GeneToPubmedDB();
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
		
//		Map<String, List<String>> g2p = getGene2PubList();
//		List<String> badpmids = filterPMIDsAboveLevel(g2p, 100);
		List<String> pmids = db.getFilteredPubmedsForGene(id, 100);
//		pmids.removeAll(badpmids);
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
	 * Translates the gene2pubmed file into a map specifying gene:pubmed list. If a serialized
	 * copy of the gene2pub map is already available, and the serialized copy was made less
	 * than 30 days ago, it will use that instead.
	 * @return map of genes to the pubmed ids that reference them
	 */

	@SuppressWarnings("unchecked")
	public Map<String, List<String>> getGene2PubList() {
		Map<String, List<String>> gene2pub = null;
		try {
			DateTime now = DateTime.now();
			DateTime lastSerialized = (DateTime) Serialize.in("last_g2p_serialization.datetime.ser");
			if (Days.daysBetween(lastSerialized, now).getDays() < 30) {
				gene2pub = (Map<String, List<String>>) Serialize.in("gene2pub.map.string_list.string.ser");
			} else {
				throw new FileNotFoundException("");
			}
		} catch (FileNotFoundException e) {
			if (new File("gene2pubmed").exists()) {
				gene2pub = BioInfoUtil.getHumanGene2pub("gene2pubmed");
			} else {
				String jarRoot = "";
				try { jarRoot = new FileHandler(true).getRoot().getCanonicalPath(); }
				catch (IOException ioe) { ioe.printStackTrace(); }
				gene2pub = BioInfoUtil.getHumanGene2pub(jarRoot+"/gene2pubmed");
			}	
			Serialize.out("last_g2p_serialization.datetime.ser", DateTime.now());
			Serialize.out("gene2pub.map.string_list.string.ser", new HashMap<String, List<String>>(gene2pub));
		}
		return gene2pub;
	}
	
	
	
	/**
	 * Returns a list of pubmed ids that reference no more than the specified number of genes.
	 * @param map
	 * @param level
	 * @return
	 */
	@Deprecated
	public List<String> filterPMIDsAboveLevel(Map<String, List<String>> gene2pubmed, int level) {
		Map<String, Integer> count = new HashMap<String, Integer>();
		for (String gene : gene2pubmed.keySet()) {
			for (String pmid : gene2pubmed.get(gene)) {
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

class GeneToPubmedDB {
	
	private final String dbName = "g2p.db";
	
	public void init() {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection con = DriverManager.getConnection("jdbc:sqlite:"+dbName);
			Statement stat = con.createStatement();
			stat.executeUpdate("drop table if exists genes");
			stat.executeUpdate("create table genes(gene, pubmed, unique(gene, pubmed) on conflict ignore);");
			stat.executeUpdate("drop table if exists citations");
			stat.executeUpdate("create table citations(pubmed, cites, unique(pubmed) on conflict replace);");
			stat.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Connection connect() throws SQLException {
		try { Class.forName("org.sqlite.JDBC"); }
		catch (ClassNotFoundException e) { e.printStackTrace(); };
		return DriverManager.getConnection("jdbc:sqlite:"+dbName);
	}
	

	public void populate(Map<String, List<String>> g2p) {
		try {
			Connection con = connect();
			con.setAutoCommit(false);
			int count = 0; 
			int size = g2p.size();
			for (String gene : g2p.keySet()) {

				System.out.println(count+"/"+size);
				List<String> pmids = g2p.get(gene);
				for (String pmid : pmids) {
					PreparedStatement prep = con.prepareStatement("insert into genes values (?,?);");
					prep.setString(1, gene);
					prep.setString(2, pmid);
					prep.addBatch();
					prep.executeBatch();
					prep.close();
				}
				count++;
			}
			con.setAutoCommit(true);
			Statement stat = con.createStatement();
			stat.executeUpdate("insert into citations(pubmed, cites) select pubmed,count(gene) from genes group by pubmed;");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public List<String> getPubmedsOverCiteLimit(int limit) {
		List<String> megapubs = new ArrayList<String>();
		
		try {
			Connection con = connect();
			ResultSet result = con.createStatement().executeQuery("select pubmed from citations where cites >"+limit);
			while (result.next()) 
				megapubs.add(result.getString("pubmed"));
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return megapubs;
	}
	
	public List<String> getPubmedsForGene(String id) {
		List<String> pmids = new ArrayList<String>();
		try {
			Connection con = connect();
			ResultSet result = con.createStatement().executeQuery("select pubmed from genes where gene=\""+id+"\"");
			while (result.next())
				pmids.add(result.getString("pubmed"));
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return pmids;
	}
	
	public List<String> getFilteredPubmedsForGene(String id, int limit) {
		List<String> pmids = new ArrayList<String>();
		try {
			Connection con = connect();
			// FIXME I know PreparedStatement is preferred but it's not working atm and I don't know why - eclarke
			ResultSet results = con.createStatement().executeQuery(String.format(
					"select genes.pubmed from genes,citations where gene=\"%s\" " +
					"and citations.cites<%d and genes.pubmed=citations.pubmed;", id, limit));
			while (results.next()) 
				pmids.add(results.getString("pubmed"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return pmids;
	}
	
}
