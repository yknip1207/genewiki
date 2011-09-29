package org.genewiki.annotationmining;

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

import org.genewiki.GeneWikiUtils;
import org.genewiki.annotationmining.annotations.CandiAnnoSorter;
import org.genewiki.parse.ParseUtils;
import org.genewiki.trust.GeneWikiTrust;
import org.scripps.datasources.FuncBase;
import org.scripps.datasources.GoGeneCrawler;
import org.scripps.datasources.PantherMapper;
import org.scripps.nlp.umls.UmlsRelationship;
import org.scripps.ontologies.go.GOmapper;
import org.scripps.search.YahooBOSS;
import org.scripps.util.BioInfoUtil;


/**
 * Upper class for executing Gene Wiki mining analysis
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
		
		//Retrieve all of the data in Wikipedia for the Gene Wiki.
		int limit = 10000000;
		long start = System.currentTimeMillis();
		boolean use_wiki_trust = true;
		GeneWikiUtils.retrieveAndStoreGeneWikiAsJava(limit, "../gw_creds.txt", Config.gwikidir, true);
		System.out.println("\n--- GW pages retrieved --- "+(System.currentTimeMillis()-start)/1000+" seconds.");
		start = System.currentTimeMillis();

		runHighPrecisionWithWikiTrust();
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
		GeneWikiPageMapper mapper = new GeneWikiPageMapper();
		mapper.annotateGeneWikiArticlesWithNCBO(100000, allowSynonyms, Config.gwikidir, Config.text_mined_annos);
//		note that the process of filtering out obviously wrong (based on heuristics) and redundant annotations takes place in where the ontology specific load methods are run
//		in CandidateAnnotations.java
		//assumes all the prerequisite files have been assembled
		CandiAnnoSorter.rankAndSaveDOAnnotations(Config.text_mined_annos);
		CandiAnnoSorter.rankAndSaveGOAnnotations(Config.text_mined_annos);
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
