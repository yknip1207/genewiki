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
from urllib2 import HTTPError, urlopen

cgitb.enable()

# Method definitions
        
''' 
Checks if given title exists.
!important: sanitize input before passing to this method!
Unsanitized strings create shell injection vulnerabilities!
'''
def checkTitle(title):
    result = subprocess.check_output(["java", "-jar", "/Users/eclarke/jars/ProteinBoxBot.jar", "--check", title])
    result.strip()
    result = True if 'true' in result else False #Converts output into boolean
    return result

'''
Checks if the given ID is a valid entrez id in mygene.info
'''    
def checkId(entrez):
    try:
        urlopen("http://mygene.info/gene/"+entrez)
        return True
    except HTTPError:
        return False 
    
'''
Returns the symbol for the given entrez.
'''
def getSymbolFromId(entrez):
    url = urlopen("http://mygene.info/gene/"+entrez+"?filter=symbol")
    symjson = json.loads(url.read())
    try:
        symbol = symjson[u'symbol'].encode()
    except:
        # Couldn't retrieve symbol for id
        symbol = None
    return symbol

'''
Creates update button
'''
def drawButton(text):
    print("<button value='"+text+"' type='submit'>"+text+"</button>")
    
# Global variable definitions
form = cgi.FieldStorage()
entrez = -1
templateTitle = ""
templateExists = False
symbol = ""
stubTitle = ""
stubExists = False 

# Sanitation and existence checks
if "id" in form:
    putative_id = form["id"].value
    # Note: non-numeric values for "id" fail this test and the global entrez variable remains -1, 
    # titles remain blank, and nothing is checked for existence
    if putative_id.isdigit():
        entrez = putative_id
        templateTitle = "Template:PBB/"+putative_id
        templateExists = checkTitle(templateTitle)  # this is sort of slow
        if checkId(entrez):
            symbol = getSymbolFromId(entrez)
            stubTitle = symbol
            stubExists = checkTitle(symbol)
                
        if not stubExists and templateExists:
            if checkTitle(symbol+" (gene)"): # sometimes genes have a (gene) suffix for disambiguation
                stubTitle = symbol+" (gene)"
                stubExists = True
        
    
# Output
print('''
    <!DOCTYPE html>
    <html><head>
    <link rel='stylesheet' type='text/css' media='screen' href='http://xanthus.scripps.edu/style.css'>
    </head><body>
        <title>GeneWiki Code Generator</title>
        <h2>Template<br /></h2>
''')
templateStatus = "'exist'> exists" if templateExists else "'nonexist'>does not exist"
print("<h3>Status: <span class="+templateStatus+"</span></h3>")
if templateExists:
    print("Found at : <a href='http://en.wikipedia.org/wiki/"+templateTitle+"'>"+templateTitle+"</a>")
    drawButton("update")
    drawButton("show code")
else:
    drawButton("create")
print("<hr />")
print("<h2>Stub<br /></h2>")
stubStatus = "'exist'> exists" if stubExists else "'nonexist'>does not exist"
print("<h3>Status: <span class="+stubStatus+"</span></h3>")
if stubExists:
    print("Found at : <a href='http://en.wikipedia.org/wiki/"+stubTitle+"'>"+stubTitle+"</a>")
    drawButton("edit")
else:
    print('''
        <form method="post" action="GeneWikiGenerator.py">
         <p>Select a title: <input type="text" name="message" /></p>
         <input type="submit" value="Submit" />
        </form>
    ''')
print("</body></html>") 



