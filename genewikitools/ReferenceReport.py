from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from django.utils import simplejson as json
import urllib
import re
#import sys

### GLOBAL VARS
#species = {"Humans": 1,"Mice": 1,"Rats": 1,"Zebrafish": 1,"Drosophila": 1}
species = ["Humans","Mice","Rats","Zebrafish","Drosophila"]


class ReferenceReport( webapp.RequestHandler ):


    def get(self):
	self.response.headers['Content-type'] = 'text/plain'
#	print "Content-type: text/plain"
#	print ''
	articleName = self.request.get('article')

        content = self.getReferences(articleName)
#	print "CONTENT: ", content
	references = json.loads(content)
#	print "LEN: ", len(references)
        speciesList = "\t".join(species)
	header = "\t".join(["Human Entrez Gene ID",speciesList,"Wikipedia Article Name","PMID","Citing sentence"])+"\n"
	self.response.out.write( header )
#	print "SPECIES: ",species
        for ref in references:
            if( ref['pmid'] == "" ):
		continue
	    pmid = ref['pmid']
	    sentence = ref['sentence']
	    meshTerms = ",".join(ref['mesh'])
	    entrezGeneId = ref['geneId']
	    speciesTally = []
	    for org in species:
		if( re.search(org,meshTerms) ):
		    speciesTally.append("YES")
	        else:
		    speciesTally.append("NO")
	    speciesInfo = "\t".join(speciesTally)
	    newLine = "\t".join([entrezGeneId,speciesInfo,articleName,pmid,sentence])+"\n"
	    self.response.out.write( newLine )

    def getReferences(self, articleName):
	url = "http://genewikitools.appspot.com/ExtractReferences?article="+articleName
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
		[('.*', ReferenceReport)],
		debug=True)

def main():
    run_wsgi_app(application)

if __name__ == "__main__":
    main()
