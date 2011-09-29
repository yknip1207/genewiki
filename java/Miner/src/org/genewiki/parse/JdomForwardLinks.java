package org.genewiki.parse;


import org.genewiki.GeneWikiPage;
import org.gnf.wikiapi.Connector;
import org.gnf.wikiapi.Link;
import org.gnf.wikiapi.Page;
import org.gnf.wikiapi.User;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class JdomForwardLinks {
	
	public static void main(String[] args){
		/**

<?xml version="1.0"?>
<api>
  <query>
    <backlinks>
      <bl pageid="1525" ns="0" title="Aspirin" />
      <bl pageid="2246" ns="0" title="Analgesic" />
      <bl pageid="2341" ns="0" title="Alkaloid" />
      <bl pageid="2504" ns="0" title="Amphetamine" />
      <bl pageid="3427" ns="0" title="Beavis and Butt-head" />
      <bl pageid="4183" ns="0" title="Botany" />
      <bl pageid="4531" ns="0" title="Bipolar disorder" />
      <bl pageid="5872" ns="0" title="Bradycardia" />
      <bl pageid="5906" ns="0" title="Carbon dioxide" />
      <bl pageid="5930" ns="0" title="Coffea" />
    </backlinks>
  </query>
  <query-continue>
    <backlinks blcontinue="0|Caffeine|6690" />
  </query-continue>
</api>

<?xml version="1.0"?><api><query><pages><page pageid="14087305" ns="0" title="ABCA1">
<links>
<pl ns="0" title="2002" />
<pl ns="0" title="2003" />
<pl ns="0" title="ABCA12" />
</links></page></pages></query></api>\n


		 */
		
		
		

	}

	
	public List<String> parseLinks(String xml){
		List<String> links = new ArrayList<String>();
		SAXBuilder builder = new SAXBuilder();
		try {
			Document doc = builder.build(new ByteArrayInputStream(xml.getBytes("UTF-8")));
			Element root = doc.getRootElement();
			Element query = root.getChild("query");
			if(query==null){
				return links;
			}
			Element flinks = query.getChild("pages").getChild("page").getChild("links");
			if(flinks==null){
				return links;
			}
			List<Element> els = flinks.getContent();
			if(els==null){
				return links;
			}
			for(Element el : els){
				links.add(el.getAttributeValue("title"));
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
				nextlink = cont.getChild("links").getAttributeValue("plcontinue");	
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
	
}

