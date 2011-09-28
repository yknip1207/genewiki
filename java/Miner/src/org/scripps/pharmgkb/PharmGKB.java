/**
 * 
 */
package org.scripps.pharmgkb;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TreeSet;

/**
 * @author bgood
 *
 */
public class PharmGKB {

	public static String db_loc = "/Users/bgood/data/bioinfo/pharmgkb/pharmgkb.db";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PharmGKB gkb = new PharmGKB();
		try {
//			gkb.populateGeneDb("/Users/bgood/data/bioinfo/pharmgkb/genes_clean.txt");
//			gkb.populateDiseaseDb("/Users/bgood/data/bioinfo/pharmgkb/diseases.tsv");
			gkb.populateRelationDb("/Users/bgood/data/bioinfo/pharmgkb/relationships.tsv");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

//create table pgkb_rel(e1_type varchar (15), e1_id varchar(15), e1_name varchar(50), e2_type varch(15), e2_id varchar(15), e3_name varchar(50), evidence varchar(300), evidence_types varchar(30));	
	public class Relation{
		String e1_type;
		String e1_id;
		String e1_name;
		String e2_type;
		String e2_id;
		String e2_name;
		String evidence;
		String evidence_types;
		String pharmacodynamic;
		String pharmacokinetic;
		
		public String getE1_type() {
			return e1_type;
		}
		public void setE1_type(String e1_type) {
			this.e1_type = e1_type;
		}
		public String getE1_id() {
			return e1_id;
		}
		public void setE1_id(String e1_id) {
			this.e1_id = e1_id;
		}
		public String getE1_name() {
			return e1_name;
		}
		public void setE1_name(String e1_name) {
			this.e1_name = e1_name;
		}
		public String getE2_type() {
			return e2_type;
		}
		public void setE2_type(String e2_type) {
			this.e2_type = e2_type;
		}
		public String getE2_id() {
			return e2_id;
		}
		public void setE2_id(String e2_id) {
			this.e2_id = e2_id;
		}
		public String getE2_name() {
			return e2_name;
		}
		public void setE2_name(String e2_name) {
			this.e2_name = e2_name;
		}
		public String getEvidence() {
			return evidence;
		}
		public void setEvidence(String evidence) {
			this.evidence = evidence;
		}
		public String getEvidence_types() {
			return evidence_types;
		}
		public void setEvidence_types(String evidence_types) {
			this.evidence_types = evidence_types;
		}
		public String getPharmacodynamic() {
			return pharmacodynamic;
		}
		public void setPharmacodynamic(String pharmacodynamic) {
			this.pharmacodynamic = pharmacodynamic;
		}
		public String getPharmacokinetic() {
			return pharmacokinetic;
		}
		public void setPharmacokinetic(String pharmacokinetic) {
			this.pharmacokinetic = pharmacokinetic;
		}
		
	}
	
	
//create table pgkb_rel(e1_type varchar (15), e1_id varchar(15), e1_name varchar(50), e2_type varch(15), e2_id varchar(15), e3_name varchar(50), evidence varchar(300), evidence_types varchar(30));	
	public void populateRelationDb(String file) throws ClassNotFoundException{

		// load the sqlite-JDBC driver using the current class loader
		Class.forName("org.sqlite.JDBC");

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+db_loc);
			PreparedStatement statement = connection.prepareStatement("insert into pgkb_rel values(?,?,?,?,?,?,?,?,?,?)");
			//get the data
			BufferedReader f = new BufferedReader(new FileReader(file));
			f.readLine(); // skip header
			String line = f.readLine();
			while(line!=null){
				String[] item = line.split("\t");
				statement.clearParameters();
				if(item.length>=6){
					Relation r = new Relation();
					r.setE1_type(item[0].split(":")[0]); r.setE1_id(item[0].split(":")[1]); r.setE1_name(item[1]); 
					r.setE2_type(item[2].split(":")[0]); r.setE2_id(item[2].split(":")[1]); r.setE2_name(item[3]); 
					r.setEvidence(item[4]);	r.setEvidence_types(item[5]);
					String pd = ""; String pk = "";
					if(item.length>6){
						pd = item[6];
					}
					if(item.length>7){
						pk = item[7];
					}				
					statement.setString(1, r.e1_type); statement.setString(2, r.e1_id); statement.setString(3, r.e1_name); 
					statement.setString(4, r.e2_type); statement.setString(5, r.e2_id); statement.setString(6, r.e2_name); 
					statement.setString(7, r.evidence); statement.setString(8, r.evidence_types);
					statement.setString(9, pd); statement.setString(10, pk);
					statement.executeUpdate();
				}else{
					System.out.println(item.length+" "+line);
				}
				line = f.readLine();
			}
		}
		catch(SQLException e)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getMessage());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(connection != null)
					connection.close();
			}
			catch(SQLException e)
			{
				// connection close failed.
				System.err.println(e);
			}
		}

	}
	
	public class Disease{
		String pgkb_acc;
		String name;
		String other_names;
		String xrefs;
		String external_ids;
		
		
//create table pgkb_disease(pgkb_acc varchar(15), name varchar(50), other_names varchar(100), xrefs varchar(300), external_ids varchar(300));		
		public Disease(String pgkb_acc, String name, String other_names,
				String xrefs, String external_ids) {
			super();
			this.pgkb_acc = pgkb_acc;
			this.name = name;
			this.other_names = other_names;
			this.xrefs = xrefs;
			this.external_ids = external_ids;
		}
		public String getPgkb_acc() {
			return pgkb_acc;
		}
		public void setPgkb_acc(String pgkb_acc) {
			this.pgkb_acc = pgkb_acc;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getOther_names() {
			return other_names;
		}
		public void setOther_names(String other_names) {
			this.other_names = other_names;
		}
		public String getXrefs() {
			return xrefs;
		}
		public void setXrefs(String xrefs) {
			this.xrefs = xrefs;
		}
		public String getExternal_ids() {
			return external_ids;
		}
		public void setExternal_ids(String external_ids) {
			this.external_ids = external_ids;
		}
		
	}
	
	public void populateDiseaseDb(String gene_file) throws ClassNotFoundException{

		// load the sqlite-JDBC driver using the current class loader
		Class.forName("org.sqlite.JDBC");

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+db_loc);
			PreparedStatement statement = connection.prepareStatement("insert into pgkb_disease values(?,?,?,?,?)");
			//get the data
			BufferedReader f = new BufferedReader(new FileReader(gene_file));
			f.readLine(); // skip header
			String line = f.readLine();
			while(line!=null){
				String[] item = line.split("\t");
				statement.clearParameters();
				if(item.length==5){
					Disease d = new Disease(item[0], item[1], item[2], item[3], item[4]);
					statement.setString(1, d.pgkb_acc); statement.setString(2, d.name); statement.setString(3, d.other_names); statement.setString(4, d.xrefs); statement.setString(5,d.external_ids);
					statement.executeUpdate();
				}else if(item.length>1){
					statement.setString(1, item[0]); statement.setString(2, item[1]); statement.setString(3, ""); statement.setString(4, ""); statement.setString(5,"");
					statement.executeUpdate();
				}
				line = f.readLine();
			}
		}
		catch(SQLException e)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getMessage());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(connection != null)
					connection.close();
			}
			catch(SQLException e)
			{
				// connection close failed.
				System.err.println(e);
			}
		}

	}
	
	public class Gene{
		String pgkb_acc;
		String entrez_id;
		String ensemble_id;
		String uniprot_id;
		String symbol;
		String genotyped;
		String is_vip;
		String pd;
		String pk;
		String has_variant;



		public Gene(String pgkb_acc, String entrez_id, String ensemble_id,
				String uniprot_id, String symbol, String genotyped,
				String is_vip, String pd, String pk, String has_variant) {
			super();
			this.pgkb_acc = pgkb_acc;
			this.entrez_id = entrez_id;
			this.ensemble_id = ensemble_id;
			this.uniprot_id = uniprot_id;
			this.symbol = symbol;
			this.genotyped = genotyped;
			this.is_vip = is_vip;
			this.pd = pd;
			this.pk = pk;
			this.has_variant = has_variant;
		}
		public String getPgkb_acc() {
			return pgkb_acc;
		}
		public void setPgkb_acc(String pgkb_acc) {
			this.pgkb_acc = pgkb_acc;
		}
		public String getEntrez_id() {
			return entrez_id;
		}
		public void setEntrez_id(String entrez_id) {
			this.entrez_id = entrez_id;
		}
		public String getEnsemble_id() {
			return ensemble_id;
		}
		public void setEnsemble_id(String ensemble_id) {
			this.ensemble_id = ensemble_id;
		}
		public String getUniprot_id() {
			return uniprot_id;
		}
		public void setUniprot_id(String uniprot_id) {
			this.uniprot_id = uniprot_id;
		}
		public String getSymbol() {
			return symbol;
		}
		public void setSymbol(String symbol) {
			this.symbol = symbol;
		}
		public String getGenotyped() {
			return genotyped;
		}
		public void setGenotyped(String genotyped) {
			this.genotyped = genotyped;
		}
		public String getIs_vip() {
			return is_vip;
		}
		public void setIs_vip(String is_vip) {
			this.is_vip = is_vip;
		}
		public String getPd() {
			return pd;
		}
		public void setPd(String pd) {
			this.pd = pd;
		}
		public String getPk() {
			return pk;
		}
		public void setPk(String pk) {
			this.pk = pk;
		}
		public String getHas_variant() {
			return has_variant;
		}
		public void setHas_variant(String has_variant) {
			this.has_variant = has_variant;
		}

	}

	/**
	 * CREATE TABLE pgkb_gene(pgkb_acc varchar(15), 
	 entrez_id varchar(15),ensemble_id varchar(20), uniprot_id varchar(15), 
	 symbol varchar(15), genotyped varchar(5), is_vip varchar(5), pd varchar(5), 
	 pk varchar(5), has_variant varchar(5));

	 * @param gene_file
	 * @throws ClassNotFoundException
	 */
	public void populateGeneDb(String gene_file) throws ClassNotFoundException{

		// load the sqlite-JDBC driver using the current class loader
		Class.forName("org.sqlite.JDBC");

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+db_loc);
			//get the data
			BufferedReader f = new BufferedReader(new FileReader(gene_file));
			f.readLine(); // skip header
			String line = f.readLine();
			while(line!=null){
				String[] item = line.split("\t");
				if(item.length==10){
					Gene g = new Gene(item[0], item[1], item[2], item[3], item[4], item[5], item[6], item[7], item[8], item[9]);
					Statement statement = connection.createStatement();
					String insert = "insert into pgkb_gene values('"+g.pgkb_acc+"','"+g.entrez_id+"','"+g.ensemble_id+"','"+g.uniprot_id+"','"+g.symbol+
					"','"+g.genotyped+"','"+g.is_vip+"','"+g.pd+"','"+g.pk+"','"+g.has_variant+"')";
					statement.executeUpdate(insert);
				}
				line = f.readLine();
			}
		}
		catch(SQLException e)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getMessage());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(connection != null)
					connection.close();
			}
			catch(SQLException e)
			{
				// connection close failed.
				System.err.println(e);
			}
		}

	}

}
