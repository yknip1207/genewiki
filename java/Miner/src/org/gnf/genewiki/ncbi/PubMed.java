package org.gnf.genewiki.ncbi;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.axis2.AxisFault;
import org.gnf.util.MapFun;

import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.AbstractTextType;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.MedlineCitationType;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.MeshHeadingListType;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.MeshHeadingType;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.PubmedArticleSet;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.PubmedArticleSetChoiceE;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.PubmedArticleSet_type0;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub.DocSumType;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub.ItemType;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub; 
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.AbstractType; 
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub;
/*
PubDate: 1986 Apr
EPubDate: null
Source: Proc Natl Acad Sci U S A
AuthorList: null
LastAuthor: Putnam FW
Title: Amino acid sequence of human plasma alpha 1B-glycoprotein: homology to the immunoglobulin supergene family.
Volume: 83
Issue: 8
Pages: 2363-7
LangList: null
NlmUniqueID: 7505876
ISSN: 0027-8424
ESSN: 1091-6490
PubTypeList: null
RecordStatus: PubMed - indexed for MEDLINE
PubStatus: ppublish
ArticleIds: null
History: null
References: null
HasAbstract: 1
PmcRefCount: 11
FullJournalName: Proceedings of the National Academy of Sciences of the United States of America
ELocationID: null
SO: 1986 Apr;83(8):2363-7
 */
public class PubMed {

	public static void main(String[] args) {
		//		Map<String, Set<String>> pmid_meshes = getCachedPmidMeshMap("/Users/bgood/data/bioinfo/pmidmeshcache.txt");
		//		System.out.println(pmid_meshes.get("3458201"));

		try {
			Set<String> pmids = new HashSet<String>();
			pmids.add("3458201");
			pmids.add("2465923");
			pmids.add("11259612");
			Map<String, Set<String>> pmid_meshs = getMeshTerms(pmids);
			System.out.println(pmid_meshs.get("3458201"));
			System.out.println(pmid_meshs.get("2465923"));
			System.out.println(pmid_meshs.get("11259612"));
			//			boolean cache = false;
			//			Map<String, String> pmid_texts = getPubmedTitlesAndAbstracts(pmids,"/tmp/tmp", cache);
			//			for(Entry<String, String> pmid_text : pmid_texts.entrySet()){
			//				System.out.println(pmid_text.getKey()+"\t"+pmid_text.getValue());
			//			}

		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param pubcache
	 * @return
	 */

	public static Map<String, String> getCachedPmidAbstractMap(String pubcache){
		Map<String, String> pmid_texts = new HashMap<String, String>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(pubcache));
			String line = f.readLine();
			while(line!=null){
				String[] row = line.split("\t");
				if(row.length==2){
					pmid_texts.put(row[0], row[1]);
				}else if (row.length > 2){
					pmid_texts.put(row[0], row[1]+"\t"+row[2]);
				}
				line = f.readLine();
			}
			f.close();
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pmid_texts;
	}

	public static Map<String, Set<String>> getCachedPmidMeshMap(String pubmeshcache){
		Map<String, Set<String>> pmid_meshs = new HashMap<String, Set<String>>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(pubmeshcache));
			String line = f.readLine();
			while(line!=null){
				String[] row = line.split("\t");
				String pmid = row[0];
				String[] meshes = row[1].split("::");
				Set<String> mshset = new HashSet<String>();
				for(String m : meshes){
					mshset.add(m);
				}
				pmid_meshs.put(pmid, mshset);
				line = f.readLine();
			}
			f.close();
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pmid_meshs;
	}

	public static Map<String, Set<String>> getMeshTerms(Set<String> idlist) throws AxisFault{
		if(idlist==null){
			return null;
		}
		Map<String, Set<String>> pmid_meshs = new HashMap<String, Set<String>>();
		// call NCBI E utility
		EFetchPubmedServiceStub service = new EFetchPubmedServiceStub();
		//Call NCBI EFetch Utilities 
		EFetchPubmedServiceStub.EFetchRequest req = new EFetchPubmedServiceStub.EFetchRequest();
		int c = 0;
		String ids = "";
		for(String id : idlist){
			ids+=id+",";
		}
		ids = ids.substring(0,ids.length()-1);
		c++;
		req.setId(ids);

		EFetchPubmedServiceStub.EFetchResult res;

		try {
			res = service.run_eFetch(req);
			// results output
			if(res==null){
				for(String id : idlist){
					pmid_meshs.put(id, null);
				}
				return pmid_meshs;
			}

			PubmedArticleSet_type0 articles = res.getPubmedArticleSet();
			String id = null;
			if(articles==null||articles.getPubmedArticleSetChoice()==null){
				for(String id1 : idlist){
					pmid_meshs.put(id1, null);
				}
				return pmid_meshs;
			}
			for(PubmedArticleSetChoiceE article : articles.getPubmedArticleSetChoice()){
				Set<String> meshes = new HashSet<String>();
				try{
					MedlineCitationType cite = article.getPubmedArticle().getMedlineCitation();
					id = cite.getPMID().getString();
					MeshHeadingListType mhlt = cite.getMeshHeadingList();
					if(id!=null&&mhlt!=null){
						for(MeshHeadingType mht : mhlt.getMeshHeading()){
							String term = mht.getDescriptorName().getString();
							meshes.add(term);
						}
						pmid_meshs.put(id, meshes);
					}
				}catch(NullPointerException e){
					System.out.println("No MeSH found for pmid "+id);
				}
			}
		} catch (RemoteException e) {
			System.out.println("error on "+ids);
			e.printStackTrace();
		} 		
		if(c%10==0){
			System.out.println("on "+c+" of "+idlist.size());
		}
		return pmid_meshs;
	}

	public static Map<String, String> getPubmedTitlesAndAbstracts(Set<String> idlist, String pubcache, boolean cache) throws AxisFault{
		if(idlist==null){
			return null;
		}
		Map<String, String> pmid_text = new HashMap<String, String>();
		if(cache){
			pmid_text = getCachedPmidAbstractMap(pubcache);
		}
		System.out.println("Cache contains: "+pmid_text.keySet().size());
		idlist.removeAll(pmid_text.keySet());
		System.out.println("Retrieving new: "+idlist.size());
		// call NCBI E utility
		EFetchPubmedServiceStub service = new EFetchPubmedServiceStub();
		//Call NCBI EFetch Utilities 
		EFetchPubmedServiceStub.EFetchRequest req = new EFetchPubmedServiceStub.EFetchRequest();
		int c = 0;
		for(String id : idlist){
			c++;
			String output = "";
			req.setId(id);

			EFetchPubmedServiceStub.EFetchResult res;
			try {
				res = service.run_eFetch(req);
				// results output
				if(res==null){
					pmid_text.put(id, "no data");
					continue;
				}
				String title = res.getPubmedArticleSet().getPubmedArticleSetChoice()[0].getPubmedArticle().getMedlineCitation().getArticle().getArticleTitle().getString();
				AbstractType abstractTypeObj = res.getPubmedArticleSet().getPubmedArticleSetChoice()[0].getPubmedArticle().getMedlineCitation().getArticle().getAbstract();
				String abstractText = "";
				if (abstractTypeObj != null) { 
					AbstractTextType[] abtexts = abstractTypeObj.getAbstractText();
					if(abtexts!=null){
						for(AbstractTextType ab : abtexts){
							abstractText+=ab.getString();
						}
					}
				}

				//				String title = res.getPubmedArticleSet().getPubmedArticle()[0].getMedlineCitation().getArticle().getArticleTitle();
				//				AbstractType abstractTypeObj = res.getPubmedArticleSet().getPubmedArticle()[0].getMedlineCitation().getArticle().getAbstract();
				//				String abstractText = "";
				//				if (abstractTypeObj != null) { 
				//					abstractText = abstractTypeObj.getAbstractText();
				//				}
				output+=title+"\t"+abstractText;
				pmid_text.put(id, output);
				if(cache){
					FileWriter f = new FileWriter(pubcache, true);
					f.write(id+"\t"+output+"\n");
					f.close();
				}
			} catch (RemoteException e) {
				System.out.println("error on "+id);
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			if(c%10==0){
				System.out.println("on "+c+" of "+idlist.size());
			}
		}
		return pmid_text;
	}



	public static Map<String, String> getPubmedArticleTypes(String idlist){
		if(idlist==null){
			return null;
		}
		Map<String, String> journal = new HashMap<String, String>();
		String info = ""; 

		EUtilsServiceStub service = null;
		try {
			service = new EUtilsServiceStub();
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// call NCBI ESummary utility
		EUtilsServiceStub.ESummaryRequest req = new EUtilsServiceStub.ESummaryRequest();
		req.setDb("pubmed");
		req.setId(idlist);
		EUtilsServiceStub.ESummaryResult res;
		try {
			res = service.run_eSummary(req);

			// results output
			if(res==null){
				return(journal);
			}
			DocSumType[] docs = res.getDocSum();
			if(docs!=null){
				for(int i=0; i<docs.length; i++)
				{
					//System.out.println("ID: "+res.getDocSum()[i].getId());
					String pmid = res.getDocSum()[i].getId();
					for (int k = 0; k < res.getDocSum()[i].getItem().length; k++)
					{
						//uncomment to see all the available fields
						//	System.out.println("    " + res.getDocSum()[i].getItem()[k].getName()+": " + res.getDocSum()[i].getItem()[k].getItemContent());
						//						if(res.getDocSum()[i].getItem()[k].getName().equals("PubDate")){
						//							String date = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"
						//							dates.put(pmid, date);
						//						}

						if(res.getDocSum()[i].getItem()[k].getName().equals("PubTypeList")){
							if(res.getDocSum()[i].getItem()[k]!=null){
								ItemType[] items = res.getDocSum()[i].getItem()[k].getItem();
								if(items!=null){
									for(ItemType item : items){
										journal.put(pmid, item.getItemContent());
									}
								}
							}
						}
					}
					info+="\t";
				}
			}
		} catch (RemoteException e) {
			System.out.println(idlist);
			e.printStackTrace();
		}		


		return journal;
	}

	public static Map<String, String> getPubmedJournals(String idlist){
		if(idlist==null){
			return null;
		}
		Map<String, String> journal = new HashMap<String, String>();
		String info = ""; 

		EUtilsServiceStub service = null;
		try {
			service = new EUtilsServiceStub();
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// call NCBI ESummary utility
		EUtilsServiceStub.ESummaryRequest req = new EUtilsServiceStub.ESummaryRequest();
		req.setDb("pubmed");
		req.setId(idlist);
		EUtilsServiceStub.ESummaryResult res;
		try {
			res = service.run_eSummary(req);

			// results output
			if(res==null){
				return(journal);
			}
			DocSumType[] docs = res.getDocSum();
			if(docs!=null){
				for(int i=0; i<docs.length; i++)
				{
					//System.out.println("ID: "+res.getDocSum()[i].getId());
					String pmid = res.getDocSum()[i].getId();
					for (int k = 0; k < res.getDocSum()[i].getItem().length; k++)
					{
						//uncomment to see all the available fields
						//System.out.println("    " + res.getDocSum()[i].getItem()[k].getName()+": " + res.getDocSum()[i].getItem()[k].getItemContent());
						if(res.getDocSum()[i].getItem()[k].getName().equals("FullJournalName")){
							List<String> auths = new ArrayList<String>();
							if(res.getDocSum()[i].getItem()[k]!=null){
								journal.put(pmid, res.getDocSum()[i].getItem()[k].getItemContent());
							}
						}
					}
					info+="\t";
				}
			}
		} catch (RemoteException e) {
			System.out.println(idlist);
			e.printStackTrace();
		}		


		return journal;
	}

	public static Map<String, Set<String>> getPubmedAuthors(String idlist){
		if(idlist==null){
			return null;
		}
		Map<String, Set<String>> authors = new HashMap<String, Set<String>>();
		String info = ""; 

		EUtilsServiceStub service = null;
		try {
			service = new EUtilsServiceStub();
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// call NCBI ESummary utility
		EUtilsServiceStub.ESummaryRequest req = new EUtilsServiceStub.ESummaryRequest();
		req.setDb("pubmed");
		req.setId(idlist);
		EUtilsServiceStub.ESummaryResult res;
		try {
			res = service.run_eSummary(req);

			// results output
			if(res==null){
				return(authors);
			}
			if(res.getDocSum()==null){
				return authors;
			}
			for(int i=0; i<res.getDocSum().length; i++)
			{
				//System.out.println("ID: "+res.getDocSum()[i].getId());
				String pmid = res.getDocSum()[i].getId();
				for (int k = 0; k < res.getDocSum()[i].getItem().length; k++)
				{
					//uncomment to see all the available fields
					//System.out.println("    " + res.getDocSum()[i].getItem()[k].getName()+": " + res.getDocSum()[i].getItem()[k].getItemContent());
					if(res.getDocSum()[i].getItem()[k].getName().equals("AuthorList")){
					Set<String> auths = new HashSet<String>();
						if(res.getDocSum()[i].getItem()[k]!=null){
							ItemType[] items = res.getDocSum()[i].getItem()[k].getItem();
							if(items!=null){
								for(int a = 0; a<items.length; a++){
									auths.add(items[a].getItemContent());
								}
								//							for(ItemType it : res.getDocSum()[i].getItem()[k].getItem()){
								//								auths.add(it.getItemContent());
								//							}
								authors.put(pmid, auths);
							}
						}
						//String date = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"
						//authors.put(pmid, date);
					}
				}
				info+="\t";
			}
		} catch (RemoteException e) {
			System.out.println("died sending about "+idlist.length()/9+" pmids");
			e.printStackTrace();
		}		


		return authors;
	}

	/**
	 * return a map linking pubmed id to date published	
	 * @param idlist
	 * @return
	 */
	public static Map<String, String> getPubmedDates(String idlist){
		if(idlist==null){
			return null;
		}
		Map<String, String> dates = new HashMap<String, String>();
		String info = ""; 

		EUtilsServiceStub service = null;
		try {
			service = new EUtilsServiceStub();
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// call NCBI ESummary utility
		EUtilsServiceStub.ESummaryRequest req = new EUtilsServiceStub.ESummaryRequest();
		req.setDb("pubmed");
		req.setId(idlist);
		EUtilsServiceStub.ESummaryResult res;
		try {
			res = service.run_eSummary(req);

			// results output
			for(int i=0; i<res.getDocSum().length; i++)
			{
				//System.out.println("ID: "+res.getDocSum()[i].getId());
				String pmid = res.getDocSum()[i].getId();
				for (int k = 0; k < res.getDocSum()[i].getItem().length; k++)
				{
					//uncomment to see all the available fields
					System.out.println("    " + res.getDocSum()[i].getItem()[k].getName()+": " + res.getDocSum()[i].getItem()[k].getItemContent());
					if(res.getDocSum()[i].getItem()[k].getName().equals("PubDate")){
						String date = res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"
						dates.put(pmid, date);
					}
				}
				info+="\t";
			}
		} catch (RemoteException e) {
			System.out.println(idlist);
			e.printStackTrace();
		}		


		return dates;
	}

	public static String getPubmedDate(String id){
		String info = ""; 
		try
		{
			EUtilsServiceStub service = new EUtilsServiceStub();
			// call NCBI ESummary utility
			EUtilsServiceStub.ESummaryRequest req = new EUtilsServiceStub.ESummaryRequest();
			req.setDb("pubmed");
			req.setId(id);
			EUtilsServiceStub.ESummaryResult res = service.run_eSummary(req);
			// results output
			for(int i=0; i<res.getDocSum().length; i++)
			{
				//       System.out.println("ID: "+res.getDocSum()[i].getId());
				for (int k = 0; k < res.getDocSum()[i].getItem().length; k++)
				{
					//uncomment to see all the available fields
					//System.out.println("    " + res.getDocSum()[i].getItem()[k].getName()+": " + res.getDocSum()[i].getItem()[k].getItemContent());
					if(res.getDocSum()[i].getItem()[k].getName().equals("PubDate")){
						info += res.getDocSum()[i].getItem()[k].getItemContent()+" "; //"\t"
					}
				}
				info+="\t";
			}
		}

		catch(Exception e) { System.out.println(e.toString()); }
		return info;
	}

	public static Map<String, Set<String>> getMeshTerms(String pmid) throws AxisFault {
		Set<String> pmids = new HashSet<String>();
		pmids.add(pmid);
		return getMeshTerms(pmids);
	}

	public static Map<String, Set<String>> getPubmedAuthors(Set<String> pmids) {
		String plist = "";
		int max_batch = 500;
		int c = 0;
		Map<String, Set<String>> pmid_authors = new HashMap<String, Set<String>>();
		if(pmids!=null&&pmids.size()>0){
			for(String pmid : pmids){
				plist+=pmid+",";
				c++;
				if(c==max_batch||c==pmids.size()){
					System.out.println("Getting author names for, batch size = "+c);
					plist = plist.substring(0, plist.length()-1);
					Map<String, Set<String>> tmp_authors = getPubmedAuthors(plist);
					pmid_authors = MapFun.mergeStringSetMaps(pmid_authors, tmp_authors);
					plist = "";
					c =0;
				}
			}
		}

		return pmid_authors;
	}

	/**
	         try
        {
            EFetchSequenceServiceStub service = new EFetchSequenceServiceStub();
            // call NCBI EFetch utility
            EFetchSequenceServiceStub.EFetchRequest req = new EFetchSequenceServiceStub.EFetchRequest();
            req.setDb("nuccore");
            req.setId(fetchIds);
            EFetchSequenceServiceStub.EFetchResult res = service.run_eFetch(req);
            // results output
            for (int i = 0; i < res.getGBSet().getGBSetSequence().length; i++)
            {
                EFetchSequenceServiceStub.GBSeq_type0 obj = res.getGBSet().getGBSetSequence()[i].getGBSeq();
                System.out.println("Organism: " + obj.getGBSeq_organism());
                System.out.println("Locus: " + obj.getGBSeq_locus());
                System.out.println("Definition: " + obj.getGBSeq_definition());
                System.out.println("------------------------------------------");
            }
        }
        catch (Exception e) { System.out.println(e.toString()); }
	 */
	/*
				 String db = "", searchTerm = "", retMax = "", relDate = "", minDate = "", maxDate = "", dateType = ""; String[] idListArray = null; String id = "", title = "", abstractText = ""; int numLiteratureId = 0;
		try {
			EUtilsServiceStub service1 = new EUtilsServiceStub();
			// call NCBI ESearch utility // NOTE: search term should be URL encoded 
			EUtilsServiceStub.ESearchRequest req1 = new EUtilsServiceStub.ESearchRequest();
			//Set parameters 
			req1.setDb(db); req1.setTerm(searchTerm); 
			req1.setDatetype(retMax); req1.setReldate(relDate); req1.setMindate(minDate); req1.setMaxdate(maxDate); req1.setDatetype(dateType);
			//Run ESearch 
			EUtilsServiceStub.ESearchResult res1 = service1.run_eSearch(req1);
			// results output
			idListArray = res1.getIdList().getId();
			if (idListArray != null) {
				EFetchPubmedServiceStub service2 = new EFetchPubmedServiceStub();
				//Call NCBI EFetch Utilities 
				EFetchPubmedServiceStub.EFetchRequest req2 = new EFetchPubmedServiceStub.EFetchRequest();
				//Get the number of returned Pubmed IDs 
				numLiteratureId = idListArray.length;
				System.out.println("NUmber of returned literatures = " + numLiteratureId);
				for (int i = 0; i < numLiteratureId; i++) {
					//Allow the thread to sleep for 3 seconds before //fetch literature. This is an attempt not to overloading //NCBI server. 
					try {
						Thread.sleep(3000); 
					} catch (InterruptedException e) { }
					id = idListArray[i]; System.out.println("Pubmed ID: " + id);
					//Set Pubmed ID 
					req2.setId(id);
					//Run EFetch 
					EFetchPubmedServiceStub.EFetchResult res2 = service2.run_eFetch(req2);
					//Print out result 
					int numPubmedArticleSet = res2.getPubmedArticleSet().getPubmedArticle().length; 
					System.out.println("Number of pubmed article sets = " + numPubmedArticleSet);
					//String pubmedId=res.getPubmedArticleSet().getPubmedArticle()[0].getMedlineCitation().getPMID();
					title = res2.getPubmedArticleSet().getPubmedArticle()[0].getMedlineCitation().getArticle().getArticleTitle();
					AbstractType abstractTypeObj = res2.getPubmedArticleSet().getPubmedArticle()[0].getMedlineCitation().getArticle().getAbstract();


					if (abstractTypeObj != null) { abstractText = abstractTypeObj.getAbstractText();
					} 
					System.out.println(title); System.out.println(abstractText); System.out.println();
				}
			}
		}catch (Exception e){}
	 */

}
