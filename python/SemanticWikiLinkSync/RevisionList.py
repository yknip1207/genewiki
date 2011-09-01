######################
#
#
# RevisionList.py
#
#
# Function to retrieve a list of GeneWiki page titles, together with their RevisionIDs.
# 
#
# The pages linking to a specific MW template will be retrieved.
# Output format: ["PageName"~"RevisionID"].
#
#
# 16/4/2010
# 
# Author: S.Loguercio
#
#
######################




import simplejson as json
import urllib
import re



def RevisionList():
     apiUrl = "http://en.wikipedia.org/w/api.php"
     templateName = 'Template:GNF_Protein_box'
     params = {'action': 'query',
               'generator': 'embeddedin',
               'geititle': templateName,
               'geinamespace': 0,
               'geifilterredir': 'nonredirects',
               'geilimit': 500,
               'prop': 'revisions',
               'rvprop': 'ids',
               'format': 'json',
               }
     geicontinue=''
     geneRevList=[]
     while True:
      if geicontinue != '':
        params['geicontinue'] = geicontinue
      UrlParams = urllib.urlencode(params)
      f = urllib.urlopen(apiUrl,UrlParams)
      output = json.loads(f.read())
      pages=output['query']['pages'].keys()
      for x in pages:
        articleName = output['query']['pages'][x]['title'].encode('utf-8')
        articleName = re.sub(" ","_",articleName)
        rev=output['query']['pages'][x]['revisions'][0]['revid']
        geneRevList.append(articleName+"~"+str(rev))
      queryContinue = "query-continue" in output.keys()
      if queryContinue:
        geicontinue = output['query-continue']['embeddedin']['geicontinue']
      else:
        break
     return geneRevList
	
