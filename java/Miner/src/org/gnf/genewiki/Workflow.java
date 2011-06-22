package org.gnf.genewiki;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.gnf.crawl.GoGeneCrawler;
import org.gnf.genewiki.associations.CandiAnnoSorter;
import org.gnf.genewiki.associations.PantherMapper;
import org.gnf.genewiki.mapping.GeneWikiPageMapper;
import org.gnf.genewiki.mapping.TextMapper;
import org.gnf.genewiki.parse.ParseUtils;
import org.gnf.genewiki.trust.GeneWikiTrust;
import org.gnf.go.GOmapper;
import org.gnf.search.YahooBOSS;
import org.gnf.umls.UmlsRelationship;
import org.gnf.util.BioInfoUtil;


/**
 * This class is where all of the methods needed to execute the Gene Wiki Mining process are called from.  In principle, 
 * if everything worked (especially external web services) you could uncomment all the lines in the main method and regenerate the results (but that is unlikely
 * because of the extensive dependence on external forces.
 * 
 * The static variables represent local configuration settings (that could be pulled into a config file somewhere if we attempted to make a standalone distribution of this)
 * 
 * @author bgood
 *
 */
public class Workflow {


	/**
	 * @param args
	 */
	public static void main(String[] args) {

		/////////////////////////////////////////////////////////////
		// Gather data
		/////////////////////////////////////////////////////////////
		
//		//Retrieve all of the data in Wikipedia for the Gene Wiki.
//		int limit = 10000000;
//		long start = System.currentTimeMillis();
//		GeneWikiUtils.retrieveAndStoreGeneWikiAsJava(limit);
//		System.out.println("\n--- GW pages retrieved --- "+(System.currentTimeMillis()-start)/1000+" seconds.");
//		start = System.currentTimeMillis();

		runHighPrecisionWithWikiTrust();
		
//		/////////////////////////////////////////////////////////////
//		// Generate Candidates Annotations via NCBO Link Mining
//		/////////////////////////////////////////////////////////////
//		//Map from targets of WikiLinks to ontology terms. (TermMapper replaced GOmapper for use with multiple ontologies)
//		TextMapper.buildGwikilinks2ConceptsWithNCBOAnnotator();
//		TextMapper.writeCandidateAnnotationsFromLinksNCBO();
//		System.out.println("\n--- NCBO link mining done ---"+(System.currentTimeMillis()-start)/1000+" seconds.");
//		start = System.currentTimeMillis();
//		/////////////////////////////////////////////////////////////
//		// Generate Candidates Annotations via NCBO Text Mining
//		/////////////////////////////////////////////////////////////
//		TextMapper.getGwikitext2ConceptsWithNCBOAnnotator();
//		System.out.println("\n--- NCBO text mining done ---"+(System.currentTimeMillis()-start)/1000+" seconds.");
//		start = System.currentTimeMillis();
		/////////////////////////////////////////////////////////////
		// Generate Candidates Annotations via MetaMap Text Mining 
		//(service must be installed locally and running)
		//from /bin in the metamap installation
		// > ./wsdserverctl start
		// > ./skrmedpostctl start
		// > ./mmserver10 
		/////////////////////////////////////////////////////////////
		
//		TextMapper.buildGwikilinks2GOConceptsWithMetamap();
//		TextMapper.writeCandidateAnnotationsFromLinksMetamap();
//		System.out.println("\n--- MetaMap link mining done ---"+(System.currentTimeMillis()-start)/1000+" seconds.");
//		start = System.currentTimeMillis();
////
//		TextMapper.getGwikiText2GOWithMetaMap();
//		System.out.println("\n--- MetaMap text mining done ---"+(System.currentTimeMillis()-start)/1000+" seconds.");
//		start = System.currentTimeMillis();
////		/////////////////////////////////////////////////////////////
////		// Build merged, master candidate annotation file (contains both metamap and ncbo data)
////		/////////////////////////////////////////////////////////////		
//		TextMapper.mergeTextAndLink();
//		System.out.println("\n--- All annotation discover complete and merged ---"+(System.currentTimeMillis()-start)/1000+" seconds.");
//		start = System.currentTimeMillis();
//		/////////////////////////////////////////////////////////////
//		// Run evaluation/ranking
//		// Build up an 'interestingness' score for each candidate
//		/////////////////////////////////////////////////////////////
//
//		//Prepare required data for ranking
//		//search engine co-occurrence
//		YahooBOSS.collectYahooCoOccurrenceData();
//		System.out.println("\n--- Yahoo done ---"+(System.currentTimeMillis()-start)/1000+" seconds.");
//		start = System.currentTimeMillis();	

//		//Assemble all of the evidence and compare to Gene Ontology annotations	
//		getGOAnnotationData();		
//		CandiAnnoSorter.rankAndSaveGOAnnotations();
		
//		build a file with no PBB-generated annotations, major obvious errors and matches to GO filtered
//		CandiAnnoSorter.getRealCandidateAnnotations();
//		System.out.println("\n--- GO predictions ranked and saved ---"+(System.currentTimeMillis()-start)/1000+" seconds.");
//		start = System.currentTimeMillis();
//////TODO select 'top one hundred' and send to friends for review
////		
////		//Disease Ontology
//////TODO contact DO
////		//compares do annotations to annotations mined from geneRIFs
//		CandiAnnoSorter.rankAndSaveDOAnnotations();
//		System.out.println("\n--- DO predictions ranked and saved ---"+(System.currentTimeMillis()-start)/1000+" seconds.");
//		start = System.currentTimeMillis();
		
	}

	/**
	 * Run the analysis with only NCBO, no synonyms and no redirects
	 */
	public static void runHighPrecisionWithWikiTrust(){
		long start = System.currentTimeMillis();
		////
		// grab the wikitrust markup - assumes the gene wiki data has already been collected from wikipedia (stored locally as objects)
		////
		GeneWikiTrust.collectAllWikiTrustMarkup(1000000);
		/////////////////////////////////////////////////////////////
		// Generate Candidates Annotations via NCBO Text Mining
		/////////////////////////////////////////////////////////////
		boolean allowSynonyms = false;
		boolean useGO = true; boolean useDO = true; boolean useFMA = true; boolean usePRO = false;
		//writes out a file with candidate annotations in it - collecting go, do, and FMA 
//		GeneWikiPageMapper.annotateGeneWikiArticlesWithNCBO(100000, allowSynonyms, useGO, useDO, useFMA, usePRO);
//		note that the process of filtering out obviously wrong (based on heuristics) and redundant annotations takes place in where the ontology specific load methods are run
//		in CandidateAnnotations.java
//		YahooBOSS.collectYahooCoOccurrenceData(Config.text_mined_annos);
		//assumes all the prerequisite file have been assembled
//		CandiAnnoSorter.rankAndSaveDOAnnotations(Config.text_mined_annos);
		CandiAnnoSorter.rankAndSaveGOAnnotations(Config.text_mined_annos);
//		CandiAnnoSorter.rankAndSaveFMAAnnotations(Config.text_mined_annos);
	}


	/**
	 * Assemble GO annotation data for comparison
	 */
	public static void getGOAnnotationData(){
		long start = System.currentTimeMillis();
//		//orthology-based evidence
//		//these third-party comparators don't depend on the above - they can be run independently and only need to be run once.
		PantherMapper.assemblePantherFamilyBasedGene2GOmap();
		System.out.println("\n--- Panther ready ---"+(System.currentTimeMillis()-start)/1000+" seconds.");
		start = System.currentTimeMillis();
//		//Computational predictions - funcbase	
		FuncBase.buildGene2Funcbase2go();
		System.out.println("\n--- FuncBase ready ---"+(System.currentTimeMillis()-start)/1000+" seconds.");
		start = System.currentTimeMillis();
//		//Literature co-occurrence - gene2go
		GoGeneCrawler.getParseAndStoreGoGeneData();		
		System.out.println("\n--- GoGene ready ---"+(System.currentTimeMillis()-start)/1000+" seconds.");
		start = System.currentTimeMillis();
	}

}
