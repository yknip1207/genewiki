package org.gnf.pbb.images;

/**
 * A utility class for binding pymol to java applications.
 * By Kristian Rother and Christoph Gille
 * Institute of Biochemistry
 * Humboldt University of Berlin
 * http://www.mail-archive.com/pymol-users@lists.sourceforge.net/msg00888.html
 */

import java.util.zip.*;
import java.io.*;
import java.util.*;

/**
		Name:        Pymol.java
  Purpose:     A class that allows to start PyMOL from Java programs and to 
               ship commands to PyMOL from an application.
  Author:      Kristian Rother and Christoph Gille
               (kristian.rot...@charite.de, christoph.gi...@charite.de)

  Created:     2003/02/17
  Copyright:   (c) 2003
  Licence:     Python License

		This class was originally written to provide PyMOL support for Strap.
		Strap is an application for editing structural alignments of proteins.

*/

public class Pymol {

				public static String examples[]=	new String[] {
								"load pdb1cse.ent",
								"color blue,pdb1cse",
								"show sticks,pdb1cse",
								"hide spheres",
								"Color Molecule Atoms Specified Specification 0,255,255"
				};
				// Name of the PyMOL executable with command-line options
				public static String customize[]= new String[] {
								"/opt/local/bin/pymol -c"
				};

				private static OutputStream stdin=null;
				private static Runtime rt=Runtime.getRuntime();								
				private static Process process=null;

				public Pymol() {}
				
				// ----------------------------------------
				public static boolean  launch() {
								String pymol=customize[0];
								try {
												process=rt.exec(pymol);
												stdin=process.getOutputStream();
								} catch(Exception e){
												System.err.println("Pymol.java:  Error launching Pymol");
												return false;
								}
								try{Thread.currentThread().sleep(5000);}catch (Exception e){};
								return true;
				}
				//----------------------------------------
				/**
							When loadProtein is called the first time
							process will be null;
							When process already terminated 	process.exitValue() succeeds;
							In both cases launch Pymol again.
				*/
				public boolean loadProtein(File pdb) {
								// process terminated
								try {
												process.exitValue();
												process=null;
								}
								catch(Exception e){};
								
								if (process==null) launch();
								if (process==null) {
												System.err.println("Could not launch PyMOL.\nIs  the path of the executable set right ?\nPlease type the complete path of the binary into the file "+customize[0]);
												return false;
								}
								interpret("load "+pdb+",prot");
								return true;
				}
				// -----------------------------+-----------
				public String interpret(String s) {
								System.err.println(getClass().getName()+": "+s);
								
								try {
												stdin.write(s.getBytes());
												stdin.write('\n');
												stdin.flush();
								}catch(Exception e){
												System.err.println("Error in "+getClass().getName()+".interpret("+s+") \n  "+e);
								}
								return "";								
				}				
				// ----------------------------------------
				public void dispose() {interpret("delete all");}				
				public String[] getExamples() {return examples;}
				// ----------------------------------------
								
				/** 
								Example application of Pymol.java 
				*/
				public static void main(String argv[]) {
								int n=argv.length;
								if (n<1) {
												System.err.println("Demo: loads  some proteins into pymol and renders the 2nd and 3rd residue with spheres");
												System.err.println("Usage:  java Pymol protein.pdb [protein2.pdb ...] ");
												return;
								}
								Pymol pym=null;
								for(int i=0;i<n;i++) {
												pym=new Pymol();

												boolean ret=pym.loadProtein(new File(argv[i]));
												pym.interpret("select sel2_3,(resi 2,3)");
												pym.interpret("show spheres,sel2_3");
												pym.interpret("color yellow,sel2_3");
								}
				}
}