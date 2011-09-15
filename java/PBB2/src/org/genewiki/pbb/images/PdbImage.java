package org.genewiki.pbb.images;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.security.auth.login.LoginException;

import org.genewiki.api.Wiki;
import org.genewiki.pbb.Configs;
import org.genewiki.pbb.exceptions.ConfigException;
import org.genewiki.pbb.util.FileHandler;
import org.joda.time.DateTime;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;

/**
 * Holds methods for downloading a PDB file from RCSB Protein Data Bank,
 * rendering a ray-traced image of that file using the host's copy of
 * PyMOL, and uploading it to Wikimedia Commons.
 * Large parts of this module derived from PDBBot, a much more elegant
 * Python script by emw. Find it on <a href="http://code.google.com/p/pdbbot/">Google Code</a> and
 * <a href="http://commons.wikimedia.org/wiki/User:PDBbot">Wikimedia Commons</a>.
 * @author Erik Clarke
 * @author emw
 *
 */
public class PdbImage {
	private String pymol;
	private String username;
	private String password;
	private String commonsRoot;
	private FileHandler filer;
	private String pdbId;
	private String entrez;
	private String pdbFile;
	private String pdbImage;
	private String description;
	private String caption;
	
	/**
	 * Downloads and renders an image from a PDB file, and stores
	 * the title and caption as fields. Upload the file to WM Commons
	 * by calling the uploadPdbImg() method, then use the pdbImage and
	 * caption fields to reference it on Wikipedia.
	 * After the render has successfully completed, a .png file with
	 * the filename <code>Protein_[symbol]_PDB_[pdbId].png</code> is created with
	 * default dimensions 1200,1000 and a transparent background.
	 * Specific changes to the rendering process can be made by changing
	 * the pym script written out in renderPdbFile().
	 * @param pdbId
	 * @param symbol
	 * @throws IOException
	 */
	public PdbImage(String pdbId, String symbol) throws IOException {
		pymol = Configs.INSTANCE.str("pymol");
		username = Configs.INSTANCE.str("commonsUsername");
		password = Configs.INSTANCE.str("commonsPassword");
		commonsRoot = Configs.INSTANCE.str("commonsRoot");
		filer = new FileHandler("pdb");
		this.pdbId = pdbId;
		this.entrez = symbol;
		this.pdbFile = downloadPdbFile(this.pdbId);
		this.pdbImage = renderPdbFile(this.pdbFile);
		DateTime dt = new DateTime();
		String date = dt.getYear()+"-"+dt.getMonthOfYear()+"-"+dt.getDayOfMonth();
		this.description = 
				"== {{int:filedesc}} == " +
				"{{Information " +
				"| Description={{en | 1=Structure of protein "+symbol+"." +
						"Based on [[w:PyMOL | PyMOL]] rendering of PDB {{PDB2|"+pdbId+"}}.}} " +
				"| Source = {{own}} " +
				"| Author = [[User:"+username+" | "+username+"]] " +
				"| Date = "+ date + "" +
				"| Permission = " +
				"| other_versions = " +
				"}}" +
				"{{PD-self}}" +
				"{{Category:Protein_structures}}";
		this.caption = "Rendering based on [[Protein_Data_Bank | PDB]] {{PDB2|"+pdbId+"}}.";
	}
	
	/**
	 * Returns image title if set
	 * @throws NullPointerException if title is null
	 */
	public String getImage() {
		return Preconditions.checkNotNull(this.pdbImage);
	}
	
	/**
	 * Returns image caption if set
	 * @throws NullPointerException if caption is null
	 * @return
	 */
	public String getCaption() {
		return Preconditions.checkNotNull(this.caption);
	}
	
	/**
	 * Downloads pdb file from RCSB.
	 * @param pdbId
	 * @return
	 */
	private String downloadPdbFile(String pdbId) {
//		System.out.print("Downloading pdb file from RCSB... ");
		filer.wget("http://www.rcsb.org/pdb/files/"+pdbId+".pdb", pdbId+".pdb");
//		System.out.println("download complete.");
		return pdbId+".pdb";
	}
	

	/**
	 * Renders the PDB file passed to it using PyMOL and the commands specified
	 * in the file commands.pml. Requires a working pymol binary on the host
	 * with the appropriate path to the binary set in bot.properties.
	 * @param pdbFile
	 * @return
	 * @throws IOException
	 */
	private String renderPdbFile(String pdbFile) throws IOException {
		String filename = "Protein_"+entrez+"_PDB_"+pdbId+".png";
		

//		System.out.print("Rendering image for "+this.pdbId+"... ");
		
		// The following assumes you've left 'pdb' set as the FileHandler root.
		// If not, change each instance in the two strings below.
		// This is the command to export as a 1200,1000 px png file
		String rendercmd = "cmd.png('pdb/"+filename+"',1200,1000)";
		// we need to append the rendercmd string at the end for proper escaping... not sure why
		String pymol = this.pymol+" -c pdb/"+this.pdbId+".pdb pdb/commands.pml -d "+rendercmd;
		Runtime rt = Runtime.getRuntime();
		Process process = rt.exec(pymol);
		
		// Comment this out if you don't want to see pymol's chatter
//		InputStream stdout = process.getInputStream();
//		ByteStreams.copy(stdout, System.out);
		
		// Wait for PyMOL to finish before continuing
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			// not clear what would interrupt it but safe to assume
			// it wouldn't finish the render, thus, no filename to return.
			return null;	// Null safe checks done downstream (in Builder.addImage())
		}
		
		
//		System.out.println("rendering complete.");
		return filename;
		
	}
	

	/**
	 * Uploads the generated PDB image to Wikimedia Commons. You need to call
	 * this method after initializing your PdbImage object, and requires valid
	 * usernames and passwords set in the bot.properties file, and valid
	 * Wikimedia Commons base URL.
	 */
	public void uploadPdbImg() {
		Wiki wmc = new Wiki(commonsRoot);
		try {
		wmc.login(username, password.toCharArray());
		File file = new File(filer.getRoot(), this.pdbImage);
		wmc.upload(file, this.pdbImage, this.description, "New rendering of molecular structure for Gene Wiki template.");
		} catch (LoginException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
			Configs.INSTANCE.setFromFile("bot.properties");
			PdbImage img = new PdbImage("1B09", "1401");
//			System.out.println(img.description);
			//img.uploadPdbImg();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}


