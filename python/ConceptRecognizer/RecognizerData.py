"""
Olga Rosado, San Diego State University, 2010
This module runs a sample of gene wiki pages through the ConceptRecognizer module
and collects metrics data.  Outputs data into a semicolon delimited file 'Data.txt'
"""

from ConceptRecognizer import *
import time


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

f = open('Data.txt', 'w')
f.write("Title" + "\t" + "Size" + "\t" + "NCBOTime" + "\t" + "Step3Time" + "\t" + "step4Time" + "\t" + "step5Time" + "\t" + "step6Time" + "\t" + "scriptTime" + "\t" + "Concept count" + "\t" + "human disease" + "\t" + "gene ontology" + "\t" + "gene ontology extension" + "\t" + "missed links" + "\t" + "unknowns" + "\n")
f.close()

for pagetitle in pageList:
    start = time.clock()
    try:
        r = recognizer(pagetitle)
    except (Error, InvalidPage, InvalidSite, ServerError, FileError) as inst:
        print 'ERROR on', pagetitle, inst
    else:
        try:
            NCBOend = time.clock()
            conceptDict = r.extractWords(r.xmlDoc)
            step3end = time.clock()
            conceptDict = r.processGOterms(conceptDict)
            step4end = time.clock()
            conceptDict = r.processConceptLinks(conceptDict)
            step5end = time.clock()
            fullDict = r.findLinks(conceptDict)
            scriptEnd = time.clock()
        except (Error, InvalidPage, InvalidSite, ServerError, FileError):
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
                if v.link=="NA" and v.linked=="False":
                    missedLinks = missedLinks + 1

            size = len(r.content)
            NCBOtime = NCBOend - start
            step3Time = step3end - NCBOend
            step4Time = step4end - step3end
            step5Time = step5end - step4end
            step6Time = scriptEnd - step5end
            scriptTime = scriptEnd - start
            conceptsCount = len(fullDict)
            HDcount = HDcount
            GOcount = GOcount
            GOEcount = GOEcount
            missedLinks = missedLinks
            unknowns = (fullDict.keys()).count("unknown")

            f = open('Data.txt', 'a')
            f.write(pagetitle + "\t" + str(size) + "\t" + str(NCBOtime) + "\t" + str(step3Time) + "\t" + str(step4Time) + "\t" + str(step5Time) + "\t" + str(step6Time) + "\t" + str(scriptTime) + "\t" + str(conceptsCount) + "\t" + str(HDcount) + "\t" + str(GOcount) + "\t" + str(GOEcount) + "\t" + str(missedLinks) + "\t" + str(unknowns) + "\n")
            f.close()
