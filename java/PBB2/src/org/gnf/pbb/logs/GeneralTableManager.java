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
import java.util.List;

import org.gnf.pbb.exceptions.FatalException;
import org.gnf.pbb.exceptions.NonFatalException;

public class GeneralTableManager {
	private final static String DBNAME = "PBBGeneral.db";
	
	public GeneralTableManager() {
		
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
		} catch (Exception e) {
			e.printStackTrace();
			throw new FatalException("Error setting up general database.");
		}
	}
	
	public static void init() {
		init(false);	// If nothing is specified, don't force an update.
	}
	
	public static Connection connect() {
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
				str = br.readLine();		// This is the most succinct way of reading a file line-by-line
			}								// I've found so far...
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

	public static void main(String[] args) {
		try{
			init(false);
		} catch (NonFatalException nfe) {
			// pass... db already created
		}
		populateFromFile("/Users/eclarke/Development/out2.txt");
		System.out.println(findPBBTargets(Integer.parseInt(args[0])));
	}

	public static void printTest() {
		try {
			Connection dbconn = connect();
			Statement stat = dbconn.createStatement();
			ResultSet results = stat.executeQuery("select * from general;");
			while (results.next()) {
				System.out.println(results.getString("gene"));
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
}
