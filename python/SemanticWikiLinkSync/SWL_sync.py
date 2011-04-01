#import simplejson as json
import json
import urllib
import re
import datetime
import mwclient 

# the rewrite rules for mapping from wikipedia to semantic media wiki
#TODO perhaps we could replace the redundancy here with some kind of optional match?
#TODO this is currently order dependent, but the template on wikipedia seems not to be.  If you swap label with type accidentally on the wikipedia page this will break (you get a confusing json error on when tryin got insert the page into SMW)
def rewrite(input):
    output = re.sub(r'\{\{SWL\|target=(.*?)\|label=(.*?)\|type=(.*?)\}\}',
                    r'[[\3::\1|\2]]', input)
    output2 = re.sub(r'\{\{SWL\|target=(.*?)\|type=(.*?)\}\}',
                        r'[[\2::\1]]',
                        output)
    return output2

# RevisionListSWL generates a list of pages that link to the SWL template, together with their most recent Revision IDs. Every entry has the following format: pageName~RevisionID.

def RevisionListSWL():
    apiUrl = "http://en.wikipedia.org/w/api.php"
    templateName = 'Template:SWL'
    params = {'action': 'query',
               'generator': 'embeddedin',
               'geititle': templateName,
               'geinamespace': 0,
               'geifilterredir': 'nonredirects',
               'geilimit': 100,
               'prop': 'revisions',
               'rvprop': 'ids',
               'format': 'json',
               }
    geneRevList=[]
    UrlParams = urllib.urlencode(params)
    f = urllib.urlopen(apiUrl,UrlParams)
    output = json.loads(f.read())
    pages=output['query']['pages'].keys()
    for x in pages:
        articleName = output['query']['pages'][x]['title'].encode('utf-8')
        articleName = re.sub(" ","_",articleName)
        rev=output['query']['pages'][x]['revisions'][0]['revid']
        geneRevList.append(articleName+"~"+str(rev))
        print articleName
    return geneRevList


# Generate the list

SWL=RevisionListSWL()

# Get a list of pagenames only

ModList=[x.split("~")[0].encode('utf-8') for x in SWL]


# Get content (MW markup, JSON encoded) for every page in the list
# Use mwclient to handle the writeAPI in Mediawiki


if len(ModList) != 0:
    log_obj = open('logfileSWL', 'a')
    succ = 0
    UrlRoot = "http://en.wikipedia.org/w/api.php"
    UrlParams = {'action': 'query', 'prop': 'revisions', 'rvprop': 'content', 'format': 'json'}
    try:
        site = mwclient.Site('www.wikidraft.org')
    except IOError:
        log_obj.write("No connection")
        log_obj.write("\n")
        log_obj.close()
    site.login('smw site user','smw site pass')
    for articleName in ModList:
        UrlParams['titles'] = articleName
        f = urllib.urlopen(UrlRoot, urllib.urlencode(UrlParams))
        z = f.read()
        output = json.loads(z)
        pages = output['query']['pages'].keys()
        contentSWL = output['query']['pages'][pages[0]]['revisions'][0]['*'].encode('utf-8')

#   contentSWL is Wiki page content, JSON encoded (a single text string).
#   SWL to SMW syntax conversion (in general, any text manipulation). 

        contentSMW = rewrite(contentSWL);
     #   contentSMW = re.sub(r'\{\{SWL\|target=(.*?)\|type=(.*?)\}\}',
     #                    r'[[\2::\1]]',
     #                    contentSWL)
    
        page = site.Pages[articleName]
    #    try:
        print "working on "+articleName
        res=page.save(contentSMW,summary='MWSync')     # WriteAPI at work
    #    except Exception:


# The remaining are just logfiles, not important.

        if res[u'result']==u'Success':
            succ=succ+1
        else:
            fail_obj = open('fail', 'a')
            fail_obj.write(str(datetime.datetime.today()))
            fail_obj.write(articleName + "-" + res[u'result'])
            fail_obj.write("\n")
            fail_obj.close()
    log_obj.write(str(succ) + " pages mirrored successfully.")
    log_obj.write("\n")
    log_obj.close()
    NewList_str = json.dumps(SWL)
    NewList_obj = open('OldListSWL', 'w')
    NewList_obj.write(NewList_str)
    NewList_obj.close()
else:
    log_obj.write("No changes.")
    log_obj.write("\n")
    log_obj.close()


