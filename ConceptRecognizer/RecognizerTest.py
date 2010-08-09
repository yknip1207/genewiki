"""
Olga Rosado, San Diego State University, 2010
Test the ConceptRecognizer's text-mining results for given wiki pages
This script runs Gene Wiki pages through the ConceptRecognizer module
and outputs results in tab/semicolon delimited text file
"""

from ConceptRecognizer import recognizer

#pagetitle = raw_input("Enter title of page to test ")
pagetitle = "CDKN1B"

try:
    r = recognizer(pagetitle)
    conceptDict = r.extractWords(r.xmlDoc)
    conceptDict = r.processGOterms(conceptDict)
    #conceptDict = r.processConceptLinks(conceptDict) 
    fullDict = r.findLinks(conceptDict)
except Exception:
    print 'An error has occurred in Recognizer test'
else:
    f = open(pagetitle + '.txt', 'w')
    f.write("**" + pagetitle.upper() + "**\n")
    f.write("Concepts recognized: " + str(len(fullDict)) + "\n\n\n")
    f.write('{:15}'.format("Accession;") + '{:15}'.format("GO term ID;") + '{:15}'.format("Ontology ID;") + '{:30}'.format("Concept;") + '{:60}'.format("Preferred Name;") + '{:7}'.format("Linked;") + '{:10}'.format("LinkType;") + "Link\n")
    f.write('{:15}'.format("=========;") + '{:15}'.format("==========;") + '{:15}'.format("===========;") + '{:30}'.format("=======;") + '{:60}'.format("==============;") + '{:7}'.format("======;") + '{:10}'.format("========;") + "====\n")
        
    for k, v in fullDict.iteritems():
        f.write('{:15}'.format(v.accession+ ";") + '{:15}'.format(v.GOtermID+ ";") + '{:15}'.format(v.localOntologyID + ";") + '{:30}'.format(k + ";") + '{:60}'.format(v.preferredName + ";") + '{:7}'.format(v.linked + ";") + '{:10}'.format(v.linkType + ";") + v.link + "\n")
    f.close()

