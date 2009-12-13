from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from django.utils import simplejson as json
import urllib
from xml.dom import minidom


###
### CLASSES
###

class Pubmed2Mesh( webapp.RequestHandler):
    def get(self):
        self.response.headers['Content-type'] = 'text/plain'

        pmidList = (16492761, 18464898)
	pmidParam = self.request.get('pmids')
	pmidList = pmidParam.split(",")
        
	outputHash = {}

        for pmid in pmidList:
            MeshList = self.getMesh(pmid)
	    outputHash[pmid] = MeshList
        
        self.response.out.write( json.dumps( outputHash, indent = 4))

    def getMesh( self, pmid ):
        urlparams = {
            'db': 'pubmed',
            'retmode': 'xml',
            'id': pmid,
	    'email': 'asu@gnf.org'
            }
        eutilsURL = 'http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi'
        
        f = urllib.urlopen( eutilsURL, urllib.urlencode(urlparams) )
        dom = minidom.parse(f)
        
        nodeList = dom.getElementsByTagName('MeshHeading')
    #    print "Length: ", nodeList.length
        MeshList = []
        for node in nodeList:
            for n in node.getElementsByTagName('DescriptorName'):
                for n2 in n.childNodes:
    #                print "D: ", n2.data
                    MeshList.append(n2.data)
        return( MeshList )

###
### MAIN
###

application = webapp.WSGIApplication(
                                     [('.*', Pubmed2Mesh)],
                                     debug=True)

def main():
    run_wsgi_app(application)

if __name__ == "__main__":
    main()

