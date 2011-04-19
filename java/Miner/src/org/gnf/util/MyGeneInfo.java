/**
 * 
 */
package org.gnf.util;

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
import org.gnf.genewiki.Config;
import org.gnf.genewiki.GeneWikiUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
		Gene g = getGeneInfoByGeneid("100270941", false);
		System.out.println(g);
		
		//	List<String> genes = new ArrayList<String>();
	//	genes.add("30682"); genes.add("1017");
	//	System.out.println(getBatchGeneInfo(genes, true));

		//		String f = "gw_data/gene_wiki_index.txt";
		//		Map<String, String> gene_wiki = GeneWikiUtils.getGeneWikiGeneIndex(f, false);
		//		try {
		//			FileWriter o = new FileWriter("gw_data/gene_symbol_wiki.txt");
		//			int n = 0;
		//			for(Entry<String, String> gw : gene_wiki.entrySet()){
		//				o.write(gw.getKey()+"\t"+getSymbolByGeneid(gw.getKey(), false)+"\t"+gw.getValue()+"\n");
		//				n++;
		//				System.out.println(n+"\t"+gw.getKey()+"\t"+getSymbolByGeneid(gw.getKey(), false)+"\t"+gw.getValue());
		//			}
		//			o.close();
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
	}


	public static Gene getGeneInfoByGeneid(String id, boolean external) throws UnsupportedEncodingException{
		Gene g = null;
		String symbol = "";
		String jsonr = getGeneInfo(id, external,"name,symbol,type_of_gene");
		if(jsonr==null||jsonr.length()==0||jsonr.startsWith("<html><title>404")){
			return null;
		}
		//System.out.println(jsonr);
		try {
			JSONObject r = new JSONObject(jsonr);
			symbol = r.getString("symbol");
			g = new Gene();
			g.setGeneID(id);
			g.setGeneSymbol(symbol);
			g.setGeneDescription(r.getString("name"));
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
		GetMethod get = new GetMethod("http://cwu-stage/query?q="+idtype+":"+encoded+"+AND+species:human");
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

	public static Map<String, Gene> getBatchGeneInfo(Collection<String> geneidss, boolean external) throws UnsupportedEncodingException{
		if(geneidss==null){
			return null;
		}
		List<String> geneids = new ArrayList<String>(geneidss);
		
		Map<String, Gene> genes = new HashMap<String, Gene>();
		int index = 0;
		String out = "";
		for(int g=0;g<geneids.size();g+=500){
			String batch = "";

			//http://mygene.info/boc/?query=1017+1018&scope=entrezgene,retired&format=json
			for(int i=g; i<geneids.size()&&i<g+500; i++){
				batch+=geneids.get(i)+" ";
			}
			String encoded = URLEncoder.encode(batch, "UTF8");
			//http://mygene.info/gene/117
			String u = "http://cwu-stage/boc/?query=";
			if(external){
				u = "http://mygene.info/boc/?query=";
			}
			String url = u+encoded+"&scope=entrezgene,retired&format=json";
			GetMethod get = new GetMethod(url);
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
				if(out!=null&&(!out.startsWith("<html"))){
					JSONArray r = new JSONArray(out);
					for(int j=0; j<r.length(); j++){
						JSONObject o = r.getJSONObject(j);
						String name = o.getString("name");
						String entrezgene = o.getString("id");
						String symbol = o.getString("symbol");
						//not supported in batch mode
						//String t = o.getString("type_of_gene");
						Gene gene = new Gene();
						gene.setGeneID(entrezgene);
						gene.setGeneSymbol(symbol);
						gene.setGeneDescription(name);
						genes.put(entrezgene, gene);
//						if(t!=null){
//							if(t.equals("pseudo")){
//								gene.setPseudo(true);
//							}
//							gene.setGenetype(t);
//						}
					}
				}
				//	System.out.println(out);
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
				get.releaseConnection();
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
		String u = "http://cwu-stage/gene/";
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
