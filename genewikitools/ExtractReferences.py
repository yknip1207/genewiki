import urllib
import re
import sys
from django.utils import simplejson as json
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app

class Reference ():
    def __init__(self):
	self.sentence = ''
	self.reference = ''
	self.pmid = ''
	self.mesh = []

    def parsePmid(self):
        matchObject = re.search("pmid[ =]*(\d+)",self.reference)
        if( not matchObject is None ):
	    self.pmid = matchObject.group(1)

    def exportToHash(self):
	output = {}
	output['sentence'] = self.sentence
	output['reference'] = self.reference
	output['pmid'] = self.pmid
	output['mesh'] = self.mesh
	return(output)

def getSentenceBefore(matchObject):
    # Strategy: 
    #   - retrieve document from beginning to start of match object
    #   - reverse that string
    #   - search for first occurrence of period or newline
    #   - return string from period/newline to start of match object

    strBefore = matchObject.string[0:matchObject.start()]
    strBefore = re.sub("<ref[^>]*>[^<]*</ref>","",strBefore)  # remove all other refs
#    strBefore = re.sub("<ref>[^>]*</ref>",replaceWithDashes,strBefore)
    strBeforeReversed = strBefore[::-1]
    sentenceBreaks = re.finditer("[.\n]",strBeforeReversed)
    for sentenceBreak in sentenceBreaks:
	if sentenceBreak.start() < 5: ### skip if period occurs right before ref
	    continue
        nextStop = sentenceBreak.start()
	break

    # pull out sentence fragment, add a "^" to mark ref location
    sentencePreceding = strBefore[-1*nextStop:]
    sentencePreceding = sentencePreceding + "^"

    ### if a period does not occur immediately before ref, then add after ref until
    ###    period or newline
#    print "RE: ", re.search("\.\s*$",sentencePreceding )
    if( re.search("\.\s*\^$",sentencePreceding ) is None ):
	strAfter = matchObject.string[matchObject.end():]
        strAfter = re.sub("<ref[^>]*>[^<]*</ref>","",strAfter)  # remove all other refs
	sentenceBreaks = re.finditer("[.\n]",strAfter)
	for sentenceBreak in sentenceBreaks:
	    nextStop = sentenceBreak.start()
	    if( sentenceBreak.group(0) == "." ):
                sentencePreceding = sentencePreceding + strAfter[0:nextStop+1]
	    else:
                sentencePreceding = sentencePreceding + strAfter[0:nextStop]

	    break

    ### clean sentence of wiki-formatting characters
    sentencePreceding = re.sub("['[\]]","",sentencePreceding)
		
    return( sentencePreceding )

### Create a Reference object and populate it with info from MatchObject
def createReference( matchObject ):
    ref = Reference()
    ref.reference = matchObject.group(0)
    ref.sentence = getSentenceBefore( matchObject )
    ref.parsePmid()
#    print "MATCH: ",matchObject.start(),"-",matchObject.end()
#    print "REF: ", ref.reference
#    print "SENTENCE: ", ref.sentence
#    print "PMID: ", ref.pmid
#    print ''
    return(ref)

### Given an array of references, populate the MeSH terms
def addMesh( references ):
    pmids = []
    for ref in references:
	if( ref.pmid != ""):
	    pmids.append(ref.pmid)

#    print "PMIDs: ", ",".join(pmids)
    url = "http://genewikitools.appspot.com/Pubmed2Mesh?pmids=" + ",".join(pmids)
    f = urllib.urlopen( url )
    z = f.read()
#    print z
    meshIndex = json.loads(z)
    for ref in references:
	if( ref.pmid != ""):
#	    print "PMID: ", ref.pmid
#	    print meshIndex[ref.pmid]
	    ref.mesh = meshIndex[ref.pmid]

    return( references )


class ExtractReferences( webapp.RequestHandler ):
    def get(self):
	self.response.headers['Content-type'] = 'text/plain'
#	print "Content-type: text/plain"
#	print ''
	articleName = self.request.get('article')

        content = self.getContent(articleName)
#	print "CONTENT: ", content
#	refList = re.findall( '([^.\n]*\.\s*<ref.*</ref>)', content )
#	refList.extend( re.findall( '([^.\n]+<ref.*</ref>[^.\n]*\.)', content ) )
        refList = re.finditer( '(<ref[^<]*</ref>)', content )
	references = []
	for matchObject in refList:
            references.append( createReference(matchObject) )

	references = addMesh( references )

	z = [ x.exportToHash() for x in references ]
	self.response.out.write( json.dumps(z,indent=4) )

    def getContent(self, articleName):
	url = "http://genewikitools.appspot.com/ReadGeneWikiPage?article="+articleName
#	url = "http://localhost:8081/ReadGeneWikiPage?article="+articleName
#	print "URL:",url
#	f = urllib.urlopen( "http://genewikitools.appspot.com/ReadGeneWikiPage?article=AKR1C1" )
	try:
	    f = urllib.urlopen( url )
    	    z = f.read()
	except DownloadError:
	    print "Something went wrong..."
	return z

###
### MAIN
###

application = webapp.WSGIApplication(
		[('.*', ExtractReferences)],
		debug=True)

def main():
    run_wsgi_app(application)

if __name__ == "__main__":
    main()
