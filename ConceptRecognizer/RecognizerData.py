"""
Olga Rosado, San Diego State University, 2010
This module runs a sample of gene wiki pages through the ConceptRecognizer module
and collects metrics data.  Outputs data into a semicolon delimited file 'Data.txt'
"""

from ConceptRecognizer import recognizer
import time

class recognizerInfo:
    """ This is a data structure for the recognizer metrics"""
    def __init__(self):
        self.size = 0
        self.NCBOtime = None
        self.scriptTime = None
        self.conceptsCount = 0
        self.HDcount = 0
        self.GOcount = 0
        self.GOEcount = 0
        self.missedLinks = 0

#Sample of Gene Wiki pages        
pageList = [
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
        "Corticotropin-releasing hormone", 
        "FOXP2", 
        "Phenylalanine hydroxylase", 
        "Leptin", 
        "Erythropoietin", 
        "Vasopressin", 
        "Haptoglobin", 
        "Alpha-fetoprotein", 
        "Thrombin", 
        "Tumor necrosis factor-alpha", 
        "Glucagon", 
        "Rhodopsin", 
        "Renin", 
        "Prostate-specific antigen", 
        "Angiotensin", 
        "Human chorionic gonadotropin", 
        "Intrinsic factor", 
        "C-reactive protein", 
        "Uridine monophosphate synthetase", 
        "Ornithine transcarbamylase", 
        "Sonic hedgehog", 
        "N-ethylmaleimide sensitive fusion protein", 
        "BRCA1", 
        "Parathyroid hormone", 
        "ASPM (gene)", 
        "Thyroxine-binding globulin", 
        "Calcitonin", 
        "Alpha 1-antitrypsin", 
        "Transferrin", 
        "Thyroglobulin", 
        "Serotonin transporter", 
        "Somatostatin", 
        "Alpha-synuclein", 
        "Thermogenin", 
        "Antithrombin", 
        "Thrombopoietin", 
        "Cholecystokinin", 
        "Factor XII", 
        "Lysozyme", 
        "Factor VIII", 
        "Tissue plasminogen activator", 
        "Glucokinase", 
        "Parathyroid hormone-related protein", 
        "Granulocyte colony-stimulating factor", 
        "Brain-derived neurotrophic factor", 
        "Insulin receptor", 
        "Gastrin", 
        "Bcl-2", 
        "Catechol-O-methyl transferase", 
        "Choline acetyltransferase", 
        "Von Willebrand factor", 
        "Insulin-like growth factor 1", 
        "Wiskott-Aldrich syndrome protein", 
        "Angiotensin-converting enzyme", 
        "Gonadotropin-releasing hormone", 
        "Lactoferrin", 
        "Adiponectin", 
        "Resistin", 
        "Perforin", 
        "Myostatin", 
        "Ghrelin", 
        "Plasmin", 
        "Keratin 1", 
        "Keratin 7", 
        "Keratin 6A", 
        "Keratin 4", 
        "Keratin 3", 
        "Keratin 2A", 
        "Keratin 14", 
        "Keratin 13", 
        "Keratin 12", 
        "Keratin 10", 
        "Keratin 9", 
        "Keratin 20", 
        "Keratin 19", 
        "Keratin 18", 
        "Keratin 17", 
        "Keratin 16", 
        "Keratin 15", 
        "Ubiquitin carboxy-terminal hydrolase L1", 
        "Parkin (ligase)", 
        "Calcineurin", 
        "Dystrophin", 
        "Stromal cell-derived factor-1", 
        "Transthyretin", 
        "Duffy antigen system", 
        "Neuropeptide Y", 
        "Ceruloplasmin", 
        "Synaptophysin", 
        "Osteoprotegerin", 
        "CYP3A4", 
        "CYP2D6", 
        "CYP2E1", 
        "Interleukin 10", 
        "High-molecular-weight kininogen", 
        "Melanopsin", 
        "MyoD", 
        "Interleukin 2", 
        "CA-125", 
        "Complement receptor 1", 
        "CD36"
        ]

Data = {}
for pagetitle in pageList:
    start = time.clock()
    try:
        r = recognizer(pagetitle)
        NCBOend = time.clock()
        conceptDict = r.extractWords(r.xmlDoc)
        conceptDict = r.processGOterms(conceptDict)
        #conceptDict = r.processConceptLinks(conceptDict) 
        fullDict = r.findLinks(conceptDict)
        scriptEnd = time.clock()
    except Exception:
        pass
    else:
        HDcount = 0
        GOcount = 0
        GOEcount = 0
        missedLinks = 0
        for v in fullDict.itervalues():
            if v.localOntologyID == "42925":
                GOEcount=GOEcount+1
            elif v.localOntologyID == "42986":
                HDcount = HDcount+1
            else:
                GOcount=GOcount+1
            if v.link=="" and v.linked=="False":
                missedLinks = missedLinks + 1

        Data[pagetitle] = recognizerInfo()
        Data[pagetitle].size = len(r.content)
        Data[pagetitle].NCBOtime = NCBOend - start
        Data[pagetitle].scriptTime = scriptEnd - start
        Data[pagetitle].conceptsCount = len(fullDict)
        Data[pagetitle].HDcount = HDcount
        Data[pagetitle].GOcount = GOcount
        Data[pagetitle].GOEcount = GOEcount
        Data[pagetitle].missedLinks = missedLinks

if len(Data)>0:
    f = open('Data.txt', 'w')
    f.write("Title; Article size; NCBO running time; Script running time; Total Concepts; Human disease concepts; GO concepts; GO extension concepts; Missed links \n")
    for k, v in Data.iteritems():
        f.write(k + ";" + str(v.size) + ";" + str(v.NCBOtime) + ";" + str(v.scriptTime) + ";" + str(v.conceptsCount) + ";" + str(v.HDcount) + ";" + str(v.GOcount) + ";" + str(v.GOEcount) + ";" + str(v.missedLinks) + "\n")
    f.close()
