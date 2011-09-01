package org.genewiki.pbb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Collections;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.genewiki.pbb.mygeneinfo.MyGeneInfoParser;
import org.genewiki.pbb.util.FileHandler;
import org.genewiki.pbb.wikipedia.ProteinBox;
import org.joda.time.DateTime;

public class Generator {

	private final MyGeneInfoParser parser;
	private ProteinBox box;
	private String symbol;
	public Generator() {
		parser = new MyGeneInfoParser();
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

	public String generateStub(String id) {
		JsonNode root = parser.getJsonForId(id);
		StringBuffer out = new StringBuffer();
		String sym = root.path("symbol").getTextValue();
		this.symbol = sym;
		String name = root.path("name").getTextValue();
		String summary = root.path("summary").getTextValue();
		String entrezCite = "<ref name=\"entrez\">{{cite web | title = Entrez Gene: "+name+"| " +
				"url = http://www.ncbi.nlm.nih.gov/sites/entrez?db=gene&cmd=retrieve&list_uids="+id+"| accessdate = " +
						DateTime.now().toString()+"}}</ref>\n\n";
		
		out.append("{{PBB|geneid="+id+"}} \n");
		out.append("'''"+name+"''', also known as "+sym+", is a human [[gene]].");
		out.append(entrezCite);
		out.append(summary).append(entrezCite);
		out.append("==References== \n {{reflist}} \n");
		out.append("==Further reading == \n {{refbegin | 2}}\n");
		
		// SO hackish. FIXME
		FileHandler fh = new FileHandler("/tmp/");
		String references = "";
		try {
			fh.wget("http://plugins.biogps.org/cgi-bin/PBB_template_gen_xml.cgi?id="+id, id+".plugindump");
			String raw = fh.read(id+".plugindump");
			int a = raw.indexOf("{{refbegin | 2}}")+16;
			int b = raw.indexOf("{{refend}}");
			references = raw.substring(a, b);
		} catch (IOException e) {
			/// dunno?
		}
		
		out.append(references+"\n");
		out.append("{{refend}}");
		
		
		return out.toString();
	}
	
	/**
	 * Just gives a string with all the citations. I'd search
	 * for pubmed ids in it. YMMV.
	 * @return
	 */
	public String getBadArticles() {
		try {
			FileHandler fh = new FileHandler("");
			return fh.read("pubmed_filter.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "";
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
	
}
