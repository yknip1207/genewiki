import os
import cgi
import urllib
from django.utils import simplejson as json
from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext.webapp import template
from google.appengine.ext import db
from google.appengine.ext.webapp.util import login_required

def parseWikipediaText(text, pagetitle):
    UrlRoot = "http://en.wikipedia.org/w/api.php"
    UrlParams = {
            'action': 'parse',
            'title': pagetitle,
            'text': text,
            'redirects': 'true',
            'format': 'json'
            }
    f = urllib.urlopen(UrlRoot, urllib.urlencode(UrlParams) )
    z = f.read()
    if z:
        output = json.loads(z)
        parsed = output['parse']['text']['*']
        return parsed
    else:
        print "parseWikipedia returned no response"
        return

class Anno(db.Model):
    entrezGeneId = db.StringProperty();
    geneWikiPageName = db.StringProperty();
    ontology = db.StringProperty();
    firstSentence = db.TextProperty();
    contextSentence = db.TextProperty();
    sectionHeading = db.StringProperty();
    targetWikiPageTitle = db.StringProperty();
    ontTerm = db.StringProperty();
    ontAcc = db.StringProperty();
    stringMatchingMethod = db.StringProperty();
    directMatch = db.StringProperty();
    parentMatch = db.StringProperty();
    confidence = db.FloatProperty();
    goPrior = db.FloatProperty();
    yahooIntersectionScore = db.FloatProperty();
    yahooGeneHits = db.IntegerProperty();
    yahooTermHits = db.IntegerProperty();
    yahooIntersectionHits = db.IntegerProperty();
    date = db.DateTimeProperty(auto_now_add=True)
    
class Assessment(db.Model):
    vote = db.StringProperty();
    author = db.UserProperty()
    comment = db.StringProperty(multiline=True)
    date = db.DateTimeProperty(auto_now_add=True)
    anno = db.ReferenceProperty(Anno);

class Verify(webapp.RequestHandler):    
    @login_required
    def get(self):
        logouturl = users.create_logout_url("/verifier/")
        verifier = users.get_current_user()
        #get an annotation that this user has not seen yet        
        annos_query = Anno.all()
        annos = annos_query.fetch(100)
        annow = ''
        for anno in annos:
            #if no back reference to an anno from this user than we are good
            done = False
            for assess in anno.assessment_set:
                if assess.author == verifier:
                    done = True
                    break
            if done == False:
                annow = anno
                break
        
        if annow == '':
            self.response.out.write("All done <a href=\""+logouturl+"\">Log in as another user</a>")

        else:                            
            if "omponent" in anno.ontology:
                anno.ontology = " can be located in "
            elif "unction" in anno.ontology:
                anno.ontology = " has molecular function "
            elif "rocess" in anno.ontology:
                anno.ontology = " plays a role in the process of "
            anno.contextSentence = anno.contextSentence.replace("!-","<b>")
            anno.contextSentence = anno.contextSentence.replace("-!","</b>")
             
            wikipageurl = "http://mobile.wikipedia.org/transcode.php?go="+anno.geneWikiPageName

         #   wikitext = parseWikipediaText(anno.contextSentence,anno.geneWikiPageName)
                            
            template_values = {
          #                     'wikitext': wikitext,
                               'wikipageurl': wikipageurl,
                               'anno': anno,
                               'annokey': str(anno.key()),
                               'logouturl': logouturl,
                               'username':verifier.nickname()
                               }            
            path = os.path.join(os.path.dirname(__file__), 'verifier.html')
            self.response.out.write(template.render(path, template_values))      

class NewVerification(webapp.RequestHandler):
    def post(self):
        assess = Assessment()
        assess.vote = self.request.get('vote')
        if ((not assess.vote) or (assess.vote == '')):
            self.redirect('/verifier/')
        else:
            assess.author = users.get_current_user()
            assess.comment = self.request.get('comment')
            theanno = Anno()
            theanno = theanno.get(self.request.get('annokey'))
            assess.anno = theanno
            assess.put()
            self.redirect('/verifier/')


class ListAnnos(webapp.RequestHandler):    
    def get(self):
        annos_query = Anno.all().order('-date')
        annos = annos_query.fetch(10)
        
        template_values = {
            'annos': annos,
            'url': 'newannobuilder',
            'url_linktext': 'Add New Anno',
            }

        path = os.path.join(os.path.dirname(__file__), 'listannos.html')
        self.response.out.write(template.render(path, template_values))        

class NewAnnoBuilder(webapp.RequestHandler):
    def get(self):
        template_values = {}
        path = os.path.join(os.path.dirname(__file__), 'annobuilder.html')
        self.response.out.write(template.render(path, template_values))  

class NewAnno(webapp.RequestHandler):
    def post(self):
        anno = Anno()
        
        anno.geneWikiPageName = self.request.get('gene')
        anno.ontology = self.request.get('ontology')
        anno.firstSentence = self.request.get('firstSentence')
        anno.contextSentence = self.request.get('contextSentence')
        anno.ontTerm = self.request.get('ontTerm')
        anno.ontAcc = self.request.get('ontAcc')
        
        anno.put()
        self.redirect('/verifier/listannos')


application = webapp.WSGIApplication([('/verifier/', Verify),
                                      ('/verifier/newassess', NewVerification),
                                      ('/verifier/listannos', ListAnnos),
                                      ('/verifier/newannobuilder', NewAnnoBuilder),
                                      ('/verifier/newanno', NewAnno)],
                                     debug=True)

def main():
    run_wsgi_app(application)

if __name__ == "__main__":
    main()
