/**
 * 
 */
package org.genewiki.parse;

import java.util.ArrayList;
import java.util.List;

import org.gnf.wikiapi.AbstractXMLParser;
import org.gnf.wikiapi.Category;
import org.gnf.wikiapi.Link;
import org.gnf.wikiapi.Page;
import org.gnf.wikiapi.Revision;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author bgood
 *
 */
public class XMLPagesParserFromGenerator  extends AbstractXMLParser {



	private String geicontinue;
	private static final String CATEGORY_MEMBERS = "embeddedin";
	
private static final String PAGE_TAG1 = "page";
	
	private static final String PAGE_TAG2 = "p";

	private static final String REV_ID_TAG = "rev"; 
	
	private static final String REVID = "revid";
	
	private static final String PARENTID = "parentid";
	
	private static final String REVSIZE = "size";

	private static final String CATEGORY_ID = "cl";

	private static final String PAGE_ID = "pageid";

	private static final String NS_ID = "ns";

	private static final String TITLE_ID = "title";

	private static final String USER = "user";
	
	private static final String URL_ID = "url";

	private static final String THUMB_URL_ID = "thumburl";
	
	private static final String ANON_ID = "anon";

	private static final String PL_ID = "pl";

	private static final String TIMESTAMP_ID = "timestamp";

	private static final String IMAGEINFO_ID = "imageinfo";

	private static final String II_ID = "ii";

    private static final String EDIT_TOKEN_ID = "edittoken";

	private Page fPage;

	private Revision fRevision;

	private List<Page> pagesList;

	public XMLPagesParserFromGenerator(String xmlText) throws SAXException {
		super(xmlText);
		pagesList = new ArrayList<Page>();
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
		fAttributes = atts;

		if ( (PAGE_TAG1.equals(qName)) ||
			   (PAGE_TAG2.equals(qName)) ) {
			fPage = new Page();
			fPage.setPageid(fAttributes.getValue(PAGE_ID));
			fPage.setNs(fAttributes.getValue(NS_ID));
			fPage.setTitle(fAttributes.getValue(TITLE_ID));
            fPage.setEditToken(fAttributes.getValue(EDIT_TOKEN_ID));
		} else if (REV_ID_TAG.equals(qName)) {
			fRevision = new Revision();
			fRevision.setAnon(fAttributes.getValue(ANON_ID));
			fRevision.setTimestamp(fAttributes.getValue(TIMESTAMP_ID));
			fRevision.setRevid(fAttributes.getValue(REVID));
			fRevision.setParentid(fAttributes.getValue(PARENTID));
			fRevision.setUser(fAttributes.getValue(USER));
			if(fAttributes.getValue(REVSIZE)!=null){
				fRevision.setSize(Integer.parseInt(fAttributes.getValue(REVSIZE)));
			}
			fPage.setCurrentRevision(fRevision);
		} else if (CATEGORY_ID.equals(qName)) {
			if (fPage != null) {
				Category cat = new Category();
				cat.setNs(fAttributes.getValue(NS_ID));
				cat.setTitle(fAttributes.getValue(TITLE_ID));
				fPage.addCategory(cat);
			}
		} else if (PL_ID.equals(qName)) {
			if (fPage != null) {
				Link link = new Link();
				link.setNs(fAttributes.getValue(NS_ID));
				link.setTitle(fAttributes.getValue(TITLE_ID));
				fPage.addLink(link);
			}
		} else if (II_ID.equals(qName)) {
			// <imgeinfo><ii url="...">...</imageinfo>
			if (fPage != null) {
				fPage.setImageUrl(fAttributes.getValue(URL_ID));
				fPage.setImageThumbUrl(fAttributes.getValue(THUMB_URL_ID));
			}
		}else if(CATEGORY_MEMBERS.equals(qName)){
			if(fAttributes.getValue("geicontinue")!=null){
				setGeicontinue(fAttributes.getValue("geicontinue"));
			}
		}
		fData = null;
	}

	@Override
	public void endElement(String uri, String name, String qName) {
		try {
			if (REV_ID_TAG.equals(qName)) {// || CATEGORY_ID.equals(qName)) {
				if (fRevision != null) {
					fRevision.setContent(getString());
				}
				// System.out.println(getString());
			} else if (PAGE_TAG1.equals(qName) || PAGE_TAG2.equals(qName)) {// || CATEGORY_ID.equals(qName)) {
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

	public String getGeicontinue() {
		return geicontinue;
	}

	public void setGeicontinue(String geicontinue) {
		this.geicontinue = geicontinue;
	}

}
