from google.appengine.ext import db
import datetime

class PageList(db.Model):
    date = db.DateProperty(auto_now_add=True)
    pages = db.StringProperty(multiline=True)

print 'Content-type:text/plain'
print ''

mostRecentEntry = PageList.all().order("-date").fetch(1)

entryDate = ""
content = ""
for z in mostRecentEntry:
    entryDate = z.date
    content = z.pages

print "Content: ", content

today = datetime.date.today()

if today == entryDate:
	# current record retrieved within one day
	print "Same date as record "
else:
	# current record more than one day old
	print "Putting new record"
	pl = PageList()
	pl.pages = get_data()
	pl.put()


print '----------------------'
z = PageList.gql('')
for entry in z:
	print entry.pages, entry.date


def get_data():
	return "asdfajjlksdf"
