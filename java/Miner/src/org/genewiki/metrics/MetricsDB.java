/**
 * 
 */
package org.genewiki.metrics;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


public class MetricsDB
{
  public static void main(String[] args)
  {
    try {
    	MetricsDB db = new MetricsDB();
    	Calendar t1 = Calendar.getInstance();

		Calendar t0 = Calendar.getInstance();
		t0.clear();
    	int s = db.getSumBytesChangedInDateRange(t0, t1);
    	System.out.println(s);
	} catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
  
  public int getSumBytesChangedInDateRange(Calendar t0, Calendar t1) throws ClassNotFoundException{
	    Class.forName("org.sqlite.JDBC");
	    int total = 0;
	    Connection connection = null;
	    try
	    {
	      // create a database connection
	      connection = DriverManager.getConnection("jdbc:sqlite:/Users/bgood/data/NARupdate2011/metrics.db");
	      Statement statement = connection.createStatement();
	      statement.setQueryTimeout(30);  // set timeout to 30 sec.
	      ResultSet rs = statement.executeQuery(
	      		"select bytes_changed from revisions " +
	      		"where time >= "+t0.getTimeInMillis()+" " +
	      		"and time <="+t1.getTimeInMillis()+" ");
	     while(rs.next())
	      {
	     	total+= rs.getInt("bytes_changed");
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
	  return total;
}  
  
  public int getRevCountInDateRange(Calendar t0, Calendar t1) throws ClassNotFoundException{
		    Class.forName("org.sqlite.JDBC");
		    int rc = 0;
		    Connection connection = null;
		    try
		    {
		      // create a database connection
		      connection = DriverManager.getConnection("jdbc:sqlite:/Users/bgood/data/NARupdate2011/metrics.db");
		      Statement statement = connection.createStatement();
		      statement.setQueryTimeout(30);  // set timeout to 30 sec.
		      ResultSet rs = statement.executeQuery(
		      		"select count(*) as r from revisions " +
		      		"where time >= "+t0.getTimeInMillis()+" " +
		      		"and time <="+t1.getTimeInMillis()+" " 
		      		); //"and is_bot = 0"
		     if(rs.next())
		      {
		       Integer r = rs.getInt("r");
		       rc = r;
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
		  return rc;
	  }  

  public Map<String, Integer> getSumReferencesByMonth() throws ClassNotFoundException{
	  SimpleDateFormat format_exl = new SimpleDateFormat("MM/dd/yyyy");
	  Map<String, Integer> monthly = new TreeMap<String, Integer>();
		// load the sqlite-JDBC driver using the current class loader
		    Class.forName("org.sqlite.JDBC");
		    
		    Connection connection = null;
		    try
		    {
		      // create a database connection
		      connection = DriverManager.getConnection("jdbc:sqlite:/Users/bgood/data/NARupdate2011/metrics.db");
		      Statement statement = connection.createStatement();
		      statement.setQueryTimeout(30);  // set timeout to 30 sec.
		      ResultSet rs = statement.executeQuery(" select date, sum(pubmed_refs) as w from monthly group by date");
		     while(rs.next())
		      {
		       Integer words = rs.getInt("w");
		       Long t = rs.getLong("date");
		       Calendar c = Calendar.getInstance();
		       c.clear();
		       c.setTime(new Date(t));
		       monthly.put(format_exl.format(c.getTime()), words);
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
		  return monthly;
	  } 
  
  public Map<String, Integer> getSumWordsByMonth() throws ClassNotFoundException{
	  SimpleDateFormat format_exl = new SimpleDateFormat("MM/dd/yyyy");
	  Map<String, Integer> monthly = new TreeMap<String, Integer>();
		// load the sqlite-JDBC driver using the current class loader
		    Class.forName("org.sqlite.JDBC");
		    
		    Connection connection = null;
		    try
		    {
		      // create a database connection
		      connection = DriverManager.getConnection("jdbc:sqlite:/Users/bgood/data/NARupdate2011/metrics.db");
		      Statement statement = connection.createStatement();
		      statement.setQueryTimeout(30);  // set timeout to 30 sec.
		      ResultSet rs = statement.executeQuery(" select date, sum(words) as w from monthly group by date");
		     while(rs.next())
		      {
		       Integer words = rs.getInt("w");
		       Long t = rs.getLong("date");
		       Calendar c = Calendar.getInstance();
		       c.clear();
		       c.setTime(new Date(t));
		       monthly.put(format_exl.format(c.getTime()), words);
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
		  return monthly;
	  }
  public Map<String, Integer> getSumPageViewsByMonth() throws ClassNotFoundException{
	  SimpleDateFormat format_exl = new SimpleDateFormat("MM/dd/yyyy");
	  Map<String, Integer> monthly = new TreeMap<String, Integer>();
		// load the sqlite-JDBC driver using the current class loader
		    Class.forName("org.sqlite.JDBC");
		    
		    Connection connection = null;
		    try
		    {
		      // create a database connection
		      connection = DriverManager.getConnection("jdbc:sqlite:/Users/bgood/data/NARupdate2011/metrics.db");
		      Statement statement = connection.createStatement();
		      statement.setQueryTimeout(30);  // set timeout to 30 sec.
		      ResultSet rs = statement.executeQuery("select year, month, sum(views) as v from page_views group by year, month");
		     while(rs.next())
		      {
		       Integer views = rs.getInt("v");
		       Integer year = rs.getInt("year");
		       Integer month = rs.getInt("month");
		       Calendar c = Calendar.getInstance();
		       c.clear();
		       c.set(Calendar.YEAR, year);
		       c.set(Calendar.MONTH, month-1); //note we start at zero..
		       c.add(Calendar.MONTH, 1); //this wraps the year end correctly..
		       c.set(Calendar.DATE, 1);
		       c.add(Calendar.DAY_OF_YEAR, -1);
		       monthly.put(format_exl.format(c.getTime()), views);
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
		  return monthly;
	  }
  
  public TreeSet<String> getPageids() throws ClassNotFoundException{
	  TreeSet<String> ids = new TreeSet<String>();
		// load the sqlite-JDBC driver using the current class loader
		    Class.forName("org.sqlite.JDBC");
		    
		    Connection connection = null;
		    try
		    {
		      // create a database connection
		      connection = DriverManager.getConnection("jdbc:sqlite:/Users/bgood/data/NARupdate2011/metrics.db");
		      Statement statement = connection.createStatement();
		      statement.setQueryTimeout(30);  // set timeout to 30 sec.
		      ResultSet rs = statement.executeQuery("select distinct(page_id) from page_info");
		     while(rs.next())
		      {
		       ids.add(rs.getString("page_id"));
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
		  return ids;
	  }
  
  public static String getWordsByGeneID(String geneid) throws ClassNotFoundException{
	  String wcount = "";
	// load the sqlite-JDBC driver using the current class loader
	    Class.forName("org.sqlite.JDBC");
	    
	    Connection connection = null;
	    try
	    {
	      // create a database connection
	      connection = DriverManager.getConnection("jdbc:sqlite:/Users/bgood/data/NARupdate2011/metrics.db");
	      Statement statement = connection.createStatement();
	      statement.setQueryTimeout(30);  // set timeout to 30 sec.
	      ResultSet rs = statement.executeQuery("select words from page_info where entrez = "+geneid);
	     if(rs.next())
	      {
	        wcount = rs.getString("words");
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
	  return wcount;
  }
 
  
  public static String getTitleByGeneID(String geneid) throws ClassNotFoundException{
	  String title = "";
	// load the sqlite-JDBC driver using the current class loader
	    Class.forName("org.sqlite.JDBC");
	    
	    Connection connection = null;
	    try
	    {
	      // create a database connection
	      connection = DriverManager.getConnection("jdbc:sqlite:/Users/bgood/data/NARupdate2011/metrics.db");
	      Statement statement = connection.createStatement();
	      statement.setQueryTimeout(30);  // set timeout to 30 sec.
	      ResultSet rs = statement.executeQuery("select title from page_info where entrez = "+geneid);
	     if(rs.next())
	      {
	        title = rs.getString("title");
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
	  return title;
  }
  
  /**
   * page view range queries
   *  
			Calendar t0 = Calendar.getInstance();
			t0.set(2009, Calendar.SEPTEMBER, 1);
			t0.set(2009, Calendar.SEPTEMBER, 1, 0, 0, 0);
			
			Calendar t1 = Calendar.getInstance();
			t1.setTimeInMillis(t0.getTimeInMillis());
			t1.add(Calendar.MONTH, 6);
			
			
	      ResultSet rs = statement.executeQuery("select count(*) as c from revisions where time > "+t0.getTimeInMillis()+" and time < "+t1.getTimeInMillis()); //+" and is_bot = 0"

   * 
   */
}