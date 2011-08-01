#!/usr/bin/env python

#===============================================================================
# ImportAnnotatorTerms.py
# 
# Imports disease and GO terms identified with the NCBO Annotator, and already added as semantic links (SWL_diseases_to_genes.py).
# The output of the NCBO Annotator pipeline is provided as two text files: candidate_go_annotations.txt and candidate_do_annotations.txt.  
#
# Input required: candidate_go_annotations.txt, candidate_do_annotations.txt. 
#
# Mar 21, 2011; Salvatore Loguercio
#
#===============================================================================

import json
import urllib
import re
import mwclient 
import csv
from itertools import groupby


# Function to extract a subset of a dictionary

def sub_dict(somedict, somekeys, default=None) :
    return dict([ (k, somedict.get(k, default) ) for k in somekeys ] )


# Keys to select (after examining the column names in the textfiles)

sel_keys=('Target_accession','Target_preferred_term')


# Function to read the textfiles; a list of dictionaries is created

def csvRead(annot):
    csvRead = csv.DictReader(open(annot,'rb'),delimiter='\t')
    dict_list = []
    for line in csvRead:
        dict_list.append(sub_dict(line,sel_keys))
    
    return dict_list

    
# Apply to the textfiles; join the lists

dict_list=csvRead('candidate_do_annotations.txt')+csvRead('candidate_go_annotations.txt')

# remove duplicates from nested dictionaries in list

keyfunc = lambda d: (d['Target_accession'], d['Target_preferred_term'])
giter = groupby(sorted(dict_list, key=keyfunc), keyfunc)
L2 = [g[1].next() for g in giter]


# turn full uppercases in capitalized:

L3=[]
for item in L2:
    x=item['Target_preferred_term']
    if len(re.findall('[A-Z]',x))==(len(x)-len(re.findall(' ',x))) and len(x)>3:
        L3.append({'Target_accession':item['Target_accession'], 'Target_preferred_term':x.capitalize()})
        print item
    else:
        L3.append(item)

# 'FG syndrome' fixed manually: L3[260]['Target_preferred_term']='FG syndrome'


# Get a list of preferred items

pagenames=list(set([item['Target_preferred_term'] for item in L3]))


# remove duplicates

length_list=[]
for page in pagenames:
    length_list.append(len([item for item in L3 if item['Target_preferred_term']==page]))
    
dups=[] 
for i in range(len(pagenames)):
    if length_list[i]>1:
        dups.append(pagenames[i])

pagenames=list(set(pagenames).difference(set(dups)))


# Initialize urllib for queries at the source wiki (Wikipedia)  

UrlRoot = "http://en.wikipedia.org/w/api.php"
UrlParams = {'action': 'query', 'prop': 'revisions', 'rvprop': 'content', 'format': 'json'}


# initialize mwclient; login to the target wiki (requires valid user and password)

site = mwclient.Site('127.0.0.1',path='/mediawiki/')
site.login('<user>','<password>')


# Main query/writeAPI loop

for page in pagenames:
    id=[item['Target_accession'] for item in L3 if item['Target_preferred_term']==page][0]  # get GO/DO ID to insert in 'same-as' property
    articleName=re.sub(" ","_",page)    # get term page content from source
    UrlParams['titles'] = articleName
    f = urllib.urlopen(UrlRoot, urllib.urlencode(UrlParams))
    z = f.read()
    output = json.loads(z)
    pages = output['query']['pages'].keys()
    if pages[0]!=u'-1': # if the page exists
        contentSWL = output['query']['pages'][pages[0]]['revisions'][0]['*'].encode('utf-8')
        if contentSWL[0:9]=='#REDIRECT':    # if the page is a redirect, append the new page to the list of terms 
            newpage=re.findall(r'\[\[(.*?)\]\]',contentSWL)[0]
            L3.append({'Target_accession':id, 'Target_preferred_term':newpage})
            pagenames.append(newpage)
            print newpage+' added to the lists'
        else:
            allWL=re.findall(r'\[\[(?!Image:|image:|File:|Category:|media:|Special:)(.*?)\]\]',contentSWL)  # find all wikilinks
            for item in allWL:  # replace wikilinks with interwiki links pointing to Wikipedia
                if item.find('|')!=-1:
                    contentSWL = contentSWL.replace('[['+item+']]','[[Wikipedia:'+item+']]')
                else:
                    contentSWL = contentSWL.replace('[['+item+']]','[[Wikipedia:'+item+'|]]')   # pipe trick
            
            
            # Insert semantic link 'same-as::id' at the bottom of the page, hidden
              
            sl='\n[[same-as::'+id+'| ]]'
            contentSWL = contentSWL+sl
        
        
    else:   # if the page doesn't exist, create one with the semantic link only
        contentSWL = '[[same-as::'+id+'|Same as '+id+']]'
    
    
    # save page 
      
    page = site.Pages[articleName.decode('utf-8')]
    print "working on "+articleName
    res=page.save(contentSWL,summary='Import_NCBO_Concepts')
            