/**
 * 
 */
package org.genewiki;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.security.auth.login.FailedLoginException;

import org.genewiki.api.Wiki;
import org.genewiki.pbb.Configs;
import org.genewiki.pbb.db.DatabaseManager;

import com.google.common.io.Files;
import com.google.common.io.Resources;

/**
 * @author eclarke
 *
 */
public class Install {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FailedLoginException 
	 */
	public static void main(String[] args) throws FailedLoginException, IOException {
		
		/* ------ Default properties file wizard ------ */
		String username, password, cwUsername, cwPassword, pymolBin, pywikipedia;
		Properties props = new Properties();
		username = readline("Enter the username this bot will operate under:");
		password = readline("Enter the password for "+username+" (warning! password stored as plaintext!): ");
		if (readline("Do you have PyMOL installed and want the bot to generate and upload images? (y/N)").equalsIgnoreCase("y")) {
			cwUsername = readline("Enter the Wikimedia Commons username this bot will operate under:");
			cwPassword = readline("Enter the password for the Commons user "+cwUsername+":");
			pymolBin = readline("Enter the absolute path to the PyMOL binary/executable: ");
			pywikipedia = readline("Enter the absolute path to your configured pywikipedia files: ");
		} else {
			cwUsername = "";
			cwPassword = "";
			pymolBin = "";
			pywikipedia = "";
		}
		StringBuffer sb = new StringBuffer();
		sb.append("name = ProteinBoxBot \n");
		sb.append("\n");
		sb.append("# Wikipedia credentials\n");
		sb.append("username = "+username+"\n");
		sb.append("password = "+password+"\n");
		sb.append("# Wikimedia Commons credentials (to upload rendered images)\n");
		sb.append("commonsUsername = "+cwUsername+"\n");
		sb.append("commonsPassword = "+cwPassword+"\n");
		sb.append("pywikipedia = "+pywikipedia+"\n");
		sb.append("# Template information\n"+
				"templatePrefix = Template:PBB/\n"+
				"templateName = GNF_Protein_box\n"+
				"api_root = http://en.wikipedia.org/w/\n"+
				"commonsRoot = http://commons.wikimedia.org/w/\n"+
				"\n"+
				"# Miscellaneous\n"+
				"loggerLevel = Info\n"+
				"\n"+
				"# PDB image rendering\n");
		sb.append("pymol = "+pymolBin);
		try {
			Files.write(sb, new File("bot.properties.test"), Charset.forName("UTF-8"));
			System.out.println("New bot.properties file created in working directory. Please ensure this \n" +
					"file is in the same folder as the bot jar file.");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not write file 'bot.properties'. Please copy and " +
					"paste \nthe following to a file named bot.properties:\n\n"+sb.toString());
			readline("\nPress enter to continue...");
		}
		
		/* ------- Verify Wikipedia credentials ------ */
		Wiki wikipedia = new Wiki();
		wikipedia.setMaxLag(0);
		try {
			wikipedia.login(username, password.toCharArray());
		} catch (FailedLoginException e) {
			username = readline("Previously entered Wikipedia username/password was invalid. Please re-enter username:");
			password = readline("Please enter password for user "+username+":");
			wikipedia.login(username, password.toCharArray());
		}
		
		/* ------ Set up databases ------ */
		log("Setting up ProteinBoxBot database...");
		String[] infoBoxes = wikipedia.whatTranscludesHere("Template:GNF_Protein_box", 10);
		List<String> targets = Arrays.asList(infoBoxes);
		StringBuffer listbuffer = new StringBuffer();
		for (String line : targets){
			listbuffer.append(line+"\n");
		}
		Files.write(listbuffer, new File("proteinboxes.list"), Charset.forName("UTF-8"));
		DatabaseManager.populateFromFile("proteinboxes.list");
		log("ProteinBoxBot database setup complete.");
		log("Setting up gene2pubmed database at g2p.db...");		
		// Downloading the gene2pubmed file from NCBI, unzipping it, and storing it
		Generator gen = new Generator();
		gen.initDB();
		log("Gene2pubmed database initialization complete.");
		log("Finished.");
	}

	
	private static String readline(String prompt) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print(prompt+" ");
		try {
			return br.readLine();
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
	
	private static void log(String message) {
		System.out.println(message);
	}

}
