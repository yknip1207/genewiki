/**
 * The model in the model-view-controller configuration for PBB.
 */
package org.gnf.pbb.mygeneinfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 
 * @author eclarke
 *
 * A GeneObject contains getters and setters for most variables stored in mygene.info (with some exceptions).
 * Ideally it is a serializable object able to be stored and passed around; it is also able to output its fields
 * as a linked hash map to facilitate comparisons and iterable procedures.
 * Contains two member classes: GeneOntology and GeneOntologyCollection.
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
	
	/**
	 * @author eclarke
	 * GeneOntologyCollection is a composite of many GeneOntology objects.
	 */
	public class GeneOntologyCollection {
		private HashSet<GeneOntology> Ontologies;
		
		public GeneOntologyCollection() {
			this.Ontologies = new HashSet<GeneOntology>();
		}

		/**
		 * Creates new GeneOntology objects from matching lists of terms and ids, with a specified category.
		 * The terms and ids list must be of the same length and in the same order (they usually are if parsed
		 * from a source file correctly).
		 * @param terms list
		 * @param ids list
		 * @param category
		 */
		public void setGeneOntologies(List<String> terms, List<String> ids, String category) {
			for (int i = 0; i < ids.size(); i++) {
				boolean success = Ontologies.add(new GeneOntology(ids.get(i), terms.get(i), category));
				if (!success) System.out.printf("The selected ontology, \"%s:%s\", was already present; not added.",ids.get(i), terms.get(i));
			}
			if (category.equalsIgnoreCase("Molecular Function")) {
				geneData.put("Function", getOntologiesAsWikiLinks(getOntologiesByCategory("Molecular Function")));
			} else if (category.equalsIgnoreCase("Cellular Component")) {
				geneData.put("Component", getOntologiesAsWikiLinks(getOntologiesByCategory("Cellular Component")));
			} else if (category.equalsIgnoreCase("Biological Process")) {
				geneData.put("Process", getOntologiesAsWikiLinks(getOntologiesByCategory("Biological Process")));
			}
		}
		
		public LinkedHashMap<String, GeneOntology> getAllOntologies() {
			LinkedHashMap<String, GeneOntology> map = new LinkedHashMap<String, GeneOntology>();
			for (GeneOntology go : Ontologies) {
				map.put(go.getId(), go);
			}
			return map;	
		}
		
		public GeneOntology getOntologyById(String id) {
			LinkedHashMap<String, GeneOntology> ontList = this.getAllOntologies();
			return ontList.get(id);
		}
		
		/**
		 * Returns a list of GeneOntology objects matching a specified category.
		 * TODO: Optimize? Inefficient category search.
		 * @param category
		 * @return list of GeneOntology objects
		 */
		public List<GeneOntology> getOntologiesByCategory(String category) {
			List<GeneOntology> list = new ArrayList<GeneOntology>();
			HashSet<String> foundCategories = new HashSet<String>();
			for (GeneOntology go : Ontologies) {
				foundCategories.add(go.getCategory());
				if (go.getCategory() == category) list.add(go);
			}
			if (list.size() == 0) {
				System.out.println("No matches for the specified category found. These categories were found: " + foundCategories);
			}
			return list;
		}
		
		/**
		 * Returns a string from a list of ontologies in the format specified by ontology.printWikified()
		 * @param list
		 * @return
		 */
		public List<String> getOntologiesAsWikiLinks(List<GeneOntology> list) {
			List<String> ontologiesString = new ArrayList<String>();
			for(GeneOntology go : list) {
				ontologiesString.add(go.printWikified());
			}
			return ontologiesString;
		}

	}
	public static class GeneOntology {
		private String id;
		private String term;
		private String category;
		public String getId() { return id; }
		public String getTerm() { return term; }
		public String getCategory() { return category; }
		public void setId(String id) { this.id = id; }
		public void setTerm(String term) { this.term = term; }
		public void setCategory(String category) { this.category = category; }
		
		public GeneOntology(String id, String term, String category) {
			setId(id);
			setTerm(term);
			setCategory(category);
		}
		
		/**
		 * Format ontology to go between {{ }} brackets as a non-expanded template link
		 * @return
		 */
		public String printWikified() {
			return "GNF_GO|id="+getId()+" |text = "+getTerm();
		}
	}
	
	public GeneOntologyCollection geneOntologies = new GeneOntologyCollection();
	private int HsEntrezGene, MmEntrezGene = 0;
	private String HsEnsemble, MmEnsemble= "";
	private String[] HsRefSeqProtein,MmRefSeqProtein = {};
	private String[] HsRefSeqmRNA, MmRefSeqmRNA = {};
	private String HsGenLocDb, MmGenLocDb = "";
	private String HsGenLocChr, MmGenLocChr = "";
	private int HsGenLocStart, MmGenLocStart = 0;
	private int HsGenLocEnd, MmGenLocEnd = 0;
	private String HsUniprot, MmUniprot = "";
	private LinkedHashMap<String,List<String>> geneData = new LinkedHashMap<String, List<String>>();
	
	/**
	 * converts a string to a List<String> for more additions or to comply with linked hash map parameters
	 * @param str
	 * @param size
	 * @return List<String>
	 */
	private List<String> toList(String str) {
		List<String> list = new ArrayList<String>(1);
		list.add(str);
		return list;	
	}
	
	public GeneObject () {
	}

	public LinkedHashMap<String, List<String>> getGeneDataAsMap() {
		return geneData;
	}
	
	public void setImage(String image) {
		this.image = image;
		geneData.put("image", toList(image));
	}



	public String getImage() {
		return image;
	}



	public void setImageSource(String imageSource) {
		this.imageSource = imageSource;
		geneData.put("image_source", toList(imageSource));
	}

	public String getImageSource() { return imageSource;	}



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
		geneData.put("PDB", Arrays.asList(PDB));
		
	}

	public String[] getPDB() {return PDB;}



	public void setName(String name) {
		this.name = name;
		geneData.put("Name", toList(name));
	}

	public String getName() {return name;}

	public void setHGNCid(String string) {
		try {
			HGNCid = Integer.parseInt(string);
			geneData.put("HGNCid", toList(string));
		} catch (NumberFormatException nfe) {
			System.out.println("Invalid HGNC id parsed. " + nfe.getMessage());
		}
	}

	public int getHGNCid() {
		return HGNCid;
	}



	public void setSymbol(String symbol) {
		this.symbol = symbol;
		geneData.put("Symbol",toList(symbol));
	}

	public String getSymbol() {
		return symbol;
	}



	public void setAltSymbols(String string) {
		if (string != null) this.altSymbols = string.split(", ");
		geneData.put("AltSymbols", Arrays.asList(this.altSymbols));
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
		if (!errorflag) {
			OMIM = string;
			geneData.put("OMIM", toList(string));
		}
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
					System.out.println("Invalid EC number parsed. " + nfe.getMessage());
					errorflag = true; // there is likely a more graceful way to do this, but oh well
				}
				
			}
			if (errorflag != true) {
				this.ECnumber = eCnumber;
				geneData.put("ECnumber", toList(eCnumber));
			}
		}
	}

	public String getECnumber() {
		return ECnumber;
	}



	public void setHomologene(int i) {
		this.Homologene = i;
		geneData.put("Homologene", toList(Integer.toString(i)));
	}

	public int getHomologene() {
		return Homologene;
	}



	public void setMGIid(int mGIid) {
		MGIid = mGIid;
		geneData.put("MGIid", toList(Integer.toString(mGIid)));
	}

	public int getMGIid() {
		return MGIid;
	}



	public void setGeneAtlas_image(String geneAtlas_image) {
		GeneAtlas_image = geneAtlas_image;
		geneData.put("GeneAtlas_image", toList(geneAtlas_image));
	}

	public String getGeneAtlas_image() {
		return GeneAtlas_image;
	}


	public void setHsEntrezGene(int hsEntrezGene) {
		HsEntrezGene = hsEntrezGene;
		geneData.put("Hs_EntrezGene", toList(Integer.toString(hsEntrezGene)));
	}

	public int getHsEntrezGene() {
		return HsEntrezGene;
	}



	public void setHsEnsemble(String hsEnsemble) {
		HsEnsemble = hsEnsemble;
		geneData.put("Hs_Ensembl", toList(hsEnsemble));
	}

	public String getHsEnsemble() {
		return HsEnsemble;
	}



	public void setHsRefSeqProtein(String[] hsRefSeqProtein) {
		HsRefSeqProtein = hsRefSeqProtein;
		geneData.put("Hs_RefHsseqProtein", Arrays.asList(hsRefSeqProtein));
	}

	public String[] getHsRefSeqProtein() {
		return HsRefSeqProtein;
	}



	public void setHsRefSeqmRNA(String[] hsRefSeqmRNA) {
		HsRefSeqmRNA = hsRefSeqmRNA;
		geneData.put("Hs_RefseqmRNA", Arrays.asList(hsRefSeqmRNA));
	}

	public String[] getHsRefSeqmRNA() {
		return HsRefSeqmRNA;
	}

	public void setHsGenLocDb(String db) {
		HsGenLocDb = db;
		geneData.put("Hs_GenLoc_db", toList(db));
	}
	
	public String getHsGenLocDb() {return HsGenLocDb; }
	

	public void setHsGenLocChr(String hsGenLocChr) {
		HsGenLocChr = hsGenLocChr;
		geneData.put("Hs_GenLoc_chr", toList(hsGenLocChr));
	}

	public String getHsGenLocChr() {
		return HsGenLocChr;
	}



	public void setHsGenLocStart(int hsGenLocStart) {
		HsGenLocStart = hsGenLocStart;
		geneData.put("Hs_GenLoc_start", toList(Integer.toString(hsGenLocStart)));
	}

	public int getHsGenLocStart() {
		return HsGenLocStart;
	}



	public void setHsGenLocEnd(int hsGenLocEnd) {
		HsGenLocEnd = hsGenLocEnd;
		geneData.put("Hs_GenLoc_end", toList(Integer.toString(hsGenLocEnd)));
	}

	public int getHsGenLocEnd() {
		return HsGenLocEnd;
	}



	public void setHsUniprot(String hsUniprot) {
		if (hsUniprot != null) {
			HsUniprot = hsUniprot;
			geneData.put("Hs_Uniprot", toList(hsUniprot));
		}
		
	}

	public String getHsUniprot() {
		return HsUniprot;
	}



	public void setMmEntrezGene(int mmEntrezGene) {
		MmEntrezGene = mmEntrezGene;
		geneData.put("Mm_EntrezGene", toList(Integer.toString(mmEntrezGene)));
	}



	public int getMmEntrezGene() {
		return MmEntrezGene;
	}



	public void setMmEnsemble(String mmEnsemble) {
		MmEnsemble = mmEnsemble;
		geneData.put("Mm_Ensembl", toList(mmEnsemble));
	}



	public String getMmEnsemble() {
		return MmEnsemble;
	}



	public void setMmRefSeqProtein(String[] mmRefSeqProtein) {
		if (mmRefSeqProtein != null) {
			MmRefSeqProtein = mmRefSeqProtein;
			geneData.put("Mm_RefseqProtein", Arrays.asList(mmRefSeqProtein));
		}
	}



	public String[] getMmRefSeqProtein() {
		return MmRefSeqProtein;
	}



	public void setMmRefSeqmRNA(String[] mmRefSeqmRNA) {
		if (mmRefSeqmRNA != null) {
			MmRefSeqmRNA = mmRefSeqmRNA;
			geneData.put("Mm_RefseqmRNA", Arrays.asList(mmRefSeqmRNA));
		}
	}



	public String[] getMmRefSeqmRNA() {
		return MmRefSeqmRNA;
	}


	public void setMmGenLocDb(String db) {
		this.MmGenLocDb = db;
		geneData.put("Mm_GenLoc_db", toList(db));
	}
	

	public void setMmGenLocChr(String mmGenLocChr) {
		MmGenLocChr = mmGenLocChr;
		geneData.put("Mm_GenLoc_chr", toList(mmGenLocChr));
	}



	public String getMmGenLocChr() {
		return MmGenLocChr;
	}



	public void setMmGenLocStart(int mmGenLocStart) {
		MmGenLocStart = mmGenLocStart;
		geneData.put("Mm_GenLoc_start", toList(Integer.toString(mmGenLocStart)));
	}



	public int getMmGenLocStart() {
		return MmGenLocStart;
	}



	public void setMmGenLocEnd(int mmGenLocEnd) {
		MmGenLocEnd = mmGenLocEnd;
		geneData.put("Mm_GenLoc_end", toList(Integer.toString(mmGenLocEnd)));
	}



	public int getMmGenLocEnd() {
		return MmGenLocEnd;
	}



	public void setMmUniprot(String mmUniprot) {
		if (mmUniprot != null) {
			MmUniprot = mmUniprot;
			geneData.put("Mm_Uniprot", toList(mmUniprot));
		}
	}



	public String getMmUniprot() {
		return MmUniprot;
	}

}
