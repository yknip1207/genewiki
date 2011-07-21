'''
Created on May 11, 2010

@author: bgood
'''
import urllib
import mwclient

#takes a list of page names separated by new lines and returns SMW RDF version of those pages
def getWikiRDF(pages):
    UrlRoot = "http://wikidraft.referata.com/wiki/Special:ExportRDF"
    UrlParams = {'postform': '1', 'pages': pages, 'backlinks': '1'}
    f = urllib.urlopen(UrlRoot, urllib.urlencode(UrlParams))
    z = f.read()
    return z

#gets all the RDF for the whole site 
def getAllAsRDF():
    try:
        site = mwclient.Site('www.wikidraft.org')
    except IOError:
        print("No connection")
    site.login('i9606', 'i9606wiki') 
    pages = "";   
    for page in site.Pages:
        pages = pages+page.name+"\n"
    return(getWikiRDF(pages))
            
if __name__ == '__main__':
    #rdf = getWikiRDF('Cyclin_B1\nKIFAP3');   
    rdf = getAllAsRDF()
    rdf_out = open('rdfExportSMGeneWiki.rdf', 'w')
    rdf_out.write(rdf)
    rdf_out.close()
 