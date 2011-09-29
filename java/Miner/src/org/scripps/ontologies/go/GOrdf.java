package org.scripps.ontologies.go;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class GOrdf {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Model go = ModelFactory.createDefaultModel();
		String gordf = "file:///C:/Users/bgood/data/ontologies/go_daily-termdb.xml";
		go.read(gordf);
		System.out.println("read go as rdf");
		System.out.println("# triples = "+go.size());
		go.close();
	}

	
	
}
