/**
 * Methods relating to pulling data from mygene.info
 * Dependent on mygene.info API as of April 2011
 */
package org.gnf.pbb.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.logging.Logger;

import org.gnf.pbb.GeneObject;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class JsonParser {
	private final static Logger logger = Logger.getLogger(JsonParser.class.getName());

	public static final String baseURL = "http://mygene.info/gene/";
	File tempfile;
	
	public static JsonNode getJsonForId(int id) throws JsonParseException, JsonMappingException, IOException {
		URL geneURL;
		URLConnection connection;
		JsonNode node = null;
		geneURL = new URL(baseURL + Integer.toString(id)); // would look like "http://mygene.info/gene/410"
		try {
			connection = geneURL.openConnection();
		
			ObjectMapper mapper = new ObjectMapper();
			node = mapper.readValue(connection.getInputStream(),JsonNode.class);
		
		} catch (IOException e) {
			System.err.println("There was an error opening connection to " + geneURL.toString());
		}
		return node;
	}
	
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
			gene.setHsUniprot(rootNode.path("uniprot").get("TrEMBL").getTextValue());
			
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
				gene.setMmUniprot(rootNode.path("uniprot").get("TrEMBL").getTextValue());
			}
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
	
	private static String[] getTextualValues(JsonNode rootNode) {
		Iterator<JsonNode> iter = rootNode.getElements();
		String[] values = new String[rootNode.size()];
		for (int i = 0; iter.hasNext(); i++) {
			values[i] = iter.next().getTextValue();
		}
		//System.out.println(Arrays.toString(values));
		return values;
	}
	
}
