package org.scripps.nlp.ncbo.uima;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.genewiki.GeneWikiPage;
import org.genewiki.GeneWikiUtils;
import org.genewiki.annotationmining.Config;

public class GeneWikiUIMA {

	/**
	 * This creates a directory of files containing just the plain text of GeneWiki articles
	 */
	public static void preparePlainTextDirectory(){
		Map<String, GeneWikiPage> pages = GeneWikiUtils.loadSerializedDir(Config.gwikidir, 1000000);
		int c = 0;
		for(GeneWikiPage page : pages.values()){
			if(page.getPageContent()!=null){
				c++;
				try {

				//	String text = page.toPlainText();
					String text = page.getPageContent();
					
					if(text!=null){
						BufferedWriter writer = new BufferedWriter(
								new FileWriter(Config.wikipagetextdir+page.getTitle()+".txt"));
						writer.write(text);
						writer.close();
					}
					System.out.println(c + " "+page.getTitle());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				System.out.println("Nothing found for: "+page.getTitle());
			}	
		}
	}

}