package org.gnf.ncbo;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class Ontologies {

	//note that these are subject to change...
	//and they did, but now should be stable - switched to 'virtual ids'
	//See http://bioportal.bioontology.org/annotator
	public static final String OLD_GO_ONT = "44171";
	public static final String GO_ONT = "1070";//45118 //"44171";
	public static final String HUMAN_DISEASE_ONT = "1009";//45125 //44172";
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
	public static final String CHEBI_ONT = "1007";

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
		ont_names.put(CHEBI_ONT, "CHEBI");
		ont_names.put(NATIONAL_DRUG_FILE_ONT, "National Drug File");

		try {
			HttpClient client = new HttpClient();
			client.getParams().setParameter(
					HttpMethodParams.USER_AGENT,
					"Java1.6"
			);
			GetMethod method = new GetMethod("http://rest.bioontology.org/obs/ontologies");
			// Execute the GET method
			int statusCode = client.executeMethod(method);
			if( statusCode != -1 && statusCode != 500) {
				String contents = method.getResponseBodyAsString();
				method.releaseConnection();

				SAXBuilder builder = new SAXBuilder();
				Document doc = builder.build(new ByteArrayInputStream(contents.getBytes("UTF-8")));
				Element root = doc.getRootElement();
				Element data = root.getChild("data");
				Element list = data.getChild("list");
				List<Element> onts = list.getChildren("ontologyBean");
				for(Element ont : onts){
					ont_names.put(ont.getChildText("localOntologyId"), ont.getChildText("name"));
					ont_names.put(ont.getChildText("virtualOntologyId"), ont.getChildText("name"));
				}
			}else{
				System.out.println("bad response getting ontology info "+statusCode);
				statusCode = -10;
			}
		}
		catch( Exception e ) {
			e.printStackTrace();
		} 

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
