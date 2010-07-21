""" Test the ConceptRecognizer's text-mining results
This script runs Gene Wiki pages through the ConceptRecognizer module
and outputs results in text file
"""
from ConceptRecognizer import recognizer

pagetitle = raw_input("Enter title of wiki page: ")
r = recognizer(pagetitle)
conceptDict = r.extractWords(r.xmlDoc)
fullDict = r.findLinks(conceptDict)

f = open(pagetitle + '.txt', 'w')
f.write("**" + pagetitle.upper() + "**\n\n\n")
    
for k, v in fullDict.iteritems():
    f.write("CONCEPT: " + k + "\n")
    f.write("PREFERRED NAME: " + v.preferredName + "\n")
    f.write("SYNONYMS: \n")
    for syn in v.synonyms:
        f.write(syn + "\n")
    f.write("LINK: " + v.link + "\n")
    f.write("\n\n")
        

f.close()

