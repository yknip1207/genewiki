/**
 * 
 */
package org.genewiki.db;

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import org.sqlite.*;

/**
 * Manages the Metrics database.
 * @author eclarke
 *
 */
public enum MetricsDatabase {

	instance;
	
	private final Object lock = new Object(); // Synchronize on this to prevent conflicting write locks
	private final Object lock2 = new Object();
	private final String dbName = "metrics.db";
	
	private MetricsDatabase() { }
	
	public static void main(String[] args) throws SQLException, InterruptedException {
		MetricsDatabase db = MetricsDatabase.instance;
		db.buildDb(db.dbName, true);
	}
	
	public void insertPageRow(
			String title, int id, int entrez, int revs) throws SQLException {
		
		System.out.println("Waiting to write to Page table...");
		synchronized (lock) {
			System.out.println("Writing to Page table...");
			Connection connection = this.connect();
			PreparedStatement prep = connection.prepareStatement("insert into pages values (?,?,?,?);");
			prep.setString(1, title);
			prep.setInt(2, id);
			prep.setInt(3, entrez);
			prep.setInt(4, revs);
			prep.addBatch();
			connection.setAutoCommit(false);
			prep.executeBatch();
			connection.setAutoCommit(true);
			connection.close();
		}
	}
	
	public void insertPageViewRow(
			int id, int month, int year, int views) throws SQLException {
		
		System.out.println("Waiting to write to Page_view table...");
		synchronized (lock) {
			System.out.println("Writing to Page_view table...");
			Connection connection = this.connect();
			PreparedStatement prep = connection.prepareStatement("insert into page_views values (?,?,?,?);");
			prep.setInt(1, id);
			prep.setInt(2, month);
			prep.setInt(3, year);
			prep.setInt(4, views);
			prep.addBatch();
			connection.setAutoCommit(false);
			prep.executeBatch();
			connection.setAutoCommit(true);
			connection.close();
		}
	}
	
	public void insertEditorRow(
			String editor, boolean is_bot, int page_id, long bytes_changed) throws SQLException {
		System.out.println("Waiting to write to Editors table...");
		synchronized(lock) {
			System.out.println("Writing to Editors table...");
			Connection connection = this.connect();
			PreparedStatement prep = connection.prepareStatement("insert into editors values (?,?,?,?);");
			prep.setString(1, editor);
			prep.setBoolean(2, is_bot);
			prep.setInt(3, page_id);
			prep.setLong(4, bytes_changed);
			prep.addBatch();
			connection.setAutoCommit(false);
			prep.executeBatch();
			connection.setAutoCommit(true);
			connection.close();
		}
	}
	
	public void insertPageInfoRow(
			int id, int bytes, int words, int links_out, int links_in, int external,
			int pubmed_refs, int redirects, int sentences, int media) throws SQLException {
		try { Class.forName("org.sqlite.JDBC"); }
		catch (ClassNotFoundException e) { e.printStackTrace(); };
		System.out.println("Waiting to write to Page_info table...");
		synchronized (lock) {
			System.out.println("Writing to Page_info table...");
			Connection connection = this.connect();
			PreparedStatement prep = connection.prepareStatement("insert into page_info values (" +
					"?,?,?,?,?,?,?,?,?,?);");
			prep.setInt(1, id);
			prep.setInt(2, bytes);
			prep.setInt(3, words);
			prep.setInt(4, links_out);
			prep.setInt(5, links_in);
			prep.setInt(6, external);
			prep.setInt(7, pubmed_refs);
			prep.setInt(8, redirects);
			prep.setInt(9, sentences);
			prep.setInt(10, media);
			prep.addBatch();
			connection.setAutoCommit(false);
			prep.executeBatch();
			connection.setAutoCommit(true);
			connection.close();
		}
		
	}
	
	public void insertRevisionRow(
			long rev_id, Calendar date, int page_id, long bytes_changed) throws SQLException {
		try { Class.forName("org.sqlite.JDBC"); }
		catch (ClassNotFoundException e) { e.printStackTrace(); };
		System.out.println("Waiting to write to Revisions table...");
		synchronized (lock) {
			System.out.println("Writing to revisions table...");
			Connection connection = this.connect();
			PreparedStatement prep = connection.prepareStatement("insert into revisions values (?,?,?,?);");
			prep.setLong(1, rev_id);
			prep.setDate(2, new Date(date.getTimeInMillis()));
			prep.setInt(3, page_id);
			prep.setLong(4, bytes_changed);
			prep.addBatch();
			connection.setAutoCommit(false);
			prep.executeBatch();
			connection.setAutoCommit(true);
			connection.close();
		}
	}
	
	
	/**
	 * Creates database with the following tables:
	 * <code>
	 * <p>pages: String title | int id | int entrez | int views | int bytes_changed | int revs
	 * <p>page_views: int page_id | int month | int year | int views
	 * <p>editors: String name | bool isBot | int page_id | int bytes_changed
	 * <p>page_info: int page_id | int bytes | int words | int links_out | int links_in | int external
	 * | int pubmed_refs | int redirects | int sentences | int media
	 * <p>revisions: int rev_id | Date date | int page_id | int bytes_changed
	 * <p>ontology: int page_id | String ontology_term | int ont_id | Date date
	 * @param db name
	 * @param force overwrite 
	 * @return
	 */
	public boolean buildDb(String db, boolean force) {
		if ((new File(db).exists()) && !force)
			return false;
			
		try {
			Class.forName("org.sqlite.JDBC");
			Connection con = DriverManager.getConnection("jdbc:sqlite:"+db);
			Statement stat = con.createStatement();
			stat.executeUpdate("drop table if exists pages;");
			stat.executeUpdate("create table pages(title, id, entrez, revs);");
			
			stat.executeUpdate("drop table if exists page_views;");
			stat.executeUpdate("create table page_views(page_id, month, year, views);");
			
			stat.executeUpdate("drop table if exists editors;");
			stat.executeUpdate("create table editors(name, is_bot, page_id, bytes_changed);");
			
			stat.executeUpdate("drop table if exists page_info;");
			stat.executeUpdate("create table page_info(" +
					"page_id, bytes, words, links_out, links_in, external, pubmed_refs, redirects, sentences, media);");

			stat.executeUpdate("drop table if exists revisions;");
			stat.executeUpdate("create table revisions(rev_id, time, page_id, bytes_changed);");
			
			stat.executeUpdate("drop table if exists ontology;");
			stat.executeUpdate("create table ontology(page_id, ontology_term, ont_id, ontology, date);");			
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private Connection connect() throws SQLException {
		try { Class.forName("org.sqlite.JDBC"); }
		catch (ClassNotFoundException e) { e.printStackTrace(); };
		return DriverManager.getConnection("jdbc:sqlite:"+dbName);
	}
	
	
	
}
