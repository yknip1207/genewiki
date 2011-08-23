package org.genewiki.pbb.mygeneinfo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.genewiki.pbb.exceptions.ExceptionHandler;
import org.genewiki.pbb.wikipedia.ProteinBox;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handles the JSON-formatted gene information from http://mygene.info.
 * Most of the action takes place in the parse() function.
 * @author eclarke
 *
 */
public class MyGeneInfoParser {
	private final static Logger logger = Logger.getLogger(MyGeneInfoParser.class.getName());
	static ExceptionHandler botState = ExceptionHandler.INSTANCE;
	
	final int MOUSE_TAXON_ID = 10090;
	
	public final String baseURL;
	public final String metadataURL;
	public final String uniprotURL;
	
	public final JsonNode metadata;
	
	/**
	 * Create a new parser for MyGeneInfo JSON documents.
	 */
	public MyGeneInfoParser() {
		baseURL = "http://mygene.info/gene/";
		metadataURL = "http://mygene.info/metadata";
		uniprotURL = "http://www.uniprot.org/uniprot/";
		metadata = getJsonForURL(metadataURL);
	}
	
	/**
	 * Dumps a URL straight into a JSON object mapper that returns the root JSON node
	 * of the document. Will trigger a fatal error if one the URL passed is invalid,
	 * or if there is an I/O Exception.
	 * @param url
	 * @return JsonNode root node of the document
	 */
	private JsonNode getJsonForURL(String url) {
		try {
			URL geneURL = new URL(url);
			URLConnection connection = geneURL.openConnection();
			
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.readValue(connection.getInputStream(), JsonNode.class);
			
			return node;	
		} catch (MalformedURLException mue) {
			System.err.println("Bad URL.");
			botState.fatal(mue);
			return null;	// Ideally the bot's in the process of shutting down
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			botState.recoverable(e);
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			botState.fatal(e);
			return null;
		}
	}
	
	/**
	 * Convenience method that creates the URL appropriate for the given id
	 * @param id
	 * @return JsonNode root node of the document for that id
	 */
	private JsonNode getJsonForId(String id) {
		return getJsonForURL(baseURL+id);
	}
	
	/**
	 * Parses the JSON document returned from mygene.info/gene/<id> and creates a 
	 * ProteinBox object from the data. Also fetches and populates the ProteinBox
	 * with information from the mouse homolog and, where possible, preferentially
	 * selects the reviewed Uniprot entry id for inclusion.
	 * @param id Entrez id for gene
	 * @return ProteinBox object.
	 */
	public ProteinBox parse(String id) {
		JsonNode root = getJsonForId(id);
		
		try {	// this try/catch block exists mostly for NullPointerExceptions
			String name = checkNotNull(root.path("name").getTextValue());
			ProteinBox.Builder builder = new ProteinBox.Builder(name, id);
			
			builder.add("PDB", parseArrayNode(root.path("pdb")));
			builder.add("HGNCid", root.path("HGNC").getTextValue());
			builder.add("Symbol", root.path("symbol").getTextValue());
			builder.add("AltSymbols", parseArrayNode(root.path("alias")));
			builder.add("OMIM", root.path("MIM").getTextValue());
			builder.add("ECnumber", root.path("ec").getTextValue());
			builder.add("Homologene", Integer.toString(root.path("homologene").path("id").getIntValue()));
			
			builder.add("Function", buildOntologyList(
					root.path("go").path("MF").findValuesAsText("term"), 
					root.path("go").path("MF").findValuesAsText("id")));
			builder.add("Component", buildOntologyList(
					root.path("go").path("CC").findValuesAsText("term"), 
					root.path("go").path("CC").findValuesAsText("id")));
			builder.add("Process", buildOntologyList(
					root.path("go").path("BP").findValuesAsText("term"), 
					root.path("go").path("BP").findValuesAsText("id")));
			
			builder.add("Hs_EntrezGene", Integer.toString(root.get("entrezgene").getIntValue()));
			builder.add("Hs_Ensembl", root.path("ensembl").path("gene").getTextValue());
			builder.add("Hs_RefseqProtein", safeGetFirstEntry(parseArrayNode(root.path("refseq").path("protein")))); // We only care about the first value 
			builder.add("Hs_RefseqmRNA", safeGetFirstEntry(parseArrayNode(root.path("refseq").path("rna"))));		 // Same
			builder.add("Hs_GenLoc_chr", root.path("genomic_pos").path("chr").getTextValue()); 
			builder.add("Hs_GenLoc_start", Integer.toString(root.path("genomic_pos").path("start").getIntValue()));
			builder.add("Hs_GenLoc_end", Integer.toString(root.path("genomic_pos").path("end").getIntValue()));
			
			// Finds the Uniprot entry that's been reviewed, if any
			builder.add("Hs_Uniprot", findReviewedUniprotEntry(root.path("uniprot")));
			
			// Need to load mouse gene data for the next group of setters
			// The information is contained in the homologene array; we need to find the array with
			// the first element being 10090 (the mouse taxon id) and then grab the corresponding
			// gene id for that taxon id.
			
			//FIXME bizarre for loop 
			Iterator<JsonNode> homologArray = root.path("homologene").path("genes").getElements();
			int mouseId = 0;
			for (int i =0; homologArray.hasNext(); i++) {
				JsonNode node = homologArray.next();
				if (node.path(0).getIntValue() == MOUSE_TAXON_ID) {
					builder.add("Mm_EntrezGene", Integer.toString(node.path(1).getIntValue())); // success!
					mouseId = node.path(1).getIntValue();
					break;
				}
			}
			if (mouseId != 0) {
				// Switching root node to the equivalent mouse gene information
				root = getJsonForId(Integer.toString(mouseId));
				builder.add("Mm_Ensembl", root.path("ensembl").path("gene").getTextValue());
				builder.add("Mm_RefseqProtein", safeGetFirstEntry(root.path("refseq").findValuesAsText("protein"))); // Only getting the first value 
				builder.add("Mm_RefseqmRNA", safeGetFirstEntry(root.path("refseq").findValuesAsText("rna")));
				builder.add("Mm_GenLoc_chr", root.path("genomic_pos").path("chr").getTextValue()); // mygene.info returns this as a string, its not
				builder.add("Mm_GenLoc_start", Integer.toString(root.path("genomic_pos").path("start").getIntValue()));
				builder.add("Mm_GenLoc_end", Integer.toString(root.path("genomic_pos").path("end").getIntValue()));
			}
			
			// set the GenLoc db numbers from mygene.info's metadata file
			builder.add("Hs_GenLoc_db", metadata.path("GENOME_ASSEMBLY").get("human").getTextValue());
			builder.add("Mm_GenLoc_db", metadata.path("GENOME_ASSEMBLY").get("mouse").getTextValue());
			
			return builder.build();
		} catch (NullPointerException npe) {
			npe.printStackTrace();
			botState.recoverable(npe);
			return null;
		}
		
	}
	

	/**
	 * Returns the first item in a list, or an empty string if the list
	 * was empty or the first item was null.
	 * @param list
	 * @return first item or empty string, never null
	 */
	private static String safeGetFirstEntry(List<String> list) {
		try {
			return checkNotNull(list.get(0));
		} catch (IndexOutOfBoundsException e) {
			// Nothing in this list...
			return "";
		} catch (NullPointerException e) {
			// And whatever was in the list was null
			return "";
		}
	}

	/**
	 * Translates an array node into a list object. If the node passed
	 * is simply a single-value text node, a single-item list is returned.
	 * @param node
	 * @return list from array node contents
	 */
	private static List<String> parseArrayNode(JsonNode node) {
		List<String> list = new ArrayList<String>();
		Iterator<JsonNode> iter = node.getElements();
		try {
			while (iter.hasNext()) {
				list.add(iter.next().getTextValue());
			}
			return list;
		} catch (NoSuchElementException e) {
			list.add(node.getTextValue());
			return list;
		}
	}
	
	/**
	 * Translates the JSON structure representing ontology terms and ids
	 * into the wikitext representation of those terms.
	 * @param terms
	 * @param ids
	 * @return list of term:id pairs for a given ontology category
	 */
	private static List<String> buildOntologyList(List<String> terms, List<String> ids) {
		List<String> outList = new ArrayList<String>();
		for (int i = 0; i < ids.size(); i++) {
			outList.add("GNF_GO|id="+ids.get(i)+" |text = "+terms.get(i));
		}
		return outList;
	}
	
	/**
	 * Does a quick search of the first line of the corresponding Uniprot 
	 * entry for each given Uniprot id for the word "Reviewed"; 
	 * Cursory examination of Uniprot entries as text indicates that 
	 * "Reviewed;" or "Unreviewed;" shows up in the first line of the 
	 * text file.
	 * @param uniprotNode
	 * @return uniprot entry that's been reviewed, if available
	 */
	private String findReviewedUniprotEntry(JsonNode uniprotNode) {
		String uniprot = null;
		URL uniprotEntry;
		URLConnection connection;
		boolean foundReviewedEntry = false;
		Iterator<JsonNode> iter = uniprotNode.getElements();
		while (iter.hasNext() && !foundReviewedEntry) {
			uniprot = iter.next().getTextValue();
			try {
				uniprotEntry = new URL(uniprotURL + uniprot +".txt");
				connection = uniprotEntry.openConnection();
				StringBuffer buffer = new StringBuffer();
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				buffer.append(reader.readLine());
				String content = buffer.toString();
				// Looks only at the first line of the file to see if it is reviewed or unreviewed
				if ((content.contains("Reviewed;")) && !(content.contains("Unreviewed;"))) {
					foundReviewedEntry = true;
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				logger.warning("Uniprot entry missing...");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		return uniprot;
	}
	
	/**
	 * For testing.
	 */
	public static void main(String[] args){
		MyGeneInfoParser parser = new MyGeneInfoParser();
		ProteinBox box = parser.parse("1231");
		String boxString = box.toString();
		System.out.println(boxString);
	}

}
