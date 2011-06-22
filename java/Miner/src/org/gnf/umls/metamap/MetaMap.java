/**
 * 
 */
package org.gnf.umls.metamap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gnf.genewiki.associations.CandidateAnnotation;
import org.gnf.genewiki.mapping.TextMapping;
import org.gnf.umls.Client;
import org.gnf.umls.UmlsDb;

import gov.nih.nlm.kss.models.meta.concept.Concept;
import gov.nih.nlm.kss.models.meta.context.Context;
import gov.nih.nlm.kss.models.meta.context.StringCxt;
import gov.nih.nlm.nls.metamap.AcronymsAbbrevs;
import gov.nih.nlm.nls.metamap.ConceptPair;
import gov.nih.nlm.nls.metamap.Ev;
import gov.nih.nlm.nls.metamap.Mapping;
import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;
import gov.nih.nlm.nls.metamap.Negation;
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Position;
import gov.nih.nlm.nls.metamap.Result;
import gov.nih.nlm.nls.metamap.Utterance;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.SQLException;

import javax.xml.rpc.ServiceException;


/**
 * @author bgood
 *
 */
public class MetaMap {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String input = "Some of the [[atypical antipsychotic]]s like [[aripiprazole]] are also [[partial agonist]]s at the 5-HT1A receptor and are sometimes used in low doses as augmentations to standard [[antidepressant]]s like the [[selective serotonin reuptake inhibitor]]s (SSRIs).";
		boolean singleterm = false;
		List<MMannotation> cs = getCUIsFromText(input, "GO,FMA,SNOMEDCT", singleterm, input);
		UmlsDb d = new UmlsDb();
		for(MMannotation c : cs){
			System.out.println(c);
			System.out.println(d.getIdsFromSourceForCUI(c.getCui(), "GO"));
		}
	}


	public static void listSourceIdsViaWebService(String cui, String[] vocabs){
		Client c = new Client("b", "k");
		List<Concept> concepts;
		try {
			concepts = c.getConceptProperties(cui, false, vocabs);

			for(Concept concept : concepts){
				Object[] contents  = concept.getCXTs().getContents();
				for (int i = 0; i < contents.length; i++){
					Context ctxs = (Context)contents[i];
					StringCxt[] stctx = ctxs.getCXT();
					for(StringCxt ctx : stctx){
						System.out.println(ctx.getSAB()+" --- "+ctx.getCode()); 
					}		
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static List<TextMapping> getTextMappingsFromMMannos(List<MMannotation> annos, String source_abbr){
		UmlsDb umls = new UmlsDb();
		List<TextMapping> map = new ArrayList<TextMapping>();
		Set<String> distinct = new HashSet<String>();
		for(MMannotation anno : annos){
			for(String id : umls.getIdsFromSourceForCUI(anno.getCui(), source_abbr)){
				if(distinct.add(id)){
					TextMapping m = new TextMapping();
					m.setConcept_id(id);
					m.setConcept_preferred_term(anno.getTermName());
					m.setEvidence(anno.getEvidence());
					m.setInput_text(anno.getInputText());
					m.setScore(anno.getScore());
					map.add(m);
				}
			}
		}
		try {
			umls.getCon().close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}
	
	/**
	 * Use MetaMap to find concepts - either in a single term (like a wikipedia title) or a block of text like a sentence.
	 * @param input
	 * @param sources
	 * @param singleTerm
	 * @return
	 */
	public static List<MMannotation> getCUIsFromText(String input, String sources, boolean singleTerm, String context){
		Set<String> u = new HashSet<String>();
		List<MMannotation> cuis = new ArrayList<MMannotation>();
		MetaMapApi api = new MetaMapApiImpl();
	//	List<String> theOptions = new ArrayList<String>();
		String opts = "";
		if(singleTerm){
			//theOptions.add("-z");  //single term processing
			opts += "-z ";
		}else{
			//theOptions.add("-y");  // turn on Word Sense Disambiguation when there is full text
			opts += "-y ";
		}
		if(sources!=null){
			//theOptions.add("--restrict_to_sources "+sources);					//-R --restrict_to_sources <sourcelist>
			opts += "-R "+sources;
		}
		if(opts.length()>0){
			api.resetOptions();
			api.setOptions(opts);
		}
	//	if (theOptions.size() > 0) {
	//		api.setOptions(theOptions);
	//	}
	//	System.out.println(api.getOptions());
		List<Result> resultList = api.processCitationsFromString(input);
		for(Result result : resultList){
			try {
				for (Utterance utterance: result.getUtteranceList()) {
					for (PCM pcm: utterance.getPCMList()) {
						for (Mapping map: pcm.getMappingList()) {
							for (Ev mapEv: map.getEvList()) {
								if(!u.contains(mapEv.getConceptId())){
									MMannotation cui = new MMannotation();
									cui.setCui(mapEv.getConceptId());
									cui.setScore(mapEv.getScore());
									cui.setStart(utterance.getPosition().getX());
									cui.setStop(utterance.getPosition().getY());
									cui.setTermName(mapEv.getConceptName());
									cui.setInputText(input);
									cui.setEvidence(context);
									u.add(mapEv.getConceptId());
									cuis.add(cui);
								}
							}				
						}
					}
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		api.getSession().disconnect();
		return cuis;
	}	


	public static void demo(String input){
		MetaMapApi api = new MetaMapApiImpl();
		List<String> theOptions = new ArrayList<String>();
		theOptions.add("-y");  // turn on Word Sense Disambiguation
		if (theOptions.size() > 0) {
			api.setOptions(theOptions);
		}
		List<Result> resultList = api.processCitationsFromString(input);

		Result result = resultList.get(0);
		List<AcronymsAbbrevs> aaList;
		try {
			aaList = result.getAcronymsAbbrevs();
			if (aaList.size() > 0) {
				System.out.println("Acronyms and Abbreviations:");
				for (AcronymsAbbrevs e: aaList) {
					System.out.println("Acronym: " + e.getAcronym());
					System.out.println("Expansion: " + e.getExpansion());
					System.out.println("Count list: " + e.getCountList());
					System.out.println("CUI list: " + e.getCUIList());
				}
			} else {
				System.out.println(" No accronyms or abbreviations found.");
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		List<Negation> negList;
		try {
			negList = result.getNegations();
			if (negList.size() > 0) {
				System.out.println("Negations:");
				for (Negation e: negList) {
					System.out.println("type: " + e.getType());
					System.out.print("Trigger: " + e.getTrigger() + ": [");
					for (Position pos: e.getTriggerPositionList()) {
						System.out.print(pos  + ",");
					}
					System.out.println("]");
					System.out.print("ConceptPairs: [");
					for (ConceptPair pair: e.getConceptPairList()) {
						System.out.print(pair + ",");
					}
					System.out.println("]");
					System.out.print("ConceptPositionList: [");
					for (Position pos: e.getConceptPositionList()) {
						System.out.print(pos + ",");
					}
					System.out.println("]");
				}
			} else {
				System.out.println(" No negations found.");
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			for (Utterance utterance: result.getUtteranceList()) {
				System.out.println("Utterance:");
				System.out.println(" Id: " + utterance.getId());
				System.out.println(" Utterance text: " + utterance.getString());
				System.out.println(" Position: " + utterance.getPosition());
				for (PCM pcm: utterance.getPCMList()) {
					System.out.println("Phrase:");
					System.out.println(" text: " + pcm.getPhrase().getPhraseText());

					System.out.println("Candidates:");
					for (Ev ev: pcm.getCandidateList()) {
						System.out.println(" Candidate:");
						System.out.println("  Score: " + ev.getScore());
						System.out.println("  Concept Id: " + ev.getConceptId());
						System.out.println("  Concept Name: " + ev.getConceptName());
						System.out.println("  Preferred Name: " + ev.getPreferredName());
						System.out.println("  Matched Words: " + ev.getMatchedWords());
						System.out.println("  Semantic Types: " + ev.getSemanticTypes());
						System.out.println("  MatchMap: " + ev.getMatchMap());
						System.out.println("  MatchMap alt. repr.: " + ev.getMatchMapList());
						System.out.println("  is Head?: " + ev.isHead());
						System.out.println("  is Overmatch?: " + ev.isOvermatch());
						System.out.println("  Sources: " + ev.getSources());
						System.out.println("  Positional Info: " + ev.getPositionalInfo());
					}
					System.out.println("Mappings:");
					for (Mapping map: pcm.getMappingList()) {
						System.out.println(" Map Score: " + map.getScore());
						for (Ev mapEv: map.getEvList()) {
							System.out.println("   Score: " + mapEv.getScore());
							System.out.println("   Concept Id: " + mapEv.getConceptId());
							System.out.println("   Concept Name: " + mapEv.getConceptName());
							System.out.println("   Preferred Name: " + mapEv.getPreferredName());
							System.out.println("   Matched Words: " + mapEv.getMatchedWords());
							System.out.println("   Semantic Types: " + mapEv.getSemanticTypes());
							System.out.println("   MatchMap: " + mapEv.getMatchMap());
							System.out.println("   MatchMap alt. repr.: " + mapEv.getMatchMapList());
							System.out.println("   is Head?: " + mapEv.isHead());
							System.out.println("   is Overmatch?: " + mapEv.isOvermatch());
							System.out.println("   Sources: " + mapEv.getSources());
							System.out.println("   Positional Info: " + mapEv.getPositionalInfo());
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/**
	 * options
	 *    -@ --WSD <hostname>                   : Which WSD server to use.
   -8 --dynamic_variant_generation       : dynamic variant generation
   -A --strict_model                     : use strict model 
   -C --relaxed_model                    : use relaxed model 
   -D --all_derivational_variants        : all derivational variants
   -J --restrict_to_sts <semtypelist>    : restrict to semantic types
   -K --ignore_stop_phrases              : ignore stop phrases.
   -R --restrict_to_sources <sourcelist> : restrict to sources
   -S --tagger <sourcelist>              : Which tagger to use.
   -V --mm_data_version <name>           : version of MetaMap data to use.
   -X --truncate_candidates_mappings     : truncate candidates mapping
   -Y --prefer_multiple_concepts         : prefer multiple concepts
   -Z --mm_data_year <name>              : year of MetaMap data to use.
   -a --all_acros_abbrs                  : allow Acronym/Abbreviation variants
   -b --compute_all_mappings             : compute/display all mappings
   -d --no_derivational_variants         : no derivational variants
   -e --exclude_sources <sourcelist>     : exclude semantic types
   -g --allow_concept_gaps               : allow concept gaps
   -i --ignore_word_order                : ignore word order
   -k --exclude_sts <semtypelist>        : exclude semantic types
   -l --allow_large_n                    : allow Large N
   -o --allow_overmatches                : allow overmatches 
   -r --threshold <integer>              : Threshold for displaying candidates. 
   -y --word_sense_disambiguation        : use WSD 
   -z --term_processing                  : use term processing 
	 */
}
