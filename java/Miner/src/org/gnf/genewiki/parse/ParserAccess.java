package org.gnf.genewiki.parse;

import info.bliki.api.Page;
import info.bliki.api.Revision;
import info.bliki.api.XMLPagesParser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gnf.genewiki.GWRevision;
import org.gnf.genewiki.GeneWikiPage;

public class ParserAccess implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3987941477941592411L;
	String nextTitle;
	String nextItemId;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public String parseWikiNextXml(String wikiXml){
		String nextitem = "";
		try {
			XMLNextItemParser xmlPagesParser = new XMLNextItemParser(wikiXml);
			xmlPagesParser.parse();
			nextitem = xmlPagesParser.getNextitem();
			if(nextitem==null){
				nextitem = "";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nextitem;
	}

	public List<GeneWikiPage> parseWikiExtlinksApiXml(String wikiXml){
		List<GeneWikiPage> pages = null;
		try {
			XMLExtLinksParser xmlPagesParser = new XMLExtLinksParser(wikiXml);
			xmlPagesParser.parse();
			pages = xmlPagesParser.getPagesList();
			System.out.println("");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pages;
	}

	public List<Page> parseWikiCategoriesApiXml(String wikiXml){
		List<Page> pages = null;
		try {
			XMLCatMembersParser xmlPagesParser = new XMLCatMembersParser(wikiXml);
			xmlPagesParser.parse();
			pages = xmlPagesParser.getPagesList();
			nextTitle = xmlPagesParser.getCmcontinue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pages;
	}

	public List<String> parseDifsXml(String wikiXml){
		List<String> difs = new ArrayList<String>();
		try {
			XMLDiffParser xmlDifParser = new XMLDiffParser(wikiXml);
			xmlDifParser.parse();
			String diff = xmlDifParser.getDiff();
			if(diff!=null){
				String reg = "<span class=\"diffchange\">.{3,}?</span>";
				Pattern pattern = 
					Pattern.compile(reg);
				Matcher matcher = 
					pattern.matcher(diff);

				while (matcher.find()) {
					String dif = matcher.group().trim();
					if(dif.length()>32){
						dif = dif.substring(25, dif.length()-7);
						difs.add(dif);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return difs;
	}

	public List<GWRevision> parseRevisionsXml(String wikiXml){
		List<GWRevision> revs = null;
		try {
			XMLRevisionsParser xmlRevsParser = new XMLRevisionsParser(wikiXml);
			xmlRevsParser.parse();
			revs = xmlRevsParser.getRevsList();
			nextItemId = xmlRevsParser.getCmcontinue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return revs;
	}

	public List<Page> parseWikiEmbeddedApiXml(String wikiXml){
		List<Page> pages = null;
		try {
			XMLEmbeddedInParser xmlPagesParser = new XMLEmbeddedInParser(wikiXml);
			xmlPagesParser.parse();
			pages = xmlPagesParser.getPagesList();
			nextTitle = xmlPagesParser.getEicontinue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pages;
	}

	public static List<Page> parseWikiContentApiXml(String wikiXml){
		List<Page> pages = null;
		try {
			XMLPagesParser xmlPagesParser = new XMLPagesParser(wikiXml);
			xmlPagesParser.parse();
			pages = xmlPagesParser.getPagesList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pages;
	}

	public String getNextTitle() {
		return nextTitle;
	}

	public void setNextTitle(String nextTitle) {
		this.nextTitle = nextTitle;
	}

	public String getNextItemId() {
		return nextItemId;
	}

	public void setNextItemId(String nextItemId) {
		this.nextItemId = nextItemId;
	}


}
