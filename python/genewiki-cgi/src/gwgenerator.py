#!/usr/bin/env python
# -*- coding: latin-1 -*-
print "Content-type: text/html \n"


'''
Created on Aug 30, 2011
Purpose:  CGI front-end for Gene Wiki code generation utility (specified in the pbb variable).
Usage:    Ensure that the global pbb variable points to the correct jarfile, and that the jarfile
          has all the necessary resources (usually a copy of the latest gene2pubmed file from NCBI,
          a sqlite database mapping entrez ids to titles, etc- all of which should be able to be 
          generated using the jarfile's bootstrap functions, if available.)
@author: eclarke@scripps.edu
'''

import cgi
import cgitb
import subprocess
from subprocess import CalledProcessError
from time import gmtime, strftime
import json
import textwrap

from urllib2 import HTTPError, urlopen


cgitb.enable()
pbb = "/Users/eclarke/jars/GeneWikiGenerator.jar"
    

def log(message):
    '''
    Logs a message to the logfile.
    '''
    f = open("/tmp/gwgenerator.log", "a+")
    timestamp = strftime("%a, %d %b %Y %H:%M:%S +0000", gmtime())
    f.write(timestamp+message+"\n");

def checkTitles(titles):
    ''' 
    Checks if given title or titles exist.
    If a list of titles is passed, a similar list will be returned,
    where each position on the list corresponds to the original, and
    the title may either be the same, altered to the correct title, or '#missing'
    which signifies that the title is missing.
    -------
    Important: sanitize input before passing to this method!
    Unsanitized strings create shell injection vulnerabilities!
    '''
    
    passedString = "|".join(titles) if not isinstance(titles, str) else titles
    result = subprocess.check_output(["java", "-jar", pbb, "--check", passedString])
    
    results = result.split("|")
    def missingToNone(x): 
        x = x.replace("\n", "")
        return None if x == "#missing" else x
    newResults = map(missingToNone, results)
    
    return newResults


def checkId(entrez):
    '''
    Verifies the provided Entrez id by checking for a 404 error from mygene.info
    '''
    try:
        urlopen("http://mygene.info/gene/"+entrez)
        return True
    except HTTPError:
        return False 
    
def sanitize(string):
    '''
    Uses str.translate to remove forbidden characters from the string.
    '''
    _allchars = "".join([chr(x) for x in range(256)])
    forbidden = '#{}<>[]|;:'
    return string.translate(_allchars, forbidden)
        
def getGeneInfo(entrez):
    '''
    Returns the full JSON data for a gene.
    '''
    url = urlopen("http://mygene.info/gene/"+entrez)
    return json.loads(url.read())

def getSymbolFromId(jsonInfo):
    '''
    Returns the symbol for the given Entrez id.
    '''
    try:
        symbol = jsonInfo[u'symbol'].encode()
    except:
        # Couldn't retrieve symbol for id
        symbol = None
    return symbol

def getNameForId(jsonInfo):
    '''
    Returns the name for the given Entrez id.
    '''
    try:
        name = jsonInfo[u'name'].encode()
    except:
        # Couldn't retrieve symbol for id
        name = None
    return name

def getSuggestedTitle(entrez):
    '''
    blah
    '''
    symbol = getSymbolFromId(getGeneInfo(entrez))
    if checkTitles(symbol):
        symbol = symbol+" (gene)"
    return symbol

def getTemplateCode(entrez):
    '''
    Get the template wikicode from the bot for the specified entrez id.
    Important: sanitize your input! Unsanitized input leads to shell injection vulnerabilities!
    '''
    try:
        template = subprocess.check_output(["java", "-jar", pbb, "--templates", entrez])
    except CalledProcessError as e:
        return "Template generation unavailable for specified entrez id. "+e.output
        log(e.output)
    return template

def getStubCode(entrez):
    '''
    Get the stub wikicode from the bot for the specified entrez id.
    Important: sanitize your input! Unsanitized input leads to shell injection vulnerabilities!
    '''
    try:
        stub = subprocess.check_output(["java", "-jar", pbb, "--stubs", entrez])
    except CalledProcessError as e:
        return "Stub generation unavailable for specified entrez id. "+e.output
        log(e.ouput)
    return stub
    

def createTemplate(entrez):
    '''
    Instructs the bot to create the template corresponding to the entrez id on Wikipedia.
    Important: sanitize your input! Unsanitized input leads to shell injection vulnerabilities!
    '''
    try:
        subprocess.check_output(["java", "-jar", pbb, "--upload", "--templates", entrez])
    except CalledProcessError as e:
        return e.returncode;
        log(e.output)
    return 0;

def createStub(title, entrez):
    '''
    Instructs the bot to create the stub on wikipedia.
    Important: sanitize your input! Unsanitized input leads to shell injection vulnerabilities!
    '''
    try:
        subprocess.check_output(["java", "-jar", pbb, "--upload", title, "--stubs", entrez])
    except CalledProcessError as e:
        return e.returncode;
        log(e.output)
    return 0;

def printError(error):
    print(error)

# Outputs user-facing html
def output(validId, validTitle, template, stub, templateExists, titles, checked_titles):
    ''' 
    Outputs user-facing HTML based on passed options. 
    '''
    
    workingTitle = "%(symbol)s (%(name)s)" % {'symbol':titles['sym'], 'name':titles['name']}
    titleExists = True if (checked_titles['name'] or checked_titles['sym'] or checked_titles['sym2'] or checked_titles['title']) else False
    vals = {
            'entrez':           validId,
            'temp_status':      'exists' if templateExists else 'missing',
            'temp_status_str':  'exists' if templateExists else 'does not exist',
            'temp_action':      'edit' if templateExists else 'create',
            'temp_view':        'visible' if templateExists else 'hidden',
            'template':         template,
            'title':            workingTitle,
            'warning_stat':     'visible' if titleExists else 'hidden',
            'gene_name':        titles['name'],
            'name_status':      'exists' if checked_titles['name'] else 'missing',
            'gene_sym':         titles['sym'],
            'sym_status':       'exists' if checked_titles['sym'] else 'missing',
            'gene_sym_2':       titles['sym2'],
            'sym2_status':      'exists' if checked_titles['sym2'] else 'missing',
            'suggestion':       titles['title'] if titles['title'] else 'Click a suggested title above or enter custom title',
            'title_stat_vis':   'visible' if titles['title'] else 'hidden',
            'title_status':     'exists' if checked_titles['title'] else 'valid',
            'stub_action':      'edit' if checked_titles['title'] else 'create',
            'stub_act_status':  'disabled' if not titles['title'] else '',
            'action_title':     titles['title'] if titles['title'] else '',
            'stub_view':        'visible' if checked_titles['title'] else 'hidden',
            'stub':             stub 
            }
    
    # The targets in the template string are wrapped with {}
    output = '''
        <!DOCTYPE html>
        <html>
            <head>
                <link rel="stylesheet" type="text/css" media="screen" href="http://xanthus.scripps.edu/~eclarke/reset.css" />
                <link rel="stylesheet" type="text/css" media="screen" href="http://xanthus.scripps.edu/~eclarke/styles.css" />
                <script src="//ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.min.js" type="text/javascript"></script>
                <script src="http://xanthus.scripps.edu/~eclarke/gwgen.js" type=text/javascript></script>
            </head>
            <body><div id="container">
                <title>GeneWiki Code Creator</title>
                <div id="template" class="block">
                    <h2>Template: PBB/<span class="entrez">{entrez}</span></h2><hr />
                    <h3>Status: <span class="{temp_status}">{temp_status_str}</span></h3>
                    <button class="show">show template code</button> 
                    <button class="{temp_action}">{temp_action}</button> 
                    <button class="view {temp_view}">view template</button>
                    <br />
                    <textarea rows="15" cols="50" class="code" readonly="readonly">
{template}
                    </textarea>
                </div>
                <div id="loading"></div>
                <div id="stub" class="block">
                    <h2>Article: <span class="title">{title}</span></h2> <hr />
                    <div class="warning {warning_stat}">
                        Article may already exist on Wikipedia. Please check the pages marked 'exists' below before creating a new article.
                    </div>
                    <h3>Possible titles:</h3>
                    <div id="titles">
                    Gene symbol:     <span class="cl sym">{gene_sym}</span>       <span class="{sym_status}">{sym_status}</span> <br />
                    Gene name:       <span class="cl alt">{gene_name}</span>      <span class="{name_status}">{name_status}</span> <br />                    
                    Alternate title: <span class="cl sym_alt">{gene_sym_2}</span> <span class="{sym2_status}">{sym2_status}</span>
                    </div>
                    
                    <h3 class="title_select"> Choose title: 
                        <form>
                        <input type="text" size="60" value="{suggestion}" name="title"/>
                        <input type="hidden" value="{entrez}" name="id">
                        <input type="submit" value="Check">
                        <span id="chosenTitle" class="{title_stat_vis} {title_status}">{title_status}</span>
                        </form>
                    </h3>
                    <button class="show">show stub code</button>
                    <button {stub_act_status} class="{stub_action}">{stub_action} article '{action_title}'</button>
                    <button class="view {stub_view}">view article</button>
                    <br />
                    <textarea rows="15" cols="50" class="code" readonly="readonly">
{stub}
                    </textarea>
                </div>
            </div></body>
        </html>
    '''
    result = output.format(**vals)
    return textwrap.dedent(result);
    
def main():
    ''' 
    Parses CGI fields and sanitizes passed arguments, then determines conditionals from the arguments. 
    '''
    
    # Common variable definitions
    form = cgi.FieldStorage()
    
    _entrez = form["id"].value if "id" in form else None
    _title = form["title"].value if "title" in form else None
    _create = form["create"].value if "create" in form else None
    templateExists = False
    articleExists = False
    titleConflicts = False
    validId = ""
    validTitle = ""

    existingTitle = ""
    suggestedTitle = ""
    geneName = ""
    geneSym = ""
    
#    _entrez = "84516"
#    _title = "DCTN5"
#    _create = "stub"
    if _entrez:
        validId = _entrez if checkId(_entrez) else None
        if validId:
            templateTitle="Template:PBB/"+validId
            template = getTemplateCode(validId)
            stub = getStubCode(validId)
            jsonInfo = getGeneInfo(validId)
            geneSym  = getSymbolFromId(jsonInfo)
            geneName = getNameForId(jsonInfo)
            geneSym2 = geneSym+" (gene)" 
            
            if _title:
                validTitle = sanitize(_title)
                
            results = checkTitles([geneSym, geneName, geneSym2, validTitle, templateTitle])
            templateExists = results[4] if validTitle else results[3]
            titles = {'sym': geneSym, 'name':geneName, 'sym2': geneSym2, 'title': validTitle}
            checked_titles = {'sym': results[0], 'name': results[1], 'sym2': results[2], 'title': results[3] if validTitle else None}               
             
            
            if _create:
                if _create == "template":
                    if not templateExists:
                        print createTemplate(validId)
                    else:
                        print("Template already exists. Use 'update' to update existing templates.")
                elif _create == "stub":
                    if validTitle and not checked_titles['title']:
                        print createStub(validTitle, validId)
                    elif not validTitle:
                        print("Invalid article title.")
                    elif checked_titles['title']: 
                        print("Article already exists under title: "+checked_titles['title'])
                    
            else:
                print(output(validId, validTitle, template, stub, templateExists, titles, checked_titles))                    
        else:
            print("Invalid Entrez identifier.")
    else:
        print("Missing Entrez identifier.")
                

##
##
##

main()