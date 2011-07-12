#!/usr/bin/env python

#===============================================================================
# SNPediaImporter
# 
# Pull SNP pages from SNPedia that are connected to genes in Gene Wiki. Semantic links pointing to the SNP page(s) (property 'has_SNP') are added to each gene page in a section called 'Known Variants'). 
# Urllib is used to query content, and mwclient to create/edit pages at the target wiki.
#
# Input required: gene_symbol_wiki.txt (a mapping between gene symbols used in SNPedia and GeneWiki titles) 
#
# Mar 23, 2011; Salvatore Loguercio
#
#===============================================================================

import json
import urllib
import re
import mwclient


# get a list of SNP pages from SNPedia: currently all pages that include the template 'Rsnum'

def SNPList():
    apiUrl = "http://www.snpedia.com/api.php"
    templateName = 'Template:Rsnum'
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
    RevList=[]
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
            RevList.append(articleName)
        queryContinue = "query-continue" in output.keys()
        if queryContinue:
            geicontinue = output['query-continue']['embeddedin']['geicontinue']
        else:
            break
    return RevList


# Load Gene Symbol to Gene Wiki mappings from an external file, as a dictionary:

GeneName2GeneWiki=dict()
for line in open('gene_symbol_wiki.txt','rb'):
    _, k, v = line.strip("\n").split("\t")
    GeneName2GeneWiki[k]=v.decode('utf-8')


# Generate the list
 
SNPlist=SNPList()


# Initialize urllib for querying page content

UrlRoot = "http://www.snpedia.com/api.php"
UrlParams = {'action': 'query','prop': 'revisions','rvprop': 'content','format': 'json'}


# Initialize gene_snps - a dict of lists. In gene_snps each gene (key) is associated with a list of SNPs

from collections import defaultdict
gene_snps = defaultdict(list)


# Fill in the masterlist

for snp in SNPlist:
    
    UrlParams['titles'] = snp   # get snp page content
    f = urllib.urlopen(UrlRoot, urllib.urlencode(UrlParams))
    z = f.read()
    output = json.loads(z)
    pages = output['query']['pages'].keys()
    content = output['query']['pages'][pages[0]]['revisions'][0]['*'].encode('utf-8')
    print(snp)
    if content.find('Gene')!=-1:    # find a gene associated with the snp
        a=re.findall(r'\|Gene(.*?)\n',content)
        if len(a)!=0:
            a=a[0].replace(' ','')
            GeneName=a.replace('=','')
            if GeneName in GeneName2GeneWiki:   # only snps with genes in GeneWiki
                gene_snps[GeneName2GeneWiki[GeneName]].append(snp)
            
            
# Save the list to a txtfile called 'snp_gene'

snpgene_str=json.dumps(gene_snps)
snpgene_obj=open('snp_gene','w')
snpgene_obj.write(snpgene_str)
snpgene_obj.close()

# To load the txtfile:
# a=open('snp_gene','r')
# b=a.read()
# gene_snps=json.loads(b)


# Initialize urllib to query content at the local wiki 

UrlTarget='http://127.0.0.1/mediawiki/api.php'
UrlTargetParams={'action': 'query','prop': 'revisions','rvprop': 'content','format': 'json'}


# Initialize mwclient; login to the local wiki (insert a valid user/password)

site = mwclient.Site('127.0.0.1',path='/mediawiki/')
site.login('<user>','<password>')


# List of gene pages out of the masterlist (gene_snps) 

genesList=[]
[genesList.append(item) for item in gene_snps.keys()]



# Loop through the gene list; get page content; if the page has a 'References' section, insert a section called 'Known Variants' right before it.
# Fill the section with associated SNPs, insert semantic links (e.g. '[[hasSNP::xyz]]') and import the corresponding SNP pages.
# If the # of snps per gene exceeds 10, they are inserted under 'Known variants' as a collapsible list.


p=re.compile('==.*References.*==')

outlist=[]

for gene in genesList:
    
    articleName=re.sub(" ","_",gene)    # get gene page content
    UrlTargetParams['titles'] = articleName.encode('utf-8')
    g = urllib.urlopen(UrlTarget, urllib.urlencode(UrlTargetParams))
    zz=g.read()
    output=json.loads(zz)
    pages = output['query']['pages'].keys()
    if pages[0]!=u'-1':
        content = output['query']['pages'][pages[0]]['revisions'][0]['*'].encode('utf-8')
        if re.findall(p,content)!=[]:   # search for the 'References' section
            
            splitP=p.search(content).start()
            SNP=''
            if len(gene_snps[gene])<=10:
                for snp in gene_snps[gene]: # loop through the list of associated snps
                    
                    UrlParams['titles'] = snp   # get snp page content
                    f = urllib.urlopen(UrlRoot, urllib.urlencode(UrlParams))
                    z = f.read()
                    output2 = json.loads(z)
                    pages2 = output2['query']['pages'].keys()
                    content2 = output2['query']['pages'][pages2[0]]['revisions'][0]['*'].encode('utf-8')
                    print(snp)
                    snppage=site.Pages[snp]
                    res=snppage.save(content2,summary='SNPedia import') # import snp page onto the target
                    SNP=SNP+'[[hasSNP::'+snp+']]\nSee [http://www.snpedia.com/index.php/'+snp+']\n\n' # assemble semantic links
                content=content[0:splitP]+'==Known variants==\n'+SNP.encode('utf-8')+content[splitP:]   # insert the list of snps (and semantic links) under 'Known variants'
            else:   # same as before, but if # of snp is exceeds 10 use a collapsible list instead
                for snp in gene_snps[gene]:
                    UrlParams['titles'] = snp
                    f = urllib.urlopen(UrlRoot, urllib.urlencode(UrlParams))
                    z = f.read()
                    output2 = json.loads(z)
                    pages2 = output2['query']['pages'].keys()
                    content2 = output2['query']['pages'][pages2[0]]['revisions'][0]['*'].encode('utf-8')
                    print(snp)
                    snppage=site.Pages[snp]
                    res=snppage.save(content2,summary='SNPedia import')
                    SNP=SNP+'|[[hasSNP::'+snp+']]\nSee [http://www.snpedia.com/index.php/'+snp+']'
                content=content[0:splitP]+'==Known variants==\n{{Collapsible list|title=List of known variants'+SNP.encode('utf-8')+'}}\n\n'+content[splitP:]
             
            genepage = site.Pages[articleName]  # Finally, rewrite the gene page with all snps added
            print "adding it to "+articleName+'        '+str(genesList.index(gene))
            res2=genepage.save(content,summary='SNPedia mashup')
        else:
            print "No References!"
            outlist.append(articleName) # list of gene pages missing the 'References' section and not edited


#===============================================================================
# TODO: Improve exception handling.
 
# In case of unknown errors, the loop above breaks down and has to be restarted manually from the last imported page.
# To keep track of the process, run from the python interpreter or, alternatively: python SNPediaImporter.py > out 
# 
# To get the index of the last imported page:
#        'ind=geneslist.index(gene)' where 'articleName' is the name of the last successfully imported page (from the output log or the interpreter);
# then re-run the main loop using:
#        'for gene in genesList[ind:]:'
#
#===============================================================================