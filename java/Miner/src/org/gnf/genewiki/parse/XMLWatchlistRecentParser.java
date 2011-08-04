package org.gnf.genewiki.parse;


import org.gnf.wikiapi.AbstractXMLParser;
import org.gnf.wikiapi.Page;
import org.gnf.wikiapi.Revision;

import java.util.ArrayList;
import java.util.List;

import org.gnf.genewiki.GWRevision;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Reads <code>Page</code> data from an XML file generated by the <a href="http://meta.wikimedia.org/w/api.php">Wikimedia API</a>
 * READS data from a call for category members
 * Code based on <code>XMLPagesParser</code>
 */
public class XMLWatchlistRecentParser extends AbstractXMLParser {
//    
    private static final String REVISIONS = "revisions";
    
    private static final String REV = "rev";
    
    private static final String REV_ID = "revid";
    
    private static final String PARENT_ID = "parentid";
    
    private static final String COMMENT_ID = "comment";
    
    private static final String MINOR = "minor";
    
    private static final String USER = "user";
    
    private static final String TIMESTAMP = "timestamp";
    
    private static final String SIZE = "size";

	private GWRevision fRev;

	private List<GWRevision> revsList;
	
	private String cmcontinue;

	private String tmptitle;
	
	public XMLWatchlistRecentParser(String xmlText) throws SAXException {
		super(xmlText);
		revsList = new ArrayList<GWRevision>();
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
		fAttributes = atts;
		if("page".equals(qName)){
			tmptitle = fAttributes.getValue("title");
		}else if ( (REV.equals(qName)) ) {
			fRev = new GWRevision();
			fRev.setTitle(tmptitle);
			fRev.setRevid(fAttributes.getValue(REV_ID));
			fRev.setUser(fAttributes.getValue(USER));
			fRev.setTimestamp(fAttributes.getValue(TIMESTAMP));
			fRev.setSize(Integer.parseInt(fAttributes.getValue(SIZE)));
			fRev.setParentid(fAttributes.getValue(PARENT_ID));
			fRev.setComment(fAttributes.getValue(COMMENT_ID));
			fRev.setMinor(fAttributes.getValue(MINOR));
		}
		else if("watchlist".equals(qName)){
			if(fAttributes.getValue("gwlstart")!=null){
				setCmcontinue(fAttributes.getValue("gwlstart"));
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
			if (REV.equals(qName)) {
				if (fRev != null) {
					fRev.setContent(getString());
					revsList.add(fRev);
				}
				// System.out.println(getString());
			}

			fData = null;
			fAttributes = null;

		} catch (RuntimeException re) {
			re.printStackTrace();
		}
	}

	public List<GWRevision> getRevsList() {
		return revsList;
	}

}
