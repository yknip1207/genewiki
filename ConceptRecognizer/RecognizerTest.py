# Test the ConceptRecognizer's text-mining results
# This script runs some Gene Wiki pages through the ConceptRecognizer module
# and saves keyword results in NCBOResults.txt file

from ConceptRecognizer import recognizer
PageList = [
        "Calreticulin", 
        "Insulin", 
        "P53", 
        "Catalase", 
        "Reelin", 
        "SRY", 
        "Myoglobin", 
        "Cytochrome c", 
        "Dihydrofolate reductase", 
        "Fibronectin", 
        "Proopiomelanocortin", 
        "Corticotropin-releasing hormone"
        ]

f = open('NCBOResults.txt', 'w')

for page in PageList:
    # Start loop
    recog = recognizer(page)
    f.write("**" + page.upper() + "**\n")
    f.write("\n")
    for k, v in recog.LinksDict.iteritems():
        f.write(k + "===" + v + "\n")
    f.write("\n")
        

f.close()

