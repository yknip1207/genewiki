package org.genewiki.parse;


import org.gnf.wikiapi.AbstractXMLParser;
import org.gnf.wikiapi.Page;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Reads <code>Page</code> data from an XML file generated by the <a href="http://meta.wikimedia.org/w/api.php">Wikimedia API</a>
 * READS data from a call for category members
 * Code based on <code>XMLPagesParser</code>
 */
public class XMLCatMembersParser extends AbstractXMLParser {
	private static final String CATEGORY_TAG1 = "cm";
	
	private static final String CATEGORY_MEMBERS = "categorymembers";
	
	private static final String PAGE_ID = "pageid";

	private static final String NS_ID = "ns";

	private static final String TITLE_ID = "title";

    private static final String EDIT_TOKEN_ID = "edittoken";

	private Page fPage;

	private List<Page> pagesList;
	
	private String cmcontinue;

	public XMLCatMembersParser(String xmlText) throws SAXException {
		super(xmlText);
		pagesList = new ArrayList<Page>();
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
		fAttributes = atts;

		if ( (CATEGORY_TAG1.equals(qName)) ) {
			fPage = new Page();
			fPage.setPageid(fAttributes.getValue(PAGE_ID));
			fPage.setNs(fAttributes.getValue(NS_ID));
			fPage.setTitle(fAttributes.getValue(TITLE_ID));
            fPage.setEditToken(fAttributes.getValue(EDIT_TOKEN_ID));
		}else if(CATEGORY_MEMBERS.equals(qName)){
			if(fAttributes.getValue("cmcontinue")!=null){
				setCmcontinue(fAttributes.getValue("cmcontinue"));
			}
		}
		/*
		<query-continue>
<categorymembers cmcontinue="24-dehydrocholesterol reductase|" />
</query-continue>
		 */
		fData = null;
	}

	public String getCmcontinue() {
		return cmcontinue;
	}

	public void setCmcontinue(String cmcontinue) {
		this.cmcontinue = cmcontinue;
	}

	@Override
	public void endElement(String uri, String name, String qName) {
		try {
			if (CATEGORY_TAG1.equals(qName)) {
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

	public List<Page> getPagesList() {
		return pagesList;
	}

}
