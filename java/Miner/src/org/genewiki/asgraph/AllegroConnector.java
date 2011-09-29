package org.genewiki.asgraph;

import java.io.IOException;

//import com.franz.agbase.AllegroGraph;
//import com.franz.agbase.AllegroGraphConnection;
//import com.franz.agbase.AllegroGraphException;
//import com.franz.agjena.AllegroGraphGraphMaker;
//import com.franz.agjena.AllegroGraphModel;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;

public class AllegroConnector {

	private Model model;
//	private AllegroGraphConnection conn;
//	private AllegroGraph agStore;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		 	
	}
//
//	/**
//	 * Get a connection for a stored allegroGraph model 
//	 * @param nameOfHomeDir (e.g. "C:/Users/bgood/WikiGeneRDF/AlzGenesUmls/")
//	 * @return
//	 */
//	public void setAllegroGraphModel(String nameOfHomeDir){
//		conn = new AllegroGraphConnection();
//		try {
//			conn.enable();
//			agStore = conn.access(nameOfHomeDir, null);  
//			AllegroGraphGraphMaker maker = new AllegroGraphGraphMaker(agStore);  
//			Graph graph = maker.getGraph();
//			model = new AllegroGraphModel(graph); 
//				
//		} catch (AllegroGraphException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}  
//	}
//	
//	public void closeEverything() throws AllegroGraphException{
//		if(model!=null){
//			model.close();
//			agStore.closeTripleStore();
//			conn.disable();
//		}
//	}
//
//	public Model getModel() {
//		return model;
//	}
//
//	public AllegroGraphConnection getConn() {
//		return conn;
//	}
//
//	public AllegroGraph getAgStore() {
//		return agStore;
//	}
//	
	
}
