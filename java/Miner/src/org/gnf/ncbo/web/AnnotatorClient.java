package org.gnf.ncbo.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.gnf.ncbo.Ontologies;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class AnnotatorClient {

	public static final String PROD_URL = "http://rest.bioontology.org/obs/annotator?email=bgood@gnf.org";

	public static void main( String[] args ) {
		String text ="The cerebellum is in the brain. The cell had a cell membrane and a nucleolus. Schizophrenia is a disease.";// "Some apoptosis of the [[atypical antipsychotic]]s like [[aripiprazole]] are also [[partial agonist]]s at the 5-HT1A receptor and are sometimes used in low doses as augmentations to standard [[antidepressant]]s like the [[selective serotonin reuptake inhibitor]]s (SSRIs).";
		boolean useGO = true; boolean useDO = true; boolean useFMA = true; boolean usePRO = true;
		List<NcboAnnotation> annos = ncboAnnotateText(text, true, useGO, useDO, useFMA, usePRO);
		
		for(NcboAnnotation anno : annos){
			System.out.println(anno + " --- "+text.substring(anno.getContext().getFrom()-1, anno.getContext().getTo()+1));
			System.out.println(anno + " --- "+anno.getContext().getMatched_text());
		}
	}
	

	public static List<NcboAnnotation> ncboAnnotateText(String text2annotate, boolean allowSynonyms, boolean useGO, boolean useDO, boolean useFMA, boolean usePRO){
		Map<String, String> reqParams = getParametersNoMapping(text2annotate, allowSynonyms, useGO, useDO, useFMA, usePRO);
		return(ncboAnnotate(reqParams, text2annotate));
	}

	/**
	 * Hard coded parameter set
	 * See: http://www.bioontology.org/wiki/index.php/Annotator_User_Guide
	 * @param text2annotate
	 * @return
	 */
	public static Map<String, String> getParametersNoMapping(String text2annotate, boolean allowSynonyms, boolean useGO, boolean useDO, boolean useFMA, boolean usePRO){
		Map<String, String> params = new HashMap<String,String>();
		params.put("isVirtualOntologyId", "true");
		params.put("rqnum", "0");
		params.put("textToAnnotate", text2annotate);
		// Configure the request form parameters
		params.put("filterNumber", "true");
		params.put("minTermSize", "3");
		if(allowSynonyms){
			params.put("withSynonyms", "true");
		}else{
			params.put("withSynonyms", "false");
		}
		params.put("longestOnly", "true");
		params.put("wholeWordOnly", "true"); //setting this to false gives really ridiculous results like 'r' matching 'aortic valve insufficiency
		params.put("stopWords", "protein, gene");
		params.put("withDefaultStopWords", "true");
		params.put("scored", "true");
		params.put("mappingTypes", "null"); //null results in only ISA mappings (if any ontologies to expand)
	//	params.put("ontologiesToExpand", 
	//			Ontologies.GO_ONT  +","+
		//		Ontologies.HUMAN_DISEASE_ONT+","+
		//		Ontologies.HPO_ONT+","+
		//		Ontologies.FMA_ONT+","+
		//		Ontologies.PRO_ONT
		//);     
//		params.put("levelMax", "2");
		
		String onts2use = "";
		if(useGO){
			onts2use+=Ontologies.GO_ONT  +",";
		}
		if(useDO){
			onts2use+=Ontologies.HUMAN_DISEASE_ONT +",";
		}
		if(useFMA){
			onts2use+=Ontologies.FMA_ONT +",";
		}
		if(usePRO){
			onts2use+=Ontologies.PRO_ONT +",";
		}
		if(onts2use.endsWith(",")){
			onts2use = onts2use.substring(0, onts2use.length()-1);
			params.put("ontologiesToKeepInResult",onts2use);
		}
//		params.put("ontologiesToKeepInResult", 
//				Ontologies.GO_ONT  +","+
//				Ontologies.HUMAN_DISEASE_ONT+","+
//				Ontologies.FMA_ONT+","+
//				Ontologies.HPO_ONT+","+
//				Ontologies.PRO_ONT
//		);
		//T999 means anything in the annotator, its not a UMLS classification
		//params.put("semanticTypes", "T999");

		//always uses xml (THE DEFAULT)
		//params.put("format", "asText");

		return params;
	}

	/**
	 * Hard coded parameter set intended for finding concepts in sentences from wikipedia
	 * See: http://www.bioontology.org/wiki/index.php/Annotator_User_Guide
	 * @param text2annotate
	 * @return

	public static Map<String, String> getSentenceParametersNoMapping(String text2annotate){
		Map<String, String> params = new HashMap<String,String>();
		params.put("rqnum", "0");
		params.put("textToAnnotate", text2annotate);
		// Configure the request form parameters
		params.put("filterNumber", "true");
		params.put("minTermSize", "3");
		params.put("withSynonyms", "true");
		params.put("longestOnly", "true");
		params.put("wholeWordOnly", "true"); //setting this to false gives really ridiculous results like 'r' matching 'aortic valve insufficiency
		params.put("stopWords", "protein, gene");
		params.put("withDefaultStopWords", "true");
		params.put("scored", "true");
		params.put("mappingTypes", "null"); //null results in only ISA mappings (if any ontologies to expand)
		params.put("ontologiesToExpand", 
				Ontologies.GO_ONT  +","+
				Ontologies.HUMAN_DISEASE_ONT+","+
				Ontologies.HPO_ONT+","+
				Ontologies.FMA_ONT+","+
				Ontologies.PRO_ONT
		);     
		params.put("levelMax", "2");
		params.put("ontologiesToKeepInResult", 
				Ontologies.GO_ONT  +","+
				Ontologies.HUMAN_DISEASE_ONT+","+
				Ontologies.HPO_ONT+","+
				Ontologies.FMA_ONT+","+
				Ontologies.PRO_ONT
		);
		//T999 means anything in the annotator, its not a UMLS classification
		//params.put("semanticTypes", "T999");

		//always uses xml (THE DEFAULT)
		//params.put("format", "asText");

		return params;
	}
	 */
	
	/**
	 * See http://www.bioontology.org/wiki/index.php/Annotator_User_Guide
	 * @param input
	 * @param params
	 * @return
	 */
	public static List<NcboAnnotation> ncboAnnotate(Map<String, String> params, String text2annotate){
		List<NcboAnnotation> annos = new ArrayList<NcboAnnotation>();
		//add matched_text
		
		try {
			HttpClient client = new HttpClient();
			client.getParams().setParameter(
					HttpMethodParams.USER_AGENT,
					"Java1.6"
			);
			PostMethod method = new PostMethod(PROD_URL);
			// Configure the form parameters
			for(Entry<String, String> param : params.entrySet()){
				if(!param.getKey().equals("rqnum")){
					method.addParameter(param.getKey(), param.getValue());
				}
			}
			// Execute the POST method
			int statusCode = client.executeMethod(method);
			if( statusCode != -1 ) {
				String contents = method.getResponseBodyAsString();
				method.releaseConnection();
				if(!contents.startsWith("<HTML><HEAD>\n<TITLE>Network Error</TITLE>")){
					annos = parseAnnotations(contents, text2annotate);
				}else{
					statusCode = -10;
				}
			}

			if(statusCode<0){
				int rnum = Integer.parseInt(params.get("rqnum"));
				if(rnum<10){
					System.out.println("No or bad response for "+params.get("textToAnnotate")+" rnum "+rnum);
					rnum++;
					params.put("rqnum", ""+rnum);
					//request failed, try again in 2 seconds up to 10 times.
					Thread.currentThread().sleep(2000);				
					return ncboAnnotate(params, text2annotate);
				}else{
					System.out.println("Quitting: Service would not answer request to annotate: "+params.get("textToAnnotate"));
					System.exit(-1);
				}
			}
		}
		catch( Exception e ) {
			e.printStackTrace();
		} 

		return annos;
	}

	/**
	 * Parses the xml returned by the NCBO Annotator
	 * @param xml
	 * @return
	 */
	public static List<NcboAnnotation> parseAnnotations(String xml, String input_text){
		//System.out.println(xml);
		List<NcboAnnotation> hits = new ArrayList<NcboAnnotation>();
		SAXBuilder builder = new SAXBuilder();
		try {
			Document doc = builder.build(new ByteArrayInputStream(xml.getBytes("UTF-8")));
			Element root = doc.getRootElement();
			if(root==null){
				return null;
			}
			Element annos_data = root.getChild("data");
			if(annos_data==null){
				return null;
			}
			Element annos_bean = annos_data.getChild("annotatorResultBean");
			if(annos_bean==null){
				return null;
			}
			Element annos_root = annos_bean.getChild("annotations");
			if(annos_root==null){
				return null;
			}
			
//			Element annos_root = root.getChild("data").getChild("annotatorResultBean").getChild("annotations");

			List<Element> annos = annos_root.getChildren("annotationBean");
			if(annos==null){
				return null;
			}
			for(Element anno : annos){
				NcboAnnotation a = new NcboAnnotation();
				Element score_e = anno.getChild("score");
				a.setScore(Double.parseDouble(score_e.getText()));
				Element context_e = anno.getChild("context");
				Context context = new Context();
				context.setContextClass(context_e.getAttributeValue("class"));
				context.setContextName(context_e.getChildText("contextName"));
				context.setFrom(Integer.parseInt(context_e.getChildText("from")));
				context.setTo(Integer.parseInt(context_e.getChildText("to")));
				if(input_text.length()<context.getTo()){
					context.setTo(input_text.length()-1);
				}
				context.setMatched_text(input_text.substring(context.getFrom()-1, context.getTo()));
				context.setDirect(Boolean.parseBoolean(context_e.getChildText("isDirect")));

				Element concept = anno.getChild("concept");
				Concept c = new Concept(concept);
				if(context.isDirect){
					a.setConcept(c);
				}else{
					context.setLevel(Integer.parseInt(context_e.getChildText("level")));
					context.setConcept(c);
				}
				a.setContext(context);	
				hits.add(a);
			}

		} catch (JDOMException e) {
			System.out.println(xml);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(xml);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hits;
	}


}
