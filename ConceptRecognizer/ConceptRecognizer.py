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
ontologies = '42925'  #gene ontology extension

class conceptInfo:
    """ This is a data structure to contain the information of a concept
    recognized by the NCBO text-mining tool"""
    def __init__(self, preferredName="", synonyms=[], link=""):
        self.preferredName=preferredName
        self.synonyms = synonyms
        self.link = link

        
class recognizer:
    """ This class looks at a wiki page and looks for biomedical keywords. It then looks to see
    if there are any links for such words, and if not, it will look for related articles in the same
    or another wiki project and save links (wiki link format) in a dictionary data structure.
            pagetitle - tile of wiki article to add interwiki links to
            Ontologies - NCBO localOntologyID to use for recognizing concepts
            family - the wiki family (project) of the article of interest
            language - language of the article of interest
    """
    def __init__(self, pagetitle, Ontologies=ontologies, family="wikipedia", language="en"):
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
        
    def getContent(self, pagetitle, family="wikipedia", language="en"):
        """Get the contents of a wiki article by title"""
        pagecontent = page.get(get_redirect = True)
        pagecontent = pagecontent.encode(page.encoding(), "replace")
        return pagecontent

    def textMine(self, text, Ontologies=ontologies):
        """Send text to NCBO text-mining tool and retrieve concept info
        output an xml object"""
        url = 'http://rest.bioontology.org/obs/annotator'
        values = {
            'scored':'false',
            'textToAnnotate':text,
            'ontologiesToExpand':Ontologies,
            'ontologiesToKeepInResult':Ontologies
            }
        data = urllib.urlencode(values)
        response = urllib2.urlopen(url, data, 600)
        result = response.read()
        return result

    def stripTags(self, string, tagName):
        """Strips xml tag, <tagName>, from string"""
        newStr = string.replace("<"+tagName+">", "")
        newStr = newStr.replace("</"+tagName+">", "")
        return newStr

    def extractWords(self, xmlObject):
        """ Parse the xml string obtained from NCBO, look for annotationBean tag, which is the xml
        node containing all information on a particular concept.
        Returns a dictionary with concepts as key and preferredName and synonyms as values"""
        xmldoc = minidom.parseString(xmlObject)
        conceptList = xmldoc.getElementsByTagName('annotationBean')        
        keylist = {} # dictionary to store info
        for concept in conceptList:
            word = str((concept.getElementsByTagName('name'))[0].toxml())
            word = self.stripTags(word, 'name')
            keylist[word]=conceptInfo()
            preferredName = str((concept.getElementsByTagName('preferredName'))[0].toxml())
            synList = concept.getElementsByTagName('string')
            synonyms = []
            for synonym in synList:
                syn = str(synonym.toxml())
                syn = self.stripTags(syn, 'string')
                synonyms.append(syn)
            keylist[word].preferredName = self.stripTags(preferredName, 'preferredName')
            keylist[word].synonyms = synonyms           
        return keylist

    def processConceptLinks(self, keywordDict):
        """Get a list of the links already in the article and see if the concepts found
        on the NCBO, which are in the keywordDict, are already there. If they are, there is no need to
        look for them so they will be deleted from the dictionary. Returns a modified keyword dictionary
        """
        pagelinks = page.linkedPages()
        linklist = []
        for link in pagelinks:
            linklist.append(link.title())
        # delete any concepts from the dictionary that are already linked
        for word in keywordDict.keys():
            if word or keywordDict[word].preferredName or [a for a in keywordDict[word].synonyms] in linklist:
                del keywordDict[word]
        return keywordDict

    def findLinks(self, keywordDict, family="wikipedia", language="en"):
        """See if there are wiki pages for the concepts remaining and store
        links (if any) as link value in keylist dictionary"""
        linksite = getSite(language, family)  #this is the wiki site where additional links will be retrieved from                  
        for word in keywordDict.keys():
            wikilink = ""
            if Page(linksite, word).exists():
                wikilink = Page(linksite, word)
            elif Page(linksite, keywordDict[word].preferredName).exists():
                wikilink = Page(linksite, keywordDict[word].preferredName)
            else:
                for syn in keywordDict[word].synonyms:
                    if Page(linksite, syn).exists():
                        wikilink = Page(linksite, syn)
                        break
            if wikilink != "":
                if family==fmly and language ==lng: # local link
                    if wikilink.title()!= word:
                        link = '[[' + wikilink.title() + '|' + word + ']]'
                    else:
                        link = wikilink.aslink()
                elif family==fmly:
                    if wikilink.title()!= word:
                        link = '[[' + wikifam[family] + ':' + wikilink.title() + '|' + word + ']]'
                    else:
                        link = '[[' + wikifam[family] + ':' + wikilink.title() + ']]'
                elif language==lng:
                    if wikilink.title()!= word:
                        link = '[[' + language + ':' + wikilink.title() + '|' + word + ']]'
                    else:
                        link = '[[' + language + ':' + wikilink.title() + ']]'
                elif wikilink.title()!= word:
                    link = '[[' + wikifam[family] + ':' + language + ':' + wikilink.title() + '|' + word + ']]'
                else:
                    link = '[[' + wikifam[family] + ':' + language + ':' + wikilink.title() + ']]'                
                keywordDict[word].link = link
        return keywordDict        
        
    
    
        
