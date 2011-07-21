package org.gnf.pbb.logs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.gnf.pbb.exceptions.FatalException;
import org.gnf.pbb.exceptions.NonFatalException;

public class DatabaseManager {
	private final static String DBNAME = "PBBGeneral.db";
	private static Calendar cal = Calendar.getInstance();
	public DatabaseManager() {
		
	}
	
	/**
	 * Initialize the database, clearing all tables and recreating them.
	 * Fails if db exists, unless the 'force' flag is specified as true.
	 * @param force initialization
	 */
	public static void init(boolean force) {
		if ((new File(DBNAME).exists()) && !force) {
			throw new NonFatalException("Database exists; cannot overwrite.");
		}
		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:"+DBNAME);
			Statement stat = conn.createStatement();
			stat.executeUpdate("drop table if exists general;");
			stat.executeUpdate("create table general (gene, updated, fields_changed, time);");
			stat.executeUpdate("drop table if exists changes;");
			stat.executeUpdate("create table changes (gene, field, old, new, time);");
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new FatalException("Error setting up general database.");
		} finally {
		}
	}
	
	private static void init() {
		init(false);	// If nothing is specified, don't force an update.
	}
	
	private static Connection connect() {
		try {
			Class.forName("org.sqlite.JDBC");
			return DriverManager.getConnection("jdbc:sqlite:"+DBNAME);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new FatalException("Error connecting to general database.");
		} catch (SQLException e) {
			e.printStackTrace();
			throw new FatalException("Error connecting to general database.");
		}
	}
	
	public static void populateFromFile(String filename) {
		try {
			init(true);	// forces re-initialization
			Connection dbconn = connect();
			PreparedStatement prep = dbconn.prepareStatement("insert into general values (?, ?, ?, ?);");
			File file = new File(filename);
		
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String str = br.readLine();
			while (str != null) {
				prep.setString(1, str);
				prep.setString(2, "false");
				prep.setString(3, "");
				prep.setString(4, "");
				prep.addBatch();
				str = br.readLine();
			}
			br.close();
			dbconn.setAutoCommit(false);
			prep.executeBatch();
			dbconn.setAutoCommit(true);
			dbconn.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new FatalException("Error reading from database");
		} catch (IOException e) {
			e.printStackTrace();
			throw new FatalException("Error reading from file.");
		}
		
		
	}
/* -- Unit test of sorts -- */ 
//	public static void main(String[] args) {
//		try{
//			init(false);
//			populateFromFile("/Users/eclarke/Development/out2.txt");
//		} catch (NonFatalException nfe) {
//			// pass... db already created
//		}
////		init(true);
////		updateDb("1401", "everything has changed!");
//		populateFromFile("/Users/eclarke/Development/out2.txt");
//		updateDb("1401", "everything has changed");
//		printTest();
//	}

	public static void printTest() {
		try {
			Connection dbconn = connect();
			Statement stat = dbconn.createStatement();
			ResultSet results = stat.executeQuery("select * from general where updated = 'true';");
			while (results.next()) {
				System.out.println(results.getString("gene"));
				System.out.println(results.getString("fields_changed"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new FatalException("Error while testing.");
		}
	}
	
	public static List<String> findPBBTargets(int updateSize) {
		List<String> targets = new ArrayList<String>(updateSize);
		try {
			Connection dbc = connect();
			Statement stat = dbc.createStatement();
			ResultSet results = stat.executeQuery("select gene from general where updated = 'false';");
			int i = 0;
			while (results.next() && i < updateSize) {
				i++;
				targets.add(results.getString("gene"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new FatalException("Cannot connect to general table to find targets for update.");
		}
		return targets;
	}
	
	public static void updateDb(String state, String geneId, String fields_changed) {
		try {
			Connection dbc = connect();
			PreparedStatement prep = dbc.prepareStatement("update general set " +
					"updated=?, fields_changed=?, time=? where gene=?;" );
			long time = cal.getTime().getTime();
			prep.setString(1, state);
			prep.setString(2, fields_changed);
			prep.setLong(3, time);
			prep.setString(4, geneId);
		
			prep.addBatch();
			dbc.setAutoCommit(false);
			prep.executeBatch();
			dbc.setAutoCommit(true);
			dbc.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new NonFatalException("Error updating database with changed id.");
		}
	}
	
	/**
	 * Add a new change entry to the database.
	 * @param gene
	 * @param field
	 * @param oldvalue
	 * @param newvalue
	 * @throws SQLException likely due to non-initialized db or misconstructed call
	 */
	public static void addChange(String gene, String field, String oldvalue, String newvalue) {
		try {
			Connection dbc = connect();
			PreparedStatement prep = dbc.prepareStatement("insert into changes values (?, ?, ?, ?, ?);");
			long timestamp = cal.getTime().getTime();
			prep.setString(1, gene);
			prep.setString(2, field);
			prep.setString(3, oldvalue);
			prep.setString(4, newvalue);
			prep.setLong(5, timestamp);
			prep.addBatch();
			
			dbc.setAutoCommit(false);
			prep.executeBatch();
			dbc.setAutoCommit(true);
			
			dbc.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new NonFatalException("Error updating changes table with field.");
		}
	}

}
