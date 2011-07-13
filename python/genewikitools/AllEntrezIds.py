#!/usr/bin/env python

# Finds all the Entrez Ids in the Gene Wiki. Just run this
# and pipe its output to a file (recommended), then do with it what
# you like.

# Requires wikitools module.
# http://code.google.com/p/python-wikitools/

# Erik Clarke, 2011

from wikitools import *

site = wiki.Wiki("http://en.wikipedia.org/w/api.php")
params = {'action':'query',
          'list': 'embeddedin',
          'eititle':'Template:GNF_Protein_box',
          'eilimit':'12000',
          'einamespace':'10'}   # The 'Template' namespace, finds
                                # the title only in Template: areas
                                # (such as Template:PBB). 
request = api.APIRequest(site, params)
results = request.query(querycontinue=False)

# Should be slightly more than 10,000... like 10,362 as of 7/13/11

# print("Number of results found: %d" % len(results[u'query'][u'embeddedin']))
# print('')

for result in results[u'query'][u'embeddedin']:
    for key in result:
        if key == 'title':
            print(result[key].lstrip('Template:PBB/'))
            # Add additional processing here if needed