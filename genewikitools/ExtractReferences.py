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
	self.geneId = ''

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
	output['geneId'] = self.geneId
	return(output)

def getSentenceBefore(matchObject):
    # Strategy: 
    #   - retrieve document from beginning to start of match object
    #   - reverse that string
    #   - search for first occurrence of period or newline
    #   - return string from period/newline to start of match object

    strBefore = matchObject.string[0:matchObject.start()]
    strBefore = re.sub("<ref[^>]*>[^<]*</ref>","",strBefore)  # remove all other refs
    strBefore = re.sub("<ref[^>]*>","",strBefore)  # remove <ref name=pmid8810341\/>

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
        strAfter = re.sub("<ref[^>]*>","",strAfter)  # remove <ref name=pmid8810341\/>
	sentenceBreaks = re.finditer("[.\n]",strAfter)
	for sentenceBreak in sentenceBreaks:
	    nextStop = sentenceBreak.start()
	    if( sentenceBreak.group(0) == "." ):
                sentencePreceding = sentencePreceding + strAfter[0:nextStop+1]
	    else:
                sentencePreceding = sentencePreceding + strAfter[0:nextStop]

	    break

    ### clean sentence of wiki-formatting characters
    # fix [[target|label]] type of wikilinks
    sentencePreceding = re.sub(r"\[\[[^]]*\|([^]|]*)\]\]",r"\1",sentencePreceding)
    # remove all other wikilinking syntax
    sentencePreceding = re.sub("['[\]]","",sentencePreceding)
		
    return( sentencePreceding )

### Create a Reference object and populate it with info from MatchObject
def createReference( matchObject,entrezGeneId ):
    ref = Reference()
    ref.reference = matchObject.group(0)
    ref.sentence = getSentenceBefore( matchObject )
    ref.geneId = entrezGeneId
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

    if( len(pmids) == 0 ):
	return(references)
#    print "PMIDs: ", ",".join(pmids)
    url = "http://genewikitools.appspot.com/Pubmed2Mesh"
    urlparams = {
         'pmids': ",".join(pmids)
	 }
#    try:
#        f = urllib.urlopen( url, urllib.urlencode(urlparams) )
#        z = f.read()
#	print "Z: ", z
#        meshIndex = json.loads(z)
#    except:
##	return(references)
#	print 'Content-type: text/plain'
#	print ''
#	print "ExtractReferences: Error reading Pubmed2Mesh"
#	print "URL: ", url
#	print "urlparams: ", urlparams
#	print "num pubmeds: ", len(pmids)
#	print "Content: ", z
#	sys.exit(1)

    f = urllib.urlopen( url, urllib.urlencode(urlparams) )
    z = f.read()
    try:
        meshIndex = json.loads(z)
    except:
	print 'Content-type: text/plain'
	print ''
	print "ExtractReferences: Error reading Pubmed2Mesh"
	print "URL: ", url
	print "urlparams: ", urlparams
	print "num pubmeds: ", len(pmids)
	print "Content: ", z, "."
	sys.exit(1)
        

#    print z

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
	articleName = re.sub(" ","_",articleName)

        content = self.getContent(articleName)
	mo = re.search("{{PBB\|geneid=(\d+)}}",content)
	entrezGeneId = mo.group(1)
	
#	print "CONTENT: ", content
#	refList = re.findall( '([^.\n]*\.\s*<ref.*</ref>)', content )
#	refList.extend( re.findall( '([^.\n]+<ref.*</ref>[^.\n]*\.)', content ) )
        refList = re.finditer( '(<ref[^<]*</ref>)', content )
	references = []
	for matchObject in refList:
            references.append( createReference(matchObject,entrezGeneId) )

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
	except :
            print 'Content-type: text/plain'
	    print ''
	    print "Something went wrong..."
	    print "URL: ", url
	    print "Z: ", z
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
