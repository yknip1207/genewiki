/**
 * Create an object to store the information in a GNF_Protein_box template, flatten it, and manipulate it
 */
package org.gnf.pbb;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import net.sourceforge.jwbf.core.contentRep.ContentAccessable;

/**
 * @author eclarke
 *
 */
public class ProteinBox implements Serializable{
	private static final long serialVersionUID = 0; //TODO fix this, cannot/should not equal zero
	
	private String image = "";
	private String imageSource = "";
	private String[] PDB = {};
	private String name = "";
	private int HGNCid = 0;
	private String symbol = "";
	private String[] altSymbols = {};
	private int OMIM = 0;
	private int ECnumber = 0;
	private int Homologene = 0;
	private int MGIid = 0;
	private String GeneAtlas_image = "";
	private String[] GoFunctions = {};
	private String[] GoComponents = {};
	private String[] GoProcess = {};
	private int HsEntrezGene, MmEntrezGene = 0;
	private String HsEnsemble, MmEnsemble= "";
	private String HsRefSeqProtein, MmRefSeqProtein = "";
	private String HsRefSeqmRNA, MmRefSeqmRNA = "";
	private int HsGenLocChr, MmGenLocChr = 0;
	private int HsGenLocStart, MmGenLocStart = 0;
	private int HsGenLocEnd, MmGenLocEnd = 0;
	private String HsUniprot, MmUniprot = "";
	private Map<String,Object> geneData = null;

	
	/**
	 * @param String
	 * 				GeneID
	 */
	public ProteinBox (final String GeneID) {
		
	}
	
	public void setValuesFromSource(int GeneID) {
		//TODO uses a local file for testing right now, fix this
		ObjectMapper mapper = new ObjectMapper();
		try {
			geneData = mapper.readValue(new File("410.json"), Map.class);
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
		
		System.out.println(geneData.get("name"));
	}
	
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



	public void setPDB(String[] pDB) {
		PDB = pDB;
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



	public void setHGNCid(int hGNCid) {
		HGNCid = hGNCid;
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



	public void setAltSymbols(String[] altSymbols) {
		this.altSymbols = altSymbols;
	}



	public String[] getAltSymbols() {
		return altSymbols;
	}



	public void setOMIM(int oMIM) {
		OMIM = oMIM;
	}



	public int getOMIM() {
		return OMIM;
	}



	public void setECnumber(int eCnumber) {
		ECnumber = eCnumber;
	}



	public int getECnumber() {
		return ECnumber;
	}



	public void setHomologene(int homologene) {
		Homologene = homologene;
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



	public void setGoFunctions(String[] goFunctions) {
		GoFunctions = goFunctions;
	}



	public String[] getGoFunctions() {
		return GoFunctions;
	}



	public void setGoComponents(String[] goComponents) {
		GoComponents = goComponents;
	}



	public String[] getGoComponents() {
		return GoComponents;
	}



	public void setGoProcess(String[] goProcess) {
		GoProcess = goProcess;
	}



	public String[] getGoProcess() {
		return GoProcess;
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



	public void setHsRefSeqProtein(String hsRefSeqProtein) {
		HsRefSeqProtein = hsRefSeqProtein;
	}



	public String getHsRefSeqProtein() {
		return HsRefSeqProtein;
	}



	public void setHsRefSeqmRNA(String hsRefSeqmRNA) {
		HsRefSeqmRNA = hsRefSeqmRNA;
	}



	public String getHsRefSeqmRNA() {
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



	public void setMmRefSeqProtein(String mmRefSeqProtein) {
		MmRefSeqProtein = mmRefSeqProtein;
	}



	public String getMmRefSeqProtein() {
		return MmRefSeqProtein;
	}



	public void setMmRefSeqmRNA(String mmRefSeqmRNA) {
		MmRefSeqmRNA = mmRefSeqmRNA;
	}



	public String getMmRefSeqmRNA() {
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
