package org.gnf.genewiki;

public class Config {

	
	//depts/CompDisc/asu/Science/proteinboxbot/entrez_index
	/**
	 {<asu>hammer:entrez_index}121 % wc ../page_index.20101008
10166 13168 90846 ../page_index.20101008
{<asu>hammer:entrez_index}122 % wc GeneWikiIndex_20101008.txt
 43 109 898 GeneWikiIndex_20101008.txt
Okay, currently regenerating.  When the GeneWikiIndex_20101008.txt file reaches 10166 lines (or stops being updated), then it's done...
	 */
	
	/****************************
	 * Files used to assemble Candidate Annotation List
	 ****************************/
	//the candidate annotation generation process depends on these files
	public static String gwroot = "/Users/bgood/data/genewiki_jan_2011/";
	//article_gene map;
	public static String article_gene_map_file = gwroot+"input/publicgeneinfo/GeneWikiIndex_20101008.txt";
	//gene to go ~term map
	//extracted from ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene2go.gz
	public static String complete_gene_go_file = gwroot+"input/publicgeneinfo/12-13-2010/gene2go";
	//using BioInfoUtil.getSpeciesSpecificGene2go	
	public static String gene_go_file = gwroot+"input/publicgeneinfo/12-13-2010/human_gene2go.txt";
	//GO ontology database dump
	public static String go_dbdir = gwroot+"input/ontologies/10-21-2010/go_daily-termdb-tables/";
	//serialized gene wiki page objects
	public static String gwikidir = gwroot+"intermediate/javaobj/";
	//serialized gene wiki page objects with wikitrust markup
	public static String gwikidir_wt = gwroot+"intermediate/javaobj_wt/";
	//output of gene wiki link to go mapping
	public static String gwlink2go = gwroot+"intermediate/gwlink2go(all)";
	//output of gene wiki link to NCBO concept mapping
	public static String gwlink2concept = gwroot+"intermediate/gwlink2concept.txt";
	//output of gene wiki link to metamap concept mapping
	public static String gwlink2concept_mm = gwroot+"intermediate/metamap/gwlink2concept.txt";
	//output of gene wiki page to NCBO concept mapping
	public static String text_mined_annos = gwroot+"intermediate/text-mined-annos.txt";
	//candidate annotations from wikilinks
	public static String link_mined_annos = gwroot+"intermediate/link-mined-annos.txt";
	//merged candidate annotations
	public static String merged_mined_annos = gwroot+"intermediate/merged-mined-annos.txt";

	//output of gene wiki page to metamap concept mapping
	public static String text_mined_annos_mm = gwroot+"intermediate/metamap/text-mined-annos.txt";
	//candidate annotations from wikilinks
	public static String link_mined_annos_mm = gwroot+"intermediate/metamap/link-mined-annos.txt";
	//merged candidate annotations
	public static String merged_mined_annos_mm = gwroot+"intermediate/metamap/merged-mined-annos.txt";
	

	public static String wikipagetextdir = null;
	/****************************
	 * Parameters/Files used in ranking the Candidate Annotation List
	 ****************************/
	//gene information
	public static String gene2ensembl = gwroot+"input/publicgeneinfo/10-21-2010/gene2ensembl";
	
	//For working with the GO
	public static String gordf_with_parts = "file:"+gwroot+"input/ontologies/9-22-2010/go_daily-termdb.owl";
	public static String gordf = 			"file:"+gwroot+"input/ontologies/9-22-2010/go_simplified.owl";
	//The GeneGo data used this version of the GO (http://projects.biotec.tu-dresden.de/gogene/gogene/)
	public static String gordf_with_parts_forGeneGo = "file:"+gwroot+"input/ontologies/3-1-2010/go_201003-termdb.owl";

	//For the Disease Ontology
	public static String dordf = 	"file:"+gwroot+"input/DO/01-6-11-DOID.owl";
	public static String generif2do = 	gwroot+"input/DO/do_rif.human.txt";
	public static String omim2do = 	gwroot+"intermediate/DO_OMIM_GENE.txt";
	
	//For Yahoo Co-occurrence analysis
	public static String yahoo_gene_hits = gwroot+"intermediate/gwiki_yahoo_count";
	public static String yahoo_target_hits = gwroot+"intermediate/target_yahoo_count";
	public static String yahoo_intersect_hits = gwroot+"intermediate/intersect_yahoo_count";
	
	//for third-party validation
	//Panther
	public static String panther = gwroot+"input/panther/PANTHER_Sequence_Classfication_files/";
	public static String panther_goa = gwroot+"input/panther/goa/";
	public static String panther_gwiki_web_export = gwroot+"input/panther/pantherGeneList.txt";
	public static String gene_panther_go_file = gwroot+"intermediate/panther/gene2ortho2go";
	
	//GoGenes
	public static String gogene_urlroot = "http://projects.biotec.tu-dresden.de/gogene/gogene/Search/SIF";
	public static String gogene_downloaded_data = gwroot+"input/gogenes/gogenes.txt";
	public static String gogene_parsed_data = gwroot+"input/gogenes/gogenes-map.txt";
	public static String gogene_missinggo = gwroot+"input/gogenes/missinggo.txt";
	//FuncBase
	public static String funcbase_input = gwroot+"input/human_funcbase/predictions/human_predictions.tab/";
	public static String funcbase = gwroot+"input/human_funcbase/predictions/human_predictions_gene.tab/";
	
	/***************************
	 * OUTPUT FILES!
	 **************************/
	public static final String merged_mined_ranked_annos_go = gwroot+"output/candidate_go_annotations.txt";
	public static final String merged_mined_ranked_filtered_annos_go = gwroot+"output/filtered_go_annotations.txt";
	public static final String merged_mined_ranked_annos_go_mm = gwroot+"output/candidate_go_annotations_mm.txt";
	public static final String merged_mined_ranked_annos_do = gwroot+"output/candidate_do_annotations.txt";
	public static final String merged_mined_ranked_annos_fma = gwroot+"output/candidate_fma_annotations.txt";
	public static final String merged_mined_ranked_annos_pro = gwroot+"output/candidate_pro_annotations.txt";
}
