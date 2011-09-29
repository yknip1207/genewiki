#!/usr/bin/env python

#===============================================================================
# SWL_gene-snps_to_diseases.py
# 
# Adds semantic queries at the bottom of disease pages, showing associated genes and snps.
# Uses a list of diseases as input, which is the union of diseases discovered in Gene Wiki and SNPedia.
# Disease pages are fetched from the target or the source wiki. In case of redirects, the original page is fetched instead. 
#     
# Input required: disease_set (union of diseases in Gene Wiki and SNPedia, to be used as article names) 
#
# May 02, 2011; Salvatore Loguercio
#
#===============================================================================


import json
import urllib
import re
import mwclient 


# Import disease names from 'disease_set'

disease_set=[]
for line in open('disease_set','rb'):
    v = line.strip("\n")
    disease_set.append(v.decode('utf-8'))
    
# Turn full uppercases into capitalized:

disease_set2=[]
for item in disease_set:
    if len(re.findall('[A-Z]',item))==(len(item)-len(re.findall(' ',item))) and len(item)>3:
        disease_set2.append(item.capitalize())
        print item
    else:
        disease_set2.append(item)

        
# Initialize urllib, mwclient; login (user/password required)
        
UrlRoot = "http://en.wikipedia.org/w/api.php"
UrlParams = {'action': 'query', 'prop': 'revisions', 'rvprop': 'content', 'format': 'json'}
       
UrlTarget='http://127.0.0.1/mediawiki/api.php'
UrlTargetParams={'action': 'query','prop': 'revisions','rvprop': 'content','format': 'json'}
 
site = mwclient.Site('127.0.0.1',path='/mediawiki/')
site.login('<user>','<password>')


# Compile some regexp to determine where to place the gene-snp tables

g=re.compile('==.*Genetic.*==')
r=re.compile('==.*References.*==')


# Main read/writeAPI loop

errorsList=[]

for page in disease_set2:
    ann=[]
    articleName=re.sub(" ","_",page)
    UrlParams['titles'] = articleName
    f = urllib.urlopen(UrlTarget, urllib.urlencode(UrlParams))  # try to get the disease page from the target first
    z = f.read()
    output = json.loads(z)
    pages = output['query']['pages'].keys()
    if pages[0]==u'-1': # if the page is not there, try the source wiki (wikipedia)
        f = urllib.urlopen(UrlRoot, urllib.urlencode(UrlParams))
        z = f.read()
        output = json.loads(z)
        pages = output['query']['pages'].keys()        
        if pages[0]==u'-1': # if the page doesn't exist on WP, give up
            content=''
            print('0')
            
        else:   # otherwise get content from WP..
            content = output['query']['pages'][pages[0]]['revisions'][0]['*'].encode('utf-8')
            Urlnext=UrlRoot
            print('1')
    else:   # or from the target wiki
        content = output['query']['pages'][pages[0]]['revisions'][0]['*'].encode('utf-8')
        Urlnext=UrlTarget
        print('2')
        
    if content[0:9]=='#REDIRECT' or content[0:9]=='#Redirect':  # if the page exists but it's a redirect..
        
        
        # fixes a previous bug - semantic links erroneously added to redirect pages
        
        ann=re.findall(r'\[\[same-as::DOID:.*| \]\]',content)
        if len(ann)!=0:
            content=re.sub(r'\[\[same-as::DOID:.*| \]\]','',content)    
            
        
        # fixes redirects, and grabs the article name the redirects points to 
            
        if content.find('Wikipedia')!=-1:   
            if content.find('|')!=-1:
                contentfixed=re.sub(r'\[\[Wikipedia:(.*?)\|(.*?)\]\]',r'[[\1]]', content)
            else:
                contentfixed=re.sub(r'\[\[Wikipedia:(.*?)\]\]',r'[[\1]]', content)            
            fixedpage = site.Pages[articleName.decode('utf-8')]
            print "fix redirect in "+articleName
            res=fixedpage.save(contentfixed,summary='fix WP-redirect')   
            articleName=re.sub(" ","_",(re.findall(r'\[\[(.*?)\]\]',contentfixed)[0]))
        else:
            if content.find('|')!=-1:
                articleName=re.sub(" ","_",(re.findall(r'\[\[(.*?)\|.*\]\]',content)[0]))
            else:
                articleName=re.sub(" ","_",(re.findall(r'\[\[(.*?)\]\]',content)[0]))
                
        UrlParams['titles'] = articleName   # get content from the new article name
        f = urllib.urlopen(Urlnext, urllib.urlencode(UrlParams))
        z = f.read()
        output = json.loads(z)
        pages = output['query']['pages'].keys()
        if pages[0]==u'-1':
            print('Fix  '+articleName)
            errorsList.append(articleName)
            continue
        content = output['query']['pages'][pages[0]]['revisions'][0]['*'].encode('utf-8')
        
        
    # if the disease page has semantic links, preserve them    
        
    if len(ann)!=0:
        print('transferring DO annotation')
        content=content+'\n'+ann[0]
        
        
    # Finally, add the semantic query to the page content - either below 'Genetic' or above 'References' section.  

    if re.findall(g,content)!=[]:
        splitG=g.search(content).end()
        content=content[0:splitG]+"\n'''Related genes and SNPs'''\n{{#ask: [[Is-associated-with::{{FULLPAGENAME}}]] [[Category:Human proteins]] OR [[hasSNP:: <q> [[Is-associated-with::{{FULLPAGENAME}}]] </q>]]\n | limit = 10\n | format= template\n | template=Query_snp_disease_row\n | introtemplate=Query_snp_disease_header\n | outrotemplate=Query_snp_disease_footer\n}}\n"+content[splitG:]
    else:
        if re.findall(r,content)!=[]:
            splitR=r.search(content).start()
            content=content[0:splitR]+"'''Related genes and SNPs'''\n{{#ask: [[Is-associated-with::{{FULLPAGENAME}}]] [[Category:Human proteins]] OR [[hasSNP:: <q> [[Is-associated-with::{{FULLPAGENAME}}]] </q>]]\n | limit = 10\n | format= template\n | template=Query_snp_disease_row\n | introtemplate=Query_snp_disease_header\n | outrotemplate=Query_snp_disease_footer\n}}\n\n"+content[splitR:]
        else:
            content=content+"\n==Related genes and SNPs==\n{{#ask: [[Is-associated-with::{{FULLPAGENAME}}]] [[Category:Human proteins]] OR [[hasSNP:: <q> [[Is-associated-with::{{FULLPAGENAME}}]] </q>]]\n | limit = 10\n | format= template\n | template=Query_snp_disease_row\n | introtemplate=Query_snp_disease_header\n | outtrotemplate=Query_snp_disease_footer\n}}\n"
            
    diseasepage = site.Pages[articleName.decode('utf-8')]
    print "adding it to "+articleName+'        '+str(disease_set2.index(page))
    res2=diseasepage.save(content,summary='Adding gene-snp tables to diseases')
    

# TODO: Improve exception handling

