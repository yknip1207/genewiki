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
import org.gnf.pbb.util.FileHandler;
import org.joda.time.DateTime;

public class PdbImage {
	private FileHandler filer;
	private String pdbId;
	private String entrez;
	private String pdbFile;
	private String pdbImage;
	private String description;
	private String caption;
	
	public PdbImage(String pdbId, String symbol) throws IOException {
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
				"| Source = {{own}} n" +
				"| Author = [[User:Pleiotrope | Pleiotrope]] n" +
				"| Date = "+ date + "n" +
				"| Permission = n" +
				"| other_versions = n" +
				"}}" +
				"{{PD-self}}" +
				"{{Category:Protein_structures}}";
		this.caption = "Rendering based on [[Protein_Data_Bank | PDB]] {{PDB2|"+pdbId+"}}.";
	}
	
	public String getImage() {
		return pdbImage;
	}
	
	public String getCaption() {
		return caption;
	}
	
//	public PdbImage(String pdbId, String entrez) throws IOException {
//		this.pdbId = pdbId;
//		this.entrez = entrez;
//		filer = new FileHandler("pdb");
//		pdbFile = downloadPdbFile(pdbId);
//		String pdbImgPath = renderPdbFile(pdbFile);
//		this.pdbImage = pdbImgPath;
//		this.caption = "Rendering based on {{PDB2|"+pdbId+"}}";
//	}
	
	private String downloadPdbFile(String pdbId) {
		System.out.print("Downloading pdb file from RCSB... ");
		filer.wget("http://www.rcsb.org/pdb/files/"+pdbId+".pdb", pdbId+".pdb");
		System.out.println("download complete.");
		return pdbId+".pdb";
	}
	


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
		
		String pymol = "/opt/local/bin/pymol -p -i -x pdb/"+this.pdbId+".pdb";
		Runtime rt = Runtime.getRuntime();
		Process process = rt.exec(pymol);
		
		OutputStream stdin = process.getOutputStream();
		InputStream stout = process.getInputStream();
		
		for (String command : commands) {
			// System.out.println("Pymol: "+command);
			//try {Thread.sleep(5000);} catch (Exception e) {}
			stdin.write(command.getBytes());
			stdin.write('\n');
			stdin.flush();
			
		}
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			// do nothing
		}
		
		
		System.out.println("rendering complete.");
		return filename;
		
	}
	

	public void uploadPdbImg() {
		try {
			MediaWikiBot wmBot = new MediaWikiBot(new URL("http://commons.wikimedia.org/w/"));
			// wmBot.login(Configs.GET.str("username"), Configs.GET.str("password"));
			wmBot.login("Pleiotrope", "Cynosura42");
			SimpleFile file = new SimpleFile(new File(filer.getRoot(), this.pdbImage));
			file.addText(this.description);
			FileUpload fu = new FileUpload(file, wmBot);
			System.out.print("Uploading file... ");
			wmBot.performAction(fu);
			System.out.println(this.pdbImage+" uploaded to Wikimedia Commons.");
		} catch (MalformedURLException e) {
			// It's not malformed.
		} catch (ActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (VersionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
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


