/**
 * 
 */
package org.genewiki.annotationmining.annotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.genewiki.GeneWikiLink;
import org.genewiki.GeneWikiPage;
import org.genewiki.GeneWikiUtils;
import org.genewiki.Sentence;
import org.genewiki.annotationmining.Config;
import org.genewiki.parse.ParseUtils;

/**
 * Manage filtering of mined annotations - after they have been collected
 * @author bgood
 *
 */
public class Filter {

	public static String pbb_summary_regx = "(?s)\\{\\{PBB_Summary.+?\\}\\}";
	Pattern pbb_summary;



	public Filter() {
		pbb_summary = Pattern.compile(pbb_summary_regx);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CandidateAnnotation canno = new CandidateAnnotation();
		canno.setEntrez_gene_id("2020");
		//	canno.setParagraph_around_link("CDK family members are highly similar to the gene products of S. cerevisiae cdc28, and S. pombe cdc2, and known as important cell cycle regulators.");
		//	canno.setSection_heading("Summary");
		canno.setParagraph_around_link("The human engrailed homologs 1 and 2 encode homeodomain-containing proteins and have been implicated in the control of pattern formation during development of the central nervous system.");
		canno.setSection_heading("Summary");

	}


	public static boolean fromUnwantedWikiSection(CandidateAnnotation canno){
		boolean unwanted = false;
		if(canno.getSection_heading().trim().equalsIgnoreCase("Further reading")){
			unwanted = true;
		}
		return unwanted;
	}

	/**
	 * Test for use of locally defined stop word or accession from the gene ontology in the annotation.  
	 * 
	 * TODO add these insert into cannos_go select * from cannos_go_dirty where term != 
	 * 'Splicing' and term != 
	 * 'Birth' and term != 
	 * 'chromosome' and term != 
	 * 'Reproduction' and term != 
	 * 'Antibodies' and term != 
	 * 'virus' and term != 
	 * 'SerA' and term != 
	 * 'soma' and term != 
	 * 'Nucleic' and term != 
	 * '"RNA splicing, via endonucleolytic cleavage and ligation"' and term != 
	 * 'Group II intron splicing' and term !=  
	 * 'Group III intron splicing' and term != 
	 * 'Group I intron splicing' and term != 
	 * '"nuclear mRNA splicing, via spliceosome"' and term != 
	 * 'seeing';
	 * 
	 */
	public static boolean containsUnwantedGOTerm(CandidateAnnotation canno){
		boolean unwanted = false;
		if( canno.getTarget_accession().contains("GO:0005623")|| //cellular component
				canno.getTarget_accession().contains("GO:0003675")|| //protein
				canno.getTarget_preferred_term().equalsIgnoreCase("chromosome")) 			
		{
			unwanted = true;
		}
		return unwanted;
	}

	public static String replaceOldGoAcc(String a){
		if(a.equals("0031202")){
			a = "0000375";
		}else if(a.equals("0000119")){
			a = "0016592";
		}else if(a.equals("0008367")){
			a = "0051635";
		}else if(a.equals("0015978")){
			a = "0015976";
		}else if(a.equals("0007025")){
			a = "0006457";
		}else if(a.equals("0007242")){
			a = "0023034";
		}else if(a.equals("0019642")){
			a = "0006096";
		}
		return a;
	}
	
	/**
	 * Test for use of locally defined stop word or accession from the disease ontology in the annotation.  
	 */
	public static boolean containsUnwantedDOTerm(CandidateAnnotation canno){
		boolean unwanted = false;
		if(canno.getTarget_preferred_term().equalsIgnoreCase("disease")||
				canno.getTarget_preferred_term().equalsIgnoreCase("disorder")||
				canno.getTarget_preferred_term().equalsIgnoreCase("syndrome")||
				canno.getTarget_preferred_term().equalsIgnoreCase("Recruitment")||
				canno.getTarget_preferred_term().equalsIgnoreCase("chronic rejection of renal transplant")){
			unwanted = true;
		}
		return unwanted;
	}

	/**
	 * Test for use of locally defined stop word or accession from the FMA in the annotation.  
	 */
	public static boolean containsUnwantedFMATerm(CandidateAnnotation canno){
		boolean unwanted = false;

		return unwanted;
	}


	/**
	 * Tests to see if the text that generated the candidate annotation occurs in the text box added by the protein box bot
	 * Note that human editors may sometimes edit this text without removing the template so this is not a guarantee of robotivity
	 * @param page
	 * @param canno
	 * @return
	 */
	public boolean isProteinBoxBotSummary(GeneWikiPage page, CandidateAnnotation canno){
		//if its not in the summary section don't worry
		if(!canno.getSection_heading().equals("Summary")){
			return false;
		}
		//does the page contain any PBB? If not, return false
		if(!page.getPageContent().contains("PBB_Summary")){
			return false;
		}
		//check if its in the summary
		Matcher matcher = pbb_summary.matcher(page.getPageContent());
		String pbb = null;
		if (matcher.find()) {
			pbb = matcher.group();
			String pretty_pbb = Sentence.makePrettyText(pbb);
			if(canno.getParagraph_around_link()!=null&&canno.getParagraph_around_link().length()>30)
				if(pretty_pbb.contains(canno.getParagraph_around_link().trim().substring(8, 20))){
					return true;
				}else{
					//	System.out.println(canno.getParagraph_around_link()+"\n\n"+pretty_pbb);
				}
			else{
				//System.out.println(canno.getParagraph_around_link()+"\n\n"+pretty_pbb);
			}
		}

		return false;
	}



}
