#!/usr/bin/env python
# -*- coding: latin-1 -*-
print "Content-type: text/html"
print

'''
Created on Aug 30, 2011

@author: eclarke
'''

import cgi
import cgitb
import subprocess
import json
from urllib2 import urlopen
cgitb.enable()

# Method definitions
        
''' 
Checks if given title exists.
!important: sanitize input before passing to this method!
Unsanitized strings create shell injection vulnerabilities!
'''
def checkTitle(title):
    result = subprocess.check_output(["java", "-jar", "/Users/eclarke/jars/ProteinBoxBot.jar", "--check ", title])
    result = True if result=="1" else False #Converts output into boolean
    return result
    
def checkId(id):
    result = urlopen("http://mygene.info/gene/"+id)
    return not "404: Not Found" in result   # should return True if page exists
    
    
'''
Returns the symbol for the given id.
'''
def getSymbolFromId(id):
    url = urlopen("http://mygene.info/gene/"+id+"?filter=symbol")
    symjson = json.loads(url.read())
    try:
        symbol = symjson[u'symbol'].encode()
    except:
        # Couldn't retrieve symbol for id
        symbol = None
    return symbol

# Global variable definitions
form = cgi.FieldStorage()
id = -1
templateTitle = ""
templateExists = False
symbol = ""
stubTitle = ""
stubExists = False 


# Sanitation and existence checks
if "id" in form:
    putative_id = form["id"].value
    # Note: non-numeric values for id fail this test and the global id variable remains -1, 
    # titles remain blank, and nothing is checked for existence
    if putative_id.isdigit():
        id = putative_id
        templateTitle = "Template:PBB/"+putative_id
        templateExists = checkTitle(templateTitle)  # this is sort of slow
        symbol = getSymbolFromId(id)
        stubTitle = symbol
        stubExists = checkTitle(symbol)
        if not stubExists:
            if checkTitle(symbol+" (gene)"): # sometimes genes have a (gene) suffix for disambiguation
                stubTitle = symbol+" (gene)"
                stubExists = True
        
    
# Output

print("<!DOCTYPE html>")
print("<html><head>")
print("<link rel='stylesheet' type='text/css' media='screen' href='http://xanthus.scripps.edu/style.css'>")

print("</head><body>")

print("<title>GeneWiki Code Generator</title>")
print("<h1>Template<br /></h1>")
print("<h2>Status: <span class"+"'exist'> exists" if templateExists else "'nonexist'>does not exist"+"</span></h2>")


print("</body></html>") 

