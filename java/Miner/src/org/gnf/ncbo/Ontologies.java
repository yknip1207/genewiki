package org.gnf.ncbo;

import java.util.HashMap;
import java.util.Map;

public class Ontologies {

	//note that these are subject to change...
	//and they did, but now should be stable - switched to 'virtual ids'
	//See http://bioportal.bioontology.org/annotator
	public static final String OLD_GO_ONT = "44171";
	public static final String GO_ONT = "1070";//"44171";
	public static final String HUMAN_DISEASE_ONT = "1009";//44172";
	public static final String FMA_ONT = "1053";//"39966";
	public static final String HPO_ONT = "1125";//"44170";
	public static final String NATIONAL_DRUG_FILE_ONT = "1352";//"40402";
	public static final String MESH_ONT = "1351";//"42142";
	public static final String NCI_ONT = "1499";//"42838";
	public static final String NCBI_ORG_CLASS_ONT = "1132";//"38802";
	public static final String SNOMED_ONT = "1353";//"42789"; 
	public static final String PRO_ONT = "1052";//"44133"; //ontology of proteins - source of gene names... http://pir.georgetown.edu/pro/pro.shtml 
	public static final String OMIM_ONT = "1348";
	public static final String OLD_HUMAN_DISEASE_ONT = "44172";
	
	public Map<String, String> ont_names;
	
	public Ontologies() {
		ont_names = new HashMap<String, String>();
		ont_names.put(GO_ONT, "Gene Ontology");
		ont_names.put(HUMAN_DISEASE_ONT, "Disease Ontology");
		ont_names.put(FMA_ONT, "FMA");
		ont_names.put(HPO_ONT, "Human Phenotype Ontology");
		ont_names.put(NATIONAL_DRUG_FILE_ONT, "National Drug File");
		ont_names.put(MESH_ONT, "MeSH");
		ont_names.put(NCI_ONT, "NCI Thesaurus");
		ont_names.put(NCBI_ORG_CLASS_ONT, "NCBI Organism classification");
		ont_names.put(SNOMED_ONT, "SNOMED");
		ont_names.put(PRO_ONT, "Protein Ontology");
		ont_names.put(OMIM_ONT, "OMIM");
	}
/**
 *       <ontologyBean>
        <id>555</id>
        <localOntologyId>44806</localOntologyId>
        <name>Gene Ontology</name>
        <version>1.1055</version>
        <description></description>
        <status>28</status>
        <virtualOntologyId>1070</virtualOntologyId>
        <format>OBO</format>
      </ontologyBean>
 */
}
