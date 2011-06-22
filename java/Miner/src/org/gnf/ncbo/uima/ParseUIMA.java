package org.gnf.ncbo.uima;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gnf.go.GOterm;

public class ParseUIMA {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String test = "/Users/bgood/Desktop/ncbo-go-anno-output.txt";
		parseNCBOOut(test);
		
	}
	
	/**
	 =================================================================================
======================= Annotation: 1 =======================
Annotator: 1|OBA|NCBO|CCP
--- AnnotationSets: 1|set|test set
--- Span: 11942 - 11951  
--- DocCollection: -1  DocID: 5-HT1A receptor.txt  DocumentSection: 3
--- Covered Text: signaling
-CLASS MENTION: GO:0023052 "signaling"	[11942..11951]
-    SLOT MENTION: Concept match type with SLOT VALUE(s): DIRECT  
-    SLOT MENTION: concept ID with SLOT VALUE(s): GO:0023052  
-    SLOT MENTION: ontology ID with SLOT VALUE(s): 42989  
	 * @param file
	 */
	
	public static List<UimaAnnotation> parseNCBOOut(String file){
		List<UimaAnnotation> annos = new ArrayList<UimaAnnotation>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(file));
			String line = f.readLine();
			while(line!=null){
				if(line.startsWith("Annotator: 1|OBA|NCBO|CCP")){
					UimaAnnotation a = new UimaAnnotation();
					a.setAnnotatorName("NCBO");
					f.readLine(); //skip one
					String spanline = f.readLine();
					String[] spans = spanline.split(" ");
					a.setStartIndex(Integer.parseInt(spans[2]));
					a.setStopIndex(Integer.parseInt(spans[4]));
					String idline = f.readLine();
					int idstart = idline.indexOf("DocID:")+7;
					int idstop = idline.indexOf(".txt");
					a.setDocId(idline.substring(idstart,idstop));
					String coveredLine = f.readLine();
					a.setCoveredText(coveredLine.substring(18));
					f.readLine();//skip class mention summary
					String matchtypeline = f.readLine();
					a.setMatchType(matchtypeline.substring(58));
					String conceptline = f.readLine();
					a.setOntologyTermId(conceptline.substring(50).trim());
					String ontline = f.readLine();
					a.setNcboOntologyId(ontline.substring(51));
			//		System.out.println(a);
					annos.add(a);
				}
				line = f.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return annos;
	}


}
