package org.gnf.genewiki.parse;


import org.gnf.wikiapi.AbstractXMLParser;
import org.gnf.wikiapi.Page;

import java.util.ArrayList;
import java.util.List;

import org.gnf.genewiki.GeneWikiPage;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Reads <code>Page</code> data from an XML file generated by the <a href="http://meta.wikimedia.org/w/api.php">Wikimedia API</a>
 * READS data from a call for external links
 * Code based on <code>XMLPagesParser</code>
 */
public class XMLExtLinksParser extends AbstractXMLParser {
	private static final String EL_TAG = "el";
	
	private static final String EXTLINKS = "extlinks";
	
	private static final String PAGE_ID = "pageid";

	private static final String NS_ID = "ns";

	private static final String TITLE_ID = "title";

    private static final String EDIT_TOKEN_ID = "edittoken";

	private GeneWikiPage fPage;

	private List<GeneWikiPage> pagesList;
	
	private List<String> extlinks;
	
	private String eloffset;

	public XMLExtLinksParser(String xmlText) throws SAXException {
		super(xmlText);
		pagesList = new ArrayList<GeneWikiPage>();
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
		fAttributes = atts;

		if ( (EL_TAG.equals(qName)) ) {
			System.out.println("");
		}else if(EXTLINKS.equals(qName)){
			System.out.println("");
		/*	if(fAttributes.getValue("eloffset")!=null){
				this.setEloffset(fAttributes.getValue("eloffset"));
			}
		*/
		}else if(qName.equals("page")){
			System.out.println("");
			fPage = new GeneWikiPage();
			fPage.getWikidetails().setPageid(fAttributes.getValue(PAGE_ID));
			fPage.getWikidetails().setNs(fAttributes.getValue(NS_ID));
			fPage.setTitle(fAttributes.getValue(TITLE_ID));
            fPage.getWikidetails().setEditToken(fAttributes.getValue(EDIT_TOKEN_ID));
            extlinks = new ArrayList<String>();
		}
		/*
  <query-continue>
    <extlinks eloffset="10" />
  </query-continue>

		<query-continue>
<categorymembers cmcontinue="24-dehydrocholesterol reductase|" />
</query-continue>
		 */
		fData = null;
	}



	@Override
	public void endElement(String uri, String name, String qName) {
		try {
			if (EL_TAG.equals(qName)) {
				if (fPage != null) {
					pagesList.add(fPage);
				}
				// System.out.println(getString());
			}

			fData = null;
			fAttributes = null;

		} catch (RuntimeException re) {
			re.printStackTrace();
		}
	}

	public List<GeneWikiPage> getPagesList() {
		return pagesList;
	}

	public String getEloffset() {
		return eloffset;
	}

	public void setEloffset(String eloffset) {
		this.eloffset = eloffset;
	}

}
