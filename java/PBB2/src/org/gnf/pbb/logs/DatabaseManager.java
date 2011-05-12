package org.gnf.pbb.logs;

import java.io.File;
import java.sql.*;
import java.util.Calendar;
import java.util.logging.Logger;

import org.gnf.pbb.Global;
import org.gnf.pbb.exceptions.ValidationException;

public class DatabaseManager {
	private static Logger logger = Logger.getLogger(DatabaseManager.class.getName());
	public static Global global = Global.getInstance();
	public static boolean locked;
	
	// This class is a singleton
	private static final DatabaseManager instance = new DatabaseManager();
	private DatabaseManager() {
		locked = false;
		try {
			init();
		} catch (ValidationException e) {
			// This is thrown if the database already exists,
			// so we don't need to do anything.
		}
	}
	public static DatabaseManager getInstance() {
		return instance;
	}
	
	
	/**
	 * Creates the database and table we're using for cataloging PBB's work.
	 * @throws ValidationException 
	 */
	public static void init() throws ValidationException {
		if ((new File(global.dbName())).exists()) {
			logger.info("Database found at "+global.dbName());
			throw new ValidationException();
		}
		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:pbb.db");
			Statement stat = conn.createStatement();
			stat.executeUpdate("drop table if exists changes;");
			stat.executeUpdate("create table changes (gene, fields, oldvalues, newvalues, time);");
			conn.close();
		} catch (SQLException e) {
			global.fail(e);
		} catch (ClassNotFoundException e) {
			global.fail(e);
		}
	}
	
	/**
	 * Should call this to open a connection; allows connection to be locked
	 * as SQLite cannot have multiple open connections. Close the connection
	 * using {@link close(Connection)}.
	 * @return connection to pbb.db
	 * @throws SQLException
	 */
	public static Connection connect() throws SQLException {
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
	public static void close(Connection conn) {
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
	
}
