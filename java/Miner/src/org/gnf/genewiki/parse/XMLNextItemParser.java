package org.gnf.genewiki.parse;
import info.bliki.api.AbstractXMLParser;
import info.bliki.api.Page;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Seeks out the next item for an iterated request
 * Code based on <code>XMLPagesParser</code>
 */
public class XMLNextItemParser extends AbstractXMLParser {
	private static final String CATEGORY_MEMBERS = "categorymembers";
	private static final String LINKS = "links";
	
	private String nextitem;
	
	
	public XMLNextItemParser(String xmlText) throws SAXException {
		super(xmlText);
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
		fAttributes = atts;

	   if(CATEGORY_MEMBERS.equals(qName)){
			if(fAttributes.getValue("cmcontinue")!=null){
				setNextitem(fAttributes.getValue("cmcontinue"));
			}
		}else if(LINKS.equals(qName)){
			if(fAttributes.getValue("plcontinue")!=null){
				setNextitem(fAttributes.getValue("plcontinue"));
			}
		}
		/*
		<query-continue>
<categorymembers cmcontinue="24-dehydrocholesterol reductase|" />
</query-continue>
		 */
		fData = null;
	}

	@Override
	public void endElement(String uri, String name, String qName) {
		try {
			 if(CATEGORY_MEMBERS.equals(qName)){
				}else if(LINKS.equals(qName)){
				}

			fData = null;
			fAttributes = null;

		} catch (RuntimeException re) {
			re.printStackTrace();
		}
	}
	
	public String getNextitem() {
		return nextitem;
	}

	public void setNextitem(String nextitem) {
		this.nextitem = nextitem;
	}



}
