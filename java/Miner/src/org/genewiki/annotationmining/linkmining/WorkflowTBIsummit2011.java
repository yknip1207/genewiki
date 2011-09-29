package org.genewiki.annotationmining.linkmining;

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
import org.genewiki.annotationmining.archive.TextMapper;
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
 * Workflow underlying link mining as described in http://proceedings.amia.org/16pc2f/1
 * Mining Gene Ontology Annotations From Hyperlinks in the Gene Wiki
Benjamin M. Good, Andrew I. Su
 * 
 * @author bgood
 *
 */
public class WorkflowTBIsummit2011 {


	/**
	 * @param args
	 */
	public static void main(String[] args) {

		/////////////////////////////////////////////////////////////
		// Gather data
		/////////////////////////////////////////////////////////////
		
//		//Retrieve all of the data in Wikipedia for the Gene Wiki.
		int limit = 10000000;
		long start = System.currentTimeMillis();
		GeneWikiUtils.retrieveAndStoreGeneWikiAsJava(limit, null, null, false);
//		System.out.println("\n--- GW pages retrieved --- "+(System.currentTimeMillis()-start)/1000+" seconds.");
		start = System.currentTimeMillis();
		
//		/////////////////////////////////////////////////////////////
//		// Generate Candidates Annotations via NCBO Link Mining
//		/////////////////////////////////////////////////////////////
		//Map from targets of WikiLinks to ontology terms. (TextMapper replaced GOmapper for use with multiple ontologies)
		TextMapper.buildGwikilinks2ConceptsWithNCBOAnnotator();
		TextMapper.writeCandidateAnnotationsFromLinksNCBO();
		System.out.println("\n--- NCBO link mining done ---"+(System.currentTimeMillis()-start)/1000+" seconds.");
		start = System.currentTimeMillis();
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

////		//compares do annotations to annotations mined from geneRIFs
//		CandiAnnoSorter.rankAndSaveDOAnnotations();
//		System.out.println("\n--- DO predictions ranked and saved ---"+(System.currentTimeMillis()-start)/1000+" seconds.");
//		start = System.currentTimeMillis();
		
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
