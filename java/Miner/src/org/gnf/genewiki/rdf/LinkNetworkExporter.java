package org.gnf.genewiki.rdf;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.gnf.genewiki.Config;
import org.gnf.genewiki.GeneWikiLink;
import org.gnf.genewiki.Workflow;
import org.gnf.genewiki.GeneWikiPage;
import org.gnf.genewiki.GeneWikiUtils;
import org.gnf.genewiki.associations.CandidateAnnotation;
import org.gnf.go.GOmapper;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


/***
 * Class to handle conversion of extracted associations to simple rdf or tab formats
 * @author bgood
 *
 */
public class LinkNetworkExporter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String file = "/Users/bgood/data/genewiki/gwiki2goannos_(all_with_panther_ortho)";
		//String file = "/Users/bgood/data/genewiki/gogenes/MergedAnnosNRwithkids.txt";
		//	String file = "/Users/bgood/data/genewiki/NCBO-output/MinedGOANNOS.txt";
		//List<CandiAnnoFromLink> testcannos = GOmapper.loadCandidateAnnotations(file);
		String outfile = "/Users/bgood/data/genewiki/rdf/LinkMined.rdf";
		dumpAllLinksAsRDF(outfile);
	}

	public static void dumpAllLinksAsRDF(String outfile){
		Model m = ModelFactory.createDefaultModel();
		Property ass = m.createProperty("http://example.org/associated_with");
		String uriroot = "http://example.org/";
		Resource genetype = m.createResource(uriroot+"Gene");
		genetype.addProperty(RDF.type, RDFS.Class);
		
		List<GeneWikiPage> pages = GeneWikiUtils.loadSerializedDir(Config.gwikidir, 1000000);
		int c = 0;
		for(GeneWikiPage page : pages){
			if(page!=null&&page.getGlinks()!=null&&page.getGlinks().size()>0){
				String sourcetitle = page.getTitle();
				try {
					sourcetitle = URLEncoder.encode(sourcetitle, "utf-8");
					Resource subject = m.createResource(uriroot+sourcetitle);
					subject.addProperty(RDF.type, genetype);
					//add labels
					for(String r : page.getWikisynset()){
						subject.addLiteral(RDFS.label, r);
						if(r.equalsIgnoreCase("fibronectin")){
							System.out.println(subject);
						}
					}				
//					for(GeneWikiLink link : page.getGlinks()){
//						c++;
////						String objecttitle = link.getTarget_page().getWikisynset().iterator().next();
////						objecttitle = URLEncoder.encode(objecttitle, "utf-8");
////						Resource object = m.createResource(uriroot+objecttitle);
////						
////						for(String r : link.getTarget_page().getWikisynset()){
////							object.addLiteral(RDFS.label, r);
////						}
//						Statement asslink = m.createStatement(subject, ass, object);
//						m.add(asslink);
//						if(c%10000==0){
//							System.out.println(c+" "+asslink);
//						}
//					}
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				//System.out.println("Nothing found for: "+page.getData().getTitle());
			}
		}
		System.out.println("Done with "+c+" writing");
		//write it all out
		try{
		      FileOutputStream fout=new FileOutputStream(outfile);
		      m.write(fout);
	      }catch(IOException e){
	    	  System.out.println("Exception caught"+e.getMessage());
	      }
	}
}
