/**
 * Methods relating to pulling data from mygene.info
 * Dependent on mygene.info API as of April 2011
 */
package org.gnf.pbb.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.gnf.pbb.exceptions.InvalidValueException;

public class JsonParser {
	private final static Logger logger = Logger.getLogger(JsonParser.class.getName());

	public static final String baseGeneURL = "http://mygene.info/gene/";
	public static final String metadataLoc = "http://mygene.info/metadata";
	public static final String uniprotURL = "http://www.uniprot.org/uniprot/";
	
	public static JsonNode metadata = null;
	
	public static JsonNode getJsonForId(int id) throws JsonParseException, JsonMappingException, IOException {
		URL geneURL;
		URLConnection connection;
		JsonNode node = null;
		geneURL = new URL(baseGeneURL + Integer.toString(id)); // would look like "http://mygene.info/gene/410"
		URL mdURL = new URL(metadataLoc);
		URLConnection mdConnection;
		try {
			connection = geneURL.openConnection();
			mdConnection = mdURL.openConnection();
			ObjectMapper mapper = new ObjectMapper();
			node = mapper.readValue(connection.getInputStream(),JsonNode.class);
			metadata = mapper.readValue(mdConnection.getInputStream(), JsonNode.class);
		
		} catch (IOException e) {
			System.err.println("There was an error opening connection to " + geneURL.toString());
		}
		return node;
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
	public GeneObject newGeneFromId(int id) throws JsonParseException, JsonMappingException, IOException {
		GeneObject gene = new GeneObject();
		JsonNode rootNode = getJsonForId(id);
		final int MOUSE_TAXON_ID = 10090;
		
		// Parsing the returned JSON tree, in order of GNF_Protein_box format	
		try {
			gene.setPDB(getTextualValues(rootNode.path("pdb")));
			gene.setName(rootNode.get("name").getTextValue());
			gene.setHGNCid(rootNode.get("HGNC").getTextValue());
			gene.setSymbol(rootNode.get("symbol").getTextValue());
			gene.setAltSymbols(rootNode.get("alias").getTextValue()); //TODO ensure setAltSymbols converts this to String[]
			gene.setOMIM(rootNode.get("MIM").getTextValue());
			gene.setECnumber(rootNode.get("ec").getTextValue());
			gene.setHomologene(rootNode.path("homologene").path("id").getIntValue()); // have to traverse down the tree a bit
			// setMGIid(null); // can't find this on downloaded json file
			// setGeneAtlas_image(null); // can't find this either
			gene.geneOntologies.setGeneOntologies(
					rootNode.path("go").path("MF").findValuesAsText("term"), 
					rootNode.path("go").path("MF").findValuesAsText("id"),
					"Molecular Function");
			gene.geneOntologies.setGeneOntologies(
					rootNode.path("go").path("CC").findValuesAsText("term"), 
					rootNode.path("go").path("CC").findValuesAsText("id"),
					"Cellular Component");
			gene.geneOntologies.setGeneOntologies(
					rootNode.path("go").path("BP").findValuesAsText("term"), 
					rootNode.path("go").path("BP").findValuesAsText("id"),
					"Biological Process");
			gene.setHsEntrezGene(rootNode.get("entrezgene").getIntValue());
			gene.setHsEnsemble(rootNode.path("ensembl").get("gene").getTextValue());
			gene.setHsRefSeqProtein(getTextualValues(rootNode.path("refseq").path("protein"))); 
			gene.setHsRefSeqmRNA(getTextualValues(rootNode.path("refseq").path("rna")));
			gene.setHsGenLocChr(Integer.parseInt(rootNode.path("genomic_pos").get("chr").getTextValue())); // mygene.info returns this as a string, its not
			gene.setHsGenLocStart(rootNode.path("genomic_pos").get("start").getIntValue());
			gene.setHsGenLocEnd(rootNode.path("genomic_pos").get("end").getIntValue());
			
			//TODO: Detect which uniprot id reviewed
			gene.setHsUniprot(findReviewedUniprotEntry(rootNode.path("uniprot")));
			
			// Need to load mouse gene data for the next group of setters
			// The information is contained in the homologene array; we need to find the array with
			// the first element being 10090 (the mouse taxon id) and then grab the corresponding
			// gene id for that taxon id.
			Iterator<JsonNode> homologArray = rootNode.path("homologene").path("genes").getElements();
			for (int i =0; homologArray.hasNext(); i++) {
				JsonNode node = homologArray.next();
				if (node.get(0).getIntValue() == MOUSE_TAXON_ID) {
					gene.setMmEntrezGene(node.get(1).getIntValue()); // success!
					break;
				}
			}
			if (gene.getMmEntrezGene() != 0) {
				// Switching rootNode to the equivalent mouse gene information
				rootNode = getJsonForId(gene.getMmEntrezGene());
				gene.setMmEnsemble(rootNode.path("ensembl").get("gene").getTextValue());
				gene.setMmRefSeqProtein(getTextualValues(rootNode.path("refseq").path("protein")));
				gene.setMmRefSeqmRNA(getTextualValues(rootNode.path("refseq").path("rna")));
				gene.setMmGenLocChr(Integer.parseInt(rootNode.path("genomic_pos").get("chr").getTextValue())); // mygene.info returns this as a string, its not
				gene.setMmGenLocStart(rootNode.path("genomic_pos").get("start").getIntValue());
				gene.setMmGenLocEnd(rootNode.path("genomic_pos").get("end").getIntValue());
				gene.setMmUniprot(findReviewedUniprotEntry(rootNode.path("uniprot")));
			}
			
			// set the GenLoc db numbers from mygene.info's metadata file
			gene.setHsGenLocDb(metadata.path("GENOME_ASSEMBLY").get("human").getTextValue());
			gene.setMmGenLocDb(metadata.path("GENOME_ASSEMBLY").get("mouse").getTextValue());
			
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			logger.severe("Error retrieving value for field; non-fatal. \n"+
					"Gene ID: "+id+"; error message: " +e.getMessage());
		}
		
		return gene;
		
	}
	
	/**
	 * iterates over a node and returns all the values 
	 * @param rootNode
	 * @return
	 */
	private static String[] getTextualValues(JsonNode rootNode) {
		Iterator<JsonNode> iter = rootNode.getElements();
		String[] values = new String[rootNode.size()];
		for (int i = 0; iter.hasNext(); i++) {
			values[i] = iter.next().getTextValue();
		}
		//System.out.println(Arrays.toString(values));
		return values;
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
	
}
