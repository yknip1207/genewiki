#!/usr/bin/env python

#===============================================================================
# SWL_diseases_to_snps.py
# 
# Adds semantic links to diseases (e.g.[[is-associated-with::xyz]]) in SNP pages. 
# Disease associations are identified separately by following wikilinks directed to/from an article in the SNPedia category 'medical condition'.
# Snp-disease links provided in 'snpedia_disease_final.csv' 
#    
# Input required: snpedia_disease_final.csv, snp_gene (gene-snp associations, details in SNPediaImporter.py) 
#
# Apr 28, 2011; Salvatore Loguercio
#
#===============================================================================

import json
import urllib
import re
import mwclient 
import csv
from itertools import groupby


# Load snp_gene, a dict of lists where each gene (key) is associated with a list of SNPs

a=open('snp_gene','r')
b=a.read()
gene_snps=json.loads(b)


# get all snps in a single list

snpList=[]

for item in gene_snps.values():
    
    for x in item:
        snpList.append(x)
    
        
# Extract a dictionary of disease-snp key-values from the input file

disease_snp=dict()
for line in open('snpedia_disease_final.csv','rb'):
    k, v = line.strip("\n").split("\t")
    disease_snp[k]=v.decode('utf-8')
    

# Make a dictionary of lists, with a list of associated diseases for every snp (key)  
    
snpdict=dict()
for snp in snpList:
    dislist=[]
    
    it = iter(sorted(disease_snp.iteritems()))
    for i in range(len(disease_snp)):  
        a=it.next()
        if a[1].find(snp+'|')!=-1:
            dislist.append(a[0])
    
    snpdict[snp]=dislist
    
   
# save it (file: 'snp_disease')
    
snpdict_str=json.dumps(snpdict)
snpdict_obj=open('snp_disease','w')
snpdict_obj.write(snpdict_str)
snpdict_obj.close()


# To load it:

# a=open('snp_disease','r')
# b=a.read()
# snpdict_old=json.loads(b)

# Initialize urllib, mwclient; login to the target wiki (username/password required)

UrlTarget='http://127.0.0.1/mediawiki/api.php'
UrlParams={'action': 'query','prop': 'revisions','rvprop': 'content','format': 'json'}
 
site = mwclient.Site('127.0.0.1',path='/mediawiki/')
site.login('<user>','<password>')


# WriteAPI loop

for snp in snpList:
    if len(snpdict[snp])!=0:
            print(snp)
            UrlParams['titles'] = snp   # get snp page content from the target wiki
            f = urllib.urlopen(UrlTarget, urllib.urlencode(UrlParams))
            z = f.read()
            output2 = json.loads(z)
            pages2 = output2['query']['pages'].keys()
            if pages2[0]!=u'-1':    # if the page exists
                content2 = output2['query']['pages'][pages2[0]]['revisions'][0]['*'].encode('utf-8')
            
                for j in range(len(snpdict[snp])):
                    sl='\n[[is-associated-with::'+snpdict[snp][j]+'| ]]'    # add hidden semantic links to diseases at the bottom of the page
                    content2 = content2+sl
                    print(snpdict[snp][j])
                
                    snppage=site.Pages[snp] # save edits
                    res=snppage.save(content2,summary='Adding diseases to snps')


# TODO: improve exception handling 
