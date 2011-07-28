SNPedia Mashup code as described in ISMB 2011, in here.



1) Import Gene wiki and SNPedia: 

	GWImporter.py:
	Pulls articles of the Gene Wiki from Wikipedia to the target wiki.


	SNPediaImporter.py:
	Pulls SNP pages from SNPedia that are connected to genes in the Gene Wiki.
	*Input required: gene_symbol_wiki.txt (a mapping between gene symbols used in SNPedia and GeneWiki titles)



2) Edit gene pages:

	SWL_diseases_to_genes.py:
	Adds semantic links to Gene Ontology and disease associations (e.g.[[is-associated-with::xyz]]) in GeneWiki pages. 
	*Input required: candidate_go_annotations.txt, candidate_do_annotations.txt (output from NCBO Annotator pipeline).


	ImportAnnotatorTerms.py
	Imports disease and GO terms identified with the NCBO Annotator, and already added as semantic links (SWL_diseases_to_genes.py).
	*Input required: candidate_go_annotations.txt, candidate_do_annotations.txt. 


	Add_snp_disease_tables.py
	Adds collapsible tables with SNPs and associated diseases to the gene pages, under the 'Known Variants' section. 
	*Input required: snp_gene (gene-snp associations, details in SNPediaImporter.py)



3) Edit snp pages:

	SWL_diseases_to_snps.py
	Adds semantic links to diseases (e.g.[[is-associated-with::xyz]]) in SNP pages.
	Disease associations are identified separately by following wikilinks directed to/from an article in the SNPedia category 'medical condition'.
	Snp-disease links provided in 'snpedia_disease_final.csv' 
	*Input required: snpedia_disease_final.csv, snp_gene (gene-snp associations, details in SNPediaImporter.py) 



3) Edit disease pages:

	SWL_gene-snps_to_diseases.py
	Adds semantic queries at the bottom of disease pages, showing associated genes and snps.
	Uses a list of diseases as input, which is the union of diseases discovered in Gene Wiki and SNPedia.
	*Input required: disease_set (union of diseases in Gene Wiki and SNPedia, to be used as article names) 


