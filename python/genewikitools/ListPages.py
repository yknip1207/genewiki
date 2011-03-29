from google.appengine.ext import db

"""
List the title of all pages in the Gene Wiki.

Created: 20091208 AS

Known issues:
	- doesn't handle special characters

"""

class PageList(db.Model):
    date = db.DateProperty(auto_now_add=True)
    pages = db.TextProperty()


#### MAIN ####

#print 'Content-Type: text/html;charset=utf-8'
print 'Content-Type: text/plain'
print ''

mostRecentEntry = PageList.all().order("-date").fetch(1)

entryDate = ""
content = ""
for z in mostRecentEntry:
    entryDate = z.date
    content = z.pages

print content
