#!/usr/bin/env python

#===============================================================================
# GWImporter
# 
# Pulls articles of the Gene Wiki from Wikipedia to the target wiki using the Mediawiki API.
# It uses urllib to query pages, and mwclient to handle the writeAPI at the target wiki.
# Runs on the target wiki.
#
# Nov 12, 2010; Salvatore Loguercio
#
#===============================================================================


import json
import urllib
import re
import datetime
import mwclient


# Function to generate a list of article names and their revision IDs; each entry looks like 'ArticleName~RevisionID'. RevisionIDs are eventually kept for later sync. 

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
        articleName = output['query']['pages'][x]['title']
        articleName = re.sub(" ","_",articleName)
        rev=output['query']['pages'][x]['revisions'][0]['revid']
        geneRevList.append(articleName+"~"+str(rev))
      queryContinue = "query-continue" in output.keys()
      if queryContinue:
        geicontinue = output['query-continue']['embeddedin']['geicontinue']
      else:
        break
     return geneRevList


# Generate the list

BigList=RevisionList()


# Get a list of article names only

ModList=[x.split("~")[0] for x in BigList]


# Initialize urllib and mwclient

log_obj=open('logfileBigImport','a')
succ=0
UrlRoot = "http://en.wikipedia.org/w/api.php"
UrlParams = {'action': 'query','prop': 'revisions','rvprop': 'content','format': 'json'}
try:
    site = mwclient.Site('127.0.0.1',path='/mediawiki/')
except IOError:
    log_obj.write("No connection")
    log_obj.write("\n")
    log_obj.close()


# login to the target wiki (insert an appropriate user/password)

site.login('<username>','<password>')


# Try to get content (MediaWiki markup, JSON encoded) for every page in the list, and write it to the target wiki

for articleName in ModList:
  UrlParams['titles'] = articleName.encode('utf-8')
  f = urllib.urlopen(UrlRoot,urllib.urlencode(UrlParams))
  z = f.read()
  try:
    output = json.loads(z)
  except ValueError:
    f = urllib.urlopen(UrlRoot,urllib.urlencode(UrlParams))
    z = f.read()
    output = json.loads(z)
    fail_obj=open('fail','a')
    fail_obj.write(str(datetime.datetime.today()))
    fail_obj.write(articleName+" - Timeout")
    fail_obj.write("\n")
    fail_obj.close()
    continue
  pages = output['query']['pages'].keys()
  content=output['query']['pages'][pages[0]]['revisions'][0]['*']
  page = site.Pages[articleName]
  print "working on "+articleName
  try:
    res=page.save(content,summary='MW_Import')     # Calls the writeAPI at the target wiki
  except ValueError:
    fail_obj=open('fail','a')
    fail_obj.write(str(datetime.datetime.today()))
    fail_obj.write(articleName+" - bigfile")
    fail_obj.write("\n")
    fail_obj.close()
    continue
  if res[u'result']==u'Success':
   succ=succ+1
  else:
    fail_obj=open('fail','a')
    fail_obj.write(str(datetime.datetime.today()))
    fail_obj.write(articleName+"-"+res[u'result'])
    fail_obj.write("\n")
    fail_obj.close()


# Write logfile

log_obj=open('logfileBigImport','a')
log_obj.write(str(succ)+" pages mirrored successfully.")
log_obj.write("\n")
log_obj.close()

# Save the list of articleNames~revisionIDs as a textfile called 'OldList' - for sync purposes.

NewList_str=json.dumps(BigList)
NewList_obj=open('OldList','w')
NewList_obj.write(NewList_str)
NewList_obj.close()



#===============================================================================
# TODO: Improve exception handling.
 
# Unhandled error sources (unicode encode/decode, http timeouts, max requests at wikipedia) might breaks down the read/write loop; it could be necessary to restart it manually from the last imported page.
#
# To keep track of the process, run from the python interpreter or, alternatively: python GWImporter.py > out 
# 
# To get the index of the last imported page:
#        'ind=ModList.index(articleName)' where 'articleName' is the name of the last successfully imported page (from the output log or the interpreter);
# then re-run the main loop using:
#        'for articleName in ModList[ind:]:'
#
#===============================================================================

