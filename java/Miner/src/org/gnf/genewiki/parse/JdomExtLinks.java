package org.gnf.genewiki.parse;

import info.bliki.api.Connector;
import info.bliki.api.Link;
import info.bliki.api.Page;
import info.bliki.api.User;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gnf.genewiki.GeneWikiPage;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class JdomExtLinks {
	
	public static void main(String[] args){
		System.out.println("http://amigo.geneontology.org/cgi-bin/amigo/go.cgi?view=details&search_constraint=terms&depth=0&query=".length());
		/**
		 <?xml version="1.0"?>
<api>
  <query>
    <pages>
      <page pageid="14131548" ns="0" title="Dopamine receptor D1">
        <extlinks>
          <el xml:space="preserve">http://amigo.geneontology.org/cgi-bin/amigo/go.cgi?view=details&amp;search_constraint=terms&amp;depth=0&amp;query=GO:0001584</el>
          <el xml:space="preserve">http://amigo.geneontology.org/cgi-bin/amigo/go.cgi?view=details&amp;search_constraint=terms&amp;depth=0&amp;query=GO:0001590</el>
          <el xml:space="preserve">http://amigo.geneontology.org/cgi-bin/amigo/go.cgi?view=details&amp;search_constraint=terms&amp;depth=0&amp;query=GO:0004872</el>
          <el xml:space="preserve">http://amigo.geneontology.org/cgi-bin/amigo/go.cgi?view=details&amp;search_constraint=terms&amp;depth=0&amp;query=GO:0004952</el>
          <el xml:space="preserve">http://amigo.geneontology.org/cgi-bin/amigo/go.cgi?view=details&amp;search_constraint=terms&amp;depth=0&amp;query=GO:0005623</el>
          <el xml:space="preserve">http://amigo.geneontology.org/cgi-bin/amigo/go.cgi?view=details&amp;search_constraint=terms&amp;depth=0&amp;query=GO:0005624</el>
          <el xml:space="preserve">http://amigo.geneontology.org/cgi-bin/amigo/go.cgi?view=details&amp;search_constraint=terms&amp;depth=0&amp;query=GO:0005886</el>
          <el xml:space="preserve">http://amigo.geneontology.org/cgi-bin/amigo/go.cgi?view=details&amp;search_constraint=terms&amp;depth=0&amp;query=GO:0005887</el>
          <el xml:space="preserve">http://amigo.geneontology.org/cgi-bin/amigo/go.cgi?view=details&amp;search_constraint=terms&amp;depth=0&amp;query=GO:0007165</el>
          <el xml:space="preserve">http://amigo.geneontology.org/cgi-bin/amigo/go.cgi?view=details&amp;search_constraint=terms&amp;depth=0&amp;query=GO:0007187</el>
        </extlinks>
      </page>
    </pages>
  </query>
  <query-continue>
    <extlinks eloffset="10" />
  </query-continue>
</api>

		 */
		
		
		

	}

	
	public List<String> parseLinks(String xml){
		List<String> links = new ArrayList<String>();
		SAXBuilder builder = new SAXBuilder();
		try {
			Document doc = builder.build(new ByteArrayInputStream(xml.getBytes("UTF-8")));
			Element root = doc.getRootElement();
			Element extlinks = root.getChild("query").getChild("pages").getChild("page").getChild("extlinks");

			if(extlinks==null){
				return links;
			}
			List<Element> els = extlinks.getChildren("el");
			for(Element el : els){
				links.add(el.getTextTrim());
			}

		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return links;
	}
	
	public String parseNextLinkChunk(String xml){
		SAXBuilder builder = new SAXBuilder();
		String nextlink = "";
		try {
			Document doc = builder.build(new ByteArrayInputStream(xml.getBytes("UTF-8")));
			Element root = doc.getRootElement();
			Element cont = root.getChild("query-continue");
			if(cont !=null){
				nextlink = cont.getChild("extlinks").getAttributeValue("eloffset");	
			}
			
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nextlink;
	}
	
	public static String retrieveAllOutBoundHyperLinks(String title){
		User  user = new User("", "", "http://en.wikipedia.org/w/api.php");
		user.login();
		int total = 3000;
		int batch = 500;
		List<String> a = new ArrayList<String>();
		a.add(title);
		String nextLinkTitle = "";
		Connector connector = new Connector();		
//		for(int i = batch; i< total; i+=batch){
			String[] linkQuery1 = { 
					"prop", "extlinks",
					"ellimit", batch+""
			};
			String[] linkQuery2 = { 
					"prop", "extlinks",
					"ellimit", batch+"",
					"eloffset", nextLinkTitle
			};

			String pagesXML = "";
			if(nextLinkTitle==""){
				pagesXML = connector.queryXML(user, a, linkQuery1);
			}else{
	//			pagesXML = connector.queryXML(user, a, linkQuery2);
			}
		return pagesXML;
	}
}

