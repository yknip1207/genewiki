/**
 * 
 */
package org.genewiki.db;

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

/**
 * Manages the Metrics database. All methods are 
 * synchronized around an internal lock object 
 * to prevent simultaneous write access attempts 
 * (which would lead to a SQLException- SQLite 
 * has only single-threaded write access.)
 * @author eclarke
 *
 */
public enum MetricsDatabase {

	instance;
	
	private final Object lock = new Object(); // Synchronize on this to prevent conflicting write locks
	private final String dbName = "metrics.db";
	
	private MetricsDatabase() { }
	
	public static void main(String[] args) throws SQLException, InterruptedException {
		MetricsDatabase db = MetricsDatabase.instance;
		db.buildDb(db.dbName, true);
	}
	
	public void insertPageRow(
			String title, int id, int entrez, int revs) throws SQLException {
		synchronized (lock) {
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
	
	public void updatePageInfo(int id, int pubmed_refs, int size) throws SQLException {
		synchronized(lock) {
			Connection connection = this.connect();
			PreparedStatement prep = connection.prepareStatement("update page_info set pubmed_refs=? where page_id=?;");
			prep.setInt(1, pubmed_refs);
			prep.setInt(2, id);
			prep.addBatch();
			connection.setAutoCommit(false);
			prep.executeBatch();
			connection.setAutoCommit(true);
			connection.close();
		}
	}
	
	
	
	public void insertPageViewRow(
			int id, int month, int year, int views, int revs) throws SQLException {
		synchronized (lock) {
			Connection connection = this.connect();
			PreparedStatement prep = connection.prepareStatement("insert into page_views values (?,?,?,?,?);");
			prep.setInt(1, id);
			prep.setInt(2, month);
			prep.setInt(3, year);
			prep.setInt(4, views);
			prep.setInt(5, revs);
			prep.addBatch();
			connection.setAutoCommit(false);
			prep.executeBatch();
			connection.setAutoCommit(true);
			connection.close();
		}
	}
	
	public void insertEditorRow(
			String editor, boolean is_bot, int page_id, long bytes_changed) throws SQLException {
		
		synchronized(lock) {

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
			int id, String title, int entrez, int revs, int bytes, int words, int links_out, int links_in, int external,
			int pubmed_refs, int redirects, int sentences, int media) throws SQLException {

		synchronized (lock) {
			Connection connection = this.connect();
			PreparedStatement prep = connection.prepareStatement("insert into page_info values (" +
					"?,?,?,?,?,?,?,?,?,?,?,?,?);");
			prep.setInt(1, id);
			prep.setString(2, title);
			prep.setInt(3, entrez);
			prep.setInt(4, revs);
			prep.setInt(5, bytes);
			prep.setInt(6, words);
			prep.setInt(7, links_out);
			prep.setInt(8, links_in);
			prep.setInt(9, external);
			prep.setInt(10, pubmed_refs);
			prep.setInt(11, redirects);
			prep.setInt(12, sentences);
			prep.setInt(13, media);
			prep.addBatch();
			connection.setAutoCommit(false);
			prep.executeBatch();
			connection.setAutoCommit(true);
			connection.close();
		}
		
	}
	
	public void insertRevisionRow(
			long rev_id, Calendar date, int page_id, long bytes_changed, String editor, boolean is_bot) throws SQLException {

		synchronized (lock) {
			Connection connection = this.connect();
			PreparedStatement prep = connection.prepareStatement("insert into revisions values (?,?,?,?,?,?);");
			prep.setLong(1, rev_id);
			prep.setDate(2, new Date(date.getTimeInMillis()));
			prep.setInt(3, page_id);
			prep.setLong(4, bytes_changed);
			prep.setString(5, editor);
			prep.setBoolean(6, is_bot);
			prep.addBatch();
			connection.setAutoCommit(false);
			prep.executeBatch();
			connection.setAutoCommit(true);
			connection.close();
		}
	}
	
	public int getRevisionsBetweenDates(int page_id, Calendar start, Calendar end) throws SQLException {
		long fStart = start.getTimeInMillis();
		long fEnd = end.getTimeInMillis();
		synchronized(lock) {
			Connection connection = this.connect();
			Statement statement = connection.createStatement();
			
			ResultSet rs = statement.executeQuery(
					"select count(rev_id) from revisions where time between "+fEnd+" and "+fStart+";");
			connection.close();
			return rs.getInt("count(rev_id)");	
		}
	}
	
	public void updatePageViews(int page_id, int rev_count, int month, int year) throws SQLException {
		synchronized(lock) {
			Connection connection = this.connect();
			PreparedStatement prep = connection.prepareStatement("" +
					"update page_views set rev_count=? where page_id=? and month=? and year=?;");
			prep.setInt(1, rev_count);
			prep.setInt(2, page_id);
			prep.setInt(3, month);
			prep.setInt(4, year);
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
	 * <p>page_views: int page_id | int month | int year | int views
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
			stat.executeUpdate("drop table if exists page_views;");
			stat.executeUpdate("create table page_views(page_id, month, year, views, rev_count);");
			
			stat.executeUpdate("drop table if exists page_info;");
			stat.executeUpdate("create table page_info(" +
					"page_id, title, entrez, revs, bytes, words, links_out, links_in, external, " +
					"pubmed_refs, redirects, sentences, media);");

			stat.executeUpdate("drop table if exists revisions;");
			stat.executeUpdate("create table revisions(rev_id, time, page_id, bytes_changed, editor, is_bot);");
			
			stat.executeUpdate("drop table if exists ontology;");
			stat.executeUpdate("create table ontology(page_id, ontology_term, ont_id, ontology, date);");			
			System.out.println("Done.");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public Connection connect() throws SQLException {
		try { Class.forName("org.sqlite.JDBC"); }
		catch (ClassNotFoundException e) { e.printStackTrace(); };
		return DriverManager.getConnection("jdbc:sqlite:"+dbName);
	}
	
	
	
}
