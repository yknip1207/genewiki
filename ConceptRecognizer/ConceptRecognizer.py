"""
ConceptRecognizer.py
Created by Olga Rosado, 2010 - San Diego State University
Created for the Gene Wiki project

This module looks for biomedical concepts in gene wiki pages and inserts wiki hyperlinks.
"""

# Set path to pywikipedia folder
mydir = "./"
pwbdir = mydir + "pywikipedia/"
import sys
sys.path.append(pwbdir)
from wikipedia import *
import urllib
import urllib2
from xml.dom import minidom

# Dictionary of wiki families and their link abbreviations
wikifam = {}
wikifam['wikipedia'] = 'w'
wikifam['wiktionary'] = 'wikt'
wikifam['wikinews'] = 'n'
wikifam['wikisource'] = 's'
wikifam['wikispecies'] = 'species'
wikifam['wikiversity'] = 'v'

# Default ontologies
ontologies = '42876,42879'  #gene ontology and human disease


class recognizer:
""" This class looks at a wiki page and looks for biomedical keywords. It then looks to see
if there are any links for such words, and if not, it will look for related articles in the same
or another wiki project and save links (wiki link format) in a dictionary.
        pagetitle - tile of wiki article to add interwiki links to
        Ontologies - NCBO ontologies to use for recognizing concepts
        family - the wiki family (project) of the article of interest
        language - language of the article of interest
        linkfam - the wiki family (project) where to look for related articles (links)
        linklang - the wiki language where to look for related articles (links)
"""
    def __init__(self, pagetitle, Ontologies=ontologies, family="wikipedia", language="en", linkfam="wikipedia", linklang="en"):
        global fmly
        global lng
        global site
        global page
        site = getSite(language, family)
        page = Page(site, pagetitle)
        fmly = family
        lng = language
        self.content = self.getContent(pagetitle, family, language)
        self.xmlDoc = self.textMine(self.content, Ontologies)
        #self.Keywords = 
        self.LinksDict = self.findLinks(self.extractWords(self.xmlDoc), linkfam, linklang)
                
    def getContent(self, pagetitle, family, language):
        # Get the contents of a wiki article by title
        pagecontent = page.get(get_redirect = True)
        pagecontent = pagecontent.encode(page.encoding(), "replace")
        return pagecontent

    def textMine(self, text, Ontologies):
        # Send text to NCBO text-mining tool and retrieve keywords
        # output a dictionary of keywords, values are empty
        url = 'http://rest.bioontology.org/obs/annotator'
        values = {
            'scored':'false',
            'textToAnnotate':text,
            'format':'xml',
            'withSynonyms':'false',
            'ontologiesToExpand':Ontologies,
            'ontologiesToKeepInResult':Ontologies
            }
        data = urllib.urlencode(values)
        response = urllib2.urlopen(url, data, 600)
        result = response.read()
        return result

    def extractWords(self, xmlObject):
        # Parse the xml string obtained from NCBO, look for preferredName tag
        # returns a dictionary with empty values
        xmldoc = minidom.parseString(xmlObject)
        conceptList = xmldoc.getElementsByTagName('preferredName')        
        # Create dictionary and insert keywords
        keylist = {}
        for node in conceptList:
            # Convert xml node to string, then replace end tag with empty string
            keyw=str(node.toxml()).replace("</preferredName>", "")
            # replace front tag as well
            keyw=keyw.replace("<preferredName>", "")
            # insert keyword.  the value is empty
            keylist[keyw]=""
        return keylist

    def findLinks(self, keywordDict, family, language):
        linksite = getSite(language, family)  #this is the wiki project additional links will be retrieved from 
        # Get a list of the links already in the article and see if the keywords found
        # on the NCBO are already there. If they are, there is no need to look for them
        pagelinks = page.linkedPages()
        linklist = []
        for link in pagelinks:
            linklist.append(link.title())
        # delete any keywords that are already linked
        for word in keywordDict.keys():
            if word in linklist:
                del keywordDict[word]
        # Now, see if there are wiki pages for the keywords remaining and store
        # links (if any) as value in keylist dictionary                 
        for word in keywordDict.keys():
            wikilink = Page(linksite, word)
            if wikilink.exists():
                if family==fmly:
                    link = wikilink.aslink() # local link
                else:
                    link = '[[' + wikifam[family] + ':' + language + ':' + wikilink.title() + ']]'
                keywordDict[word] = link
        return keywordDict        
        
    
    
        