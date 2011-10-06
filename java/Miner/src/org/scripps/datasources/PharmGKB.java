/**
 * 
 */
package org.scripps.datasources;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.scripps.ontologies.mesh.MeshRDF;

import com.hp.hpl.jena.ontology.OntClass;

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
			//Map<String,Set<String>> gene_meshes = gkb.getGeneMeshLinks();
			//System.out.println(gene_meshes.size());
			//gkb.populateDiseaseDb("/Users/bgood/data/bioinfo/pharmgkb/diseases.tsv");
			gkb.populateExpandedGeneMeshDb();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}	


	/**
	 * for all the gene-mesh combinations in pgkb, add the ancestors for the mesh term
	 * @throws ClassNotFoundException
	 */
	public void populateExpandedGeneMeshDb() throws ClassNotFoundException{
		Map<String, Set<String>> mesh_parents = new TreeMap<String, Set<String>>();
		MeshRDF mesh = new MeshRDF();
		System.out.println("loaded mesh");
		// load the sqlite-JDBC driver using the current class loader
		Class.forName("org.sqlite.JDBC");
		Connection connection = null;
		try
		{
			int counter = 0; int rc = 0;
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+db_loc);
			PreparedStatement insert = connection.prepareStatement("insert into pgkb_gene_mesh_expanded values(?,?,?,?)");
			Statement statement = connection.createStatement();
			//get the data
			ResultSet rslt = statement.executeQuery("select e1_name, entrez_id, e2_name, mesh_id from pgkb_gene_mesh");
			while(rslt.next()){
				rc++;
				String gene = rslt.getString("entrez_id");
				String mesh_id = rslt.getString("mesh_id");
				String e1_name = rslt.getString("e1_name");
				String e2_name = rslt.getString("e2_name");

				Set<String> parents = mesh_parents.get(mesh_id);
				if(parents==null){
					OntClass c = mesh.getMeshConceptById(mesh_id);
					if(c!=null){
						parents = new HashSet<String>();
						Set<String> uris = mesh.getFamilyIds(c, true);
						for(String uri : uris){
							//"http://www.nlm.nih.gov/mesh/"+id+"#concept
							String parent = uri.replace("http://www.nlm.nih.gov/mesh/", "");
							parent = parent.replace("#concept", "");
							parents.add(parent);
						}
						mesh_parents.put(mesh_id, parents);
					}else{
						mesh_parents.put(mesh_id, new HashSet<String>());
					}
					counter++;
					if(counter%100==0){
						mesh = null;
						mesh = new MeshRDF(); //jena memory problem hack
						System.out.println("\nReloading mesh again\n");
					}
				}
				if(parents!=null){
					for(String parent : parents){
						insert.clearParameters();
						insert.setString(1, e1_name); insert.setString(2, gene); insert.setString(3, e2_name); insert.setString(4,parent);
						insert.executeUpdate();
					}
				}			
				
				System.out.println(rc +" ");
			}
			statement.close();

		}
		catch(SQLException e)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getMessage());
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

	public Map<String,Set<String>> getGeneMeshLinks() throws ClassNotFoundException{
		Map<String,Set<String>> gene_meshes = new HashMap<String,Set<String>>();
		// load the sqlite-JDBC driver using the current class loader
		Class.forName("org.sqlite.JDBC");

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+db_loc);
			String get_gene_diseases = "" +
			"select e1_id,e1_name,entrez_id,e2_name,external_ids " +
			"from pgkb_rel,pgkb_gene,pgkb_disease " +
			"where e1_type = 'Gene' and e2_type = 'Disease' and e1_id=pgkb_gene.pgkb_acc and e2_id=pgkb_disease.pgkb_acc";
			Statement statement = connection.createStatement();
			ResultSet r = statement.executeQuery(get_gene_diseases);
			//MeSH:D015430(Weight Gain),SnoMedCT:161831008(Weight increasing),SnoMedCT:262286000(Weight increased),SnoMedCT:8943002(Weight gain finding),UMLS:C0043094(C0043094)
			while(r.next()){
				String gene_id = r.getString("entrez_id");
				Set<String> meshes = gene_meshes.get(gene_id);
				String disease = r.getString("e2_name");
				String ext = r.getString("external_ids");
				if(ext!=null){
					String[] ids = ext.split(",");
					for(String id : ids){
						if(id.startsWith("MeSH")){
							String mesh = id.substring(5);
							mesh = mesh.substring(0,mesh.indexOf("("));
							if(meshes==null){
								meshes = new HashSet<String>();
							}
							meshes.add(mesh);
						}
					}
					if(meshes!=null){
						gene_meshes.put(gene_id, meshes);
					}
				}
			}

		}
		catch(SQLException e)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getMessage());
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
		return gene_meshes;
	}

	public void populateDatabase(){
		try {
			populateGeneDb("/Users/bgood/data/bioinfo/pharmgkb/genes_clean.txt");
			populateDiseaseDb("/Users/bgood/data/bioinfo/pharmgkb/diseases.tsv");
			populateRelationDb("/Users/bgood/data/bioinfo/pharmgkb/relationships.tsv");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//create table pgkb_rel(e1_type varchar (15), e1_id varchar(15), e1_name varchar(50), e2_type varch(15), e2_id varchar(15), e2_name varchar(50), evidence varchar(300), evidence_types varchar(30),pharmacodynamic varchar(5), pharmacokinetic varchar(5));	
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
		String mesh_id;
		String umls_id;
		String snomed_id;


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

		public String getMesh_id() {
			return mesh_id;
		}

		public void setMesh_id(String mesh_id) {
			this.mesh_id = mesh_id;
		}

		public String getUmls_id() {
			return umls_id;
		}

		public void setUmls_id(String umls_id) {
			this.umls_id = umls_id;
		}

		public String getSnomed_id() {
			return snomed_id;
		}

		public void setSnomed_id(String snomed_id) {
			this.snomed_id = snomed_id;
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
			PreparedStatement statement = connection.prepareStatement("insert into pgkb_disease values(?,?,?,?,?,?,?,?)");
			//get the data
			BufferedReader f = new BufferedReader(new FileReader(gene_file));
			f.readLine(); // skip header
			String line = f.readLine();
			while(line!=null){
				String[] item = line.split("\t");
				String mesh = "";
				String umls = "";
				String snomed = "";
				statement.clearParameters();
				if(item.length==5){
					Disease d = new Disease(item[0], item[1], item[2], item[3], item[4]);
					if(d.external_ids!=null){
						String[] ids = d.external_ids.split(",");

						for(String id : ids){
							if(id.startsWith("MeSH")){
								if(id.indexOf("(")>0){
									mesh = id.substring(5);
									mesh = mesh.substring(0,mesh.indexOf("("));
								}
							}
							else if(id.startsWith("UMLS")){
								if(id.indexOf("(")>0){
									umls = id.substring(5);
									umls = umls.substring(0,umls.indexOf("("));
								}
							}
							else if(id.startsWith("SnoMedCT")){
								if(id.indexOf("(")>0){
									snomed = id.substring(9);
									snomed = snomed.substring(0,snomed.indexOf("("));
								}
							}
						}


					}
					statement.setString(1, d.pgkb_acc); statement.setString(2, d.name); statement.setString(3, d.other_names); statement.setString(4, d.xrefs); statement.setString(5,d.external_ids);
					statement.setString(6,mesh); statement.setString(7,umls); statement.setString(8,snomed);
					statement.executeUpdate();
				}else if(item.length>1){
					statement.setString(1, item[0]); statement.setString(2, item[1]); statement.setString(3, ""); statement.setString(4, ""); statement.setString(5,"");
					statement.setString(6,"");statement.setString(7,"");statement.setString(8,"");
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
