print 'Content-Type: text/html'
print ''

print '''
<pre>
List of tools
-------------

/ListPages
   Input: None
   Output: page titles of all Gene Wiki pages (JSON)
   Example: <a href="/ListPages">/ListPages</a>
   Note: returns the most recent entry in the data store

/Pubmed2Mesh
   Input: List of Pubmed IDs (comma separated)
   Output: MeSH terms for each PMID (JSON)
   Example: <a href="/Pubmed2Mesh?pmids=18464898,16492761">/Pubmed2Mesh?pmids=18464898,16492761</a>

/RefreshPageList
   Input: None
   Output: confirmation message
   Example: <a href="/RefreshPageList">/RefreshPageList</a>
   Note: refreshes the Gene Wiki page list in the data store; typically called by cron only

</pre>
'''
