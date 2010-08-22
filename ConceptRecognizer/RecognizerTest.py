"""
Olga Rosado, San Diego State University, 2010
Test the ConceptRecognizer's text-mining results for given wiki pages.
This script runs Gene Wiki pages through the ConceptRecognizer module
and outputs results in tab delimited text file
"""
from ConceptRecognizer import *


pagetitle = raw_input("Enter title of wiki page: ")

try:
    r = recognizer(pagetitle)
    conceptDict = r.extractWords(r.xmlDoc)
    conceptDict = r.processGOterms(conceptDict)
    #conceptDict = r.processConceptLinks(conceptDict) 
    fullDict = r.findLinks(conceptDict)
except (Error, InvalidPage, InvalidSite, ServerError, FileError) as inst:
    print 'ERROR:', inst
else:
    f = open(pagetitle + '.txt', 'w')
    f.write("**" + pagetitle.upper() + "**\n")
    f.write("Concepts recognized: " + str(len(fullDict)) + "\n\n\n")
    f.write("Accession\t" + "GO term ID\t" + "Ontology ID\t" + "Concept\t" + "Preferred Name\t" + "Linked\t" + "LinkType\t" + "Link\n")
    f.write("=========\t" + "==========\t" + "===========\t" + "=======\t" + "==============\t" + "======\t" + "========\t" + "====\n")
        
    for k, v in fullDict.iteritems():
        f.write(v.accession+ "\t" + v.GOtermID+ "\t" + v.localOntologyID + "\t" + k + "\t" + v.preferredName + "\t" + v.linked + "\t" + v.linkType + "\t" + v.link + "\n")
    f.close()

