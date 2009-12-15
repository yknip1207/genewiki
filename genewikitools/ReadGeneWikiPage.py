from django.utils import simplejson as json
import urllib
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app

class GeneWikiPage( webapp.RequestHandler):
    def __init__(self):
	self.UrlRoot = "http://en.wikipedia.org/w/api.php"
	self.UrlParams = {
			'action': 'query',
			'prop': 'revisions',
			'rvprop': 'content',
			'format': 'json'
			}

    def get(self):
	self.response.headers['Content-type'] = 'text/plain'
	articleName = self.request.get('article')
	content = self._get_content(articleName)
#	self.response.out.write( "Content: " + content + "\n" )

    def _get_content( self, articleName ):
	self.UrlParams['titles'] = articleName
	f = urllib.urlopen( self.UrlRoot, urllib.urlencode(self.UrlParams) )
	z = f.read()
	output = json.loads(z)
        pages = output['query']['pages'].keys()
	content = output['query']['pages'][pages[0]]['revisions'][0]['*'].encode('utf-8')
#	self.response.out.write(json.dumps(output, indent=4))
	self.response.out.write(content)

###
### MAIN
###

application = webapp.WSGIApplication(
		[('.*', GeneWikiPage)],
		debug=True)

def main():
    run_wsgi_app(application)

if __name__ == "__main__":
    main()
