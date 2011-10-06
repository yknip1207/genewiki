/**
 * 
 */
package org.scripps.nlp.umls;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Interact with a local installation of the UMLS
 * See http://www.nlm.nih.gov/research/umls/implementation_resources/query_diagrams/
 * @author bgood
 *
 */
public class UmlsDb {

	java.sql.Connection con;
	String dburl = "jdbc:mysql://localhost:3306/umls";
	PreparedStatement map2source;
	PreparedStatement typeAbbreviationlookup;
	PreparedStatement getGroupForTypeIds;
	PreparedStatement getGroupForTypeNames;
	PreparedStatement getMeSHCodeFromString;
	PreparedStatement getCuidsFromString;
	PreparedStatement getSemanticTypesFromCUI;

	public UmlsDb() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch(java.lang.ClassNotFoundException e) {
			System.err.print("ClassNotFoundException: ");
			System.err.println(e.getMessage());
		}
		try {
			con = DriverManager.getConnection(dburl,"root", "");
			map2source = con.prepareStatement("SELECT * FROM MRCONSO WHERE CUI = ? and SAB = ? ");
			typeAbbreviationlookup = con.prepareStatement("select * from SRDEF where ABR = ?");
			getGroupForTypeIds = con.prepareStatement("select * from SGROUPS where STY_ID = ?");
			getGroupForTypeNames = con.prepareStatement("select * from SGROUPS where STY_NAME = ?");
			getMeSHCodeFromString = con.prepareStatement("select CODE from MRCONSO where SAB = 'MSH' and STR = ?");
			getCuidsFromString = con.prepareStatement("select CUI from MRCONSO where SAB = ? and STR = ?");
			getSemanticTypesFromCUI = con.prepareStatement("select * from MRSTY where CUI = ?");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String cui = "C0012984";//"C0001807";
		String abbr = "SNOMEDCT";//"GO";
		String atom = "Wounds and Injuries";

		UmlsDb d = new UmlsDb();
		System.out.println("groups "+d.getGroupForStypeName("Neoplastic Process"));
		//System.out.println(d.getSemanticTypesForMeshAtom(atom));
		//		System.out.println(d.getIdsFromSourceForCUI(cui, abbr));
		//		System.out.println(d.getIdsFromSourceForCUI(cui, abbr));
		//		System.out.println(d.getIdsFromSourceForCUI(cui, abbr));
	}

	public Set<String> getSemanticTypesFromCUI(String cui){
		Set<String> codes = new HashSet<String>();
		try {
			getSemanticTypesFromCUI.clearParameters();
			getSemanticTypesFromCUI.setString(1, cui);
			ResultSet srs = getSemanticTypesFromCUI.executeQuery();
			while(srs.next()){
				String code = srs.getString("STY");
				codes.add(code);
			}
			srs.close();
		}  catch(SQLException ex) {
			System.err.println("-----SQLException-----");
			System.err.println("SQLState:  " + ex.getSQLState());
			System.err.println("Message:  " + ex.getMessage());
			System.err.println("Vendor:  " + ex.getErrorCode());
		}
		return codes;
	}


	public Set<String> getSemanticTypesForMeshAtom(String term){
	Set<String> roots = new HashSet<String>();
	Set<String> cuis = getCuidsFromMeSHAtom(term);
	if(cuis!=null&&cuis.size()>0){
		for(String cui : cuis){
			Set<String> types = getSemanticTypesFromCUI(cui);
			if(types!=null){
				roots.addAll(types);
			}
		}
	}
	return roots;
	}
	
	/**
	 * See http://www.nlm.nih.gov/mesh/2011/mesh_browser/MeSHtree.html
	 * @param term
	 * @return
	 */
//	public Set<String> getMeshRootsFromAtom(String term){
//		Set<String> roots = new HashSet<String>();
//		Set<String> cuis = getCuidsFromMeSHAtom(term);
//		if(cuis!=null&&cuis.size()>0){
//
//			for(String cui : cuis){
//				Set<String> types = getSourceVocabSemanticTypesFromCUI(cui);
//				if(types!=null){
//					for(String code : types){
//						if(code.toLowerCase().startsWith("a")){
//							roots.add("Anatomy");
//						}else if(code.toLowerCase().startsWith("b")){
//							roots.add("Organisms");
//						}else if(code.toLowerCase().startsWith("c")){
//							roots.add("Diseases");
//						}else if(code.toLowerCase().startsWith("d")){
//							roots.add("Chemicals and Drugs");
//						}else if(code.toLowerCase().startsWith("e")){
//							roots.add("Analytical, Diagnostic and Therapeutic Techniques and Equipment");
//						}else if(code.toLowerCase().startsWith("f")){
//							roots.add("Psychiatry and Psychology");
//						}else if(code.toLowerCase().startsWith("g")){
//							roots.add("Phenomena and Processes");
//						}else if(code.toLowerCase().startsWith("h")){
//							roots.add("Disciplines and Occupations");
//						}else if(code.toLowerCase().startsWith("i")){
//							roots.add("Anthropology, Education, Sociology and Social Phenomena");
//						}else if(code.toLowerCase().startsWith("j")){
//							roots.add("Technology, Industry, Agriculture");
//						}else if(code.toLowerCase().startsWith("k")){
//							roots.add("Humanities");
//						}else if(code.toLowerCase().startsWith("l")){
//							roots.add("Information Science");
//						}else if(code.toLowerCase().startsWith("m")){
//							roots.add("Named Groups");
//						}else if(code.toLowerCase().startsWith("n")){
//							roots.add("Health Care");
//						}else if(code.toLowerCase().startsWith("v")){
//							roots.add("Publication Characteristics");
//						}else if(code.toLowerCase().startsWith("z")){
//							roots.add("Geographicals");
//						}
//					}
//				}
//			}
//		}
//		return roots;
//	}

	public Set<String> getCuidsFromMeSHAtom(String term){
		Set<String> codes = new HashSet<String>();
		try {
			getCuidsFromString.clearParameters();
			getCuidsFromString.setString(1, "MSH");
			getCuidsFromString.setString(2, term);
			ResultSet srs = getCuidsFromString.executeQuery();
			while(srs.next()){
				String code = srs.getString("CUI");
				codes.add(code);
			}
			srs.close();
		}  catch(SQLException ex) {
			System.err.println(" getCuidsFromMeSHAtom -----SQLException----- "+term);
			System.err.println("SQLState:  " + ex.getSQLState());
			System.err.println("Message:  " + ex.getMessage());
			System.err.println("Vendor:  " + ex.getErrorCode());
		}
		return codes;
	}

	public Set<String> getMeshCodesFromAtom(String term){
		Set<String> codes = new HashSet<String>();
		try {
			getMeSHCodeFromString.clearParameters();
			getMeSHCodeFromString.setString(1, term);
			ResultSet srs = getMeSHCodeFromString.executeQuery();
			while(srs.next()){
				String code = srs.getString("CODE");
				codes.add(code);
			}
			srs.close();
		}  catch(SQLException ex) {
			System.err.println(" getMeshCodesFromAtom -----SQLException----- "+term);
			System.err.println("SQLState:  " + ex.getSQLState());
			System.err.println("Message:  " + ex.getMessage());
			System.err.println("Vendor:  " + ex.getErrorCode());
		}
		return codes;
	}

	public List<String> getIdsFromSourceForCUI(String CUI, String source_vocab_abbr){
		List<String> ids = new ArrayList<String>();

		try {
			map2source.clearParameters();
			map2source.setString(1, CUI);
			map2source.setString(2, source_vocab_abbr);
			ResultSet srs = map2source.executeQuery();
			while(srs.next()){
				ids.add(srs.getString("CODE"));
			}
			srs.close();
		}  catch(SQLException ex) {
			System.err.println("getIdsFromSourceForCUI-----SQLException----- "+CUI);
			System.err.println("SQLState:  " + ex.getSQLState());
			System.err.println("Message:  " + ex.getMessage());
			System.err.println("Vendor:  " + ex.getErrorCode());
		}
		return ids;
	}

	public String getSemanticTypeInfoFromAbbreviation(String abbr){
		String type = "";

		try {
			typeAbbreviationlookup.clearParameters();
			typeAbbreviationlookup.setString(1, abbr);
			ResultSet srs = typeAbbreviationlookup.executeQuery();
			if(srs.next()){
				type = srs.getString("STY_RL")+"\t"+srs.getString("UI");
			}else{
				type = null;
			}

			srs.close();
		}  catch(SQLException ex) {
			System.err.println("getSemanticTypeInfoFromAbbreviation -----SQLException----- "+abbr);
			System.err.println("SQLState:  " + ex.getSQLState());
			System.err.println("Message:  " + ex.getMessage());
			System.err.println("Vendor:  " + ex.getErrorCode());
		}
		return type;
	}

	public String getGroupForStype(String type_id){
		String type = "";

		try {
			getGroupForTypeIds.clearParameters();
			getGroupForTypeIds.setString(1, type_id);
			ResultSet srs = getGroupForTypeIds.executeQuery();
			if(srs.next()){
				type = srs.getString("GROUP_NAME");
			}else{
				type = null;
			}

			srs.close();
		}  catch(SQLException ex) {
			System.err.println("getGroupForStype -----SQLException----- "+type_id);
			System.err.println("SQLState:  " + ex.getSQLState());
			System.err.println("Message:  " + ex.getMessage());
			System.err.println("Vendor:  " + ex.getErrorCode());
		}
		return type;
	}
	public String getGroupForStypeName(String type_name){
		String type = "";

		try {
			getGroupForTypeNames.clearParameters();
			getGroupForTypeNames.setString(1, type_name);
			ResultSet srs = getGroupForTypeNames.executeQuery();
			if(srs.next()){
				type = srs.getString("GROUP_NAME");
			}else{
				type = null;
			}

			srs.close();
		}  catch(SQLException ex) {
			System.err.println("getGroupForStypeName -----SQLException----- "+type_name);
			System.err.println("SQLState:  " + ex.getSQLState());
			System.err.println("Message:  " + ex.getMessage());
			System.err.println("Vendor:  " + ex.getErrorCode());
		}
		return type;
	}	


	public java.sql.Connection getCon() {
		return con;
	}


	public void setCon(java.sql.Connection con) {
		this.con = con;
	}

}
