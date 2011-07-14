package org.gnf.pbb.logs;

import java.io.File;
import java.sql.*;
import java.util.Calendar;
import java.util.logging.Logger;

import org.gnf.pbb.Configs;
import org.gnf.pbb.exceptions.ConfigException;
import org.gnf.pbb.exceptions.ExceptionHandler;
import org.gnf.pbb.exceptions.PbbExceptionHandler;
import org.gnf.pbb.exceptions.Severity;
import org.gnf.pbb.exceptions.ValidationException;

public class DatabaseManager {
	private Logger logger = Logger.getLogger(DatabaseManager.class.getName());
	public ExceptionHandler exHandler;
	public boolean locked;
	public String dbName;
	
	public DatabaseManager() {
		exHandler = PbbExceptionHandler.INSTANCE;
		try {
			dbName = "pbb.db";
		} catch (ConfigException e) {
			exHandler.pass(e, Severity.FATAL);
		}
	}
	
	public static DatabaseManager factory(ExceptionHandler _exHandler) {
		DatabaseManager dbmanager = new DatabaseManager(); 
		dbmanager.exHandler = _exHandler;
		dbmanager.locked = false;
		return dbmanager;
	}
	
	
	
	
	/**
	 * Creates the database and table we're using for cataloging PBB's work.
	 * @throws ValidationException when database is already found
	 */
	public void init() throws ValidationException {
		if ((new File(dbName).exists())) {
			logger.info("Database found at "+dbName);
			throw new ValidationException();
		}
		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:pbb.db");
			Statement stat = conn.createStatement();
			stat.executeUpdate("drop table if exists changes;");
			stat.executeUpdate("create table changes (gene, field, oldvalues, newvalues, time);");
			stat.executeUpdate("drop table if exists missing;");
			stat.executeUpdate("create table missing (gene, field, wikiValues);");
			conn.close();
		} catch (SQLException e) {
			exHandler.pass(e, Severity.FATAL);
		} catch (ClassNotFoundException e) {
			exHandler.pass(e, Severity.FATAL);
		}
	}
	
	/**
	 * Should call this to open a connection; allows connection to be locked
	 * as SQLite cannot have multiple open connections. Close the connection
	 * using {@link close(Connection)}.
	 * @return connection to pbb.db
	 * @throws SQLException
	 */
	public Connection connect() throws SQLException {
		if (locked) {
			throw new SQLException("Cannot open connection to database: database locked.");
		} else {
			locked = true;
			try { Class.forName("org.sqlite.JDBC");} 
				catch (ClassNotFoundException e) { e.printStackTrace();}
			return DriverManager.getConnection("jdbc:sqlite:pbb.db");
		}
	}
	
	/**
	 * Closes the connection and unlocks it so another connection can be made.
	 * @param conn
	 */
	public void close(Connection conn) {
		try {
			conn.close();
			locked = false;
		} catch (SQLException e) {
			e.printStackTrace();
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
	public void addChange(String gene, String field, String oldvalue, String newvalue) throws SQLException {
		Connection dbconnect = connect();
		PreparedStatement prep = dbconnect.prepareStatement("insert into changes values (?, ?, ?, ?, ?);");
		Calendar cal = Calendar.getInstance();
		long timestamp = cal.getTime().getTime();
		prep.setString(1, gene);
		prep.setString(2, field);
		prep.setString(3, oldvalue);
		prep.setString(4, newvalue);
		prep.setLong(5, timestamp);
		prep.addBatch();
		
		dbconnect.setAutoCommit(false);
		prep.executeBatch();
		dbconnect.setAutoCommit(true);
		
		close(dbconnect);
	}
	
	/**
	 * Makes a note of information missing from mygene.info in the table 'missing'
	 * @param gene
	 * @param field
	 * @param wikiValue
	 * @throws SQLException
	 */
	public void addMissingFromSource(String gene, String field, String wikiValue) throws SQLException {
		Connection dbconnect = connect();
		PreparedStatement prep = dbconnect.prepareStatement("insert into missing values (?, ?, ?);");
		prep.setString(1, gene);
		prep.setString(2, field);
		prep.setString(3, wikiValue);
		
		dbconnect.setAutoCommit(false);
		prep.executeBatch();
		dbconnect.setAutoCommit(true);
		
		close(dbconnect);
	}
	
	public String printChanges() throws SQLException {
		Connection dbconnect = connect();
		Statement stat = dbconnect.createStatement();
		ResultSet results = stat.executeQuery("select * from changes;");
		StringBuilder sb = new StringBuilder();
		while (results.next()) {
			String geneId = results.getString("gene");
			String field = results.getString("field");
			String oldvalue = results.getString("oldvalues");
			String newvalue = results.getString("newvalues");
			long timeStamp = results.getLong("time");
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(timeStamp);
			String time = cal.getTime().toString();
			//sb.append(String.format("Gene,Field,Old,New,Time\n"));
			sb.append(String.format("%s,%s,\"%s\",\"%s\",%s\n", geneId, field, oldvalue, newvalue, time));
		}
		results.close();
		close(dbconnect);
		return sb.toString();
	}
	
	public String printMissing() throws SQLException {
		Connection dbconnect = connect();
		Statement stat = dbconnect.createStatement();
		ResultSet results = stat.executeQuery("select * from missing;");
		StringBuilder sb = new StringBuilder();
		while (results.next()) {
			String geneId = results.getString("gene");
			String field = results.getString("field");
			String wikiValue = results.getString("wikiValue");
			sb.append(String.format("Gene,Field,Values from Wikipedia\n"));
			sb.append(String.format("%s,%s,\"%s\"\n", geneId, field, wikiValue));
			System.out.println("Errata added to database");
		}
		results.close();
		close(dbconnect);
		return sb.toString();
	}

}
