/**
 * Create an object to store the information in a GNF_Protein_box template, flatten it, and manipulate it
 */
package org.gnf.pbb;

import java.io.Serializable;
import java.util.List;

/**
 * @author eclarke
 *
 */

public class GeneObject implements Serializable{
	private static final long serialVersionUID = -579129255945205253L;
	
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
		
		public void setGeneOntologies(List<String> terms, List<String> ids) {

			String term;
			// String[] collated = new String[terms.size()];
			if (terms.size() == ids.size()) {
				for (int i = 0; i < terms.size(); i++) {
					term = terms.get(i);
					// Checking for and removing duplicates
					if (terms.lastIndexOf(term) != terms.indexOf(term)) {
						terms.remove(terms.lastIndexOf(term));
						ids.remove(terms.lastIndexOf(term)); // since the duplicate in both would be at the same index
					}

					// collated[i] = "{{GNF_GO|id=GO:" + ids.get(i) + " |text = " + term;
				}
			}
			
			setIds(ids);
			setTerms(terms);
			// setCollated(collated);
		}
	}
	public GeneOntology GoFunctions = new GeneOntology();
	public GeneOntology GoComponents = new GeneOntology();
	public GeneOntology GoProcesses = new GeneOntology();
	private int HsEntrezGene, MmEntrezGene = 0;
	private String HsEnsemble, MmEnsemble= "";
	private String[] HsRefSeqProtein,MmRefSeqProtein = {};
	private String[] HsRefSeqmRNA, MmRefSeqmRNA = {};
	private int HsGenLocChr, MmGenLocChr = 0;
	private int HsGenLocStart, MmGenLocStart = 0;
	private int HsGenLocEnd, MmGenLocEnd = 0;
	private String HsUniprot, MmUniprot = "";
	//private Map<String,Object> geneData = null;
		
	public GeneObject () {
		
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
