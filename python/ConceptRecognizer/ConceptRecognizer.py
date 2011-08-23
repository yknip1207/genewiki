"""
ConceptRecognizer.py
Created by Olga Rosado, 2010 - San Diego State University
Created for the Gene Wiki project

This module looks for biomedical concepts in gene wiki pages and looks for relevant hyperlinks in wiki families.

Classes:
    conceptInfo - data structure for containing concepts and relevant information obtained from NCBO
    synInfo - data structure for containing information on concept synonyms
    recognizer - retrieves wiki articles and looks for biomedical concepts based on ontologies availabe in the NCBO text-mining tool

Exceptions:
    Error - base error class for general script problems
    InvalidPage - raised when wiki title is not recognized, wiki page does not exist, or cannot be found
    InvalidSite - raised when an invalid wiki family and/or language is provided
    ServerError - raised when there is an issue connecting to the wikipedia or NCBO website
    FileError - raised when there is a problem opening a file

Methods:
    getContent - retrieves the content of wiki article by wiki title, family and language
    textMine - sends text to the NCBO for text-mining using given ontologies
    stripTags - strips xml tag from string
    extractWords - parses xml object, looking for biomedical concepts and ontology information. Stores info in dictionary
    processGOterms - loads GO term and synonym information from mysql dump files.  Inserts synonym info to dictionary
    processConceptLinks - gets a list of links already in wiki page, then checks to see if any of the concepts recognized are linked.
    findLinks - finds wiki links for concepts that are not linked on wiki family and language provided
    
Sample code:
To text-mine the english Insulin wikipedia page and look for biomedical concept links in the english wikipedia:
    r = recognizer('Insulin', 'wikipedia', 'en')
    insulin = r.extractWords(r.xmlDoc)
    insulin = r.processGOterms(insulin)
    insulin = r.processConceptLinks(insulin)
    insulin = r.findLinks(insulin, 'wikipedia', 'en')

The insulin variable will contain a dictionary with concepts as keys and conceptInfo as value.  The links would be in
insulin[concept].link
"""

# Add path to pywikipedia folder
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

# Default values
ontologies = '42925,42986,42989'  #gene ontology, gene ontology extension, human disease ontology by NCBO's localOntologyID


# Exception classes
class Error(Exception):
    """ConceptRecognizer error"""

class InvalidPage(Error):
    """Invalid wiki page"""

class InvalidSite(Error):
    """Site does not exist"""

class ServerError(Error):
    """Got unexpected server response"""

class FileError(Error):
    """Trouble manipulating file"""


# Data structure classes    
class conceptInfo:
    """ This is a data structure to contain the information of a concept
    recognized by the NCBO text-mining tool"""
    def __init__(self):
        self.accession = ""
        self.localOntologyID = ""
        self.GOtermID = "NA"
        self.preferredName=""
        self.synonyms = {}
        self.linked = "False"
        self.link = "NA"
        self.linkType = "NA"

class synInfo:
    """ This is a data structure to contain the information of a GO synonym"""
    def __init__(self):
        self.termID = "NA"
        self.typeID="NA"


# Recognizer class        
class recognizer:
    """ This class looks at a wiki page and looks for biomedical keywords. It then looks to see
    if there are any links for such words, and if not, it will look for related articles in the same
    or another wiki project and save links (wiki link format) in a dictionary data structure.
            pagetitle - tile of wiki article to text-mine
            Ontologies - NCBO localOntologyIDs to use for recognizing concepts, separated by comas (no spaces)
            family - the wiki family (project) of the article of interest
            language - language of the article of interest
    """
    def __init__(self, pagetitle, Ontologies=ontologies, family="wikipedia", language="en"):
        global fmly
        global lng
        global site
        global page
        try:
            site = getSite(language, family)
        except NoSuchSite:
            raise InvalidSite('Invalid wiki language and/or family')
        else:
            page = Page(site, pagetitle)
            fmly = family
            lng = language
            self.content = self.getContent(pagetitle, family, language)
            self.xmlDoc = self.textMine(self.content, Ontologies)
        
    def getContent(self, pagetitle, family="wikipedia", language="en"):
        """Get the contents of a wiki article by title, wiki family and language"""
        if pagetitle is None:
            raise Error('No page title provided for recognizer')
        try:
            pagecontent = page.get(get_redirect = True)
        except (NoPage, BadTitle, PageNotFound, ServerError):
            raise InvalidPage('Invalid wiki page title')
        else:
            pagecontent = pagecontent.encode(page.encoding(), "replace")
            return pagecontent

    def textMine(self, text, Ontologies=ontologies):
        """Sends page content to NCBO text-mining tool and retrieves biomedical concepts
        Returns an xml object"""
        if text is None:
            raise Error('No content to mine')
        url = 'http://rest.bioontology.org/obs/annotator'
        values = {
            'scored':'false',
            'textToAnnotate':text,
            'ontologiesToExpand':Ontologies,
            'ontologiesToKeepInResult':Ontologies
            }
        data = urllib.urlencode(values)
        try:
            response = urllib2.urlopen(url, data, 600)
        except (urllib2.URLError, urllib2.HTTPError):
            raise ServerError('Error opening NCBO service website')
        else:
            result = response.read()
            return result

    def stripTags(self, string, tagName):
        """Strips xml tag, <tagName>, from string"""
        if string is None:
            raise Error('String cannot be None')
        newStr = string.replace("<"+tagName+">", "")
        newStr = newStr.replace("</"+tagName+">", "")
        return newStr

    def extractWords(self, xmlObject):
        """ Parse the xml object obtained from NCBO, look for annotationBean tag, which is the xml
        node containing all information on a particular concept.
        Returns a dictionary with concepts as key and preferredName, synonyms, and conceptID as values"""
        if xmlObject is None:
            raise Error('No xml object provided for parsing')
        try:
            xmldoc = minidom.parseString(xmlObject)
        except Exception:
            raise Error('Error parsing xml object')
        else:
            conceptList = xmldoc.getElementsByTagName('annotationBean')        
            keylist = {} # dictionary to store info
            for concept in conceptList:
                if len(concept.getElementsByTagName('name'))==0:
                    word = "unknown"
                else:
                    word = str((concept.getElementsByTagName('name'))[0].toxml())
                    word = self.stripTags(word, 'name')
                keylist[word]=conceptInfo()
                conceptID = str((concept.getElementsByTagName('localConceptId'))[0].toxml())
                preferredName = str((concept.getElementsByTagName('preferredName'))[0].toxml())
                if len(concept.getElementsByTagName('string'))==0:
                    synList = "None"
                else:
                    synList = concept.getElementsByTagName('string')
                    for synonym in synList:
                        syn = str(synonym.toxml())
                        syn = self.stripTags(syn, 'string')
                        keylist[word].synonyms[syn]=synInfo()
                conceptID = (self.stripTags(conceptID, 'localConceptId')).split('/')
                keylist[word].localOntologyID = conceptID[0]
                keylist[word].accession = conceptID[1]
                keylist[word].preferredName = self.stripTags(preferredName, 'preferredName')
            return keylist

    def processGOterms(self, keywordDict):
        """Load the details of the Gene Ontology (GO) terms and synonyms from the MySQL dump files. Add info
        to the synonyms dictionary"""
        if keywordDict is None:
            raise Error('No keyword dictionary provided for processing')
        synDetails = {}
        termDetails = {}
        try:
            termf = open('go_daily-termdb-tables/term.txt', 'r+')
        except IOError:
            raise FileError('Could not open GO term table dump file')
        else:
            termfile = termf.readlines()
            termf.close()
            for line in termfile:
                details = line.split("\t")
                termDetails[details[3]]=details[0]
        try:
            synf = open('go_daily-termdb-tables/term_synonym.txt', 'r+')
        except IOError:
            raise FileError('Could not open GO synonym table dump file')
        else:
            synfile = synf.readlines()
            synf.close()
            for line in synfile:
                details = line.split("\t")
                synDetails[details[1]]=synInfo()
                synDetails[details[1]].termID=details[0]
                synDetails[details[1]].typeID=details[3] # key='term_synonym' and value='synonym_type_id', 'term_id'
            for word in keywordDict.keys():
                if keywordDict[word].accession.startswith('GO'):
                    if keywordDict[word].accession in termDetails.keys():
                        keywordDict[word].GOtermID = termDetails[keywordDict[word].accession]
                    if keywordDict[word].synonyms != "None":
                        for syn in keywordDict[word].synonyms.keys():
                            if syn in synDetails.keys():
                                keywordDict[word].synonyms[syn]=synDetails[syn]
        return keywordDict

    def processConceptLinks(self, keywordDict):
        """Get a list of the links already in the article and see if the concepts (or preferredName or any synonym) found
        on the NCBO, which are in the keywordDict, are already there. If they are, there is no need to
        look for them so set the 'Linked' property to True. Returns a modified keyword dictionary
        """
        if keywordDict is None:
            raise Error('No keyword dictionary provided for processing')
        pagelinks = page.linkedPages()
        linklist = []
        for link in pagelinks:
            linklist.append(link.title())
        for word in keywordDict.keys():
            if word or keywordDict[word].preferredName or [a for a in keywordDict[word].synonyms.keys()] in linklist:
                keywordDict[word].linked = "True"
        return keywordDict

    def findLinks(self, keywordDict, family="wikipedia", language="en"):
        """If any of the concepts are not linked (linked property set to False) then see if there are wiki pages for these and store
        links (if any) as link value in keylist dictionary. Only synonyms that are not of typeID 71 or 40 will be considered for links.
        Links are formatted in wikilink format."""
        if keywordDict is None:
            raise Error('No keyword dictionary provided for linking')
        try:
            linksite = getSite(language, family)  #this is the wiki site where additional links will be retrieved from                  
        except NoSuchSite:
            raise InvalidSite('Invalid wiki language and/or family for links')
        else:
            for word in keywordDict.keys():
                wikilink = ""
                if keywordDict[word].linked == "False":
                    if Page(linksite, keywordDict[word].preferredName).exists():
                        wikilink = Page(linksite, keywordDict[word].preferredName)
                        keywordDict[word].linkType = "preferred"
                    else:
                        for syn in keywordDict[word].synonyms.keys():
                            if Page(linksite, syn).exists() and keywordDict[word].synonyms[syn].typeID != ("71" or "40"):
                                wikilink = Page(linksite, syn)
                                if wikilink.title() == (word or word.capitalize()):
                                    keywordDict[word].linkType = "concept"
                                else:
                                    keywordDict[word].linkType = "synonym"
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
        
    
    
        
