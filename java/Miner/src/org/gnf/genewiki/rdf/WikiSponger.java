package org.gnf.genewiki.rdf;


import org.gnf.wikiapi.Page;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gnf.genewiki.GeneWikiPage;
import org.gnf.genewiki.GeneWikiLink;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ReifiedStatement;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDFS;


public class WikiSponger {

	public static void main(String args[]){
		
	}
	
	
	public static void grabRuttenBergDBPediaRDF() throws UnsupportedEncodingException{
		//test genes from Ruttenberg paper 
		String [] genes = {
				"Dopamine_receptor_D2",
				"Dopamine_receptor_D1",
				"ADRB2",
				"GRIN1",
				"GRIN2A",
				"GRIN2B",
				"GRIK1"};
		Set<String> titles = new HashSet<String>();
		List<GeneWikiPage> pages = new ArrayList<GeneWikiPage>();
		for(String gene : genes){
			GeneWikiPage prot = new GeneWikiPage(gene);
			prot.defaultPopulate();
			pages.add(prot);//tracks the outgoing links for each starting point
			prot.setTitle(prot.getTitle().replace(' ', '_'));
			titles.add(prot.getTitle());
			
			/* - this would get ~all the links including the infoboxes and templates
			for(int i=0;i<prot.sizeOfLinksList();i++){
				titles.add(prot.getLink(i).getTitle().replace(' ', '_'));
			}
			*/
			//this just gets the ones from the text
			for(GeneWikiLink glink : prot.getGlinks()){
				titles.add(glink.getTarget_page());
			}
		}
	//save the page list
		try {
			FileWriter w = new FileWriter("test\\AlzGeneCloudPages.txt");
			for(String title : titles){
				w.write(title+"\n");
			}
			w.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Collected "+titles.size()+" page titles from WP, now getting RDF");
	//go get the RDF for each page	
		Model model = ModelFactory.createDefaultModel();
		
		for(String title : titles){
			//title = "Dopamine_receptor_D2";
			String dbpediaget = "http://dbpedia.org/sparql?query=%20DESCRIBE%20%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2F" +
				title +
				"%3E&output=application%2Frdf%2Bxml";
			model.read(dbpediaget);
		
		}
		System.out.println("finished reading from dbpedia");
	//add the rdf for the html links	
	//	Property link = model.getProperty("http://gnf.org#hyperlink");
	
		for(GeneWikiPage page : pages){
			Resource subject = model.getResource("http://dbpedia.org/resource/"+page.getTitle());
			subject.addLiteral(RDFS.label, page.getTitle().replace('_', ' '));
	//		for(int i=0; i<page.sizeOfLinksList();i++){
			for(GeneWikiLink glink : page.getGlinks()){
				Resource object = model.getResource("http://dbpedia.org/resource/"+glink.getTarget_page());
				object.addLiteral(RDFS.label, glink.getTarget_page());
				String safe = URLEncoder.encode(glink.getSectionHeader(),"UTF-8");
				Property link = model.createProperty("http://gnf.org#"+safe);
				link.addLiteral(RDFS.label, glink.getSectionHeader());
				Statement addlink = model.createStatement(subject, link, object);
		//		ReifiedStatement reified = addlink.createReifiedStatement();
		//		reified.addLiteral(RDFS.comment, glink.getSnippet());
				model.add(addlink);
			}
		}
		
	//write it all out
		try{
		      FileOutputStream fout=new FileOutputStream("test\\AlzGeneCloudNoTransclude.xml");
		      model.write(fout);
	      }catch(IOException e){
	    	  System.out.println("Exception caught"+e.getMessage());
	      }
	}
	
	
}
