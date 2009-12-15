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
#	print "Content-type: text/plain"
#	print ''
        self.response.headers['Content-type'] = 'text/plain'

#        pmidList = (16492761, 18464898)
	pmids = self.request.get('pmids')
        
	outputHash = self.getMesh(pmids)
        
        self.response.out.write( json.dumps( outputHash, indent = 4))

    def getMesh( self, pmids ):
        urlparams = {
            'db': 'pubmed',
            'retmode': 'xml',
            'id': pmids,
	    'email': 'asu@gnf.org'
            }
        eutilsURL = 'http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi'
        
        f = urllib.urlopen( eutilsURL, urllib.urlencode(urlparams) )
        dom = minidom.parse(f)
#	print 'DOM'
#	print dom.toprettyxml()
        
	outputHash = {}
	pubmedList = dom.getElementsByTagName('MedlineCitation')
#        self.response.out.write("P: "+str(pubmedList.length))
	for pubmedEntry in pubmedList:
            ### get PMID
	    pmid = pubmedEntry.getElementsByTagName('PMID')[0].childNodes[0].data
#	    print "P: ", pmid
#	    self.response.out.write("P: "+str(p.length))
#	    self.response.out.write(p)
#	    for z in p.childNodes:
#		pmid = z.data
#	    self.response.out.write("PMID: "+pmid)

	    ### get MeshList
            nodeList = pubmedEntry.getElementsByTagName('MeshHeading')
        #    print "Length: ", nodeList.length
            MeshList = []
            for node in nodeList:
                for n in node.getElementsByTagName('DescriptorName'):
                    for n2 in n.childNodes:
        #                print "D: ", n2.data
                        MeshList.append(n2.data)

            outputHash[pmid] = MeshList

        return( outputHash )

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

