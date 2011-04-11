/**
 * Create an object to store the information in a GNF_Protein_box template, flatten it, and manipulate it
 */
package org.gnf.pbb;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.jwbf.core.contentRep.ContentAccessable;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

/**
 * @author eclarke
 *
 */

public class ProteinBox implements Serializable{
	private static final long serialVersionUID = 0; //TODO fix this, cannot/should not equal zero
	private static final int MOUSE_TAXON_ID = 10090;
	
	private String image = "";
	private String imageSource = "";
	private String[] PDB = {};
	private String name = "";
	private int HGNCid = 0;
	private String symbol = "";
	private String[] altSymbols = {};
	private String OMIM = "";
	private String ECnumber = "";
	private int Homologene = 0;
	private int MGIid = 0;
	private String GeneAtlas_image = "";
	public static class GeneOntology {
		private List<String> terms;
		private List<String> ids;
		private String[] collated;
		
		public List<String> getTerms() { return terms; }
		public List<String> getIds() { return ids; }
		public String[] getCollated() { return collated; }
		
		public void setTerms(List<String> _terms) {this.terms = _terms;}
		public void setIds(List<String> _ids) {this.ids = _ids;}
		public void setCollated(String[] _collated) { this.collated = _collated; }
		
		public void setGeneOntologies(JsonNode rootNode, String field) {
			JsonNode node = rootNode.path("go").path(field);
			List<String> terms = node.findValuesAsText("term");
			List<String> ids = node.findValuesAsText("id");
			
			String[] collated = new String[terms.size()];
			if (terms.size() == ids.size()) {
				for (int i = 0; i < terms.size(); i++) {
					//TODO An ugly and hackish way to detect duplicates... sorry
					if (terms.subList(i+1, terms.size()).contains(terms.get(i))) {
						int duplicateItem = terms.subList(i, terms.size()).indexOf(terms.get(i));
						terms.remove(duplicateItem);
						ids.remove(duplicateItem);
					}
					collated[i] = "{{GNF_GO|id=GO:" + ids.get(i) + " |text = " + terms.get(i);
				}
			}
			setIds(ids);
			setTerms(terms);
			setCollated(collated);
		}
	}
	private GeneOntology GoFunctions = new GeneOntology();
	private GeneOntology GoComponents = new GeneOntology();
	private GeneOntology GoProcesses = new GeneOntology();
	private int HsEntrezGene, MmEntrezGene = 0;
	private String HsEnsemble, MmEnsemble= "";
	private String[] HsRefSeqProtein,MmRefSeqProtein = {};
	private String[] HsRefSeqmRNA, MmRefSeqmRNA = {};
	private int HsGenLocChr, MmGenLocChr = 0;
	private int HsGenLocStart, MmGenLocStart = 0;
	private int HsGenLocEnd, MmGenLocEnd = 0;
	private String HsUniprot, MmUniprot = "";
	//private Map<String,Object> geneData = null;
	
	/**
	 * @param int
	 * 				GeneID
	 */
	public ProteinBox (int GeneID) {
		try {
			setValuesFromSource(GeneID);
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
	
	public ProteinBox () {
		
	}
	
	//public String[] getValuesFromPath
	
	public void setValuesFromSource(int GeneID) throws JsonParseException, JsonMappingException, IOException {
		JsonNode rootNode = MyGeneInfo.getGeneWithId(GeneID);

		// Parsing the returned JSON tree, in order of GNF_Protein_box format	
		setPDB(getTextualValues(rootNode.path("pdb")));
		setName(rootNode.get("name").getTextValue());
		setHGNCid(rootNode.get("HGNC").getTextValue());
		setSymbol(rootNode.get("symbol").getTextValue());
		setAltSymbols(rootNode.get("alias").getTextValue()); //TODO ensure setAltSymbols converts this to String[]
		setOMIM(rootNode.get("MIM").getTextValue());
		setECnumber(rootNode.get("ec").getTextValue());
		setHomologene(rootNode.path("homologene").path("id").getIntValue()); // have to traverse down the tree a bit
		// setMGIid(null); // can't find this on downloaded json file
		// setGeneAtlas_image(null); // can't find this either
		GoFunctions.setGeneOntologies(rootNode, "MF");
		GoComponents.setGeneOntologies(rootNode, "CC");
		GoProcesses.setGeneOntologies(rootNode,"BP");
		setHsEntrezGene(rootNode.get("entrezgene").getIntValue());
		setHsEnsemble(rootNode.path("ensembl").get("gene").getTextValue());
		setHsRefSeqProtein(getTextualValues(rootNode.path("refseq").path("protein"))); 
		setHsRefSeqmRNA(getTextualValues(rootNode.path("refseq").path("rna")));
		setHsGenLocChr(Integer.parseInt(rootNode.path("genomic_pos").get("chr").getTextValue())); // mygene.info returns this as a string, its not
		setHsGenLocStart(rootNode.path("genomic_pos").get("start").getIntValue());
		setHsGenLocEnd(rootNode.path("genomic_pos").get("end").getIntValue());
		setHsUniprot(rootNode.path("uniprot").get("TrEMBL").getTextValue());
		
		// Need to load mouse gene data for the next group of setters
		// The information is contained in the homologene array; we need to find the array with
		// the first element being 10090 (the mouse taxon id) and then grab the corresponding
		// gene id for that taxon id.
		Iterator<JsonNode> homologArray = rootNode.path("homologene").path("genes").getElements();
		for (int i =0; homologArray.hasNext(); i++) {
			JsonNode node = homologArray.next();
			System.out.println(node.get(1).getIntValue());
			if (node.get(0).getIntValue() == MOUSE_TAXON_ID) {
				setMmEntrezGene(node.get(1).getIntValue()); // success!
				break;
			}
		}
		if (getMmEntrezGene() != 0) {
			// Switching rootNode to the equivalent mouse gene information
			rootNode = MyGeneInfo.getGeneWithId(getMmEntrezGene());
			setMmEnsemble(rootNode.path("ensembl").get("gene").getTextValue());
			setMmRefSeqProtein(getTextualValues(rootNode.path("refseq").path("protein")));
			setMmRefSeqmRNA(getTextualValues(rootNode.path("refseq").path("rna")));
			setMmGenLocChr(Integer.parseInt(rootNode.path("genomic_pos").get("chr").getTextValue())); // mygene.info returns this as a string, its not
			setMmGenLocStart(rootNode.path("genomic_pos").get("start").getIntValue());
			setMmGenLocEnd(rootNode.path("genomic_pos").get("end").getIntValue());
			setMmUniprot(rootNode.path("uniprot").get("TrEMBL").getTextValue());
		}
	}

	
	private String[] getTextualValues(JsonNode rootNode) {
		Iterator<JsonNode> iter = rootNode.getElements();
		String[] values = new String[rootNode.size()];
		for (int i = 0; iter.hasNext(); i++) {
			values[i] = iter.next().getTextValue();
		}
		//System.out.println(Arrays.toString(values));
		return values;
	}


	
	/**
	 * @param deprecated
	 * @param box
	 */
	public void setValuesFromWikipedia(ContentAccessable box) {
		//TODO add methods for grabbing the preexisting information off a Wikipedia article; also probably written?
	}

	public void setImage(String image) {
		this.image = image;
	}



	public String getImage() {
		return image;
	}



	public void setImageSource(String imageSource) {
		this.imageSource = imageSource;
	}



	public String getImageSource() {
		return imageSource;
	}



	public void setPDB(String[] pdbBuffer) throws IllegalArgumentException {
		this.PDB = new String[pdbBuffer.length];
		
		// Validation: all PDB values are of length 4
		for (int i = 0; i < pdbBuffer.length; i++){
			if (pdbBuffer[i].length() != 4) {
				throw new IllegalArgumentException("Invalid format for PDB codes.");
			} else {
				PDB[i] = pdbBuffer[i];
			}
		}
		
		
	}



	public String[] getPDB() {
		return PDB;
	}



	public void setName(String name) {
		this.name = name;
	}



	public String getName() {
		return name;
	}



	public void setHGNCid(String string) {
		try {
			HGNCid = Integer.parseInt(string);
		} catch (NumberFormatException nfe) {
			System.out.println("Invalid HGNC id parsed. " + nfe.getMessage());
		}
	}



	public int getHGNCid() {
		return HGNCid;
	}



	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}



	public String getSymbol() {
		return symbol;
	}



	public void setAltSymbols(String string) {
		if (string != null) this.altSymbols = string.split(", ");
		// TODO I would throw in a verification here but I don't
		// have any parameters defining alt symbols yet.
	}



	public String[] getAltSymbols() {
		return altSymbols;
	}



	public void setOMIM(String string) {
		boolean errorflag = false;
		try {
			Integer.parseInt(string);
		} catch (NumberFormatException nfe) {
			System.out.println("Invalid or null MIM id parsed. " + nfe.getMessage());
			errorflag = true;
		}
		if (!errorflag) OMIM = string;
	}



	public String getOMIM() {
		return OMIM;
	}



	public void setECnumber(String eCnumber) {
		// Validation: Enzyme Commission numbers are in the format #.#.#.#
		String[] eCBuffer = {};
		Boolean errorflag = false;
		if (eCnumber == null || eCnumber == "") {
			ECnumber = ""; // No EC number parsed, which is fine.
		} else {
			eCBuffer = eCnumber.split(".");
			for (String s : eCBuffer) {
				try {
					Integer.parseInt(s); // We're not storing them as ints, just checking
				} catch (NumberFormatException nfe){
					System.out.println("Invalid or EC number parsed. " + nfe.getMessage());
					errorflag = true; // there is likely a more graceful way to do this, but oh well
				}
				
			}
			if (errorflag != true) {
				this.ECnumber = eCnumber;
			}
		}
	}



	public String getECnumber() {
		return ECnumber;
	}



	public void setHomologene(int i) {
		this.Homologene = i;
	}



	public int getHomologene() {
		return Homologene;
	}



	public void setMGIid(int mGIid) {
		MGIid = mGIid;
	}



	public int getMGIid() {
		return MGIid;
	}



	public void setGeneAtlas_image(String geneAtlas_image) {
		GeneAtlas_image = geneAtlas_image;
	}



	public String getGeneAtlas_image() {
		return GeneAtlas_image;
	}

	public GeneOntology getGoFunctions() {
		return GoFunctions;
	}
	
	public void setGoFunctions(GeneOntology go) {
		GoFunctions = go;
	}
	
	public GeneOntology getGoComponents() {
		return GoComponents;
	}
	
	public void setGoComponents(GeneOntology go) {
		GoComponents = go;
	}
	
	public GeneOntology getGoProcesses() {
		return GoProcesses;
	}
	
	public void setGoProcesses(GeneOntology go) {
		GoProcesses = go;
	}

	public void setHsEntrezGene(int hsEntrezGene) {
		HsEntrezGene = hsEntrezGene;
	}



	public int getHsEntrezGene() {
		return HsEntrezGene;
	}



	public void setHsEnsemble(String hsEnsemble) {
		HsEnsemble = hsEnsemble;
	}



	public String getHsEnsemble() {
		return HsEnsemble;
	}



	public void setHsRefSeqProtein(String[] hsRefSeqProtein) {
		HsRefSeqProtein = hsRefSeqProtein;
	}



	public String[] getHsRefSeqProtein() {
		return HsRefSeqProtein;
	}



	public void setHsRefSeqmRNA(String[] hsRefSeqmRNA) {
		HsRefSeqmRNA = hsRefSeqmRNA;
	}



	public String[] getHsRefSeqmRNA() {
		return HsRefSeqmRNA;
	}



	public void setHsGenLocChr(int hsGenLocChr) {
		HsGenLocChr = hsGenLocChr;
	}



	public int getHsGenLocChr() {
		return HsGenLocChr;
	}



	public void setHsGenLocStart(int hsGenLocStart) {
		HsGenLocStart = hsGenLocStart;
	}



	public int getHsGenLocStart() {
		return HsGenLocStart;
	}



	public void setHsGenLocEnd(int hsGenLocEnd) {
		HsGenLocEnd = hsGenLocEnd;
	}



	public int getHsGenLocEnd() {
		return HsGenLocEnd;
	}



	public void setHsUniprot(String hsUniprot) {
		HsUniprot = hsUniprot;
	}



	public String getHsUniprot() {
		return HsUniprot;
	}



	public void setMmEntrezGene(int mmEntrezGene) {
		MmEntrezGene = mmEntrezGene;
	}



	public int getMmEntrezGene() {
		return MmEntrezGene;
	}



	public void setMmEnsemble(String mmEnsemble) {
		MmEnsemble = mmEnsemble;
	}



	public String getMmEnsemble() {
		return MmEnsemble;
	}



	public void setMmRefSeqProtein(String[] mmRefSeqProtein) {
		if (mmRefSeqProtein != null)
			MmRefSeqProtein = mmRefSeqProtein;
	}



	public String[] getMmRefSeqProtein() {
		return MmRefSeqProtein;
	}



	public void setMmRefSeqmRNA(String[] mmRefSeqmRNA) {
		if (mmRefSeqmRNA != null)
			MmRefSeqmRNA = mmRefSeqmRNA;
	}



	public String[] getMmRefSeqmRNA() {
		return MmRefSeqmRNA;
	}



	public void setMmGenLocChr(int mmGenLocChr) {
		MmGenLocChr = mmGenLocChr;
	}



	public int getMmGenLocChr() {
		return MmGenLocChr;
	}



	public void setMmGenLocStart(int mmGenLocStart) {
		MmGenLocStart = mmGenLocStart;
	}



	public int getMmGenLocStart() {
		return MmGenLocStart;
	}



	public void setMmGenLocEnd(int mmGenLocEnd) {
		MmGenLocEnd = mmGenLocEnd;
	}



	public int getMmGenLocEnd() {
		return MmGenLocEnd;
	}



	public void setMmUniprot(String mmUniprot) {
		MmUniprot = mmUniprot;
	}



	public String getMmUniprot() {
		return MmUniprot;
	}

}
