package org.gnf.genewiki.lingpipe;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;

import com.aliasi.sentences.IndoEuropeanSentenceModel;
import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.Files;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gnf.genewiki.GeneWikiPage;
import org.gnf.genewiki.GeneWikiUtils;
import org.gnf.genewiki.Reference;
import org.gnf.genewiki.Sentence;

/** Use SentenceModel to find sentence boundaries in text */
public class SentenceSplitter {

	static final TokenizerFactory TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;
	static final SentenceModel SENTENCE_MODEL  = new MedlineSentenceModel(); //IndoEuropeanSentenceModel(); 
	static final SentenceChunker SENTENCE_CHUNKER = new SentenceChunker(TOKENIZER_FACTORY,SENTENCE_MODEL);

//	Pattern refPattern1;
//	Pattern refPattern2;
	Pattern refPattern;
	Pattern citeTemplate;
	Pattern header;
	Pattern references;
	Pattern trustPattern;
	
	public SentenceSplitter(){
		
		//String refPatternReg = "<ref {1,5}name {0,5}=.{0,40}/>|<ref.+?</ref>";//"<ref name=.{0,25}/>|<ref.+?</ref>";
		refPattern = Pattern.compile(Reference.regexP);
//		String reg1 = "<ref[^<]*/>";
//		refPattern1 = Pattern.compile(reg1);
		
//		String reg2 = "<ref[^<]*</ref>";	
//		refPattern2 = Pattern.compile(reg2);
		
		String trustP = "\\{\\{\\#.+?}}";
		trustPattern = Pattern.compile(trustP);
		
		String reg3 = "\\{\\{cite [^\\}]*\\}\\}";
		citeTemplate = Pattern.compile(reg3);
		
		String reg4 = "==.*==";
		header = Pattern.compile(reg4);
		
		String reg5 = "==.{0,5}References.{0,5}==.*";
		references = Pattern.compile(reg5);
	}

	public static void main(String[] args) throws IOException {
		//GeneWikiPage prot = GeneWikiUtils.deserializeGeneWikiPage("/Users/bgood/data/genewiki/intermediate/javaobj/30835"); //1001 has 7
		String title = "DC-SIGN"; 
		GeneWikiPage prot = new GeneWikiPage(title);
		prot.defaultPopulateWikiTrust();	
		String	text = prot.getPageContent();
		test(text);
	}
	
	public static void test (String text){
		String textex = "{{PBB|geneid=1647}}'''Reelin''' is a [[protein]] that helps regulate processes of [[Neural development#Neuronal migration|neuronal migration]] " +
		"and positioning in the developing brain. " +
		"Besides this important role in early development, reelin continues to work in the adult brain. " +
		"It modulates the [[synaptic plasticity]] by enhancing the induction and maintenance of [[long-term potentiation]].<ref name=\"LTP1\"/>" +
		"<ref name=\"LTP2\"/> " +
		"It also stimulates dendrite<ref name=\"Niu_2004\"/> and [[dendritic spine]]<ref name=\"pmid18842893\"/>" +
		" development and regulates the continuing migration of [[neuroblast]]s generated in [[adult neurogenesis]] sites like " +
		"[[subventricular zone|subventricular]] and [[subgranular zone]]s. " +
		"It is found not only in the [[brain]], but also in the [[spinal cord]], " +
		"[[blood]], and other body organs and tissues. {{PBB|geneid=1647}}'''Growth arrest and DNA-damage-inducible protein GADD45 alpha''' is a [[protein]] that in humans is " +
		"encoded by the ''GADD45A'' [[gene]]." +
		"" +
		"<ref name=\"pmid1990262\">{{cite journal | author = Papathanasiou MA, Kerr NC, Robbins JH, " +
		"McBride OW, Alamo I Jr, Barrett SF, Hickson ID, Fornace AJ Jr | title = Induction by ionizing radiation of the gadd45 gene " +
		"in cultured human cells: lack of mediation by protein kinase C | journal = Mol Cell Biol | volume = 11 | issue = 2 | pages = " +
		"1009�16 | year = 1991 | month = Mar | pmid = 1990262 | pmc = 359769 | doi =  }}</ref>" +
		"" +
		"<ref name=\"pmid8226988\">{{cite journal |" +
		" author = Hollander MC, Alamo I, Jackman J, Wang MG, McBride OW, Fornace AJ Jr | title = Analysis of the mammalian gadd45 gene " +
		"and its response to DNA damage | journal = J Biol Chem | volume = 268 | issue = 32 | pages = 24385�93 | year = 1993 | month = " +
		"Dec | pmid = 8226988 | pmc =  | doi =  }}</ref>" +
		"" +
		"<ref name=\"entrez\">{{cite web | title = Entrez Gene: GADD45A growth arrest and" +
		" DNA-damage-inducible, alpha| url = http://www.ncbi.nlm.nih.gov/sites/entrez?Db=gene&Cmd=ShowDetailView&TermToSearch=1647| " +
		"accessdate = }}</ref>" +
		"'''Growth arrest and DNA-damage-inducible protein GADD45 alpha''' is a [[protein]] that in humans is " +
		"encoded by the ''GADD45A'' [[gene]]." +
		"" +
		"";


	//	text = "There is evidence that the change is selective, and DNMT1 is overexpressed in reelin-secreting GABAegric neurons but not in their glutamatergic neighbours.<ref name=\"DNMT_inhibition_GAD67_Reelin_2004\"/> [[Methylation]] inhibitors and [[histone deacetylase]] inhibitors, such as [[valproic acid]], increase reelin mRNA levels,<ref name =\"valpro\"/> while L-methionine treatment downregulates the phenotypic expression of reelin.";
	// text = "A potential inhibitor of the Hedgehog signaling pathway has been found and dubbed 'Robotnikinin', in honor of Sonic the Hedgehog's nemesis, [[Doctor Ivo Robotnik|Dr. Ivo \"Eggman\" Robotnik]].<ref> cite web | url=http://www.lifescientist.com.au/article/273516/robotnikinin_takes_sonic_hedgehog?fp=4&fpid=1013 | title=Robotnikinin takes on Sonic hedgehog - robotnikinin, Sonic hedgehog - Australian Life Scientist | publisher=www.lifescientist.com.au | accessdate=2009-09-21 | last= | first= </ref> Some clinicians and scientists criticize giving genes frivolous, whimsical, or quirky names, calling it inappropriate that patients with \"a serious illness or [[disability]] are told that they or their child have a [[mutation]] in a gene such as ''Sonic hedgehog''.\" [[Zbtb7]], a gene which was originally named \"[[Pok�mon]]\" [[Pikachurin]], a retinal protein named after [[Pikachu]] [[Cyclopia]] ";
		
		
		SentenceSplitter s = new SentenceSplitter();
		List<Sentence> chunks = s.splitWikiSentences(text);
		int i = 1;
		for (Sentence sentence : chunks) {
			System.out.println("SENTENCE "+(i++)+":");
			System.out.println(sentence.getStartIndex()+"-"+sentence.getStopIndex()+" "+sentence.getNextStartIndex()+" ---- "+sentence.getPrettyText());
		}
	}

	/***
	 * Use this to split wiki pages into sentence chunks.  
	 * In-line references are replaced with empty spaces to preserve the length of the text and to enable the LingPipe sentence splitter to work.
	 * 
	 * Note..  this completely fails when presented with horrendous wikitext used for reference formatting.  Hence we use maskWiki to clean these up first.
	 */

	public List<Sentence> splitWikiSentences(String text){
		String wikitext = new String(maskWiki(text));
		Chunking chunking = SENTENCE_CHUNKER.chunk(wikitext.toCharArray(),0,wikitext.length());
		Set<Chunk> sentences = chunking.chunkSet();
		if (sentences.size() < 1) {
			return null;
		}
		List<Sentence> s = new ArrayList<Sentence>();
		String slice = chunking.charSequence().toString();
		Sentence previous = null;
		for (Iterator<Chunk> it = sentences.iterator(); it.hasNext(); ) {
			Chunk sentence = it.next();
			int start = sentence.start();
			int end = sentence.end();
			String t = slice.substring(start,end);

			Sentence sent = new Sentence();
			sent.setStartIndex(start); 
			sent.setStopIndex(end); 
			sent.setText(text.substring(start,end));
			sent.setText(t);
			
			if(previous!=null){
				previous.setNextStartIndex(start);
			}
			if(it.hasNext()==false){
				sent.setNextStartIndex(-1);
			}
			if(keepSentence(t)){
				s.add(sent);
			}
			previous = sent;

		}
		return s;
	}

	public boolean keepSentence(String sentence){
		String test = new String(sentence);
		if(test!=null){
			test = Sentence.makePrettyText(test);
			if(test.length()>10){
				return true;
			}
		}
		return false;
	}

	
	public String maskWiki(String input){
		if(input==null){
			return "";
		}
		input = zapBelowRefs(input);
		input = trust2spaces(input);
		input = refs2spaces(input);
		return input;
	}

	public static String zapBelowRefs(String input){
		String output = new String(input);
		int index = output.lastIndexOf("References");
		if(index>0){
			output = replaceStringStretch(output,index,output.length(),' ');
		}
		return output;
	}

	/***
	 * finds wikiformatted references in text and replaces them with blank spaces
	 * @param input
	 * @return
	 */
	public String refs2spaces(String input){
		String output = new String(input.replaceAll("\\*", " "));
		Matcher matcher = refPattern.matcher(input);
		while (matcher.find()) {
			output = replaceStringStretch(output, matcher.start(), matcher.end(), ' ');
		}
		input = output;

//		matcher = refPattern2.matcher(input);
//		while (matcher.find()) {
//			String match = matcher.group();
//			output = replaceStringStretch(output, matcher.start(), matcher.end(), ' ');
//		}
		
		input = output;	
		matcher = header.matcher(input);
		while (matcher.find()) {
			output = replaceStringStretch(output, matcher.start(), matcher.end(), ' ');
		}
		output = output.replaceAll("\\}", " ");
		output = output.replaceAll("\\{", " ");

		return output;
	}

	/***
	 * finds trust annotations
	 * @param input
	 * @return
	 */
	public String trust2spaces(String input){
		String output = new String(input);
		Matcher matcher = trustPattern.matcher(input);
		while (matcher.find()) {
			output = replaceStringStretch(output, matcher.start(), matcher.end(), ' ');
		}
		input = output;
		return output;
	}
	
	/***
	 * Replace a particular set of characters, by index, with the replaceWith character
	 * @param input
	 * @param start
	 * @param stop
	 * @param replaceWith
	 * @return
	 */
	public static String replaceStringStretch(String input, int start, int stop, char replaceWith){
		if(start>input.length()||stop>input.length()){
			return input;
		}
		char[] out = input.toCharArray();
		for(int i=start; i<stop; i++){
			out[i] = replaceWith;
		}		
		String output = new String(out);
		return output;
	}

}