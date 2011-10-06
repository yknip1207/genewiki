/**
 * 
 */
package org.genewiki.annotationmining.annotations;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.scripps.datasources.PharmGKB.Relation;

/**
 * 
 * Capture, access mined annotations in a database
 * @author bgood
 *
 */
public class AnnotationDb {

	String db_loc = "/Users/bgood/data/genewiki/mined.db";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
/**
 create table cannos (
 gene_id varchar(10), 
 title varchar(50), 
 heading varchar(50), 
 author varchar(50), 
 wikitrust_page float, 
 wikitrust_sentence float, 
 annotation_score float, 
 target_acc varchar(15), 
 target_uri varchar(100), 
 target_preferred_term varchar(100), 
 target_stypes varchar(200), 
 target_vocabulary varchar(200), 
 target_vocabulary_id varchar(15), 
 matched_text varchar(50), 
 sentence varchar(200), 
 refs varchar(200));

 * @param canno
 * @throws ClassNotFoundException
 */
	
	public void insertAnntotation(CandidateAnnotation canno) throws ClassNotFoundException{

		// load the sqlite-JDBC driver using the current class loader
		Class.forName("org.sqlite.JDBC");

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+db_loc);
			PreparedStatement statement = connection.prepareStatement("insert into cannos values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");			
					statement.setString(1, canno.getEntrez_gene_id()); statement.setString(2, canno.getSource_wiki_page_title()); statement.setString(3, canno.getSection_heading()); 
					statement.setString(4, canno.getLink_author()); statement.setString(5, canno.getWikitrust_page()+""); statement.setString(6, canno.getWikitrust_sentence()+""); 
					statement.setString(7, canno.getAnnotationScore()+""); statement.setString(8, canno.getTarget_accession());
					statement.setString(9, canno.getTarget_uri()); statement.setString(10, canno.getTarget_preferred_term());
					statement.setString(11, canno.getTarget_semantic_types()); statement.setString(12, canno.getTarget_vocabulary());
					statement.setString(13, canno.getTarget_vocabulary_id()); statement.setString(14, canno.getMatched_text());
					statement.setString(15, canno.getParagraph_around_link()); statement.setString(16, canno.getCsvrefs());
					statement.executeUpdate();
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
}
