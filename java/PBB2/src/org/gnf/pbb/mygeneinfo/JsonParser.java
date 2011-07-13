/**
 * Methods relating to pulling data from mygene.info
 * Dependent on mygene.info API as of April 2011
 */
package org.gnf.pbb.mygeneinfo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.gnf.pbb.exceptions.PbbExceptionHandler;
import org.gnf.pbb.wikipedia.ProteinBox;

public class JsonParser {
	private final static Logger logger = Logger.getLogger(JsonParser.class.getName());
	static PbbExceptionHandler botState = PbbExceptionHandler.INSTANCE;
	public static final String baseGeneURL = "http://mygene.info/gene/";
	public static final String metadataLoc = "http://mygene.info/metadata";
	public static final String uniprotURL = "http://www.uniprot.org/uniprot/";
	
	public static JsonNode metadata = null;
	
	public static JsonNode getJsonForId(int id) throws JsonParseException, JsonMappingException, IOException {
		String strId = Integer.toString(id);
		return getJsonForId(strId);
	}
	
	public static JsonNode getJsonForId(String id) throws JsonParseException, JsonMappingException, IOException {
		try {
			Integer.parseInt(id);
		} catch (NumberFormatException e) {
			logger.warning(id + " is not a number; Entrez gene ids are exclusively in number form.");
			botState.recoverable(e);
			return null;
		}
		URL geneURL;
		URLConnection connection;
		JsonNode node = null;
		geneURL = new URL(baseGeneURL + id); // would look like "http://mygene.info/gene/410"
		URL mdURL = new URL(metadataLoc);
		URLConnection mdConnection;
		try {
			connection = geneURL.openConnection();
			mdConnection = mdURL.openConnection();
			ObjectMapper mapper = new ObjectMapper();
			node = mapper.readValue(connection.getInputStream(),JsonNode.class);
			metadata = mapper.readValue(mdConnection.getInputStream(), JsonNode.class);
			
		} catch (IOException e) {
			logger.severe("There was an error opening connection to " + geneURL.toString());
			botState.recoverable(e);
			return null;
		}
		return node;
	}
	
	public static JsonNode getJsonFromFile(String filename) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(new FileInputStream(filename), JsonNode.class);
		} catch (IOException e) {
			logger.severe("There was an error opening file "+ filename+ ".");
			botState.recoverable(e);
		}
		return null;
	}
	
	/**
	 * Traverses a json tree to extract the needed values to populate a gene object. Initially the tree is
	 * based on the human gene information for that id. After the human information is populated,
	 * the parser finds the appropriate corresponding mouse gene ID, if available, and populates the 
	 * mouse gene fields with the json tree from that gene.
	 * Additionally, will set Uniprot entries, if more than one, preferentially with the reviewed uniprot
	 * entry. If a node is unavailable or does not exist, (a NullPointerException),
	 * this method throws a warning and continues parsing.
	 * @param id
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static ProteinBox newGeneFromId(String id) throws JsonParseException, JsonMappingException, IOException {
		JsonNode rootNode = getJsonForId(id);
		final int MOUSE_TAXON_ID = 10090;
		ProteinBox.Builder builder = new ProteinBox.Builder(rootNode.path("name").getTextValue(), id);
		// Parsing the returned JSON tree, in order of GNF_Protein_box format	
		try {
			builder.add("PDB", buildListFromArray(rootNode.path("pdb")));
			builder.add("HGNCid", rootNode.path("HGNC").getTextValue());
			builder.add("AltSymbols", buildListFromArray(rootNode.path("alias"))); //TODO ensure setAltSymbols converts this to String[]
			builder.add("OMIM",rootNode.path("MIM").getTextValue());
			builder.add("ECnumber",rootNode.path("ec").getTextValue());
			builder.add("Homologene", Integer.toString(rootNode.path("homologene").path("id").getIntValue())); // have to traverse down the tree a bit
			// setMGIid(null); // can't find this on downloaded json file
			// setGeneAtlas_image(null); // can't find this either
			builder.add("Function", buildOntologyList(
					rootNode.path("go").path("MF").findValuesAsText("term"), 
					rootNode.path("go").path("MF").findValuesAsText("id")));
			builder.add("Component", buildOntologyList(
					rootNode.path("go").path("CC").findValuesAsText("term"), 
					rootNode.path("go").path("CC").findValuesAsText("id")));
			builder.add("Process", buildOntologyList(
					rootNode.path("go").path("BP").findValuesAsText("term"), 
					rootNode.path("go").path("BP").findValuesAsText("id")));
			builder.add("Hs_EntrezGene", Integer.toString(rootNode.get("entrezgene").getIntValue()));
			builder.add("Hs_Ensembl", rootNode.path("ensembl").path("gene").getTextValue());
			builder.add("Hs_RefseqProtein", rootNode.path("refseq").findValuesAsText("protein").get(0)); // Only getting the first value 
			builder.add("Hs_RefseqmRNA", rootNode.path("refseq").findValuesAsText("rna").get(0));
			builder.add("Hs_GenLoc_chr", rootNode.path("genomic_pos").path("chr").getTextValue()); // mygene.info returns this as a string, its not
			builder.add("Hs_GenLoc_start", Integer.toString(rootNode.path("genomic_pos").path("start").getIntValue()));
			builder.add("Hs_GenLoc_end", Integer.toString(rootNode.path("genomic_pos").path("end").getIntValue()));
			
			// Finds the Uniprot entry that's been reviewed, if any
			builder.add("Hs_Uniprot", findReviewedUniprotEntry(rootNode.path("uniprot")));
			
			// Need to load mouse gene data for the next group of setters
			// The information is contained in the homologene array; we need to find the array with
			// the first element being 10090 (the mouse taxon id) and then grab the corresponding
			// gene id for that taxon id.
			Iterator<JsonNode> homologArray = rootNode.path("homologene").path("genes").getElements();
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
				// Switching rootNode to the equivalent mouse gene information
				rootNode = getJsonForId(mouseId);
				builder.add("Mm_Ensembl", rootNode.path("ensembl").path("gene").getTextValue());
				builder.add("Mm_RefseqProtein", safeGetFirstEntry(rootNode.path("refseq").findValuesAsText("protein"))); // Only getting the first value 
				builder.add("Mm_RefseqmRNA", rootNode.path("refseq").findValuesAsText("rna").get(0));
				builder.add("Mm_GenLoc_chr", rootNode.path("genomic_pos").path("chr").getTextValue()); // mygene.info returns this as a string, its not
				builder.add("Mm_GenLoc_start", Integer.toString(rootNode.path("genomic_pos").path("start").getIntValue()));
				builder.add("Mm_GenLoc_end", Integer.toString(rootNode.path("genomic_pos").path("end").getIntValue()));
			}
			
			// set the GenLoc db numbers from mygene.info's metadata file
			builder.add("Hs_GenLoc_db", metadata.path("GENOME_ASSEMBLY").get("human").getTextValue());
			builder.add("Mm_GenLoc_db", metadata.path("GENOME_ASSEMBLY").get("mouse").getTextValue());
			
		} catch (Exception e) {
			botState.recoverable(e);
			return null;
		}
		
		return builder.build();
		
	}
	
	private static String safeGetFirstEntry(List<String> list) {
		try {
			return list.get(0);
		} catch (Exception e){
			return "";
		}
	}

	private static List<String> buildListFromArray(JsonNode arrayNode) {
		List<String> outList = new ArrayList<String>();
		Iterator<JsonNode> it = arrayNode.getElements();
		while (it.hasNext()) {
			outList.add(it.next().getTextValue());
		}
		return outList;
	}
	
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
	private static String findReviewedUniprotEntry(JsonNode uniprotNode) {
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
	
	public static void main(String[] args) {
		
		
		try {
//			JsonNode root = JsonParser.getJsonForId("410");
//			ArrayNode pdb = (ArrayNode) root.path("pdb");
//			Iterator<JsonNode> it = pdb.getElements();
//			while (it.hasNext()) {
//				System.out.println(it.next().getTextValue());
//			}
			ProteinBox pb = JsonParser.newGeneFromId("410");
			System.out.println(pb.toString());
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}