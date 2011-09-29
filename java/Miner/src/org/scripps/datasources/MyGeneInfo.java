/**
 * 
 */
package org.scripps.datasources;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.genewiki.GeneWikiUtils;
import org.genewiki.annotationmining.Config;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scripps.util.Gene;
import org.scripps.util.HttpUtil;

/**
 * @author bgood
 *
 */
public class MyGeneInfo {

	/**
	 * @param args
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws UnsupportedEncodingException {
		Gene g = getGeneInfoByGeneid("2989", true);
		System.out.println(g);

	}


	public static Gene getGeneInfoByGeneid(String id, boolean external) throws UnsupportedEncodingException{
		Gene g = null;
		String symbol = "";
		String jsonr = getGeneInfo(id, external,"name,symbol,type_of_gene,uniprot");
		if(jsonr==null||jsonr.length()==0||jsonr.startsWith("<html><title>404")){
			return null;
		}
		//System.out.println(jsonr);
		try {
			JSONObject r = new JSONObject(jsonr);

			g = new Gene();
			g.setGeneID(id);
			if(r.has("symbol")){
				symbol = r.getString("symbol");
				g.setGeneSymbol(symbol);
			}
			g.setGeneDescription(r.getString("name"));
			g.setUniprot("none");
			if(r.has("uniprot")){
				JSONObject u = new JSONObject(r.getString("uniprot"));
				if(u!=null&&u.has("Swiss-Prot")){
					String uni = u.getString("Swiss-Prot");
					if(uni.startsWith("[")){
						g.setUniprot("multi");
					}else{
						g.setUniprot(uni);
					}
				}
			}
			String t = r.getString("type_of_gene");
			if(t!=null){
				if(t.equals("pseudo")){
					g.setPseudo(true);
				}
				g.setGenetype(t);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			System.err.println("unparseable jsonr for "+id+"\n"+jsonr);
			e.printStackTrace();
		}
		return g;
	}

	/**
	 * 
	 * @param id
	 * @param external
	 * @return
	 * @throws UnsupportedEncodingException
	 */


	public static String getSymbolByGeneid(String id, boolean external) throws UnsupportedEncodingException{
		String symbol = "";
		String jsonr = getGeneInfo(id, external, "symbol");
		if(jsonr==null||jsonr.length()==0||jsonr.startsWith("<html><title>404")){
			return null;
		}
		//System.out.println(jsonr);
		try {
			JSONObject r = new JSONObject(jsonr);
			symbol = r.getString("symbol");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			System.err.println("unparseable jsonr for "+id+"\n"+jsonr);
			e.printStackTrace();
		}
		return symbol;
	}

	public static Map<String, String> mapUniprot2NCBIGene(String id) throws UnsupportedEncodingException{
		Map<String, String> mapped = new HashMap<String, String>();
		String jsonr = queryHumanGeneLocalDev(id, "uniprot");
		//System.out.println(jsonr);
		try {
			JSONObject r = new JSONObject(jsonr);
			if(r==null){
				return null;
			}
			JSONArray rows = (JSONArray) r.get("rows");
			if(rows== null){
				return null;
			}
			for(int i=0; i<rows.length(); i++){
				JSONObject job = (JSONObject) rows.get(i);
				String gene = job.getString("id");

				JSONObject f = (JSONObject)job.get("fields");
				JSONObject fields = (JSONObject)f.get("value");
				String symbol = fields.getString("symbol");
				String name = fields.getString("name");
				int taxid = fields.getInt("taxid");

				mapped.put(gene, symbol+"\t"+name+"\t"+taxid);

			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mapped;
	}

	public static Set<String> mapGeneSymbol2NCBIGene(String id) throws UnsupportedEncodingException{
		Set<String> mapped = new HashSet<String>();
		String jsonr = queryHumanGeneLocalDev(id, "symbol");
		if(jsonr.startsWith("<html><title>404")){
			return null;
		}
		try {
			JSONObject r = new JSONObject(jsonr);
			if(r==null){
				return null;
			}
			JSONArray rows = (JSONArray) r.get("rows");
			if(rows== null){
				return null;
			}
			for(int i=0; i<rows.length(); i++){
				JSONObject job = (JSONObject) rows.get(i);
				mapped.add(job.getString("id"));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mapped;
	}

	public static String queryHumanGeneLocalDev(String geneid, String idtype) throws UnsupportedEncodingException{
		String out = "";
		//http://mygene.info/query?q=cdk2+AND+species:human
		String encoded = URLEncoder.encode(geneid, "UTF8");
		GetMethod get = new GetMethod("http://cwudev/query?q="+idtype+":"+encoded+"+AND+species:human");
		// Get HTTP client
		HttpClient httpclient = new HttpClient();
		// Execute request
		try {
			int result = httpclient.executeMethod(get);
			// Display status code
			//	System.out.println("Response status code: " + result);
			// Display response
			//	System.out.println("Response body: ");
			//	out = get.getResponseBodyAsString();
			//	System.out.println(out);
			InputStream s = get.getResponseBodyAsStream(); 
			out = HttpUtil.convertStreamToString(s);

		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// Release current connection to the connection pool once you are done
			get.releaseConnection();
		}
		return out;
	}

	/**
	 * Batch request for MyGeneinfo - note that you can change the values returned with the filter parameter (hard coded at the moment)
	 * See http://mygene.info/doc/anno_service for available properties.
	 * @param geneidss
	 * @param external
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static Map<String, Gene> getBatchGeneInfo(Collection<String> geneidss, boolean external) throws UnsupportedEncodingException{
		if(geneidss==null){
			return null;
		}
		//make non-redundant list
		List<String> geneids = new ArrayList<String>(new HashSet<String>(geneidss));

		Map<String, Gene> genes = new HashMap<String, Gene>();
		int batchsize = 1000;
		String out = "";
		for(int g=0;g<geneids.size();g+=batchsize){
			Set<String> current_set = new HashSet<String>();
			//get the gene set for this batch
			String batch = "";
			for(int i=g; i<geneids.size()&&i<g+batchsize; i++){
				batch+=geneids.get(i)+",";
				current_set.add(geneids.get(i));
			}
			if(current_set.size()>0){
				batch = batch.substring(0, batch.length()-1);
			}
			//prepare the request for this set
			String u = "http://cwudev/gene";
			if(external){
				u = "http://mygene.info/gene";
			}
			PostMethod post = new PostMethod(u);
			post.addParameter("ids", batch);
			post.addParameter("filter","name,id,symbol,type_of_gene");

			// Get HTTP client
			HttpClient httpclient = new HttpClient();
			// Execute request
			Set<String> response_set = new HashSet<String>();
			try {
				int result = httpclient.executeMethod(post);				
				out = post.getResponseBodyAsString();
				if(result==200&&out!=null&&(!out.startsWith("<html"))){
					JSONArray r = new JSONArray(out);

					for(int j=0; j<r.length(); j++){
						JSONObject o = r.getJSONObject(j);
						String name = o.getString("name");
						String entrezgene = o.getString("_id");
						String symbol = o.getString("symbol");
						String t = o.getString("type_of_gene");
						Gene gene = new Gene();
						gene.setGeneID(entrezgene);
						gene.setGeneSymbol(symbol);
						gene.setGeneDescription(name);
						gene.setGenetype(t);
						if(t!=null){
							if(t.equals("pseudo")){
								gene.setPseudo(true);
							}
							gene.setGenetype(t);
						}
						genes.put(entrezgene, gene);
						response_set.add(entrezgene);
					}
				}else{
					System.out.println(out);
				}
				if(response_set.size()!=current_set.size()){

					System.out.println("MyGeneInfo returned "+response_set.size()+" results for "+current_set.size()+" queried gene ids, missing ids:");
					current_set.removeAll(response_set);
					System.out.println(current_set);
				}
			} catch (HttpException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				// Release current connection to the connection pool once you are done
				post.releaseConnection();
			}
		}
		return genes;
	}

	public static String getGeneInfo(String geneid, boolean external, String filter) throws UnsupportedEncodingException{
		if(geneid==null){
			return null;
		}
		String out = "";
		//http://mygene.info/query?q=cdk2+AND+species:human
		String encoded = URLEncoder.encode(geneid, "UTF8");
		//http://mygene.info/gene/117
		String u = "http://cwudev/gene/"; //http://cwudev/gene
		if(external){
			u = "http://mygene.info/gene/";
		}
		GetMethod get = new GetMethod(u+encoded+"?filter="+filter);
		// Get HTTP client
		HttpClient httpclient = new HttpClient();
		// Execute request
		try {
			int result = httpclient.executeMethod(get);
			// Display status code
			//	System.out.println("Response status code: " + result);
			// Display response
			//	System.out.println("Response body: ");
			out = get.getResponseBodyAsString();
			//	System.out.println(out);
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// Release current connection to the connection pool once you are done
			get.releaseConnection();
		}
		return out;
	}

	public static String queryHumanGenePublic(String geneid, String idtype) throws UnsupportedEncodingException{
		String out = "";
		//http://mygene.info/query?q=cdk2+AND+species:human
		String encoded = URLEncoder.encode(geneid, "UTF8");
		GetMethod get = new GetMethod("http://mygene.info/query?q="+idtype+":"+encoded+"+AND+species:human");
		// Get HTTP client
		HttpClient httpclient = new HttpClient();
		// Execute request
		try {
			int result = httpclient.executeMethod(get);
			// Display status code
			//	System.out.println("Response status code: " + result);
			// Display response
			//	System.out.println("Response body: ");
			out = get.getResponseBodyAsString();
			//	System.out.println(out);
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// Release current connection to the connection pool once you are done
			get.releaseConnection();
		}
		return out;
	}

	public static String queryHumanGene(String geneid) throws UnsupportedEncodingException{
		String out = "";
		//http://mygene.info/query?q=cdk2+AND+species:human
		String encoded = URLEncoder.encode(geneid, "UTF8");
		GetMethod get = new GetMethod("http://mygene.info/query?q="+encoded+"+AND+species:human");
		//		NameValuePair[] data = {
		//				new NameValuePair("q", geneid+"+AND+species:human"),
		//		};
		// Request content will be retrieved directly
		// from the input stream
		//post.setRequestBody(data);
		// Get HTTP client
		HttpClient httpclient = new HttpClient();
		// Execute request
		try {
			int result = httpclient.executeMethod(get);
			// Display status code
			//	System.out.println("Response status code: " + result);
			// Display response
			//	System.out.println("Response body: ");
			out = get.getResponseBodyAsString();
			//	System.out.println(out);
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// Release current connection to the connection pool once you are done
			get.releaseConnection();
		}
		return out;
	}

	public static Set<String> mapGeneAlias2NCBIGene(String id) throws UnsupportedEncodingException{
		Set<String> mapped = new HashSet<String>();
		String jsonr = queryHumanGeneLocalDev(id, "alias");
		//System.out.println(jsonr);
		try {
			JSONObject r = new JSONObject(jsonr);
			if(r==null){
				return null;
			}
			JSONArray rows = (JSONArray) r.get("rows");
			if(rows== null){
				return null;
			}
			for(int i=0; i<rows.length(); i++){
				JSONObject job = (JSONObject) rows.get(i);
				mapped.add(job.getString("id"));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mapped;
	}
}
