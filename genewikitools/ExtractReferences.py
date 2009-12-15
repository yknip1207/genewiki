import urllib
import re
import sys
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app

class Reference ():
    def __init__(self):
	self.sentence = ''
	self.reference = ''
	self.pmid = ''
	self.mesh = []

def replaceWithDashes(mo):
   return (mo.group(1)+"-"*len(mo.group(2))+mo.group(3))

def getSentenceBefore(matchObject):
    strBefore = matchObject.string[0:matchObject.start()]
    strBefore = re.sub("(<ref[^>]*>)([^<]*)(</ref>)",replaceWithDashes,strBefore)
#    strBefore = re.sub("<ref>[^>]*</ref>",replaceWithDashes,strBefore)
    strBeforeReversed = strBefore[::-1]
    sentenceBreaks = re.finditer("[.\n]",strBeforeReversed)
    for sentenceBreak in sentenceBreaks:
	if sentenceBreak.start() < 5:
	    continue
        nextStop = sentenceBreak.start()
	break
    print "BEF: ", strBefore
    print "NS: ", nextStop
    return( strBefore[-1*nextStop:])

def createReference( matchObject ):
    ref = Reference()
    ref.reference = matchObject.group(0)
    ref.sentence = getSentenceBefore( matchObject )
    print "MATCH: ",matchObject.start(),"-",matchObject.end()
    print "REF: ", ref.reference
    print "SENTENCE: ", ref.sentence
    print ''


class ExtractReferences( webapp.RequestHandler ):
    def get(self):
	print "Content-type: text/plain"
	print ''
	articleName = self.request.get('article')
	print articleName

        content = self.getContent(articleName)
#	print "CONTENT: ", content
#	refList = re.findall( '([^.\n]*\.\s*<ref.*</ref>)', content )
#	refList.extend( re.findall( '([^.\n]+<ref.*</ref>[^.\n]*\.)', content ) )
        refList = re.finditer( '(<ref[^<]*</ref>)', content )
	references = []
	for matchObject in refList:
            references.append( createReference(matchObject) )

	print "count: ",len(references)

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
