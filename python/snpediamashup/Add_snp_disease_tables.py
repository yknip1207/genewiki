#!/usr/bin/env python

#===============================================================================
# Add_snp_disease_tables.py
# 
# Adds collapsible tables with SNPs and associated diseases to the gene pages, under the 'Known Variants' section. 
# If # of SNPs is smaller than 10 the table is expanded, otherwise collapsed.
# Replaces the simple lists of snps previously written with SNPediaImporter.py.
# 
# Input required: snp_gene (gene-snp associations, details in SNPediaImporter.py)
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


# Initialize urllib, mwclient; login to the target (user/password required)

UrlTarget='http://127.0.0.1/mediawiki/api.php'
UrlTargetParams={'action': 'query','prop': 'revisions','rvprop': 'content','format': 'json'}
 
site = mwclient.Site('127.0.0.1',path='/mediawiki/')
site.login('<user>','<password>')


# Load snp_gene, a dict of lists where each gene (key) is associated with a list of SNPs

a=open('snp_gene','r')
b=a.read()
gene_snps=json.loads(b)


# Get a list of gene pages to be edited out of gene_snps

genesList=[]
[genesList.append(item) for item in gene_snps.keys()]


# WriteAPI loop

p=re.compile('==Known variants==')
q=re.compile('==.*References.*==')

outlist=[]
for gene in genesList:
    
    articleName=re.sub(" ","_",gene)
    UrlTargetParams['titles'] = articleName.encode('utf-8')
    g = urllib.urlopen(UrlTarget, urllib.urlencode(UrlTargetParams))
    zz=g.read()
    output=json.loads(zz)
    pages = output['query']['pages'].keys()
    if pages[0]!=u'-1':
        content = output['query']['pages'][pages[0]]['revisions'][0]['*'].encode('utf-8')
        if re.findall(p,content)!=[]:
            if len(gene_snps[gene])<=10:
                hd='{| class="wikitable collapsible"\n|-\n! SNPs\n! Related phenotypes\n! Reference\n|-\n'
            else:
                hd='{| class="wikitable collapsible collapsed"\n|-\n! SNPs\n! Related phenotypes\n! Reference\n|-\n'
        
            content2=content[0:p.search(content).start()]+content[q.search(content).start():]   # remove previously written content between 'Known variants' and 'References'.
            splitP=q.search(content2).start()
            SNP=''
            for snp in gene_snps[gene]:
                SNP=SNP+'| [[hasSNP::'+snp+']]\n| {{#show: '+snp+' | ?Is-associated-with}}\n| [http://www.snpedia.com/index.php/'+snp+']\n|-\n'
            content3=content2[0:splitP]+'==Known variants==\n'+hd+SNP.encode('utf-8')+'|}\n\n'+content2[splitP:]
             
            genepage = site.Pages[articleName]
            print "adding it to "+articleName+'        '+str(genesList.index(gene))
            res2=genepage.save(content3,summary='SNPedia mashup - adding snp-disease tables')
        else:
            print "No References!"
            outlist.append(articleName)

    
# save 'outlist' - list of gene pages missing the 'References' section and not edited
          
a=json.dumps(outlist)
a_obj=open('outlist','w')
a_obj.write(a)
a_obj.close()

# TODO: improve exception handling in the writeAPI loop 
