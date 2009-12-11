from django.utils import simplejson as json
import urllib
import sys

"""
List the title of all pages in the Gene Wiki.

Created: 20091208 AS

Known issues:
	- doesn't handle special characters
"""

print 'Content-Type: application/json'
print ''

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

print json.dumps({"Count": len(geneWikiList), "PageList": geneWikiList}, indent=4)
