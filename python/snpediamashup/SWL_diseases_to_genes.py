#!/usr/bin/env python

#===============================================================================
# SWL_diseases_to_genes.py
# 
# Adds semantic links to Gene Ontology and disease associations (e.g.[[is-associated-with::xyz]]) in GeneWiki pages. 
# Disease and GO associations are identified in the text of Gene Wiki articles using the NCBO Annotator.
# The output of the NCBO Annotator pipeline is provided here as two textfiles: candidate_go_annotations.txt and candidate_do_annotations.txt.
# Also, converts SWL templates in SMW semantic properties.   
#
# Input required: candidate_go_annotations.txt, candidate_do_annotations.txt. 
#
# Mar 10, 2011; Salvatore Loguercio
#
#===============================================================================


import json
import urllib
import re
import mwclient 
import csv


# Function to convert SWL templates in SMW links 

def rewrite(input):
    output = re.sub(r'\{\{SWL\|target=(.*?)\|label=(.*?)\|type=(.*?)\}\}',
                    r'[[\3::\1|\2]]', input)
    output2 = re.sub(r'\{\{SWL\|target=(.*?)\|type=(.*?)\}\}',
                        r'[[\2::\1]]',
                        output)
    return output2


# Function to extract a subset of a dictionary

def sub_dict(somedict, somekeys, default=None) :
    return dict([ (k, somedict.get(k, default) ) for k in somekeys ] )


# Keys to select (after examining the column names in the textfiles)

sel_keys=('Source_wiki_page_title','Target_accession','Target_preferred_term')


# Function to read the textfiles; a list of dictionaries is created

def csvRead(annot):
    csvRead = csv.DictReader(open(annot,'rb'),delimiter='\t')
    dict_list = []
    for line in csvRead:
        dict_list.append(sub_dict(line,sel_keys))
    
    return dict_list
    

# Apply to the textfiles; join the lists
   
dict_list=csvRead('candidate_do_annotations.txt')+csvRead('candidate_go_annotations.txt')


# Get unique Gene Wiki titles using 'set' 

pagenames=list(set([item['Source_wiki_page_title'] for item in dict_list]))


# error logfile

log_obj = open('logfileSWL', 'a')


# Initialize urllib for queries at the source wiki (Wikipedia) 

UrlRoot = "http://en.wikipedia.org/w/api.php"
UrlParams = {'action': 'query', 'prop': 'revisions', 'rvprop': 'content', 'format': 'json'}


# initialize mwclient; login to the target wiki (requires valid user and password)

site = mwclient.Site('127.0.0.1',path='/mediawiki/')
site.login('<user>','<password>')


# Loop through the gene titles: get content from the source - add semantic links - save the page  

for page in pagenames:
    
    page_list=[item for item in dict_list if item['Source_wiki_page_title']==page]   # get a list of go/do associations for that gene
    
    articleName=re.sub(" ","_",page)    # get Gene wiki page content
    UrlParams['titles'] = articleName
    f = urllib.urlopen(UrlRoot, urllib.urlencode(UrlParams))
    z = f.read()
    output = json.loads(z)
    pages = output['query']['pages'].keys()
    contentSWL = output['query']['pages'][pages[0]]['revisions'][0]['*'].encode('utf-8')
    
    
    for item in page_list:
        
        pref=item['Target_preferred_term']  # insert preferred terms
  
        # If the preferred term is already in the page, replace it with a semantic link; otherwise add it at the bottom of the page (invisible).
        
        if contentSWL.find('[['+pref+']]')!=-1:
            sl='[[is-associated-with::'+pref+']]'
            contentSWL = contentSWL.replace('[['+pref+']]',sl)
        else:
            sl='\n[[is-associated-with::'+pref+'| ]]'
            contentSWL = contentSWL+sl
            
        
    # Eventually, replace SWL templates with SMW properties
    
    if contentSWL.find('{{SWL|')!=-1:
        contentSWL=rewrite(contentSWL)


    # Save changes to the page

    page = site.Pages[articleName]
    print "working on "+articleName
    res=page.save(contentSWL,summary='SWLseeding')
    

