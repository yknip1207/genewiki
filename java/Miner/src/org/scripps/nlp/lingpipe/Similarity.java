/**
 * 
 */
package org.scripps.nlp.lingpipe;

import com.aliasi.spell.EditDistance;
import com.aliasi.spell.JaccardDistance;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Proximity;
import com.aliasi.util.Distance;

/**
 * @author bgood
 *
 */
public class Similarity {

	static final Distance<CharSequence> D1  = new EditDistance(false);
	Distance<CharSequence> jaccard; 
	TokenizerFactory tokenizerFactory;
	
	public Similarity(){
		tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
		jaccard = new JaccardDistance(tokenizerFactory);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String s1 = "I am a happy cat";
		String s2 = "I am happy cat because I am fat";
		System.out.println(D1.distance(s1,s2));
		Similarity s = new Similarity();
		System.out.println(s.jaccard.distance(s1,s2));
	}

}
