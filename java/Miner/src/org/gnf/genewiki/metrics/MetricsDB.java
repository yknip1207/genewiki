/**
 * 
 */
package org.gnf.genewiki.metrics;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;


public class MetricsDB
{
  public static void main(String[] args)
  {
    try {
		System.out.println(getTitleByGeneID("3630")+"\t"+getWordsByGeneID("3630"));
	} catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
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