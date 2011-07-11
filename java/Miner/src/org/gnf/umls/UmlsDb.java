/**
 * 
 */
package org.gnf.umls;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Interact with a local installation of the UMLS
 * @author bgood
 *
 */
public class UmlsDb {

	java.sql.Connection con;
	String dburl = "jdbc:mysql://localhost:3306/umls";
	PreparedStatement map2source;
	PreparedStatement typeAbbreviationlookup;
	PreparedStatement getGroupForTypeIds;
	
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
		
		UmlsDb d = new UmlsDb();
		System.out.println(d.getIdsFromSourceForCUI(cui, abbr));
		System.out.println(d.getIdsFromSourceForCUI(cui, abbr));
		System.out.println(d.getIdsFromSourceForCUI(cui, abbr));
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
			System.err.println("-----SQLException-----");
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
			System.err.println("-----SQLException-----");
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
			System.err.println("-----SQLException-----");
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
