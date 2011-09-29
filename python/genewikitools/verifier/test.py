import re
import urllib
import simplejson as json

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

def rewrite(input):
    output = re.sub(r'\{\{SWL\|target=(.*?)\|label=(.*?)\|type=(.*?)\}\}',
                    r'[[\3::\1|\2]]', input)
    output2 = re.sub(r'\{\{SWL\|target=(.*?)\|type=(.*?)\}\}',
                        r'[[\2::\1]]',
                        output)
    return output2

def main():
    print "hello"
    out = parseWikipediaText("{{PBB|geneid=1812}}", 'Dopamine_receptor_D1')
    print out
    
if __name__ == "__main__":
    main()