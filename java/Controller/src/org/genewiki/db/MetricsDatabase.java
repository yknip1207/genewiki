/**
 * 
 */
package org.genewiki.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.genewiki.util.Serialize;

/**
 * <p> Manages the GeneWiki metrics database. All methods are 
 * synchronized around an internal lock object 
 * to prevent simultaneous write access attempts 
 * (which would lead to a SQLException- SQLite 
 * has only single-threaded write access.) This allows
 * this singleton object to be shared among arbitrary 
 * numbers of threads (i.e. in a Distributor object).
 * 
 * <p> Methods are added and removed as needed to create the 
 * metrics.db file. The core methods should be kept updated
 * and the "update"-prefixed methods are generally implemented
 * as single-use bugfixes and are marked as @Deprecated to 
 * note their hackishness.
 * @author eclarke
 *
 */
public enum MetricsDatabase {

	instance;
	
	private final Object lock = new Object(); // Synchronize on this to prevent conflicting write locks
	private final String dbName = "metrics.db";
	
	private MetricsDatabase() { }
	
	/**
	 * Warning! Calling this method (re-)initializes the metrics database in
	 * the current working directory.
	 * @param args
	 * @throws SQLException
	 * @throws InterruptedException
	 * @throws FileNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws SQLException, InterruptedException, FileNotFoundException {
		MetricsDatabase db = MetricsDatabase.instance;
//		db.buildDb(db.dbName, true);
//		List<String> failed = (List<String>) Serialize.in("failed.list.string");
//		List<String> completed = (List<String>) Serialize.in("completed.list.string");
//		System.out.println(failed.toString());
//		System.out.println(completed.toString());
		db.fixMonths();
	}
	
	/**
	 * Provides a list of titles currently in database. Use to compare against list of 
	 * titles queried from Wikipedia in order to determine 
	 * @return
	 * @throws SQLException
	 */
	public List<String> titlesInDb() throws SQLException {
		List<String> titles = new ArrayList<String>();
		synchronized(lock) {
			Connection connection = this.connect();
			Statement state = connection.createStatement();
			ResultSet set = state.executeQuery("select title from page_info");
			while (set.next()) {
				titles.add(set.getString("title"));
			}
			connection.close();
		}
		return titles;
	}
	
	/**
	 * Provides a list of rows in page_view table missing revision counts. 
	 * @return
	 * @throws SQLException
	 */
	@Deprecated
	public List<String> getMissingRevCounts() throws SQLException {
		List<String> missingIdentifiers = new ArrayList<String>();
		synchronized(lock) {
			Connection connection = this.connect();
			Statement state = connection.createStatement();
			ResultSet set = state.executeQuery("select page_id from page_views3 where rev_count=-1;");
			while (set.next()) {
				missingIdentifiers.add(set.getString("page_id"));
			}
			connection.close();
		}
		return missingIdentifiers;
	}
	
	/**
	 * Translates a Wikipedia page id to its corresponding title (entry must
	 * exist in database; this is not a Wikipedia query method.)
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public String pageIdToTitle(int id) throws SQLException {
		String title = "";
		synchronized(lock) {
			Connection connection = this.connect();
			ResultSet set = connection.createStatement().executeQuery("select title from page_info where page_id="+id);
			while(set.next()) {
				title = set.getString("title");
			}
			connection.close();
		}
		return title;
	}
	
	/**
	 * Thread-safe way to add an entry to the revisions table.
	 * @param rev_id unique revision id
	 * @param date timestamp of revision
	 * @param page_id wikipedia page id
	 * @param bytes_changed difference in size between this revision and the previous
	 * @param editor who wrote this revision
	 * @param is_bot boolean determined generally by the presence of "bot" in the name
	 * @throws SQLException
	 */
	@Deprecated
	public void updateRevisionRow(
			long rev_id, Calendar date, int page_id, long bytes_changed, String editor, boolean is_bot) throws SQLException {

		synchronized (lock) {
			Connection connection = this.connect();
			PreparedStatement prep = connection.prepareStatement("update rev_id=?, date=?, page_id=?, bytes_changed=?, editor=?, is_bot=?" +
					" in revisions where rev_id=?;");
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
	
	/**
	 * Provides a list of rows in page_info missing word counts, which usually
	 * indicates that volume stats parsing failed for that row.
	 * @return
	 * @throws SQLException
	 */
	@Deprecated
	public List<String> getMissingWordCounts() throws SQLException {
		List<String> missingWords = new ArrayList<String>();
		synchronized(lock) {
			Connection connection = this.connect();
			Statement state = connection.createStatement();
			ResultSet set = state.executeQuery("select title from page_info where words=0;");
			while (set.next()) {
				missingWords.add(set.getString("title"));
			}
			connection.close();
		}
		return missingWords;
	}

	@Deprecated
	public void updateRevCount(String identifier, int rev_count) throws SQLException {
		synchronized(lock) {
			Connection connection = this.connect();
			PreparedStatement prep = connection.prepareStatement("update page_views3 set rev_count=? where identifier=?;");
			prep.setInt(1, rev_count);
			prep.setString(2, identifier);
			prep.addBatch();
			connection.setAutoCommit(false);
			prep.executeBatch();
			connection.setAutoCommit(true);
			connection.close();
		}
	}
	
	@Deprecated
	public void updatePageInfoRow(
			int id, String title, int entrez, int revs, int bytes, int words, int links_out, int links_in, int external,
			int pubmed_refs, int redirects, int sentences, int media, Calendar created, String creator) throws SQLException {

		synchronized (lock) {
			Connection connection = this.connect();
			PreparedStatement prep = connection.prepareStatement("update page_info set page_id=?, title=?, entrez=?, revs=?, bytes=?," +
					"words=?, links_out=?, links_in=?, external=?, pubmed_refs=?, redirects=?, sentences=?, media=?, created=?," +
					"creator=? where title=?;");
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
			prep.setDate(14, new Date(created.getTimeInMillis()));
			prep.setString(15, creator);
			prep.setString(16, title);
			prep.addBatch();
			connection.setAutoCommit(false);
			prep.executeBatch();
			connection.setAutoCommit(true);
			connection.close();
		}
		
	}

	@Deprecated
	public void updatePageCreators(String title, Calendar created, String creator) throws SQLException {
		synchronized(lock) {
			Connection connection = this.connect();
			PreparedStatement prep = connection.prepareStatement("update page_info set created=?, creator=? where title=?;");
			prep.setDate(1, new Date(created.getTimeInMillis()));
			prep.setString(2, creator);
			prep.setString(3, title);
			prep.addBatch();
			connection.setAutoCommit(false);
			prep.executeBatch();
			connection.setAutoCommit(true);
			connection.close();
		}
	}
	
	/**
	 * Thread-safe way to add an entry into the page_views table with the specified parameters.
	 * Additionally adds a unique identifier for easier post-mortem querying, consisting of 
	 * the concatenation of the page_id, month, and year fields.
	 * @param page_id
	 * @param month
	 * @param year
	 * @param views
	 * @param rev_count
	 * @throws SQLException
	 */
	public void insertPageViewRow(
			int page_id, int month, int year, int views, int rev_count) throws SQLException {
		synchronized (lock) {
			Connection connection = this.connect();
			PreparedStatement prep = connection.prepareStatement("insert into page_views values (?,?,?,?,?,?);");
			prep.setInt(1, page_id);
			prep.setInt(2, month);
			prep.setInt(3, year);
			prep.setInt(4, views);
			prep.setInt(5, rev_count);
			prep.setString(6, ""+page_id+month+year);
			prep.addBatch();
			connection.setAutoCommit(false);
			prep.executeBatch();
			connection.setAutoCommit(true);
			connection.close();
		}
	}
	
	/**
	 * Thread-safe way to add an entry into the page_info table with the specified parameters.
	 * 
	 * @param page_id the wikipedia page id
	 * @param title the wikipedia title
	 * @param entrez the entrez id corresponding to the gene
	 * @param revs the number of revisions up to current date
	 * @param bytes the size of the article
	 * @param words the wordcount
	 * @param links_out the outgoing links (links to other pages)
	 * @param links_in the incoming links (pages linking to this)
	 * @param external links to non-wikipedia pages
	 * @param pubmed_refs the pubmed references
	 * @param redirects the number of pages that serve as redirects to this page
	 * @param sentences the sentence count
	 * @param media the number of media files used on this page
	 * @param created timestamp of the first revision
	 * @param creator editor who first created the page
	 * @throws SQLException
	 */
	public void insertPageInfoRow(
			int page_id, String title, int entrez, int revs, int bytes, int words, int links_out, int links_in, int external,
			int pubmed_refs, int redirects, int sentences, int media, Calendar created, String creator) throws SQLException {

		synchronized (lock) {
			Connection connection = this.connect();
			PreparedStatement prep = connection.prepareStatement("insert into page_info values (" +
					"?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
			prep.setInt(1, page_id);
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
			prep.setDate(14, new Date(created.getTimeInMillis()));
			
			prep.setString(15, creator);
			prep.addBatch();
			connection.setAutoCommit(false);
			prep.executeBatch();
			connection.setAutoCommit(true);
			connection.close();
		}
		
	}
	
	/**
	 * Thread-safe way to add an entry to the monthly table.
	 * @param page_id
	 * @param title
	 * @param entrez
	 * @param revs
	 * @param bytes
	 * @param words
	 * @param links_out
	 * @param links_in
	 * @param external
	 * @param pubmed_refs
	 * @param redirects
	 * @param sentences
	 * @param media
	 * @param created
	 * @param creator
	 * @param date
	 * @throws SQLException
	 */
	public void insertMonthlyRow(int page_id, String title, int entrez, int revs, int bytes, int words, int links_out, int links_in, int external,
			int pubmed_refs, int redirects, int sentences, int media, Calendar created, String creator, Calendar date) throws SQLException {
		
		synchronized(lock) {
			Connection connection = this.connect();
			PreparedStatement prep = connection.prepareStatement("insert into monthly values (" +
					"?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
			prep.setInt(1, page_id);
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
			prep.setLong(14, created.getTimeInMillis());
			prep.setString(15, creator);
			prep.setLong(16, date.getTimeInMillis());
			prep.addBatch();
			connection.setAutoCommit(false);
			prep.executeBatch();
			connection.setAutoCommit(true);
			connection.close();
		}
	}
	
	public void fixMonths(long start, long end) throws SQLException {
		synchronized(lock) {
			Connection conn = this.connect();
			PreparedStatement prep = conn.prepareStatement("update monthly3 set date=? where date between ? and ?;");
			
			prep.setLong(1, start);
			prep.setLong(2, start);
			prep.setLong(3, end);
			prep.addBatch();
			conn.setAutoCommit(false);
			prep.executeBatch();
			conn.setAutoCommit(true);
			conn.close();
		}
	}
	
	/**
	 * Thread-safe way to add an entry to the revisions table.
	 * @param rev_id unique revision id
	 * @param date timestamp of revision
	 * @param page_id wikipedia page id
	 * @param bytes_changed difference in size between this revision and the previous
	 * @param editor who wrote this revision
	 * @param is_bot boolean determined generally by the presence of "bot" in the name
	 * @throws SQLException
	 */
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
	
	/**
	 * Utility method to get the number of revisions between two dates (NOT the list of revisions).
	 * This method is not a Wikipedia query; the revisions must exist in the database for the
	 * specified page_id.
	 * @param page_id
	 * @param start
	 * @param end
	 * @return
	 * @throws SQLException
	 */
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
	
	@Deprecated
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
	
	public void updatePageViewRow(int page_id, int month, int year, int views,
			int rev_count) throws SQLException {

		synchronized (lock) {
			Connection connection = this.connect();
			PreparedStatement prep = connection.prepareStatement("" +
					"update page_views set views=?, rev_count=? where page_id=? and month=? and year=?;");
			prep.setInt(1, views);
			prep.setInt(2, rev_count);
			prep.setInt(3, page_id);
			prep.setInt(4, month);
			prep.setInt(5, year);
			prep.addBatch();
			connection.setAutoCommit(false);
			prep.executeBatch();
			connection.setAutoCommit(true);
			connection.close();
		}
		
	}
	
	
	/**
	 * Creates database with the required tables.
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
//			stat.executeUpdate("drop table if exists page_views;");
//			stat.executeUpdate("create table page_views(page_id, month, year, views, rev_count, identifier);");
//			
//			stat.executeUpdate("drop table if exists page_info;");
//			stat.executeUpdate("create table page_info(" +
//					"page_id, title, entrez, revs, bytes, words, links_out, links_in, external, " +
//					"pubmed_refs, redirects, sentences, media, created, creator);");

			stat.executeUpdate("drop table if exists revisions;");
			stat.executeUpdate("create table revisions(rev_id, time, page_id, bytes_changed, editor, is_bot);");
			
			stat.executeUpdate("drop table if exists monthly;");
			stat.executeUpdate("create table monthly(" +
					"page_id, title, entrez, revs, bytes, words, links_out, links_in, external, " +
					"pubmed_refs, redirects, sentences, media, created, creator, date, unique(title, date));");
			stat.close();
			con.close();
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

	public LinkedHashMap<String, String> makeEntrezTitleMap() {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		try {
			Connection con = connect();
			Statement state = con.createStatement();
			ResultSet results = state.executeQuery("select entrez, title from page_info;");
			while (results.next()) {
				map.put(results.getString("entrez"), results.getString("title"));
			}
		} catch (SQLException e) {
			// boo hoo
		}
		return map;
	}

	/**
	 * Subtracts a month from every date in the Revisions table
	 * @throws SQLException
	 */
	public void fixMonths() throws SQLException {
		synchronized (lock) {
			Connection con = connect();
			Statement stat = con.createStatement();
			ResultSet res = stat.executeQuery("select time,rev_id from revisions;");
			Map<String, String> times = new HashMap<String, String>();
			while (res.next()) {
				times.put(res.getString("rev_id"), res.getString("time"));
			}
			stat.close();
			int count = 0;
			int size = times.size();
			for (String rev_id : times.keySet()) {
				Calendar newTime = new GregorianCalendar();
				newTime.setTimeInMillis(Long.parseLong(times.get(rev_id)));
				newTime.add(Calendar.MONTH, -1);
				newTime.getTimeInMillis();
				PreparedStatement prep = con.prepareStatement("update revisions set time=? where time=?;");
				prep.setLong(1, newTime.getTimeInMillis());
				prep.setLong(2, Long.parseLong(times.get(rev_id)));
				prep.addBatch();
				//con.setAutoCommit(false);
				prep.executeBatch();
				//con.setAutoCommit(true);
				count++;
				prep.close();
				System.out.println(count);
			}
			con.close();
			
		}
	}

	public void fixPageIds(String title, int page_id, int entrez) throws SQLException {
		synchronized (lock) {
			Connection con = connect();
			PreparedStatement prep = con.prepareStatement("update monthly set page_id=?, entrez=? where title =?");
			prep.setInt(1, page_id);
			prep.setInt(2, entrez);
			prep.setString(3, title);
			prep.addBatch();
			con.setAutoCommit(false);
			prep.executeBatch();
			con.setAutoCommit(true);
			con.close();
		}
		
	}
	
	
	
	
}
