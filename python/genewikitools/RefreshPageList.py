from django.utils import simplejson as json
import urllib
import sys
from google.appengine.ext import db
import datetime

"""
Refresh the page list of Gene Wiki pages and store result in data store

Created: 20091211 AS

Known issues:
	- doesn't handle special characters

"""

class PageList(db.Model):
    date = db.DateTimeProperty(auto_now_add=True)
    pages = db.TextProperty()

def refresh_page_list():
    apiUrl = "http://en.wikipedia.org/w/api.php"
    templateName = 'Template:GNF_Protein_box'
    #templateName = 'Template:GNF_Ortholog_box'
    
    params = {'action': 'query',
              'list': 'embeddedin',
              'eititle': templateName,
              'einamespace': 0,
              'eifilterredir': 'nonredirects',
              'eilimit': 1000,
              'format': 'json',
              }
    eicontinue = ''
    geneWikiList = []
    while True:
        if eicontinue != '':
            params['eicontinue'] = eicontinue
    
    #    print "Querying... (" + str(len(geneWikiList)) + ")"
        UrlParams = urllib.urlencode(params)
        f = urllib.urlopen(apiUrl,UrlParams)
        output = json.loads(f.read())
        #print json.dumps(output, sort_keys=True, indent=4)
        titleList = [x['title'].encode('utf-8') for x in output['query']['embeddedin']]
        geneWikiList.extend(titleList)
    
        queryContinue = "query-continue" in output.keys()
        if queryContinue:
            eicontinue = output['query-continue']['embeddedin']['eicontinue']
        else:
            break
    
    return json.dumps({
	    "Count": len(geneWikiList), 
	    "Date": str(datetime.datetime.today()),
	    "PageList": geneWikiList
	    }, indent=4)


#### MAIN ####

print 'Content-Type: text/plain'
print ''
print 'Executing...  please wait...'
sys.stdout.flush()

# current record more than one day old
pl = PageList()
pl.pages = refresh_page_list()
pl.put()
print "Done refreshing"

