######################
#
#
# Single_mirroring.py
#
#
# A script to mirror GeneWiki pages from one Mediawiki to another.
# 
# I: 	GeneWiki page name 			(GeneName)
#	Target Mediawiki server URL		(TargetServer)
#	Login data on the target server	(user, password)
#	Path		"	"	"	  	(path)
#
# O:	GeneWiki page mirrored on the target server.
#
#
# 16/3/2010
# 
# Author: S.Loguercio
#
#
######################



import simplejson as json
import urllib
import re




	
		UrlRoot = "http://en.wikipedia.org/w/api.php"
		UrlParams = {
			'action': 'query',
			'prop': 'revisions',
			'rvprop': 'content',
			'format': 'json'
			}
       
	articleName='GeneName'
		articleName = re.sub(" ","_",articleName)
		UrlParams['titles'] = articleName
		f = urllib.urlopen(UrlRoot,urllib.urlencode(UrlParams) )
		z = f.read()
		output = json.loads(z)
		pages = output['query']['pages'].keys()
		content = output['query']['pages'][pages[0]]['revisions'][0]['*'].encode('utf-8')
		
		
		import mwclient
		site = mwclient.Site('TargetServer')
		site.login('user','password')
		page = site.Pages['path'+articleName]
		page.save(content,summary='comment')
    



# Example

UrlRoot = "http://en.wikipedia.org/w/api.php"
		UrlParams = {
			'action': 'query',
			'prop': 'revisions',
			'rvprop': 'content',
			'format': 'json'
			}
       
	articleName='ITK (gene)'
		articleName = re.sub(" ","_",articleName)
		UrlParams['titles'] = articleName
		f = urllib.urlopen(UrlRoot,urllib.urlencode(UrlParams) )
		z = f.read()
		output = json.loads(z)
		pages = output['query']['pages'].keys()
		content = output['query']['pages'][pages[0]]['revisions'][0]['*'].encode('utf-8')
		
		
		import mwclient
		site = mwclient.Site('www.wikidraft.org')
		site.login('Sal9000','salva')
		page = site.Pages['User:Sal9000/'+articleName]
		page.save(content,summary='testing write api')
    
