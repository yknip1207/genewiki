package org.gnf.pbb.images;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import net.sourceforge.jwbf.core.actions.util.ActionException;
import net.sourceforge.jwbf.core.actions.util.ProcessException;
import net.sourceforge.jwbf.mediawiki.actions.editing.FileUpload;
import net.sourceforge.jwbf.mediawiki.actions.util.VersionException;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;
import net.sourceforge.jwbf.mediawiki.contentRep.SimpleFile;

import org.gnf.pbb.Configs;
import org.gnf.pbb.exceptions.ConfigException;
import org.gnf.pbb.util.FileHandler;
import org.joda.time.DateTime;

import com.google.common.base.Preconditions;

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
		pymol = Configs.GET.str("pymol");
		username = Configs.GET.str("commonsUsername");
		password = Configs.GET.str("commonsPassword");
		commonsRoot = Configs.GET.str("commonsRoot");
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
		System.out.print("Downloading pdb file from RCSB... ");
		filer.wget("http://www.rcsb.org/pdb/files/"+pdbId+".pdb", pdbId+".pdb");
		System.out.println("download complete.");
		return pdbId+".pdb";
	}
	

	/**
	 * Renders the PDB file passed to it using PyMOL and the commands specified
	 * in the commands[] array. Requires a working pymol binary on the host
	 * with the appropriate path to the binary set in bot.properties.
	 * @param pdbFile
	 * @return
	 * @throws IOException
	 */
	private String renderPdbFile(String pdbFile) throws IOException {
		String filename = "Protein_"+entrez+"_PDB_"+pdbId+".png";
		
		/*
		 * These strings form the prep commands to orient and color
		 * the PDB molecule appropriately.
		 */
		String[] commands = new String[] {
				"hide everything, all",
				"show cartoon, all",
				"util.chainbow !resn da+dt+dg+dc+du+hetatm",
				"set opaque_background=0",
				"set show_alpha_checker=1",
				"set cartoon_transparency=0",
				"set depth_cue=0",
				"set ray_trace_fog=0",
				"orient",
				"ray 1200, 1000",
				"png pdb/"+filename, 	// If changed, make sure this matches up with the FileHandler root
				"quit"
		};
		
		
		System.out.print("Rendering image for "+this.pdbId+"... ");
		
		// -p: accept input from stdin
		// -i: no openGL interface
		// -x: no external interface
		// -c should have worked but program aborts immediately...? YMMV.
		String pymol = this.pymol+" -p -i -x pdb/"+this.pdbId+".pdb";
		Runtime rt = Runtime.getRuntime();
		Process process = rt.exec(pymol);
		
		OutputStream stdin = process.getOutputStream();
		InputStream stout = process.getInputStream();
		
		// Sends the process the commands specified in commands[] iteratively.
		for (String command : commands) {
			System.out.println("Pymol: "+command);
			stdin.write(command.getBytes());
			stdin.write('\n');
			stdin.flush();
			
		}
		
		// Wait for PyMOL to finish before continuing
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			// not clear what would interrupt it but safe to assume
			// it wouldn't finish the render, thus, no filename to return.
			return null;
		}
		
		
		System.out.println("rendering complete.");
		return filename;
		
	}
	

	/**
	 * Uploads the generated PDB image to Wikimedia Commons. You need to call
	 * this method after initializing your PdbImage object, and requires valid
	 * usernames and passwords set in the bot.properties file, and valid
	 * Wikimedia Commons base URL.
	 * @throws ConfigException if URL or credentials are invalid
	 */
	public void uploadPdbImg() {
		try {
			MediaWikiBot commonsBot = new MediaWikiBot(new URL(commonsRoot));
			commonsBot.login(this.username, this.password);
			
			SimpleFile file = new SimpleFile(new File(filer.getRoot(), this.pdbImage));
			file.addText(this.description); // Forms the 
			FileUpload fu = new FileUpload(file, commonsBot);
			
			System.out.print("Uploading file... ");
			commonsBot.performAction(fu);
			System.out.println(this.pdbImage+" uploaded to Wikimedia Commons.");
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new ConfigException("Bad URL set for Wikimedia Commons in the config file.");			
		} catch (ActionException e) {
			e.printStackTrace();
			throw new ConfigException("Bad credentials for Wikimedia Commons.");
		} catch (VersionException e) {
			// This shouldn't show up if you're working with Wikipedia and WM Commons
			e.printStackTrace();
		} catch (ProcessException e) {

			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
			Configs.GET.setFromFile("bot.properties");
			PdbImage img = new PdbImage("1B09", "1401");
			System.out.println(img.description);
			img.uploadPdbImg();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}


